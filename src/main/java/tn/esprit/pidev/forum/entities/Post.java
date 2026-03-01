package tn.esprit.pidev.forum.entities;

import java.time.LocalDateTime;

public class Post {
    private int id_post;
    private int id_auteur;
    private String titre;
    private String contenu;
    private String categorie;
    private int nb_likes;
    private LocalDateTime date;

    // Constructeur vide
    public Post() {
    }

    // Constructeur avec paramètres (sans id - pour l'insertion)
    public Post(int id_auteur, String titre, String contenu, String categorie) {
        this.id_auteur = id_auteur;
        this.titre = titre;
        this.contenu = contenu;
        this.categorie = categorie;
        this.nb_likes = 0;
        this.date = LocalDateTime.now();
    }

    // Constructeur complet (avec id - pour récupération de la DB)
    public Post(int id_post, int id_auteur, String titre, String contenu,
                String categorie, int nb_likes, LocalDateTime date) {
        this.id_post = id_post;
        this.id_auteur = id_auteur;
        this.titre = titre;
        this.contenu = contenu;
        this.categorie = categorie;
        this.nb_likes = nb_likes;
        this.date = date;
    }

    // Getters et Setters
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

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public int getNb_likes() {
        return nb_likes;
    }

    public void setNb_likes(int nb_likes) {
        this.nb_likes = nb_likes;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id_post=" + id_post +
                ", id_auteur=" + id_auteur +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", categorie='" + categorie + '\'' +
                ", nb_likes=" + nb_likes +
                ", date=" + date +
                '}';
    }
}