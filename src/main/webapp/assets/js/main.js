/**
 * Main JavaScript file for Real-Time Task Application
 */

document.addEventListener("DOMContentLoaded", () => {
  // Initialize components
  initSidebar();
  initNotifications();
  initDarkMode();
  initTooltips();
  setupFormValidation();

  // Handle any page-specific initialization
  const currentPage = getCurrentPage();
  initializePage(currentPage);
});

/**
 * Initialize sidebar functionality
 */
function initSidebar() {
  const sidebarToggle = document.querySelector(".sidebar-toggle");
  const sidebar = document.querySelector(".sidebar");
  const mainContent = document.querySelector(".main-content");

  if (sidebarToggle && sidebar) {
    sidebarToggle.addEventListener("click", () => {
      sidebar.classList.toggle("show");
      if (mainContent) {
        mainContent.classList.toggle("shifted");
      }
    });

    // Close sidebar on small screens when clicking outside
    document.addEventListener("click", (event) => {
      const isSmallScreen = window.innerWidth < 768;
      const clickedOutsideSidebar = !sidebar.contains(event.target) && event.target !== sidebarToggle;

      if (isSmallScreen && clickedOutsideSidebar && sidebar.classList.contains("show")) {
        sidebar.classList.remove("show");
        if (mainContent) {
          mainContent.classList.remove("shifted");
        }
      }
    });
  }
}

/**
 * Initialize notifications system
 */
function initNotifications() {
  const notificationBell = document.querySelector(".notification-bell");
  const notificationDropdown = document.querySelector(".notification-dropdown");

  if (notificationBell && notificationDropdown) {
    notificationBell.addEventListener("click", (event) => {
      event.preventDefault();
      event.stopPropagation();
      notificationDropdown.classList.toggle("show");

      if (notificationDropdown.classList.contains("show")) {
        fetchNotifications();
      }
    });

    // Close notifications dropdown when clicking outside
    document.addEventListener("click", (event) => {
      if (notificationDropdown && !notificationDropdown.contains(event.target) && event.target !== notificationBell) {
        notificationDropdown.classList.remove("show");
      }
    });
  }
}

/**
 * Fetch notifications from server
 */
function fetchNotifications() {
  const notificationList = document.querySelector(".notification-list");

  if (notificationList) {
    // Show loading state
    notificationList.innerHTML = '<div class="notification-loading">Loading notifications...</div>';

    // Fetch notifications from server
    fetch("/notifications/unread")
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to fetch notifications");
        }
        return response.json();
      })
      .then((data) => {
        renderNotifications(data, notificationList);
      })
      .catch((error) => {
        console.error("Error fetching notifications:", error);
        notificationList.innerHTML = '<div class="notification-error">Could not load notifications</div>';
      });
  }
}

/**
 * Render notifications in the dropdown
 */
function renderNotifications(notifications, container) {
  if (!notifications || notifications.length === 0) {
    container.innerHTML = '<div class="notification-empty">No new notifications</div>';
    return;
  }

  container.innerHTML = "";

  notifications.forEach((notification) => {
    const notificationItem = document.createElement("div");
    notificationItem.classList.add("notification-item");

    // Add appropriate icon based on notification type
    let iconClass = "notification-icon";
    switch (notification.type) {
      case "task":
        iconClass += " task-icon";
        break;
      case "message":
        iconClass += " message-icon";
        break;
      case "alert":
        iconClass += " alert-icon";
        break;
      default:
        iconClass += " info-icon";
    }

    notificationItem.innerHTML = `
            <div class="${iconClass}"></div>
            <div class="notification-content">
                <div class="notification-title">${notification.title}</div>
                <div class="notification-message">${notification.message}</div>
                <div class="notification-time">${formatTimeAgo(notification.timestamp)}</div>
            </div>
            <div class="notification-actions">
                <button class="btn btn-sm mark-read" data-id="${notification.id}">Mark Read</button>
            </div>
        `;

    container.appendChild(notificationItem);
  });

  // Add event listeners for mark as read buttons
  const markReadButtons = container.querySelectorAll(".mark-read");
  markReadButtons.forEach((button) => {
    button.addEventListener("click", function (event) {
      event.stopPropagation();
      markNotificationAsRead(this.dataset.id);
      this.closest(".notification-item").classList.add("read");
    });
  });
}

/**
 * Mark a notification as read
 */
function markNotificationAsRead(notificationId) {
  fetch(`/notifications/${notificationId}/read`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to mark notification as read");
      }
      updateNotificationCounter(-1);
      return response.json();
    })
    .catch((error) => {
      console.error("Error marking notification as read:", error);
    });
}

