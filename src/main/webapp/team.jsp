<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="common/header.jsp">
  <jsp:param name="pageTitle" value="Team Management" />
</jsp:include>
<jsp:include page="common/navigation.jsp" />

<div class="container-fluid">
  <div class="row">
    <jsp:include page="common/sidebar.jsp" />
    
    <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <h1 class="h2">Team Management</h1>
        <div class="btn-toolbar mb-2 mb-md-0">
          <button type="button" class="btn btn-sm btn-outline-primary" id="createTeamBtn">
            <i class="bi bi-people"></i> Create New Team
          </button>
        </div>
      </div>

      <c:if test="${not empty successMessage}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
          ${successMessage}
          <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
      </c:if>
      
      <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
          ${errorMessage}
          <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
      </c:if>
      
      <c:choose>
        <c:when test="${empty team}">
          <!-- Teams List View -->
          <div class="row row-cols-1 row-cols-md-2 row-cols-xl-3 g-4">
            <c:forEach var="teamItem" items="${teams}">
              <div class="col">
                <div class="card h-100 team-card">
                  <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="card-title mb-0">
                      <a href="${pageContext.request.contextPath}/team?id=${teamItem.id}" class="text-decoration-none">
                        ${teamItem.name}
                      </a>
                    </h5>
                    <span class="badge bg-primary rounded-pill">${teamItem.memberCount} members</span>
                  </div>
                  <div class="card-body">
                    <p class="card-text">${teamItem.description}</p>
                    <div class="team-members-preview">
                      <c:forEach var="member" items="${teamItem.members}" varStatus="status">
                        <c:if test="${status.index < 5}">
                          <div class="team-member-avatar" title="${member.fullName}">
                            <c:choose>
                              <c:when test="${not empty member.avatarUrl}">
                                <img src="${member.avatarUrl}" alt="${member.fullName}">
                              </c:when>
                              <c:otherwise>
                                <div class="avatar-placeholder">
                                  ${member.initials}
                                </div>
                              </c:otherwise>
                            </c:choose>
                            <c:if test="${member.id == teamItem.leaderId}">
                              <span class="team-leader-badge" title="Team Leader">
                                <i class="bi bi-star-fill"></i>
                              </span>
                            </c:if>
                          </div>
                        </c:if>
                      </c:forEach>
                      <c:if test="${teamItem.memberCount > 5}">
                        <div class="team-member-avatar more-members">
                          +${teamItem.memberCount - 5}
                        </div>
                      </c:if>
                    </div>
                  </div>
                  <div class="card-footer">
                    <div class="d-flex justify-content-between">
                      <div>
                        <small class="text-muted">
                          Created: <fmt:formatDate value="${teamItem.createdAt}" pattern="MMM d, yyyy" />
                        </small>
                      </div>
                      <div>
                        <c:if test="${teamItem.leaderId == currentUser.id || userRole == 'ADMIN'}">
                          <button class="btn btn-sm btn-outline-secondary edit-team-btn" data-team-id="${teamItem.id}">
                            <i class="bi bi-pencil"></i>
                          </button>
                          <button class="btn btn-sm btn-outline-danger delete-team-btn" data-team-id="${teamItem.id}" data-team-name="${teamItem.name}">
                            <i class="bi bi-trash"></i>
                          </button>
                        </c:if>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </c:forEach>
            
            <c:if test="${empty teams}">
              <div class="col-12">
                <div class="empty-state text-center py-5">
                  <i class="bi bi-people display-4"></i>
                  <h3 class="mt-3">No Teams</h3>
                  <p class="text-muted">There are no teams created yet. Click the "Create New Team" button to get started.</p>
                </div>
              </div>
            </c:if>
          </div>
        </c:when>
        
        <c:otherwise>
          <!-- Team Detail View -->
          <div class="row mb-4">
            <div class="col-md-8">
              <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                  <h4>${team.name}</h4>
                  <div>
                    <c:if test="${team.leaderId == currentUser.id || userRole == 'ADMIN'}">
                      <button class="btn btn-sm btn-outline-primary" id="editTeamDetailBtn">
                        <i class="bi bi-pencil"></i> Edit Team
                      </button>
                    </c:if>
                    <button class="btn btn-sm btn-outline-secondary" id="backToTeamsBtn">
                      <i class="bi bi-arrow-left"></i> Back to Teams
                    </button>
                  </div>
                </div>
                <div class="card-body">
                  <div class="team-info mb-4">
                    <div class="row">
                      <div class="col-md-6">
                        <h5>Description</h5>
                        <p>${team.description}</p>
                      </div>
                      <div class="col-md-6">
                        <h5>Team Details</h5>
                        <ul class="list-unstyled">
                          <li>
                            <strong>Team Leader:</strong> ${teamLeader.fullName}
                            ${teamLeader.id == currentUser.id ? ' (You)' : ''}
                          </li>
                          <li><strong>Created:</strong> <fmt:formatDate value="${team.createdAt}" pattern="MMM d, yyyy" /></li>
                          <li><strong>Members:</strong> ${team.memberCount}</li>
                          <li><strong>Projects:</strong> ${team.projectCount}</li>
                        </ul>
                      </div>
                    </div>
                  </div>
                  
                  <div class="team-statistics mb-4">
                    <h5>Team Activity</h5>
                    <div class="row">
                      <div class="col-md-3">
                        <div class="card">
                          <div class="card-body text-center">
                            <h5>${teamStats.openTasks}</h5>
                            <p class="text-muted">Open Tasks</p>
                          </div>
                        </div>
                      </div>
                      <div class="col-md-3">
                        <div class="card">
                          <div class="card-body text-center">
                            <h5>${teamStats.completedTasks}</h5>
                            <p class="text-muted">Completed Tasks</p>
                          </div>
                        </div>
                      </div>
                      <div class="col-md-3">
                        <div class="card">
                          <div class="card-body text-center">
                            <h5>${teamStats.onTimeCompletionRate}%</h5>
                            <p class="text-muted">On-time Rate</p>
                          </div>
                        </div>
                      </div>
                      <div class="col-md-3">
                        <div class="card">
                          <div class="card-body text-center">
                            <h5>${teamStats.averageTasksPerDay}</h5>
                            <p class="text-muted">Avg Tasks/Day</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div class="team-projects mb-4">
                    <h5>Projects</h5>
                    <div class="list-group">
                      <c:forEach var="project" items="${teamProjects}">
                        <a href="${pageContext.request.contextPath}/project?id=${project.id}" class="list-group-item list-group-item-action">
                          <div class="d-flex w-100 justify-content-between">
                            <h6 class="mb-1">${project.name}</h6>
                            <small>
                              <span class="badge bg-${project.status == 'ACTIVE' ? 'success' : (project.status == 'ON_HOLD' ? 'warning' : 'secondary')}">
                                ${project.status}
                              </span>
                            </small>
                          </div>
                          <p class="mb-1">${project.description}</p>
                          <small>Tasks: ${project.taskCount} | Deadline: <fmt:formatDate value="${project.deadline}" pattern="MMM d, yyyy" /></small>
                        </a>
                      </c:forEach>
                      
                      <c:if test="${empty teamProjects}">
                        <div class="list-group-item">
                          <p class="text-muted mb-0">This team has no projects assigned yet.</p>
                        </div>
                      </c:if>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="col-md-4">
              <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                  <h5>Team Members</h5>
                  <c:if test="${team.leaderId == currentUser.id || userRole == 'ADMIN'}">
                    <button class="btn btn-sm btn-outline-primary" id="addMembersBtn">
                      <i class="bi bi-plus"></i> Add Members
                    </button>
                  </c:if>
                </div>
                <div class="card-body p-0">
                  <ul class="list-group list-group-flush team-members-list">
                    <c:forEach var="member" items="${teamMembers}">
                      <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div class="d-flex align-items-center">
                          <div class="team-member-avatar me-3">
                            <c:choose>
                              <c:when test="${not empty member.avatarUrl}">
                                <img src="${member.avatarUrl}" alt="${member.fullName}">
                              </c:when>
                              <c:otherwise>
                                <div class="avatar-placeholder">
                                  ${member.initials}
                                </div>
                              </c:otherwise>
                            </c:choose>
                          </div>
                          <div>
                            <h6 class="mb-0">
                              ${member.fullName}
                              <c:if test="${member.id == team.leaderId}">
                                <span class="team-leader-label ms-2">
                                  <i class="bi bi-star-fill text-warning"></i> Leader
                                </span>
                              </c:if>
                            </h6>
                            <p class="text-muted small mb-0">${member.jobTitle}</p>
                          </div>
                        </div>
                        <div class="member-actions">
                          <c:if test="${team.leaderId == currentUser.id || userRole == 'ADMIN'}">
                            <c:if test="${member.id != team.leaderId}">
                              <button class="btn btn-sm btn-outline-primary promote-member-btn" title="Promote to Team Leader" data-member-id="${member.id}" data-member-name="${member.fullName}">
                                <i class="bi bi-star"></i>
                              </button>
                            </c:if>
                            <c:if test="${member.id != currentUser.id}">
                              <button class="btn btn-sm btn-outline-danger remove-member-btn" title="Remove from Team" data-member-id="${member.id}" data-member-name="${member.fullName}">
                                <i class="bi bi-person-dash"></i>
                              </button>
                            </c:if>
                          </c:if>
                        </div>
                      </li>
                    </c:forEach>
                  </ul>
                </div>
              </div>
              
              <!-- Recent Activity -->
              <div class="card mt-4">
                <div class="card-header">
                  <h5>Recent Activity</h5>
                </div>
                <div class="card-body p-0">
                  <ul class="list-group list-group-flush activity-list">
                    <c:forEach var="activity" items="${teamActivity}">
                      <li class="list-group-item">
                        <div class="d-flex">
                          <div class="activity-icon me-3 ${activity.iconClass}">
                            <i class="bi ${activity.icon}"></i>
                          </div>
                          <div>
                            <p class="mb-1">${activity.description}</p>
                            <small class="text-muted">
                              ${activity.userFullName} • <fmt:formatDate value="${activity.timestamp}" pattern="MMM d, yyyy h:mm a" />
                            </small>
                          </div>
                        </div>
                      </li>
                    </c:forEach>
                    
                    <c:if test="${empty teamActivity}">
                      <li class="list-group-item">
                        <p class="text-muted mb-0">No recent activity.</p>
                      </li>
                    </c:if>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </c:otherwise>
      </c:choose>
    </main>
  </div>
