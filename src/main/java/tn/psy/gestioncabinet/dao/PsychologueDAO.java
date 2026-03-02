package tn.psy.gestioncabinet.dao;

import tn.psy.gestioncabinet.model.Psychologue;
import tn.psy.gestioncabinet.util.Connexion;
import tn.psy.gestioncabinet.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO Psychologue - CRUD complet
 */
public class PsychologueDAO {

    public boolean ajouter(Psychologue p) {
        String sql = "INSERT INTO psychologue (nom, specialite, email, telephone, mot_de_passe) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getSpecialite() != null ? p.getSpecialite() : "");
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getTelephone() != null ? p.getTelephone() : "");
            ps.setString(5, p.getMotDePasse());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur ajout psychologue : " + e.getMessage());
            return false;
        }
    }

    public List<Psychologue> findAll() {
        List<Psychologue> liste = new ArrayList<>();
        String sql = "SELECT id_psy, nom, specialite, email, telephone, mot_de_passe FROM psychologue ORDER BY nom";
        try (Connection conn = Connexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture psychologues : " + e.getMessage());
        }
        return liste;
    }

    public Optional<Psychologue> findById(int id) {
        String sql = "SELECT id_psy, nom, specialite, email, telephone, mot_de_passe FROM psychologue WHERE id_psy = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche psychologue : " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Psychologue> findByEmail(String email) {
        String sql = "SELECT id_psy, nom, specialite, email, telephone, mot_de_passe FROM psychologue WHERE email = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche psychologue par email : " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Psychologue> findByEmailAndPassword(String email, String motDePasse) {
        Optional<Psychologue> psy = findByEmail(email);
        if (psy.isPresent() && PasswordUtil.verify(motDePasse, psy.get().getMotDePasse())) {
            return psy;
        }
        return Optional.empty();
    }

    public boolean modifier(Psychologue p) {
        String sql = "UPDATE psychologue SET nom = ?, specialite = ?, email = ?, telephone = ?, mot_de_passe = ? WHERE id_psy = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getSpecialite() != null ? p.getSpecialite() : "");
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getTelephone() != null ? p.getTelephone() : "");
            ps.setString(5, p.getMotDePasse());
            ps.setInt(6, p.getIdPsy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur modification psychologue : " + e.getMessage());
            return false;
        }
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM psychologue WHERE id_psy = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression psychologue : " + e.getMessage());
            return false;
        }
    }

    private Psychologue mapResultSet(ResultSet rs) throws SQLException {
        return new Psychologue(
                rs.getInt("id_psy"),
                rs.getString("nom"),
                rs.getString("specialite"),
                rs.getString("email"),
                rs.getString("telephone"),
                rs.getString("mot_de_passe")
        );
    }
}
