package util;

public class TotpVerifier {


    //boolean isValidCode(byte[] key, String userOtp)

   // Steps:

    // Get the current Unix time (in seconds)

    // Divide by 30 → gives the time step

    // Convert that time step into an 8-byte array

    // Compute HMAC-SHA1 of that array using the secret key

    // Extract a 6-digit code from the HMAC (this is tricky — see RFC 4226, §5.4)

    // Compare to userOtp (allow for ±1 time window if desired)


}
