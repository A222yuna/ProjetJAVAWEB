package tn.psy.gestioncabinet.model;

import java.time.LocalDateTime;

/**
 * Modèle Cabinet
 * Table : cabinet
 */
public class Cabinet {

    private int idCabinet;
    private String adresse;
    private String ville;
    private String horaires;
    private String description;
    private boolean valide;
    private boolean archive;
    private LocalDateTime dateCreation;

    public Cabinet() {
    }

    public Cabinet(String adresse, String ville, String horaires, String description) {
        this.adresse = adresse;
        this.ville = ville;
        this.horaires = horaires;
        this.description = description;
        this.valide = false;
        this.archive = false;
    }

    public Cabinet(int idCabinet, String adresse, String ville, String horaires, String description, boolean valide, boolean archive) {
        this.idCabinet = idCabinet;
        this.adresse = adresse;
        this.ville = ville;
        this.horaires = horaires;
        this.description = description;
        this.valide = valide;
        this.archive = archive;
    }

    public int getIdCabinet() {
        return idCabinet;
    }

    public void setIdCabinet(int idCabinet) {
        this.idCabinet = idCabinet;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getHoraires() {
        return horaires;
    }

    public void setHoraires(String horaires) {
        this.horaires = horaires;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isValide() {
        return valide;
    }

    public void setValide(boolean valide) {
        this.valide = valide;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatutTexte() {
        // Mapping combiné des deux flags vers un statut lisible :
        // - valide = false, archive = false  -> EN_ATTENTE
        // - valide = true,  archive = false  -> ACTIF
        // - valide = false, archive = true   -> REFUSE
        // - valide = true,  archive = true   -> ARCHIVE (cas administratif)
        if (!valide && !archive) {
            return "En attente";
        }
        if (valide && !archive) {
            return "Actif";
        }
        if (!valide && archive) {
            return "Refusé";
        }
        return "Archivé";
    }

    /**
     * Texte pour la colonne Statut dans les tableaux d'administration.
     */
    public String getStatutArchiveTexte() {
        return getStatutTexte();
    }
}
