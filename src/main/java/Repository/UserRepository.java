package Repository;

import Models.User;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private final Connection connection;

    public UserRepository() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public void save(User user) {
        String sql = "INSERT INTO user (nom_user, sexe_user, dn_user, adresse_user, mail_user, tel_user) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getNomUser());
            ps.setString(2, user.getSexeUser());
            ps.setDate(3, Date.valueOf(user.getDnUser()));
            ps.setString(4, user.getAdresseUser());
            ps.setString(5, user.getMailUser());
            ps.setString(6, user.getTelUser());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) user.setIdUser(rs.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public User findById(int id) {
        String sql = "SELECT * FROM user WHERE id_user = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM user";
        List<User> users = new ArrayList<>();
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) users.add(mapResultSetToUser(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public void update(User user) {
        String sql = "UPDATE user SET nom_user=?, sexe_user=?, dn_user=?, adresse_user=?, mail_user=?, tel_user=? WHERE id_user=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getNomUser());
            ps.setString(2, user.getSexeUser());
            ps.setDate(3, Date.valueOf(user.getDnUser()));
            ps.setString(4, user.getAdresseUser());
            ps.setString(5, user.getMailUser());
            ps.setString(6, user.getTelUser());
            ps.setInt(7, user.getIdUser());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        String sql = "DELETE FROM user WHERE id_user = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setIdUser(rs.getInt("id_user"));
        user.setNomUser(rs.getString("nom_user"));
        user.setSexeUser(rs.getString("sexe_user"));
        user.setDnUser(rs.getDate("dn_user").toLocalDate());
        user.setAdresseUser(rs.getString("adresse_user"));
        user.setMailUser(rs.getString("mail_user"));
        user.setTelUser(rs.getString("tel_user"));
        return user;
    }
}