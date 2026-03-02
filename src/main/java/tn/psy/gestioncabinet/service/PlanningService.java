package tn.psy.gestioncabinet.service;

import tn.psy.gestioncabinet.dao.CreneauDAO;
import tn.psy.gestioncabinet.dao.DisponibiliteDAO;
import tn.psy.gestioncabinet.model.Creneau;
import tn.psy.gestioncabinet.model.Disponibilite;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service de planification : génération des créneaux à partir des disponibilités et réservation.
 */
public class PlanningService {

    private final DisponibiliteDAO disponibiliteDAO = new DisponibiliteDAO();
    private final CreneauDAO creneauDAO = new CreneauDAO();

    /**
     * Génère les créneaux pour une disponibilité sur une plage de dates.
     * Pour chaque jour dans la plage dont le jour de la semaine correspond à la disponibilité,
     * crée un créneau toutes les duree_consultation minutes entre heure_debut et heure_fin.
     */
    public int generateCreneaux(int disponibiliteId, LocalDate dateDebut, LocalDate dateFin) {
        Disponibilite d = disponibiliteDAO.findById(disponibiliteId).orElse(null);
        if (d == null || d.getHeureDebut() == null || d.getHeureFin() == null || d.getDureeConsultation() <= 0) {
            return 0;
        }
        // Supprimer les créneaux LIBRE existants sur la plage pour éviter doublons
        creneauDAO.supprimerByDisponibiliteEtDateRange(disponibiliteId, dateDebut, dateFin);

        int jourAttendu = d.getJour(); // 1 = Lundi ... 7 = Dimanche (Java DayOfWeek compatible)
        LocalTime slot = d.getHeureDebut();
        LocalTime fin = d.getHeureFin();
        int step = d.getDureeConsultation();
        int count = 0;

        for (LocalDate date = dateDebut; !date.isAfter(dateFin); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() != jourAttendu) {
                continue;
            }
            slot = d.getHeureDebut();
            while (slot != null && !slot.plusMinutes(step).isAfter(fin)) {
                Creneau c = new Creneau();
                c.setDisponibiliteId(disponibiliteId);
                c.setDateCreneau(date);
                c.setHeure(slot);
                c.setStatut("LIBRE");
                c.setPatientId(null);
                if (creneauDAO.ajouter(c) > 0) {
                    count++;
                }
                slot = slot.plusMinutes(step);
            }
        }
        return count;
    }

    /**
     * Liste des créneaux libres pour un cabinet sur une plage de dates (côté patient).
     */
    public List<Creneau> getCreneauxLibres(int cabinetId, LocalDate dateDebut, LocalDate dateFin) {
        return creneauDAO.findLibresByCabinetAndDateRange(cabinetId, dateDebut, dateFin);
    }

    /**
     * Réserve un créneau pour un patient. Retourne false si déjà réservé ou inexistant.
     */
    public boolean reserverCreneau(int creneauId, int patientId) {
        return creneauDAO.reserver(creneauId, patientId);
    }

    /**
     * Liste des disponibilités d'un cabinet (pour le psy).
     */
    public List<Disponibilite> getDisponibilitesByCabinet(int cabinetId) {
        return disponibiliteDAO.findByCabinet(cabinetId);
    }

    /**
     * Ajoute une disponibilité (jour, heure début/fin, durée consultation).
     */
    public int ajouterDisponibilite(Disponibilite d) {
        return disponibiliteDAO.ajouter(d);
    }
}
