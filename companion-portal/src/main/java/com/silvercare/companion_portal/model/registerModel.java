package com.silvercare.companion_portal.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Handles user registration logic.
 *
 * Responsibilities: - Insert new customer records into the `customers` table -
 * Hash passwords before storing them
 *
 * Note: - This class currently uses SHA-256 hashing; consider BCrypt for
 * production.
 */
public class registerModel {

	/**
	 * Registers a new user by inserting all form fields into the database.
	 *
	 * @param username    customer's chosen username
	 * @param email       customer's email
	 * @param fullName    customer's full legal name
	 * @param phoneNumber customer's phone number
	 * @param address     customer's address
	 * @param zipcode     customer's postal code
	 * @param password    raw password (will be hashed)
	 *
	 * @return true if insert succeeded, false otherwise
	 */
	public boolean registerUser(String username, String email, String fullName, String phoneNumber, String address,
			String zipcode, String password) {

		boolean result = false;

		// try-with-resources ensures connection auto-closes
		try (Connection conn = DBConnection.getConnection()) {

			String sql = "INSERT INTO customers (username, email, full_name, phone, address, zipcode, password) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement ps = conn.prepareStatement(sql);

			// Set values for the prepared statement
			ps.setString(1, username);
			ps.setString(2, email);
			ps.setString(3, fullName);
			ps.setString(4, phoneNumber);
			ps.setString(5, address);
			ps.setString(6, zipcode);
			ps.setString(7, hashPassword(password)); // Hashing before insert

			int rows = ps.executeUpdate();
			if (rows > 0) {
				result = true; // Insert successful
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Hashes a password using SHA-256.
	 *
	 * WARNING: SHA-256 is a one-way hash with no salt; it is NOT optimal for
	 * production. Consider using BCrypt for real applications.
	 *
	 * @param password raw password from user
	 * @return hashed password as a hex string
	 */
	public String hashPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			// Convert password to SHA-256 hash bytes
			byte[] hashedBytes = md.digest(password.getBytes());

			// Convert bytes → hex string
			StringBuilder sb = new StringBuilder();
			for (byte b : hashedBytes) {
				sb.append(String.format("%02x", b));
			}

			return sb.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
}
