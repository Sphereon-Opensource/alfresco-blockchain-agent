package com.sphereon.alfresco.blockchain.agent.backend.commands.certficate;

import com.sphereon.libs.blockchain.commons.Utils;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;

@Component
public class Signer {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Signer.class);

    @Value("${BLOCKCHAIN_CERT_PASSWORD:#{null}}")
    private String certificatePassword;

    @Value("${BLOCKCHAIN_CERT_PATH:#{null}}")
    private String certificatePathString;

    @Value("${BLOCKCHAIN_CERT_ALIAS:#{null}}")
    private String certificateAlias;

    private KeyPair keyPair;

    @PostConstruct
    public void init() {
        Assert.notNull(certificatePathString, "The environment variable BLOCKCHAIN_CERT_PATH is not set.");
        Assert.notNull(certificatePassword, "The environment variable BLOCKCHAIN_CERT_PASSWORD is not set.");

        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            char[] password = certificatePassword.toCharArray();
            InputStream certificateStream = getCertificateStream();
            keystore.load(certificateStream, password);
            final var privateKey = (PrivateKey) keystore.getKey(certificateAlias, password);
            final var certificate = (X509Certificate) keystore.getCertificate(certificateAlias);
            final var publicKey = certificate.getPublicKey();
            this.keyPair = new KeyPair(publicKey, privateKey);
        } catch (Throwable throwable) {
            throw new RuntimeException("Could not load keystore " + certificatePathString, throwable);
        }
    }

    private InputStream getCertificateStream() {
        var certificatePath = Paths.get(certificatePathString);
        if (Files.exists(certificatePath)) {
            try {
                byte[] certificateContent = Files.readAllBytes(certificatePath);
                boolean isBase64 = false;
                try {
                    isBase64 = Base64.isBase64(certificateContent);
                } catch (Throwable ignored) {
                }
                if (isBase64) {
                    return new ByteArrayInputStream(Base64.decodeBase64(certificateContent));
                } else {
                    return new ByteArrayInputStream(certificateContent);
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not open certificate " + certificatePathString, e);
            }
        }
        throw new RuntimeException("Certificate " + certificatePathString + " could not be found.");
    }

    public String sign(byte[] data) {
        try {
            final var signature = Signature.getInstance("SHA512withRSA");
            signature.initSign(keyPair.getPrivate());
            signature.update(data);
            return Utils.Hex.encodeAsString(signature.sign());
        } catch (Throwable throwable) {
            throw new RuntimeException("An error occurred whilst signing the content hash", throwable);

        }
    }
}
