package tn.psy.gestioncabinet.dao;

import tn.psy.gestioncabinet.model.PsyCabinet;
import tn.psy.gestioncabinet.util.Connexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO PsyCabinet - CRUD complet pour la liaison Psychologue-Cabinet
 */
public class PsyCabinetDAO {

    public boolean ajouter(PsyCabinet pc) {
        String sql = "INSERT INTO psy_cabinet (psychologue_id_user, id_cabinet, date_debut, date_fin) VALUES (?, ?, ?, ?)";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pc.getPsychologueIdUser());
            ps.setInt(2, pc.getIdCabinet());
            ps.setDate(3, Date.valueOf(pc.getDateDebut()));
            ps.setObject(4, pc.getDateFin() != null ? Date.valueOf(pc.getDateFin()) : null);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur ajout psy_cabinet : " + e.getMessage());
            return false;
        }
    }

    public List<PsyCabinet> findAll() {
        List<PsyCabinet> liste = new ArrayList<>();
        String sql = "SELECT pc.psychologue_id_user, pc.id_cabinet, pc.date_debut, pc.date_fin, " +
                "u.nom AS nom_psy, c.adresse, c.ville " +
                "FROM psy_cabinet pc " +
                "JOIN users u ON pc.psychologue_id_user = u.id_user " +
                "JOIN cabinet c ON pc.id_cabinet = c.id_cabinet " +
                "ORDER BY pc.date_debut DESC";
        try (Connection conn = Connexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture psy_cabinet : " + e.getMessage());
        }
        return liste;
    }

    public List<PsyCabinet> findByPsychologue(int idPsyUser) {
        List<PsyCabinet> liste = new ArrayList<>();
        String sql = "SELECT pc.psychologue_id_user, pc.id_cabinet, pc.date_debut, pc.date_fin, " +
                "u.nom AS nom_psy, c.adresse, c.ville " +
                "FROM psy_cabinet pc " +
                "JOIN users u ON pc.psychologue_id_user = u.id_user " +
                "JOIN cabinet c ON pc.id_cabinet = c.id_cabinet " +
                "WHERE pc.psychologue_id_user = ? AND (pc.date_fin IS NULL OR pc.date_fin >= CURDATE()) " +
                "ORDER BY pc.date_debut DESC";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPsyUser);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture cabinets du psychologue : " + e.getMessage());
        }
        return liste;
    }

    public List<PsyCabinet> findByCabinet(int idCabinet) {
        List<PsyCabinet> liste = new ArrayList<>();
        String sql = "SELECT pc.psychologue_id_user, pc.id_cabinet, pc.date_debut, pc.date_fin, " +
                "u.nom AS nom_psy, c.adresse, c.ville " +
                "FROM psy_cabinet pc " +
                "JOIN users u ON pc.psychologue_id_user = u.id_user " +
                "JOIN cabinet c ON pc.id_cabinet = c.id_cabinet " +
                "WHERE pc.id_cabinet = ? AND (pc.date_fin IS NULL OR pc.date_fin >= CURDATE()) " +
                "ORDER BY p.nom";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCabinet);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture psychologues du cabinet : " + e.getMessage());
        }
        return liste;
    }

    public Optional<PsyCabinet> findById(int idPsyUser, int idCabinet) {
        String sql = "SELECT psychologue_id_user, id_cabinet, date_debut, date_fin FROM psy_cabinet WHERE psychologue_id_user = ? AND id_cabinet = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPsyUser);
            ps.setInt(2, idCabinet);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche psy_cabinet : " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean existeLiaison(int idPsyUser, int idCabinet) {
        return findById(idPsyUser, idCabinet).isPresent();
    }

    public boolean modifier(PsyCabinet pc) {
        String sql = "UPDATE psy_cabinet SET date_debut = ?, date_fin = ? WHERE psychologue_id_user = ? AND id_cabinet = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(pc.getDateDebut()));
            ps.setObject(2, pc.getDateFin() != null ? Date.valueOf(pc.getDateFin()) : null);
            ps.setInt(3, pc.getPsychologueIdUser());
            ps.setInt(4, pc.getIdCabinet());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur modification psy_cabinet : " + e.getMessage());
            return false;
        }
    }

    public boolean supprimer(int idPsy, int idCabinet) {
        String sql = "DELETE FROM psy_cabinet WHERE psychologue_id_user = ? AND id_cabinet = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPsy);
            ps.setInt(2, idCabinet);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression psy_cabinet : " + e.getMessage());
            return false;
        }
    }

    public boolean supprimerParCabinet(int idCabinet) {
        String sql = "DELETE FROM psy_cabinet WHERE id_cabinet = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCabinet);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression psy_cabinet par cabinet : " + e.getMessage());
            return false;
        }
    }

    private PsyCabinet mapResultSet(ResultSet rs) throws SQLException {
        PsyCabinet pc = new PsyCabinet();
        pc.setPsychologueIdUser(rs.getInt("psychologue_id_user"));
        pc.setIdCabinet(rs.getInt("id_cabinet"));
        Date d = rs.getDate("date_debut");
        pc.setDateDebut(d != null ? d.toLocalDate() : null);
        d = rs.getDate("date_fin");
        pc.setDateFin(d != null ? d.toLocalDate() : null);
        return pc;
    }

    private PsyCabinet mapResultSetWithDetails(ResultSet rs) throws SQLException {
        PsyCabinet pc = mapResultSet(rs);
        pc.setNomPsychologue(rs.getString("nom_psy"));
        pc.setAdresseCabinet(rs.getString("adresse"));
        pc.setVilleCabinet(rs.getString("ville"));
        return pc;
    }
}
