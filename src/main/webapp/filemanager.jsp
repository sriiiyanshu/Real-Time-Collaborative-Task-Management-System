<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="common/header.jsp">
  <jsp:param name="pageTitle" value="File Manager" />
</jsp:include>
<jsp:include page="common/navigation.jsp" />

<div class="container-fluid">
  <div class="row">
    <jsp:include page="common/sidebar.jsp" />
    
    <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <h1 class="h2">File Manager</h1>
        <div class="btn-toolbar mb-2 mb-md-0">
          <div class="btn-group me-2">
            <button type="button" class="btn btn-sm btn-outline-secondary" id="uploadFileBtn">
              <i class="bi bi-upload"></i> Upload File
            </button>
            <button type="button" class="btn btn-sm btn-outline-secondary" id="createFolderBtn">
              <i class="bi bi-folder-plus"></i> New Folder
            </button>
          </div>
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
      
      <!-- Breadcrumb navigation -->
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <a href="${pageContext.request.contextPath}/filemanager"><i class="bi bi-house"></i> Root</a>
          </li>
          <c:forEach var="pathItem" items="${breadcrumbs}">
            <li class="breadcrumb-item ${pathItem.active ? 'active' : ''}">
              <c:choose>
                <c:when test="${pathItem.active}">
                  ${pathItem.name}
                </c:when>
                <c:otherwise>
                  <a href="${pageContext.request.contextPath}/filemanager?path=${pathItem.path}">${pathItem.name}</a>
                </c:otherwise>
              </c:choose>
            </li>
          </c:forEach>
        </ol>
      </nav>
      
      <!-- File listing table -->
      <div class="table-responsive">
        <table class="table table-striped table-sm file-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Type</th>
              <th>Size</th>
              <th>Modified</th>
              <th>Owner</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <!-- Parent directory -->
            <c:if test="${not empty parentPath}">
              <tr class="folder-row">
                <td>
                  <a href="${pageContext.request.contextPath}/filemanager?path=${parentPath}" class="folder-link">
                    <i class="bi bi-arrow-up-circle"></i> ..
                  </a>
                </td>
                <td>Folder</td>
                <td>-</td>
                <td>-</td>
                <td>-</td>
                <td>-</td>
              </tr>
            </c:if>
            
            <!-- Folders first -->
            <c:forEach var="folder" items="${folders}">
              <tr class="folder-row">
                <td>
                  <a href="${pageContext.request.contextPath}/filemanager?path=${currentPath}${folder.name}" class="folder-link">
                    <i class="bi bi-folder"></i> ${folder.name}
                  </a>
                </td>
                <td>Folder</td>
                <td>-</td>
                <td><fmt:formatDate value="${folder.modifiedDate}" pattern="yyyy-MM-dd HH:mm" /></td>
                <td>${folder.owner}</td>
                <td>
                  <div class="dropdown">
                    <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                      Actions
                    </button>
                    <ul class="dropdown-menu">
                      <li>
                        <a class="dropdown-item rename-folder" href="#" data-folder-name="${folder.name}" data-folder-path="${currentPath}${folder.name}">
                          <i class="bi bi-pencil"></i> Rename
                        </a>
                      </li>
                      <li>
                        <a class="dropdown-item share-folder" href="#" data-folder-name="${folder.name}" data-folder-path="${currentPath}${folder.name}">
                          <i class="bi bi-share"></i> Share
                        </a>
                      </li>
                      <li><hr class="dropdown-divider"></li>
                      <li>
                        <a class="dropdown-item delete-folder text-danger" href="#" data-folder-name="${folder.name}" data-folder-path="${currentPath}${folder.name}">
                          <i class="bi bi-trash"></i> Delete
                        </a>
                      </li>
                    </ul>
                  </div>
                </td>
              </tr>
            </c:forEach>
            
            <!-- Files -->
            <c:forEach var="file" items="${files}">
              <tr class="file-row">
                <td>
                  <a href="${pageContext.request.contextPath}/files/download?path=${currentPath}${file.name}" class="file-link">
                    <i class="bi ${file.iconClass}"></i> ${file.name}
                  </a>
                </td>
                <td>${file.type}</td>
                <td>${file.formattedSize}</td>
                <td><fmt:formatDate value="${file.modifiedDate}" pattern="yyyy-MM-dd HH:mm" /></td>
                <td>${file.owner}</td>
                <td>
                  <div class="dropdown">
                    <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                      Actions
                    </button>
                    <ul class="dropdown-menu">
                      <li>
                        <a class="dropdown-item" href="${pageContext.request.contextPath}/files/download?path=${currentPath}${file.name}">
                          <i class="bi bi-download"></i> Download
                        </a>
                      </li>
                      <li>
                        <a class="dropdown-item rename-file" href="#" data-file-name="${file.name}" data-file-path="${currentPath}${file.name}">
                          <i class="bi bi-pencil"></i> Rename
                        </a>
                      </li>
                      <li>
                        <a class="dropdown-item share-file" href="#" data-file-name="${file.name}" data-file-path="${currentPath}${file.name}">
                          <i class="bi bi-share"></i> Share
                        </a>
                      </li>
                      <li><hr class="dropdown-divider"></li>
                      <li>
                        <a class="dropdown-item delete-file text-danger" href="#" data-file-name="${file.name}" data-file-path="${currentPath}${file.name}">
                          <i class="bi bi-trash"></i> Delete
                        </a>
                      </li>
                    </ul>
                  </div>
                </td>
              </tr>
            </c:forEach>
            
            <!-- Empty state -->
            <c:if test="${empty folders && empty files}">
              <tr>
                <td colspan="6" class="text-center py-4">
                  <div class="empty-state">
                    <i class="bi bi-folder-x display-4"></i>
                    <p>This folder is empty</p>
                  </div>
                </td>
              </tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </main>
  </div>
