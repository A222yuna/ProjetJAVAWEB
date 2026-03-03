package org.example.util;

/**
 * Twilio credentials configuration.
 *
 * ╔══════════════════════════════════════════════════════════╗ ║ HOW TO FILL
 * THIS FILE ║ ║ 1. Go to https://twilio.com → create a free trial ║ ║ 2.
 * Dashboard → Account Info → copy SID + Auth Token ║ ║ 3. Phone Numbers → Get a
 * free number → copy it ║ ║ 4. For email: twilio.com/sendgrid → Settings → ║ ║
 * API Keys → create one → copy it ║
 * ╚══════════════════════════════════════════════════════════╝
 */
public class TwilioConfig {

    // ── SMS (Twilio) ───────────────────────────────────────────────────────────
    public static final String ACCOUNT_SID = "YOUR_TWILIO_ACCOUNT_SID";   // e.g. ACxxxxxxxxxxxxxxxx
    public static final String AUTH_TOKEN = "YOUR_TWILIO_AUTH_TOKEN";    // e.g. xxxxxxxxxxxxxxxx
    public static final String FROM_PHONE = "YOUR_TWILIO_PHONE_NUMBER";  // e.g. +12015551234

    // ── Email (SendGrid via Twilio) ────────────────────────────────────────────
    public static final String SENDGRID_API_KEY = "YOUR_SENDGRID_API_KEY"; // e.g. SG.xxxxxxxxxxxxxxxx
    public static final String FROM_EMAIL = "no-reply@yourapp.com";  // must be verified in SendGrid
    public static final String FROM_NAME = "Appointment System";

    // ── Helper: are credentials configured? ───────────────────────────────────
    public static boolean isSmsConfigured() {
        return !ACCOUNT_SID.startsWith("YOUR")
                && !AUTH_TOKEN.startsWith("YOUR")
                && !FROM_PHONE.startsWith("YOUR");
    }

    public static boolean isEmailConfigured() {
        return !SENDGRID_API_KEY.startsWith("YOUR")
                && !FROM_EMAIL.startsWith("no-reply@yourapp");
    }

    private TwilioConfig() {
    }
}