</div>

<!-- Create/Edit Team Modal -->
<div class="modal fade" id="teamModal" tabindex="-1" aria-labelledby="teamModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="teamModalLabel">Create New Team</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="teamForm" action="${pageContext.request.contextPath}/team/save" method="post">
        <div class="modal-body">
          <input type="hidden" id="teamId" name="id" value="">
          <div class="mb-3">
            <label for="teamName" class="form-label">Team Name</label>
            <input type="text" class="form-control" id="teamName" name="name" required>
          </div>
          <div class="mb-3">
            <label for="teamDescription" class="form-label">Description</label>
            <textarea class="form-control" id="teamDescription" name="description" rows="3"></textarea>
          </div>
          <div class="mb-3" id="teamLeaderSelection">
            <label for="teamLeader" class="form-label">Team Leader</label>
            <select class="form-select" id="teamLeader" name="leaderId">
              <option value="${currentUser.id}" selected>${currentUser.fullName} (You)</option>
              <c:forEach var="user" items="${availableLeaders}">
                <c:if test="${user.id != currentUser.id}">
                  <option value="${user.id}">${user.fullName}</option>
                </c:if>
              </c:forEach>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary">Save</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Add Members Modal -->
<div class="modal fade" id="addMembersModal" tabindex="-1" aria-labelledby="addMembersModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="addMembersModalLabel">Add Team Members</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addMembersForm" action="${pageContext.request.contextPath}/team/add-members" method="post">
        <div class="modal-body">
          <input type="hidden" name="teamId" value="${team.id}">
          <div class="mb-3">
            <label for="searchMembers" class="form-label">Search Users</label>
            <input type="text" class="form-control" id="searchMembers" placeholder="Type to search...">
          </div>
          <div class="mb-3">
            <label class="form-label">Available Users</label>
            <div class="user-list" id="availableUsers">
              <c:forEach var="user" items="${availableUsers}">
                <div class="form-check user-item">
                  <input class="form-check-input" type="checkbox" name="selectedUsers" value="${user.id}" id="user${user.id}">
                  <label class="form-check-label d-flex align-items-center" for="user${user.id}">
                    <div class="user-avatar me-2">
                      <c:choose>
                        <c:when test="${not empty user.avatarUrl}">
                          <img src="${user.avatarUrl}" alt="${user.fullName}" class="avatar-sm">
                        </c:when>
                        <c:otherwise>
                          <div class="avatar-placeholder-sm">
                            ${user.initials}
                          </div>
                        </c:otherwise>
                      </c:choose>
                    </div>
                    <div>
                      <div>${user.fullName}</div>
                      <small class="text-muted">${user.email}</small>
                    </div>
                  </label>
                </div>
              </c:forEach>
              
              <c:if test="${empty availableUsers}">
                <p class="text-muted">No available users to add.</p>
              </c:if>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary">Add Selected Members</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Delete Team Confirmation Modal -->
