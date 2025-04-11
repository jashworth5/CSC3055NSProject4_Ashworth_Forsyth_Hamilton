package util;
import java.security.SecureRandom;

/**
 * Utility class for encryption operations.
 */
public class EncryptionUtil {
    private static final SecureRandom secureRandom = new SecureRandom();

    private static final int AES_KEY_SIZE = 256; // AES key size in bits

    // Alias method to match what's expected in Client.java
    public static String[] generateKeyPair() throws Exception {
        return generateElGamelKeyPair();
    }

    public static String[] generateElGamelKeyPair() throws Exception {
        return CryptoUtils.generateElGamalKeyPair();
    }
}

