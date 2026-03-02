package tn.psy.gestioncabinet.service;

import tn.psy.gestioncabinet.dao.CabinetDAO;
import tn.psy.gestioncabinet.dao.PsyCabinetDAO;
import tn.psy.gestioncabinet.dao.PsychologueDAO;
import tn.psy.gestioncabinet.model.Cabinet;
import tn.psy.gestioncabinet.model.PsyCabinet;
import tn.psy.gestioncabinet.model.Psychologue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service métier Cabinet
 */
public class CabinetService {

    private final CabinetDAO cabinetDAO = new CabinetDAO();
    private final PsyCabinetDAO psyCabinetDAO = new PsyCabinetDAO();

    public int ajouterCabinet(Cabinet cabinet, int psychologueIdUser) {
        if (cabinet == null || cabinet.getAdresse() == null || cabinet.getAdresse().isBlank() || cabinet.getVille() == null || cabinet.getVille().isBlank()) {
            return -1;
        }
        cabinet.setValide(false); // En attente de validation admin
        int idCabinet = cabinetDAO.ajouter(cabinet);
        if (idCabinet > 0) {
            PsyCabinet pc = new PsyCabinet(psychologueIdUser, idCabinet, LocalDate.now(), null);
            psyCabinetDAO.ajouter(pc);
        }
        return idCabinet;
    }

    public boolean modifierCabinet(Cabinet cabinet) {
        if (cabinet == null || cabinet.getIdCabinet() <= 0) {
            return false;
        }
        return cabinetDAO.modifier(cabinet);
    }

    public boolean supprimerCabinet(int idCabinet, boolean estAdmin) {
        Optional<Cabinet> opt = cabinetDAO.findById(idCabinet);
        if (opt.isEmpty()) {
            return false;
        }
        Cabinet cabinet = opt.get();
        if (cabinet.isValide()) {
            // Une fois validé (statut ACTIF), seul l'administrateur peut supprimer
            if (!estAdmin) {
                return false;
            }
            return cabinetDAO.supprimerAdmin(idCabinet);
        }
        // EN_ATTENTE ou REFUSE : suppression autorisée (psy ou admin)
        return cabinetDAO.supprimer(idCabinet);
    }

    /**
     * Archivage d'un cabinet validé (marquage visuel, sans suppression).
     */
    public boolean archiverCabinet(int idCabinet) {
        return cabinetDAO.archiver(idCabinet);
    }

    /**
     * Bascule l'état d'archivage du cabinet (Archiver / Désarchiver).
     * Le cabinet n'est jamais supprimé et reste visible partout.
     */
    public boolean toggleArchiveCabinet(int idCabinet) {
        return cabinetDAO.toggleArchive(idCabinet);
    }

    public boolean validerCabinet(int idCabinet) {
        return cabinetDAO.valider(idCabinet);
    }

    public List<Cabinet> findAll() {
        return cabinetDAO.findAll();
    }

    public List<Cabinet> findAllValides() {
        return cabinetDAO.findAllValides();
    }

    public List<Cabinet> findAllNonArchives() {
        return cabinetDAO.findAllNonArchives();
    }

    public List<Cabinet> findAllArchives() {
        return cabinetDAO.findAllArchives();
    }

    public List<Cabinet> rechercher(String critere) {
        if (critere == null || critere.isBlank()) {
            return cabinetDAO.findAllValides();
        }
        return cabinetDAO.rechercher(critere);
    }

    public Optional<Cabinet> findById(int id) {
        return cabinetDAO.findById(id);
    }

    public List<PsyCabinet> getCabinetsByPsychologue(int idPsy) {
        return psyCabinetDAO.findByPsychologue(idPsy);
    }

    public List<PsyCabinet> getPsychologuesByCabinet(int idCabinet) {
        return psyCabinetDAO.findByCabinet(idCabinet);
    }

    /**
     * Retourne la liste des emails des psychologues associés au cabinet (pour envoi email validation).
     */
    public List<String> getEmailsPsychologuesPourCabinet(int idCabinet) {
        List<String> emails = new ArrayList<>();
        // Les liaisons psy_cabinet joignent désormais directement la table users.
        for (PsyCabinet pc : psyCabinetDAO.findByCabinet(idCabinet)) {
            if (pc.getNomPsychologue() != null && !pc.getNomPsychologue().isBlank()) {
                // L'email n'est pas nécessairement disponible côté modèle ; la logique
                // d'envoi peut être adaptée ultérieurement pour exploiter un champ email.
            }
        }
        return emails;
    }
}
