package com.collabtask.dao;

import com.collabtask.app.DataSourceProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDao {
	public static class ProjectRow { public long id; public String name; }

	public List<ProjectRow> listForUser(long userId) throws SQLException {
		String sql = "SELECT p.id, p.name FROM projects p JOIN project_members m ON p.id=m.project_id WHERE m.user_id=? ORDER BY p.created_at DESC";
		try (Connection c = DataSourceProvider.get().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				List<ProjectRow> list = new ArrayList<>();
				while (rs.next()) { ProjectRow pr = new ProjectRow(); pr.id=rs.getLong(1); pr.name=rs.getString(2); list.add(pr);} 
				return list;
			}
		}
	}

	public long create(long ownerId, String name, String description) throws SQLException {
		String sql = "INSERT INTO projects(name, description, owner_id) VALUES(?,?,?)";
		try (Connection c = DataSourceProvider.get().getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, name);
			ps.setString(2, description);
			ps.setLong(3, ownerId);
			ps.executeUpdate();
			try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1);} 
		}
		throw new SQLException("Failed to create project");
	}

	public void addMember(long projectId, long userId, String role) throws SQLException {
		String sql = "INSERT IGNORE INTO project_members(project_id, user_id, role) VALUES(?,?,?)";
		try (Connection c = DataSourceProvider.get().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, projectId); ps.setLong(2, userId); ps.setString(3, role); ps.executeUpdate();
		}
	}
}
