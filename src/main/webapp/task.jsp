<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="common/header.jsp">
    <jsp:param name="pageTitle" value="${task != null ? task.title : 'Tasks'}" />
</jsp:include>

<jsp:include page="common/navigation.jsp">
    <jsp:param name="active" value="tasks" />
</jsp:include>

<div class="main-container">
    <jsp:include page="common/sidebar.jsp">
        <jsp:param name="sidebarType" value="task" />
        <jsp:param name="sidebarTitle" value="Tasks" />
    </jsp:include>

    <main class="main-content">
        <div class="container">
            <c:choose>
                <c:when test="${param.action == 'new' || param.action == 'edit'}">
                    <!-- Task Form -->
                    <div class="section-header">
                        <h1>${param.action == 'new' ? 'Create New Task' : 'Edit Task'}</h1>
                    </div>
                    
                    <div class="card">
                        <div class="card-body">
                            <c:if test="${not empty errorMessage}">
                                <div class="alert alert-danger">${errorMessage}</div>
                            </c:if>
                            
                            <form action="${pageContext.request.contextPath}/task" method="post">
                                <input type="hidden" name="action" value="${param.action}" />
                                <c:if test="${param.action == 'edit'}">
                                    <input type="hidden" name="id" value="${task.id}" />
                                </c:if>
                                <c:if test="${not empty param.projectId}">
                                    <input type="hidden" name="projectId" value="${param.projectId}" />
                                </c:if>
                                
                                <div class="form-group">
                                    <label for="taskTitle">Task Title</label>
                                    <input type="text" id="taskTitle" name="title" class="form-control" 
                                           value="${task != null ? task.title : ''}" required />
                                </div>
                                
                                <div class="form-group">
                                    <label for="taskDescription">Description</label>
                                    <textarea id="taskDescription" name="description" rows="4" 
                                              class="form-control">${task != null ? task.description : ''}</textarea>
                                </div>
                                
                                <c:if test="${empty param.projectId && task.projectId == null}">
                                    <div class="form-group">
                                        <label for="projectId">Project</label>
                                        <select id="projectId" name="projectId" class="form-control">
                                            <option value="">-- No Project --</option>
                                            <c:forEach items="${userProjects}" var="project">
                                                <option value="${project.id}" 
                                                        ${task != null && task.projectId == project.id ? 'selected' : ''}>
                                                    ${project.name}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </c:if>
                                
                                <div class="form-row">
                                    <div class="form-group col-md-6">
                                        <label for="taskStatus">Status</label>
                                        <select id="taskStatus" name="status" class="form-control">
                                            <c:forEach items="${taskStatuses}" var="status">
                                                <option value="${status}" 
                                                        ${task != null && task.status == status ? 'selected' : ''}>
                                                    ${status}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    
                                    <div class="form-group col-md-6">
                                        <label for="taskPriority">Priority</label>
                                        <select id="taskPriority" name="priority" class="form-control">
                                            <c:forEach items="${taskPriorities}" var="priority">
                                                <option value="${priority}" 
                                                        ${task != null && task.priority == priority ? 'selected' : ''}>
                                                    ${priority}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                                
                                <div class="form-row">
                                    <div class="form-group col-md-6">
                                        <label for="dueDate">Due Date</label>
                                        <input type="date" id="dueDate" name="dueDate" class="form-control" 
                                               value="<fmt:formatDate value='${task != null ? task.dueDate : null}' pattern='yyyy-MM-dd'/>" />
                                    </div>
                                    
                                    <div class="form-group col-md-6">
                                        <label for="estimatedHours">Estimated Hours</label>
                                        <input type="number" id="estimatedHours" name="estimatedHours" class="form-control" 
                                               min="0" step="0.5" value="${task != null ? task.estimatedHours : '0'}" />
                                    </div>
                                </div>
                                
                                <div class="form-group">
                                    <label for="assigneeId">Assignee</label>
                                    <select id="assigneeId" name="assigneeId" class="form-control">
                                        <c:choose>
                                            <c:when test="${not empty projectMembers}">
                                                <c:forEach items="${projectMembers}" var="member">
                                                    <option value="${member.id}" 
                                                            ${task != null && task.assigneeId == member.id ? 'selected' : ''}>
                                                        ${member.firstName} ${member.lastName}
                                                    </option>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                <option value="${sessionScope.user.id}" selected>${sessionScope.user.firstName} ${sessionScope.user.lastName}</option>
                                            </c:otherwise>
                                        </c:choose>
                                    </select>
                                </div>
                                
                                <div class="form-group">
                                    <label>Subtasks</label>
                                    <div class="subtasks-container">
                                        <c:if test="${not empty task.subtasks}">
                                            <c:forEach items="${task.subtasks}" var="subtask" varStatus="status">
                                                <div class="subtask-item">
                                                    <input type="hidden" name="subtaskId" value="${subtask.id}" />
                                                    <div class="form-row">
                                                        <div class="col-md-6">
                                                            <input type="text" name="subtaskTitle" class="form-control" 
                                                                   value="${subtask.title}" required />
                                                        </div>
                                                        <div class="col-md-4">
                                                            <select name="subtaskStatus" class="form-control">
                                                                <c:forEach items="${taskStatuses}" var="status">
                                                                    <option value="${status}" ${subtask.status == status ? 'selected' : ''}>${status}</option>
                                                                </c:forEach>
                                                            </select>
                                                        </div>
                                                        <div class="col-md-2">
                                                            <button type="button" class="btn btn-danger remove-subtask">
                                                                <i class="remove-icon"></i>
                                                            </button>
                                                        </div>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                        </c:if>
                                        <div class="subtask-template" style="display: none;">
                                            <div class="subtask-item">
                                                <div class="form-row">
                                                    <div class="col-md-6">
                                                        <input type="text" name="subtaskTitle" class="form-control" placeholder="Subtask title" required />
                                                    </div>
                                                    <div class="col-md-4">
                                                        <select name="subtaskStatus" class="form-control">
                                                            <c:forEach items="${taskStatuses}" var="status">
                                                                <option value="${status}">${status}</option>
                                                            </c:forEach>
                                                        </select>
                                                    </div>
                                                    <div class="col-md-2">
                                                        <button type="button" class="btn btn-danger remove-subtask">
                                                            <i class="remove-icon"></i>
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <button type="button" class="btn btn-sm btn-secondary add-subtask">
                                        <i class="add-icon"></i> Add Subtask
                                    </button>
                                </div>
                                
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">
                                        ${param.action == 'new' ? 'Create Task' : 'Save Changes'}
                                    </button>
                                    <a href="${pageContext.request.contextPath}/task" class="btn btn-secondary">Cancel</a>
                                </div>
                            </form>
                        </div>
                    </div>
                </c:when>
                
                <c:when test="${task != null}">
                    <!-- Task Details View -->
                    <div class="section-header">
                        <div class="d-flex justify-content-between align-items-center">
                            <h1>${task.title}</h1>
                            <div class="actions">
                                <a href="${pageContext.request.contextPath}/task?action=edit&id=${task.id}" class="btn btn-secondary">
                                    <i class="edit-icon"></i> Edit
                                </a>
                            </div>
                        </div>
                    </div>
                    
                    <div class="task-details">
                        <div class="card">
                            <div class="card-body">
                                <div class="task-meta">
                                    <div class="meta-row">
                                        <div class="meta-item">
                                            <span class="meta-label">Status</span>
                                            <span class="status-badge status-${task.status.toLowerCase()}">${task.status}</span>
                                        </div>
                                        <div class="meta-item">
                                            <span class="meta-label">Priority</span>
                                            <span class="priority-badge priority-${task.priority.toLowerCase()}">${task.priority}</span>
                                        </div>
                                    </div>
                                    <div class="meta-row">
                                        <div class="meta-item">
                                            <span class="meta-label">Assigned To</span>
                                            <div class="assignee">
                                                <img src="${pageContext.request.contextPath}/assets/img/user-default.png" alt="${task.assignee.firstName}" />
                                                <span>${task.assignee.firstName} ${task.assignee.lastName}</span>
                                            </div>
                                        </div>
                                        <div class="meta-item">
                                            <span class="meta-label">Due Date</span>
                                            <span class="meta-value">
                                                <c:choose>
                                                    <c:when test="${task.dueDate != null}">
                                                        <fmt:formatDate value="${task.dueDate}" pattern="MMM d, yyyy" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        Not specified
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>
                                    <c:if test="${task.projectId != null}">
                                        <div class="meta-row">
                                            <div class="meta-item">
                                                <span class="meta-label">Project</span>
                                                <a href="${pageContext.request.contextPath}/project?id=${task.projectId}" class="project-link">
                                                    ${task.project.name}
                                                </a>
                                            </div>
                                        </div>
                                    </c:if>
                                    <div class="meta-row">
                                        <div class="meta-item">
                                            <span class="meta-label">Estimated Hours</span>
                                            <span class="meta-value">${task.estimatedHours}</span>
                                        </div>
                                        <div class="meta-item">
                                            <span class="meta-label">Logged Hours</span>
                                            <span class="meta-value">${task.loggedHours}</span>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="task-description">
                                    <h3>Description</h3>
                                    <p>${task.description != null && not empty task.description ? task.description : 'No description provided.'}</p>
                                </div>
                                
                                <c:if test="${not empty task.subtasks}">
                                    <div class="subtasks">
                                        <h3>Subtasks</h3>
                                        <ul class="subtask-list">
                                            <c:forEach items="${task.subtasks}" var="subtask">
                                                <li class="subtask-item ${subtask.status == 'COMPLETED' ? 'completed' : ''}">
                                                    <div class="subtask-checkbox">
                                                        <input type="checkbox" id="subtask-${subtask.id}" 
                                                               data-id="${subtask.id}" 
                                                               ${subtask.status == 'COMPLETED' ? 'checked' : ''} />
                                                        <label for="subtask-${subtask.id}">${subtask.title}</label>
                                                    </div>
                                                </li>
                                            </c:forEach>
                                        </ul>
                                    </div>
                                </c:if>
                                
                                <div class="time-tracking">
                                    <h3>Time Tracking</h3>
                                    <div class="time-progress">
                                        <div class="progress-bar">
                                            <div class="progress-fill" style="width: ${task.loggedHours > 0 ? (task.loggedHours / task.estimatedHours * 100) : 0}%"></div>
                                        </div>
                                        <div class="time-labels">
                                            <span>${task.loggedHours}h logged</span>
                                            <span>${task.estimatedHours}h estimated</span>
                                        </div>
                                    </div>
                                    <form class="log-time-form" action="${pageContext.request.contextPath}/task/log-time" method="post">
                                        <input type="hidden" name="taskId" value="${task.id}" />
                                        <div class="form-inline">
                                            <div class="form-group">
                                                <label for="hours">Log time: </label>
                                                <input type="number" id="hours" name="hours" min="0.25" step="0.25" class="form-control" required />
                                                <span class="input-suffix">hours</span>
                                            </div>
                                            <button type="submit" class="btn btn-primary">Log Time</button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="section-header">
                        <h2>Comments</h2>
                    </div>
                    
                    <div class="comments-section">
                        <div class="comment-list">
                            <c:choose>
                                <c:when test="${not empty taskComments}">
                                    <c:forEach items="${taskComments}" var="comment">
                                        <div class="comment-card">
                                            <div class="comment-header">
                                                <div class="comment-author">
                                                    <img src="${pageContext.request.contextPath}/assets/img/user-default.png" alt="${comment.author.firstName}" />
                                                    <span>${comment.author.firstName} ${comment.author.lastName}</span>
                                                </div>
                                                <span class="comment-date">
                                                    <fmt:formatDate value="${comment.createdAt}" pattern="MMM d, yyyy HH:mm" />
                                                </span>
                                            </div>
                                            <div class="comment-content">
                                                <p>${comment.content}</p>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <div class="empty-state">
                                        <p>No comments yet</p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        
                        <div class="comment-form">
                            <form action="${pageContext.request.contextPath}/comment" method="post">
                                <input type="hidden" name="taskId" value="${task.id}" />
                                <div class="form-group">
                                    <textarea name="content" rows="3" class="form-control" placeholder="Add a comment..." required></textarea>
                                </div>
                                <button type="submit" class="btn btn-primary">Post Comment</button>
                            </form>
                        </div>
                    </div>
                </c:when>
                
                <c:otherwise>
                    <!-- Tasks List View -->
                    <div class="section-header">
                        <div class="d-flex justify-content-between align-items-center">
                            <h1>My Tasks</h1>
                            <a href="${pageContext.request.contextPath}/task.jsp?action=new" class="btn btn-primary">
                                <i class="add-icon"></i> New Task
                            </a>
                        </div>
                    </div>
                    
                    <div class="tasks-filters">
                        <div class="filter-group">
                            <label for="statusFilter">Status</label>
                            <select id="statusFilter" class="form-control">
                                <option value="all">All</option>
                                <c:forEach items="${taskStatuses}" var="status">
                                    <option value="${status.toLowerCase()}">${status}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="filter-group">
                            <label for="priorityFilter">Priority</label>
                            <select id="priorityFilter" class="form-control">
                                <option value="all">All</option>
                                <c:forEach items="${taskPriorities}" var="priority">
                                    <option value="${priority.toLowerCase()}">${priority}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="filter-group">
                            <label for="projectFilter">Project</label>
                            <select id="projectFilter" class="form-control">
                                <option value="all">All</option>
                                <option value="none">No Project</option>
                                <c:forEach items="${userProjects}" var="project">
                                    <option value="${project.id}">${project.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="filter-group search">
                            <input type="text" id="taskSearch" class="form-control" placeholder="Search tasks...">
                        </div>
                    </div>
                    
                    <div class="tasks-container">
                        <c:choose>
                            <c:when test="${not empty userTasks}">
                                <div class="task-list">
                                    <c:forEach items="${userTasks}" var="task">
                                        <div class="task-card" data-status="${task.status.toLowerCase()}" 
                                             data-priority="${task.priority.toLowerCase()}" 
                                             data-project="${task.projectId != null ? task.projectId : 'none'}">
                                            <div class="task-header">
                                                <span class="status-indicator status-${task.status.toLowerCase()}"></span>
                                                <h3 class="task-title">
                                                    <a href="${pageContext.request.contextPath}/task?id=${task.id}">${task.title}</a>
                                                </h3>
                                                <div class="task-priority priority-${task.priority.toLowerCase()}">${task.priority}</div>
                                            </div>
                                            <div class="task-body">
                                                <p class="task-description">${task.description}</p>
                                                <c:if test="${task.subtasks != null && not empty task.subtasks}">
                                                    <div class="subtask-progress">
                                                        <span>${task.completedSubtasks} of ${task.subtasks.size()} subtasks</span>
                                                        <div class="progress-bar">
                                                            <div class="progress-fill" style="width: ${task.subtasks.size() > 0 ? (task.completedSubtasks / task.subtasks.size() * 100) : 0}%"></div>
                                                        </div>
                                                    </div>
                                                </c:if>
                                            </div>
                                            <div class="task-footer">
                                                <div class="task-meta">
                                                    <c:if test="${task.projectId != null}">
                                                        <span class="project-badge">
                                                            <a href="${pageContext.request.contextPath}/project?id=${task.projectId}">${task.project.name}</a>
                                                        </span>
                                                    </c:if>
                                                    <span class="due-date ${task.dueDate != null && task.dueDate.before(now) ? 'overdue' : ''}">
                                                        <i class="due-icon"></i>
                                                        <c:choose>
                                                            <c:when test="${task.dueDate != null}">
                                                                Due: <fmt:formatDate value="${task.dueDate}" pattern="MMM d" />
                                                            </c:when>
                                                            <c:otherwise>
                                                                No due date
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    <span class="assignee">
                                                        <img src="${pageContext.request.contextPath}/assets/img/user-default.png" alt="${task.assignee.firstName}" />
                                                        ${task.assignee.firstName}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state">
                                    <p>You have no tasks assigned to you</p>
                                    <a href="${pageContext.request.contextPath}/task.jsp?action=new" class="btn btn-primary">
                                        Create New Task
                                    </a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </main>
</div>

<jsp:include page="common/footer.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/tasks.js"></script>