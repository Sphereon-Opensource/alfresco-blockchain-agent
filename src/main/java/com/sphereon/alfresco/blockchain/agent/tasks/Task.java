package com.sphereon.alfresco.blockchain.agent.tasks;

public interface Task<R> {
    R execute();
}
