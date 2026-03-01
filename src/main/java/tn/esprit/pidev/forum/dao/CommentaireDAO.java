package tn.esprit.pidev.forum.dao;

import tn.esprit.pidev.forum.entities.Commentaire;
import tn.esprit.pidev.forum.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireDAO {

    /**
     * CREATE - Ajouter un nouveau commentaire (avec support threading)
     */
    public void addCommentaire(Commentaire commentaire) {
        // ✅ UPDATED: Inclut parent_comment_id
        String query = "INSERT INTO Commentaire (id_post, id_auteur, contenu, nb_likes, date, parent_comment_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, commentaire.getId_post());
            pstmt.setInt(2, commentaire.getId_auteur());
            pstmt.setString(3, commentaire.getContenu());
            pstmt.setInt(4, commentaire.getNb_likes());
            pstmt.setTimestamp(5, Timestamp.valueOf(commentaire.getDate()));

            // ✅ NEW: parent_comment_id (peut être NULL pour top-level comments)
            if (commentaire.getParent_comment_id() == null) {
                pstmt.setNull(6, Types.INTEGER);
            } else {
                pstmt.setInt(6, commentaire.getParent_comment_id());
            }

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    commentaire.setId_comment(rs.getInt(1));
                }
                System.out.println("✅ Commentaire ajouté avec succès! ID: " + commentaire.getId_comment());
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout du commentaire!");
            e.printStackTrace();
        }
    }

    /**
     * READ - Récupérer tous les commentaires d'un post (avec parent_comment_id)
     */
    public List<Commentaire> getCommentairesByPost(int id_post) {
        List<Commentaire> commentaires = new ArrayList<>();
        String query = "SELECT * FROM Commentaire WHERE id_post = ? ORDER BY date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id_post);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Commentaire comment = new Commentaire();
                comment.setId_comment(rs.getInt("id_comment"));
                comment.setId_post(rs.getInt("id_post"));
                comment.setId_auteur(rs.getInt("id_auteur"));
                comment.setContenu(rs.getString("contenu"));
                comment.setDate(rs.getTimestamp("date").toLocalDateTime());
                comment.setNb_likes(rs.getInt("nb_likes"));

                // ✅ NEW: Récupérer parent_comment_id (peut être NULL)
                Integer parentId = rs.getObject("parent_comment_id", Integer.class);
                comment.setParent_comment_id(parentId);

                commentaires.add(comment);
            }

            System.out.println("✅ " + commentaires.size() + " commentaires récupérés pour le post " + id_post);

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des commentaires!");
            e.printStackTrace();
        }

        return commentaires;
    }

    /**
     * READ - Récupérer un commentaire par ID
     */
    public Commentaire getCommentaireById(int id) {
        String query = "SELECT * FROM Commentaire WHERE id_comment = ?";
        Commentaire commentaire = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                commentaire = new Commentaire();
                commentaire.setId_comment(rs.getInt("id_comment"));
                commentaire.setId_post(rs.getInt("id_post"));
                commentaire.setId_auteur(rs.getInt("id_auteur"));
                commentaire.setContenu(rs.getString("contenu"));
                commentaire.setNb_likes(rs.getInt("nb_likes"));
                commentaire.setDate(rs.getTimestamp("date").toLocalDateTime());

                // ✅ NEW: parent_comment_id
                Integer parentId = rs.getObject("parent_comment_id", Integer.class);
                commentaire.setParent_comment_id(parentId);

                System.out.println("✅ Commentaire trouvé.");
            } else {
                System.out.println("❌ Aucun commentaire trouvé avec l'ID: " + id);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération du commentaire!");
            e.printStackTrace();
        }

        return commentaire;
    }

    /**
     * UPDATE - Modifier un commentaire
     */
    public void updateCommentaire(Commentaire commentaire) {
        String query = "UPDATE Commentaire SET contenu = ?, nb_likes = ? WHERE id_comment = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, commentaire.getContenu());
            pstmt.setInt(2, commentaire.getNb_likes());
            pstmt.setInt(3, commentaire.getId_comment());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Commentaire modifié avec succès!");
            } else {
                System.out.println("❌ Aucun commentaire trouvé avec cet ID.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la modification du commentaire!");
            e.printStackTrace();
        }
    }

    /**
     * DELETE - Supprimer un commentaire
     */
    public void deleteCommentaire(int id) {
        String query = "DELETE FROM Commentaire WHERE id_comment = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Commentaire supprimé avec succès!");
            } else {
                System.out.println("❌ Aucun commentaire trouvé avec cet ID.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression du commentaire!");
            e.printStackTrace();
        }
    }

    /**
     * Incrémenter les likes d'un commentaire
     */
    public void incrementLikes(int id_comment) {
        String query = "UPDATE Commentaire SET nb_likes = nb_likes + 1 WHERE id_comment = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id_comment);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Like ajouté au commentaire!");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout du like!");
            e.printStackTrace();
        }
    }

    /**
     * Compter le nombre de commentaires d'un post
     */
    public int countCommentsByPost(int id_post) {
        String query = "SELECT COUNT(*) as total FROM Commentaire WHERE id_post = ?";
        int count = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id_post);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("total");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors du comptage des commentaires!");
            e.printStackTrace();
        }

        return count;
    }
}