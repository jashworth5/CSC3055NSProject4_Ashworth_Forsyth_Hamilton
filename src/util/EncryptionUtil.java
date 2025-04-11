package util;

import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // SCRYPT parameters from the project
    private static final int SCRYPT_N = 16384;
    private static final int SCRYPT_R = 8;
    private static final int SCRYPT_P = 1;
    private static final int KEY_LENGTH = 16; // 128 bits

    public static byte[] generateRandomBytes(int length) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    public static byte[] scryptHash(String password, byte[] salt) {
        return SCrypt.generate(password.getBytes(), salt, SCRYPT_N, SCRYPT_R, SCRYPT_P, KEY_LENGTH);
    }

    public static String[] generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ElGamal", "BC");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        PublicKey pub = pair.getPublic();
        PrivateKey priv = pair.getPrivate();
        return new String[]{
            Base64.getEncoder().encodeToString(pub.getEncoded()),
            Base64.getEncoder().encodeToString(priv.getEncoded())
        };
    }

    public static String[] encryptMessage(String message, String base64PublicKey) throws Exception {
        // Generate AES key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey aesKey = keyGen.generateKey();

        // Encrypt message with AES-GCM
        byte[] iv = generateRandomBytes(12);
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] ciphertext = aesCipher.doFinal(message.getBytes());

        // Encrypt AES key using recipient's public key (ElGamal)
        byte[] decodedPub = Base64.getDecoder().decode(base64PublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("ElGamal", "BC");
        PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedPub));

        Cipher elgamal = Cipher.getInstance("ElGamal/None/PKCS1Padding", "BC");
        elgamal.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] wrappedKey = elgamal.doFinal(aesKey.getEncoded());

        return new String[]{
            Base64.getEncoder().encodeToString(ciphertext),
            Base64.getEncoder().encodeToString(wrappedKey),
            Base64.getEncoder().encodeToString(iv)
        };
    }

    public static String decryptMessage(String ciphertextB64, String wrappedKeyB64, String ivB64, String base64PrivateKey) throws Exception {
        // Decode everything
        byte[] ciphertext = Base64.getDecoder().decode(ciphertextB64);
        byte[] wrappedKey = Base64.getDecoder().decode(wrappedKeyB64);
        byte[] iv = Base64.getDecoder().decode(ivB64);
        byte[] decodedPriv = Base64.getDecoder().decode(base64PrivateKey);

        // Decrypt AES key with ElGamal private key
        KeyFactory keyFactory = KeyFactory.getInstance("ElGamal", "BC");
        PrivateKey privKey = keyFactory.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(decodedPriv));

        Cipher elgamal = Cipher.getInstance("ElGamal/None/PKCS1Padding", "BC");
        elgamal.init(Cipher.DECRYPT_MODE, privKey);
        byte[] aesKeyBytes = elgamal.doFinal(wrappedKey);

        SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        // Decrypt AES-GCM message
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] plaintext = aesCipher.doFinal(ciphertext);

        return new String(plaintext);
    }
}