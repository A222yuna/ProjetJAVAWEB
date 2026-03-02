package org.example.dao;

import org.example.model.Appointment;
import org.example.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public static Appointment getAppointmentById(int id) {
        Appointment appointment = null;
        String query = "SELECT a.*, u.full_name as patient_name, p.full_name as psychologue_name, " +
                "pl.day_of_week, pl.period FROM appointments a " +
                "JOIN users u ON a.patient_id = u.id " +
                "JOIN psychologue_plans pl ON a.plan_id = pl.id " +
                "JOIN users p ON pl.psychologue_id = p.id WHERE a.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                appointment = new Appointment();
                appointment.setId(rs.getInt("id"));
                appointment.setPatientId(rs.getInt("patient_id"));
                appointment.setPlanId(rs.getInt("plan_id"));
                appointment.setStatus(rs.getString("status"));
                appointment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                appointment.setPatientName(rs.getString("patient_name"));
                appointment.setPsychologueName(rs.getString("psychologue_name"));
                appointment.setDayOfWeek(rs.getString("day_of_week"));
                appointment.setPeriod(rs.getString("period"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointment;
    }

    public static List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT a.*, u.full_name as patient_name, p.full_name as psychologue_name, " +
                "pl.day_of_week, pl.period FROM appointments a " +
                "JOIN users u ON a.patient_id = u.id " +
                "JOIN psychologue_plans pl ON a.plan_id = pl.id " +
                "JOIN users p ON pl.psychologue_id = p.id ORDER BY a.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Appointment appointment = new Appointment();
                appointment.setId(rs.getInt("id"));
                appointment.setPatientId(rs.getInt("patient_id"));
                appointment.setPlanId(rs.getInt("plan_id"));
                appointment.setStatus(rs.getString("status"));
                appointment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                appointment.setPatientName(rs.getString("patient_name"));
                appointment.setPsychologueName(rs.getString("psychologue_name"));
                appointment.setDayOfWeek(rs.getString("day_of_week"));
                appointment.setPeriod(rs.getString("period"));
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public static List<Appointment> getAppointmentsByPatient(int patientId) {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT a.*, u.full_name as patient_name, p.full_name as psychologue_name, " +
                "pl.day_of_week, pl.period FROM appointments a " +
                "JOIN users u ON a.patient_id = u.id " +
                "JOIN psychologue_plans pl ON a.plan_id = pl.id " +
                "JOIN users p ON pl.psychologue_id = p.id WHERE a.patient_id = ? ORDER BY a.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Appointment appointment = new Appointment();
                appointment.setId(rs.getInt("id"));
                appointment.setPatientId(rs.getInt("patient_id"));
                appointment.setPlanId(rs.getInt("plan_id"));
                appointment.setStatus(rs.getString("status"));
                appointment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                appointment.setPatientName(rs.getString("patient_name"));
                appointment.setPsychologueName(rs.getString("psychologue_name"));
                appointment.setDayOfWeek(rs.getString("day_of_week"));
                appointment.setPeriod(rs.getString("period"));
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public static List<Appointment> getAppointmentsByPsychologue(int psychologueId) {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT a.*, u.full_name as patient_name, p.full_name as psychologue_name, " +
                "pl.day_of_week, pl.period FROM appointments a " +
                "JOIN users u ON a.patient_id = u.id " +
                "JOIN psychologue_plans pl ON a.plan_id = pl.id " +
                "JOIN users p ON pl.psychologue_id = p.id WHERE pl.psychologue_id = ? ORDER BY a.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, psychologueId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Appointment appointment = new Appointment();
                appointment.setId(rs.getInt("id"));
                appointment.setPatientId(rs.getInt("patient_id"));
                appointment.setPlanId(rs.getInt("plan_id"));
                appointment.setStatus(rs.getString("status"));
                appointment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                appointment.setPatientName(rs.getString("patient_name"));
                appointment.setPsychologueName(rs.getString("psychologue_name"));
                appointment.setDayOfWeek(rs.getString("day_of_week"));
                appointment.setPeriod(rs.getString("period"));
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public static boolean createAppointment(Appointment appointment) {
        String query = "INSERT INTO appointments (patient_id, plan_id, status) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getPlanId());
            ps.setString(3, appointment.getStatus());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateAppointmentStatus(int appointmentId, String status) {
        String query = "UPDATE appointments SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, status);
            ps.setInt(2, appointmentId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteAppointment(int appointmentId) {
        String query = "DELETE FROM appointments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int countAppointmentsForPlan(int planId) {
        String query = "SELECT COUNT(*) FROM appointments WHERE plan_id = ? AND status = 'SCHEDULED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, planId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}