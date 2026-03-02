package tn.esprit.mindconnect.services;

import tn.esprit.mindconnect.entities.ActiviteProgramme;
import tn.esprit.mindconnect.interfaces.IService;
import tn.esprit.mindconnect.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActiviteService implements IService<ActiviteProgramme> {

    private Connection cnx;

    public ActiviteService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void add(ActiviteProgramme a) {
        String req = "INSERT INTO activite_programme (idProgramme, jour, heureDebut, titre, description, dureeMinutes, typeActivite) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, a.getIdProgramme());
            pst.setInt(2, a.getJour());
            pst.setTime(3, a.getHeureDebut());
            pst.setString(4, a.getTitre());
            pst.setString(5, a.getDescription());
            pst.setInt(6, a.getDureeMinutes());
            pst.setString(7, a.getTypeActivite());
            pst.executeUpdate();
            System.out.println("Activité ajoutée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de l'activité : " + e.getMessage());
        }
    }

    @Override
    public void update(ActiviteProgramme a) {
        String req = "UPDATE activite_programme SET idProgramme=?, jour=?, heureDebut=?, titre=?, description=?, dureeMinutes=?, typeActivite=? WHERE idActivite=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, a.getIdProgramme());
            pst.setInt(2, a.getJour());
            pst.setTime(3, a.getHeureDebut());
            pst.setString(4, a.getTitre());
            pst.setString(5, a.getDescription());
            pst.setInt(6, a.getDureeMinutes());
            pst.setString(7, a.getTypeActivite());
            pst.setInt(8, a.getIdActivite());
            pst.executeUpdate();
            System.out.println("Activité modifiée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de l'activité : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM activite_programme WHERE idActivite=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Activité supprimée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de l'activité : " + e.getMessage());
        }
    }

    @Override
    public ActiviteProgramme getById(int id) {
        String req = "SELECT * FROM activite_programme WHERE idActivite=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new ActiviteProgramme(
                        rs.getInt("idActivite"),
                        rs.getInt("idProgramme"),
                        rs.getInt("jour"),
                        rs.getTime("heureDebut"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getInt("dureeMinutes"),
                        rs.getString("typeActivite"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de l'activité : " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ActiviteProgramme> getAll() {
        List<ActiviteProgramme> activites = new ArrayList<>();
        String req = "SELECT * FROM activite_programme";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                activites.add(new ActiviteProgramme(
                        rs.getInt("idActivite"),
                        rs.getInt("idProgramme"),
                        rs.getInt("jour"),
                        rs.getTime("heureDebut"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getInt("dureeMinutes"),
                        rs.getString("typeActivite")));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des activités : " + e.getMessage());
        }
        return activites;
    }

    public List<ActiviteProgramme> getByProgrammeId(int idProgramme) {
        List<ActiviteProgramme> activites = new ArrayList<>();
        String req = "SELECT * FROM activite_programme WHERE idProgramme=? ORDER BY jour, heureDebut";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, idProgramme);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                activites.add(new ActiviteProgramme(
                        rs.getInt("idActivite"),
                        rs.getInt("idProgramme"),
                        rs.getInt("jour"),
                        rs.getTime("heureDebut"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getInt("dureeMinutes"),
                        rs.getString("typeActivite")));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des activités par programme : " + e.getMessage());
        }
        return activites;
    }
}
