<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="common/header.jsp">
  <jsp:param name="pageTitle" value="Search Results" />
</jsp:include>
<jsp:include page="common/navigation.jsp" />

<div class="container-fluid">
  <div class="row">
    <jsp:include page="common/sidebar.jsp" />
    
    <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <h1 class="h2">Search Results</h1>
      </div>
      
      <!-- Search form -->
      <div class="search-container mb-4">
        <form action="${pageContext.request.contextPath}/search" method="get" class="search-form">
          <div class="input-group">
            <input type="text" class="form-control" name="query" value="${query}" placeholder="Search for tasks, projects, files, comments..." aria-label="Search">
            <select class="form-select" name="type" style="max-width: 150px;">
              <option value="all" ${type == 'all' ? 'selected' : ''}>All</option>
              <option value="task" ${type == 'task' ? 'selected' : ''}>Tasks</option>
              <option value="project" ${type == 'project' ? 'selected' : ''}>Projects</option>
              <option value="file" ${type == 'file' ? 'selected' : ''}>Files</option>
              <option value="comment" ${type == 'comment' ? 'selected' : ''}>Comments</option>
              <option value="user" ${type == 'user' ? 'selected' : ''}>Users</option>
            </select>
            <button type="submit" class="btn btn-primary">
              <i class="bi bi-search"></i> Search
            </button>
          </div>
          
          <div class="search-filters mt-2">
            <div class="form-check form-check-inline">
              <input class="form-check-input" type="checkbox" id="myItemsOnly" name="myItemsOnly" value="true" ${myItemsOnly ? 'checked' : ''}>
              <label class="form-check-label" for="myItemsOnly">My items only</label>
            </div>
            
            <div class="form-check form-check-inline">
              <input class="form-check-input" type="checkbox" id="includeArchived" name="includeArchived" value="true" ${includeArchived ? 'checked' : ''}>
              <label class="form-check-label" for="includeArchived">Include archived</label>
            </div>
            
            <div class="form-check form-check-inline">
              <input class="form-check-input" type="checkbox" id="includeCompleted" name="includeCompleted" value="true" ${includeCompleted ? 'checked' : ''}>
              <label class="form-check-label" for="includeCompleted">Include completed</label>
            </div>
          </div>
        </form>
      </div>
      
      <c:if test="${empty query}">
        <div class="empty-state text-center py-5">
          <i class="bi bi-search display-4"></i>
          <h3 class="mt-3">Start Searching</h3>
          <p class="text-muted">Enter a search term to find tasks, projects, files, or comments.</p>
        </div>
      </c:if>
      
      <c:if test="${not empty query && empty results}">
        <div class="empty-state text-center py-5">
          <i class="bi bi-search display-4"></i>
          <h3 class="mt-3">No Results Found</h3>
          <p class="text-muted">We couldn't find any results for "${query}".</p>
        </div>
      </c:if>
      
      <c:if test="${not empty results}">
        <div class="search-stats mb-3">
          <p>Found ${totalResults} results for <strong>"${query}"</strong></p>
        </div>
        
        <div class="search-results">
          <!-- Tasks -->
          <c:if test="${not empty results.tasks}">
            <div class="search-category mb-4">
              <h2 class="h5 mb-3">Tasks (${fn:length(results.tasks)})</h2>
              
              <div class="list-group">
                <c:forEach var="task" items="${results.tasks}">
                  <a href="${pageContext.request.contextPath}/task?id=${task.id}" class="list-group-item list-group-item-action">
                    <div class="d-flex w-100 justify-content-between">
                      <h5 class="mb-1">
                        <span class="task-priority-indicator priority-${task.priority.toLowerCase()}"></span>
                        ${task.title}
                      </h5>
                      <small>
                        <span class="badge bg-${task.status == 'COMPLETED' ? 'success' : (task.status == 'IN_PROGRESS' ? 'primary' : 'secondary')}">
                          ${task.status.replace('_', ' ')}
                        </span>
                      </small>
                    </div>
                    <p class="mb-1">${fn:substring(task.description, 0, 200)}${fn:length(task.description) > 200 ? '...' : ''}</p>
                    <small>
                      Project: ${task.projectName} | Due: <fmt:formatDate value="${task.dueDate}" pattern="MMM d, yyyy" />
                    </small>
                  </a>
                </c:forEach>
              </div>
            </div>
          </c:if>
          
          <!-- Projects -->
          <c:if test="${not empty results.projects}">
            <div class="search-category mb-4">
              <h2 class="h5 mb-3">Projects (${fn:length(results.projects)})</h2>
              
              <div class="row row-cols-1 row-cols-md-2 row-cols-xl-3 g-4">
                <c:forEach var="project" items="${results.projects}">
                  <div class="col">
                    <div class="card h-100">
                      <div class="card-body">
                        <h5 class="card-title">
                          <a href="${pageContext.request.contextPath}/project?id=${project.id}" class="text-decoration-none">
                            ${project.name}
                          </a>
                        </h5>
                        <p class="card-text">${fn:substring(project.description, 0, 100)}${fn:length(project.description) > 100 ? '...' : ''}</p>
                      </div>
                      <div class="card-footer">
                        <small class="text-muted">
                          <span class="badge bg-${project.status == 'ACTIVE' ? 'success' : (project.status == 'ON_HOLD' ? 'warning' : 'secondary')}">
                            ${project.status.replace('_', ' ')}
                          </span>
                          | Tasks: ${project.taskCount} | Team: ${project.teamSize}
                        </small>
                      </div>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </div>
          </c:if>
          
          <!-- Files -->
          <c:if test="${not empty results.files}">
            <div class="search-category mb-4">
              <h2 class="h5 mb-3">Files (${fn:length(results.files)})</h2>
              
              <div class="list-group">
                <c:forEach var="file" items="${results.files}">
                  <a href="${pageContext.request.contextPath}/files/download?path=${file.path}" class="list-group-item list-group-item-action">
                    <div class="d-flex w-100 justify-content-between">
                      <h5 class="mb-1">
                        <i class="bi ${file.iconClass} me-2"></i>
                        ${file.name}
                      </h5>
                      <small>
                        <fmt:formatDate value="${file.modifiedDate}" pattern="MMM d, yyyy" />
                      </small>
                    </div>
                    <p class="mb-1">Type: ${file.type} | Size: ${file.formattedSize}</p>
                    <small>
                      Uploaded by: ${file.uploaderName} | Path: ${file.path}
                    </small>
                  </a>
                </c:forEach>
              </div>
            </div>
          </c:if>
          
          <!-- Comments -->
          <c:if test="${not empty results.comments}">
            <div class="search-category mb-4">
              <h2 class="h5 mb-3">Comments (${fn:length(results.comments)})</h2>
              
              <div class="list-group">
                <c:forEach var="comment" items="${results.comments}">
                  <a href="${pageContext.request.contextPath}/${comment.entityType.toLowerCase()}?id=${comment.entityId}&comment=${comment.id}" class="list-group-item list-group-item-action">
                    <div class="d-flex w-100 justify-content-between">
                      <h5 class="mb-1">Comment on ${comment.entityType}: ${comment.entityTitle}</h5>
                      <small>
                        <fmt:formatDate value="${comment.createdAt}" pattern="MMM d, yyyy" />
                      </small>
                    </div>
                    <p class="mb-1">${fn:substring(comment.content, 0, 200)}${fn:length(comment.content) > 200 ? '...' : ''}</p>
                    <small>
                      By: ${comment.authorName}
                    </small>
                  </a>
                </c:forEach>
              </div>
            </div>
          </c:if>
          
          <!-- Users -->
          <c:if test="${not empty results.users}">
            <div class="search-category mb-4">
              <h2 class="h5 mb-3">Users (${fn:length(results.users)})</h2>
              
              <div class="row row-cols-1 row-cols-md-2 row-cols-xl-3 g-4">
                <c:forEach var="user" items="${results.users}">
                  <div class="col">
                    <div class="card h-100 user-card">
                      <div class="card-body">
                        <div class="d-flex">
                          <div class="user-avatar me-3">
                            <c:choose>
                              <c:when test="${not empty user.avatarUrl}">
                                <img src="${user.avatarUrl}" alt="${user.fullName}" class="avatar-image">
                              </c:when>
                              <c:otherwise>
                                <div class="avatar-placeholder">
                                  ${user.initials}
                                </div>
                              </c:otherwise>
                            </c:choose>
                          </div>
                          <div>
                            <h5 class="card-title mb-1">${user.fullName}</h5>
                            <p class="card-text text-muted mb-2">${user.jobTitle}</p>
                            <p class="card-text"><small class="text-muted"><i class="bi bi-envelope"></i> ${user.email}</small></p>
                          </div>
                        </div>
                      </div>
                      <div class="card-footer">
                        <a href="${pageContext.request.contextPath}/user?id=${user.id}" class="btn btn-sm btn-outline-secondary">View Profile</a>
                        <c:if test="${currentUser.id != user.id}">
                          <a href="#" class="btn btn-sm btn-outline-primary send-message-btn" data-user-id="${user.id}" data-user-name="${user.fullName}">
                            <i class="bi bi-chat-dots"></i> Message
                          </a>
                        </c:if>
                      </div>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </div>
          </c:if>
        </div>
        
        <!-- Pagination -->
        <c:if test="${totalPages > 1}">
          <div class="mt-4">
            <nav aria-label="Search result pagination">
              <ul class="pagination justify-content-center">
                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                  <a class="page-link" href="${pageContext.request.contextPath}/search?query=${query}&type=${type}&page=${currentPage - 1}${myItemsOnly ? '&myItemsOnly=true' : ''}${includeArchived ? '&includeArchived=true' : ''}${includeCompleted ? '&includeCompleted=true' : ''}" aria-label="Previous">
                    <span aria-hidden="true">&laquo;</span>
                  </a>
                </li>
                <c:forEach begin="1" end="${totalPages}" var="pageNum">
                  <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                    <a class="page-link" href="${pageContext.request.contextPath}/search?query=${query}&type=${type}&page=${pageNum}${myItemsOnly ? '&myItemsOnly=true' : ''}${includeArchived ? '&includeArchived=true' : ''}${includeCompleted ? '&includeCompleted=true' : ''}">${pageNum}</a>
                  </li>
                </c:forEach>
                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                  <a class="page-link" href="${pageContext.request.contextPath}/search?query=${query}&type=${type}&page=${currentPage + 1}${myItemsOnly ? '&myItemsOnly=true' : ''}${includeArchived ? '&includeArchived=true' : ''}${includeCompleted ? '&includeCompleted=true' : ''}" aria-label="Next">
                    <span aria-hidden="true">&raquo;</span>
                  </a>
                </li>
              </ul>
            </nav>
          </div>
        </c:if>
      </c:if>
    </main>
  </div>
