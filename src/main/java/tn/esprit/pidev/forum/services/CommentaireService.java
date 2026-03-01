package tn.esprit.pidev.forum.services;

import tn.esprit.pidev.forum.dao.CommentaireDAO;
import tn.esprit.pidev.forum.entities.Commentaire;
import tn.esprit.pidev.forum.utils.BadWordsFilter;  // ✅ NOUVEAU IMPORT
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for comment business logic and validation.
 */
public class CommentaireService {
    private final CommentaireDAO commentaireDAO = new CommentaireDAO();

    public boolean addCommentaire(Commentaire commentaire) {
        if (commentaire.getContenu() == null || commentaire.getContenu().trim().isEmpty()) {
            return false;
        }
        if (commentaire.getContenu().length() < 2) {
            return false;
        }

        // ✅✅✅ NOUVEAU: FILTRER LES BAD WORDS ✅✅✅
        System.out.println("\n🔍 === BAD WORDS FILTER API (Commentaire) === 🔍");
        String originalContent = commentaire.getContenu();
        commentaire.setContenu(BadWordsFilter.filterText(commentaire.getContenu()));

        if (!originalContent.equals(commentaire.getContenu())) {
            System.out.println("⚠️ GROS MOTS DÉTECTÉS dans le commentaire et censurés!");
        } else {
            System.out.println("✅ Commentaire propre - Aucun gros mot!");
        }
        System.out.println("==========================================\n");
        // ✅✅✅ FIN DU FILTRAGE ✅✅✅

        commentaireDAO.addCommentaire(commentaire);
        return true;
    }

    public List<Commentaire> getCommentairesByPost(int id_post) {
        return commentaireDAO.getCommentairesByPost(id_post);
    }

    /**
     * ✅ MÉTIER AVANCÉ: Get only top-level comments (no parent)
     */
    public List<Commentaire> getTopLevelComments(int id_post) {
        return commentaireDAO.getCommentairesByPost(id_post).stream()
                .filter(c -> c.getParent_comment_id() == null)
                .collect(Collectors.toList());
    }

    /**
     * ✅ MÉTIER AVANCÉ: Get replies for a specific comment
     */
    public List<Commentaire> getReplies(int parentCommentId, int id_post) {
        return commentaireDAO.getCommentairesByPost(id_post).stream()
                .filter(c -> c.getParent_comment_id() != null &&
                        c.getParent_comment_id() == parentCommentId)
                .collect(Collectors.toList());
    }

    public Commentaire getCommentaireById(int id) {
        return commentaireDAO.getCommentaireById(id);
    }

    public boolean updateCommentaire(Commentaire commentaire) {
        if (commentaire.getContenu() == null || commentaire.getContenu().trim().isEmpty()) {
            return false;
        }

        // ✅ NOUVEAU: Filtrer lors de la modification
        System.out.println("🔍 Vérification des gros mots...");
        commentaire.setContenu(BadWordsFilter.filterText(commentaire.getContenu()));

        commentaireDAO.updateCommentaire(commentaire);
        return true;
    }

    public void deleteCommentaire(int id) {
        commentaireDAO.deleteCommentaire(id);
    }

    public void likeCommentaire(int id_comment) {
        commentaireDAO.incrementLikes(id_comment);
    }

    public int countCommentsByPost(int id_post) {
        return commentaireDAO.countCommentsByPost(id_post);
    }
}