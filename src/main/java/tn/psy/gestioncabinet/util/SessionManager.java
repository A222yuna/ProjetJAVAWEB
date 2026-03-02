package tn.psy.gestioncabinet.util;

import tn.psy.gestioncabinet.service.AuthService;

/**
 * Gestionnaire de session - stocke l'utilisateur connecté
 */
public class SessionManager {

    private static SessionManager instance;
    private AuthService.Role role;
    private Object user;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setSession(AuthService.Role role, Object user) {
        this.role = role;
        this.user = user;
    }

    public AuthService.Role getRole() {
        return role;
    }

    public Object getUser() {
        return user;
    }

    public void clearSession() {
        this.role = null;
        this.user = null;
    }
}
