<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %> <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="common/header.jsp">
  <jsp:param name="pageTitle" value="Dashboard" />
  <jsp:param name="customCss" value="dashboard.css" />
</jsp:include>

<div class="logout-button">
  <a href="${pageContext.request.contextPath}/logout" class="btn btn-danger btn-sm">Logout</a>
</div>

<jsp:include page="common/navigation.jsp">
  <jsp:param name="active" value="dashboard" />
</jsp:include>

<main class="main-content">
  <div class="container">
    <div class="dashboard-header">
      <h1><i class="fas fa-tachometer-alt"></i> Dashboard</h1>
      <div class="date">
        <i class="far fa-calendar-alt"></i>
        <fmt:formatDate value="${now}" pattern="EEEE, MMMM d, yyyy" />
      </div>
    </div>

    <div class="welcome-banner">
      <div class="welcome-message">
        <h2>Welcome back, ${sessionScope.user.firstName}!</h2>
        <p>Here's an overview of your tasks and projects</p>
      </div>
      <div class="quick-actions">
        <a href="${pageContext.request.contextPath}/task.jsp?action=new" class="btn btn-primary btn-sm"> <i class="fas fa-plus"></i> New Task </a>
        <a href="${pageContext.request.contextPath}/project.jsp?action=new" class="btn btn-secondary btn-sm"> <i class="fas fa-folder-plus"></i> New Project </a>
      </div>
    </div>

    <div class="dashboard-stats">
      <div class="stat-card">
        <div class="stat-icon"><i class="fas fa-calendar-day"></i></div>
        <div class="stat-value">${tasksDueToday}</div>
        <div class="stat-label">Tasks Due Today</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><i class="fas fa-exclamation-circle"></i></div>
        <div class="stat-value">${tasksOverdue}</div>
        <div class="stat-label">Overdue Tasks</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><i class="fas fa-project-diagram"></i></div>
        <div class="stat-value">${projectsInProgress}</div>
        <div class="stat-label">Projects In Progress</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><i class="fas fa-check-circle"></i></div>
        <div class="stat-value">${tasksCompleted}</div>
        <div class="stat-label">Completed Tasks</div>
      </div>
    </div>

    <div class="dashboard-grid">
      <div class="widget recent-tasks">
        <div class="widget-header">
          <h3><i class="fas fa-tasks"></i> Recent Tasks</h3>
          <a href="${pageContext.request.contextPath}/task.jsp" class="view-all">View All <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="widget-content">
          <c:choose>
            <c:when test="${not empty recentTasks}">
              <ul class="task-list">
                <c:forEach items="${recentTasks}" var="task">
                  <li class="task-item ${task.status == 'COMPLETED' ? 'completed' : ''} ${task.dueDate < now ? 'overdue' : ''}">
                    <div class="task-status">
                      <span class="status-indicator status-${fn:toLowerCase(task.status)}"></span>
                    </div>
                    <div class="task-info">
                      <h4>
                        <a href="${pageContext.request.contextPath}/task?id=${task.id}">${task.title}</a>
                      </h4>
                      <div class="task-meta">
                        <span class="project-name"><i class="fas fa-folder"></i> ${task.project.name}</span>
                        <span class="due-date">
                          <i class="far fa-clock"></i>
                          Due: <fmt:formatDate value="${task.dueDate}" pattern="MMM d" />
                        </span>
                      </div>
                    </div>
                    <div class="task-priority priority-${fn:toLowerCase(task.priority)}"><i class="fas fa-flag"></i> ${task.priority}</div>
                  </li>
                </c:forEach>
              </ul>
            </c:when>
            <c:otherwise>
              <div class="empty-state">
                <i class="fas fa-clipboard-list empty-icon"></i>
                <p>No recent tasks found</p>
                <a href="${pageContext.request.contextPath}/task.jsp?action=new" class="btn btn-primary btn-sm"> <i class="fas fa-plus"></i> Create Task </a>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>

      <div class="widget active-projects">
        <div class="widget-header">
          <h3><i class="fas fa-folder-open"></i> Active Projects</h3>
          <a href="${pageContext.request.contextPath}/project.jsp" class="view-all">View All <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="widget-content">
          <c:choose>
            <c:when test="${not empty activeProjects}">
              <ul class="project-list">
                <c:forEach items="${activeProjects}" var="project">
                  <li class="project-item">
                    <h4>
                      <a href="${pageContext.request.contextPath}/project?id=${project.id}">${project.name}</a>
                    </h4>
                    <div class="project-meta">
                      <span class="task-count"><i class="fas fa-tasks"></i> ${project.taskCount} tasks</span>
                      <span class="team-size"><i class="fas fa-users"></i> ${project.teamMembers.size()} members</span>
                    </div>
                    <div class="project-progress">
                      <div class="progress-label">
                        <span>Progress</span>
                        <span>${project.completionPercentage}%</span>
                      </div>
                      <div class="progress-bar">
                        <div class="progress-fill" style="width: ${project.completionPercentage}%"></div>
                      </div>
                    </div>
                  </li>
                </c:forEach>
              </ul>
            </c:when>
            <c:otherwise>
              <div class="empty-state">
                <i class="fas fa-project-diagram empty-icon"></i>
                <p>No active projects found</p>
                <a href="${pageContext.request.contextPath}/project.jsp?action=new" class="btn btn-primary btn-sm"> <i class="fas fa-plus"></i> Create Project </a>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>

      <div class="widget notifications">
        <div class="widget-header">
          <h3><i class="fas fa-bell"></i> Recent Notifications</h3>
          <a href="${pageContext.request.contextPath}/notifications.jsp" class="view-all">View All <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="widget-content">
          <c:choose>
            <c:when test="${not empty recentNotifications}">
              <ul class="notification-list">
                <c:forEach items="${recentNotifications}" var="notification">
                  <li class="notification-item ${notification.read ? '' : 'unread'}">
                    <div class="notification-icon ${fn:toLowerCase(notification.type)}-icon">
                      <c:choose>
                        <c:when test="${notification.type == 'TASK'}"><i class="fas fa-tasks"></i></c:when>
                        <c:when test="${notification.type == 'PROJECT'}"><i class="fas fa-folder"></i></c:when>
                        <c:when test="${notification.type == 'COMMENT'}"><i class="fas fa-comment"></i></c:when>
                        <c:when test="${notification.type == 'MENTION'}"><i class="fas fa-at"></i></c:when>
                        <c:otherwise><i class="fas fa-bell"></i></c:otherwise>
                      </c:choose>
                    </div>
                    <div class="notification-content">
                      <div class="notification-message">${notification.message}</div>
                      <div class="notification-time"><i class="far fa-clock"></i> <fmt:formatDate value="${notification.createdAt}" pattern="MMM d, h:mm a" /></div>
                    </div>
                  </li>
                </c:forEach>
              </ul>
            </c:when>
            <c:otherwise>
              <div class="empty-state">
                <i class="fas fa-bell-slash empty-icon"></i>
                <p>No recent notifications</p>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>

      <div class="widget upcoming-deadlines">
        <div class="widget-header">
          <h3><i class="fas fa-calendar-alt"></i> Upcoming Deadlines</h3>
          <a href="${pageContext.request.contextPath}/task.jsp?filter=upcoming" class="view-all">View All <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="widget-content">
          <c:choose>
            <c:when test="${not empty upcomingDeadlines}">
              <ul class="deadline-list">
                <c:forEach items="${upcomingDeadlines}" var="task">
                  <li class="deadline-item">
                    <div class="deadline-date">
                      <span class="day"><fmt:formatDate value="${task.dueDate}" pattern="d" /></span>
                      <span class="month"><fmt:formatDate value="${task.dueDate}" pattern="MMM" /></span>
                    </div>
                    <div class="deadline-info">
                      <h4>
                        <a href="${pageContext.request.contextPath}/task?id=${task.id}">${task.title}</a>
                      </h4>
                      <div class="project-name"><i class="fas fa-folder"></i> ${task.project.name}</div>
                    </div>
                    <div class="days-remaining">
                      <c:set var="daysRemaining" value="${task.daysRemaining}" />
                      <span class="${daysRemaining <= 2 ? 'urgent' : 'normal'}"> <i class="far fa-clock"></i> ${daysRemaining} day<c:if test="${daysRemaining != 1}">s</c:if> left </span>
                    </div>
                  </li>
                </c:forEach>
              </ul>
            </c:when>
            <c:otherwise>
              <div class="empty-state">
                <i class="fas fa-calendar-check empty-icon"></i>
                <p>No upcoming deadlines</p>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>
</main>

<jsp:include page="common/footer.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/dashboard.js"></script>
