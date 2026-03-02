package tn.psy.gestioncabinet.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitaire pour le hash et la vérification des mots de passe avec BCrypt.
 */
public class PasswordUtil {

    private static final int WORKLOAD = 10;

    /**
     * Hash un mot de passe avec BCrypt.
     */
    public static String hash(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(WORKLOAD));
    }

    /**
     * Vérifie qu'un mot de passe correspond au hash stocké.
     */
    public static boolean verify(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
