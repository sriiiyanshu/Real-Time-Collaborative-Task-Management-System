package com.taskmanager.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utility class for encryption operations and password hashing.
 */
public class EncryptionUtil {

    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final byte[] SECRET_KEY = "taskmanager_secure_key_bytes".getBytes(StandardCharsets.UTF_8);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Hashes a password with a salt using PBKDF2.
     * 
     * @param password The password to hash
     * @param salt The salt for the hash
     * @return The hashed password
     * @throws NoSuchAlgorithmException If the algorithm is not available
     * @throws InvalidKeySpecException If the key specification is invalid
     */
    public static String hashPassword(String password, byte[] salt) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(HASH_ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Generates a random salt for password hashing.
     * 
     * @return A random salt
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Verifies a password against a hash.
     * 
     * @param password The password to verify
     * @param hash The hash to check against
     * @param salt The salt used for the hash
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String hash, byte[] salt) {
        try {
            String calculatedHash = hashPassword(password, salt);
            return calculatedHash.equals(hash);
        } catch (Exception e) {
            LogUtil.error("Error verifying password", e);
            return false;
        }
    }

    /**
     * Encrypts a string using AES encryption.
     * 
     * @param plainText The text to encrypt
     * @return The encrypted text as a Base64 string
     */
    public static String encrypt(String plainText) {
        try {
            byte[] iv = new byte[16];
            SECURE_RANDOM.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(new String(SECRET_KEY).toCharArray(), iv, ITERATIONS, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] combinedData = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combinedData, 0, iv.length);
            System.arraycopy(encrypted, 0, combinedData, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combinedData);
        } catch (Exception e) {
            LogUtil.error("Error encrypting data", e);
            return null;
        }
    }

    /**
     * Decrypts a Base64 encoded, AES encrypted string.
     * 
     * @param encryptedText The encrypted text
     * @return The decrypted text
     */
    public static String decrypt(String encryptedText) {
        try {
            byte[] combinedData = Base64.getDecoder().decode(encryptedText);
            byte[] iv = new byte[16];
            byte[] encrypted = new byte[combinedData.length - iv.length];

            System.arraycopy(combinedData, 0, iv, 0, iv.length);
            System.arraycopy(combinedData, iv.length, encrypted, 0, encrypted.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(new String(SECRET_KEY).toCharArray(), iv, ITERATIONS, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LogUtil.error("Error decrypting data", e);
            return null;
        }
    }
    
    /**
     * Generates an MD5 hash (for non-security purposes like file checksums).
     * 
     * @param input The input to hash
     * @return The MD5 hash as a hex string
     */
    public static String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LogUtil.error("Error generating MD5", e);
            return null;
        }
    }
}