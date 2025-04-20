/**
 * Tasks.js - JavaScript for task management functionality
 */
document.addEventListener("DOMContentLoaded", function () {
  // Handle adding subtasks
  initSubtaskFunctionality();

  // Initialize task filters
  initTaskFilters();

  // Handle subtask checkboxes on task detail page
  initSubtaskCheckboxes();

  // Update the color of due dates (highlight overdue tasks)
  updateDueDateColors();

  // Initialize task form submission handler
  initTaskFormHandler();

  // Direct form submission event handler
  directFormSubmissionHandler();
});

/**
 * Initializes the subtask functionality for adding/removing subtasks
 */
function initSubtaskFunctionality() {
  const addSubtaskBtn = document.querySelector(".add-subtask");
  if (addSubtaskBtn) {
    addSubtaskBtn.addEventListener("click", function () {
      const subtasksContainer = document.querySelector(".subtasks-container");
      const template = document.querySelector(".subtask-template").innerHTML;

      // Create a new subtask item from template
      const subtaskWrapper = document.createElement("div");
      subtaskWrapper.innerHTML = template;
      const newSubtask = subtaskWrapper.firstElementChild;

      // Append to container
      subtasksContainer.appendChild(newSubtask);

      // Add event listener to the remove button
      const removeBtn = newSubtask.querySelector(".remove-subtask");
      if (removeBtn) {
        removeBtn.addEventListener("click", function () {
          newSubtask.remove();
        });
      }
    });
  }

  // Add event listeners to existing remove buttons
  const removeSubtaskBtns = document.querySelectorAll(".remove-subtask");
  removeSubtaskBtns.forEach((btn) => {
    btn.addEventListener("click", function () {
      const subtaskItem = this.closest(".subtask-item");
      if (subtaskItem) {
        subtaskItem.remove();
      }
    });
  });
}

/**
 * Initializes task filtering functionality
 */
function initTaskFilters() {
  const statusFilter = document.getElementById("statusFilter");
  const priorityFilter = document.getElementById("priorityFilter");
  const projectFilter = document.getElementById("projectFilter");
  const searchInput = document.getElementById("taskSearch");

  // Only proceed if we're on the task list page
  if (!statusFilter || !priorityFilter || !projectFilter || !searchInput) {
    return;
  }

  const taskCards = document.querySelectorAll(".task-card");

  // Function to apply all filters
  function applyFilters() {
    const statusValue = statusFilter.value;
    const priorityValue = priorityFilter.value;
    const projectValue = projectFilter.value;
    const searchValue = searchInput.value.toLowerCase();

    taskCards.forEach((card) => {
      const cardStatus = card.dataset.status;
      const cardPriority = card.dataset.priority;
      const cardProject = card.dataset.project;
      const cardTitle = card.querySelector(".task-title").textContent.toLowerCase();
      const cardDescription = card.querySelector(".task-description") ? card.querySelector(".task-description").textContent.toLowerCase() : "";

      // Check if card matches all filter criteria
      const matchesStatus = statusValue === "all" || cardStatus === statusValue;
      const matchesPriority = priorityValue === "all" || cardPriority === priorityValue;
      const matchesProject = projectValue === "all" || (projectValue === "none" && cardProject === "none") || cardProject === projectValue;
      const matchesSearch = cardTitle.includes(searchValue) || cardDescription.includes(searchValue);

      // Show or hide based on matches
      if (matchesStatus && matchesPriority && matchesProject && matchesSearch) {
        card.style.display = "";
      } else {
        card.style.display = "none";
      }
    });
  }

  // Add event listeners to filters
  statusFilter.addEventListener("change", applyFilters);
  priorityFilter.addEventListener("change", applyFilters);
  projectFilter.addEventListener("change", applyFilters);
  searchInput.addEventListener("input", applyFilters);
}

/**
 * Initializes subtask checkbox functionality
 */
function initSubtaskCheckboxes() {
  const subtaskCheckboxes = document.querySelectorAll('.subtask-checkbox input[type="checkbox"]');

  subtaskCheckboxes.forEach((checkbox) => {
    checkbox.addEventListener("change", function () {
      const subtaskId = this.dataset.id;
      const status = this.checked ? "COMPLETED" : "IN_PROGRESS";

      // Update subtask status via AJAX
      fetch(`${contextPath}/task/update-subtask`, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: `subtaskId=${subtaskId}&status=${status}`,
      })
        .then((response) => {
          if (response.ok) {
            // Update UI
            const subtaskItem = this.closest(".subtask-item");
            if (status === "COMPLETED") {
              subtaskItem.classList.add("completed");
            } else {
              subtaskItem.classList.remove("completed");
            }
          } else {
            console.error("Failed to update subtask status");
            // Revert checkbox state
            this.checked = !this.checked;
          }
        })
        .catch((error) => {
          console.error("Error:", error);
          // Revert checkbox state
          this.checked = !this.checked;
        });
    });
  });
}

