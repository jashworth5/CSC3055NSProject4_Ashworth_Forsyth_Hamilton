package src.util;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.ByteBuffer;
import java.security.Security;


public class TotpVerifier {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    // Number of digits in the OTP
    private static final int DIGITS = 6;
    
    // Time step in seconds (30 seconds is standard)
    private static final int TIME_STEP = 30;
    
    // Allow codes within this many time steps (before/after) to account for clock skew
    private static final int TIME_SKEW = 1;
    
    /**
     * Verifies if the provided OTP code is valid for the given secret key
     * 
     * @param key The secret TOTP key (raw bytes)
     * @param userOtp The code provided by the user
     * @return true if the code is valid, false otherwise
     */
    public static boolean isValidCode(byte[] key, String userOtp) {
        // Remove any spaces from the OTP
        userOtp = userOtp.replaceAll("\\s+", "");
        
        // Validate that the OTP has the correct number of digits
        if (userOtp.length() != DIGITS || !userOtp.matches("\\d+")) {
            return false;
        }
        
        try {
            // Get the current timestamp
            long currentTimeMillis = System.currentTimeMillis();
            long currentTimeStep = currentTimeMillis / 1000 / TIME_STEP;
            
            // Check the OTP against the current time step and the allowed time skew
            for (int i = -TIME_SKEW; i <= TIME_SKEW; i++) {
                String generatedOtp = generateTOTP(key, currentTimeStep + i, DIGITS);
                if (generatedOtp.equals(userOtp)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generates a TOTP code for a specific time step using Bouncy Castle
     * 
     * @param key The secret TOTP key (raw bytes)
     * @param timeStep The time step to generate the code for
     * @param digits The number of digits in the OTP
     * @return The generated OTP code
     */
    private static String generateTOTP(byte[] key, long timeStep, int digits) {
        // Convert time step to bytes (RFC 4226 section 5.2)
        byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array();
        
        // Generate the HMAC-SHA1 hash using Bouncy Castle
        HMac hmac = new HMac(new SHA1Digest());
        hmac.init(new KeyParameter(key));
        
        byte[] hmacResult = new byte[hmac.getMacSize()];
        hmac.update(timeBytes, 0, timeBytes.length);
        hmac.doFinal(hmacResult, 0);
        
        // Dynamic truncation (RFC 4226 section 5.3 and 5.4)
        int offset = hmacResult[hmacResult.length - 1] & 0xf;
        int binary = ((hmacResult[offset] & 0x7f) << 24) |
                    ((hmacResult[offset + 1] & 0xff) << 16) |
                    ((hmacResult[offset + 2] & 0xff) << 8) |
                    (hmacResult[offset + 3] & 0xff);
        
        // Generate the code with the appropriate number of digits
        int modulo = (int) Math.pow(10, digits);
        String code = Integer.toString(binary % modulo);
        
        // Pad with leading zeros if necessary
        while (code.length() < digits) {
            code = "0" + code;
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
    public static String getCurrentCode(byte[] key) {
        long currentTimeMillis = System.currentTimeMillis();
        long currentTimeStep = currentTimeMillis / 1000 / TIME_STEP;
        return generateTOTP(key, currentTimeStep, DIGITS);
    }
    
    /**
     * Generate a QR code URL for use with Google Authenticator or similar apps
     * 
     * @param issuer The issuer name (e.g., your app or company name)
     * @param accountName The user's account name or email
     * @param key The TOTP key in base32 format
     * @return A URL that can be turned into a QR code
     */
    public static String getQRCodeUrl(String issuer, String accountName, String key) {
        String encodedIssuer = java.net.URLEncoder.encode(issuer, java.nio.charset.StandardCharsets.UTF_8);
        String encodedAccount = java.net.URLEncoder.encode(accountName, java.nio.charset.StandardCharsets.UTF_8);
        
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
            encodedIssuer, encodedAccount, key, encodedIssuer, DIGITS, TIME_STEP
        );
    }
}