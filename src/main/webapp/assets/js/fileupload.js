/**
 * File Upload Module for Real-Time Task Application
 * Handles file uploads and management functionalities
 */
document.addEventListener("DOMContentLoaded", function () {
  "use strict";

  // File Upload Module
  const FileUpload = {
    // Config
    maxFileSize: 50 * 1024 * 1024, // 50MB
    allowedFileTypes: [
      "image/jpeg",
      "image/png",
      "image/gif",
      "application/pdf",
      "application/msword",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "application/vnd.ms-excel",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "application/vnd.ms-powerpoint",
      "application/vnd.openxmlformats-officedocument.presentationml.presentation",
      "text/plain",
      "text/csv",
      "application/zip",
      "application/x-rar-compressed",
    ],
    uploadQueue: [],
    activeUploads: 0,
    maxConcurrentUploads: 3,

    // Initialize file upload functionality
    init: function () {
      console.log("File Upload module initialized");
      this.setupEventListeners();
      this.setupDropzone();
    },

    // Set up event listeners for file upload components
    setupEventListeners: function () {
      // File input change event
      const fileInput = document.getElementById("file-input");
      if (fileInput) {
        fileInput.addEventListener("change", (event) => {
          this.handleFileSelection(event.target.files);
        });
      }

      // Upload form submit event
      const uploadForm = document.getElementById("upload-form");
      if (uploadForm) {
        uploadForm.addEventListener("submit", (event) => {
          event.preventDefault();
          this.processQueue();
        });
      }

      // Delete file buttons
      document.addEventListener("click", (event) => {
        if (event.target.classList.contains("delete-file-btn")) {
          this.deleteFile(event.target.dataset.fileId);
        }
      });
    },

    // Set up the file dropzone for drag and drop uploads
    setupDropzone: function () {
      const dropzone = document.getElementById("dropzone");
      if (!dropzone) return;

      // Highlight dropzone on drag over
      dropzone.addEventListener("dragover", (event) => {
        event.preventDefault();
        event.stopPropagation();
        dropzone.classList.add("highlight");
      });

      // Remove highlight on drag leave
      dropzone.addEventListener("dragleave", (event) => {
        event.preventDefault();
        event.stopPropagation();
        dropzone.classList.remove("highlight");
      });

      // Handle drop event
      dropzone.addEventListener("drop", (event) => {
        event.preventDefault();
        event.stopPropagation();
        dropzone.classList.remove("highlight");

        const files = event.dataTransfer.files;
        this.handleFileSelection(files);
      });

      // Handle click to select files
      dropzone.addEventListener("click", () => {
        document.getElementById("file-input").click();
      });
    },

    // Handle selected files from input or drop
    handleFileSelection: function (files) {
      const fileList = document.getElementById("file-list");
      if (!fileList) return;

      // Convert FileList to array and filter valid files
      Array.from(files).forEach((file) => {
        // Validate file
        if (!this.validateFile(file)) return;

        // Create a unique identifier for the file
        const fileId = `file-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

        // Add to upload queue
        this.uploadQueue.push({
          id: fileId,
          file: file,
          status: "pending",
        });

        // Create file item in the UI
        const fileItem = document.createElement("div");
        fileItem.id = fileId;
        fileItem.className = "file-item";

        // Get file icon based on type
        const fileIcon = this.getFileIcon(file.type);

        // Format file size
        const fileSize = this.formatFileSize(file.size);

        fileItem.innerHTML = `
                    <div class="file-icon">${fileIcon}</div>
                    <div class="file-info">
                        <div class="file-name">${file.name}</div>
                        <div class="file-meta">
                            <span class="file-type">${file.type || "Unknown type"}</span>
                            <span class="file-size">${fileSize}</span>
                        </div>
                        <div class="progress-bar">
                            <div class="progress" style="width: 0%"></div>
                        </div>
                    </div>
                    <div class="file-actions">
                        <button type="button" class="cancel-upload-btn" data-file-id="${fileId}">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                `;

        fileList.appendChild(fileItem);

        // Add event listener to cancel button
        fileItem.querySelector(".cancel-upload-btn").addEventListener("click", () => {
          this.cancelUpload(fileId);
        });
      });

      // Show upload button if files are added
      if (this.uploadQueue.length > 0) {
        document.getElementById("upload-btn")?.classList.remove("hidden");
      }
    },

    // Process the upload queue
    processQueue: function () {
      if (this.uploadQueue.length === 0 || this.activeUploads >= this.maxConcurrentUploads) return;

      // Get pending uploads up to the max concurrent limit
      const pendingUploads = this.uploadQueue.filter((item) => item.status === "pending");
      const uploadsToProcess = pendingUploads.slice(0, this.maxConcurrentUploads - this.activeUploads);

      uploadsToProcess.forEach((uploadItem) => {
        uploadItem.status = "uploading";
        this.activeUploads++;
        this.uploadFile(uploadItem);
      });
    },

    // Upload a single file with progress tracking
    uploadFile: function (uploadItem) {
      const formData = new FormData();
      formData.append("file", uploadItem.file);

      // Add metadata if available
      const projectId = document.getElementById("project-id")?.value;
      const taskId = document.getElementById("task-id")?.value;
      const description = document.getElementById("file-description")?.value;

      if (projectId) formData.append("projectId", projectId);
      if (taskId) formData.append("taskId", taskId);
      if (description) formData.append("description", description);

      // Update UI to show upload started
      const fileElement = document.getElementById(uploadItem.id);
      if (fileElement) {
        fileElement.classList.add("uploading");
      }

      // Create and configure XMLHttpRequest
      const xhr = new XMLHttpRequest();
      uploadItem.xhr = xhr;

      // Track upload progress
      xhr.upload.addEventListener("progress", (event) => {
        if (event.lengthComputable) {
          const percentComplete = Math.round((event.loaded * 100) / event.total);
          this.updateProgress(uploadItem.id, percentComplete);
        }
      });

      // Handle successful upload
      xhr.addEventListener("load", () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          this.handleSuccessfulUpload(uploadItem, JSON.parse(xhr.responseText));
        } else {
          this.handleFailedUpload(uploadItem, xhr.statusText);
        }

        // Decrement active uploads and process next in queue
        this.activeUploads--;
        this.processQueue();
      });

      // Handle upload errors
      xhr.addEventListener("error", () => {
        this.handleFailedUpload(uploadItem, "Network error");
        this.activeUploads--;
        this.processQueue();
      });

      // Handle upload abort
      xhr.addEventListener("abort", () => {
        this.handleCancelledUpload(uploadItem);
        this.activeUploads--;
        this.processQueue();
      });

      // Send the request
      xhr.open("POST", "/api/files/upload", true);
      xhr.send(formData);
    },

    // Update progress in the UI
    updateProgress: function (fileId, percent) {
      const fileElement = document.getElementById(fileId);
      if (!fileElement) return;

      const progressBar = fileElement.querySelector(".progress");
      if (progressBar) {
        progressBar.style.width = `${percent}%`;
      }
    },

    // Handle successful file upload
    handleSuccessfulUpload: function (uploadItem, response) {
      const fileElement = document.getElementById(uploadItem.id);
      if (!fileElement) return;

      // Update UI
      fileElement.classList.remove("uploading");
      fileElement.classList.add("upload-success");

      // Update file actions
      const actionsContainer = fileElement.querySelector(".file-actions");
      if (actionsContainer) {
        actionsContainer.innerHTML = `
                    <button type="button" class="download-file-btn" data-file-id="${response.id}" title="Download">
                        <i class="fas fa-download"></i>
                    </button>
                    <button type="button" class="delete-file-btn" data-file-id="${response.id}" title="Delete">
                        <i class="fas fa-trash"></i>
                    </button>
                `;

        // Add event listeners to new buttons
        actionsContainer.querySelector(".download-file-btn").addEventListener("click", () => {
          window.location.href = `/api/files/download/${response.id}`;
        });

        actionsContainer.querySelector(".delete-file-btn").addEventListener("click", () => {
          this.deleteFile(response.id);
        });
      }

      // Remove from upload queue
      this.uploadQueue = this.uploadQueue.filter((item) => item.id !== uploadItem.id);

      // Show notification
      this.showNotification("success", `${uploadItem.file.name} uploaded successfully!`);

      // Hide upload button if queue is empty
      if (this.uploadQueue.length === 0) {
        document.getElementById("upload-btn")?.classList.add("hidden");
      }
    },

    // Handle failed file upload
    handleFailedUpload: function (uploadItem, errorMessage) {
      const fileElement = document.getElementById(uploadItem.id);
      if (!fileElement) return;

      // Update UI
      fileElement.classList.remove("uploading");
      fileElement.classList.add("upload-failed");

      // Add error message
      const fileInfo = fileElement.querySelector(".file-info");
      if (fileInfo) {
        const errorDiv = document.createElement("div");
        errorDiv.className = "file-error";
        errorDiv.textContent = `Error: ${errorMessage}`;
        fileInfo.appendChild(errorDiv);
      }

      // Update file actions
      const actionsContainer = fileElement.querySelector(".file-actions");
      if (actionsContainer) {
        actionsContainer.innerHTML = `
                    <button type="button" class="retry-upload-btn" data-file-id="${uploadItem.id}" title="Retry">
                        <i class="fas fa-redo"></i>
                    </button>
                    <button type="button" class="remove-file-btn" data-file-id="${uploadItem.id}" title="Remove">
                        <i class="fas fa-times"></i>
                    </button>
                `;

        // Add event listeners to new buttons
        actionsContainer.querySelector(".retry-upload-btn").addEventListener("click", () => {
          this.retryUpload(uploadItem.id);
        });

        actionsContainer.querySelector(".remove-file-btn").addEventListener("click", () => {
          this.removeFileFromList(uploadItem.id);
        });
      }

      // Update status in queue
      const queueItem = this.uploadQueue.find((item) => item.id === uploadItem.id);
      if (queueItem) {
        queueItem.status = "failed";
      }

      // Show notification
      this.showNotification("error", `Failed to upload ${uploadItem.file.name}`);
    },

    // Handle cancelled file upload
    handleCancelledUpload: function (uploadItem) {
      this.removeFileFromList(uploadItem.id);

      // Remove from upload queue
      this.uploadQueue = this.uploadQueue.filter((item) => item.id !== uploadItem.id);

      // Hide upload button if queue is empty
      if (this.uploadQueue.length === 0) {
        document.getElementById("upload-btn")?.classList.add("hidden");
      }
    },

    // Cancel an ongoing upload
    cancelUpload: function (fileId) {
      const uploadItem = this.uploadQueue.find((item) => item.id === fileId);

      if (uploadItem && uploadItem.status === "uploading" && uploadItem.xhr) {
        uploadItem.xhr.abort();
        this.handleCancelledUpload(uploadItem);
      } else {
        // For pending uploads, just remove from queue
        this.removeFileFromList(fileId);
        this.uploadQueue = this.uploadQueue.filter((item) => item.id !== fileId);

        // Hide upload button if queue is empty
        if (this.uploadQueue.length === 0) {
          document.getElementById("upload-btn")?.classList.add("hidden");
        }
      }
    },

    // Retry a failed upload
    retryUpload: function (fileId) {
      const uploadItem = this.uploadQueue.find((item) => item.id === fileId);

      if (uploadItem && uploadItem.status === "failed") {
        // Reset UI
        const fileElement = document.getElementById(fileId);
        if (fileElement) {
          fileElement.classList.remove("upload-failed");

          // Remove error message
          fileElement.querySelector(".file-error")?.remove();

          // Reset progress bar
          const progressBar = fileElement.querySelector(".progress");
          if (progressBar) {
            progressBar.style.width = "0%";
          }

          // Update actions
          const actionsContainer = fileElement.querySelector(".file-actions");
          if (actionsContainer) {
            actionsContainer.innerHTML = `
                            <button type="button" class="cancel-upload-btn" data-file-id="${fileId}">
                                <i class="fas fa-times"></i>
                            </button>
                        `;

            // Add event listener to cancel button
            actionsContainer.querySelector(".cancel-upload-btn").addEventListener("click", () => {
              this.cancelUpload(fileId);
            });
          }
        }

        // Update status in queue
        uploadItem.status = "pending";

        // Process queue
        this.processQueue();
      }
    },

    // Remove file from the UI list
    removeFileFromList: function (fileId) {
      const fileElement = document.getElementById(fileId);
      if (fileElement) {
        fileElement.remove();
      }
    },

    // Delete a file from the server
    deleteFile: function (fileId) {
      if (!confirm("Are you sure you want to delete this file? This action cannot be undone.")) {
        return;
      }

      fetch(`/api/files/${fileId}`, {
        method: "DELETE",
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("File deletion failed");
          }
          return response.json();
        })
        .then(() => {
          // Remove from UI
          const fileElement = document.getElementById(`file-${fileId}`) || document.querySelector(`[data-file-id="${fileId}"]`).closest(".file-item");
          if (fileElement) {
            fileElement.remove();
          }

          this.showNotification("success", "File deleted successfully");
        })
        .catch((error) => {
          console.error("Error deleting file:", error);
          this.showNotification("error", "Failed to delete file");
        });
    },

    // Validate file size and type
    validateFile: function (file) {
      // Check file size
      if (file.size > this.maxFileSize) {
        this.showNotification("error", `${file.name} exceeds the maximum file size (50MB)`);
        return false;
      }

      // Check file type
      if (!this.allowedFileTypes.includes(file.type)) {
        this.showNotification("error", `${file.name} has an unsupported file type`);
        return false;
      }

      return true;
    },

    // Format file size for display
    formatFileSize: function (bytes) {
      if (bytes === 0) return "0 Bytes";

      const k = 1024;
      const sizes = ["Bytes", "KB", "MB", "GB", "TB"];
      const i = Math.floor(Math.log(bytes) / Math.log(k));

      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
    },

    // Get appropriate icon for file type
    getFileIcon: function (fileType) {
      if (fileType.includes("image")) {
        return '<i class="fas fa-file-image"></i>';
      } else if (fileType.includes("pdf")) {
        return '<i class="fas fa-file-pdf"></i>';
      } else if (fileType.includes("word") || fileType.includes("document")) {
        return '<i class="fas fa-file-word"></i>';
      } else if (fileType.includes("excel") || fileType.includes("sheet")) {
        return '<i class="fas fa-file-excel"></i>';
      } else if (fileType.includes("powerpoint") || fileType.includes("presentation")) {
        return '<i class="fas fa-file-powerpoint"></i>';
      } else if (fileType.includes("text")) {
        return '<i class="fas fa-file-alt"></i>';
      } else if (fileType.includes("zip") || fileType.includes("compressed")) {
        return '<i class="fas fa-file-archive"></i>';
      } else {
        return '<i class="fas fa-file"></i>';
      }
    },

    // Show notification to the user
    showNotification: function (type, message) {
      const notificationsContainer = document.getElementById("notifications-container");
      if (!notificationsContainer) {
        // Create container if it doesn't exist
        const newContainer = document.createElement("div");
        newContainer.id = "notifications-container";
        document.body.appendChild(newContainer);

        // Use the new container
        this.showNotification(type, message);
        return;
      }

      const notification = document.createElement("div");
      notification.className = `notification notification-${type}`;
      notification.innerHTML = `
                <div class="notification-icon">
                    <i class="fas ${type === "success" ? "fa-check-circle" : "fa-exclamation-circle"}"></i>
                </div>
                <div class="notification-message">${message}</div>
                <button class="notification-close">
                    <i class="fas fa-times"></i>
                </button>
            `;

      notificationsContainer.appendChild(notification);

      // Add close button functionality
      notification.querySelector(".notification-close").addEventListener("click", () => {
        notification.classList.add("notification-hiding");
        setTimeout(() => {
          notification.remove();
        }, 300);
      });

      // Auto remove after 5 seconds
      setTimeout(() => {
        notification.classList.add("notification-hiding");
        setTimeout(() => {
          notification.remove();
        }, 300);
      }, 5000);

      // Animate in
      setTimeout(() => {
        notification.classList.add("notification-visible");
      }, 10);
    },
  };

  // Initialize file upload module
  FileUpload.init();
});
