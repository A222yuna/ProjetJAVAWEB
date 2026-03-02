package tn.psy.gestioncabinet.model;

/**
 * Modèle Administrateur - pour authentification
 * Table : administrateur
 */
public class Administrateur {

    private int idAdmin;
    private String nom;
    private String email;
    private String motDePasse;

    public Administrateur() {
    }

    public Administrateur(String nom, String email, String motDePasse) {
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
    }

    public Administrateur(int idAdmin, String nom, String email, String motDePasse) {
        this.idAdmin = idAdmin;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
}
