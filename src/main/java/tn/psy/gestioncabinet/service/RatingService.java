package tn.psy.gestioncabinet.service;

import tn.psy.gestioncabinet.dao.RatingDAO;
import tn.psy.gestioncabinet.model.Rating;

import java.util.Optional;

/**
 * Service métier pour la notation des cabinets par les patients.
 */
public class RatingService {

    private final RatingDAO ratingDAO = new RatingDAO();

    /**
     * Enregistre ou met à jour la note d'un patient pour un cabinet (1 à 5).
     * Un patient ne peut noter qu'une fois par cabinet.
     */
    public boolean noter(int patientIdUser, int cabinetId, int note) {
        if (note < 1 || note > 5) {
            return false;
        }
        Rating r = new Rating(patientIdUser, cabinetId, note);
        return ratingDAO.ajouterOuModifier(r);
    }

    /**
     * Moyenne des notes du cabinet (0.0 si aucun avis).
     */
    public double getMoyenne(int cabinetId) {
        return ratingDAO.getMoyenne(cabinetId);
    }

    /**
     * Nombre total d'avis pour le cabinet.
     */
    public int getNombreAvis(int cabinetId) {
        return ratingDAO.getNombreAvis(cabinetId);
    }

    /**
     * Indique si le patient a déjà noté ce cabinet.
     */
    public boolean aDejaNote(int patientIdUser, int cabinetId) {
        return ratingDAO.findByPatientEtCabinet(patientIdUser, cabinetId).isPresent();
    }

    /**
     * Note actuelle du patient pour ce cabinet (1-5), vide si pas encore noté.
     */
    public Optional<Integer> getNotePatient(int patientIdUser, int cabinetId) {
        return ratingDAO.findByPatientEtCabinet(patientIdUser, cabinetId).map(Rating::getNote);
    }
}
