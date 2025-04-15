/**
 * Chat Module for Real-Time Task Application
 * Handles real-time messaging between team members
 */
document.addEventListener("DOMContentLoaded", function () {
  "use strict";

  // Chat Module
  const Chat = {
    // WebSocket connection
    socket: null,

    // Current user and chat info
    currentUser: null,
    activeConversation: null,
    conversations: [],

    // Initialize chat functionality
    init: function () {
      // Get current user info from page data
      this.currentUser = JSON.parse(document.getElementById("current-user-data")?.dataset.user || "{}");

      if (!this.currentUser.id) {
        console.error("User data not available for chat");
        return;
      }

      console.log("Chat module initialized for user:", this.currentUser.name);
      this.setupWebSocket();
      this.loadConversations();
      this.setupEventListeners();
    },

    // Set up WebSocket connection
    setupWebSocket: function () {
      const wsProtocol = window.location.protocol === "https:" ? "wss://" : "ws://";
      const wsUrl = `${wsProtocol}${window.location.host}/ws/chat/${this.currentUser.id}`;

      this.socket = new WebSocket(wsUrl);

      this.socket.onopen = () => {
        console.log("WebSocket connection established");
        this.setStatus("online");
      };

      this.socket.onmessage = (event) => {
        this.handleIncomingMessage(JSON.parse(event.data));
      };

      this.socket.onclose = () => {
        console.log("WebSocket connection closed");
        // Try to reconnect after a delay
        setTimeout(() => {
          this.setupWebSocket();
        }, 3000);
      };

      this.socket.onerror = (error) => {
        console.error("WebSocket error:", error);
      };
    },

    // Set user online/offline status
    setStatus: function (status) {
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        this.socket.send(
          JSON.stringify({
            type: "STATUS",
            senderId: this.currentUser.id,
            status: status,
          })
        );
      }
    },

    // Load user conversations
    loadConversations: function () {
      fetch(`/api/chat/conversations`)
        .then((response) => response.json())
        .then((data) => {
          this.conversations = data;
          this.renderConversationList();

          // Load the first conversation if available
          if (data.length > 0) {
            this.openConversation(data[0].id);
          }
        })
        .catch((error) => {
          console.error("Error loading conversations:", error);
        });
    },

    // Set up event listeners for chat UI
    setupEventListeners: function () {
      // Send message on form submit
      const messageForm = document.getElementById("message-form");
      if (messageForm) {
        messageForm.addEventListener("submit", (event) => {
          event.preventDefault();
          this.sendMessage();
        });
      }

      // Send message on Ctrl+Enter in textarea
      const messageInput = document.getElementById("message-input");
      if (messageInput) {
        messageInput.addEventListener("keydown", (event) => {
          if (event.ctrlKey && event.key === "Enter") {
            event.preventDefault();
            this.sendMessage();
          }
        });
      }

      // New conversation button
      const newConversationBtn = document.getElementById("new-conversation-btn");
      if (newConversationBtn) {
        newConversationBtn.addEventListener("click", () => {
          this.showNewConversationModal();
        });
      }

      // Handle window beforeunload to set offline status
      window.addEventListener("beforeunload", () => {
        this.setStatus("offline");
      });
    },

    // Render conversation list in sidebar
    renderConversationList: function () {
      const conversationsList = document.getElementById("conversations-list");
      if (!conversationsList) return;

      let html = "";
      this.conversations.forEach((conversation) => {
        const unreadBadge = conversation.unreadCount > 0 ? `<span class="unread-badge">${conversation.unreadCount}</span>` : "";

        const activeClass = this.activeConversation && this.activeConversation.id === conversation.id ? "active" : "";

        html += `
                    <div class="conversation-item ${activeClass}" data-conversation-id="${conversation.id}">
                        ${
                          conversation.isGroup
                            ? `<div class="group-avatar">${conversation.name.charAt(0)}</div>`
                            : `<img src="${conversation.participants[0].avatar}" alt="Avatar" class="user-avatar">`
                        }
                        <div class="conversation-info">
                            <div class="conversation-name">${conversation.name}</div>
                            <div class="last-message">${conversation.lastMessage || "No messages yet"}</div>
                        </div>
                        ${unreadBadge}
                    </div>
                `;
      });

      conversationsList.innerHTML = html;

      // Add click event to conversation items
      document.querySelectorAll(".conversation-item").forEach((item) => {
        item.addEventListener("click", () => {
          const conversationId = item.dataset.conversationId;
          this.openConversation(conversationId);
        });
      });
    },

    // Open a specific conversation
    openConversation: function (conversationId) {
      fetch(`/api/chat/conversations/${conversationId}`)
        .then((response) => response.json())
        .then((data) => {
          this.activeConversation = data;
          this.renderConversationHeader();
          this.loadMessages(conversationId);

          // Mark conversation as read
          if (data.unreadCount > 0) {
            this.markAsRead(conversationId);
          }

          // Update active conversation in the list
          document.querySelectorAll(".conversation-item").forEach((item) => {
            item.classList.remove("active");
            if (item.dataset.conversationId === conversationId) {
              item.classList.add("active");
              item.querySelector(".unread-badge")?.remove();
            }
          });
        })
        .catch((error) => {
          console.error("Error opening conversation:", error);
        });
    },

    // Load messages for the current conversation
    loadMessages: function (conversationId) {
      const messagesContainer = document.getElementById("messages-container");
      messagesContainer.innerHTML = '<div class="loading">Loading messages...</div>';

      fetch(`/api/chat/conversations/${conversationId}/messages`)
        .then((response) => response.json())
        .then((data) => {
          this.renderMessages(data);
        })
        .catch((error) => {
          console.error("Error loading messages:", error);
          messagesContainer.innerHTML = '<div class="error">Failed to load messages.</div>';
        });
    },

    // Render conversation header with participant info
    renderConversationHeader: function () {
      const header = document.getElementById("conversation-header");
      if (!header || !this.activeConversation) return;

      let participantsText = "";
      if (this.activeConversation.isGroup) {
        const participantCount = this.activeConversation.participants.length;
        participantsText = `${participantCount} members`;
      } else {
        const participant = this.activeConversation.participants[0];
        participantsText = participant.online ? "Online" : "Offline";
      }

      header.innerHTML = `
                <div class="conversation-title">
                    <h3>${this.activeConversation.name}</h3>
                    <span class="participant-status">${participantsText}</span>
                </div>
                <div class="conversation-actions">
                    <button class="btn btn-icon" id="conversation-info-btn">
                        <i class="fas fa-info-circle"></i>
                    </button>
                </div>
            `;

      // Add event listener to info button
      document.getElementById("conversation-info-btn")?.addEventListener("click", () => {
        this.showConversationInfo();
      });
    },

    // Render messages in the conversation
    renderMessages: function (messages) {
      const messagesContainer = document.getElementById("messages-container");
      if (!messagesContainer) return;

      if (messages.length === 0) {
        messagesContainer.innerHTML = '<div class="no-messages">No messages yet. Start the conversation!</div>';
        return;
      }

      let html = "";
      let lastDate = null;

      messages.forEach((message) => {
        // Add date separator if it's a new day
        const messageDate = new Date(message.timestamp).toLocaleDateString();
        if (lastDate !== messageDate) {
          html += `<div class="date-separator">${messageDate}</div>`;
          lastDate = messageDate;
        }

        const isOwnMessage = message.senderId === this.currentUser.id;
        const messageClass = isOwnMessage ? "own-message" : "other-message";
        const timeString = new Date(message.timestamp).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });

        html += `
                    <div class="message ${messageClass}">
                        ${!isOwnMessage && this.activeConversation.isGroup ? `<div class="message-sender">${message.senderName}</div>` : ""}
                        <div class="message-content">
                            ${message.content}
                            <div class="message-time">${timeString}</div>
                        </div>
                    </div>
                `;
      });

      messagesContainer.innerHTML = html;

      // Scroll to bottom
      messagesContainer.scrollTop = messagesContainer.scrollHeight;
    },

    // Send a chat message
    sendMessage: function () {
      const messageInput = document.getElementById("message-input");
      const content = messageInput.value.trim();

      if (!content || !this.activeConversation) return;

      const message = {
        type: "MESSAGE",
        conversationId: this.activeConversation.id,
        senderId: this.currentUser.id,
        senderName: this.currentUser.name,
        content: content,
        timestamp: new Date().toISOString(),
      };

      // Clear input first for better UX
      messageInput.value = "";

      // Send via WebSocket
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        this.socket.send(JSON.stringify(message));

        // Optimistically add message to UI
        const messagesContainer = document.getElementById("messages-container");
        const timeString = new Date(message.timestamp).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });

        const messageHTML = `
                    <div class="message own-message">
                        <div class="message-content">
                            ${message.content}
                            <div class="message-time">${timeString}</div>
                        </div>
                    </div>
                `;

        messagesContainer.insertAdjacentHTML("beforeend", messageHTML);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
      } else {
        console.error("WebSocket not connected");
        alert("Connection lost. Please refresh the page.");
      }
    },

    // Handle incoming WebSocket messages
    handleIncomingMessage: function (data) {
      switch (data.type) {
        case "MESSAGE":
          this.handleNewMessage(data);
          break;
        case "STATUS":
          this.handleStatusUpdate(data);
          break;
        case "TYPING":
          this.handleTypingIndicator(data);
          break;
        default:
          console.log("Unknown message type:", data.type);
      }
    },

    // Handle new incoming message
    handleNewMessage: function (message) {
      // If message is for the active conversation, add it to the UI
      if (this.activeConversation && message.conversationId === this.activeConversation.id) {
        const messagesContainer = document.getElementById("messages-container");
        const timeString = new Date(message.timestamp).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });

        const messageHTML = `
                    <div class="message other-message">
                        ${this.activeConversation.isGroup ? `<div class="message-sender">${message.senderName}</div>` : ""}
                        <div class="message-content">
                            ${message.content}
                            <div class="message-time">${timeString}</div>
                        </div>
                    </div>
                `;

        messagesContainer.insertAdjacentHTML("beforeend", messageHTML);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;

        // Mark as read since user is viewing this conversation
        this.markAsRead(message.conversationId);
      } else {
        // Update the conversations list to show new message
        this.conversations.forEach((conv) => {
          if (conv.id === message.conversationId) {
            conv.lastMessage = message.content;
            conv.unreadCount = (conv.unreadCount || 0) + 1;
          }
        });

        this.renderConversationList();

        // Show notification
        this.showNotification(message);
      }
    },

    // Handle user status updates
    handleStatusUpdate: function (data) {
      // Update status in conversations list
      this.conversations.forEach((conv) => {
        if (!conv.isGroup) {
          conv.participants.forEach((participant) => {
            if (participant.id === data.senderId) {
              participant.online = data.status === "online";
            }
          });
        }
      });

      // Update active conversation header if needed
      if (this.activeConversation && !this.activeConversation.isGroup) {
        this.activeConversation.participants.forEach((participant) => {
          if (participant.id === data.senderId) {
            participant.online = data.status === "online";
            this.renderConversationHeader();
          }
        });
      }
    },

    // Handle typing indicators
    handleTypingIndicator: function (data) {
      const typingIndicator = document.getElementById("typing-indicator");

      if (this.activeConversation && data.conversationId === this.activeConversation.id) {
        if (data.isTyping) {
          typingIndicator.textContent = `${data.senderName} is typing...`;
          typingIndicator.style.display = "block";
        } else {
          typingIndicator.style.display = "none";
        }
      }
    },

    // Mark conversation as read
    markAsRead: function (conversationId) {
      fetch(`/api/chat/conversations/${conversationId}/read`, {
        method: "POST",
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to mark conversation as read");
          }

          // Update local state
          this.conversations.forEach((conv) => {
            if (conv.id === conversationId) {
              conv.unreadCount = 0;
            }
          });
        })
        .catch((error) => {
          console.error("Error marking conversation as read:", error);
        });
    },

    // Show browser notification for new message
    showNotification: function (message) {
      // Check if browser supports notifications
      if (!("Notification" in window)) return;

      // Check if permission is granted
      if (Notification.permission === "granted") {
        this.createNotification(message);
      }
      // Otherwise, ask for permission
      else if (Notification.permission !== "denied") {
        Notification.requestPermission().then((permission) => {
          if (permission === "granted") {
            this.createNotification(message);
          }
        });
      }
    },

    // Create browser notification
    createNotification: function (message) {
      // Find conversation to get the name
      const conversation = this.conversations.find((c) => c.id === message.conversationId);
      if (!conversation) return;

      const notification = new Notification(`New message from ${message.senderName}`, {
        body: message.content,
        icon: "/assets/img/logo-small.png",
      });

      notification.onclick = function () {
        window.focus();
        Chat.openConversation(message.conversationId);
      };
    },

    // Show modal for creating a new conversation
    showNewConversationModal: function () {
      // Implementation for new conversation modal
      console.log("Show new conversation modal");
    },

    // Show conversation info sidebar
    showConversationInfo: function () {
      // Implementation for conversation info sidebar
      console.log("Show conversation info");
    },
  };

  // Initialize chat module
  Chat.init();
});
