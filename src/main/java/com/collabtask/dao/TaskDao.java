package com.collabtask.dao;

import com.collabtask.app.DataSourceProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDao {
	public static class TaskRow { public long id; public String title; public String status; }

	public List<TaskRow> listByProject(long projectId) throws SQLException {
		String sql = "SELECT id, title, status FROM tasks WHERE project_id=? ORDER BY created_at DESC";
		try (Connection c = DataSourceProvider.get().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, projectId);
			try (ResultSet rs = ps.executeQuery()) {
				List<TaskRow> list = new ArrayList<>();
				while (rs.next()) { TaskRow tr = new TaskRow(); tr.id=rs.getLong(1); tr.title=rs.getString(2); tr.status=rs.getString(3); list.add(tr);} 
				return list;
			}
		}
	}

	public long create(long projectId, String title, String description) throws SQLException {
		String sql = "INSERT INTO tasks(project_id, title, description) VALUES(?,?,?)";
		try (Connection c = DataSourceProvider.get().getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, projectId); ps.setString(2, title); ps.setString(3, description); ps.executeUpdate();
			try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1);} 
		}
		throw new SQLException("Failed to create task");
	}

	public void updateStatus(long taskId, String status) throws SQLException {
		String sql = "UPDATE tasks SET status=? WHERE id=?";
		try (Connection c = DataSourceProvider.get().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, status); ps.setLong(2, taskId); ps.executeUpdate();
		}
	}
}
