package tn.psy.gestioncabinet.dao;

import tn.psy.gestioncabinet.model.Rating;
import tn.psy.gestioncabinet.util.Connexion;

import java.sql.*;
import java.util.Optional;

/**
 * DAO Rating - notation des cabinets par les patients (1 à 5 étoiles).
 */
public class RatingDAO {

    /**
     * Ajoute ou met à jour la note d'un patient pour un cabinet.
     * Un patient ne peut avoir qu'une seule note par cabinet (contrainte UNIQUE).
     */
    public boolean ajouterOuModifier(Rating rating) {
        if (rating == null || rating.getNote() < 1 || rating.getNote() > 5) {
            return false;
        }
        String sql = "INSERT INTO rating (patient_id, cabinet_id, note) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE note = VALUES(note)";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rating.getPatientId());
            ps.setInt(2, rating.getCabinetId());
            ps.setInt(3, rating.getNote());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("RatingDAO ajout: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère la note du patient pour le cabinet, s'il en a déjà donné une.
     */
    public Optional<Rating> findByPatientEtCabinet(int patientId, int cabinetId) {
        String sql = "SELECT id, patient_id, cabinet_id, note FROM rating WHERE patient_id = ? AND cabinet_id = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, cabinetId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("RatingDAO findByPatientEtCabinet: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Moyenne des notes pour un cabinet (arrondie à 1 décimale).
     */
    public double getMoyenne(int cabinetId) {
        String sql = "SELECT AVG(note) FROM rating WHERE cabinet_id = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cabinetId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : Math.round(avg * 10.0) / 10.0;
            }
        } catch (SQLException e) {
            System.err.println("RatingDAO getMoyenne: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Nombre total d'avis pour un cabinet.
     */
    public int getNombreAvis(int cabinetId) {
        String sql = "SELECT COUNT(*) FROM rating WHERE cabinet_id = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cabinetId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("RatingDAO getNombreAvis: " + e.getMessage());
        }
        return 0;
    }

    private Rating mapResultSet(ResultSet rs) throws SQLException {
        Rating r = new Rating();
        r.setId(rs.getInt("id"));
        r.setPatientId(rs.getInt("patient_id"));
        r.setCabinetId(rs.getInt("cabinet_id"));
        r.setNote(rs.getInt("note"));
        return r;
    }
}
