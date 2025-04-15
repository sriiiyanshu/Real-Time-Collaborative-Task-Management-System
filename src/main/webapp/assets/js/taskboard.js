/**
 * Taskboard Module for Real-Time Task Application
 * Handles kanban-style task board functionality
 */
document.addEventListener('DOMContentLoaded', function() {
    'use strict';
    
    // Taskboard Module
    const Taskboard = {
        // Configuration
        config: {
            refreshInterval: 30000, // 30 seconds
            animationDuration: 300,
            maxDescriptionLength: 120
        },
        
        // State tracking
        draggedTask: null,
        tasks: [],
        taskMap: {}, // For quick access by ID
        filters: {
            status: 'all',
            priority: 'all',
            assignee: 'all',
            dueDate: 'all',
            search: ''
        },
        
        // Task refresh timer
        refreshTimer: null,
        
        // Initialize the taskboard
        init: function() {
            console.log('Taskboard module initialized');
            this.loadTasks();
            this.setupEventListeners();
            this.setupAutoRefresh();
            this.initWebSocket();
        },
        
        // Set up event listeners
        setupEventListeners: function() {
            // Add task button
            const addTaskBtn = document.getElementById('add-task-btn');
            if (addTaskBtn) {
                addTaskBtn.addEventListener('click', () => {
                    this.showAddTaskModal();
                });
            }
            
            // Filter handlers
            document.querySelectorAll('.task-filter').forEach(filter => {
                filter.addEventListener('change', () => {
                    this.updateFilters();
                });
            });
            
            // Clear filters button
            const clearFiltersBtn = document.getElementById('clear-filters-btn');
            if (clearFiltersBtn) {
                clearFiltersBtn.addEventListener('click', () => {
                    this.clearFilters();
                });
            }
            
            // Search input
            const searchInput = document.getElementById('task-search');
            if (searchInput) {
                searchInput.addEventListener('input', () => {
                    this.filters.search = searchInput.value;
                    this.applyFilters();
                });
            }
            
            // Expand/collapse column headers
            document.querySelectorAll('.column-header').forEach(header => {
                header.addEventListener('click', (event) => {
                    // Ignore if click was on button inside header
                    if (event.target.closest('button')) return;
                    
                    const column = header.closest('.task-column');
                    column.classList.toggle('collapsed');
                });
            });
            
            // Task view options
            document.querySelectorAll('.view-option').forEach(option => {
                option.addEventListener('click', () => {
                    document.querySelectorAll('.view-option').forEach(o => {
                        o.classList.remove('active');
                    });
                    option.classList.add('active');
                    
                    const viewType = option.dataset.view;
                    this.switchViewType(viewType);
                });
            });
            
            // Board refresh button
            const refreshBtn = document.getElementById('refresh-board-btn');
            if (refreshBtn) {
                refreshBtn.addEventListener('click', () => {
                    this.loadTasks();
                });
            }
            
            // Initialize drag and drop
            this.initDragAndDrop();
        },
        
        // Load tasks from the server
        loadTasks: function() {
            const projectId = document.getElementById('taskboard-data')?.dataset.projectId;
            const url = projectId ? 
                `/api/projects/${projectId}/tasks` : 
                '/api/tasks';
            
            // Show loading state
            document.querySelectorAll('.task-column-content').forEach(column => {
                column.innerHTML = '<div class="loading-tasks">Loading tasks...</div>';
            });
            
            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to load tasks');
                    }
                    return response.json();
                })
                .then(data => {
                    this.tasks = data;
                    this.buildTaskMap();
                    this.renderTasks();
                })
                .catch(error => {
                    console.error('Error loading tasks:', error);
                    document.querySelectorAll('.task-column-content').forEach(column => {
                        column.innerHTML = '<div class="error-message">Failed to load tasks</div>';
                    });
                });
        },
        
        // Build task map for quick access by ID
        buildTaskMap: function() {
            this.taskMap = {};
            this.tasks.forEach(task => {
                this.taskMap[task.id] = task;
            });
        },
        
        // Render tasks to columns
        renderTasks: function() {
            // Clear existing tasks
            document.querySelectorAll('.task-column-content').forEach(column => {
                column.innerHTML = '';
            });
            
            // Group tasks by status
            const tasksByStatus = {
                'to-do': [],
                'in-progress': [],
                'review': [],
                'completed': []
            };
            
            // Filter and sort tasks
            const filteredTasks = this.getFilteredTasks();
            filteredTasks.forEach(task => {
                const status = this.getStatusKey(task.status);
                if (tasksByStatus[status]) {
                    tasksByStatus[status].push(task);
                }
            });
            
            // Render each status column
            for (const status in tasksByStatus) {
                const columnId = `${status}-column`;
                const column = document.getElementById(columnId);
                if (!column) continue;
                
                const columnContent = column.querySelector('.task-column-content');
                if (!columnContent) continue;
                
                const columnTasks = tasksByStatus[status];
                
                if (columnTasks.length === 0) {
                    columnContent.innerHTML = `<div class="empty-column">No tasks</div>`;
                    continue;
                }
                
                // Sort tasks within column
                columnTasks.sort((a, b) => {
                    // First by priority
                    const priorityOrder = { 'High': 1, 'Medium': 2, 'Low': 3 };
                    const priorityDiff = priorityOrder[a.priority] - priorityOrder[b.priority];
                    if (priorityDiff !== 0) return priorityDiff;
                    
                    // Then by due date
                    if (a.dueDate && b.dueDate) {
                        return new Date(a.dueDate) - new Date(b.dueDate);
                    }
                    if (a.dueDate) return -1;
                    if (b.dueDate) return 1;
                    
                    // Finally by creation date
                    return new Date(b.createdAt) - new Date(a.createdAt);
                });
                
                // Render tasks
                columnTasks.forEach(task => {
                    const taskElement = this.createTaskElement(task);
                    columnContent.appendChild(taskElement);
                });
                
                // Update column counter
                const counter = column.querySelector('.task-count');
                if (counter) {
                    counter.textContent = columnTasks.length;
                }
            }
            
            // Setup task event listeners
            this.setupTaskEventListeners();
        },
        
        // Create task element
        createTaskElement: function(task) {
            const taskElement = document.createElement('div');
            taskElement.className = `task-card priority-${task.priority.toLowerCase()}`;
            taskElement.dataset.taskId = task.id;
            taskElement.draggable = true;
            
            // Format dates
            const createdDate = new Date(task.createdAt).toLocaleDateString();
            const dueDate = task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'No deadline';
            
            // Check if task is overdue
            const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'Completed';
            const dueDateClass = isOverdue ? 'overdue' : '';
            
            // Truncate description
            const shortDescription = this.truncateText(task.description, this.config.maxDescriptionLength);
            
            // Build task card HTML
            taskElement.innerHTML = `
                <div class="task-header">
                    <div class="task-id">#${task.id}</div>
                    <div class="task-actions">
                        <button class="btn-icon edit-task-btn" title="Edit Task">
                            <i class="fas fa-edit"></i>
                        </button>
                    </div>
                </div>
                <div class="task-title">${task.title}</div>
                <div class="task-description">${shortDescription}</div>
                <div class="task-meta">
                    <div class="task-dates">
                        <div class="task-created" title="Created on">
                            <i class="far fa-calendar"></i> ${createdDate}
                        </div>
                        <div class="task-due ${dueDateClass}" title="Due date">
                            <i class="far fa-calendar-alt"></i> ${dueDate}
                        </div>
                    </div>
                    <div class="task-priority-badge">${task.priority}</div>
                </div>
                <div class="task-footer">
                    ${this.renderAssignees(task.assignees)}
                    ${task.comments ? `
                        <div class="comments-indicator" title="${task.comments} comments">
                            <i class="far fa-comment"></i> ${task.comments}
                        </div>
                    ` : ''}
                </div>
            `;
            
            return taskElement;
        },
        
        // Render assignees for a task
        renderAssignees: function(assignees) {
            if (!assignees || assignees.length === 0) {
                return '<div class="unassigned">Unassigned</div>';
            }
            
            let html = '<div class="assignees">';
            
            // Show up to 3 assignees directly
            assignees.slice(0, 3).forEach(user => {
                html += `
                    <div class="assignee-avatar" title="${user.name}">
                        <img src="${user.avatar || '/assets/img/default-avatar.png'}" alt="${user.name}">
                    </div>
                `;
            });
            
            // Show count for additional assignees
            if (assignees.length > 3) {
                html += `<div class="more-assignees">+${assignees.length - 3}</div>`;
            }
            
            html += '</div>';
            return html;
        },
        
        // Setup event listeners for task cards
        setupTaskEventListeners: function() {
            // Task click opens detail view
            document.querySelectorAll('.task-card').forEach(card => {
                card.addEventListener('click', (event) => {
                    // Don't open detail view if clicked on action buttons
                    if (event.target.closest('.task-actions')) return;
                    
                    const taskId = card.dataset.taskId;
                    this.openTaskDetails(taskId);
                });
            });
            
            // Edit button
            document.querySelectorAll('.edit-task-btn').forEach(btn => {
                btn.addEventListener('click', (event) => {
                    event.stopPropagation();
                    const taskId = btn.closest('.task-card').dataset.taskId;
                    this.editTask(taskId);
                });
            });
        },
        
        // Initialize drag and drop functionality
        initDragAndDrop: function() {
            // Set up dropzones (columns)
            document.querySelectorAll('.task-column-content').forEach(column => {
                column.addEventListener('dragover', (event) => {
                    event.preventDefault();
                    column.classList.add('dragover');
                });
                
                column.addEventListener('dragleave', () => {
                    column.classList.remove('dragover');
                });
                
                column.addEventListener('drop', (event) => {
                    event.preventDefault();
                    column.classList.remove('dragover');
                    
                    const taskId = event.dataTransfer.getData('text/plain');
                    if (!taskId) return;
                    
                    // Get new status from column ID
                    const columnId = column.closest('.task-column').id;
                    const newStatus = this.getStatusFromColumnId(columnId);
                    
                    // Update task status
                    this.updateTaskStatus(taskId, newStatus);
                });
            });
            
            // Set up draggable tasks (added after tasks are rendered)
            this.setupDraggableTasks();
        },
        
        // Set up draggable behavior for task cards
        setupDraggableTasks: function() {
            document.querySelectorAll('.task-card').forEach(task => {
                task.addEventListener('dragstart', (event) => {
                    this.draggedTask = task;
                    task.classList.add('dragging');
                    event.dataTransfer.setData('text/plain', task.dataset.taskId);
                });
                
                task.addEventListener('dragend', () => {
                    task.classList.remove('dragging');
                    this.draggedTask = null;
                });
            });
        },
        
        // Update a task's status via API
        updateTaskStatus: function(taskId, newStatus) {
            const task = this.taskMap[taskId];
            if (!task) return;
            
            const oldStatus = task.status;
            if (oldStatus === newStatus) return;
            
            // Optimistically update UI
            task.status = newStatus;
            this.renderTasks();
            
            // Send update to server
            fetch(`/api/tasks/${taskId}/status`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ status: newStatus })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to update task status');
                }
                return response.json();
            })
            .then(updatedTask => {
                // Update task in local data
                Object.assign(this.taskMap[taskId], updatedTask);
            })
            .catch(error => {
                console.error('Error updating task status:', error);
                
                // Revert optimistic update
                task.status = oldStatus;
                this.renderTasks();
                
                // Show error notification
                this.showNotification('error', 'Failed to update task status');
            });
        },
        
        // Open task details panel/modal
        openTaskDetails: function(taskId) {
            const task = this.taskMap[taskId];
            if (!task) return;
            
            // Redirect to task detail page
            window.location.href = `/task.jsp?id=${taskId}`;
        },
        
        // Open edit task form
        editTask: function(taskId) {
            const task = this.taskMap[taskId];
            if (!task) return;
            
            // Show edit task modal/panel
            this.showEditTaskModal(task);
        },
        
        // Show modal for adding a new task
        showAddTaskModal: function() {
            // Implementation for showing add task modal
            const modalId = 'add-task-modal';
            
            // Check if modal already exists
            let modal = document.getElementById(modalId);
            if (!modal) {
                // Create modal if it doesn't exist
                modal = this.createTaskModal(modalId, 'Add New Task');
                document.body.appendChild(modal);
            }
            
            // Clear form inputs
            const form = modal.querySelector('form');
            if (form) form.reset();
            
            // Show modal
            modal.classList.add('show');
        },
        
        // Show modal for editing an existing task
        showEditTaskModal: function(task) {
            // Implementation for showing edit task modal
            const modalId = 'edit-task-modal';
            
            // Check if modal already exists
            let modal = document.getElementById(modalId);
            if (!modal) {
                // Create modal if it doesn't exist
                modal = this.createTaskModal(modalId, 'Edit Task');
                document.body.appendChild(modal);
            }
            
            // Fill form with task data
            const form = modal.querySelector('form');
            if (form) {
                form.elements['title'].value = task.title;
                form.elements['description'].value = task.description;
                form.elements['priority'].value = task.priority;
                form.elements['status'].value = task.status;
                if (task.dueDate) {
                    form.elements['dueDate'].value = new Date(task.dueDate).toISOString().split('T')[0];
                }
                // Set selected assignees
                // This would be more complex depending on your UI implementation
            }
            
            // Set task ID for form submission
            modal.dataset.taskId = task.id;
            
            // Show modal
            modal.classList.add('show');
        },
        
        // Create a task modal element
        createTaskModal: function(id, title) {
            const modal = document.createElement('div');
            modal.id = id;
            modal.className = 'modal';
            
            // Modal content HTML
            modal.innerHTML = `
                <div class="modal-content">
                    <div class="modal-header">
                        <h3>${title}</h3>
                        <button class="close-modal">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                    <div class="modal-body">
                        <form class="task-form">
                            <div class="form-group">
                                <label for="title">Title</label>
                                <input type="text" id="title" name="title" required>
                            </div>
                            <div class="form-group">
                                <label for="description">Description</label>
                                <textarea id="description" name="description" rows="4"></textarea>
                            </div>
                            <div class="form-row">
                                <div class="form-group">
                                    <label for="priority">Priority</label>
                                    <select id="priority" name="priority">
                                        <option value="High">High</option>
                                        <option value="Medium" selected>Medium</option>
                                        <option value="Low">Low</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label for="status">Status</label>
                                    <select id="status" name="status">
                                        <option value="To Do">To Do</option>
                                        <option value="In Progress">In Progress</option>
                                        <option value="Review">Review</option>
                                        <option value="Completed">Completed</option>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="dueDate">Due Date</label>
                                <input type="date" id="dueDate" name="dueDate">
                            </div>
                            <div class="form-group">
                                <label>Assignees</label>
                                <div class="assignee-selector">
                                    <!-- Assignee selection UI would go here -->
                                    <div class="assignee-placeholder">Loading users...</div>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button class="btn btn-secondary cancel-btn">Cancel</button>
                        <button class="btn btn-primary save-btn">Save Task</button>
                    </div>
                </div>
            `;
            
            // Add event listeners
            modal.querySelector('.close-modal').addEventListener('click', () => {
                modal.classList.remove('show');
            });
            
            modal.querySelector('.cancel-btn').addEventListener('click', () => {
                modal.classList.remove('show');
            });
            
            modal.querySelector('.save-btn').addEventListener('click', () => {
                const form = modal.querySelector('form');
                if (!form.checkValidity()) {
                    form.reportValidity();
                    return;
                }
                
                const taskId = modal.dataset.taskId;
                const formData = new FormData(form);
                
                if (id === 'add-task-modal') {
                    this.saveNewTask(formData);
                } else {
                    this.updateTask(taskId, formData);
                }
                
                modal.classList.remove('show');
            });
            
            return modal;
        },
        
        // Save a new task
        saveNewTask: function(formData) {
            const task = {
                title: formData.get('title'),
                description: formData.get('description'),
                priority: formData.get('priority'),
                status: formData.get('status'),
                dueDate: formData.get('dueDate') || null,
                // Add other fields as needed
            };
            
            // Get project ID if available
            const projectId = document.getElementById('taskboard-data')?.dataset.projectId;
            if (projectId) {
                task.projectId = projectId;
            }
            
            fetch('/api/tasks', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(task)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to create task');
                }
                return response.json();
            })
            .then(newTask => {
                // Add to task collections
                this.tasks.push(newTask);
                this.taskMap[newTask.id] = newTask;
                
                // Update UI
                this.renderTasks();
                
                // Show success notification
                this.showNotification('success', 'Task created successfully');
            })
            .catch(error => {
                console.error('Error creating task:', error);
                this.showNotification('error', 'Failed to create task');
            });
        },
        
        // Update an existing task
        updateTask: function(taskId, formData) {
            const task = {
                title: formData.get('title'),
                description: formData.get('description'),
                priority: formData.get('priority'),
                status: formData.get('status'),
                dueDate: formData.get('dueDate') || null,
                // Add other fields as needed
            };
            
            fetch(`/api/tasks/${taskId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(task)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to update task');
                }
                return response.json();
            })
            .then(updatedTask => {
                // Update task in collections
                Object.assign(this.taskMap[taskId], updatedTask);
                
                // Update UI
                this.renderTasks();
                
                // Show success notification
                this.showNotification('success', 'Task updated successfully');
            })
            .catch(error => {
                console.error('Error updating task:', error);
                this.showNotification('error', 'Failed to update task');
            });
        },
        
        // Get filtered tasks based on current filters
        getFilteredTasks: function() {
            return this.tasks.filter(task => {
                // Status filter
                if (this.filters.status !== 'all' && this.getStatusKey(task.status) !== this.filters.status) {
                    return false;
                }
                
                // Priority filter
                if (this.filters.priority !== 'all' && task.priority.toLowerCase() !== this.filters.priority) {
                    return false;
                }
                
                // Assignee filter
                if (this.filters.assignee !== 'all') {
                    if (this.filters.assignee === 'unassigned' && task.assignees && task.assignees.length > 0) {
                        return false;
                    }
                    if (this.filters.assignee !== 'unassigned' && 
                        (!task.assignees || !task.assignees.some(a => a.id === this.filters.assignee))) {
                        return false;
                    }
                }
                
                // Due date filter
                if (this.filters.dueDate !== 'all') {
                    const today = new Date();
                    today.setHours(0, 0, 0, 0);
                    
                    const tomorrow = new Date(today);
                    tomorrow.setDate(tomorrow.getDate() + 1);
                    
                    const nextWeek = new Date(today);
                    nextWeek.setDate(nextWeek.getDate() + 7);
                    
                    const dueDate = task.dueDate ? new Date(task.dueDate) : null;
                    
                    switch (this.filters.dueDate) {
                        case 'overdue':
                            if (!dueDate || dueDate >= today || task.status === 'Completed') {
                                return false;
                            }
                            break;
                        case 'today':
                            if (!dueDate || dueDate < today || dueDate >= tomorrow) {
                                return false;
                            }
                            break;
                        case 'week':
                            if (!dueDate || dueDate < today || dueDate >= nextWeek) {
                                return false;
                            }
                            break;
                        case 'no-date':
                            if (dueDate) {
                                return false;
                            }
                            break;
                    }
                }
                
                // Search filter
                if (this.filters.search) {
                    const searchLower = this.filters.search.toLowerCase();
                    const matchesSearch = 
                        task.title.toLowerCase().includes(searchLower) ||
                        (task.description && task.description.toLowerCase().includes(searchLower));
                    
                    if (!matchesSearch) {
                        return false;
                    }
                }
                
                return true;
            });
        },
        
        // Update filters based on form values
        updateFilters: function() {
            const statusFilter = document.getElementById('status-filter');
            if (statusFilter) {
                this.filters.status = statusFilter.value;
            }
            
            const priorityFilter = document.getElementById('priority-filter');
            if (priorityFilter) {
                this.filters.priority = priorityFilter.value;
            }
            
            const assigneeFilter = document.getElementById('assignee-filter');
            if (assigneeFilter) {
                this.filters.assignee = assigneeFilter.value;
            }
            
            const dueDateFilter = document.getElementById('due-date-filter');
            if (dueDateFilter) {
                this.filters.dueDate = dueDateFilter.value;
            }
            
            this.applyFilters();
        },
        
        // Apply current filters and update view
        applyFilters: function() {
            this.renderTasks();
        },
        
        // Clear all filters
        clearFilters: function() {
            this.filters = {
                status: 'all',
                priority: 'all',
                assignee: 'all',
                dueDate: 'all',
                search: ''
            };
            
            // Reset form controls
            document.getElementById('status-filter')?.value = 'all';
            document.getElementById('priority-filter')?.value = 'all';
            document.getElementById('assignee-filter')?.value = 'all';
            document.getElementById('due-date-filter')?.value = 'all';
            document.getElementById('task-search').value = '';
            
            this.applyFilters();
        },
        
        // Switch between different view types (board, list, etc.)
        switchViewType: function(viewType) {
            const taskboardElement = document.getElementById('taskboard');
            if (!taskboardElement) return;
            
            // Remove previous view classes
            taskboardElement.classList.remove('board-view', 'list-view', 'compact-view');
            
            // Add new view class
            taskboardElement.classList.add(`${viewType}-view`);
        },
        
        // Set up automatic refresh of tasks
        setupAutoRefresh: function() {
            if (this.refreshTimer) {
                clearInterval(this.refreshTimer);
            }
            
            this.refreshTimer = setInterval(() => {
                this.loadTasks();
            }, this.config.refreshInterval);
        },
        
        // Initialize WebSocket for real-time updates
        initWebSocket: function() {
            const wsProtocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
            const socket = new WebSocket(`${wsProtocol}${window.location.host}/ws/tasks`);
            
            socket.onmessage = (event) => {
                const data = JSON.parse(event.data);
                
                if (data.type === 'TASK_UPDATED') {
                    this.handleTaskUpdate(data.task);
                } else if (data.type === 'TASK_CREATED') {
                    this.handleTaskCreated(data.task);
                } else if (data.type === 'TASK_DELETED') {
                    this.handleTaskDeleted(data.taskId);
                }
            };
            
            socket.onerror = (error) => {
                console.error('WebSocket error:', error);
            };
            
            socket.onclose = () => {
                // Try to reconnect after a delay
                setTimeout(() => {
                    this.initWebSocket();
                }, 5000);
            };
        },
        
        // Handle real-time task update
        handleTaskUpdate: function(updatedTask) {
            const existingTask = this.taskMap[updatedTask.id];
            if (existingTask) {
                Object.assign(existingTask, updatedTask);
                this.renderTasks();
            }
        },
        
        // Handle real-time task creation
        handleTaskCreated: function(newTask) {
            this.tasks.push(newTask);
            this.taskMap[newTask.id] = newTask;
            this.renderTasks();
        },
        
        // Handle real-time task deletion
        handleTaskDeleted: function(taskId) {
            this.tasks = this.tasks.filter(task => task.id !== taskId);
            delete this.taskMap[taskId];
            this.renderTasks();
        },
        
        // Helper function to get status key from status display name
        getStatusKey: function(status) {
            switch(status) {
                case 'To Do':
                    return 'to-do';
                case 'In Progress':
                    return 'in-progress';
                case 'Review':
                    return 'review';
                case 'Completed':
                    return 'completed';
                default:
                    return status.toLowerCase().replace(' ', '-');
            }
        },
        
        // Helper function to get status display name from column ID
        getStatusFromColumnId: function(columnId) {
            switch(columnId) {
                case 'to-do-column':
                    return 'To Do';
                case 'in-progress-column':
                    return 'In Progress';
                case 'review-column':
                    return 'Review';
                case 'completed-column':
                    return 'Completed';
                default:
                    return 'To Do';
            }
        },
        
        // Helper function to truncate text with ellipsis
        truncateText: function(text, maxLength) {
            if (!text) return '';
            return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
        },
        
        // Show notification to the user
        showNotification: function(type, message) {
            // Notify user using the app's notification system
            if (window.Notifications) {
                window.Notifications.showToastNotification({
                    type: type,
                    message: message
                });
            } else {
                alert(message);
            }
        }
    };
    
    // Initialize taskboard module
    Taskboard.init();
});