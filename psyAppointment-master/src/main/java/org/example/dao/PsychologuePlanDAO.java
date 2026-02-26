package org.example.dao;

import org.example.model.PsychologuePlan;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PsychologuePlanDAO {

    public static PsychologuePlan getPlanById(int id) {
        PsychologuePlan plan = null;
        String query = "SELECT * FROM psychologue_plans WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                plan = new PsychologuePlan();
                plan.setId(rs.getInt("id"));
                plan.setPsychologueId(rs.getInt("psychologue_id"));
                plan.setDayOfWeek(rs.getString("day_of_week"));
                plan.setPeriod(rs.getString("period"));
                plan.setMaxAppointments(rs.getInt("max_appointments"));
                plan.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plan;
    }

    public static List<PsychologuePlan> getPlansByPsychologue(int psychologueId) {
        List<PsychologuePlan> plans = new ArrayList<>();
        String query = "SELECT * FROM psychologue_plans WHERE psychologue_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, psychologueId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PsychologuePlan plan = new PsychologuePlan();
                plan.setId(rs.getInt("id"));
                plan.setPsychologueId(rs.getInt("psychologue_id"));
                plan.setDayOfWeek(rs.getString("day_of_week"));
                plan.setPeriod(rs.getString("period"));
                plan.setMaxAppointments(rs.getInt("max_appointments"));
                plan.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                plans.add(plan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plans;
    }

    public static List<PsychologuePlan> getAllPlans() {
        List<PsychologuePlan> plans = new ArrayList<>();
        String query = "SELECT * FROM psychologue_plans";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                PsychologuePlan plan = new PsychologuePlan();
                plan.setId(rs.getInt("id"));
                plan.setPsychologueId(rs.getInt("psychologue_id"));
                plan.setDayOfWeek(rs.getString("day_of_week"));
                plan.setPeriod(rs.getString("period"));
                plan.setMaxAppointments(rs.getInt("max_appointments"));
                plan.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                plans.add(plan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plans;
    }

    public static boolean createPlan(PsychologuePlan plan) {
        String query = "INSERT INTO psychologue_plans (psychologue_id, day_of_week, period, max_appointments) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, plan.getPsychologueId());
            ps.setString(2, plan.getDayOfWeek());
            ps.setString(3, plan.getPeriod());
            ps.setInt(4, plan.getMaxAppointments());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deletePlan(int planId) {
        String query = "DELETE FROM psychologue_plans WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, planId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}