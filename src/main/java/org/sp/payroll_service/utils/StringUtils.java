package org.sp.payroll_service.utils;

import java.util.regex.Pattern;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * String manipulation utilities with validation.
 */
public final class StringUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[1-9]\\d{1,14}$"
    );

    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile("\\d{4}");

    private StringUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if string is null or blank.
     * @param str string to check
     * @return true if null or blank
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Checks if string has text (not null or blank).
     * @param str string to check
     * @return true if has text
     */
    public static boolean hasText(String str) {
        return !isBlank(str);
    }

    /**
     * Validates email format.
     * @param email email to validate
     * @return true if valid email format
     */
    public static boolean isValidEmail(String email) {
        return hasText(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates phone number format.
     * @param phone phone number to validate
     * @return true if valid phone format
     */
    public static boolean isValidPhone(String phone) {
        return hasText(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates employee ID format (4 digits).
     * @param employeeId employee ID to validate
     * @return true if valid employee ID format
     */
    public static boolean isValidEmployeeId(String employeeId) {
        return hasText(employeeId) && EMPLOYEE_ID_PATTERN.matcher(employeeId).matches();
    }

    /**
     * Masks sensitive information in string.
     * @param input input string
     * @param visibleChars number of visible characters at start
     * @param maskChar masking character
     * @return masked string
     */
    public static String mask(String input, int visibleChars, char maskChar) {
        if (isBlank(input) || input.length() <= visibleChars) {
            return input;
        }

        String visible = input.substring(0, visibleChars);
        String masked = String.valueOf(maskChar).repeat(input.length() - visibleChars);
        return visible + masked;
    }

    /**
     * Capitalizes first letter of each word.
     * @param input input string
     * @return title case string
     */
    public static String toTitleCase(String input) {
        if (isBlank(input)) {
            return input;
        }

        return Arrays.stream(input.trim().split("\\s+"))
                .map(word -> {
                    if (word.isEmpty()) return "";
                    return word.substring(0, 1).toUpperCase() +
                            word.substring(1).toLowerCase();
                })
                .collect(Collectors.joining(" "));
    }

    /**
     * Generates random alphanumeric string.
     * @param length desired length
     * @return random string
     */
    public static String generateRandomAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();

        return random.ints(length, 0, chars.length())
                .mapToObj(chars::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}