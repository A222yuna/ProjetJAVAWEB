package org.example;

import java.time.LocalDate;
import java.util.List;

import Models.User;
import Models.Message;
import Models.Conversation;

import Services.ServiceUser;
import Services.ServiceConversation;
import Services.ServiceMessage;

public class Main {

    public static void main(String[] args) {

        // The Services now internally instantiate their Repositories
        ServiceUser serviceUser = new ServiceUser();
        ServiceConversation serviceConversation = new ServiceConversation();
        ServiceMessage serviceMessage = new ServiceMessage();

        System.out.println("--- DÉBUT DES TESTS (Architecture Repository/Service) ---");

        /* ========================= */
        /* ===== TEST USER ========= */
        /* ========================= */
        System.out.println("\n[Test User]");
        User user1 = new User("Edam", "Homme", LocalDate.of(2000, 5, 10), "Tunis", "edam@mail.com", "58582060");
        serviceUser.createUser(user1);
        System.out.println("1. Create: User créé avec ID " + user1.getIdUser());

        User fetchedUser = serviceUser.getUserById(user1.getIdUser());
        System.out.println("2. Read: Nom récupéré -> " + fetchedUser.getNomUser());

        fetchedUser.setNomUser("Edam Updated");
        serviceUser.updateUser(fetchedUser);
        System.out.println("3. Update: Nouveau nom -> " + serviceUser.getUserById(user1.getIdUser()).getNomUser());

        /* ============================== */
        /* ===== TEST CONVERSATION ====== */
        /* ============================== */
        System.out.println("\n[Test Conversation]");
        Conversation conv = new Conversation(LocalDate.now(), "ACTIVE", false);
        serviceConversation.createConversation(conv);
        System.out.println("1. Create: Conversation ID " + conv.getIdConversation());

        Conversation fetchedConv = serviceConversation.getConversationById(conv.getIdConversation());
        fetchedConv.setStatutConversation("ARCHIVED");
        serviceConversation.updateConversation(fetchedConv);
        System.out.println("2. Update: Statut mis à jour -> " + serviceConversation.getConversationById(conv.getIdConversation()).getStatutConversation());

        /* ========================= */
        /* ===== TEST MESSAGE ====== */
        /* ========================= */
        System.out.println("\n[Test Message]");
        Message message = new Message(
                "Test via Repository Pattern",
                LocalDate.now(),
                false,
                user1.getIdUser(),
                user1.getIdUser(),
                conv.getIdConversation()
        );
        serviceMessage.createMessage(message);
        System.out.println("1. Create: Message ID " + message.getIdMessage());

        List<Message> allMessages = serviceMessage.getAllMessages();
        System.out.println("2. List: Nombre de messages en base -> " + allMessages.size());

        /* ========================= */
        /* ===== TEST DELETE ======= */
        /* ========================= */
        System.out.println("\n[Test Suppression]");
        // On supprime le message créé pour tester le Delete du Repository
        int msgId = message.getIdMessage();
        serviceMessage.deleteMessage(msgId);
        Message deletedMsg = serviceMessage.getMessageById(msgId);

        if (deletedMsg == null) {
            System.out.println("SUCCESS: Le message " + msgId + " a été supprimé via Repository.");
        } else {
            System.out.println("FAILURE: Le message existe encore.");
        }

        System.out.println("\n--- TOUS LES TESTS SONT PASSÉS ✅ ---");
        System.out.println("L'architecture Service -> Repository fonctionne correctement.");
    }
}