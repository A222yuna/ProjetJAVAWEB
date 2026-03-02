package tn.psy.gestioncabinet.model;

import java.time.LocalDate;

/**
 * Modèle PsyCabinet - Table de liaison Psychologue-Cabinet
 * Table : psy_cabinet
 */
public class PsyCabinet {

    private int idPsy;
    private int idCabinet;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    // Champs supplémentaires pour affichage
    private String nomPsychologue;
    private String adresseCabinet;
    private String villeCabinet;

    public PsyCabinet() {
    }

    public PsyCabinet(int idPsy, int idCabinet, LocalDate dateDebut, LocalDate dateFin) {
        this.idPsy = idPsy;
        this.idCabinet = idCabinet;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public int getIdPsy() {
        return idPsy;
    }

    public void setIdPsy(int idPsy) {
        this.idPsy = idPsy;
    }

    public int getIdCabinet() {
        return idCabinet;
    }

    public void setIdCabinet(int idCabinet) {
        this.idCabinet = idCabinet;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getNomPsychologue() {
        return nomPsychologue;
    }

    public void setNomPsychologue(String nomPsychologue) {
        this.nomPsychologue = nomPsychologue;
    }

    public String getAdresseCabinet() {
        return adresseCabinet;
    }

    public void setAdresseCabinet(String adresseCabinet) {
        this.adresseCabinet = adresseCabinet;
    }

    public String getVilleCabinet() {
        return villeCabinet;
    }

    public void setVilleCabinet(String villeCabinet) {
        this.villeCabinet = villeCabinet;
    }
}
