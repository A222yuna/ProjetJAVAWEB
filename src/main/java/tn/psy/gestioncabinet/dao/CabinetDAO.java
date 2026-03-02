package tn.psy.gestioncabinet.dao;

import tn.psy.gestioncabinet.model.Cabinet;
import tn.psy.gestioncabinet.util.Connexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO Cabinet - CRUD complet
 */
public class CabinetDAO {

    public int ajouter(Cabinet c) {
        String sql = "INSERT INTO cabinet (adresse, ville, horaires, description, valide, archive) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getAdresse());
            ps.setString(2, c.getVille());
            ps.setString(3, c.getHoraires());
            ps.setString(4, c.getDescription());
            ps.setBoolean(5, c.isValide());
            ps.setBoolean(6, c.isArchive());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout cabinet : " + e.getMessage());
        }
        return -1;
    }

    public List<Cabinet> findAll() {
        List<Cabinet> liste = new ArrayList<>();
        String sql = "SELECT id_cabinet, adresse, ville, horaires, description, valide, archive FROM cabinet ORDER BY id_cabinet";
        try (Connection conn = Connexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture cabinets : " + e.getMessage());
        }
        return liste;
    }

    public List<Cabinet> findAllValides() {
        List<Cabinet> liste = new ArrayList<>();
        String sql = "SELECT id_cabinet, adresse, ville, horaires, description, valide, archive FROM cabinet WHERE valide = TRUE ORDER BY ville, adresse";
        try (Connection conn = Connexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture cabinets validés : " + e.getMessage());
        }
        return liste;
    }

    public List<Cabinet> rechercherParVille(String ville) {
        List<Cabinet> liste = new ArrayList<>();
        String sql = "SELECT id_cabinet, adresse, ville, horaires, description, valide, archive FROM cabinet WHERE valide = TRUE AND LOWER(ville) LIKE LOWER(?) ORDER BY ville";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + ville + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche cabinet par ville : " + e.getMessage());
        }
        return liste;
    }

    public List<Cabinet> rechercher(String critere) {
        List<Cabinet> liste = new ArrayList<>();
        String sql = "SELECT id_cabinet, adresse, ville, horaires, description, valide, archive FROM cabinet WHERE valide = TRUE AND (LOWER(ville) LIKE LOWER(?) OR LOWER(adresse) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?)) ORDER BY ville";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + critere + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche cabinet : " + e.getMessage());
        }
        return liste;
    }

    public Optional<Cabinet> findById(int id) {
        String sql = "SELECT id_cabinet, adresse, ville, horaires, description, valide, archive FROM cabinet WHERE id_cabinet = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche cabinet : " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean modifier(Cabinet c) {
        String sql = "UPDATE cabinet SET adresse = ?, ville = ?, horaires = ?, description = ?, valide = ?, archive = ? WHERE id_cabinet = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getAdresse());
            ps.setString(2, c.getVille());
            ps.setString(3, c.getHoraires());
            ps.setString(4, c.getDescription());
            ps.setBoolean(5, c.isValide());
            ps.setBoolean(6, c.isArchive());
            ps.setInt(7, c.getIdCabinet());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur modification cabinet : " + e.getMessage());
            return false;
        }
    }

    public boolean valider(int idCabinet) {
        String sql = "UPDATE cabinet SET valide = TRUE WHERE id_cabinet = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCabinet);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur validation cabinet : " + e.getMessage());
            return false;
        }
    }

    /**
     * Archivage d'un cabinet (marqué comme archivé, mais non supprimé).
     */
    public boolean archiver(int idCabinet) {
        String sql = "UPDATE cabinet SET archive = TRUE WHERE id_cabinet = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCabinet);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur archivage cabinet : " + e.getMessage());
            return false;
        }
    }

    /**
     * Désarchivage d'un cabinet.
     */
    public boolean desarchiver(int idCabinet) {
        String sql = "UPDATE cabinet SET archive = FALSE WHERE id_cabinet = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCabinet);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur désarchivage cabinet : " + e.getMessage());
            return false;
        }
    }

    /**
     * Bascule l'état d'archivage d'un cabinet (TRUE -> FALSE ou FALSE -> TRUE).
     * Le cabinet reste visible partout ; aucun DELETE.
     */
    public boolean toggleArchive(int idCabinet) {
        Optional<Cabinet> opt = findById(idCabinet);
        if (opt.isEmpty()) {
            return false;
        }
        boolean currentArchive = opt.get().isArchive();
        return currentArchive ? desarchiver(idCabinet) : archiver(idCabinet);
    }

    /**
     * Suppression physique d'un cabinet UNIQUEMENT s'il n'est pas validé.
     * Utilisé pour les cabinets en attente (valide = FALSE).
     */
    public boolean supprimer(int id) {
        String sql = "DELETE FROM cabinet WHERE id_cabinet = ? AND valide = FALSE";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression cabinet : " + e.getMessage());
            return false;
        }
    }

    /**
     * Suppression sans condition de validation (utilisée par l'administrateur).
     */
    public boolean supprimerAdmin(int id) {
        String sql = "DELETE FROM cabinet WHERE id_cabinet = ?";
        try (Connection conn = Connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression cabinet (admin) : " + e.getMessage());
            return false;
        }
    }

    /**
     * Cabinets non archivés (actifs), quel que soit leur statut de validation.
     */
    public List<Cabinet> findAllNonArchives() {
        List<Cabinet> liste = new ArrayList<>();
        String sql = "SELECT id_cabinet, adresse, ville, horaires, description, valide, archive FROM cabinet WHERE archive = FALSE ORDER BY id_cabinet";
        try (Connection conn = Connexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture cabinets non archivés : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Cabinets archivés.
     */
    public List<Cabinet> findAllArchives() {
        List<Cabinet> liste = new ArrayList<>();
        String sql = "SELECT id_cabinet, adresse, ville, horaires, description, valide, archive FROM cabinet WHERE archive = TRUE ORDER BY id_cabinet";
        try (Connection conn = Connexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture cabinets archivés : " + e.getMessage());
        }
        return liste;
    }

    private Cabinet mapResultSet(ResultSet rs) throws SQLException {
        Cabinet c = new Cabinet(
                rs.getInt("id_cabinet"),
                rs.getString("adresse"),
                rs.getString("ville"),
                rs.getString("horaires"),
                rs.getString("description"),
                rs.getBoolean("valide"),
                rs.getBoolean("archive")
        );
        // date_creation optionnel : si la colonne existe en base, on peut l'ajouter plus tard
        try {
            Timestamp ts = rs.getTimestamp("date_creation");
            if (ts != null) {
                c.setDateCreation(ts.toLocalDateTime());
            }
        } catch (SQLException ignored) {
            // Colonne date_creation absente : on laisse dateCreation à null
        }
        return c;
    }
}
