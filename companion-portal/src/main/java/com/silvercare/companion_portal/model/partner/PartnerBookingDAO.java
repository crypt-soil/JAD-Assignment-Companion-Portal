package com.silvercare.companion_portal.model.partner;

import java.sql.*;
import java.util.*;

import com.silvercare.companion_portal.model.DBConnection;

public class PartnerBookingDAO {

	public List<Map<String, Object>> getPartnerBookings(int partnerId) {

		List<Map<String, Object>> list = new ArrayList<>();

		String sql = "SELECT " + "  bd.detail_id, bd.booking_id, bd.service_id, "
				+ "  b.booking_date, b.status AS booking_status, " + "  c.customer_id, c.full_name, c.phone, "
				+ "  s.name AS service_name, "
				+ "  bd.start_time, bd.end_time, bd.subtotal, bd.special_request, bd.caregiver_status "
				+ "FROM booking_details bd " + "JOIN bookings b   ON bd.booking_id = b.booking_id "
				+ "JOIN customers c  ON b.customer_id = c.customer_id "
				+ "JOIN service s    ON bd.service_id = s.service_id " + "WHERE bd.partner_id = ? "
				+ "ORDER BY bd.start_time DESC";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, partnerId);

			try (ResultSet rs = stmt.executeQuery()) {

				while (rs.next()) {
					Map<String, Object> row = new HashMap<>();

					row.put("detailId", rs.getInt("detail_id"));
					row.put("bookingId", rs.getInt("booking_id"));
					row.put("serviceId", rs.getInt("service_id"));

					row.put("bookingDate", rs.getTimestamp("booking_date"));
					row.put("bookingStatus", rs.getInt("booking_status"));

					row.put("customerId", rs.getInt("customer_id"));
					row.put("customerName", rs.getString("full_name"));
					row.put("customerPhone", rs.getString("phone"));

					row.put("serviceName", rs.getString("service_name"));

					row.put("startTime", rs.getTimestamp("start_time"));
					row.put("endTime", rs.getTimestamp("end_time"));
					row.put("subtotal", rs.getBigDecimal("subtotal"));
					row.put("specialRequest", rs.getString("special_request"));
					row.put("caregiverStatus", rs.getInt("caregiver_status"));

					list.add(row);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	// ====== DETAIL: job + customer + medical (single row) ======
	public Map<String, Object> getPartnerBookingDetail(int partnerId, int detailId) {

		String sql = "SELECT " + "  bd.detail_id, bd.booking_id, bd.service_id, "
				+ "  b.booking_date, b.status AS booking_status, " + "  s.name AS service_name, "
				+ "  bd.start_time, bd.end_time, bd.subtotal, bd.special_request, bd.caregiver_status, "
				+ "  c.customer_id, c.full_name, c.email, c.phone, c.address, c.zipcode, "
				+ "  mi.conditions_csv, mi.allergies_text " + "FROM booking_details bd "
				+ "JOIN bookings b ON bd.booking_id = b.booking_id "
				+ "JOIN customers c ON b.customer_id = c.customer_id "
				+ "JOIN service s ON bd.service_id = s.service_id "
				+ "LEFT JOIN customer_medical_info mi ON mi.customer_id = c.customer_id "
				+ "WHERE bd.partner_id = ? AND bd.detail_id = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, partnerId);
			stmt.setInt(2, detailId);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					Map<String, Object> d = new HashMap<>();

					d.put("detailId", rs.getInt("detail_id"));
					d.put("bookingId", rs.getInt("booking_id"));
					d.put("serviceId", rs.getInt("service_id"));

					d.put("bookingDate", rs.getTimestamp("booking_date"));
					d.put("bookingStatus", rs.getInt("booking_status"));

					d.put("serviceName", rs.getString("service_name"));
					d.put("startTime", rs.getTimestamp("start_time"));
					d.put("endTime", rs.getTimestamp("end_time"));
					d.put("subtotal", rs.getBigDecimal("subtotal"));
					d.put("specialRequest", rs.getString("special_request"));
					d.put("caregiverStatus", rs.getInt("caregiver_status"));

					d.put("customerId", rs.getInt("customer_id"));
					d.put("customerName", rs.getString("full_name"));
					d.put("customerEmail", rs.getString("email"));
					d.put("customerPhone", rs.getString("phone"));
					d.put("customerAddress", rs.getString("address"));
					d.put("customerZipcode", rs.getString("zipcode"));

					// medical fields (can be null)
					d.put("conditionsCsv", rs.getString("conditions_csv"));
					d.put("allergiesText", rs.getString("allergies_text"));

					return d;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// ====== Emergency contacts for customer ======
	public List<Map<String, Object>> getEmergencyContacts(int customerId) {
		List<Map<String, Object>> list = new ArrayList<>();

		String sql = "SELECT contact_id, contact_name, relationship, phone, email " + "FROM emergency_contacts "
				+ "WHERE customer_id = ? " + "ORDER BY contact_id ASC";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, customerId);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Map<String, Object> c = new HashMap<>();
					c.put("contactId", rs.getInt("contact_id"));
					c.put("contactName", rs.getString("contact_name"));
					c.put("relationship", rs.getString("relationship"));
					c.put("phone", rs.getString("phone"));
					c.put("email", rs.getString("email"));
					list.add(c);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}
}
