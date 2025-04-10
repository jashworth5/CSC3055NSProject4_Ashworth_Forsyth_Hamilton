package src.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TotpVerifier {
    private static final int DIGITS = 6;
    private static final int TIME_STEP = 30; // seconds
    private static final int TIME_SKEW = 1; // allow for 1 time step skew

    public static boolean isValidCode(byte[] key, String userOtp) {
        userOtp = userOtp.replaceAll("\\s+", ""); // Remove all whitespace

        if (userOtp.length() != DIGITS || !userOtp.matches("\\d+")) {
            return false; // Invalid OTP length
        }

        try {
            long currentTimeMillies = System.currentTimeMillis();
            long currentTime = currentTimeMillies / 1000 / TIME_STEP; // Convert to seconds

            for (int i = -TIME_SKEW; i <= TIME_SKEW; i++) {
                String generatedOtp = generateTOTP(key, currentTime + i, DIGITS);
                if (generatedOtp.equals(userOtp)) {
                    return true; // OTP is valid
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Error occurred
        }
    }

    /**
     * Generates a TOTP code for a specific time step
     * 
     * @param key The secret TOTP key (raw bytes)
     * @param timeStep The time step to generate the code for
     * @param digits The number of digits in the OTP
     * @return The generated OTP code
     */
    public static String generateTOTP(byte[] key, long timeStep, int digits) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array();

        SecretKeySpec signkey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signkey);

        byte[] hash = mac.doFinal(timeBytes);

        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24) | ((hash[offset + 1] & 0xFF) << 16) | ((hash[offset + 2] & 0xFF) << 8) | (hash[offset + 3] & 0xFF);

        int modulo = (int) Math.pow(10, digits);
        String code = Integer.toString(binary % modulo);

        while(code.length() < digits) {
            code = "0" + code; // Pad with leading zeros
        }
        return code;
    }

     /**
     * Convenience method to generate the current TOTP code for a given key
     * Used for testing or displaying in a client app
     * 
     * @param key The secret TOTP key (raw bytes)
     * @return The current TOTP code
     */
    public static String getCurrentCode(byte[] key) throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        long currentTimeStep = currentTimeMillis / 1000 / TIME_STEP;
        return generateTOTP(key, currentTimeStep, DIGITS);
    }
}