package tn.esprit.mindconnect.entities;

import java.sql.Date;

public class Avis {
    private int idAvis;
    private int idProgramme;
    private int idPsychologue;
    private int note;
    private String commentaire;
    private Date dateAvis;

    public Avis() {
    }

    public Avis(int idAvis, int idProgramme, int idPsychologue, int note, String commentaire, Date dateAvis) {
        this.idAvis = idAvis;
        this.idProgramme = idProgramme;
        this.idPsychologue = idPsychologue;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
    }

    public Avis(int idProgramme, int idPsychologue, int note, String commentaire, Date dateAvis) {
        this.idProgramme = idProgramme;
        this.idPsychologue = idPsychologue;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
    }

    public int getIdAvis() {
        return idAvis;
    }

    public void setIdAvis(int idAvis) {
        this.idAvis = idAvis;
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

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Date getDateAvis() {
        return dateAvis;
    }

    public void setDateAvis(Date dateAvis) {
        this.dateAvis = dateAvis;
    }

    @Override
    public String toString() {
        return "Avis{" +
                "idAvis=" + idAvis +
                ", idProgramme=" + idProgramme +
                ", idPsychologue=" + idPsychologue +
                ", note=" + note +
                ", commentaire='" + commentaire + '\'' +
                ", dateAvis=" + dateAvis +
                '}';
    }
}
