package com.sphereon.alfresco.blockchain.agent.factom.config;

import org.blockchain_innovation.factom.client.api.ops.Encoding;

import java.util.List;

import static java.util.Arrays.asList;

public class ExternalIds {
    public static final String HASH_TYPE = "HashType";
    public static final String HASH = "Hash";
    public static final String CHAIN_NAME = "ChainName";

    public static List<String> getExternalIds(final byte[] contentHash) {
        return asList(ExternalIds.HASH, Encoding.UTF_8.encode(contentHash));
    }
}
