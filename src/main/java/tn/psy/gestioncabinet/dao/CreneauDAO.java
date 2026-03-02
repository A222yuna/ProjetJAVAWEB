package tn.psy.gestioncabinet.dao;

import tn.psy.gestioncabinet.model.Creneau;
import tn.psy.gestioncabinet.util.Connexion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO Creneau - créneaux générés, réservables par les patients.
 */
public class CreneauDAO {

    public int ajouter(Creneau c) {
        String sql = "INSERT INTO creneau (disponibilite_id, date_creneau, heure, statut, patient_id_user) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getDisponibiliteId());
            ps.setDate(2, Date.valueOf(c.getDateCreneau()));
            ps.setTime(3, Time.valueOf(c.getHeure()));
            ps.setString(4, c.getStatut() != null ? c.getStatut() : "LIBRE");
            ps.setObject(5, c.getPatientIdUser());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("CreneauDAO ajout: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Créneaux libres pour un cabinet sur une plage de dates (pour affichage patient).
     */
    public List<Creneau> findLibresByCabinetAndDateRange(int cabinetId, LocalDate dateDebut, LocalDate dateFin) {
        List<Creneau> liste = new ArrayList<>();
        String sql = "SELECT c.id, c.disponibilite_id, c.date_creneau, c.heure, c.statut, c.patient_id_user " +
                "FROM creneau c JOIN disponibilite d ON c.disponibilite_id = d.id " +
                "WHERE d.cabinet_id = ? AND c.date_creneau >= ? AND c.date_creneau <= ? AND c.statut = 'LIBRE' " +
                "ORDER BY c.date_creneau, c.heure";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cabinetId);
            ps.setDate(2, Date.valueOf(dateDebut));
            ps.setDate(3, Date.valueOf(dateFin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("CreneauDAO findLibresByCabinetAndDateRange: " + e.getMessage());
        }
        return liste;
    }

    public Optional<Creneau> findById(int id) {
        String sql = "SELECT id, disponibilite_id, date_creneau, heure, statut, patient_id_user FROM creneau WHERE id = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("CreneauDAO findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Réserve un créneau pour un patient (statut = RESERVE, patient_id = idPatient).
     * Retourne true si le créneau était LIBRE et a été réservé.
     */
    public boolean reserver(int creneauId, int patientId) {
        String sql = "UPDATE creneau SET statut = 'RESERVE', patient_id_user = ? WHERE id = ? AND statut = 'LIBRE'";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, creneauId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("CreneauDAO reserver: " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime les créneaux d'une disponibilité sur une plage de dates (pour régénération).
     */
    public int supprimerByDisponibiliteEtDateRange(int disponibiliteId, LocalDate dateDebut, LocalDate dateFin) {
        String sql = "DELETE FROM creneau WHERE disponibilite_id = ? AND date_creneau >= ? AND date_creneau <= ? AND statut = 'LIBRE'";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, disponibiliteId);
            ps.setDate(2, Date.valueOf(dateDebut));
            ps.setDate(3, Date.valueOf(dateFin));
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("CreneauDAO supprimerByDisponibiliteEtDateRange: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Vérifie si un créneau existe et est libre (éviter double réservation).
     */
    public boolean estLibre(int creneauId) {
        return findById(creneauId).map(Creneau::isLibre).orElse(false);
    }

    private Creneau mapResultSet(ResultSet rs) throws SQLException {
        Creneau c = new Creneau();
        c.setId(rs.getInt("id"));
        c.setDisponibiliteId(rs.getInt("disponibilite_id"));
        Date d = rs.getDate("date_creneau");
        c.setDateCreneau(d != null ? d.toLocalDate() : null);
        Time t = rs.getTime("heure");
        c.setHeure(t != null ? t.toLocalTime() : null);
        c.setStatut(rs.getString("statut"));
        int pid = rs.getInt("patient_id_user");
        c.setPatientIdUser(rs.wasNull() ? null : pid);
        return c;
    }
}
