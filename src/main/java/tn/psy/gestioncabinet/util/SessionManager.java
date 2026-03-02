package tn.psy.gestioncabinet.util;

/**
 * Gestionnaire de session - stocke l'utilisateur connecté
 */
public class SessionManager {

    private static SessionManager instance;
    public enum Role {
        ADMIN,
        PSYCHOLOGUE,
        PATIENT
    }

    private Role role;
    /**
     * Identifiant technique de l'utilisateur connecté (users.id_user).
     * Renseigné par le module central d'authentification.
     */
    private int userId;
    /**
     * Objet représentant l'utilisateur connecté.
     * Il provient du module central de gestion des users (table users)
     * et n'est pas géré ici (pas de login/register local).
     */
    private Object user;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setSession(Role role, int userId, Object user) {
        this.role = role;
        this.userId = userId;
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public Object getUser() {
        return user;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void clearSession() {
        this.role = null;
        this.user = null;
        this.userId = 0;
    }
}
