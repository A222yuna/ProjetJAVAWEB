package tn.psy.gestioncabinet.service;

import tn.psy.gestioncabinet.dao.PsyCabinetDAO;
import tn.psy.gestioncabinet.model.PsyCabinet;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service métier PsyCabinet
 */
public class PsyCabinetService {

    private final PsyCabinetDAO psyCabinetDAO = new PsyCabinetDAO();

    public boolean ajouter(PsyCabinet pc) {
<<<<<<< HEAD
        if (pc == null || pc.getIdPsy() <= 0 || pc.getIdCabinet() <= 0 || pc.getDateDebut() == null) {
            return false;
        }
        if (psyCabinetDAO.existeLiaison(pc.getIdPsy(), pc.getIdCabinet())) {
=======
        if (pc == null || pc.getPsychologueIdUser() <= 0 || pc.getIdCabinet() <= 0 || pc.getDateDebut() == null) {
            return false;
        }
        if (psyCabinetDAO.existeLiaison(pc.getPsychologueIdUser(), pc.getIdCabinet())) {
>>>>>>> origin/gestion-cabinet
            return false; // Liaison déjà existante
        }
        return psyCabinetDAO.ajouter(pc);
    }

    public List<PsyCabinet> findAll() {
        return psyCabinetDAO.findAll();
    }

    public List<PsyCabinet> findByPsychologue(int idPsy) {
        return psyCabinetDAO.findByPsychologue(idPsy);
    }

    public List<PsyCabinet> findByCabinet(int idCabinet) {
        return psyCabinetDAO.findByCabinet(idCabinet);
    }

    public Optional<PsyCabinet> findById(int idPsy, int idCabinet) {
        return psyCabinetDAO.findById(idPsy, idCabinet);
    }

    public boolean modifier(PsyCabinet pc) {
        if (pc == null || pc.getIdPsy() <= 0 || pc.getIdCabinet() <= 0) {
            return false;
        }
        return psyCabinetDAO.modifier(pc);
    }

    public boolean supprimer(int idPsy, int idCabinet) {
        return psyCabinetDAO.supprimer(idPsy, idCabinet);
    }
}
