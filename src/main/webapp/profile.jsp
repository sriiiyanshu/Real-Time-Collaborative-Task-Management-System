<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="common/header.jsp">
  <jsp:param name="pageTitle" value="User Profile" />
</jsp:include>
<jsp:include page="common/navigation.jsp" />

<div class="container-fluid">
  <div class="row">
    <jsp:include page="common/sidebar.jsp" />
    
    <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <h1 class="h2">My Profile</h1>
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

      <div class="row">
        <div class="col-md-4">
          <div class="card profile-card mb-4">
            <div class="card-body text-center">
              <div class="profile-avatar-container">
                <c:choose>
                  <c:when test="${not empty user.avatarUrl}">
                    <img src="${user.avatarUrl}" alt="User Avatar" class="profile-avatar">
                  </c:when>
                  <c:otherwise>
                    <div class="profile-avatar-placeholder">
                      ${user.initials}
                    </div>
                  </c:otherwise>
                </c:choose>
                <button class="edit-avatar-btn" id="changeAvatarBtn">
                  <i class="bi bi-camera"></i>
                </button>
              </div>
              <h3 class="my-3">${user.fullName}</h3>
              <p class="text-muted mb-1">${user.jobTitle}</p>
              <p class="text-muted mb-3">${user.department}</p>
              <div class="user-status">
                <span class="badge ${user.active ? 'bg-success' : 'bg-secondary'}">
                  ${user.active ? 'Active' : 'Inactive'}
                </span>
                <span class="badge bg-info">
                  ${user.role}
                </span>
              </div>
            </div>
          </div>
          
          <div class="card mb-4">
            <div class="card-header">Contact Information</div>
            <div class="card-body">
              <div class="mb-3">
                <label class="form-label text-muted">Email</label>
                <p><i class="bi bi-envelope-fill me-2"></i>${user.email}</p>
              </div>
              <div class="mb-3">
                <label class="form-label text-muted">Phone</label>
                <p><i class="bi bi-telephone-fill me-2"></i>${user.phone}</p>
              </div>
              <div class="mb-3">
                <label class="form-label text-muted">Location</label>
                <p><i class="bi bi-geo-alt-fill me-2"></i>${user.location}</p>
              </div>
            </div>
          </div>
          
          <div class="card mb-4">
            <div class="card-header">Teams</div>
            <div class="card-body">
              <ul class="list-group list-group-flush">
                <c:forEach var="team" items="${userTeams}">
                  <li class="list-group-item d-flex justify-content-between align-items-center">
                    <span>
                      <c:if test="${team.isLeader}"><i class="bi bi-star-fill text-warning me-2"></i></c:if>
                      ${team.name}
                    </span>
                    <span class="badge bg-primary rounded-pill">${team.memberCount} members</span>
                  </li>
                </c:forEach>
              </ul>
            </div>
          </div>
        </div>
        
        <div class="col-md-8">
          <div class="card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
              <span>Profile Information</span>
              <button class="btn btn-sm btn-outline-primary" id="editProfileBtn">
                <i class="bi bi-pencil"></i> Edit
              </button>
            </div>
            
            <div class="card-body">
              <form id="profileForm" action="${pageContext.request.contextPath}/user/update-profile" method="post" style="display: none;">
                <div class="row mb-3">
                  <div class="col">
                    <label for="firstName" class="form-label">First Name</label>
                    <input type="text" class="form-control" id="firstName" name="firstName" value="${user.firstName}" required>
                  </div>
                  <div class="col">
                    <label for="lastName" class="form-label">Last Name</label>
                    <input type="text" class="form-control" id="lastName" name="lastName" value="${user.lastName}" required>
                  </div>
                </div>
                
                <div class="mb-3">
                  <label for="email" class="form-label">Email address</label>
                  <input type="email" class="form-control" id="email" name="email" value="${user.email}" required>
                </div>
                
                <div class="mb-3">
                  <label for="phone" class="form-label">Phone</label>
                  <input type="tel" class="form-control" id="phone" name="phone" value="${user.phone}">
                </div>
                
                <div class="mb-3">
                  <label for="location" class="form-label">Location</label>
                  <input type="text" class="form-control" id="location" name="location" value="${user.location}">
                </div>
                
                <div class="mb-3">
                  <label for="jobTitle" class="form-label">Job Title</label>
                  <input type="text" class="form-control" id="jobTitle" name="jobTitle" value="${user.jobTitle}">
                </div>
                
                <div class="mb-3">
                  <label for="department" class="form-label">Department</label>
                  <input type="text" class="form-control" id="department" name="department" value="${user.department}">
                </div>
                
                <div class="mb-3">
                  <label for="bio" class="form-label">Bio</label>
                  <textarea class="form-control" id="bio" name="bio" rows="3">${user.bio}</textarea>
                </div>
                
                <button type="submit" class="btn btn-primary">Save Changes</button>
                <button type="button" class="btn btn-secondary" id="cancelEditBtn">Cancel</button>
              </form>
              
              <div id="profileInfo">
                <div class="row mb-3">
                  <div class="col">
                    <label class="form-label text-muted">First Name</label>
                    <p>${user.firstName}</p>
                  </div>
                  <div class="col">
                    <label class="form-label text-muted">Last Name</label>
                    <p>${user.lastName}</p>
                  </div>
                </div>
                
                <div class="mb-3">
                  <label class="form-label text-muted">Job Title</label>
                  <p>${user.jobTitle}</p>
                </div>
                
                <div class="mb-3">
                  <label class="form-label text-muted">Department</label>
                  <p>${user.department}</p>
                </div>
                
                <div class="mb-3">
                  <label class="form-label text-muted">Bio</label>
                  <p>${not empty user.bio ? user.bio : '<em>No bio information</em>'}</p>
                </div>
                
                <div class="mb-3">
                  <label class="form-label text-muted">Joined Date</label>
                  <p><fmt:formatDate value="${user.createdAt}" pattern="MMM d, yyyy" /></p>
                </div>
                
                <div class="mb-3">
                  <label class="form-label text-muted">Last Login</label>
                  <p><fmt:formatDate value="${user.lastLogin}" pattern="MMM d, yyyy h:mm a" /></p>
                </div>
              </div>
            </div>
          </div>
          
          <div class="card mb-4">
            <div class="card-header">Change Password</div>
            <div class="card-body">
              <form id="passwordForm" action="${pageContext.request.contextPath}/user/change-password" method="post">
                <div class="mb-3">
                  <label for="currentPassword" class="form-label">Current Password</label>
                  <div class="password-input">
                    <input type="password" class="form-control" id="currentPassword" name="currentPassword" required>
                    <button type="button" class="toggle-password" aria-label="Toggle Password Visibility"></button>
                  </div>
                </div>
                
                <div class="mb-3">
                  <label for="newPassword" class="form-label">New Password</label>
                  <div class="password-input">
                    <input type="password" class="form-control" id="newPassword" name="newPassword" required>
                    <button type="button" class="toggle-password" aria-label="Toggle Password Visibility"></button>
                  </div>
                  <div class="password-strength mt-2" id="passwordStrength">
                    <div class="progress">
                      <div class="progress-bar" role="progressbar" style="width: 0%;" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"></div>
                    </div>
                    <small class="text-muted password-feedback">Password strength indicator</small>
                  </div>
                </div>
                
                <div class="mb-3">
                  <label for="confirmPassword" class="form-label">Confirm New Password</label>
                  <div class="password-input">
                    <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
                    <button type="button" class="toggle-password" aria-label="Toggle Password Visibility"></button>
                  </div>
                  <div id="passwordMatch" class="form-text"></div>
                </div>
                
                <button type="submit" class="btn btn-primary" id="changePasswordBtn">Change Password</button>
              </form>
            </div>
          </div>
          
          <div class="card mb-4">
            <div class="card-header">Activity</div>
            <div class="card-body">
              <ul class="timeline">
                <c:forEach var="activity" items="${userActivities}">
                  <li class="timeline-item mb-3">
                    <span class="timeline-icon bg-${activity.iconClass}">
                      <i class="bi ${activity.icon}"></i>
                    </span>
                    <h5 class="fw-bold">${activity.title}</h5>
                    <p class="text-muted mb-2 fw-bold">${activity.description}</p>
                    <p class="text-muted">
                      <small><fmt:formatDate value="${activity.timestamp}" pattern="MMM d, yyyy h:mm a" /></small>
                    </p>
                  </li>
                </c:forEach>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</div>

