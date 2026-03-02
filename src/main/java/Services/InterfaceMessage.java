package Services;
import Models.Message;

import java.util.List;

public interface InterfaceMessage {

    void createMessage(Message message);

    Message getMessageById(int id);

    List<Message> getAllMessages();

    void updateMessage(Message message);

    void deleteMessage(int id);
}
