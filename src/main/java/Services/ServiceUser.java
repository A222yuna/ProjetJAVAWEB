package Services;

import Models.User;
import Repository.UserRepository;
import java.util.List;

public class ServiceUser implements InterfaceUser {

    // Dependency on the Repository
    private final UserRepository userRepository;

    public ServiceUser() {
        // Direct instantiation as requested
        this.userRepository = new UserRepository();
    }

    @Override
    public void createUser(User user) {
        // Business logic (e.g., checking if user already exists or email is valid)
        userRepository.save(user);
    }

    @Override
    public User getUserById(int id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void updateUser(User user) {
        userRepository.update(user);
    }

    @Override
    public void deleteUser(int id) {
        userRepository.delete(id);
    }
}