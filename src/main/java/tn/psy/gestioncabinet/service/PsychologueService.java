package tn.psy.gestioncabinet.service;

import tn.psy.gestioncabinet.dao.PsychologueDAO;
import tn.psy.gestioncabinet.model.Psychologue;

import java.util.List;
import java.util.Optional;

/**
 * Service métier Psychologue
 */
public class PsychologueService {

    private final PsychologueDAO psychologueDAO = new PsychologueDAO();

    public boolean ajouter(Psychologue p) {
        if (p == null || p.getNom() == null || p.getNom().isBlank() || p.getEmail() == null || p.getEmail().isBlank()) {
            return false;
        }
        if (psychologueDAO.findByEmail(p.getEmail()).isPresent()) {
            return false; // Email déjà utilisé
        }
        return psychologueDAO.ajouter(p);
    }

    public List<Psychologue> findAll() {
        return psychologueDAO.findAll();
    }

    public Optional<Psychologue> findById(int id) {
        return psychologueDAO.findById(id);
    }

    public boolean modifier(Psychologue p) {
        if (p == null || p.getIdPsy() <= 0 || p.getNom() == null || p.getNom().isBlank()) {
            return false;
        }
        return psychologueDAO.modifier(p);
    }

    public boolean supprimer(int id) {
        return psychologueDAO.supprimer(id);
    }

    public Optional<Psychologue> findByEmail(String email) {
        return psychologueDAO.findByEmail(email);
    }
}
