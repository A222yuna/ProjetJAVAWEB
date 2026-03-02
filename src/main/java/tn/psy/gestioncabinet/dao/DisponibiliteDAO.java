package tn.psy.gestioncabinet.dao;

import tn.psy.gestioncabinet.model.Disponibilite;
import tn.psy.gestioncabinet.util.Connexion;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO Disponibilite - plages horaires récurrentes par cabinet.
 */
public class DisponibiliteDAO {

    public int ajouter(Disponibilite d) {
        String sql = "INSERT INTO disponibilite (cabinet_id, jour, heure_debut, heure_fin, duree_consultation) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getCabinetId());
            ps.setInt(2, d.getJour());
            ps.setTime(3, Time.valueOf(d.getHeureDebut()));
            ps.setTime(4, Time.valueOf(d.getHeureFin()));
            ps.setInt(5, d.getDureeConsultation());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("DisponibiliteDAO ajout: " + e.getMessage());
        }
        return -1;
    }

    public List<Disponibilite> findByCabinet(int cabinetId) {
        List<Disponibilite> liste = new ArrayList<>();
        String sql = "SELECT id, cabinet_id, jour, heure_debut, heure_fin, duree_consultation FROM disponibilite WHERE cabinet_id = ? ORDER BY jour, heure_debut";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cabinetId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("DisponibiliteDAO findByCabinet: " + e.getMessage());
        }
        return liste;
    }

    public Optional<Disponibilite> findById(int id) {
        String sql = "SELECT id, cabinet_id, jour, heure_debut, heure_fin, duree_consultation FROM disponibilite WHERE id = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("DisponibiliteDAO findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    private Disponibilite mapResultSet(ResultSet rs) throws SQLException {
        Disponibilite d = new Disponibilite();
        d.setId(rs.getInt("id"));
        d.setCabinetId(rs.getInt("cabinet_id"));
        d.setJour(rs.getInt("jour"));
        Time t = rs.getTime("heure_debut");
        d.setHeureDebut(t != null ? t.toLocalTime() : null);
        t = rs.getTime("heure_fin");
        d.setHeureFin(t != null ? t.toLocalTime() : null);
        d.setDureeConsultation(rs.getInt("duree_consultation"));
        return d;
    }
}
