package com.sphereon.alfresco.blockchain.agent.backend.tasks;

public interface Task<R> {
    R execute();
}
