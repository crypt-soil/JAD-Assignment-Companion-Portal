package com.silvercare.companion_portal.model.partner;

import java.sql.*;
import java.time.LocalDateTime;

import com.silvercare.companion_portal.model.DBConnection;
import com.silvercare.companion_portal.model.registerModel;

public class PartnerAuthDAO {

    public PartnerInfo validateLogin(String identifier, String passwordPlain) {
        String sql =
            "SELECT partner_id, username, email, company_name " +
            "FROM partner_user " +
            "WHERE is_active = 1 AND (username = ? OR email = ?) AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            registerModel rm = new registerModel();
            String hashed = rm.hashPassword(passwordPlain);

            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            stmt.setString(3, hashed);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PartnerInfo p = new PartnerInfo();
                    p.partnerId = rs.getInt("partner_id");
                    p.username = rs.getString("username");
                    p.email = rs.getString("email");
                    p.companyName = rs.getString("company_name");
                    return p;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String createToken(int partnerId, int minutesValid) {
        String token = TokenUtil.randomToken(60);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(minutesValid);

        String sql = "INSERT INTO partner_tokens (token, partner_id, expires_at) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            stmt.setInt(2, partnerId);
            stmt.setTimestamp(3, Timestamp.valueOf(expiresAt));
            stmt.executeUpdate();
            return token;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Integer validateToken(String token) {
        String sql =
            "SELECT partner_id " +
            "FROM partner_tokens " +
            "WHERE token = ? AND expires_at > NOW()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("partner_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean deleteToken(String token) {
        String sql = "DELETE FROM partner_tokens WHERE token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
