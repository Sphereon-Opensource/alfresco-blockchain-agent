package com.sphereon.alfresco.blockchain.agent.backend.commands.controllers;

import com.google.common.util.concurrent.SettableFuture;

/**
 * Created by Sander on 17-3-2017.
 */

public abstract class Command<E> {

    private E result;
    private SettableFuture<E> futureResult = SettableFuture.create();


    public E execute() {
        return executeCommand();
    }


    protected abstract E executeCommand();


    public abstract void undo();

}
