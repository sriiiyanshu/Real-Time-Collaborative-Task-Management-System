/**
 * Notifications Module for Real-Time Task Application
 * Handles notification display, management, and interaction
 */
document.addEventListener("DOMContentLoaded", function () {
  "use strict";

  // Notifications Module
  const Notifications = {
    // Configuration
    config: {
      maxToastNotifications: 3,
      toastDuration: 5000, // 5 seconds
      soundEnabled: true,
      desktopEnabled: true,
      pollInterval: 30000, // 30 seconds
    },

    // State
    notifications: [],
    unreadCount: 0,
    polling: null,

    // Initialize notifications module
    init: function () {
      console.log("Notifications module initialized");
      this.loadUserPreferences();
      this.setupEventListeners();
      this.requestNotificationPermission();
      this.loadNotifications();
      this.startPolling();
      this.listenForWebSocketEvents();
    },

    // Load user notification preferences
    loadUserPreferences: function () {
      // Try to load from localStorage
      const soundEnabled = localStorage.getItem("notification_sound");
      const desktopEnabled = localStorage.getItem("notification_desktop");

      if (soundEnabled !== null) {
        this.config.soundEnabled = soundEnabled === "true";
      }

      if (desktopEnabled !== null) {
        this.config.desktopEnabled = desktopEnabled === "true";
      }
    },

    // Set up event listeners
    setupEventListeners: function () {
      // Notification toggle button
      const notificationToggle = document.getElementById("notification-toggle");
      if (notificationToggle) {
        notificationToggle.addEventListener("click", () => {
          this.toggleNotificationsPanel();
        });
      }

      // Mark all as read button
      const markAllReadBtn = document.getElementById("mark-all-read");
      if (markAllReadBtn) {
        markAllReadBtn.addEventListener("click", () => {
          this.markAllAsRead();
        });
      }

      // Sound toggle option
      const soundToggle = document.getElementById("notification-sound-toggle");
      if (soundToggle) {
        soundToggle.checked = this.config.soundEnabled;
        soundToggle.addEventListener("change", () => {
          this.config.soundEnabled = soundToggle.checked;
          localStorage.setItem("notification_sound", soundToggle.checked);
        });
      }

      // Desktop notifications toggle
      const desktopToggle = document.getElementById("notification-desktop-toggle");
      if (desktopToggle) {
        desktopToggle.checked = this.config.desktopEnabled;
        desktopToggle.addEventListener("change", () => {
          this.config.desktopEnabled = desktopToggle.checked;
          localStorage.setItem("notification_desktop", desktopToggle.checked);

          if (desktopToggle.checked) {
            this.requestNotificationPermission();
          }
        });
      }

      // Settings button
      const settingsBtn = document.getElementById("notification-settings-btn");
      if (settingsBtn) {
        settingsBtn.addEventListener("click", () => {
          this.toggleSettingsPanel();
        });
      }

      // Document click for closing dropdown
      document.addEventListener("click", (event) => {
        const panel = document.getElementById("notifications-panel");
        const toggle = document.getElementById("notification-toggle");

        if (panel && !panel.contains(event.target) && toggle && !toggle.contains(event.target) && panel.classList.contains("show")) {
          panel.classList.remove("show");
        }
      });
    },

    // Request permission for browser notifications
    requestNotificationPermission: function () {
      if (!("Notification" in window)) {
        console.warn("This browser does not support desktop notifications");
        return;
      }

      if (Notification.permission !== "granted" && Notification.permission !== "denied") {
        Notification.requestPermission().then((permission) => {
          if (permission === "granted") {
            console.log("Notification permission granted");
          }
        });
      }
    },

    // Load notifications from server
    loadNotifications: function () {
      fetch("/api/notifications")
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to load notifications");
          }
          return response.json();
        })
        .then((data) => {
          this.notifications = data.notifications || [];
          this.unreadCount = data.unreadCount || 0;
          this.updateNotificationBadge();
          this.renderNotifications();
        })
        .catch((error) => {
          console.error("Error loading notifications:", error);
        });
    },

    // Start polling for new notifications
    startPolling: function () {
      if (this.polling) {
        clearInterval(this.polling);
      }

      this.polling = setInterval(() => {
        this.checkForNewNotifications();
      }, this.config.pollInterval);
    },

    // Stop polling for new notifications
    stopPolling: function () {
      if (this.polling) {
        clearInterval(this.polling);
        this.polling = null;
      }
    },

    // Check for new notifications
    checkForNewNotifications: function () {
      // Only check if we're not using WebSockets or as a fallback
      if (window.WebSocketManager && window.WebSocketManager.isConnected()) {
        return; // WebSocket will handle real-time notifications
      }

      // Get timestamp of newest notification
      const latestTimestamp = this.notifications.length > 0 ? this.notifications[0].timestamp : null;

      fetch(`/api/notifications/check${latestTimestamp ? "?since=" + latestTimestamp : ""}`)
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to check for notifications");
          }
          return response.json();
        })
        .then((data) => {
          if (data.newCount > 0) {
            this.loadNotifications();
          }
        })
        .catch((error) => {
          console.error("Error checking notifications:", error);
        });
    },

    // Listen for WebSocket notification events
    listenForWebSocketEvents: function () {
      if (!window.WebSocketManager) return;

      window.addEventListener("DOMContentLoaded", () => {
        if (window.WebSocketManager) {
          // Register for notification events
          window.WebSocketManager.registerHandler("NOTIFICATION", (data) => {
            this.handleNewNotification(data);
          });
        }
      });
    },

    // Handle a new notification from WebSocket
    handleNewNotification: function (data) {
      // Check if this notification already exists
      const exists = this.notifications.some((n) => n.id === data.id);
      if (exists) return;

      // Add to notifications list
      this.notifications.unshift(data);
      this.unreadCount++;

      // Update UI
      this.updateNotificationBadge();
      this.renderNotifications();

      // Show toast notification
      this.showToastNotification(data);

      // Show desktop notification
      if (this.config.desktopEnabled) {
        this.showDesktopNotification(data);
      }

      // Play sound if enabled
      if (this.config.soundEnabled) {
        this.playNotificationSound();
      }
    },

    // Toggle notifications panel visibility
    toggleNotificationsPanel: function () {
      const panel = document.getElementById("notifications-panel");
      if (!panel) return;

      panel.classList.toggle("show");

      // If showing panel, mark notifications as seen
      if (panel.classList.contains("show")) {
        this.markNotificationsAsSeen();
      }
    },

    // Toggle notification settings panel
    toggleSettingsPanel: function () {
      const settingsPanel = document.getElementById("notification-settings-panel");
      if (!settingsPanel) return;

      settingsPanel.classList.toggle("show");
    },

    // Mark notifications as seen (not necessarily read)
    markNotificationsAsSeen: function () {
      const unseenIds = this.notifications.filter((notification) => !notification.seen).map((notification) => notification.id);

      if (unseenIds.length === 0) return;

      // Mark as seen locally
      this.notifications.forEach((notification) => {
        if (unseenIds.includes(notification.id)) {
          notification.seen = true;
        }
      });

      // Send to server
      fetch("/api/notifications/seen", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ ids: unseenIds }),
      }).catch((error) => {
        console.error("Error marking notifications as seen:", error);
      });
    },

    // Mark a single notification as read
    markAsRead: function (id) {
      // Update local state
      const notification = this.notifications.find((n) => n.id === id);
      if (notification && !notification.read) {
        notification.read = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
        this.updateNotificationBadge();
        this.renderNotifications();
      }

      // Send to server
      fetch(`/api/notifications/${id}/read`, {
        method: "PUT",
      }).catch((error) => {
        console.error("Error marking notification as read:", error);
      });
    },

    // Mark all notifications as read
    markAllAsRead: function () {
      // Update local state
      const unreadNotifications = this.notifications.filter((n) => !n.read);
      if (unreadNotifications.length === 0) return;

      unreadNotifications.forEach((notification) => {
        notification.read = true;
      });

      this.unreadCount = 0;
      this.updateNotificationBadge();
      this.renderNotifications();

      // Send to server
      fetch("/api/notifications/read-all", {
        method: "PUT",
      }).catch((error) => {
        console.error("Error marking all notifications as read:", error);
      });
    },

    // Delete a notification
    deleteNotification: function (id) {
      // Update local state
      const index = this.notifications.findIndex((n) => n.id === id);
      if (index === -1) return;

      const wasUnread = !this.notifications[index].read;
      this.notifications.splice(index, 1);

      if (wasUnread) {
        this.unreadCount = Math.max(0, this.unreadCount - 1);
        this.updateNotificationBadge();
      }

      this.renderNotifications();

      // Send to server
      fetch(`/api/notifications/${id}`, {
        method: "DELETE",
      }).catch((error) => {
        console.error("Error deleting notification:", error);
      });
    },

    // Update notification counter badge
    updateNotificationBadge: function () {
      const badge = document.getElementById("notification-badge");
      if (!badge) return;

      if (this.unreadCount > 0) {
        badge.textContent = this.unreadCount > 99 ? "99+" : this.unreadCount;
        badge.classList.add("show");
      } else {
        badge.classList.remove("show");
      }
    },

    // Render notifications in the panel
    renderNotifications: function () {
      const container = document.getElementById("notifications-list");
      if (!container) return;

      if (this.notifications.length === 0) {
        container.innerHTML = `
                    <div class="empty-notifications">
                        <div class="empty-icon"><i class="fas fa-bell-slash"></i></div>
                        <div class="empty-message">No notifications</div>
                    </div>
                `;
        return;
      }

      let html = "";

      this.notifications.forEach((notification) => {
        const timeAgo = this.formatTimeAgo(new Date(notification.timestamp));
        const unreadClass = notification.read ? "" : "unread";

        html += `
                    <div class="notification-item ${unreadClass}" data-id="${notification.id}">
                        <div class="notification-icon ${notification.type}">
                            ${this.getNotificationIcon(notification.type)}
                        </div>
                        <div class="notification-content">
                            <div class="notification-header">
                                <span class="notification-title">${notification.title}</span>
                                <span class="notification-time">${timeAgo}</span>
                            </div>
                            <div class="notification-message">${notification.message}</div>
                        </div>
                        <div class="notification-actions">
                            <button class="btn-icon read-notification" title="Mark as read">
                                <i class="fas fa-check"></i>
                            </button>
                            <button class="btn-icon delete-notification" title="Delete">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                `;
      });

      container.innerHTML = html;

      // Add event listeners to notification actions
      container.querySelectorAll(".read-notification").forEach((button) => {
        button.addEventListener("click", (event) => {
          event.stopPropagation();
          const notificationItem = button.closest(".notification-item");
          const id = notificationItem?.dataset.id;
          if (id) {
            this.markAsRead(id);
          }
        });
      });

      container.querySelectorAll(".delete-notification").forEach((button) => {
        button.addEventListener("click", (event) => {
          event.stopPropagation();
          const notificationItem = button.closest(".notification-item");
          const id = notificationItem?.dataset.id;
          if (id) {
            this.deleteNotification(id);
          }
        });
      });

      // Add click event to notification items to navigate
      container.querySelectorAll(".notification-item").forEach((item) => {
        item.addEventListener("click", () => {
          const id = item.dataset.id;
          if (id) {
            const notification = this.notifications.find((n) => n.id === id);
            if (notification) {
              this.markAsRead(id);
              this.navigateToNotification(notification);
            }
          }
        });
      });
    },

    // Show a toast notification
    showToastNotification: function (data) {
      // Create toast container if it doesn't exist
      let toastContainer = document.getElementById("toast-container");
      if (!toastContainer) {
        toastContainer = document.createElement("div");
        toastContainer.id = "toast-container";
        document.body.appendChild(toastContainer);
      }

      // Limit number of visible toasts
      const existingToasts = toastContainer.querySelectorAll(".toast");
      if (existingToasts.length >= this.config.maxToastNotifications) {
        toastContainer.removeChild(existingToasts[0]);
      }

      // Create new toast
      const toast = document.createElement("div");
      toast.className = `toast ${data.type || "default"}`;

      toast.innerHTML = `
                <div class="toast-header">
                    <div class="toast-icon">${this.getNotificationIcon(data.type)}</div>
                    <div class="toast-title">${data.title}</div>
                    <button class="toast-close">×</button>
                </div>
                <div class="toast-body">
                    <div class="toast-message">${data.message}</div>
                </div>
            `;

      // Add to container
      toastContainer.appendChild(toast);

      // Add event listeners
      const closeBtn = toast.querySelector(".toast-close");
      if (closeBtn) {
        closeBtn.addEventListener("click", () => {
          this.removeToast(toast);
        });
      }

      toast.addEventListener("click", (event) => {
        if (!event.target.matches(".toast-close")) {
          if (data.id) {
            this.markAsRead(data.id);
          }
          this.navigateToNotification(data);
        }
      });

      // Auto remove after duration
      setTimeout(() => {
        this.removeToast(toast);
      }, this.config.toastDuration);
    },

    // Remove a toast notification with animation
    removeToast: function (toast) {
      toast.classList.add("removing");
      setTimeout(() => {
        toast.parentNode?.removeChild(toast);
      }, 300); // Match CSS animation duration
    },

    // Show a browser desktop notification
    showDesktopNotification: function (data) {
      if (!("Notification" in window) || Notification.permission !== "granted") {
        return;
      }

      const options = {
        body: data.message,
        icon: "/assets/img/logo-icon.png",
        badge: "/assets/img/notification-badge.png",
        tag: `rtta-notification-${data.id || Date.now()}`,
      };

      const notification = new Notification(data.title, options);

      notification.onclick = () => {
        window.focus();
        notification.close();
        if (data.id) {
          this.markAsRead(data.id);
        }
        this.navigateToNotification(data);
      };
    },

    // Play notification sound
    playNotificationSound: function () {
      const audio = new Audio("/assets/audio/notification.mp3");
      audio.play().catch((error) => {
        console.warn("Failed to play notification sound:", error);
      });
    },

    // Navigate to the related resource
    navigateToNotification: function (notification) {
      if (!notification.link) return;

      // If it's an internal link, use history API
      if (notification.link.startsWith("/")) {
        window.location.href = notification.link;
      } else {
        // External link, open in new tab
        window.open(notification.link, "_blank");
      }
    },

    // Get icon HTML for notification type
    getNotificationIcon: function (type) {
      switch (type) {
        case "task":
          return '<i class="fas fa-tasks"></i>';
        case "comment":
          return '<i class="fas fa-comment"></i>';
        case "mention":
          return '<i class="fas fa-at"></i>';
        case "assignment":
          return '<i class="fas fa-user-check"></i>';
        case "project":
          return '<i class="fas fa-project-diagram"></i>';
        case "deadline":
          return '<i class="fas fa-calendar-alt"></i>';
        case "system":
          return '<i class="fas fa-cog"></i>';
        case "success":
          return '<i class="fas fa-check-circle"></i>';
        case "error":
          return '<i class="fas fa-exclamation-circle"></i>';
        case "warning":
          return '<i class="fas fa-exclamation-triangle"></i>';
        default:
          return '<i class="fas fa-bell"></i>';
      }
    },

    // Format timestamp to relative time string
    formatTimeAgo: function (date) {
      const now = new Date();
      const seconds = Math.floor((now - date) / 1000);

      // Less than a minute
      if (seconds < 60) {
        return "Just now";
      }

      // Less than an hour
      const minutes = Math.floor(seconds / 60);
      if (minutes < 60) {
        return `${minutes} minute${minutes !== 1 ? "s" : ""} ago`;
      }

      // Less than a day
      const hours = Math.floor(minutes / 60);
      if (hours < 24) {
        return `${hours} hour${hours !== 1 ? "s" : ""} ago`;
      }

      // Less than a week
      const days = Math.floor(hours / 24);
      if (days < 7) {
        return `${days} day${days !== 1 ? "s" : ""} ago`;
      }

      // Format as date
      const options = { year: "numeric", month: "short", day: "numeric" };
      return date.toLocaleDateString(undefined, options);
    },
  };

  // Initialize notifications module
  Notifications.init();

  // Make it globally available
  window.Notifications = Notifications;
});
