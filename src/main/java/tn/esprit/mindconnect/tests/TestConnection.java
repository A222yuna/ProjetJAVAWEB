package tn.esprit.mindconnect.tests;

import tn.esprit.mindconnect.utils.MyConnection;

public class TestConnection {

    public static void main(String[] args) {

        MyConnection mc = MyConnection.getInstance();

        if (mc.getConnection() != null) {
            System.out.println("Connexion réussie !");
        } else {
            System.out.println("Connexion échouée !");
        }
    }
}
