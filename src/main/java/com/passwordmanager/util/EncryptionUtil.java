package com.passwordmanager.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for AES-256-GCM authenticated symmetric encryption and decryption.
 * Formats encrypted payload as: [12-byte IV] + [Ciphertext + 16-byte GCM Auth Tag]
 * Base64 encoded for safe storage and transmission.
 */
@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits recommended for GCM
    private static final int GCM_TAG_LENGTH = 128; // 128 bit authentication tag

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Encrypt plaintext string using AES-256-GCM with a 256-bit base64-encoded key.
     *
     * @param plaintext Data to encrypt
     * @param base64Key Base64 encoded 256-bit AES key
     * @return Base64 encoded [IV + Ciphertext + Tag]
     */
    public String encrypt(String plaintext, String base64Key) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error during AES-256-GCM encryption: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt AES-256-GCM ciphertext using provided 256-bit base64 key.
     *
     * @param base64EncryptedData Base64 encoded [IV + Ciphertext + Tag]
     * @param base64Key Base64 encoded 256-bit AES key
     * @return Decrypted plaintext string
     */
    public String decrypt(String base64EncryptedData, String base64Key) {
        if (base64EncryptedData == null) {
            return null;
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            byte[] decoded = Base64.getDecoder().decode(base64EncryptedData);

            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] decryptedBytes = cipher.doFinal(cipherText);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error during AES-256-GCM decryption. Key or data may be invalid.", e);
        }
    }

    /**
     * Generate a new cryptographically strong 256-bit AES key (base64 encoded).
     */
    public String generateAes256Key() {
        byte[] key = new byte[32]; // 256 bits
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
