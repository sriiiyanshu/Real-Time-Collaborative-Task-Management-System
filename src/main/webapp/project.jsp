<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="common/header.jsp">
    <jsp:param name="pageTitle" value="${project != null ? project.name : 'Projects'}" />
</jsp:include>

<jsp:include page="common/navigation.jsp">
    <jsp:param name="active" value="projects" />
</jsp:include>

<div class="main-container">
    <jsp:include page="common/sidebar.jsp">
        <jsp:param name="sidebarType" value="project" />
        <jsp:param name="sidebarTitle" value="Projects" />
    </jsp:include>

    <main class="main-content">
        <div class="container">
            <c:choose>
                <c:when test="${param.action == 'new' || param.action == 'edit'}">
                    <!-- Project Form -->
                    <div class="section-header">
                        <h1>${param.action == 'new' ? 'Create New Project' : 'Edit Project'}</h1>
                    </div>
                    
                    <div class="card">
                        <div class="card-body">
                            <c:if test="${not empty errorMessage}">
                                <div class="alert alert-danger">${errorMessage}</div>
                            </c:if>
                            
                            <form action="${pageContext.request.contextPath}/project" method="post">
                                <input type="hidden" name="action" value="${param.action}" />
                                <c:if test="${param.action == 'edit'}">
                                    <input type="hidden" name="id" value="${project.id}" />
                                </c:if>
                                
                                <div class="form-group">
                                    <label for="projectName">Project Name</label>
                                    <input type="text" id="projectName" name="name" class="form-control" 
                                           value="${project != null ? project.name : ''}" required />
                                </div>
                                
                                <div class="form-group">
                                    <label for="projectDescription">Description</label>
                                    <textarea id="projectDescription" name="description" rows="4" 
                                              class="form-control">${project != null ? project.description : ''}</textarea>
                                </div>
                                
                                <div class="form-row">
                                    <div class="form-group col-md-6">
                                        <label for="startDate">Start Date</label>
                                        <input type="date" id="startDate" name="startDate" class="form-control" 
                                               value="<fmt:formatDate value='${project != null ? project.startDate : now}' pattern='yyyy-MM-dd'/>" required />
                                    </div>
                                    
                                    <div class="form-group col-md-6">
                                        <label for="endDate">End Date</label>
                                        <input type="date" id="endDate" name="endDate" class="form-control" 
                                               value="<fmt:formatDate value='${project != null ? project.endDate : null}' pattern='yyyy-MM-dd'/>" />
                                    </div>
                                </div>
                                
                                <div class="form-group">
                                    <label for="projectStatus">Status</label>
                                    <select id="projectStatus" name="status" class="form-control">
                                        <c:forEach items="${projectStatuses}" var="status">
                                            <option value="${status}" 
                                                    ${project != null && project.status == status ? 'selected' : ''}>
                                                ${status}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                
                                <div class="form-group">
                                    <label>Team Members</label>
                                    <div class="team-selector">
                                        <div class="selected-members">
                                            <c:forEach items="${projectMembers}" var="member">
                                                <div class="selected-member">
                                                    <input type="hidden" name="teamMembers" value="${member.id}" />
                                                    <img src="${pageContext.request.contextPath}/assets/img/user-default.png" alt="${member.firstName}" />
                                                    <span>${member.firstName} ${member.lastName}</span>
                                                    <button type="button" class="remove-member">&times;</button>
                                                </div>
                                            </c:forEach>
                                        </div>
                                        <button type="button" class="btn btn-sm" id="addTeamMemberBtn">
                                            <i class="add-icon"></i> Add Team Member
                                        </button>
                                    </div>
                                </div>
                                
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">
                                        ${param.action == 'new' ? 'Create Project' : 'Save Changes'}
                                    </button>
                                    <a href="${pageContext.request.contextPath}/project.jsp" class="btn btn-secondary">Cancel</a>
                                </div>
                            </form>
                        </div>
                    </div>
                </c:when>
                
                <c:when test="${project != null}">
                    <!-- Project Details View -->
                    <div class="section-header">
                        <div class="d-flex justify-content-between align-items-center">
                            <h1>${project.name}</h1>
                            <div class="actions">
                                <a href="${pageContext.request.contextPath}/project?action=edit&id=${project.id}" class="btn btn-secondary">
                                    <i class="edit-icon"></i> Edit
                                </a>
                            </div>
                        </div>
                    </div>
                    
                    <div class="project-overview">
                        <div class="card">
                            <div class="card-body">
                                <div class="project-meta">
                                    <div class="meta-item">
                                        <span class="meta-label">Status</span>
                                        <span class="status-badge status-${project.status.toLowerCase()}">${project.status}</span>
                                    </div>
                                    <div class="meta-item">
                                        <span class="meta-label">Start Date</span>
                                        <span class="meta-value"><fmt:formatDate value="${project.startDate}" pattern="MMM d, yyyy" /></span>
                                    </div>
                                    <div class="meta-item">
                                        <span class="meta-label">End Date</span>
                                        <span class="meta-value">
                                            <c:choose>
                                                <c:when test="${project.endDate != null}">
                                                    <fmt:formatDate value="${project.endDate}" pattern="MMM d, yyyy" />
                                                </c:when>
                                                <c:otherwise>
                                                    Not specified
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="meta-item">
                                        <span class="meta-label">Progress</span>
                                        <div class="progress-container">
                                            <div class="progress-bar">
                                                <div class="progress-fill" style="width: ${project.completionPercentage}%"></div>
                                            </div>
                                            <span class="progress-percentage">${project.completionPercentage}%</span>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="project-description">
                                    <h3>Description</h3>
                                    <p>${project.description != null && !empty project.description ? project.description : 'No description provided.'}</p>
                                </div>
                                
                                <div class="project-team">
                                    <h3>Team</h3>
                                    <div class="team-members">
                                        <c:forEach items="${projectMembers}" var="member">
                                            <div class="team-member">
                                                <img src="${pageContext.request.contextPath}/assets/img/user-default.png" alt="${member.firstName}" />
                                                <div class="member-info">
                                                    <span class="member-name">${member.firstName} ${member.lastName}</span>
                                                    <span class="member-role">${member.role}</span>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="section-header">
                        <div class="d-flex justify-content-between align-items-center">
                            <h2>Tasks</h2>
                            <a href="${pageContext.request.contextPath}/task?action=new&projectId=${project.id}" class="btn btn-primary">
                                <i class="add-icon"></i> New Task
                            </a>
                        </div>
                    </div>
                    
                    <div class="tasks-container">
                        <div class="task-filters">
                            <div class="filter-group">
                                <label for="taskStatusFilter">Status</label>
                                <select id="taskStatusFilter" class="form-control">
                                    <option value="all">All</option>
                                    <c:forEach items="${taskStatuses}" var="status">
                                        <option value="${status.toLowerCase()}">${status}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="filter-group">
                                <label for="taskAssigneeFilter">Assignee</label>
                                <select id="taskAssigneeFilter" class="form-control">
                                    <option value="all">All</option>
                                    <c:forEach items="${projectMembers}" var="member">
                                        <option value="${member.id}">${member.firstName} ${member.lastName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        
                        <div class="task-list-container">
                            <c:choose>
                                <c:when test="${not empty projectTasks}">
                                    <div class="task-list">
                                        <c:forEach items="${projectTasks}" var="task">
                                            <div class="task-card" data-status="${task.status.toLowerCase()}" data-assignee="${task.assignee.id}">
                                                <div class="task-header">
                                                    <span class="status-indicator status-${task.status.toLowerCase()}"></span>
                                                    <h3 class="task-title">
                                                        <a href="${pageContext.request.contextPath}/task?id=${task.id}">${task.title}</a>
                                                    </h3>
                                                    <div class="task-priority priority-${task.priority.toLowerCase()}">${task.priority}</div>
                                                </div>
                                                <div class="task-body">
                                                    <p class="task-description">${task.description}</p>
                                                </div>
                                                <div class="task-footer">
                                                    <div class="task-meta">
                                                        <span class="due-date">
                                                            <i class="due-icon"></i>
                                                            Due: <fmt:formatDate value="${task.dueDate}" pattern="MMM d" />
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
                                        <p>No tasks have been created for this project yet</p>
                                        <a href="${pageContext.request.contextPath}/task?action=new&projectId=${project.id}" class="btn btn-sm">
                                            Create First Task
                                        </a>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:when>
                
                <c:otherwise>
                    <!-- Projects List View -->
                    <div class="section-header">
                        <div class="d-flex justify-content-between align-items-center">
                            <h1>My Projects</h1>
                            <a href="${pageContext.request.contextPath}/project.jsp?action=new" class="btn btn-primary">
                                <i class="add-icon"></i> New Project
                            </a>
                        </div>
                    </div>
                    
                    <div class="project-filters">
                        <div class="filter-group">
                            <label for="projectStatusFilter">Status</label>
                            <select id="projectStatusFilter" class="form-control">
                                <option value="all">All</option>
                                <c:forEach items="${projectStatuses}" var="status">
                                    <option value="${status.toLowerCase()}">${status}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="filter-group">
                            <input type="text" id="projectSearchInput" class="form-control" placeholder="Search projects...">
                        </div>
                    </div>
                    
                    <div class="projects-grid">
                        <c:choose>
                            <c:when test="${not empty userProjects}">
                                <c:forEach items="${userProjects}" var="project">
                                    <div class="project-card" data-status="${project.status.toLowerCase()}">
                                        <div class="project-card-header">
                                            <span class="status-badge status-${project.status.toLowerCase()}">${project.status}</span>
                                            <h3 class="project-title">
                                                <a href="${pageContext.request.contextPath}/project?id=${project.id}">${project.name}</a>
                                            </h3>
                                        </div>
                                        <div class="project-card-body">
                                            <p class="project-description">${project.description}</p>
                                            <div class="project-progress">
                                                <div class="progress-label">
                                                    <span>Progress</span>
                                                    <span>${project.completionPercentage}%</span>
                                                </div>
                                                <div class="progress-bar">
                                                    <div class="progress-fill" style="width: ${project.completionPercentage}%"></div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="project-card-footer">
                                            <div class="project-meta">
                                                <span class="task-count">${project.taskCount} tasks</span>
                                                <span class="team-size">${project.teamMembers.size()} members</span>
                                                <span class="project-dates">
                                                    <fmt:formatDate value="${project.startDate}" pattern="MMM d" /> - 
                                                    <c:choose>
                                                        <c:when test="${project.endDate != null}">
                                                            <fmt:formatDate value="${project.endDate}" pattern="MMM d" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            Ongoing
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state">
                                    <p>No projects found</p>
                                    <a href="${pageContext.request.contextPath}/project?action=new" class="btn btn-primary">
                                        Create Your First Project
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

<!-- Project Member Selection Modal -->
<div class="modal" id="teamMemberModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Add Team Members</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <input type="text" id="memberSearchInput" class="form-control" placeholder="Search members...">
                <div class="member-list">
                    <c:forEach items="${availableUsers}" var="user">
                        <div class="member-item" data-id="${user.id}" data-name="${user.firstName} ${user.lastName}">
                            <img src="${pageContext.request.contextPath}/assets/img/user-default.png" alt="${user.firstName}">
                            <span>${user.firstName} ${user.lastName}</span>
                            <span class="member-role">${user.role}</span>
                        </div>
                    </c:forEach>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                <button type="button" class="btn btn-primary" id="addSelectedMembers">Add Selected</button>
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/projects.js"></script>