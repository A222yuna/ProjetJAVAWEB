package tn.psy.gestioncabinet.service;

import tn.psy.gestioncabinet.dao.PatientDAO;
import tn.psy.gestioncabinet.dao.PsychologueDAO;
import tn.psy.gestioncabinet.model.Patient;
import tn.psy.gestioncabinet.model.Psychologue;
import tn.psy.gestioncabinet.util.PasswordUtil;

/**
 * Service d'inscription - gère la création de comptes
 */
public class InscriptionService {

    private final PsychologueDAO psychologueDAO = new PsychologueDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    public enum InscriptionResult {
        SUCCESS,
        EMAIL_EXISTS,
        PASSWORDS_DO_NOT_MATCH,
        INVALID_DATA,
        ERROR
    }

    public InscriptionResult inscrire(String nomComplet, String email, String motDePasse, String confirmerMotDePasse, AuthService.Role role) {
        // Validation des champs
        if (nomComplet == null || nomComplet.isBlank()) {
            return InscriptionResult.INVALID_DATA;
        }
        if (email == null || email.isBlank()) {
            return InscriptionResult.INVALID_DATA;
        }
        if (motDePasse == null || motDePasse.isBlank()) {
            return InscriptionResult.INVALID_DATA;
        }
        if (!motDePasse.equals(confirmerMotDePasse)) {
            return InscriptionResult.PASSWORDS_DO_NOT_MATCH;
        }
        if (!email.contains("@")) {
            return InscriptionResult.INVALID_DATA;
        }

        // Seuls Patient et Psychologue peuvent s'inscrire
        if (role != AuthService.Role.PSYCHOLOGUE && role != AuthService.Role.PATIENT) {
            return InscriptionResult.INVALID_DATA;
        }

        // Vérifier que l'email n'existe pas déjà
        if (role == AuthService.Role.PSYCHOLOGUE && psychologueDAO.findByEmail(email.trim()).isPresent()) {
            return InscriptionResult.EMAIL_EXISTS;
        }
        if (role == AuthService.Role.PATIENT && patientDAO.findByEmail(email.trim()).isPresent()) {
            return InscriptionResult.EMAIL_EXISTS;
        }

        // Validation : mot de passe minimum 6 caractères
        if (motDePasse.length() < 6) {
            return InscriptionResult.INVALID_DATA;
        }

        // Hash du mot de passe avec BCrypt
        String motDePasseHash = PasswordUtil.hash(motDePasse);

        // Créer le compte
        if (role == AuthService.Role.PSYCHOLOGUE) {
            Psychologue psy = new Psychologue(nomComplet.trim(), "", email.trim(), "", motDePasseHash);
            return psychologueDAO.ajouter(psy) ? InscriptionResult.SUCCESS : InscriptionResult.ERROR;
        } else {
            Patient patient = new Patient(nomComplet.trim(), email.trim(), motDePasseHash);
            return patientDAO.ajouter(patient) ? InscriptionResult.SUCCESS : InscriptionResult.ERROR;
        }
    }
}
