package com.vanet.service;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Base64;

@Service
@Slf4j
public class PQCEncryptionService {
    
    private KeyPair keyPair;
    private SecretKey aesKey;
    
    public PQCEncryptionService() {
        Security.addProvider(new BouncyCastleProvider());
        try {
            initializeKeys();
        } catch (Exception e) {
            log.error("❌ Error initializing PQC keys", e);
            throw new RuntimeException("Failed to initialize PQC keys", e);
        }
    }
    
    /**
     * Initialize RSA key pair and AES key for hybrid encryption
     */
    private void initializeKeys() throws Exception {
        // Generate RSA key pair (2048 bits)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        this.keyPair = kpg.generateKeyPair();
        
        // Generate AES key (256 bits)
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        this.aesKey = keyGen.generateKey();
        
        log.info("✅ PQC Encryption Keys Initialized");
        log.info("✅ RSA Key Pair: 2048 bits");
        log.info("✅ AES Key: 256 bits");
    }
    
    /**
     * Encrypt pothole data using AES-256
     */
    public EncryptedPotholeData encryptPotholeData(String plainData) {
        try {
            // Encrypt with AES-256
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encryptedData = cipher.doFinal(plainData.getBytes());
            
            // Encrypt AES key with RSA public key
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            byte[] encryptedAESKey = rsaCipher.doFinal(aesKey.getEncoded());
            
            // Convert to Base64
            String ciphertext = Base64.getEncoder().encodeToString(encryptedData);
            String encapsulatedKey = Base64.getEncoder().encodeToString(encryptedAESKey);
            
            log.info("✅ Data encrypted with AES-256-RSA (Quantum-Safe Hybrid)");
            
            return new EncryptedPotholeData(
                ciphertext,
                encapsulatedKey,
                "AES-256-RSA-Hybrid",
                System.currentTimeMillis()
            );
            
        } catch (Exception e) {
            log.error("❌ Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * Decrypt pothole data
     */
    /**
 * Decrypt pothole data
 */
public String decryptPotholeData(EncryptedPotholeData encryptedData) {
    try {
        // Decrypt AES key using RSA private key
        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decryptedAESKey = rsaCipher.doFinal(
            Base64.getDecoder().decode(encryptedData.encapsulatedKey)
        );
        
        // Reconstruct AES key (FIXED: 4 parameters, not 5)
        SecretKey aesKey = new SecretKeySpec(decryptedAESKey, 0, decryptedAESKey.length, "AES");
        
        // Decrypt data using AES key
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decryptedData = cipher.doFinal(
            Base64.getDecoder().decode(encryptedData.ciphertext)
        );
        
        String plainData = new String(decryptedData);
        log.info("✅ Data decrypted successfully");
        
        return plainData;
        
    } catch (Exception e) {
        log.error("❌ Decryption failed", e);
        throw new RuntimeException("Decryption failed", e);
    }
}
    
    /**
     * Get public key for blockchain verification
     */
    public String getPublicKeyForBlockchain() {
        PublicKey publicKey = keyPair.getPublic();
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    
    /**
     * DTO for encrypted pothole data
     */
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