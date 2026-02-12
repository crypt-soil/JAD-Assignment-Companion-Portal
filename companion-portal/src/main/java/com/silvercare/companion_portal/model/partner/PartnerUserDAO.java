package com.silvercare.companion_portal.model.partner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.silvercare.companion_portal.model.DBConnection;
import com.silvercare.companion_portal.model.registerModel;

public class PartnerUserDAO {

    public Integer validatePartnerAndGetId(String usernameOrEmail, String password) {
        Integer partnerId = null;

        try (Connection conn = DBConnection.getConnection()) {
            registerModel rm = new registerModel();
            String hashedPassword = rm.hashPassword(password);

            String sql = """
                SELECT partner_id
                FROM partner_user
                WHERE is_active = 1
                  AND (username = ?)
                  AND password = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, hashedPassword);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                partnerId = rs.getInt("partner_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return partnerId;
    }

    public String getCompanyName(int partnerId) {
        String name = null;

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT company_name FROM partner_user WHERE partner_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, partnerId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                name = rs.getString("company_name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return name;
    }
}