/**
 * Update notification counter
 */
function updateNotificationCounter(change) {
  const counter = document.querySelector(".notification-counter");
  if (counter) {
    const currentCount = parseInt(counter.textContent);
    const newCount = Math.max(0, currentCount + change);

    counter.textContent = newCount;

    if (newCount === 0) {
      counter.classList.add("hidden");
    } else {
      counter.classList.remove("hidden");
    }
  }
}

/**
 * Format time ago from timestamp
 */
function formatTimeAgo(timestamp) {
  const date = new Date(timestamp);
  const now = new Date();
  const diff = Math.floor((now - date) / 1000); // seconds

  if (diff < 60) {
    return "Just now";
  } else if (diff < 3600) {
    const mins = Math.floor(diff / 60);
    return `${mins} minute${mins > 1 ? "s" : ""} ago`;
  } else if (diff < 86400) {
    const hours = Math.floor(diff / 3600);
    return `${hours} hour${hours > 1 ? "s" : ""} ago`;
  } else if (diff < 604800) {
    const days = Math.floor(diff / 86400);
    return `${days} day${days > 1 ? "s" : ""} ago`;
  } else {
    const options = { year: "numeric", month: "short", day: "numeric" };
    return date.toLocaleDateString(undefined, options);
  }
}

/**
 * Initialize dark mode toggle
 */
function initDarkMode() {
  const darkModeToggle = document.querySelector(".dark-mode-toggle");

  if (darkModeToggle) {
    // Check user preference from local storage
    const isDarkMode = localStorage.getItem("darkMode") === "true";

    // Apply dark mode if needed
    if (isDarkMode) {
      document.body.classList.add("dark-mode");
      darkModeToggle.classList.add("active");
    }

    // Toggle dark mode on click
    darkModeToggle.addEventListener("click", () => {
      document.body.classList.toggle("dark-mode");
      const isDark = document.body.classList.contains("dark-mode");

      darkModeToggle.classList.toggle("active", isDark);
      localStorage.setItem("darkMode", isDark);
    });
  }
}

/**
 * Initialize tooltips
 */
function initTooltips() {
  const tooltipElements = document.querySelectorAll("[data-tooltip]");

  tooltipElements.forEach((element) => {
    element.addEventListener("mouseenter", function () {
      const tooltip = document.createElement("div");
      tooltip.classList.add("tooltip");
      tooltip.textContent = this.getAttribute("data-tooltip");

      document.body.appendChild(tooltip);

      // Position tooltip
      const rect = this.getBoundingClientRect();
      const tooltipRect = tooltip.getBoundingClientRect();

      const top = rect.top - tooltipRect.height - 10;
      const left = rect.left + rect.width / 2 - tooltipRect.width / 2;

      tooltip.style.top = `${top + window.scrollY}px`;
      tooltip.style.left = `${left}px`;

      this.setAttribute("data-tooltip-active", true);
    });

    element.addEventListener("mouseleave", function () {
      const tooltip = document.querySelector(".tooltip");
      if (tooltip) {
        tooltip.remove();
      }
      this.removeAttribute("data-tooltip-active");
    });
  });
}

/**
 * Setup form validation for all forms with data-validate attribute
 */
function setupFormValidation() {
  const forms = document.querySelectorAll("form[data-validate]");

  forms.forEach((form) => {
    form.addEventListener("submit", (event) => {
      const isValid = validateForm(form);

      if (!isValid) {
        event.preventDefault();
      }
    });

    // Add validation on input change
    const inputs = form.querySelectorAll("input, select, textarea");
    inputs.forEach((input) => {
      input.addEventListener("blur", () => {
        validateInput(input);
      });
    });
  });
}

/**
 * Validate an individual form input
 */
