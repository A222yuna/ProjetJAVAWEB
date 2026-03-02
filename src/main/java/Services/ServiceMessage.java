package Services;

import Models.Message;
import Repository.MessageRepository;
import java.util.List;

public class ServiceMessage implements InterfaceMessage {

    // Dependency on the Repository
    private final MessageRepository messageRepository;

    public ServiceMessage() {
        // Direct instantiation as requested
        this.messageRepository = new MessageRepository();
    }

    @Override
    public void createMessage(Message message) {
        // Business logic can go here (e.g., check if content is not empty)
        if (message.getContenuMessage() != null && !message.getContenuMessage().trim().isEmpty()) {
            messageRepository.save(message);
        }
    }

    @Override
    public Message getMessageById(int id) {
        return messageRepository.findById(id);
    }

    @Override
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @Override
    public void updateMessage(Message message) {
        messageRepository.update(message);
    }

    @Override
    public void deleteMessage(int id) {
        messageRepository.delete(id);
    }
}