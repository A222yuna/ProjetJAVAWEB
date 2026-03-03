package org.example.util;

import com.sendgrid.*;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

public class NotificationService {

    // initialize Twilio when the class is loaded if configuration is present
    static {
        if (TwilioConfig.isSmsConfigured()) {
            Twilio.init(TwilioConfig.ACCOUNT_SID, TwilioConfig.AUTH_TOKEN);
        }
    }

    private static void sendSms(String to, String body) {
        if (!TwilioConfig.isSmsConfigured()) {
            System.out.println("[Notification] SMS not configured, skipping");
            return;
        }
        try {
            Message.creator(
                    new com.twilio.type.PhoneNumber(to),
                    new com.twilio.type.PhoneNumber(TwilioConfig.FROM_PHONE),
                    body
            ).create();
            System.out.println("[Notification] SMS sent to " + to + ": " + body);
        } catch (Exception e) {
            System.out.println("[Notification] SMS error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void sendEmail(String to, String subject, String body) {
        if (!TwilioConfig.isEmailConfigured()) {
            System.out.println("[Notification] Email not configured, skipping");
            return;
        }
        Email from = new Email(TwilioConfig.FROM_EMAIL, TwilioConfig.FROM_NAME);
        Email toEmail = new Email(to);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, toEmail, content);
        SendGrid sg = new SendGrid(TwilioConfig.SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("[Notification] Email sent to " + to + " status=" + response.getStatusCode());
        } catch (Exception e) {
            System.out.println("[Notification] Email error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String buildMessage(String patientName, String day, String period, String psychName, String prefix) {
        return prefix + " " + patientName + ", your appointment with " + psychName + " is scheduled for "
                + day + " (" + period + ").";
    }

    public static void notifyAppointmentBooked(String patientName, String email, String phone,
            String day, String period, String psychName) {
        String text = buildMessage(patientName, day, period, psychName, "Booked:");
        if (phone != null && !phone.isEmpty()) {
            sendSms(phone, text);
        }
        if (email != null && !email.isEmpty()) {
            sendEmail(email, "Appointment Booked", text);
        }
    }

    public static void notifyAppointmentCancelled(String patientName, String email, String phone,
            String day, String period, String psychName) {
        String text = buildMessage(patientName, day, period, psychName, "Cancelled:");
        if (phone != null && !phone.isEmpty()) {
            sendSms(phone, text);
        }
        if (email != null && !email.isEmpty()) {
            sendEmail(email, "Appointment Cancelled", text);
        }
    }

    public static void notifyAppointmentCompleted(String patientName, String email, String phone,
            String day, String period, String psychName) {
        String text = buildMessage(patientName, day, period, psychName, "Completed:");
        if (phone != null && !phone.isEmpty()) {
            sendSms(phone, text);
        }
        if (email != null && !email.isEmpty()) {
            sendEmail(email, "Appointment Completed", text);
        }
    }

    private NotificationService() {
        // utility class
    }
}
