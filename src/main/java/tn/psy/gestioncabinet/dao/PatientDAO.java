package tn.psy.gestioncabinet.dao;

import tn.psy.gestioncabinet.model.Patient;
import tn.psy.gestioncabinet.util.Connexion;
import tn.psy.gestioncabinet.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO Patient - pour authentification et inscription
 */
public class PatientDAO {

    public List<Patient> findAll() {
        List<Patient> liste = new ArrayList<>();
        String sql = "SELECT id_patient, nom, email, telephone, mot_de_passe FROM patient ORDER BY nom";
        try (Connection conn = Connexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Patient(
                        rs.getInt("id_patient"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("telephone"),
                        rs.getString("mot_de_passe")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture patients : " + e.getMessage());
        }
        return liste;
    }

    public boolean ajouter(Patient p) {
        String sql = "INSERT INTO patient (nom, email, telephone, mot_de_passe) VALUES (?, ?, ?, ?)";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getEmail());
            ps.setString(3, p.getTelephone());
            ps.setString(4, p.getMotDePasse());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur ajout patient : " + e.getMessage());
            return false;
        }
    }

    public Optional<Patient> findByEmail(String email) {
        String sql = "SELECT id_patient, nom, email, telephone, mot_de_passe FROM patient WHERE email = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Patient(
                        rs.getInt("id_patient"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("telephone"),
                        rs.getString("mot_de_passe")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche patient par email : " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Patient> findByEmailAndPassword(String email, String motDePasse) {
        Optional<Patient> patient = findByEmail(email);
        if (patient.isPresent() && PasswordUtil.verify(motDePasse, patient.get().getMotDePasse())) {
            return patient;
        }
        return Optional.empty();
    }
}