<div class="modal fade" id="deleteTeamModal" tabindex="-1" aria-labelledby="deleteTeamModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteTeamModalLabel">Confirm Delete</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <p>Are you sure you want to delete the team <strong id="deleteTeamName"></strong>?</p>
        <p class="text-danger"><strong>Warning:</strong> This action cannot be undone and will remove all team associations.</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
        <form id="deleteTeamForm" action="${pageContext.request.contextPath}/team/delete" method="post">
          <input type="hidden" id="deleteTeamId" name="id">
          <button type="submit" class="btn btn-danger">Delete Team</button>
        </form>
      </div>
    </div>
  </div>
</div>

<!-- Remove Member Confirmation Modal -->
<div class="modal fade" id="removeMemberModal" tabindex="-1" aria-labelledby="removeMemberModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="removeMemberModalLabel">Remove Team Member</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <p>Are you sure you want to remove <strong id="removeMemberName"></strong> from the team?</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
        <form id="removeMemberForm" action="${pageContext.request.contextPath}/team/remove-member" method="post">
          <input type="hidden" name="teamId" value="${team.id}">
          <input type="hidden" id="removeMemberId" name="userId">
          <button type="submit" class="btn btn-danger">Remove</button>
        </form>
      </div>
    </div>
  </div>
