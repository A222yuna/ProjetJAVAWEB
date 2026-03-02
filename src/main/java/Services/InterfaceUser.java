package Services;
import Models.User;
import java.util.List;

public interface InterfaceUser {

    void createUser(User user);

    User getUserById(int id);

    List<User> getAllUsers();

    void updateUser(User user);

    void deleteUser(int id);
}
