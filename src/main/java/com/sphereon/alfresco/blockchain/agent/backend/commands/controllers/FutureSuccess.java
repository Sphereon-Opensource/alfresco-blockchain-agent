package com.sphereon.alfresco.blockchain.agent.backend.commands.controllers;

import com.google.common.annotations.GwtCompatible;
import org.checkerframework.checker.nullness.qual.Nullable;

@GwtCompatible
public interface FutureSuccess<V> {
    void onSuccess(@Nullable V value);
}
