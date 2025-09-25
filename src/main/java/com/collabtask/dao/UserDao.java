package com.collabtask.dao;

import com.collabtask.app.DataSourceProvider;
import com.collabtask.model.Models.User;

import java.sql.*;
import java.time.Instant;

public class UserDao {
	public User findByEmail(String email) throws SQLException {
		String sql = "SELECT id, email, name, password_hash, created_at FROM users WHERE email = ?";
		try (Connection c = DataSourceProvider.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return map(rs);
			}
		}
		return null;
	}

	public User findById(long id) throws SQLException {
		String sql = "SELECT id, email, name, password_hash, created_at FROM users WHERE id = ?";
		try (Connection c = DataSourceProvider.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return map(rs);
			}
		}
		return null;
	}

	public long create(String email, String name, String passwordHash) throws SQLException {
		String sql = "INSERT INTO users(email, name, password_hash) VALUES(?,?,?)";
		try (Connection c = DataSourceProvider.get().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, email);
			ps.setString(2, name);
			ps.setString(3, passwordHash);
			ps.executeUpdate();
			try (ResultSet keys = ps.getGeneratedKeys()) {
				if (keys.next()) return keys.getLong(1);
			}
		}
		throw new SQLException("Failed to insert user");
	}

	private User map(ResultSet rs) throws SQLException {
		User u = new User();
		u.id = rs.getLong("id");
		u.email = rs.getString("email");
		u.name = rs.getString("name");
		u.passwordHash = rs.getString("password_hash");
		Timestamp created = rs.getTimestamp("created_at");
		u.createdAt = created != null ? created.toInstant() : Instant.now();
		return u;
	}
}
