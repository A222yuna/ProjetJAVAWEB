package com.psychoapp.util;

import com.psychoapp.model.Utilisateur;

/**
 * Holds the currently logged-in user for the whole app session.
 */
public class Session {

    private static Utilisateur currentUser = null;

    public static void setCurrentUser(Utilisateur u) { currentUser = u; }
    public static Utilisateur getCurrentUser()       { return currentUser; }
    public static void clear()                       { currentUser = null; }

    public static boolean isAdmin() {
        return currentUser != null
                && currentUser.getRole() == Utilisateur.Role.Admin;
    }
}
