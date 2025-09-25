package com.collabtask.app;

import com.collabtask.dao.UserDao;
import com.collabtask.model.Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {
	private final UserDao userDao = new UserDao();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getServletPath();
		if ("/login".equals(path)) {
			login(req, resp);
		} else if ("/register".equals(path)) {
			register(req, resp);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if ("/logout".equals(req.getServletPath())) {
			HttpSession session = req.getSession(false);
			if (session != null) session.invalidate();
			resp.sendRedirect("/index.jsp");
			return;
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		try {
			User u = userDao.findByEmail(email);
			if (u != null && BCrypt.checkpw(password, u.passwordHash)) {
				HttpSession session = req.getSession(true);
				session.setAttribute("userId", u.id);
				session.setAttribute("userName", u.name);
				resp.sendRedirect("/dashboard.jsp");
				return;
			}
			resp.sendRedirect("/index.jsp?error=Invalid%20credentials");
		} catch (SQLException e) {
			resp.sendError(500, "DB Error");
		}
	}

	private void register(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String email = req.getParameter("email");
		String name = req.getParameter("name");
		String password = req.getParameter("password");
		try {
			User existing = userDao.findByEmail(email);
			if (existing != null) {
				resp.sendRedirect("/index.jsp?error=Email%20already%20used");
				return;
			}
			String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
			long id = userDao.create(email, name, hash);
			HttpSession session = req.getSession(true);
			session.setAttribute("userId", id);
			session.setAttribute("userName", name);
			resp.sendRedirect("/dashboard.jsp");
		} catch (SQLException e) {
			resp.sendError(500, "DB Error");
		}
	}
}
