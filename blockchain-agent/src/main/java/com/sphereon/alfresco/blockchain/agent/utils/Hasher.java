package com.sphereon.alfresco.blockchain.agent.utils;

import com.sphereon.libs.blockchain.commons.Digest;

public class Hasher {
    public static byte[] hash(final byte[] content, final Digest.Algorithm hashAlgorithm) {
        return Digest.getInstance().getHashAsHex(hashAlgorithm, content);
    }
}
