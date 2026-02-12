package com.silvercare.companion_portal.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	// Keep credentials private so they can’t be accessed directly
	private static final String URL = "jdbc:mysql://localhost:3306/silvercare";
	private static final String USER = "root";
	private static final String PASSWORD = "root1234";

	public static Connection getConnection() {
		Connection conn = null;

		try {
			// Load the MySQL JDBC driver
			Class.forName("com.mysql.cj.jdbc.Driver");

			// Attempt to connect to the database
			conn = DriverManager.getConnection(URL, USER, PASSWORD);
//			System.out.println("Successfully connected to MySQL!");
		}

		// If driver class not found
		catch (ClassNotFoundException e) {
			System.out.println("MySQL JDBC Driver not found!");
			e.printStackTrace();
		}

		// If database connection fails
		catch (SQLException e) {
			System.out.println("Database connection failed!");
			System.out.println("Error Message: " + e.getMessage());
			e.printStackTrace();
		}

		// Return the connection object (null if connection failed)
		return conn;
	}

	// 🔹 Optional: method to safely close the connection
	public static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
				System.out.println("Connection closed successfully.");
			} catch (SQLException e) {
				System.out.println("Error closing connection.");
				e.printStackTrace();
			}
		}
	}
}
