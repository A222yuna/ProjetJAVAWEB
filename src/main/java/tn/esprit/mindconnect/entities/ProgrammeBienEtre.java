package tn.esprit.mindconnect.entities;

public class ProgrammeBienEtre {
    private int idProgramme;
    private int idPsychologue;
    private String nom;
    private String objectif;
    private int duree;
    private String statut;
    private String image;
    private String niveauDifficulte;

    public ProgrammeBienEtre() {
    }

    public ProgrammeBienEtre(int idProgramme, int idPsychologue, String nom, String objectif, int duree, String statut,
            String image, String niveauDifficulte) {
        this.idProgramme = idProgramme;
        this.idPsychologue = idPsychologue;
        this.nom = nom;
        this.objectif = objectif;
        this.duree = duree;
        this.statut = statut;
        this.image = image;
        this.niveauDifficulte = niveauDifficulte;
    }

    public ProgrammeBienEtre(int idPsychologue, String nom, String objectif, int duree, String statut, String image,
            String niveauDifficulte) {
        this.idPsychologue = idPsychologue;
        this.nom = nom;
        this.objectif = objectif;
        this.duree = duree;
        this.statut = statut;
        this.image = image;
        this.niveauDifficulte = niveauDifficulte;
    }

    public int getIdProgramme() {
        return idProgramme;
    }

    public void setIdProgramme(int idProgramme) {
        this.idProgramme = idProgramme;
    }

    public int getIdPsychologue() {
        return idPsychologue;
    }

    public void setIdPsychologue(int idPsychologue) {
        this.idPsychologue = idPsychologue;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getObjectif() {
        return objectif;
    }

    public void setObjectif(String objectif) {
        this.objectif = objectif;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNiveauDifficulte() {
        return niveauDifficulte;
    }

    public void setNiveauDifficulte(String niveauDifficulte) {
        this.niveauDifficulte = niveauDifficulte;
    }

    @Override
    public String toString() {
        return "ProgrammeBienEtre{" +
                "idProgramme=" + idProgramme +
                ", idPsychologue=" + idPsychologue +
                ", nom='" + nom + '\'' +
                ", objectif='" + objectif + '\'' +
                ", duree=" + duree +
                ", statut='" + statut + '\'' +
                ", image='" + image + '\'' +
                ", niveauDifficulte='" + niveauDifficulte + '\'' +
                '}';
    }
}
