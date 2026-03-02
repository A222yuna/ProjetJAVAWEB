package Models;

import java.time.LocalDate;

public class User {

    private int idUser;
    private String nomUser;
    private String sexeUser;
    private LocalDate dnUser;
    private String adresseUser;
    private String mailUser;
    private String telUser;

    // Constructeur vide
    public User() {
    }

    // Constructeur avec paramètres
    public User(String nomUser, String sexeUser, LocalDate dnUser,
                String adresseUser, String mailUser, String telUser) {
        this.nomUser = nomUser;
        this.sexeUser = sexeUser;
        this.dnUser = dnUser;
        this.adresseUser = adresseUser;
        this.mailUser = mailUser;
        this.telUser = telUser;
    }


    // Getters et Setters
    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNomUser() {
        return nomUser;
    }

    public void setNomUser(String nomUser) {
        this.nomUser = nomUser;
    }

    public String getSexeUser() {
        return sexeUser;
    }

    public void setSexeUser(String sexeUser) {
        this.sexeUser = sexeUser;
    }

    public LocalDate getDnUser() {
        return dnUser;
    }

    public void setDnUser(LocalDate dnUser) {
        this.dnUser = dnUser;
    }

    public String getAdresseUser() {
        return adresseUser;
    }

    public void setAdresseUser(String adresseUser) {
        this.adresseUser = adresseUser;
    }

    public String getMailUser() {
        return mailUser;
    }

    public void setMailUser(String mailUser) {
        this.mailUser = mailUser;
    }

    public String getTelUser() {
        return telUser;
    }

    public void setTelUser(String telUser) {
        this.telUser = telUser;
    }

    @Override
    public String toString() {
        return "User{" +
                "idUser=" + idUser +
                ", nomUser='" + nomUser + '\'' +
                ", sexeUser='" + sexeUser + '\'' +
                ", dnUser=" + dnUser +
                ", adresseUser='" + adresseUser + '\'' +
                ", mailUser='" + mailUser + '\'' +
                ", telUser='" + telUser + '\'' +
                '}';
    }
}
