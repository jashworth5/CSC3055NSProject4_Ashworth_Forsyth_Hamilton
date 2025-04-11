package src.util;


import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.util.Base64;


public class CryptoUtils {
    private static final SecureRandom secureRandom = new SecureRandom();

    private static final int SCRYPT_N = 16384; // CPU/memory cost
    private static final int SCRYPT_R = 8;     // Block size
    private static final int SCRYPT_P = 1;     // Parallelization
    private static final int SCRYPT_LENGTH = 32; // Output hash length

    public static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    public static SecretKey generateAESKey(int keySize) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize, secureRandom);
        return keyGenerator.generateKey();
    }


    
}