<!-- Change Avatar Modal -->
<div class="modal fade" id="changeAvatarModal" tabindex="-1" aria-labelledby="changeAvatarModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="changeAvatarModalLabel">Change Profile Photo</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <div class="avatar-upload-container text-center mb-3">
          <div class="avatar-preview" id="avatarPreview">
            <c:choose>
              <c:when test="${not empty user.avatarUrl}">
                <img src="${user.avatarUrl}" alt="User Avatar" class="avatar-image">
              </c:when>
              <c:otherwise>
                <div class="avatar-placeholder">
                  ${user.initials}
                </div>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
        
        <form id="avatarForm" action="${pageContext.request.contextPath}/user/update-avatar" method="post" enctype="multipart/form-data">
          <div class="mb-3">
            <label for="avatarFile" class="form-label">Select Image</label>
            <input class="form-control" type="file" id="avatarFile" name="avatarFile" accept="image/*">
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
        <button type="button" class="btn btn-danger" id="removeAvatarBtn">Remove</button>
        <button type="button" class="btn btn-primary" id="saveAvatarBtn">Save</button>
      </div>
    </div>
  </div>
</div>

<jsp:include page="common/footer.jsp" />

<script>
  document.addEventListener('DOMContentLoaded', function() {
    // Edit profile button
    const editProfileBtn = document.getElementById('editProfileBtn');
    const cancelEditBtn = document.getElementById('cancelEditBtn');
    const profileForm = document.getElementById('profileForm');
    const profileInfo = document.getElementById('profileInfo');
    
    editProfileBtn.addEventListener('click', function() {
      profileInfo.style.display = 'none';
      profileForm.style.display = 'block';
      editProfileBtn.style.display = 'none';
    });
    
    cancelEditBtn.addEventListener('click', function() {
      profileInfo.style.display = 'block';
      profileForm.style.display = 'none';
      editProfileBtn.style.display = 'inline-block';
    });
    
    // Password toggle visibility
    const toggleButtons = document.querySelectorAll('.toggle-password');
    toggleButtons.forEach((button) => {
      button.addEventListener('click', function() {
        const passwordInput = this.previousElementSibling;
        const type = passwordInput.getAttribute('type');
        passwordInput.setAttribute('type', type === 'password' ? 'text' : 'password');
        this.classList.toggle('show');
      });
    });
    
    // Password strength meter
    const newPassword = document.getElementById('newPassword');
    const passwordStrengthBar = document.querySelector('#passwordStrength .progress-bar');
    const passwordFeedback = document.querySelector('#passwordStrength .password-feedback');
    
    if (newPassword) {
      newPassword.addEventListener('input', function() {
        const value = this.value;
        let strength = 0;
        let feedback = '';
        
        if (value.length >= 8) {
          strength += 20;
        }
        
        if (value.match(/[a-z]+/)) {
          strength += 20;
        }
        
        if (value.match(/[A-Z]+/)) {
          strength += 20;
        }
        
        if (value.match(/[0-9]+/)) {
          strength += 20;
        }
        
        if (value.match(/[!@#$%^&*(),.?":{}|<>]+/)) {
          strength += 20;
        }
        
        passwordStrengthBar.style.width = strength + '%';
        
        if (strength < 40) {
          passwordStrengthBar.className = 'progress-bar bg-danger';
          feedback = 'Weak password';
        } else if (strength < 80) {
          passwordStrengthBar.className = 'progress-bar bg-warning';
          feedback = 'Moderate password';
        } else {
          passwordStrengthBar.className = 'progress-bar bg-success';
          feedback = 'Strong password';
        }
        
        passwordFeedback.textContent = feedback;
      });
    }
    
    // Password matching validation
    const confirmPassword = document.getElementById('confirmPassword');
    const passwordMatch = document.getElementById('passwordMatch');
    const changePasswordBtn = document.getElementById('changePasswordBtn');
    
    if (confirmPassword && newPassword) {
      function checkPasswordMatch() {
        if (newPassword.value && confirmPassword.value) {
          if (newPassword.value !== confirmPassword.value) {
            passwordMatch.textContent = 'Passwords do not match';
            passwordMatch.className = 'form-text text-danger';
            changePasswordBtn.disabled = true;
          } else {
            passwordMatch.textContent = 'Passwords match';
            passwordMatch.className = 'form-text text-success';
            changePasswordBtn.disabled = false;
          }
        } else {
          passwordMatch.textContent = '';
          changePasswordBtn.disabled = false;
        }
      }
      
      newPassword.addEventListener('input', checkPasswordMatch);
      confirmPassword.addEventListener('input', checkPasswordMatch);
    }
    
    // Avatar handling
    const changeAvatarBtn = document.getElementById('changeAvatarBtn');
    const changeAvatarModal = new bootstrap.Modal(document.getElementById('changeAvatarModal'));
    const avatarFile = document.getElementById('avatarFile');
    const avatarPreview = document.getElementById('avatarPreview');
    const saveAvatarBtn = document.getElementById('saveAvatarBtn');
    const removeAvatarBtn = document.getElementById('removeAvatarBtn');
    
    changeAvatarBtn.addEventListener('click', function() {
      changeAvatarModal.show();
    });
    
    if (avatarFile) {
      avatarFile.addEventListener('change', function() {
        if (this.files && this.files[0]) {
          const reader = new FileReader();
          
          reader.onload = function(e) {
            avatarPreview.innerHTML = `<img src="${e.target.result}" alt="Avatar Preview" class="avatar-image">`;
          }
          
          reader.readAsDataURL(this.files[0]);
        }
      });
    }
    
    saveAvatarBtn.addEventListener('click', function() {
      const formData = new FormData(document.getElementById('avatarForm'));
      
      fetch('${pageContext.request.contextPath}/user/update-avatar', {
        method: 'POST',
        body: formData
      })
      .then(response => response.json())
      .then(data => {
        if (data.success) {
          window.location.reload();
        } else {
          alert(data.message || 'Failed to update avatar');
        }
      })
      .catch(error => {
        console.error('Error:', error);
        alert('An error occurred while updating the avatar');
      });
    });
    
    removeAvatarBtn.addEventListener('click', function() {
      fetch('${pageContext.request.contextPath}/user/remove-avatar', {
        method: 'POST'
      })
      .then(response => response.json())
      .then(data => {
        if (data.success) {
          window.location.reload();
        } else {
          alert(data.message || 'Failed to remove avatar');
        }
      })
      .catch(error => {
        console.error('Error:', error);
        alert('An error occurred while removing the avatar');
      });
    });
  });
</script>