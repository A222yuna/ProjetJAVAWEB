package Models;

import java.time.LocalDate;

public class Conversation {

    private int idConversation;
    private LocalDate dateCreation;
    private String statutConversation;
    private boolean archiverConversation;

    // Constructeur vide
    public Conversation() {
    }

    // Constructeur avec paramètres
    public Conversation( LocalDate dateCreation,
                        String statutConversation, boolean archiverConversation) {
        this.dateCreation = dateCreation;
        this.statutConversation = statutConversation;
        this.archiverConversation = archiverConversation;
    }

    // Getters et Setters
    public int getIdConversation() {
        return idConversation;
    }

    public void setIdConversation(int idConversation) {
        this.idConversation = idConversation;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatutConversation() {
        return statutConversation;
    }

    public void setStatutConversation(String statutConversation) {
        this.statutConversation = statutConversation;
    }

    public boolean isArchiverConversation() {
        return archiverConversation;
    }

    public void setArchiverConversation(boolean archiverConversation) {
        this.archiverConversation = archiverConversation;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "idConversation=" + idConversation +
                ", dateCreation=" + dateCreation +
                ", statutConversation='" + statutConversation + '\'' +
                ", archiverConversation=" + archiverConversation +
                '}';
    }
}