</div>

<!-- Upload File Modal -->
<div class="modal fade" id="uploadFileModal" tabindex="-1" aria-labelledby="uploadFileModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="uploadFileModalLabel">Upload File</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="uploadForm" action="${pageContext.request.contextPath}/files/upload" method="post" enctype="multipart/form-data">
        <div class="modal-body">
          <input type="hidden" name="currentPath" value="${currentPath}">
          <div class="mb-3">
            <label for="fileInput" class="form-label">Select File</label>
            <input class="form-control" type="file" id="fileInput" name="file" required>
          </div>
          <div class="progress" style="display: none;">
            <div class="progress-bar" role="progressbar" style="width: 0%;" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">0%</div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary">Upload</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- New Folder Modal -->
<div class="modal fade" id="newFolderModal" tabindex="-1" aria-labelledby="newFolderModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="newFolderModalLabel">Create New Folder</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="newFolderForm" action="${pageContext.request.contextPath}/files/create-folder" method="post">
        <div class="modal-body">
          <input type="hidden" name="currentPath" value="${currentPath}">
          <div class="mb-3">
            <label for="folderName" class="form-label">Folder Name</label>
            <input type="text" class="form-control" id="folderName" name="folderName" required>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary">Create</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Delete Confirmation Modal -->
<div class="modal fade" id="deleteConfirmationModal" tabindex="-1" aria-labelledby="deleteConfirmationModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteConfirmationModalLabel">Confirm Delete</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <p>Are you sure you want to delete <span id="deleteItemName"></span>?</p>
        <p class="text-danger"><strong>Warning:</strong> This action cannot be undone.</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
        <form id="deleteForm" action="${pageContext.request.contextPath}/files/delete" method="post">
          <input type="hidden" name="path" id="deleteItemPath" value="">
          <button type="submit" class="btn btn-danger">Delete</button>
        </form>
      </div>
    </div>
  </div>
</div>

<!-- Rename Modal -->
<div class="modal fade" id="renameModal" tabindex="-1" aria-labelledby="renameModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="renameModalLabel">Rename</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="renameForm" action="${pageContext.request.contextPath}/files/rename" method="post">
        <div class="modal-body">
          <input type="hidden" name="oldPath" id="oldPath" value="">
          <input type="hidden" name="currentPath" value="${currentPath}">
          <div class="mb-3">
            <label for="newName" class="form-label">New Name</label>
            <input type="text" class="form-control" id="newName" name="newName" required>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary">Rename</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Share Modal -->
<div class="modal fade" id="shareModal" tabindex="-1" aria-labelledby="shareModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="shareModalLabel">Share</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="shareItemPath" value="">
        <div class="mb-3">
          <label class="form-label">Share with Team Members</label>
          <select class="form-select" id="shareUsers" multiple>
            <c:forEach var="user" items="${teamMembers}">
              <option value="${user.id}">${user.name}</option>
            </c:forEach>
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label">Permission Level</label>
          <div class="form-check">
            <input class="form-check-input" type="radio" name="permission" id="permissionRead" value="READ" checked>
            <label class="form-check-label" for="permissionRead">
              Read Only
            </label>
          </div>
          <div class="form-check">
            <input class="form-check-input" type="radio" name="permission" id="permissionReadWrite" value="WRITE">
            <label class="form-check-label" for="permissionReadWrite">
              Read and Write
            </label>
          </div>
        </div>
        <div class="mb-3">
          <label class="form-label">Or get a shareable link</label>
          <div class="input-group">
            <input type="text" class="form-control" id="shareLink" readonly>
            <button class="btn btn-outline-secondary" type="button" id="copyShareLink">
              <i class="bi bi-clipboard"></i>
            </button>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
        <button type="button" class="btn btn-primary" id="shareSubmit">Share</button>
      </div>
    </div>
  </div>
