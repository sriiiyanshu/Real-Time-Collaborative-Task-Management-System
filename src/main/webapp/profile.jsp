<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="common/header.jsp">
  <jsp:param name="pageTitle" value="My Profile" />
  <jsp:param name="customCss" value="profile.css" />
</jsp:include>

<jsp:include page="common/navigation.jsp">
  <jsp:param name="active" value="profile" />
</jsp:include>

<main class="main-content">
  <div class="container">
    <div class="page-header">
      <h1><i class="fas fa-user-circle"></i> My Profile</h1>
    </div>

    <c:if test="${not empty successMessage}">
      <div class="alert alert-success">
        <i class="fas fa-check-circle"></i> ${successMessage}
      </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
      <div class="alert alert-danger">
        <i class="fas fa-exclamation-circle"></i> ${errorMessage}
      </div>
    </c:if>

    <div class="profile-container">
      <div class="profile-sidebar">
        <div class="profile-image">
          <c:choose>
            <c:when test="${not empty sessionScope.user.profileImage}">
              <img src="${pageContext.request.contextPath}/assets/img/profiles/${sessionScope.user.profileImage}" alt="Profile Image">
            </c:when>
            <c:otherwise>
              <div class="profile-initial">${fn:substring(sessionScope.user.firstName, 0, 1)}${fn:substring(sessionScope.user.lastName, 0, 1)}</div>
            </c:otherwise>
          </c:choose>
          <div class="image-upload">
            <form action="${pageContext.request.contextPath}/user/uploadProfileImage" method="post" enctype="multipart/form-data" id="profileImageForm">
              <input type="file" name="profileImage" id="profileImageInput" accept="image/*" style="display: none;">
              <button type="button" class="btn btn-sm btn-secondary" id="uploadImageBtn">
                <i class="fas fa-camera"></i> Change Photo
              </button>
            </form>
          </div>
        </div>

        <div class="profile-stats">
          <div class="stat-item">
            <div class="stat-value">${userStats.taskCount}</div>
            <div class="stat-label">Tasks</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">${userStats.projectCount}</div>
            <div class="stat-label">Projects</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">${userStats.teamCount}</div>
            <div class="stat-label">Teams</div>
          </div>
        </div>

        <div class="profile-actions">
          <button class="btn btn-outline-secondary btn-block" id="changePasswordBtn">
            <i class="fas fa-key"></i> Change Password
          </button>
        </div>
      </div>

      <div class="profile-content">
        <div class="card profile-details">
          <div class="card-header">
            <h3>Personal Information</h3>
            <button class="btn btn-sm btn-primary" id="editProfileBtn">
              <i class="fas fa-edit"></i> Edit
            </button>
          </div>
          <div class="card-body">
            <div id="profileViewMode">
              <div class="profile-field">
                <label>Name</label>
                <div class="field-value">${sessionScope.user.firstName} ${sessionScope.user.lastName}</div>
              </div>
              <div class="profile-field">
                <label>Email</label>
                <div class="field-value">${sessionScope.user.email}</div>
              </div>
              <div class="profile-field">
                <label>Job Title</label>
                <div class="field-value">${not empty sessionScope.user.jobTitle ? sessionScope.user.jobTitle : '-'}</div>
              </div>
              <div class="profile-field">
                <label>Department</label>
                <div class="field-value">${not empty sessionScope.user.department ? sessionScope.user.department : '-'}</div>
              </div>
              <div class="profile-field">
                <label>Phone</label>
                <div class="field-value">${not empty sessionScope.user.phone ? sessionScope.user.phone : '-'}</div>
              </div>
              <div class="profile-field">
                <label>Member Since</label>
                <div class="field-value"><fmt:formatDate value="${sessionScope.user.registrationDate}" pattern="MMMM d, yyyy" /></div>
              </div>
            </div>

            <div id="profileEditMode" style="display:none;">
              <form action="${pageContext.request.contextPath}/user/updateProfile" method="post" id="profileForm">
                <div class="form-group row">
                  <label class="col-sm-3 col-form-label">First Name</label>
                  <div class="col-sm-9">
                    <input type="text" class="form-control" name="firstName" value="${sessionScope.user.firstName}" required>
                  </div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-3 col-form-label">Last Name</label>
                  <div class="col-sm-9">
                    <input type="text" class="form-control" name="lastName" value="${sessionScope.user.lastName}" required>
                  </div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-3 col-form-label">Email</label>
                  <div class="col-sm-9">
                    <input type="email" class="form-control" name="email" value="${sessionScope.user.email}" required>
                  </div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-3 col-form-label">Job Title</label>
                  <div class="col-sm-9">
                    <input type="text" class="form-control" name="jobTitle" value="${sessionScope.user.jobTitle}">
                  </div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-3 col-form-label">Department</label>
                  <div class="col-sm-9">
                    <input type="text" class="form-control" name="department" value="${sessionScope.user.department}">
                  </div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-3 col-form-label">Phone</label>
                  <div class="col-sm-9">
                    <input type="text" class="form-control" name="phone" value="${sessionScope.user.phone}">
                  </div>
                </div>
                <div class="form-buttons">
                  <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save"></i> Save Changes
                  </button>
                  <button type="button" class="btn btn-secondary" id="cancelEditBtn">
                    <i class="fas fa-times"></i> Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>

        <div class="card activity-history">
          <div class="card-header">
            <h3>Recent Activity</h3>
          </div>
          <div class="card-body">
            <c:choose>
              <c:when test="${not empty userActivity}">
                <ul class="timeline">
                  <c:forEach items="${userActivity}" var="activity">
                    <li class="timeline-item">
                      <div class="timeline-marker ${fn:toLowerCase(activity.type)}-marker">
                        <c:choose>
                          <c:when test="${activity.type == 'TASK_CREATED'}"><i class="fas fa-plus"></i></c:when>
                          <c:when test="${activity.type == 'TASK_COMPLETED'}"><i class="fas fa-check"></i></c:when>
                          <c:when test="${activity.type == 'COMMENT_ADDED'}"><i class="fas fa-comment"></i></c:when>
                          <c:when test="${activity.type == 'PROJECT_CREATED'}"><i class="fas fa-folder-plus"></i></c:when>
                          <c:when test="${activity.type == 'FILE_UPLOADED'}"><i class="fas fa-file-upload"></i></c:when>
                          <c:otherwise><i class="fas fa-circle"></i></c:otherwise>
                        </c:choose>
                      </div>
                      <div class="timeline-content">
                        <h4 class="timeline-title">${activity.description}</h4>
                        <p class="timeline-time">
                          <i class="far fa-clock"></i> <fmt:formatDate value="${activity.timestamp}" pattern="MMM d, h:mm a" />
                        </p>
                      </div>
                    </li>
                  </c:forEach>
                </ul>
              </c:when>
              <c:otherwise>
                <div class="empty-state">
                  <i class="fas fa-history empty-icon"></i>
                  <p>No recent activity found</p>
                </div>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Change Password Modal -->
  <div class="modal fade" id="changePasswordModal" tabindex="-1" role="dialog" aria-labelledby="changePasswordModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="changePasswordModalLabel">
            <i class="fas fa-key"></i> Change Password
          </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <form id="changePasswordForm" action="${pageContext.request.contextPath}/user/changePassword" method="post">
            <div class="form-group">
              <label for="currentPassword">Current Password</label>
              <input type="password" class="form-control" id="currentPassword" name="currentPassword" required>
            </div>
            <div class="form-group">
              <label for="newPassword">New Password</label>
              <input type="password" class="form-control" id="newPassword" name="newPassword" required>
              <small class="form-text text-muted">Password must be at least 8 characters long and include uppercase, lowercase, number, and special character.</small>
            </div>
            <div class="form-group">
              <label for="confirmPassword">Confirm New Password</label>
              <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
          <button type="button" class="btn btn-primary" id="savePasswordBtn">Save Changes</button>
        </div>
      </div>
    </div>
  </div>
</main>

<jsp:include page="common/footer.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/profile.js"></script>