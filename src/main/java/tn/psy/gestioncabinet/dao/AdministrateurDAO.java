package tn.psy.gestioncabinet.dao;

import tn.psy.gestioncabinet.model.Administrateur;
import tn.psy.gestioncabinet.util.Connexion;
import tn.psy.gestioncabinet.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO Administrateur - pour authentification et inscription
 */
public class AdministrateurDAO {

    public List<Administrateur> findAll() {
        List<Administrateur> liste = new ArrayList<>();
        String sql = "SELECT id_admin, nom, email, mot_de_passe FROM administrateur ORDER BY nom";
        try (Connection conn = Connexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Administrateur(
                        rs.getInt("id_admin"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture administrateurs : " + e.getMessage());
        }
        return liste;
    }

    public boolean ajouter(Administrateur a) {
        String sql = "INSERT INTO administrateur (nom, email, mot_de_passe) VALUES (?, ?, ?)";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getEmail());
            ps.setString(3, a.getMotDePasse());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur ajout administrateur : " + e.getMessage());
            return false;
        }
    }

    public Optional<Administrateur> findByEmail(String email) {
        String sql = "SELECT id_admin, nom, email, mot_de_passe FROM administrateur WHERE email = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Administrateur(
                        rs.getInt("id_admin"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche administrateur par email : " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Administrateur> findByEmailAndPassword(String email, String motDePasse) {
        Optional<Administrateur> admin = findByEmail(email);
        if (admin.isPresent() && PasswordUtil.verify(motDePasse, admin.get().getMotDePasse())) {
            return admin;
        }
        return Optional.empty();
    }
}
