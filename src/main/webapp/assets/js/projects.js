/**
 * Projects Module for Real-Time Task Application
 * Handles project management functionality
 */
document.addEventListener("DOMContentLoaded", function () {
  "use strict";

  // Projects Module
  const Projects = {
    // Store active project data
    activeProject: null,
    projectMembers: [],

    // Initialize projects functionality
    init: function () {
      console.log("Projects module initialized");
      this.loadActiveProject();
      this.setupEventListeners();
    },

    // Load active project data
    loadActiveProject: function () {
      const projectElement = document.getElementById("project-data");
      if (!projectElement) return;

      try {
        // Get project ID from DOM data attribute
        const projectId = projectElement.dataset.projectId;
        if (!projectId) return;

        // Fetch project details
        fetch(`/api/projects/${projectId}`)
          .then((response) => {
            if (!response.ok) {
              throw new Error("Failed to load project");
            }
            return response.json();
          })
          .then((data) => {
            this.activeProject = data;
            this.renderProjectDetails();
            this.loadProjectMembers();
            this.loadProjectTasks();
          })
          .catch((error) => {
            console.error("Error loading project:", error);
            this.showNotification("error", "Failed to load project details");
          });
      } catch (error) {
        console.error("Error parsing project data:", error);
      }
    },

    // Set up event listeners for project components
    setupEventListeners: function () {
      // Project tabs
      document.querySelectorAll(".project-tab").forEach((tab) => {
        tab.addEventListener("click", (event) => {
          event.preventDefault();
          this.switchTab(tab.dataset.tab);
        });
      });

      // Add task button
      const addTaskBtn = document.getElementById("add-task-btn");
      if (addTaskBtn) {
        addTaskBtn.addEventListener("click", () => {
          this.showAddTaskModal();
        });
      }

      // Add member button
      const addMemberBtn = document.getElementById("add-member-btn");
      if (addMemberBtn) {
        addMemberBtn.addEventListener("click", () => {
          this.showAddMemberModal();
        });
      }

      // Edit project button
      const editProjectBtn = document.getElementById("edit-project-btn");
      if (editProjectBtn) {
        editProjectBtn.addEventListener("click", () => {
          this.showEditProjectModal();
        });
      }

      // Delete project button
      const deleteProjectBtn = document.getElementById("delete-project-btn");
      if (deleteProjectBtn) {
        deleteProjectBtn.addEventListener("click", () => {
          this.confirmDeleteProject();
        });
      }

      // Search within project
      const projectSearchForm = document.getElementById("project-search-form");
      if (projectSearchForm) {
        projectSearchForm.addEventListener("submit", (event) => {
          event.preventDefault();
          const searchInput = document.getElementById("project-search-input");
          if (searchInput) {
            this.searchProject(searchInput.value);
          }
        });
      }

      // Task status filter
      document.querySelectorAll(".task-status-filter").forEach((filter) => {
        filter.addEventListener("click", () => {
          document.querySelectorAll(".task-status-filter").forEach((f) => {
            f.classList.remove("active");
          });
          filter.classList.add("active");
          this.filterTasks(filter.dataset.status);
        });
      });

      // Handle task drag and drop
      this.initializeTaskDragDrop();
    },

    // Switch between project tabs
    switchTab: function (tabId) {
      // Hide all tab content
      document.querySelectorAll(".tab-content").forEach((content) => {
        content.classList.remove("active");
      });

      // Show selected tab content
      const selectedTab = document.getElementById(`${tabId}-tab`);
      if (selectedTab) {
        selectedTab.classList.add("active");
      }

      // Update active tab indicator
      document.querySelectorAll(".project-tab").forEach((tab) => {
        tab.classList.remove("active");
      });

      document.querySelector(`.project-tab[data-tab="${tabId}"]`)?.classList.add("active");

      // Additional actions based on tab
      switch (tabId) {
        case "activity":
          this.loadProjectActivity();
          break;
        case "files":
          this.loadProjectFiles();
          break;
        case "discussion":
          this.loadProjectDiscussions();
          break;
        case "milestones":
          this.loadProjectMilestones();
          break;
      }
    },

    // Render project details
    renderProjectDetails: function () {
      if (!this.activeProject) return;

      // Update project header
      const projectTitle = document.getElementById("project-title");
      if (projectTitle) {
        projectTitle.textContent = this.activeProject.name;
      }

      // Update project description
      const projectDesc = document.getElementById("project-description");
      if (projectDesc) {
        projectDesc.textContent = this.activeProject.description;
      }

      // Update project progress
      this.updateProjectProgress();

      // Update project metadata
      const projectDates = document.getElementById("project-dates");
      if (projectDates) {
        const startDate = new Date(this.activeProject.startDate).toLocaleDateString();
        const dueDate = this.activeProject.dueDate ? new Date(this.activeProject.dueDate).toLocaleDateString() : "No deadline";

        projectDates.innerHTML = `
                    <div><strong>Start Date:</strong> ${startDate}</div>
                    <div><strong>Due Date:</strong> ${dueDate}</div>
                `;
      }

      // Update project status badge
      const statusBadge = document.getElementById("project-status");
      if (statusBadge) {
        statusBadge.className = `status-badge ${this.activeProject.status.toLowerCase()}`;
        statusBadge.textContent = this.activeProject.status;
      }
    },

    // Update project progress bar
    updateProjectProgress: function () {
      if (!this.activeProject) return;

      const progressBar = document.getElementById("project-progress-bar");
      const progressText = document.getElementById("project-progress-text");

      if (progressBar && progressText) {
        const progress = this.activeProject.progress || 0;
        progressBar.style.width = `${progress}%`;
        progressText.textContent = `${progress}%`;
      }
    },

    // Load project members
    loadProjectMembers: function () {
      if (!this.activeProject?.id) return;

      fetch(`/api/projects/${this.activeProject.id}/members`)
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to load project members");
          }
          return response.json();
        })
        .then((data) => {
          this.projectMembers = data;
          this.renderProjectMembers();
        })
        .catch((error) => {
          console.error("Error loading project members:", error);
        });
    },

    // Render project members list
    renderProjectMembers: function () {
      const membersContainer = document.getElementById("project-members");
      if (!membersContainer) return;

      if (this.projectMembers.length === 0) {
        membersContainer.innerHTML = '<div class="empty-state">No members in this project</div>';
        return;
      }

      let html = "";
      this.projectMembers.forEach((member) => {
        const roleClass = member.role.toLowerCase().replace(/\s+/g, "-");

        html += `
                    <div class="member-item">
                        <div class="member-avatar">
                            <img src="${member.avatar || "/assets/img/default-avatar.png"}" alt="${member.name}">
                            <span class="status-indicator ${member.online ? "online" : "offline"}"></span>
                        </div>
                        <div class="member-info">
                            <div class="member-name">${member.name}</div>
                            <div class="member-role ${roleClass}">${member.role}</div>
                        </div>
                        <div class="member-actions">
                            <button class="btn-icon member-action" title="Message" data-user-id="${member.id}">
                                <i class="fas fa-comment"></i>
                            </button>
                            ${
                              this.activeProject.isAdmin
                                ? `
                                <button class="btn-icon member-action" title="Change Role" data-action="change-role" data-user-id="${member.id}">
                                    <i class="fas fa-user-tag"></i>
                                </button>
                                <button class="btn-icon member-action danger" title="Remove from Project" data-action="remove-member" data-user-id="${member.id}">
                                    <i class="fas fa-user-minus"></i>
                                </button>
                            `
                                : ""
                            }
                        </div>
                    </div>
                `;
      });

      membersContainer.innerHTML = html;

      // Add event listeners to member actions
      document.querySelectorAll(".member-action").forEach((button) => {
        button.addEventListener("click", () => {
          const userId = button.dataset.userId;
          const action = button.dataset.action;

          if (action === "change-role") {
            this.showChangeRoleModal(userId);
          } else if (action === "remove-member") {
            this.confirmRemoveMember(userId);
          } else {
            // Default to message action
            this.startConversation(userId);
          }
        });
      });
    },

    // Load project tasks
    loadProjectTasks: function () {
      if (!this.activeProject?.id) return;

      fetch(`/api/projects/${this.activeProject.id}/tasks`)
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to load project tasks");
          }
          return response.json();
        })
        .then((data) => {
          this.renderTaskBoard(data);
        })
        .catch((error) => {
          console.error("Error loading project tasks:", error);
        });
    },

    // Render task board with task cards
    renderTaskBoard: function (tasks) {
      if (!tasks) return;

      // Group tasks by status
      const tasksByStatus = {
        todo: [],
        "in-progress": [],
        review: [],
        completed: [],
      };

      tasks.forEach((task) => {
        const status = task.status.toLowerCase().replace(" ", "-");
        if (tasksByStatus[status]) {
          tasksByStatus[status].push(task);
        }
      });

      // Render each status column
      for (const status in tasksByStatus) {
        const columnId = `${status}-tasks`;
        const column = document.getElementById(columnId);
        if (!column) continue;

        const columnTasks = tasksByStatus[status];

        if (columnTasks.length === 0) {
          column.innerHTML = `<div class="empty-column-message">No tasks</div>`;
          continue;
        }

        let html = "";
        columnTasks.forEach((task) => {
          const priorityClass = task.priority.toLowerCase();
          const dueDateDisplay = task.dueDate ? new Date(task.dueDate).toLocaleDateString() : "No deadline";

          html += `
                        <div class="task-card" draggable="true" data-task-id="${task.id}">
                            <div class="task-priority ${priorityClass}"></div>
                            <div class="task-header">
                                <h4 class="task-title">${task.title}</h4>
                                <span class="task-id">#${task.id}</span>
                            </div>
                            <div class="task-description">${this.truncateText(task.description, 100)}</div>
                            <div class="task-meta">
                                <div class="task-due-date">
                                    <i class="far fa-calendar-alt"></i> ${dueDateDisplay}
                                </div>
                            </div>
                            <div class="task-footer">
                                <div class="task-assignees">
                                    ${this.renderTaskAssignees(task.assignees)}
                                </div>
                                <div class="task-actions">
                                    <button class="btn-icon task-action" title="Edit Task" data-action="edit" data-task-id="${task.id}">
                                        <i class="fas fa-pencil-alt"></i>
                                    </button>
                                    <button class="btn-icon task-action" title="View Task" data-action="view" data-task-id="${task.id}">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    `;
        });

        column.innerHTML = html;
      }

      // Add event listeners to task cards
      document.querySelectorAll(".task-action").forEach((button) => {
        button.addEventListener("click", (event) => {
          event.stopPropagation(); // Prevent triggering parent click
          const taskId = button.dataset.taskId;
          const action = button.dataset.action;

          if (action === "edit") {
            this.showEditTaskModal(taskId);
          } else if (action === "view") {
            this.navigateToTask(taskId);
          }
        });
      });

      // Add click event to the entire task card for viewing
      document.querySelectorAll(".task-card").forEach((card) => {
        card.addEventListener("click", () => {
          const taskId = card.dataset.taskId;
          this.navigateToTask(taskId);
        });
      });

      // Set up drag and drop after rendering
      this.setupTaskDragDrop();
    },

    // Render task assignees avatars
    renderTaskAssignees: function (assignees) {
      if (!assignees || assignees.length === 0) {
        return '<span class="no-assignees">Unassigned</span>';
      }

      let html = "";
      assignees.slice(0, 3).forEach((assignee) => {
        html += `
                    <div class="assignee-avatar" title="${assignee.name}">
                        <img src="${assignee.avatar || "/assets/img/default-avatar.png"}" alt="${assignee.name}">
                    </div>
                `;
      });

      // Add indicator for additional assignees
      if (assignees.length > 3) {
        html += `<div class="assignee-more">+${assignees.length - 3}</div>`;
      }

      return html;
    },

    // Initialize task drag and drop functionality
    initializeTaskDragDrop: function () {
      const taskColumns = document.querySelectorAll(".task-column");

      // Task cards will be initialized in renderTaskBoard method
      // This method just sets up the columns as drop targets
      taskColumns.forEach((column) => {
        column.addEventListener("dragover", (event) => {
          event.preventDefault();
          column.classList.add("drag-over");
        });

        column.addEventListener("dragleave", () => {
          column.classList.remove("drag-over");
        });

        column.addEventListener("drop", (event) => {
          event.preventDefault();
          column.classList.remove("drag-over");

          const taskId = event.dataTransfer.getData("text/plain");
          const newStatus = column.id.replace("-tasks", "");

          this.updateTaskStatus(taskId, newStatus);
        });
      });
    },

    // Set up drag events on task cards
    setupTaskDragDrop: function () {
      const taskCards = document.querySelectorAll(".task-card");

      taskCards.forEach((card) => {
        card.addEventListener("dragstart", (event) => {
          event.dataTransfer.setData("text/plain", card.dataset.taskId);
          card.classList.add("dragging");
        });

        card.addEventListener("dragend", () => {
          card.classList.remove("dragging");
        });
      });
    },

    // Update task status (when dragged to new column)
    updateTaskStatus: function (taskId, newStatus) {
      fetch(`/api/tasks/${taskId}/status`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          status: this.formatStatus(newStatus),
        }),
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to update task status");
          }

          // Move card to new column without reloading
          const taskCard = document.querySelector(`.task-card[data-task-id="${taskId}"]`);
          const targetColumn = document.getElementById(`${newStatus}-tasks`);

          if (taskCard && targetColumn) {
            const emptyMessage = targetColumn.querySelector(".empty-column-message");
            if (emptyMessage) {
              emptyMessage.remove();
            }

            targetColumn.appendChild(taskCard);
          }

          // Show notification
          this.showNotification("success", "Task status updated successfully");

          // Update project progress
          this.refreshProjectProgress();
        })
        .catch((error) => {
          console.error("Error updating task status:", error);
          this.showNotification("error", "Failed to update task status");

          // Reload tasks to reset any inconsistent state
          this.loadProjectTasks();
        });
    },

    // Format status string for API
    formatStatus: function (status) {
      switch (status) {
        case "todo":
          return "To Do";
        case "in-progress":
          return "In Progress";
        case "review":
          return "Review";
        case "completed":
          return "Completed";
        default:
          return status;
      }
    },

    // Filter tasks by status
    filterTasks: function (status) {
      if (status === "all") {
        document.querySelectorAll(".task-card").forEach((card) => {
          card.style.display = "block";
        });
        return;
      }

      document.querySelectorAll(".task-card").forEach((card) => {
        const taskStatus = card.closest(".task-column").id.replace("-tasks", "");
        if (taskStatus === status) {
          card.style.display = "block";
        } else {
          card.style.display = "none";
        }
      });
    },

    // Refresh project progress after task updates
    refreshProjectProgress: function () {
      if (!this.activeProject?.id) return;

      fetch(`/api/projects/${this.activeProject.id}/progress`)
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to get project progress");
          }
          return response.json();
        })
        .then((data) => {
          this.activeProject.progress = data.progress;
          this.updateProjectProgress();
        })
        .catch((error) => {
          console.error("Error updating project progress:", error);
        });
    },

    // Load project activity history
    loadProjectActivity: function () {
      if (!this.activeProject?.id) return;

      const activityContainer = document.getElementById("activity-tab");
      if (!activityContainer) return;

      activityContainer.innerHTML = '<div class="loading">Loading activity history...</div>';

      fetch(`/api/projects/${this.activeProject.id}/activity`)
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to load activity history");
          }
          return response.json();
        })
        .then((data) => {
          this.renderActivityHistory(data);
        })
        .catch((error) => {
          console.error("Error loading activity:", error);
          activityContainer.innerHTML = '<div class="error">Failed to load activity history</div>';
        });
    },

    // Render activity history timeline
    renderActivityHistory: function (activities) {
      const activityContainer = document.getElementById("activity-tab");
      if (!activityContainer) return;

      if (!activities || activities.length === 0) {
        activityContainer.innerHTML = '<div class="empty-state">No activity yet</div>';
        return;
      }

      // Group activities by date
      const groupedActivities = this.groupActivitiesByDate(activities);

      let html = '<div class="activity-timeline">';

      Object.keys(groupedActivities).forEach((date) => {
        html += `
                    <div class="activity-date">
                        <div class="date-header">${date}</div>
                        <div class="date-activities">
                `;

        groupedActivities[date].forEach((activity) => {
          const icon = this.getActivityIcon(activity.type);
          const timeString = new Date(activity.timestamp).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });

          html += `
                        <div class="activity-item">
                            <div class="activity-time">${timeString}</div>
                            <div class="activity-icon ${activity.type}-activity">${icon}</div>
                            <div class="activity-content">
                                <div class="activity-user">${activity.userName}</div>
                                <div class="activity-message">${activity.message}</div>
                            </div>
                        </div>
                    `;
        });

        html += `
                        </div>
                    </div>
                `;
      });

      html += "</div>";
      activityContainer.innerHTML = html;
    },

    // Group activities by date
    groupActivitiesByDate: function (activities) {
      const groupedActivities = {};

      activities.forEach((activity) => {
        const activityDate = new Date(activity.timestamp);
        const dateString = activityDate.toLocaleDateString();

        if (!groupedActivities[dateString]) {
          groupedActivities[dateString] = [];
        }

        groupedActivities[dateString].push(activity);
      });

      // Sort activities within each date group
      for (const date in groupedActivities) {
        groupedActivities[date].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
      }

      return groupedActivities;
    },

    // Get appropriate icon for activity type
    getActivityIcon: function (type) {
      switch (type) {
        case "task-created":
          return '<i class="fas fa-plus-circle"></i>';
        case "task-updated":
          return '<i class="fas fa-edit"></i>';
        case "task-completed":
          return '<i class="fas fa-check-circle"></i>';
        case "task-comment":
          return '<i class="fas fa-comment"></i>';
        case "member-added":
          return '<i class="fas fa-user-plus"></i>';
        case "member-removed":
          return '<i class="fas fa-user-minus"></i>';
        case "project-updated":
          return '<i class="fas fa-project-diagram"></i>';
        case "file-uploaded":
          return '<i class="fas fa-file-upload"></i>';
        default:
          return '<i class="fas fa-info-circle"></i>';
      }
    },

    // Load project files
    loadProjectFiles: function () {
      if (!this.activeProject?.id) return;

      const filesContainer = document.getElementById("files-tab");
      if (!filesContainer) return;

      filesContainer.innerHTML = '<div class="loading">Loading files...</div>';

      fetch(`/api/projects/${this.activeProject.id}/files`)
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to load project files");
          }
          return response.json();
        })
        .then((data) => {
          this.renderProjectFiles(data);
        })
        .catch((error) => {
          console.error("Error loading files:", error);
          filesContainer.innerHTML = '<div class="error">Failed to load files</div>';
        });
    },

    // Render project files list
    renderProjectFiles: function (files) {
      // Implementation for rendering project files
    },

    // Load project discussions
    loadProjectDiscussions: function () {
      if (!this.activeProject?.id) return;

      const discussionsContainer = document.getElementById("discussion-tab");
      if (!discussionsContainer) return;

      discussionsContainer.innerHTML = '<div class="loading">Loading discussions...</div>';

      fetch(`/api/projects/${this.activeProject.id}/discussions`)
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to load discussions");
          }
          return response.json();
        })
        .then((data) => {
          this.renderProjectDiscussions(data);
        })
        .catch((error) => {
          console.error("Error loading discussions:", error);
          discussionsContainer.innerHTML = '<div class="error">Failed to load discussions</div>';
        });
    },

    // Render project discussions
    renderProjectDiscussions: function (discussions) {
      // Implementation for rendering project discussions
    },

    // Load project milestones
    loadProjectMilestones: function () {
      if (!this.activeProject?.id) return;

      const milestonesContainer = document.getElementById("milestones-tab");
      if (!milestonesContainer) return;

      milestonesContainer.innerHTML = '<div class="loading">Loading milestones...</div>';

      fetch(`/api/projects/${this.activeProject.id}/milestones`)
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to load milestones");
          }
          return response.json();
        })
        .then((data) => {
          this.renderProjectMilestones(data);
        })
        .catch((error) => {
          console.error("Error loading milestones:", error);
          milestonesContainer.innerHTML = '<div class="error">Failed to load milestones</div>';
        });
    },

    // Render project milestones
    renderProjectMilestones: function (milestones) {
      // Implementation for rendering project milestones
    },

    // Show add task modal
    showAddTaskModal: function () {
      // Implementation for add task modal
    },

    // Show edit task modal
    showEditTaskModal: function (taskId) {
      // Implementation for edit task modal
    },

    // Show add member modal
    showAddMemberModal: function () {
      // Implementation for add member modal
    },

    // Show change role modal
    showChangeRoleModal: function (userId) {
      // Implementation for change role modal
    },

    // Show edit project modal
    showEditProjectModal: function () {
      // Implementation for edit project modal
    },

    // Confirm remove member
    confirmRemoveMember: function (userId) {
      // Implementation for confirming member removal
    },

    // Confirm delete project
    confirmDeleteProject: function () {
      // Implementation for confirming project deletion
    },

    // Navigate to task details page
    navigateToTask: function (taskId) {
      window.location.href = `/task.jsp?id=${taskId}`;
    },

    // Start a conversation with a user
    startConversation: function (userId) {
      // Implementation for starting conversation
    },

    // Search within project
    searchProject: function (query) {
      // Implementation for project search
    },

    // Helper function to truncate text
    truncateText: function (text, maxLength) {
      if (!text) return "";
      return text.length > maxLength ? text.substring(0, maxLength) + "..." : text;
    },

    // Show notification to the user
    showNotification: function (type, message) {
      // Notify user using the app's notification system
      if (window.Notifications) {
        window.Notifications.showToastNotification({
          type: type,
          message: message,
        });
      } else {
        alert(message);
      }
    },
  };

  // Initialize projects module
  Projects.init();

  // Initialize team member functionality
  initTeamMemberForm();

  // Initialize project form functionality
  function initTeamMemberForm() {
    // Add team member button
    const addTeamMemberBtn = document.getElementById("addTeamMemberBtn");
    if (addTeamMemberBtn) {
      // Add click event handler to the button
      addTeamMemberBtn.addEventListener("click", function () {
        // Show the team member selection modal
        $("#teamMemberModal").modal("show");
      });
    }

    // Team member search functionality
    const memberSearchInput = document.getElementById("memberSearchInput");
    if (memberSearchInput) {
      memberSearchInput.addEventListener("input", function () {
        const searchTerm = this.value.toLowerCase();
        document.querySelectorAll(".member-item").forEach(function (item) {
          const memberName = item.getAttribute("data-name").toLowerCase();
          if (memberName.includes(searchTerm)) {
            item.style.display = "flex";
          } else {
            item.style.display = "none";
          }
        });
      });
    }

    // Select team members
    document.querySelectorAll(".member-item").forEach(function (item) {
      item.addEventListener("click", function () {
        this.classList.toggle("selected");
      });
    });

    // Add selected members button
    const addSelectedMembersBtn = document.getElementById("addSelectedMembers");
    if (addSelectedMembersBtn) {
      addSelectedMembersBtn.addEventListener("click", function () {
        const selectedMembers = document.querySelectorAll(".member-item.selected");
        const selectedMembersContainer = document.querySelector(".selected-members");

        selectedMembers.forEach(function (member) {
          const memberId = member.getAttribute("data-id");
          const memberName = member.getAttribute("data-name");
          const memberImg = member.querySelector("img").getAttribute("src");

          // Check if this member is already added
          const existingMember = document.querySelector(`.selected-member input[value="${memberId}"]`);
          if (!existingMember) {
            // Create a new member element
            const memberElement = document.createElement("div");
            memberElement.className = "selected-member";
            memberElement.innerHTML = `
              <input type="hidden" name="teamMembers" value="${memberId}">
              <img src="${memberImg}" alt="${memberName}">
              <span>${memberName}</span>
              <button type="button" class="remove-member">&times;</button>
            `;

            // Add the new member to the container
            selectedMembersContainer.appendChild(memberElement);

            // Add remove functionality
            memberElement.querySelector(".remove-member").addEventListener("click", function () {
              memberElement.remove();
            });
          }
        });

        // Close the modal
        $("#teamMemberModal").modal("hide");
      });
    }

    // Initialize remove buttons for existing members
    document.querySelectorAll(".remove-member").forEach(function (button) {
      button.addEventListener("click", function () {
        this.closest(".selected-member").remove();
      });
    });
  }
});
