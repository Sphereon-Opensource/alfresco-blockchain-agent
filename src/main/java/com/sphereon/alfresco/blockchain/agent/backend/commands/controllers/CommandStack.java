package com.sphereon.alfresco.blockchain.agent.backend.commands.controllers;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

@Component
@Scope("prototype")
public class CommandStack {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CommandStack.class);

    private final TaskExecutor pooledTaskExecutor;

    private Stack<Command<?>> executedCommands;
    // TODO: 24-Nov-17 SPMS-171 Make a stack instead of a single exception
    private Throwable lastException;
    private SettableFuture<Void> allSuccessfulResult;
    private final Set<ListenableFuture> futureListeners;
    private final Set<ListenableFuture> succeededListeners;
    private final Set<SettableFuture> exceptionListeners;

    public CommandStack(TaskExecutor pooledTaskExecutor) {
        this.pooledTaskExecutor = pooledTaskExecutor;
        executedCommands = new Stack<>();
        allSuccessfulResult = null;
        futureListeners = new HashSet<>();
        succeededListeners = new HashSet<>();
        exceptionListeners = new HashSet<>();
    }

    public <E> ListenableFuture<E> executeAsync(final Command<E> command) {
        SettableFuture<E> futureResult = SettableFuture.create();
        try {
            checkPreviousException();
            if (command instanceof AsyncCommand) {
                executeAsyncCommand(command, futureResult);
            } else {
                executeInPool(command, futureResult);
            }
        } catch (Throwable t) {
            handleExceptionAndUndo(command, futureResult, t);
        }
        return futureResult;
    }

    private <E> void executeAsyncCommand(Command<E> command, SettableFuture<E> futureResult) {
        ListenableFuture<E> futureCommand = ((AsyncCommand) command).executeAsync();
        Futures.addCallback(futureCommand, new FutureCallback<E>() {
            @Override
            public void onSuccess(E value) {
                executedCommands.push(command);
                futureResult.set(value);
            }

            @Override
            public void onFailure(Throwable throwable) {
                handleExceptionAndUndo(command, futureResult, throwable);
            }
        }, directExecutor());
        onCommandComplete(futureCommand);
    }

    public <E> E execute(Command<E> command) throws Throwable {
        checkPreviousException();
        try {
            E result = command.execute();
            executedCommands.push(command);
            return result;
        } catch (Throwable t) {
            lastException = t;
            undo();
            throw t;
        }
    }

    public void undo() {
        if (!executedCommands.empty()) {
            final Command command = executedCommands.pop();
            command.undo();
        }
    }

    public void undo(int stepNr) {
        if (!executedCommands.empty()) {
            final Command command = executedCommands.elementAt(stepNr);
            command.undo();
        }
    }

    @PreDestroy
    public void reset() {
        executedCommands.clear();
        futureListeners.clear();
        exceptionListeners.clear();
        allSuccessfulResult = null;
        lastException = null;
    }

    public <I, O> ListenableFuture<O> append(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function) {
        return Futures.transformAsync(input, function, MoreExecutors.directExecutor());
    }

    public <I, O> ListenableFuture<O> append(ListenableFuture<I> input, final Command<O> command) {
        return append(input, command, null);
    }

    public <I, O> ListenableFuture<O> append(ListenableFuture<I> input, final Command<O> command, FutureSuccess<? super I> inputSuccessCallback) {
        SettableFuture<O> future = SettableFuture.create();
        Futures.addCallback(input, new FutureCallback<I>() {
            @Override
            public void onSuccess(@Nullable I inputValue) {
                if (inputSuccessCallback != null) {
                    try {
                        inputSuccessCallback.onSuccess(inputValue);
                    } catch (Throwable throwable) {
                        future.setException(throwable);
                        return;
                    }
                }
                if (command instanceof AsyncCommand) {
                    submitAsyncCommand();
                } else {
                    executeInPoolAppend(command, future);
                }
            }

            private void submitAsyncCommand() {
                try {
                    ListenableFuture<O> result = ((AsyncCommand) command).executeAsync();
                    future.setFuture(result);
                } catch (Throwable throwable) {
                    future.setException(throwable);
                }
            }


            @Override
            public void onFailure(Throwable throwable) {
                future.setException(throwable);
                exceptionListeners.forEach(settableFuture -> settableFuture.setException(throwable));
            }
        }, MoreExecutors.directExecutor());
        return future;
    }

    private <E> void executeInPool(Command<E> command, SettableFuture<E> futureResult) {
        onCommandComplete(futureResult);
        pooledTaskExecutor.execute(() -> {
            try {
                futureResult.set(command.execute());
            } catch (Throwable throwable) {
                futureResult.setException(throwable);
            }
        });
    }

    private <O> void executeInPoolAppend(Command<O> command, SettableFuture<O> future) {
        pooledTaskExecutor.execute(() -> {
            try {
                future.set(command.execute());
            } catch (Throwable throwable) {
                future.setException(throwable);
            }
        });
    }

    private void onCommandComplete(final ListenableFuture future) {
        futureListeners.add(future);
        Futures.addCallback(future, new FutureCallback<>() {
            @Override
            public void onSuccess(@Nullable Object o) {
                succeededListeners.add(future);
                if (allSuccessfulResult != null && succeededListeners.size() == futureListeners.size()) {
                    allSuccessfulResult.set(null);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                allSuccessfulResult.setException(throwable);
            }
        }, MoreExecutors.directExecutor());
    }

    private void checkPreviousException() throws Exception {
        if (lastException != null && executedCommands.size() > 0) {
            String lastCommandName = executedCommands.peek().getClass().getName();
            throw new Exception(String.format("A previous commands failed with message '%s', the last executed command was: %s.", lastException.getMessage(), lastCommandName), lastException);
        }
    }

    private void handleExceptionAndUndo(Command<?> command, SettableFuture<?> futureResult, Throwable throwable) {
        lastException = throwable;
        futureResult.setException(throwable);
        exceptionListeners.forEach(settableFuture -> {
            if (settableFuture != futureResult) {
                settableFuture.setException(throwable);
            }
        });
        logger.error(String.format("Command %s failed with message '%s'. Trying to undo.", command.getClass().getName(), throwable.getMessage()), throwable);
        undo();
    }

    public ListenableFuture whenAllSuccessful() {
        allSuccessfulResult = SettableFuture.create();
        return allSuccessfulResult;
    }

    public void whenAllSuccessful(SettableFuture<?> result) {
        Futures.addCallback(whenAllSuccessful(), new FutureCallback<>() {
            @Override
            public void onSuccess(@Nullable Object o) {
                result.set(null);
            }


            @Override
            public void onFailure(Throwable throwable) {
                result.setException(throwable);
            }
        }, MoreExecutors.directExecutor());
    }

    public void whenAllSuccessful(FutureCallback<Void> objectFutureCallback) {
        Futures.addCallback(whenAllSuccessful(), objectFutureCallback, MoreExecutors.directExecutor());
    }

    public void addExceptionListener(SettableFuture<?> future) {
        exceptionListeners.add(future);
    }
}
