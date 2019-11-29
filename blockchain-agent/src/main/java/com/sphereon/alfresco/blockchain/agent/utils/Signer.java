package com.sphereon.alfresco.blockchain.agent.utils;

import com.sphereon.libs.blockchain.commons.Utils;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Component
public class Signer {
    private final String certificatePassword;
    private final String certificatePathString;
    private final String certificateAlias;

    private KeyPair keyPair;

    public Signer(@Value("${sphereon.blockchain.agent.cert-password}") final String certificatePassword,
                  @Value("${sphereon.blockchain.agent.cert-path}") final String certificatePathString,
                  @Value("${sphereon.blockchain.agent.cert-alias}") final String certificateAlias) {
        this.certificatePassword = certificatePassword;
        this.certificatePathString = certificatePathString;
        this.certificateAlias = certificateAlias;
    }

    @PostConstruct
    public void init() {
        Assert.notNull(certificatePathString, "No certificate path received. See README for details on configuration.");
        Assert.notNull(certificatePassword, "No certificate password received. See README for details on configuration.");

        try {
            final var keystore = KeyStore.getInstance("PKCS12");
            final char[] password = certificatePassword.toCharArray();
            final InputStream certificateStream = getCertificateStream();
            keystore.load(certificateStream, password);

            final var privateKey = (PrivateKey) keystore.getKey(certificateAlias, password);
            final var certificate = (X509Certificate) keystore.getCertificate(certificateAlias);
            final var publicKey = certificate.getPublicKey();
            this.keyPair = new KeyPair(publicKey, privateKey);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | IOException | CertificateException exception) {
            throw new RuntimeException("Could not load keystore " + certificatePathString, exception);
        }
    }

    private InputStream getCertificateStream() throws FileNotFoundException {
        final var certificatePath = ResourceUtils.getFile(certificatePathString).toPath();

        if (!Files.exists(certificatePath)) {
            throw new RuntimeException("Certificate " + certificatePathString + " could not be found.");
        }

        try {
            final byte[] certificateContent = Files.readAllBytes(certificatePath);
            if (Base64.isBase64(certificateContent)) {
                return new ByteArrayInputStream(Base64.decodeBase64(certificateContent));
            }
            return new ByteArrayInputStream(certificateContent);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not open certificate " + certificatePathString, e);
        }
    }

    public String sign(final byte[] data) {
        try {
            final var signature = Signature.getInstance("SHA512withRSA");
            signature.initSign(keyPair.getPrivate());
            signature.update(data);
            return Utils.Hex.encodeAsString(signature.sign());
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException exception) {
            throw new RuntimeException("An error occurred whilst signing the content hash", exception);
        }
    }
}
