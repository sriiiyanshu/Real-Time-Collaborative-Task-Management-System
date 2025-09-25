<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%
	HttpSession s = request.getSession(false);
	if (s == null || s.getAttribute("userId") == null) {
		response.sendRedirect("/index.jsp");
		return;
	}
	String userName = (String) s.getAttribute("userName");
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<title>Dashboard - CollabTask</title>
	<link rel="stylesheet" href="assets/css/style.css" />
</head>
<body>
<header class="topbar">
	<div class="brand">CollabTask</div>
	<div class="spacer"></div>
	<div class="user">Hello, <%= userName %> | <a href="/logout">Logout</a></div>
</header>
<main class="layout">
	<aside class="sidebar">
		<h3>Projects</h3>
		<ul id="projectList"></ul>
		<button id="newProject">+ New Project</button>
	</aside>
	<section class="content">
		<div class="content-header">
			<h2 id="projectTitle">Select a project</h2>
		</div>
		<div id="taskBoard" class="board hidden">
			<div class="column" data-status="todo">
				<h3>To Do</h3>
				<div class="tasks" id="todoCol"></div>
			</div>
			<div class="column" data-status="in_progress">
				<h3>In Progress</h3>
				<div class="tasks" id="inProgressCol"></div>
			</div>
			<div class="column" data-status="done">
				<h3>Done</h3>
				<div class="tasks" id="doneCol"></div>
			</div>
		</div>
		<div id="analytics" class="analytics">
			<canvas id="chart1"></canvas>
		</div>
	</section>
</main>
<script src="assets/js/app.js"></script>
</body>
</html>