/**
 * Updates the color of due dates based on whether they are overdue
 */
function updateDueDateColors() {
  const dueDates = document.querySelectorAll(".due-date");
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  dueDates.forEach((dateElement) => {
    // If it already has the overdue class, it was determined server-side
    if (dateElement.classList.contains("overdue")) {
      dateElement.style.color = "#ff4747";
    }
  });
}

/**
 * Initialize task form validation and submission handling
 */
function initTaskFormHandler() {
  const taskForm = document.querySelector('form[action*="/task"]');
  if (!taskForm) return;

  taskForm.addEventListener("submit", function (event) {
    console.log("Form submission attempted");

    // Prevent default form submission if validation fails
    if (!validateTaskForm(this)) {
      console.log("Form validation failed");
      event.preventDefault();
      return false;
    }

    console.log("Form validation passed, submitting form");
    // Form is valid, allow submission to proceed
    return true;
  });

  // Add input validation listeners for required fields
  const requiredInputs = taskForm.querySelectorAll("input[required], textarea[required], select[required]");
  requiredInputs.forEach((input) => {
    input.addEventListener("input", function () {
      if (this.value.trim()) {
        this.classList.remove("is-invalid");
      } else {
        this.classList.add("is-invalid");
      }
    });
  });

  // Add click handler to the submit button for additional debugging
  const submitBtn = taskForm.querySelector('button[type="submit"]');
  if (submitBtn) {
    submitBtn.addEventListener("click", function (e) {
      console.log("Submit button clicked");
    });
  }
}

/**
 * Validate the task form before submission
 * @param {HTMLFormElement} form - The form to validate
 * @return {boolean} - Whether the form is valid
 */
function validateTaskForm(form) {
  let isValid = true;
  console.log("Starting form validation");

  // Check required fields
  const requiredFields = form.querySelectorAll("input[required], textarea[required], select[required]");
  console.log(`Found ${requiredFields.length} required fields`);

  requiredFields.forEach((field) => {
    console.log(`Checking field ${field.name}: '${field.value}'`);
    if (!field.value || !field.value.trim()) {
      field.classList.add("is-invalid");
      console.log(`Field ${field.name} is invalid - empty value`);
      isValid = false;
    } else {
      field.classList.remove("is-invalid");
    }
  });

  // Check title length only if title is present
  const titleField = form.querySelector('input[name="title"]');
  if (titleField) {
    console.log(`Title field value: '${titleField.value}'`);
    if (titleField.value && titleField.value.trim().length < 3) {
      titleField.classList.add("is-invalid");
      console.log("Title field is too short (less than 3 chars)");
      isValid = false;
    }
  }

  // Only validate non-empty subtasks (don't fail on empty subtasks as they are optional)
  const subtaskTitles = form.querySelectorAll('input[name="subtaskTitle"]');
  if (subtaskTitles.length > 0) {
    console.log(`Found ${subtaskTitles.length} subtask title fields`);

    let hasNonEmptySubtask = false;
    subtaskTitles.forEach((input) => {
      if (input.value && input.value.trim() !== "") {
        hasNonEmptySubtask = true;
      }
    });

    // Only validate subtasks if any non-empty ones exist
    if (hasNonEmptySubtask) {
      subtaskTitles.forEach((input) => {
        if (input.value.trim() === "") {
          input.classList.add("is-invalid");
          console.log("Empty subtask found when others are filled");
          isValid = false;
        } else {
          input.classList.remove("is-invalid");
        }
      });
    }
  }

  console.log(`Form validation result: ${isValid ? "valid" : "invalid"}`);
  return isValid;
}

/**
 * Global contextPath variable to be used in AJAX requests
 */
const contextPath = document.querySelector('meta[name="context-path"]')?.content || "";

/**
 * Direct form submission handler that bypasses other events
 */
function directFormSubmissionHandler() {
  const createTaskButton = document.getElementById("createTaskButton");
  if (createTaskButton) {
    createTaskButton.addEventListener("click", function (e) {
      e.preventDefault();
      console.log("Direct submit handler triggered");

      const form = document.getElementById("taskForm");

      try {
        if (form) {
          // First attempt validation
          if (validateTaskForm(form)) {
            console.log("Form is valid, submitting manually");
            form.submit();
          } else {
            console.log("Form validation failed in direct handler");

            // Alert the user
            const errorMsg = "There are validation errors in the form. Click OK to submit anyway or Cancel to review errors.";
            if (confirm(errorMsg)) {
              // Override validation and submit anyway
              console.log("Forcing form submission");

              // Remove all required attributes temporarily
              const requiredFields = form.querySelectorAll("[required]");
              requiredFields.forEach((field) => {
                field.removeAttribute("required");
              });

              // Submit the form
              form.submit();
            }
          }
        }
      } catch (error) {
        console.error("Error in form handler:", error);
        // Last resort direct submission
        if (form && confirm("An error occurred. Try to submit anyway?")) {
          form.submit();
        }
      }
    });
  }
}
