package tn.esprit.pidev.forum.entities;

import java.time.LocalDateTime;

public class Commentaire {
    
    private int id_comment;
    private int id_post;
    private int id_auteur;
    private String contenu;
    private LocalDateTime date;
    private int nb_likes;
    
    // ✅ NEW: For reply threading
    private Integer parent_comment_id;  // NULL if top-level comment

    // Constructors
    public Commentaire() {}
    
    // Constructor for new comment (top-level)
    public Commentaire(int id_post, int id_auteur, String contenu) {
        this.id_post = id_post;
        this.id_auteur = id_auteur;
        this.contenu = contenu;
        this.date = LocalDateTime.now();
        this.nb_likes = 0;
        this.parent_comment_id = null;  // Top-level by default
    }
    
    // ✅ NEW: Constructor for reply to another comment
    public Commentaire(int id_post, int id_auteur, String contenu, Integer parent_comment_id) {
        this.id_post = id_post;
        this.id_auteur = id_auteur;
        this.contenu = contenu;
        this.date = LocalDateTime.now();
        this.nb_likes = 0;
        this.parent_comment_id = parent_comment_id;
    }
    
    // Getters & Setters
    public int getId_comment() {
        return id_comment;
    }
    
    public void setId_comment(int id_comment) {
        this.id_comment = id_comment;
    }
    
    public int getId_post() {
        return id_post;
    }
    
    public void setId_post(int id_post) {
        this.id_post = id_post;
    }
    
    public int getId_auteur() {
        return id_auteur;
    }
    
    public void setId_auteur(int id_auteur) {
        this.id_auteur = id_auteur;
    }
    
    public String getContenu() {
        return contenu;
    }
    
    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public int getNb_likes() {
        return nb_likes;
    }
    
    public void setNb_likes(int nb_likes) {
        this.nb_likes = nb_likes;
    }
    
    // ✅ NEW: Getters & Setters for parent_comment_id
    public Integer getParent_comment_id() {
        return parent_comment_id;
    }
    
    public void setParent_comment_id(Integer parent_comment_id) {
        this.parent_comment_id = parent_comment_id;
    }
    
    // ✅ NEW: Helper method to check if this is a reply
    public boolean isReply() {
        return parent_comment_id != null;
    }
    
    @Override
    public String toString() {
        return "Commentaire{" +
                "id_comment=" + id_comment +
                ", id_post=" + id_post +
                ", contenu='" + contenu + '\'' +
                ", parent_comment_id=" + parent_comment_id +
                '}';
    }
}
