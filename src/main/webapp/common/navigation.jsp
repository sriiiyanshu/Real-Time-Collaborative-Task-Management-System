<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<nav class="main-nav">
  <div class="container">
    <ul class="nav-list">
      <li class="nav-item ${param.active == 'dashboard' ? 'active' : ''}">
        <a href="${pageContext.request.contextPath}/dashboard.jsp">
          <i class="fas fa-tachometer-alt nav-icon"></i>
          <span>Dashboard</span>
        </a>
      </li>
      <li class="nav-item ${param.active == 'projects' ? 'active' : ''}">
        <a href="${pageContext.request.contextPath}/project.jsp">
          <i class="fas fa-project-diagram nav-icon"></i>
          <span>Projects</span>
        </a>
      </li>
      <li class="nav-item ${param.active == 'tasks' ? 'active' : ''}">
        <a href="${pageContext.request.contextPath}/task.jsp">
          <i class="fas fa-tasks nav-icon"></i>
          <span>Tasks</span>
        </a>
      </li>
      <li class="nav-item ${param.active == 'teams' ? 'active' : ''}">
        <a href="${pageContext.request.contextPath}/team.jsp">
          <i class="fas fa-users nav-icon"></i>
          <span>Teams</span>
        </a>
      </li>
      <li class="nav-item ${param.active == 'chat' ? 'active' : ''}">
        <a href="${pageContext.request.contextPath}/chat.jsp">
          <i class="fas fa-comments nav-icon"></i>
          <span>Chat</span>
        </a>
      </li>
      <li class="nav-item ${param.active == 'files' ? 'active' : ''}">
        <a href="${pageContext.request.contextPath}/filemanager.jsp">
          <i class="fas fa-file-alt nav-icon"></i>
          <span>Files</span>
        </a>
      </li>
      <c:if test="${sessionScope.user.role == 'ADMIN' || sessionScope.user.role == 'MANAGER'}">
        <li class="nav-item ${param.active == 'analytics' ? 'active' : ''}">
          <a href="${pageContext.request.contextPath}/analytics.jsp">
            <i class="fas fa-chart-bar nav-icon"></i>
            <span>Analytics</span>
          </a>
        </li>
      </c:if>
    </ul>
  </div>
</nav>
