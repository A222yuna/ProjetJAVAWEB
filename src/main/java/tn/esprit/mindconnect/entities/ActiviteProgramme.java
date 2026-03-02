package tn.esprit.mindconnect.entities;

import java.sql.Time;

public class ActiviteProgramme {
    private int idActivite;
    private int idProgramme;
    private int jour;
    private Time heureDebut;
    private String titre;
    private String description;
    private int dureeMinutes;
    private String typeActivite;

    public ActiviteProgramme() {
    }

    public ActiviteProgramme(int idActivite, int idProgramme, int jour, Time heureDebut, String titre,
            String description, int dureeMinutes, String typeActivite) {
        this.idActivite = idActivite;
        this.idProgramme = idProgramme;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.titre = titre;
        this.description = description;
        this.dureeMinutes = dureeMinutes;
        this.typeActivite = typeActivite;
    }

    public ActiviteProgramme(int idProgramme, int jour, Time heureDebut, String titre, String description,
            int dureeMinutes, String typeActivite) {
        this.idProgramme = idProgramme;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.titre = titre;
        this.description = description;
        this.dureeMinutes = dureeMinutes;
        this.typeActivite = typeActivite;
    }

    public int getIdActivite() {
        return idActivite;
    }

    public void setIdActivite(int idActivite) {
        this.idActivite = idActivite;
    }

    public int getIdProgramme() {
        return idProgramme;
    }

    public void setIdProgramme(int idProgramme) {
        this.idProgramme = idProgramme;
    }

    public int getJour() {
        return jour;
    }

    public void setJour(int jour) {
        this.jour = jour;
    }

    public Time getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(Time heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(int dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public String getTypeActivite() {
        return typeActivite;
    }

    public void setTypeActivite(String typeActivite) {
        this.typeActivite = typeActivite;
    }

    @Override
    public String toString() {
        return "ActiviteProgramme{" +
                "idActivite=" + idActivite +
                ", idProgramme=" + idProgramme +
                ", jour=" + jour +
                ", heureDebut=" + heureDebut +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", dureeMinutes=" + dureeMinutes +
                ", typeActivite='" + typeActivite + '\'' +
                '}';
    }
}
