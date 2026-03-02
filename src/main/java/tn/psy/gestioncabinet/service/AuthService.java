package tn.psy.gestioncabinet.service;

import tn.psy.gestioncabinet.dao.AdministrateurDAO;
import tn.psy.gestioncabinet.dao.PatientDAO;
import tn.psy.gestioncabinet.dao.PsychologueDAO;
import tn.psy.gestioncabinet.model.Administrateur;
import tn.psy.gestioncabinet.model.Patient;
import tn.psy.gestioncabinet.model.Psychologue;

import java.util.Optional;

/**
 * Service d'authentification
 */
public class AuthService {

    private final PsychologueDAO psychologueDAO = new PsychologueDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final AdministrateurDAO administrateurDAO = new AdministrateurDAO();

    public enum Role {
        PSYCHOLOGUE,
        PATIENT,
        ADMINISTRATEUR
    }

    public static class AuthResult {
        private final boolean success;
        private final Role role;
        private final Object user;

        public AuthResult(boolean success, Role role, Object user) {
            this.success = success;
            this.role = role;
            this.user = user;
        }

        public boolean isSuccess() {
            return success;
        }

        public Role getRole() {
            return role;
        }

        public Object getUser() {
            return user;
        }
    }

    public AuthResult login(String email, String password, Role role) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return new AuthResult(false, null, null);
        }

        switch (role) {
            case PSYCHOLOGUE:
                Optional<Psychologue> psy = psychologueDAO.findByEmailAndPassword(email.trim(), password);
                return psy.map(p -> new AuthResult(true, Role.PSYCHOLOGUE, p))
                        .orElse(new AuthResult(false, null, null));

            case PATIENT:
                Optional<Patient> pat = patientDAO.findByEmailAndPassword(email.trim(), password);
                return pat.map(p -> new AuthResult(true, Role.PATIENT, p))
                        .orElse(new AuthResult(false, null, null));

            case ADMINISTRATEUR:
                Optional<Administrateur> admin = administrateurDAO.findByEmailAndPassword(email.trim(), password);
                return admin.map(a -> new AuthResult(true, Role.ADMINISTRATEUR, a))
                        .orElse(new AuthResult(false, null, null));

            default:
                return new AuthResult(false, null, null);
        }
    }
}
