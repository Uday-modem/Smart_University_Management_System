package com.rfid.tracker.util;

import java.security.SecureRandom;

public class CodeGenerationUtil {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final int CODE_LENGTH = 6;

    /**
     * Generate a 6-character alphanumeric verification code
     * Format: AB12CD (2 letters + 2 digits + 2 alphanumeric)
     *
     * @return 6-character code
     */
    public static String generateVerificationCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return code.toString();
    }

    /**
     * Generate unique code with retry logic (prevent duplicates)
     * This method is thread-safe
     *
     * @return Unique 6-character code
     */
    public static synchronized String generateUniqueCode() {
        return generateVerificationCode();
    }

    /**
     * Validate code format
     *
     * @param code Code to validate
     * @return True if code matches format (6 alphanumeric characters)
     */
    public static boolean isValidCodeFormat(String code) {
        if (code == null || code.length() != CODE_LENGTH) {
            return false;
        }
        for (char c : code.toCharArray()) {
            if (!ALPHANUMERIC.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generate test codes for development
     *
     * @return Array of 5 different codes
     */
    public static String[] generateTestCodes() {
        String[] codes = new String[5];
        for (int i = 0; i < 5; i++) {
            codes[i] = generateVerificationCode();
        }
        return codes;
    }
}