function validateInput(input) {
  const validations = input.dataset.validate ? input.dataset.validate.split(" ") : [];
  let isValid = true;
  let errorMessage = "";

  // Clear previous error
  const existingError = input.parentNode.querySelector(".form-error");
  if (existingError) {
    existingError.remove();
  }

  // Don't validate disabled inputs
  if (input.disabled) {
    return true;
  }

  // Required validation
  if (validations.includes("required") && !input.value.trim()) {
    isValid = false;
    errorMessage = "This field is required";
  }

  // Email validation
  if (validations.includes("email") && input.value.trim()) {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(input.value.trim())) {
      isValid = false;
      errorMessage = "Please enter a valid email address";
    }
  }

  // Min length validation
  const minLength = validations.find((v) => v.startsWith("minlength:"));
  if (minLength && input.value.trim()) {
    const length = parseInt(minLength.split(":")[1]);
    if (input.value.length < length) {
      isValid = false;
      errorMessage = `This field must be at least ${length} characters long`;
    }
  }

  // Max length validation
  const maxLength = validations.find((v) => v.startsWith("maxlength:"));
  if (maxLength && input.value.trim()) {
    const length = parseInt(maxLength.split(":")[1]);
    if (input.value.length > length) {
      isValid = false;
      errorMessage = `This field must be no more than ${length} characters long`;
    }
  }

  // Pattern validation
  if (input.pattern && input.value.trim()) {
    const regex = new RegExp(input.pattern);
    if (!regex.test(input.value)) {
      isValid = false;
      errorMessage = input.dataset.errorPattern || "Please enter a valid value";
    }
  }

  // Display error if invalid
  if (!isValid) {
    const errorElement = document.createElement("div");
    errorElement.classList.add("form-error");
    errorElement.textContent = errorMessage;
    input.parentNode.appendChild(errorElement);
    input.classList.add("is-invalid");
  } else {
    input.classList.remove("is-invalid");
  }

  return isValid;
}

/**
 * Validate an entire form
 */
function validateForm(form) {
  const inputs = form.querySelectorAll("input, select, textarea");
  let formValid = true;

  inputs.forEach((input) => {
    const inputValid = validateInput(input);
    if (!inputValid) {
      formValid = false;
    }
  });

  return formValid;
}

/**
 * Get the current page name from URL
 */
function getCurrentPage() {
  const path = window.location.pathname;
  const pageName = path.split("/").pop().split(".")[0];

  if (!pageName || pageName === "") {
    return "index";
  }

  return pageName;
}

/**
 * Initialize page-specific functionality
 */
function initializePage(pageName) {
  switch (pageName) {
    case "dashboard":
      if (typeof initializeDashboard === "function") {
        initializeDashboard();
      }
      break;
    case "tasks":
    case "task":
      if (typeof initializeTaskBoard === "function") {
        initializeTaskBoard();
      }
      break;
    case "chat":
      if (typeof initializeChat === "function") {
        initializeChat();
      }
      break;
    case "analytics":
      if (typeof initializeAnalytics === "function") {
        initializeAnalytics();
      }
      break;
    case "filemanager":
      if (typeof initializeFileManager === "function") {
        initializeFileManager();
      }
      break;
    default:
    // Default initialization - nothing specific needed
  }
}

/**
 * Show a toast notification
 */
function showToast(message, type = "info", duration = 3000) {
  // Create toast container if it doesn't exist
  let toastContainer = document.querySelector(".toast-container");

  if (!toastContainer) {
    toastContainer = document.createElement("div");
    toastContainer.classList.add("toast-container");
    document.body.appendChild(toastContainer);
  }

  // Create toast element
  const toast = document.createElement("div");
  toast.classList.add("toast", `toast-${type}`);
  toast.textContent = message;

  // Add close button
  const closeBtn = document.createElement("button");
  closeBtn.classList.add("toast-close");
  closeBtn.innerHTML = "&times;";
  closeBtn.addEventListener("click", () => {
    toast.classList.add("toast-hiding");
    setTimeout(() => {
      toast.remove();
    }, 300);
  });

  toast.appendChild(closeBtn);
  toastContainer.appendChild(toast);

  // Automatically remove after duration
  setTimeout(() => {
    toast.classList.add("toast-hiding");
    setTimeout(() => {
      toast.remove();
    }, 300);
  }, duration);

  // Animate in
  setTimeout(() => {
    toast.classList.add("toast-visible");
  }, 10);
}

/**
 * Fetch data from server with error handling
 */
async function fetchData(url, options = {}) {
  try {
    const response = await fetch(url, options);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `Request failed with status ${response.status}`);
    }

    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
      return await response.json();
    } else {
      return await response.text();
    }
  } catch (error) {
    console.error(`Error fetching ${url}:`, error);
    showToast(`Error: ${error.message}`, "error");
    throw error;
  }
}

/**
 * Format date for display
 */
function formatDate(dateString, format = "short") {
  const date = new Date(dateString);

  if (isNaN(date)) {
    return "";
  }

  if (format === "short") {
    return date.toLocaleDateString();
  } else if (format === "long") {
    return date.toLocaleDateString(undefined, {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  } else if (format === "time") {
    return date.toLocaleTimeString(undefined, {
      hour: "2-digit",
      minute: "2-digit",
    });
  } else if (format === "datetime") {
    return date.toLocaleString(undefined, {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  return date.toLocaleString();
}
