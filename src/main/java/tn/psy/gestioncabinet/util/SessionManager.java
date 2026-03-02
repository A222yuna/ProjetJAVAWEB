package tn.psy.gestioncabinet.util;

<<<<<<< HEAD
import tn.psy.gestioncabinet.service.AuthService;

=======
>>>>>>> origin/gestion-cabinet
/**
 * Gestionnaire de session - stocke l'utilisateur connecté
 */
public class SessionManager {

    private static SessionManager instance;
<<<<<<< HEAD
    private AuthService.Role role;
=======
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
>>>>>>> origin/gestion-cabinet
    private Object user;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

<<<<<<< HEAD
    public void setSession(AuthService.Role role, Object user) {
        this.role = role;
        this.user = user;
    }

    public AuthService.Role getRole() {
=======
    public void setSession(Role role, int userId, Object user) {
        this.role = role;
        this.userId = userId;
        this.user = user;
    }

    public Role getRole() {
>>>>>>> origin/gestion-cabinet
        return role;
    }

    public Object getUser() {
        return user;
    }

<<<<<<< HEAD
    public void clearSession() {
        this.role = null;
        this.user = null;
=======
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
>>>>>>> origin/gestion-cabinet
    }
}
