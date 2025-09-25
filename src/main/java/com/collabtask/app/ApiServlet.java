package com.collabtask.app;

import com.collabtask.dao.ProjectDao;
import com.collabtask.dao.TaskDao;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ApiServlet", urlPatterns = {"/api/projects", "/api/tasks", "/api/analytics"})
public class ApiServlet extends HttpServlet {
	private final Gson gson = new Gson();
	private final ProjectDao projectDao = new ProjectDao();
	private final TaskDao taskDao = new TaskDao();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		String path = req.getServletPath();
		Long userId = (Long) req.getSession().getAttribute("userId");
		if (userId == null) { resp.setStatus(401); return; }
		try (PrintWriter out = resp.getWriter()) {
			if ("/api/projects".equals(path)) {
				List<ProjectDao.ProjectRow> projects = projectDao.listForUser(userId);
				Map<String, Object> r = new HashMap<>(); r.put("projects", projects);
				out.write(gson.toJson(r));
				return;
			}
			if ("/api/tasks".equals(path)) {
				long projectId = Long.parseLong(req.getParameter("projectId"));
				List<TaskDao.TaskRow> tasks = taskDao.listByProject(projectId);
				Map<String, Object> r = new HashMap<>(); r.put("tasks", tasks);
				out.write(gson.toJson(r));
				return;
			}
			if ("/api/analytics".equals(path)) {
				long projectId = Long.parseLong(req.getParameter("projectId"));
				Map<String, Object> r = new HashMap<>();
				try (Connection c = DataSourceProvider.get().getConnection();
				     PreparedStatement ps = c.prepareStatement("SELECT status, COUNT(*) cnt FROM tasks WHERE project_id=? GROUP BY status")) {
					ps.setLong(1, projectId);
					try (ResultSet rs = ps.executeQuery()) {
						Map<String, Integer> counts = new HashMap<>();
						counts.put("todo", 0); counts.put("in_progress", 0); counts.put("done", 0);
						while (rs.next()) counts.put(rs.getString(1), rs.getInt(2));
						r.put("counts", counts);
					}
				}
				out.write(gson.toJson(r));
				return;
			}
			resp.setStatus(404);
		} catch (SQLException e) {
			resp.setStatus(500);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		Long userId = (Long) req.getSession().getAttribute("userId");
		if (userId == null) { resp.setStatus(401); return; }
		String path = req.getServletPath();
		try (PrintWriter out = resp.getWriter()) {
			if ("/api/projects".equals(path)) {
				String action = req.getParameter("action");
				if ("create".equals(action)) {
					String name = req.getParameter("name");
					String description = req.getParameter("description");
					long id = projectDao.create(userId, name, description);
					projectDao.addMember(id, userId, "owner");
					out.write("{\"ok\":true,\"id\":" + id + "}");
					return;
				}
				resp.setStatus(400);
				return;
			}
			if ("/api/tasks".equals(path)) {
				String action = req.getParameter("action");
				if ("create".equals(action)) {
					long projectId = Long.parseLong(req.getParameter("projectId"));
					String title = req.getParameter("title");
					String description = req.getParameter("description");
					long id = taskDao.create(projectId, title, description);
					broadcast(projectId, "created", id);
					out.write("{\"ok\":true,\"id\":" + id + "}");
					return;
				}
				if ("update_status".equals(action)) {
					long taskId = Long.parseLong(req.getParameter("taskId"));
					String status = req.getParameter("status");
					long projectId = Long.parseLong(req.getParameter("projectId"));
					taskDao.updateStatus(taskId, status);
					broadcast(projectId, "updated", taskId);
					out.write("{\"ok\":true}");
					return;
				}
				resp.setStatus(400);
				return;
			}
			resp.setStatus(404);
		} catch (SQLException e) {
			resp.setStatus(500);
		}
	}

	private void broadcast(long projectId, String kind, long id) {
		try {
			com.collabtask.websocket.TaskSocket.broadcast(Long.toString(projectId),
					new Gson().toJson(Map.of("type", "task_update", "kind", kind, "id", id)), null);
		} catch (IOException ignored) {}
	}
}
