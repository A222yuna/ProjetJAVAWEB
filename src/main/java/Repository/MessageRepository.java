package Repository;

import Models.Message;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private final Connection connection;

    public MessageRepository() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public void save(Message message) {
        String sql = "INSERT INTO message (contenu_message, date_message, est_lu, id_expediteur, id_destinataire, id_conversation) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, message.getContenuMessage());
            ps.setDate(2, Date.valueOf(message.getDateMessage()));
            ps.setBoolean(3, message.isEstLu());
            ps.setInt(4, message.getIdExpediteur());
            ps.setInt(5, message.getIdDestinataire());
            ps.setInt(6, message.getIdConversation());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) message.setIdMessage(rs.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Message findById(int id) {
        String sql = "SELECT * FROM message WHERE id_message = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToMessage(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Message> findAll() {
        String sql = "SELECT * FROM message";
        List<Message> messages = new ArrayList<>();
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return messages;
    }

    public void update(Message message) {
        String sql = "UPDATE message SET contenu_message=?, date_message=?, est_lu=?, id_expediteur=?, id_destinataire=?, id_conversation=? WHERE id_message=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, message.getContenuMessage());
            ps.setDate(2, Date.valueOf(message.getDateMessage()));
            ps.setBoolean(3, message.isEstLu());
            ps.setInt(4, message.getIdExpediteur());
            ps.setInt(5, message.getIdDestinataire());
            ps.setInt(6, message.getIdConversation());
            ps.setInt(7, message.getIdMessage());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        String sql = "DELETE FROM message WHERE id_message = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Helper method to map a ResultSet row to a Message object.
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setIdMessage(rs.getInt("id_message"));
        m.setContenuMessage(rs.getString("contenu_message"));
        m.setDateMessage(rs.getDate("date_message").toLocalDate());
        m.setEstLu(rs.getBoolean("est_lu"));
        m.setIdExpediteur(rs.getInt("id_expediteur"));
        m.setIdDestinataire(rs.getInt("id_destinataire"));
        m.setIdConversation(rs.getInt("id_conversation"));
        return m;
    }
}