</div>

<!-- Promote Member Confirmation Modal -->
<div class="modal fade" id="promoteMemberModal" tabindex="-1" aria-labelledby="promoteMemberModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="promoteMemberModalLabel">Promote to Team Leader</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <p>Are you sure you want to promote <strong id="promoteMemberName"></strong> to team leader?</p>
        <p class="text-warning"><strong>Note:</strong> You will no longer be the team leader after this action.</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
        <form id="promoteMemberForm" action="${pageContext.request.contextPath}/team/promote-leader" method="post">
          <input type="hidden" name="teamId" value="${team.id}">
          <input type="hidden" id="promoteMemberId" name="userId">
          <button type="submit" class="btn btn-primary">Promote</button>
        </form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="common/footer.jsp" />

<script>
  document.addEventListener('DOMContentLoaded', function() {
    // Create Team Button
    const createTeamBtn = document.getElementById('createTeamBtn');
    const teamModal = new bootstrap.Modal(document.getElementById('teamModal'));
    const teamForm = document.getElementById('teamForm');
    const teamId = document.getElementById('teamId');
    const teamName = document.getElementById('teamName');
    const teamDescription = document.getElementById('teamDescription');
    const teamLeaderSelection = document.getElementById('teamLeaderSelection');
    const teamModalLabel = document.getElementById('teamModalLabel');
    
    if (createTeamBtn) {
      createTeamBtn.addEventListener('click', function() {
        // Reset form for creating a new team
        teamModalLabel.textContent = 'Create New Team';
        teamId.value = '';
        teamName.value = '';
        teamDescription.value = '';
        teamLeaderSelection.style.display = '';
        teamModal.show();
      });
    }

    // Edit Team Buttons (list view)
    document.querySelectorAll('.edit-team-btn').forEach(button => {
      button.addEventListener('click', function() {
        const id = this.dataset.teamId;
        
        // Fetch team details via AJAX
        fetch('${pageContext.request.contextPath}/team/get?id=' + id)
          .then(response => response.json())
          .then(data => {
            if (data.success) {
              // Populate form with team data
              teamModalLabel.textContent = 'Edit Team';
              teamId.value = data.team.id;
              teamName.value = data.team.name;
              teamDescription.value = data.team.description;
              
              // Hide leader selection for existing teams (can only be changed via promotion)
              teamLeaderSelection.style.display = 'none';
              
              teamModal.show();
            } else {
              alert('Error: ' + data.message);
            }
          })
          .catch(error => {
            console.error('Error:', error);
            alert('Failed to load team details');
          });
      });
    });
    
    // Edit Team Detail Button (detail view)
    const editTeamDetailBtn = document.getElementById('editTeamDetailBtn');
    if (editTeamDetailBtn) {
      editTeamDetailBtn.addEventListener('click', function() {
        // Populate form with current team data
        teamModalLabel.textContent = 'Edit Team';
        teamId.value = '${team.id}';
        teamName.value = '${team.name}';
        teamDescription.value = '${team.description}';
        
        // Hide leader selection for existing teams
        teamLeaderSelection.style.display = 'none';
        
        teamModal.show();
      });
    }
    
    // Delete Team Buttons
    const deleteTeamModal = new bootstrap.Modal(document.getElementById('deleteTeamModal'));
    const deleteTeamId = document.getElementById('deleteTeamId');
    const deleteTeamName = document.getElementById('deleteTeamName');
    
    document.querySelectorAll('.delete-team-btn').forEach(button => {
      button.addEventListener('click', function() {
        deleteTeamId.value = this.dataset.teamId;
        deleteTeamName.textContent = this.dataset.teamName;
        deleteTeamModal.show();
      });
    });
    
    // Back to Teams Button
    const backToTeamsBtn = document.getElementById('backToTeamsBtn');
    if (backToTeamsBtn) {
      backToTeamsBtn.addEventListener('click', function() {
        window.location.href = '${pageContext.request.contextPath}/team';
      });
    }
    
    // Add Members Button
    const addMembersBtn = document.getElementById('addMembersBtn');
    const addMembersModal = new bootstrap.Modal(document.getElementById('addMembersModal'));
    
    if (addMembersBtn) {
      addMembersBtn.addEventListener('click', function() {
        addMembersModal.show();
      });
    }
    
    // Member Search Functionality
    const searchMembers = document.getElementById('searchMembers');
    const availableUsers = document.getElementById('availableUsers');
    
    if (searchMembers && availableUsers) {
      searchMembers.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();
        const userItems = availableUsers.querySelectorAll('.user-item');
        
        userItems.forEach(item => {
          const label = item.querySelector('label').textContent.toLowerCase();
          if (label.includes(searchTerm)) {
            item.style.display = '';
          } else {
            item.style.display = 'none';
          }
        });
      });
    }
    
    // Remove Member Buttons
    const removeMemberModal = new bootstrap.Modal(document.getElementById('removeMemberModal'));
    const removeMemberId = document.getElementById('removeMemberId');
    const removeMemberName = document.getElementById('removeMemberName');
    
    document.querySelectorAll('.remove-member-btn').forEach(button => {
      button.addEventListener('click', function() {
        removeMemberId.value = this.dataset.memberId;
        removeMemberName.textContent = this.dataset.memberName;
        removeMemberModal.show();
      });
    });
    
    // Promote Member Buttons
    const promoteMemberModal = new bootstrap.Modal(document.getElementById('promoteMemberModal'));
    const promoteMemberId = document.getElementById('promoteMemberId');
    const promoteMemberName = document.getElementById('promoteMemberName');
    
    document.querySelectorAll('.promote-member-btn').forEach(button => {
      button.addEventListener('click', function() {
        promoteMemberId.value = this.dataset.memberId;
        promoteMemberName.textContent = this.dataset.memberName;
        promoteMemberModal.show();
      });
    });
  });
</script>