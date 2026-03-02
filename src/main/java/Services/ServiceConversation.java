package Services;

import Models.Conversation;
import Repository.ConversationRepository;
import java.util.List;

public class ServiceConversation implements InterfaceConversation {

    // Dependency on the Repository
    private final ConversationRepository conversationRepository;

    public ServiceConversation() {
        // Direct instantiation of the repository
        this.conversationRepository = new ConversationRepository();
    }

    @Override
    public void createConversation(Conversation conversation) {
        // Business logic can be added here (e.g., validation)
        conversationRepository.save(conversation);
    }

    @Override
    public Conversation getConversationById(int id) {
        // Delegate retrieval to the repository
        return conversationRepository.findById(id);
    }

    @Override
    public List<Conversation> getAllConversations() {
        // Delegate fetching the list to the repository
        return conversationRepository.findAll();
    }

    @Override
    public void updateConversation(Conversation conversation) {
        // Delegate update logic to the repository
        conversationRepository.update(conversation);
    }

    @Override
    public void deleteConversation(int id) {
        // Delegate deletion to the repository
        conversationRepository.delete(id);
    }
}