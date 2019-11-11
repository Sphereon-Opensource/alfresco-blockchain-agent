package com.sphereon.alfresco.blockchain.agent.backend.commands.controllers;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Validator;

public abstract class AsyncCommand<E> extends Command<E> {
    private E result;
    private SettableFuture<E> futureResult = SettableFuture.create();

    @Autowired
    private Validator validator;

    public ListenableFuture<E> executeAsync() {
        executeCommandAsync();
        return futureResult;
    }

    public E executeCommand() {
        try {
            executeCommandAsync();
            return getResult();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    protected abstract void executeCommandAsync();

    protected void setFuture(ListenableFuture<E> future) {
        futureResult.setFuture(future);
    }

    public E getResult() {
        if (result == null) {
            SettableFuture<E> futureResult = getFutureResult();
            if (futureResult != null) {
                try {
                    return futureResult.get();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        } else {
            return result;
        }
        return null;
    }

    public void setResult(E result) {
        this.result = result;
        futureResult.set(result);
    }

    public void setException(Throwable throwable) {
        futureResult.setException(throwable);
    }

    public SettableFuture<E> getFutureResult() {
        return futureResult;
    }

    public void setFutureResult(SettableFuture<E> futureResult) {
        this.futureResult = futureResult;
    }
}
