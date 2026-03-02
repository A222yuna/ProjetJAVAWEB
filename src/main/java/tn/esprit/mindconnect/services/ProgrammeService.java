package tn.esprit.mindconnect.services;

import tn.esprit.mindconnect.entities.ProgrammeBienEtre;
import tn.esprit.mindconnect.interfaces.IService;
import tn.esprit.mindconnect.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgrammeService implements IService<ProgrammeBienEtre> {

    private Connection cnx;

    public ProgrammeService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void add(ProgrammeBienEtre p) {
        String req = "INSERT INTO programme_bien_etre (idPsychologue, nom, objectif, duree, statut, image, niveauDifficulte) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, p.getIdPsychologue());
            pst.setString(2, p.getNom());
            pst.setString(3, p.getObjectif());
            pst.setInt(4, p.getDuree());
            pst.setString(5, p.getStatut());
            pst.setString(6, p.getImage());
            pst.setString(7, p.getNiveauDifficulte());
            pst.executeUpdate();
            System.out.println("Programme ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du programme : " + e.getMessage());
        }
    }

    @Override
    public void update(ProgrammeBienEtre p) {
        String req = "UPDATE programme_bien_etre SET idPsychologue=?, nom=?, objectif=?, duree=?, statut=?, image=?, niveauDifficulte=? WHERE idProgramme=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, p.getIdPsychologue());
            pst.setString(2, p.getNom());
            pst.setString(3, p.getObjectif());
            pst.setInt(4, p.getDuree());
            pst.setString(5, p.getStatut());
            pst.setString(6, p.getImage());
            pst.setString(7, p.getNiveauDifficulte());
            pst.setInt(8, p.getIdProgramme());
            pst.executeUpdate();
            System.out.println("Programme modifié avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du programme : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM programme_bien_etre WHERE idProgramme=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Programme supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du programme : " + e.getMessage());
        }
    }

    @Override
    public ProgrammeBienEtre getById(int id) {
        String req = "SELECT * FROM programme_bien_etre WHERE idProgramme=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new ProgrammeBienEtre(
                        rs.getInt("idProgramme"),
                        rs.getInt("idPsychologue"),
                        rs.getString("nom"),
                        rs.getString("objectif"),
                        rs.getInt("duree"),
                        rs.getString("statut"),
                        rs.getString("image"),
                        rs.getString("niveauDifficulte"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du programme : " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ProgrammeBienEtre> getAll() {
        List<ProgrammeBienEtre> programmes = new ArrayList<>();
        String req = "SELECT * FROM programme_bien_etre";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                programmes.add(new ProgrammeBienEtre(
                        rs.getInt("idProgramme"),
                        rs.getInt("idPsychologue"),
                        rs.getString("nom"),
                        rs.getString("objectif"),
                        rs.getInt("duree"),
                        rs.getString("statut"),
                        rs.getString("image"),
                        rs.getString("niveauDifficulte")));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des programmes : " + e.getMessage());
        }
        return programmes;
    }
}
