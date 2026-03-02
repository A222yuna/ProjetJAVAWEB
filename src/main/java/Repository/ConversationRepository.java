package Repository;
import Models.Conversation;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationRepository {
    private final Connection connection;

    public ConversationRepository() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public void save(Conversation conversation) {
        String sql = "INSERT INTO conversation (date_creation, statut_conversation, archiver_conversation) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(conversation.getDateCreation()));
            ps.setString(2, conversation.getStatutConversation());
            ps.setBoolean(3, conversation.isArchiverConversation());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                conversation.setIdConversation(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Conversation findById(int id) {
        String sql = "SELECT * FROM conversation WHERE id_conversation = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToConversation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Conversation> findAll() {
        String sql = "SELECT * FROM conversation";
        List<Conversation> conversations = new ArrayList<>();
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                conversations.add(mapResultSetToConversation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    public void update(Conversation conversation) {
        String sql = "UPDATE conversation SET date_creation=?, statut_conversation=?, archiver_conversation=? WHERE id_conversation=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(conversation.getDateCreation()));
            ps.setString(2, conversation.getStatutConversation());
            ps.setBoolean(3, conversation.isArchiverConversation());
            ps.setInt(4, conversation.getIdConversation());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM conversation WHERE id_conversation = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Conversation mapResultSetToConversation(ResultSet rs) throws SQLException {
        Conversation conv = new Conversation();
        conv.setIdConversation(rs.getInt("id_conversation"));
        conv.setDateCreation(rs.getDate("date_creation").toLocalDate());
        conv.setStatutConversation(rs.getString("statut_conversation"));
        conv.setArchiverConversation(rs.getBoolean("archiver_conversation"));
        return conv;
    }
}