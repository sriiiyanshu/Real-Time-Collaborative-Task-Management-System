/**
 * WebSocket Module for Real-Time Task Application
 * Handles real-time communication and event handling
 */
document.addEventListener("DOMContentLoaded", function () {
  "use strict";

  // WebSocket Module
  const WebSocketManager = {
    // WebSocket connection
    socket: null,

    // Connection status
    connected: false,

    // Reconnection parameters
    reconnectAttempts: 0,
    maxReconnectAttempts: 10,
    reconnectInterval: 2000, // Start with 2 seconds
    maxReconnectInterval: 30000, // Max 30 seconds

    // Message handlers by type
    handlers: {},

    // Event listener callbacks
    eventListeners: {
      connect: [],
      disconnect: [],
      error: [],
      message: [],
    },

    // Initialize WebSocket connection
    init: function () {
      console.log("WebSocket module initialized");

      // Register default message handlers
      this.registerDefaultHandlers();

      // Establish connection
      this.connect();

      // Handle page visibility changes
      document.addEventListener("visibilitychange", () => {
        if (document.visibilityState === "visible") {
          if (!this.connected) {
            this.connect();
          }
        }
      });

      // Handle before unload to close connection gracefully
      window.addEventListener("beforeunload", () => {
        this.disconnect();
      });
    },

    // Connect to WebSocket server
    connect: function () {
      // Get current user ID if available
      const userId = document.getElementById("current-user-data")?.dataset.userId;

      // Determine WebSocket protocol
      const wsProtocol = window.location.protocol === "https:" ? "wss://" : "ws://";

      // Build WebSocket URL
      let wsUrl = `${wsProtocol}${window.location.host}/ws`;

      // Add user ID if available
      if (userId) {
        wsUrl += `/user/${userId}`;
      }

      try {
        this.socket = new WebSocket(wsUrl);
        this.setupSocketEventListeners();
      } catch (error) {
        console.error("Error creating WebSocket connection:", error);
        this.triggerEvent("error", { message: "Failed to create WebSocket connection", error });
        this.scheduleReconnect();
      }
    },

    // Set up WebSocket event listeners
    setupSocketEventListeners: function () {
      if (!this.socket) return;

      // Connection opened
      this.socket.addEventListener("open", (event) => {
        console.log("WebSocket connection established");
        this.connected = true;
        this.reconnectAttempts = 0;
        this.reconnectInterval = 2000;

        // Send authentication if needed
        this.authenticate();

        // Trigger connect event
        this.triggerEvent("connect", { timestamp: new Date() });

        // Update UI to show connected status
        this.updateConnectionStatus(true);
      });

      // Connection closed
      this.socket.addEventListener("close", (event) => {
        console.log(`WebSocket connection closed. Code: ${event.code}, Reason: ${event.reason}`);
        this.connected = false;

        // Trigger disconnect event
        this.triggerEvent("disconnect", {
          code: event.code,
          reason: event.reason,
          wasClean: event.wasClean,
        });

        // Update UI to show disconnected status
        this.updateConnectionStatus(false);

        // Try to reconnect if not deliberately closed
        if (event.code !== 1000) {
          this.scheduleReconnect();
        }
      });

      // Error occurred
      this.socket.addEventListener("error", (event) => {
        console.error("WebSocket error:", event);

        // Trigger error event
        this.triggerEvent("error", { message: "WebSocket connection error", event });

        // Update UI to show error status
        this.updateConnectionStatus(false, true);
      });

      // Message received
      this.socket.addEventListener("message", (event) => {
        try {
          const data = JSON.parse(event.data);

          // Log for debugging
          console.debug("WebSocket message received:", data);

          // Trigger message event for all listeners
          this.triggerEvent("message", data);

          // Process message with appropriate handler
          this.handleMessage(data);
        } catch (error) {
          console.error("Error processing WebSocket message:", error, event.data);
        }
      });
    },

    // Authenticate with WebSocket server if needed
    authenticate: function () {
      if (!this.connected) return;

      // Get authentication token (e.g., from localStorage)
      const authToken = localStorage.getItem("authToken");

      if (authToken) {
        this.send({
          type: "AUTH",
          token: authToken,
        });
      }
    },

    // Send message through WebSocket
    send: function (message) {
      if (!this.connected || !this.socket) {
        console.warn("Cannot send message, WebSocket not connected");
        return false;
      }

      try {
        // Convert message object to JSON string
        const messageStr = typeof message === "string" ? message : JSON.stringify(message);

        // Send the message
        this.socket.send(messageStr);
        return true;
      } catch (error) {
        console.error("Error sending WebSocket message:", error);
        return false;
      }
    },

    // Disconnect WebSocket
    disconnect: function () {
      if (this.socket && this.connected) {
        this.socket.close(1000, "User navigated away");
        this.connected = false;
      }
    },

    // Schedule reconnection attempt
    scheduleReconnect: function () {
      if (this.reconnectAttempts >= this.maxReconnectAttempts) {
        console.warn(`Maximum reconnect attempts (${this.maxReconnectAttempts}) reached`);
        this.updateConnectionStatus(false, true);
        return;
      }

      // Exponential backoff with jitter
      const jitter = Math.random() * 0.5 + 0.75; // Random between 0.75 and 1.25
      const delay = Math.min(this.reconnectInterval * jitter, this.maxReconnectInterval);

      console.log(`Scheduling WebSocket reconnect attempt in ${Math.round(delay / 1000)}s`);

      // Show reconnecting status in UI
      this.updateConnectionStatus(false, false, true);

      // Schedule reconnection
      setTimeout(() => {
        this.reconnectAttempts++;
        this.reconnectInterval = Math.min(this.reconnectInterval * 1.5, this.maxReconnectInterval);
        this.connect();
      }, delay);
    },

    // Update connection status in UI
    updateConnectionStatus: function (connected, error = false, reconnecting = false) {
      // Find status indicator element
      const statusIndicator = document.getElementById("connection-status");
      if (!statusIndicator) return;

      // Update element class and text
      statusIndicator.classList.remove("connected", "disconnected", "reconnecting", "error");

      if (connected) {
        statusIndicator.classList.add("connected");
        statusIndicator.innerHTML = '<i class="fas fa-circle"></i> Connected';
      } else if (reconnecting) {
        statusIndicator.classList.add("reconnecting");
        statusIndicator.innerHTML = '<i class="fas fa-sync fa-spin"></i> Reconnecting...';
      } else if (error) {
        statusIndicator.classList.add("error");
        statusIndicator.innerHTML = '<i class="fas fa-exclamation-circle"></i> Connection Error';
      } else {
        statusIndicator.classList.add("disconnected");
        statusIndicator.innerHTML = '<i class="fas fa-circle"></i> Disconnected';
      }
    },

    // Handle incoming message by type
    handleMessage: function (data) {
      if (!data || !data.type) {
        console.warn("Received WebSocket message without type:", data);
        return;
      }

      // Find handler for message type
      const handler = this.handlers[data.type];

      if (handler && typeof handler === "function") {
        try {
          handler(data);
        } catch (error) {
          console.error(`Error in message handler for type '${data.type}':`, error);
        }
      } else {
        console.debug(`No handler registered for message type: ${data.type}`);
      }
    },

    // Register default message handlers
    registerDefaultHandlers: function () {
      // Handle ping messages to keep connection alive
      this.registerHandler("PING", (data) => {
        this.send({ type: "PONG", timestamp: new Date().toISOString() });
      });

      // Handle auth responses
      this.registerHandler("AUTH_RESPONSE", (data) => {
        if (data.success) {
          console.log("WebSocket authentication successful");
        } else {
          console.error("WebSocket authentication failed:", data.message);

          // Try to reauthenticate if token might be expired
          if (data.reason === "TOKEN_EXPIRED") {
            this.refreshAuthToken();
          }
        }
      });

      // Handle user notifications
      this.registerHandler("NOTIFICATION", (data) => {
        // Pass to notification system if available
        if (window.Notifications) {
          window.Notifications.handleNewNotification(data);
        }
      });

      // Handle task updates
      this.registerHandler("TASK_UPDATE", (data) => {
        // Dispatch custom event for task updates that other modules can listen for
        window.dispatchEvent(new CustomEvent("taskUpdate", { detail: data }));
      });

      // Handle project updates
      this.registerHandler("PROJECT_UPDATE", (data) => {
        // Dispatch custom event for project updates
        window.dispatchEvent(new CustomEvent("projectUpdate", { detail: data }));
      });

      // Handle chat messages
      this.registerHandler("CHAT_MESSAGE", (data) => {
        // Dispatch custom event for chat messages
        window.dispatchEvent(new CustomEvent("chatMessage", { detail: data }));
      });
    },

    // Try to refresh authentication token
    refreshAuthToken: function () {
      fetch("/api/auth/refresh", {
        method: "POST",
        credentials: "include",
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Token refresh failed");
          }
          return response.json();
        })
        .then((data) => {
          if (data.token) {
            localStorage.setItem("authToken", data.token);
            console.log("Auth token refreshed successfully");

            // Reconnect with new token
            if (this.connected) {
              this.disconnect();
            }
            this.connect();
          }
        })
        .catch((error) => {
          console.error("Error refreshing auth token:", error);

          // Redirect to login if token refresh fails
          window.location.href = "/login.jsp?redirectTo=" + encodeURIComponent(window.location.pathname);
        });
    },

    // Register a message handler
    registerHandler: function (type, callback) {
      if (typeof callback !== "function") {
        throw new Error("Handler must be a function");
      }

      this.handlers[type] = callback;
    },

    // Remove a message handler
    removeHandler: function (type) {
      delete this.handlers[type];
    },

    // Add event listener
    addEventListener: function (event, callback) {
      if (!this.eventListeners[event]) {
        this.eventListeners[event] = [];
      }

      this.eventListeners[event].push(callback);

      // Return a function to remove this listener
      return () => {
        this.removeEventListener(event, callback);
      };
    },

    // Remove event listener
    removeEventListener: function (event, callback) {
      if (!this.eventListeners[event]) return;

      this.eventListeners[event] = this.eventListeners[event].filter((listener) => listener !== callback);
    },

    // Trigger event for all listeners
    triggerEvent: function (event, data) {
      if (!this.eventListeners[event]) return;

      this.eventListeners[event].forEach((callback) => {
        try {
          callback(data);
        } catch (error) {
          console.error(`Error in '${event}' event listener:`, error);
        }
      });
    },

    // Get connection status
    isConnected: function () {
      return this.connected;
    },
  };

  // Initialize WebSocket manager
  WebSocketManager.init();

  // Make WebSocket manager available globally
  window.WebSocketManager = WebSocketManager;
});
