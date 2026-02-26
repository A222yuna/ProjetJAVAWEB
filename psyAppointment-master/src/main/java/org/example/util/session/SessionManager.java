package org.example.util.session;

import org.example.model.User;

public class SessionManager {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isPsychologue() {
        return isLoggedIn() && currentUser.getRole().equals("PSYCHOLOGUE");
    }

    public static boolean isPatient() {
        return isLoggedIn() && currentUser.getRole().equals("PATIENT");
    }
}