package com.sphereon.alfresco.blockchain.agent.backend.commands.controllers;

public interface ExecutionEvent {
    // TODO: SPMS-172 24-Nov-17 Add pre and post hooks
    void commandExecuted();

    void commandUndo();

    void reset();
}