</div>

<!-- Send Message Modal -->
<div class="modal fade" id="messageModal" tabindex="-1" aria-labelledby="messageModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="messageModalLabel">Send Message</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="messageForm" action="${pageContext.request.contextPath}/chat/send-direct" method="post">
        <div class="modal-body">
          <input type="hidden" id="recipientId" name="recipientId" value="">
          <div class="mb-3">
            <label class="form-label">To</label>
            <input type="text" class="form-control" id="recipientName" readonly>
          </div>
          <div class="mb-3">
            <label for="messageContent" class="form-label">Message</label>
            <textarea class="form-control" id="messageContent" name="content" rows="4" required></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary">Send</button>
        </div>
      </form>
    </div>
  </div>
</div>

<jsp:include page="common/footer.jsp" />

<script>
  document.addEventListener('DOMContentLoaded', function() {
    // Highlight search terms in results
    const searchQuery = '${query}';
    if (searchQuery) {
      const terms = searchQuery.split(' ').filter(term => term.length > 2);
      
      if (terms.length) {
        const elements = document.querySelectorAll('.list-group-item p, .card-text');
        
        elements.forEach(element => {
          let html = element.innerHTML;
          
          terms.forEach(term => {
            const regex = new RegExp('(' + term + ')', 'gi');
            html = html.replace(regex, '<mark>$1</mark>');
          });
          
          element.innerHTML = html;
        });
      }
    }
    
    // Send message modal
    const messageModal = new bootstrap.Modal(document.getElementById('messageModal'));
    const recipientId = document.getElementById('recipientId');
    const recipientName = document.getElementById('recipientName');
    
    document.querySelectorAll('.send-message-btn').forEach(btn => {
      btn.addEventListener('click', function(e) {
        e.preventDefault();
        
        recipientId.value = this.dataset.userId;
        recipientName.value = this.dataset.userName;
        
        messageModal.show();
      });
    });
    
    // Handle message form submission
    document.getElementById('messageForm').addEventListener('submit', function(e) {
      e.preventDefault();
      
      const formData = new FormData(this);
      const params = new URLSearchParams();
      
      for (const pair of formData) {
        params.append(pair[0], pair[1]);
      }
      
      fetch(this.action, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: params
      })
      .then(response => response.json())
      .then(data => {
        if (data.success) {
          messageModal.hide();
          alert('Message sent successfully.');
          document.getElementById('messageContent').value = '';
        } else {
          alert(data.message || 'Failed to send message.');
        }
      })
      .catch(error => {
        console.error('Error:', error);
        alert('An error occurred while sending the message.');
      });
    });
  });
</script>