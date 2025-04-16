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
                    <div class="section-header mb-4">
                        <h1 class="page-title">${param.action == 'new' ? 'Create New Project' : 'Edit Project'}</h1>
                    </div>
                    
                    <div class="card shadow-sm">
                        <div class="card-body p-4">
                            <c:if test="${not empty errorMessage}">
                                <div class="alert alert-danger mb-4">${errorMessage}</div>
                            </c:if>
                            
                            <form action="${pageContext.request.contextPath}/projects" method="post">
                                <input type="hidden" name="action" value="${param.action}" />
                                <c:if test="${param.action == 'edit'}">
                                    <input type="hidden" name="id" value="${project.id}" />
                                </c:if>
                                
                                <div class="form-group mb-4">
                                    <label for="projectName" class="form-label">Project Name</label>
                                    <input type="text" id="projectName" name="name" class="form-control" 
                                           value="${project != null ? project.name : ''}" required />
                                </div>
                                
                                <div class="form-group mb-4">
                                    <label for="projectDescription" class="form-label">Description</label>
                                    <textarea id="projectDescription" name="description" rows="4" 
                                              class="form-control">${project != null ? project.description : ''}</textarea>
                                </div>
                                
                                <div class="row mb-4">
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label for="startDate" class="form-label">Start Date</label>
                                            <input type="date" id="startDate" name="startDate" class="form-control" 
                                                   value="<fmt:formatDate value='${project != null ? project.startDate : now}' pattern='yyyy-MM-dd'/>" required />
                                        </div>
                                    </div>
                                    
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label for="endDate" class="form-label">End Date</label>
                                            <input type="date" id="endDate" name="endDate" class="form-control" 
                                                   value="<fmt:formatDate value='${project != null ? project.endDate : null}' pattern='yyyy-MM-dd'/>" />
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="form-group mb-4">
                                    <label for="projectStatus" class="form-label">Status</label>
                                    <select id="projectStatus" name="status" class="form-control form-select">
                                        <c:forEach items="${projectStatuses}" var="status">
                                            <option value="${status}" 
                                                    ${project != null && project.status == status ? 'selected' : ''}>
                                                ${status}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                
                                <div class="form-group mb-4">
                                    <label class="form-label">Team Members</label>
                                    <div class="team-selector border rounded p-2">
                                        <div class="selected-members mb-2">
                                            <c:forEach items="${projectMembers}" var="member">
                                                <div class="selected-member d-inline-flex align-items-center me-2 mb-2 rounded px-2 py-1 bg-light">
                                                    <input type="hidden" name="teamMembers" value="${member.id}" />
                                                    <img src="${pageContext.request.contextPath}/assets/img/user-default.png" 
                                                         alt="${member.firstName}" class="avatar avatar-sm rounded-circle me-2" />
                                                    <span class="me-2">${member.firstName} ${member.lastName}</span>
                                                    <button type="button" class="remove-member btn btn-sm btn-link text-danger p-0">&times;</button>
                                                </div>
                                            </c:forEach>
                                        </div>
                                        <button type="button" class="btn btn-sm btn-outline-secondary" id="addTeamMemberBtn">
                                            <i class="fas fa-plus"></i> Add Team Member
                                        </button>
                                    </div>
                                </div>
                                
                                <div class="form-actions mt-4 d-flex">
                                    <button type="submit" class="btn btn-primary me-2">
                                        ${param.action == 'new' ? 'Create Project' : 'Save Changes'}
                                    </button>
                                    <a href="${pageContext.request.contextPath}/projects" class="btn btn-outline-secondary">Cancel</a>
                                </div>
                            </form>
                        </div>
                    </div>
                </c:when>
                
                <c:when test="${project != null}">
                    <!-- Project Details View -->
                    <div class="section-header mb-4">
                        <div class="d-flex justify-content-between align-items-center">
                            <h1 class="page-title">${project.name}</h1>
                            <div class="actions">
                                <a href="${pageContext.request.contextPath}/projects?action=edit&id=${project.id}" class="btn btn-outline-primary">
                                    <i class="fas fa-edit"></i> Edit
                                </a>
                            </div>
                        </div>
                    </div>
                    
                    <div class="project-overview mb-5">
                        <div class="card shadow-sm">
                            <div class="card-body p-4">
                                <div class="project-meta d-flex flex-wrap mb-4">
                                    <div class="meta-item me-4 mb-3">
                                        <span class="meta-label d-block text-muted">Status</span>
                                        <span class="status-badge badge rounded-pill status-${project.status.toLowerCase()}">${project.status}</span>
                                    </div>
                                    <div class="meta-item me-4 mb-3">
                                        <span class="meta-label d-block text-muted">Start Date</span>
                                        <span class="meta-value fw-bold"><fmt:formatDate value="${project.startDate}" pattern="MMM d, yyyy" /></span>
                                    </div>
                                    <div class="meta-item me-4 mb-3">
                                        <span class="meta-label d-block text-muted">End Date</span>
                                        <span class="meta-value fw-bold">
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
                                    <div class="meta-item mb-3">
                                        <span class="meta-label d-block text-muted">Progress</span>
                                        <div class="progress-container d-flex align-items-center">
                                            <div class="progress flex-grow-1 me-2" style="height: 8px;">
                                                <div class="progress-bar bg-success" style="width: ${project.completionPercentage}%"></div>
                                            </div>
                                            <span class="progress-percentage fw-bold">${project.completionPercentage}%</span>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="project-description mb-4">
                                    <h3 class="h5 mb-3">Description</h3>
                                    <p class="mb-0">${project.description != null && !empty project.description ? project.description : 'No description provided.'}</p>
                                </div>
                                
                                <div class="project-team">
                                    <h3 class="h5 mb-3">Team</h3>
                                    <div class="team-members d-flex flex-wrap">
                                        <c:forEach items="${projectMembers}" var="member">
                                            <div class="team-member d-flex align-items-center me-3 mb-2">
                                                <img src="${pageContext.request.contextPath}/assets/img/user-default.png" 
                                                     alt="${member.firstName}" class="avatar rounded-circle me-2" />
                                                <div class="member-info">
                                                    <span class="member-name d-block">${member.firstName} ${member.lastName}</span>
                                                    <span class="member-role text-muted small">${member.role}</span>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="section-header mb-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <h2 class="h3">Tasks</h2>
                            <a href="${pageContext.request.contextPath}/task?action=new&projectId=${project.id}" class="btn btn-primary">
                                <i class="fas fa-plus"></i> New Task
                            </a>
                        </div>
                    </div>
                    
                    <div class="tasks-container">
                        <div class="task-filters mb-3 p-3 bg-light rounded d-flex flex-wrap">
                            <div class="filter-group me-3 mb-2">
                                <label for="taskStatusFilter" class="form-label small fw-bold mb-1">Status</label>
                                <select id="taskStatusFilter" class="form-control form-select form-select-sm">
                                    <option value="all">All</option>
                                    <c:forEach items="${taskStatuses}" var="status">
                                        <option value="${status.toLowerCase()}">${status}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="filter-group mb-2">
                                <label for="taskAssigneeFilter" class="form-label small fw-bold mb-1">Assignee</label>
                                <select id="taskAssigneeFilter" class="form-control form-select form-select-sm">
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
                                            <div class="task-card card mb-3 shadow-sm" data-status="${task.status.toLowerCase()}" data-assignee="${task.assignee.id}">
                                                <div class="card-body p-3">
                                                    <div class="task-header d-flex align-items-center mb-2">
                                                        <span class="status-indicator status-${task.status.toLowerCase()} me-2"></span>
                                                        <h3 class="task-title h5 mb-0 flex-grow-1">
                                                            <a href="${pageContext.request.contextPath}/task?id=${task.id}" class="text-decoration-none">${task.title}</a>
                                                        </h3>
                                                        <div class="task-priority badge priority-${task.priority.toLowerCase()}">${task.priority}</div>
                                                    </div>
                                                    <div class="task-body mb-3">
                                                        <p class="task-description mb-0 text-muted">${task.description}</p>
                                                    </div>
                                                    <div class="task-footer d-flex align-items-center">
                                                        <div class="task-meta d-flex flex-wrap">
                                                            <span class="due-date me-3">
                                                                <i class="far fa-calendar me-1"></i>
                                                                Due: <fmt:formatDate value="${task.dueDate}" pattern="MMM d" />
                                                            </span>
                                                            <span class="assignee d-flex align-items-center">
                                                                <img src="${pageContext.request.contextPath}/assets/img/user-default.png" 
                                                                     alt="${task.assignee.firstName}" class="avatar avatar-xs rounded-circle me-1" />
                                                                ${task.assignee.firstName}
                                                            </span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="empty-state text-center p-5 bg-light rounded">
                                        <i class="fas fa-tasks mb-3" style="font-size: 2rem; opacity: 0.3;"></i>
                                        <p class="mb-3">No tasks have been created for this project yet</p>
                                        <a href="${pageContext.request.contextPath}/task?action=new&projectId=${project.id}" class="btn btn-primary">
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
                    <div class="section-header mb-4">
                        <div class="d-flex justify-content-between align-items-center">
                            <h1 class="page-title">My Projects</h1>
                            <a href="${pageContext.request.contextPath}/projects?action=new" class="btn btn-primary">
                                <i class="fas fa-plus"></i> New Project
                            </a>
                        </div>
                    </div>
                    
                    <div class="project-filters mb-4 p-3 bg-light rounded d-flex flex-wrap align-items-end">
                        <div class="filter-group me-3 mb-2">
                            <label for="projectStatusFilter" class="form-label small fw-bold mb-1">Status</label>
                            <select id="projectStatusFilter" class="form-control form-select">
                                <option value="all">All</option>
                                <c:forEach items="${projectStatuses}" var="status">
                                    <option value="${status.toLowerCase()}">${status}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="filter-group mb-2 flex-grow-1">
                            <label for="projectSearchInput" class="form-label small fw-bold mb-1">Search</label>
                            <input type="text" id="projectSearchInput" class="form-control" placeholder="Search projects...">
                        </div>
                    </div>
                    
                    <div class="projects-grid">
                        <c:choose>
                            <c:when test="${not empty userProjects}">
                                <div class="row">
                                    <c:forEach items="${userProjects}" var="project">
                                        <div class="col-md-6 col-lg-4 mb-4">
                                            <div class="project-card card h-100 shadow-sm" data-status="${project.status.toLowerCase()}">
                                                <div class="card-body p-3">
                                                    <div class="project-card-header mb-3">
                                                        <span class="status-badge badge rounded-pill status-${project.status.toLowerCase()} mb-2 d-inline-block">${project.status}</span>
                                                        <h3 class="project-title h5">
                                                            <a href="${pageContext.request.contextPath}/projects?id=${project.id}" class="text-decoration-none">${project.name}</a>
                                                        </h3>
                                                    </div>
                                                    <div class="project-card-body">
                                                        <p class="project-description mb-3 text-muted">${project.description}</p>
                                                        <div class="project-progress mb-3">
                                                            <div class="progress-label d-flex justify-content-between mb-1">
                                                                <span>Progress</span>
                                                                <span>${project.completionPercentage}%</span>
                                                            </div>
                                                            <div class="progress" style="height: 6px;">
                                                                <div class="progress-bar bg-success" style="width: ${project.completionPercentage}%"></div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div class="project-card-footer">
                                                        <div class="project-meta d-flex flex-wrap">
                                                            <span class="task-count badge bg-light text-dark me-2 mb-1">
                                                                <i class="fas fa-tasks me-1"></i>${project.taskCount} tasks
                                                            </span>
                                                            <span class="team-size badge bg-light text-dark me-2 mb-1">
                                                                <i class="fas fa-users me-1"></i>${project.teamMembers.size()} members
                                                            </span>
                                                            <span class="project-dates badge bg-light text-dark mb-1">
                                                                <i class="far fa-calendar me-1"></i>
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
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state text-center p-5 bg-light rounded">
                                    <i class="fas fa-folder-open mb-3" style="font-size: 2rem; opacity: 0.3;"></i>
                                    <p class="mb-3">No projects found</p>
                                    <a href="${pageContext.request.contextPath}/projects?action=new" class="btn btn-primary">
                                        Create Your First Project
                                    </a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
        
        <!-- Project Member Selection Modal -->
        <div class="modal fade" id="teamMemberModal" tabindex="-1" role="dialog" aria-labelledby="teamMemberModalLabel" aria-hidden="true">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="teamMemberModalLabel">Add Team Members</h5>
                        <button type="button" class="btn-close" data-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <input type="text" id="memberSearchInput" class="form-control mb-3" placeholder="Search members...">
                        <div class="member-list">
                            <c:forEach items="${availableUsers}" var="user">
                                <div class="member-item d-flex align-items-center p-2 border-bottom cursor-pointer hover-bg-light" 
                                     data-id="${user.id}" data-name="${user.firstName} ${user.lastName}">
                                    <img src="${pageContext.request.contextPath}/assets/img/user-default.png" 
                                         alt="${user.firstName}" class="avatar rounded-circle me-3" />
                                    <div class="flex-grow-1">
                                        <span class="d-block">${user.firstName} ${user.lastName}</span>
                                        <span class="member-role small text-muted">${user.role}</span>
                                    </div>
                                    <div class="form-check">
                                        <input type="checkbox" class="form-check-input member-select" id="member-check-${user.id}">
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-dismiss="modal">Close</button>
                        <button type="button" class="btn btn-primary" id="addSelectedMembers">Add Selected</button>
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>

<jsp:include page="common/footer.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/projects.js"></script>