package tn.esprit.pidev.forum.dao;

import tn.esprit.pidev.forum.entities.Post;
import tn.esprit.pidev.forum.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    /**
     * CREATE - Ajouter un nouveau post
     */
    public void addPost(Post post) {
        String query = "INSERT INTO Post (id_auteur, titre, contenu, categorie, nb_likes, date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, post.getId_auteur());
            pstmt.setString(2, post.getTitre());
            pstmt.setString(3, post.getContenu());
            pstmt.setString(4, post.getCategorie());
            pstmt.setInt(5, post.getNb_likes());
            pstmt.setTimestamp(6, Timestamp.valueOf(post.getDate()));

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Récupérer l'ID généré
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    post.setId_post(rs.getInt(1));
                }
                System.out.println("✅ Post ajouté avec succès! ID: " + post.getId_post());
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout du post!");
            e.printStackTrace();
        }
    }

    /**
     * READ - Récupérer tous les posts
     */
    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM Post ORDER BY date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Post post = new Post(
                        rs.getInt("id_post"),
                        rs.getInt("id_auteur"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("categorie"),
                        rs.getInt("nb_likes"),
                        rs.getTimestamp("date").toLocalDateTime()
                );
                posts.add(post);
            }

            System.out.println("✅ " + posts.size() + " posts récupérés.");

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des posts!");
            e.printStackTrace();
        }

        return posts;
    }

    /**
     * READ - Récupérer un post par ID
     */
    public Post getPostById(int id) {
        String query = "SELECT * FROM Post WHERE id_post = ?";
        Post post = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                post = new Post(
                        rs.getInt("id_post"),
                        rs.getInt("id_auteur"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("categorie"),
                        rs.getInt("nb_likes"),
                        rs.getTimestamp("date").toLocalDateTime()
                );
                System.out.println("✅ Post trouvé: " + post.getTitre());
            } else {
                System.out.println("❌ Aucun post trouvé avec l'ID: " + id);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération du post!");
            e.printStackTrace();
        }

        return post;
    }

    /**
     * READ - Récupérer les posts par catégorie
     */
    public List<Post> getPostsByCategory(String categorie) {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM Post WHERE categorie = ? ORDER BY date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, categorie);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Post post = new Post(
                        rs.getInt("id_post"),
                        rs.getInt("id_auteur"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("categorie"),
                        rs.getInt("nb_likes"),
                        rs.getTimestamp("date").toLocalDateTime()
                );
                posts.add(post);
            }

            System.out.println("✅ " + posts.size() + " posts trouvés dans la catégorie: " + categorie);

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des posts par catégorie!");
            e.printStackTrace();
        }

        return posts;
    }

    /**
     * UPDATE - Modifier un post
     */
    public void updatePost(Post post) {
        String query = "UPDATE Post SET titre = ?, contenu = ?, categorie = ? WHERE id_post = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, post.getTitre());
            pstmt.setString(2, post.getContenu());
            pstmt.setString(3, post.getCategorie());
            pstmt.setInt(4, post.getId_post());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Post modifié avec succès!");
            } else {
                System.out.println("❌ Aucun post trouvé avec cet ID.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la modification du post!");
            e.printStackTrace();
        }
    }

    /**
     * DELETE - Supprimer un post
     */
    public void deletePost(int id) {
        String query = "DELETE FROM Post WHERE id_post = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Post supprimé avec succès!");
            } else {
                System.out.println("❌ Aucun post trouvé avec cet ID.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression du post!");
            e.printStackTrace();
        }
    }

    /**
     * Incrémenter les likes d'un post
     */
    public void incrementLikes(int id_post) {
        String query = "UPDATE Post SET nb_likes = nb_likes + 1 WHERE id_post = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id_post);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Like ajouté au post!");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout du like!");
            e.printStackTrace();
        }
    }

    /**
     * Récupérer toutes les catégories uniques
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT DISTINCT categorie FROM Post ORDER BY categorie";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                categories.add(rs.getString("categorie"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des catégories!");
            e.printStackTrace();
        }

        return categories;
    }
}