package com.vanet.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Security;
import java.util.Base64;
import java.util.logging.Logger;

@Service
public class PQCEncryptionService {

    private static final Logger log = 
        Logger.getLogger(PQCEncryptionService.class.getName());

    private KeyPair keyPair;
    private SecretKey aesKey;

    public PQCEncryptionService() {
        Security.addProvider(new BouncyCastleProvider());
        try {
            initializeKeys();
        } catch (Exception e) {
            log.severe("Error initializing PQC keys: " + e.getMessage());
            throw new RuntimeException("Failed to initialize PQC keys", e);
        }
    }

    private void initializeKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        this.keyPair = kpg.generateKeyPair();

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        this.aesKey = keyGen.generateKey();

        log.info("PQC Encryption Keys Initialized");
        log.info("RSA Key Pair: 2048 bits");
        log.info("AES Key: 256 bits");
    }

    public EncryptedPotholeData encryptPotholeData(String plainData) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encryptedData = cipher.doFinal(plainData.getBytes());

            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            byte[] encryptedAESKey = rsaCipher.doFinal(aesKey.getEncoded());

            String ciphertext = Base64.getEncoder().encodeToString(encryptedData);
            String encapsulatedKey = Base64.getEncoder().encodeToString(encryptedAESKey);

            log.info("Data encrypted with AES-256-RSA Hybrid");

            return new EncryptedPotholeData(
                ciphertext,
                encapsulatedKey,
                "AES-256-RSA-Hybrid",
                System.currentTimeMillis()
            );

        } catch (Exception e) {
            log.severe("Encryption failed: " + e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decryptPotholeData(EncryptedPotholeData encryptedData) {
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedAESKey = rsaCipher.doFinal(
                Base64.getDecoder().decode(encryptedData.encapsulatedKey)
            );

            SecretKey aesKey = new SecretKeySpec(
                decryptedAESKey, 0, decryptedAESKey.length, "AES"
            );

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] decryptedData = cipher.doFinal(
                Base64.getDecoder().decode(encryptedData.ciphertext)
            );

            log.info("Data decrypted successfully");
            return new String(decryptedData);

        } catch (Exception e) {
            log.severe("Decryption failed: " + e.getMessage());
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public String getPublicKeyForBlockchain() {
        PublicKey publicKey = keyPair.getPublic();
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static class EncryptedPotholeData {
        public String ciphertext;
        public String encapsulatedKey;
        public String algorithm;
        public Long timestamp;

        public EncryptedPotholeData(String ciphertext, String encapsulatedKey,
                                    String algorithm, Long timestamp) {
            this.ciphertext = ciphertext;
            this.encapsulatedKey = encapsulatedKey;
            this.algorithm = algorithm;
            this.timestamp = timestamp;
        }
    }
}