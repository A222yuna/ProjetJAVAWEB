package tn.esprit.pidev.forum.services;

import tn.esprit.pidev.forum.dao.PostDAO;
import tn.esprit.pidev.forum.entities.Post;
import tn.esprit.pidev.forum.utils.BadWordsFilter;  // ✅ NOUVEAU IMPORT

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour gérer la logique métier des Posts
 */
public class PostService {
    private PostDAO postDAO;

    public PostService() {
        this.postDAO = new PostDAO();
    }

    /**
     * Ajouter un post avec validation + filtrage bad words
     */
    public boolean addPost(Post post) {
        // Validation
        if (post.getTitre() == null || post.getTitre().trim().isEmpty()) {
            System.out.println("❌ Le titre ne peut pas être vide!");
            return false;
        }

        if (post.getContenu() == null || post.getContenu().trim().isEmpty()) {
            System.out.println("❌ Le contenu ne peut pas être vide!");
            return false;
        }

        if (post.getCategorie() == null || post.getCategorie().trim().isEmpty()) {
            System.out.println("❌ La catégorie ne peut pas être vide!");
            return false;
        }

        // ✅✅✅ NOUVEAU: FILTRER LES BAD WORDS AVANT D'AJOUTER ✅✅✅
        System.out.println("\n🔍 === BAD WORDS FILTER API === 🔍");
        System.out.println("Vérification du titre...");
        String originalTitle = post.getTitre();
        post.setTitre(BadWordsFilter.filterText(post.getTitre()));

        System.out.println("Vérification du contenu...");
        String originalContent = post.getContenu();
        post.setContenu(BadWordsFilter.filterText(post.getContenu()));

        // Alerter si bad words détectés
        boolean hadBadWords = false;
        if (!originalTitle.equals(post.getTitre())) {
            System.out.println("⚠️ GROS MOTS DÉTECTÉS dans le titre et censurés!");
            hadBadWords = true;
        }
        if (!originalContent.equals(post.getContenu())) {
            System.out.println("⚠️ GROS MOTS DÉTECTÉS dans le contenu et censurés!");
            hadBadWords = true;
        }

        if (!hadBadWords) {
            System.out.println("✅ Aucun gros mot détecté - Texte propre!");
        }
        System.out.println("=================================\n");
        // ✅✅✅ FIN DU FILTRAGE ✅✅✅

        // Si validation OK, ajouter le post
        postDAO.addPost(post);
        return true;
    }

    /**
     * ✅ MÉTIER AVANCÉ: Récupérer les posts les plus likés
     */
    public List<Post> getTopPostsByLikes(int limit) {
        return postDAO.getAllPosts().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getNb_likes(), p1.getNb_likes()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * ✅ MÉTIER AVANCÉ: Récupérer les posts les plus likés du mois
     */
    public List<Post> getTopPostsThisMonth(int limit) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return postDAO.getAllPosts().stream()
                .filter(p -> p.getDate().isAfter(oneMonthAgo))
                .sorted((p1, p2) -> Integer.compare(p2.getNb_likes(), p1.getNb_likes()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer tous les posts
     */
    public List<Post> getAllPosts() {
        return postDAO.getAllPosts();
    }

    /**
     * Récupérer un post par ID
     */
    public Post getPostById(int id) {
        return postDAO.getPostById(id);
    }

    /**
     * Récupérer les posts par catégorie
     */
    public List<Post> getPostsByCategory(String categorie) {
        return postDAO.getPostsByCategory(categorie);
    }

    /**
     * Modifier un post avec validation + filtrage bad words
     */
    public boolean updatePost(Post post) {
        // Validation
        if (post.getTitre() == null || post.getTitre().trim().isEmpty()) {
            System.out.println("❌ Le titre ne peut pas être vide!");
            return false;
        }

        if (post.getContenu() == null || post.getContenu().trim().isEmpty()) {
            System.out.println("❌ Le contenu ne peut pas être vide!");
            return false;
        }

        // ✅ NOUVEAU: Filtrer les bad words lors de la modification
        System.out.println("🔍 Vérification des gros mots...");
        post.setTitre(BadWordsFilter.filterText(post.getTitre()));
        post.setContenu(BadWordsFilter.filterText(post.getContenu()));

        // Si validation OK, modifier le post
        postDAO.updatePost(post);
        return true;
    }

    /**
     * Supprimer un post
     */
    public void deletePost(int id) {
        postDAO.deletePost(id);
    }

    /**
     * Liker un post
     */
    public void likePost(int id_post) {
        postDAO.incrementLikes(id_post);
    }

    /**
     * Récupérer toutes les catégories
     */
    public List<String> getAllCategories() {
        return postDAO.getAllCategories();
    }

    /**
     * Rechercher des posts par mot-clé dans le titre ou contenu
     */
    public List<Post> searchPosts(String keyword) {
        List<Post> allPosts = postDAO.getAllPosts();
        List<Post> results = new java.util.ArrayList<>();

        for (Post post : allPosts) {
            if (post.getTitre().toLowerCase().contains(keyword.toLowerCase()) ||
                    post.getContenu().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(post);
            }
        }

        return results;
    }
}