</div>

<jsp:include page="common/footer.jsp" />

<script>
  document.addEventListener('DOMContentLoaded', function() {
    // Upload file button
    const uploadFileBtn = document.getElementById('uploadFileBtn');
    const uploadFileModal = new bootstrap.Modal(document.getElementById('uploadFileModal'));
    
    uploadFileBtn.addEventListener('click', function() {
      uploadFileModal.show();
    });
    
    // New folder button
    const createFolderBtn = document.getElementById('createFolderBtn');
    const newFolderModal = new bootstrap.Modal(document.getElementById('newFolderModal'));
    
    createFolderBtn.addEventListener('click', function() {
      newFolderModal.show();
    });
    
    // Delete file/folder
    const deleteConfirmationModal = new bootstrap.Modal(document.getElementById('deleteConfirmationModal'));
    const deleteItemName = document.getElementById('deleteItemName');
    const deleteItemPath = document.getElementById('deleteItemPath');
    
    document.querySelectorAll('.delete-file, .delete-folder').forEach(item => {
      item.addEventListener('click', function(e) {
        e.preventDefault();
        const name = this.dataset.fileName || this.dataset.folderName;
        const path = this.dataset.filePath || this.dataset.folderPath;
        
        deleteItemName.textContent = name;
        deleteItemPath.value = path;
        deleteConfirmationModal.show();
      });
    });
    
    // Rename file/folder
    const renameModal = new bootstrap.Modal(document.getElementById('renameModal'));
    const oldPath = document.getElementById('oldPath');
    const newName = document.getElementById('newName');
    
    document.querySelectorAll('.rename-file, .rename-folder').forEach(item => {
      item.addEventListener('click', function(e) {
        e.preventDefault();
        const name = this.dataset.fileName || this.dataset.folderName;
        const path = this.dataset.filePath || this.dataset.folderPath;
        
        oldPath.value = path;
        newName.value = name;
        renameModal.show();
      });
    });
    
    // Share file/folder
    const shareModal = new bootstrap.Modal(document.getElementById('shareModal'));
    const shareItemPath = document.getElementById('shareItemPath');
    const shareLink = document.getElementById('shareLink');
    
    document.querySelectorAll('.share-file, .share-folder').forEach(item => {
      item.addEventListener('click', function(e) {
        e.preventDefault();
        const path = this.dataset.filePath || this.dataset.folderPath;
        
        shareItemPath.value = path;
        shareLink.value = window.location.origin + '${pageContext.request.contextPath}/files/shared?token=' + btoa(path);
        shareModal.show();
      });
    });
    
    // Copy share link
    document.getElementById('copyShareLink').addEventListener('click', function() {
      shareLink.select();
      document.execCommand('copy');
      this.innerHTML = '<i class="bi bi-check"></i>';
      setTimeout(() => {
        this.innerHTML = '<i class="bi bi-clipboard"></i>';
      }, 2000);
    });
    
    // Share form submit
    document.getElementById('shareSubmit').addEventListener('click', function() {
      const path = shareItemPath.value;
      const users = Array.from(document.getElementById('shareUsers').selectedOptions).map(opt => opt.value);
      const permission = document.querySelector('input[name="permission"]:checked').value;
      
      fetch('${pageContext.request.contextPath}/files/share', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          path: path,
          users: users,
          permission: permission
        })
      })
      .then(response => response.json())
      .then(data => {
        if (data.success) {
          shareModal.hide();
          alert('File shared successfully');
        } else {
          alert(data.message || 'Failed to share file');
        }
      })
      .catch(error => {
        console.error('Error:', error);
        alert('An error occurred while sharing the file');
      });
    });
    
    // File upload with progress
    const uploadForm = document.getElementById('uploadForm');
    const progressBar = uploadForm.querySelector('.progress-bar');
    const progress = uploadForm.querySelector('.progress');
    
    uploadForm.addEventListener('submit', function(e) {
      e.preventDefault();
      
      const formData = new FormData(this);
      
      progress.style.display = 'block';
      
      const xhr = new XMLHttpRequest();
      xhr.open('POST', this.action, true);
      
      xhr.upload.addEventListener('progress', function(e) {
        if (e.lengthComputable) {
          const percentComplete = Math.round((e.loaded / e.total) * 100);
          progressBar.style.width = percentComplete + '%';
          progressBar.textContent = percentComplete + '%';
          progressBar.setAttribute('aria-valuenow', percentComplete);
        }
      });
      
      xhr.addEventListener('load', function() {
        if (xhr.status === 200) {
          window.location.reload();
        } else {
          alert('Upload failed');
          progress.style.display = 'none';
        }
      });
      
      xhr.send(formData);
    });
  });
</script>