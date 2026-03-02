package tn.esprit.mindconnect.services;

import tn.esprit.mindconnect.entities.Avis;
import tn.esprit.mindconnect.interfaces.IService;
import tn.esprit.mindconnect.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AvisService implements IService<Avis> {

    private Connection cnx;

    public AvisService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void add(Avis a) {
        String req = "INSERT INTO avis (idProgramme, idPsychologue, note, commentaire, dateAvis) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, a.getIdProgramme());
            pst.setInt(2, a.getIdPsychologue());
            pst.setInt(3, a.getNote());
            pst.setString(4, a.getCommentaire());
            pst.setDate(5, a.getDateAvis());
            pst.executeUpdate();
            System.out.println("Avis ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de l'avis : " + e.getMessage());
        }
    }

    @Override
    public void update(Avis a) {
        String req = "UPDATE avis SET idProgramme=?, idPsychologue=?, note=?, commentaire=?, dateAvis=? WHERE idAvis=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, a.getIdProgramme());
            pst.setInt(2, a.getIdPsychologue());
            pst.setInt(3, a.getNote());
            pst.setString(4, a.getCommentaire());
            pst.setDate(5, a.getDateAvis());
            pst.setInt(6, a.getIdAvis());
            pst.executeUpdate();
            System.out.println("Avis modifié avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de l'avis : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM avis WHERE idAvis=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Avis supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de l'avis : " + e.getMessage());
        }
    }

    @Override
    public Avis getById(int id) {
        String req = "SELECT * FROM avis WHERE idAvis=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Avis(
                        rs.getInt("idAvis"),
                        rs.getInt("idProgramme"),
                        rs.getInt("idPsychologue"),
                        rs.getInt("note"),
                        rs.getString("commentaire"),
                        rs.getDate("dateAvis"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de l'avis : " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Avis> getAll() {
        List<Avis> avisList = new ArrayList<>();
        String req = "SELECT * FROM avis";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                avisList.add(new Avis(
                        rs.getInt("idAvis"),
                        rs.getInt("idProgramme"),
                        rs.getInt("idPsychologue"),
                        rs.getInt("note"),
                        rs.getString("commentaire"),
                        rs.getDate("dateAvis")));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des avis : " + e.getMessage());
        }
        return avisList;
    }
}
