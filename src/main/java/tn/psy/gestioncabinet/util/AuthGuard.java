package tn.psy.gestioncabinet.util;

/**
 * Utilitaire centralisé pour le contrôle d'accès basé sur la session.
 * Il ne gère pas le login / register (délégué au module Gestion User),
 * mais vérifie simplement :
 *  - si un utilisateur est connecté
 *  - si son rôle autorise l'accès à une vue donnée.
 */
public class AuthGuard {

    // MODE TEST TEMPORAIRE – A désactiver après intégration Gestion User
    // Passez cette constante à false pour réactiver le contrôle réel.
    public static final boolean TEST_MODE = true;

    public enum RequiredRole {
        ANY,           // juste connecté
        ADMIN,
        PSYCHOLOGUE,
        PATIENT
    }

    /**
     * Vérifie que l'utilisateur est connecté et que son rôle correspond
     * au rôle requis. Retourne false si non autorisé.
     */
    public static boolean check(RequiredRole required) {
        // MODE TEST TEMPORAIRE : on bypass totalement les vérifications
        if (TEST_MODE) {
            return true;
        }
        SessionManager session = SessionManager.getInstance();
        if (session.getUser() == null || session.getRole() == null) {
            return false;
        }
        if (required == null || required == RequiredRole.ANY) {
            return true;
        }
        return switch (required) {
            case ADMIN -> session.getRole() == SessionManager.Role.ADMIN;
            case PSYCHOLOGUE -> session.getRole() == SessionManager.Role.PSYCHOLOGUE;
            case PATIENT -> session.getRole() == SessionManager.Role.PATIENT;
            default -> false;
        };
    }
}

