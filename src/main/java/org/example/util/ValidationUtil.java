package org.example.util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation across the application.
 */
public class ValidationUtil {

    // ─── Regex patterns ───────────────────────────────────────────────────────
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9\\s\\-().]{7,20}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{3,50}$");

    private static final Pattern FULL_NAME_PATTERN =
            Pattern.compile("^[A-Za-zÀ-ÿ\\s.'-]{2,100}$");

    // ─── Field validators ─────────────────────────────────────────────────────

    public static String validateUsername(String username) {
        if (username == null || username.trim().isEmpty())
            return "Username is required.";
        if (!USERNAME_PATTERN.matcher(username.trim()).matches())
            return "Username must be 3–50 characters (letters, digits, underscore only).";
        return null; // valid
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty())
            return "Password is required.";
        if (password.length() < 6)
            return "Password must be at least 6 characters.";
        if (password.length() > 100)
            return "Password must not exceed 100 characters.";
        return null;
    }

    public static String validateFullName(String name) {
        if (name == null || name.trim().isEmpty())
            return "Full name is required.";
        if (!FULL_NAME_PATTERN.matcher(name.trim()).matches())
            return "Full name must be 2–100 characters (letters and spaces only).";
        return null;
    }

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty())
            return null; // email is optional
        if (!EMAIL_PATTERN.matcher(email.trim()).matches())
            return "Invalid email format (e.g. name@example.com).";
        return null;
    }

    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty())
            return null; // phone is optional
        if (!PHONE_PATTERN.matcher(phone.trim()).matches())
            return "Invalid phone number (7–20 digits, may include +, -, spaces).";
        return null;
    }

    public static String validateRole(String role) {
        if (role == null || role.trim().isEmpty())
            return "Role is required.";
        if (!role.equals("PATIENT") && !role.equals("PSYCHOLOGUE"))
            return "Role must be PATIENT or PSYCHOLOGUE.";
        return null;
    }

    public static String validateMaxAppointments(int value) {
        if (value < 1)
            return "Maximum appointments must be at least 1.";
        if (value > 20)
            return "Maximum appointments cannot exceed 20.";
        return null;
    }

    // ─── Convenience builder ──────────────────────────────────────────────────

    /**
     * Collects all registration-form errors into a single message.
     * Returns null if everything is valid.
     */
    public static String validateRegistration(String username, String password,
                                               String fullName, String email,
                                               String phone, String role) {
        StringBuilder errors = new StringBuilder();

        appendIfError(errors, validateUsername(username));
        appendIfError(errors, validatePassword(password));
        appendIfError(errors, validateFullName(fullName));
        appendIfError(errors, validateEmail(email));
        appendIfError(errors, validatePhone(phone));
        appendIfError(errors, validateRole(role));

        return errors.length() == 0 ? null : errors.toString().trim();
    }

    private static void appendIfError(StringBuilder sb, String error) {
        if (error != null) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("• ").append(error);
        }
    }
}
