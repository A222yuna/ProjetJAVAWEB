package Services;
import Models.Conversation;

import java.util.List;

public interface InterfaceConversation {

    void createConversation(Conversation conversation);

    Conversation getConversationById(int id);

    List<Conversation> getAllConversations();

    void updateConversation(Conversation conversation);

    void deleteConversation(int id);
}