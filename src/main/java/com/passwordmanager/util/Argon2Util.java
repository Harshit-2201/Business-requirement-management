package com.passwordmanager.util;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for Argon2id Key Derivation and Verification.
 * Used for deriving 256-bit symmetric encryption keys from master passwords
 * and generating zero-knowledge verification tokens.
 */
@Component
public class Argon2Util {

    private static final int DEFAULT_ITERATIONS = 3;
    private static final int DEFAULT_MEMORY_KB = 65536; // 64 MB
    private static final int DEFAULT_PARALLELISM = 1;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32; // 256 bits

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a cryptographically secure random base64-encoded salt.
     */
    public String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Derive a 256-bit key from password and salt using Argon2id.
     *
     * @param password Raw master password or secret
     * @param base64Salt Base64 encoded salt
     * @return Base64 encoded 256-bit derived key
     */
    public String deriveKey(String password, String base64Salt) {
        byte[] salt = Base64.getDecoder().decode(base64Salt);
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(DEFAULT_ITERATIONS)
                .withMemoryAsKB(DEFAULT_MEMORY_KB)
                .withParallelism(DEFAULT_PARALLELISM)
                .withSalt(salt);

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());

        byte[] result = new byte[HASH_LENGTH];
        generator.generateBytes(passwordBytes, result, 0, result.length);

        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * Verify if candidate password matches derived hash with provided salt.
     */
    public boolean verify(String password, String base64Salt, String expectedBase64Hash) {
        String derived = deriveKey(password, base64Salt);
        return derived.equals(expectedBase64Hash);
    }
}
