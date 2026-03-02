package Models;

import java.time.LocalDate;

public class Message {

    private int idMessage;
    private String contenuMessage;
    private LocalDate dateMessage;
    private boolean estLu;
    private int idExpediteur;
    private int idDestinataire;
    private int idConversation;

    // Constructeur vide
    public Message() {
    }

    // Constructeur avec paramètres
    public Message( String contenuMessage, LocalDate dateMessage,
                   boolean estLu, int idExpediteur,
                   int idDestinataire, int idConversation) {
        this.contenuMessage = contenuMessage;
        this.dateMessage = dateMessage;
        this.estLu = estLu;
        this.idExpediteur = idExpediteur;
        this.idDestinataire = idDestinataire;
        this.idConversation = idConversation;
    }

    // Getters et Setters
    public int getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(int idMessage) {
        this.idMessage = idMessage;
    }

    public String getContenuMessage() {
        return contenuMessage;
    }

    public void setContenuMessage(String contenuMessage) {
        this.contenuMessage = contenuMessage;
    }

    public LocalDate getDateMessage() {
        return dateMessage;
    }

    public void setDateMessage(LocalDate dateMessage) {
        this.dateMessage = dateMessage;
    }

    public boolean isEstLu() {
        return estLu;
    }

    public void setEstLu(boolean estLu) {
        this.estLu = estLu;
    }

    public int getIdExpediteur() {
        return idExpediteur;
    }

    public void setIdExpediteur(int idExpediteur) {
        this.idExpediteur = idExpediteur;
    }

    public int getIdDestinataire() {
        return idDestinataire;
    }

    public void setIdDestinataire(int idDestinataire) {
        this.idDestinataire = idDestinataire;
    }

    public int getIdConversation() {
        return idConversation;
    }

    public void setIdConversation(int idConversation) {
        this.idConversation = idConversation;
    }

    @Override
    public String toString() {
        return "Message{" +
                "idMessage=" + idMessage +
                ", contenuMessage='" + contenuMessage + '\'' +
                ", dateMessage=" + dateMessage +
                ", estLu=" + estLu +
                ", idExpediteur=" + idExpediteur +
                ", idDestinataire=" + idDestinataire +
                ", idConversation=" + idConversation +
                '}';
    }
}
