<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="common/header.jsp">
  <jsp:param name="pageTitle" value="Chat" />
</jsp:include>
<jsp:include page="common/navigation.jsp" />

<div class="container-fluid">
  <div class="row">
    <jsp:include page="common/sidebar.jsp" />
    
    <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <h1 class="h2">Team Chat</h1>
      </div>
      
      <div class="chat-container">
        <div class="chat-sidebar">
          <div class="chat-header">
            <h3>Channels</h3>
            <button class="btn btn-sm btn-outline-primary" id="createChannelBtn">
              <i class="bi bi-plus"></i> New
            </button>
          </div>
          
          <ul class="chat-channels">
            <c:forEach var="channel" items="${channels}">
              <li class="${channel.id == activeChannel.id ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/chat?channel=${channel.id}">
                  # ${channel.name}
                </a>
              </li>
            </c:forEach>
          </ul>
          
          <div class="chat-header mt-4">
            <h3>Direct Messages</h3>
          </div>
          
          <ul class="chat-users">
            <c:forEach var="user" items="${teamMembers}">
              <li class="${user.id == activeUser.id ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/chat?user=${user.id}">
                  <span class="status-indicator ${user.online ? 'online' : 'offline'}"></span>
                  ${user.name}
                </a>
              </li>
            </c:forEach>
          </ul>
        </div>
        
        <div class="chat-content">
          <div class="chat-header-bar">
            <h3>
              <c:choose>
                <c:when test="${not empty activeChannel}">
                  # ${activeChannel.name}
                </c:when>
                <c:otherwise>
                  ${activeUser.name}
                </c:otherwise>
              </c:choose>
            </h3>
          </div>
          
          <div class="message-container" id="messageContainer">
            <c:forEach var="message" items="${messages}">
              <div class="message ${message.senderId == currentUser.id ? 'own-message' : ''}">
                <div class="message-avatar">
                  <img src="${pageContext.request.contextPath}/assets/img/avatars/${message.senderAvatar}" alt="${message.sender}">
                </div>
                <div class="message-content">
                  <div class="message-header">
                    <span class="message-sender">${message.sender}</span>
                    <span class="message-time">${message.formattedTime}</span>
                  </div>
                  <div class="message-body">
                    ${message.content}
                  </div>
                </div>
              </div>
            </c:forEach>
          </div>
          
          <div class="message-input">
            <form id="messageForm">
              <div class="input-group">
                <button type="button" class="btn btn-outline-secondary attachment-btn">
                  <i class="bi bi-paperclip"></i>
                </button>
                <input type="text" class="form-control" id="messageInput" placeholder="Type your message here...">
                <button type="submit" class="btn btn-primary">Send</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </main>
  </div>
</div>

<jsp:include page="common/footer.jsp" />

<script>
  document.addEventListener('DOMContentLoaded', function() {
    const socket = new WebSocket('ws://' + window.location.host + '${pageContext.request.contextPath}/chat-socket');
    const messageContainer = document.getElementById('messageContainer');
    const messageForm = document.getElementById('messageForm');
    const messageInput = document.getElementById('messageInput');
    
    // Scroll to bottom of message container
    messageContainer.scrollTop = messageContainer.scrollHeight;
    
    socket.onopen = function(event) {
      console.log('WebSocket connection established');
    };
    
    socket.onmessage = function(event) {
      const message = JSON.parse(event.data);
      appendMessage(message);
    };
    
    socket.onclose = function(event) {
      console.log('WebSocket connection closed');
    };
    
    socket.onerror = function(error) {
      console.error('WebSocket error:', error);
    };
    
    messageForm.addEventListener('submit', function(e) {
      e.preventDefault();
      const messageText = messageInput.value.trim();
      if (messageText) {
        const message = {
          content: messageText,
          channelId: '${activeChannel.id}',
          userId: '${activeUser.id}',
          recipientId: '${currentUser.id}'
        };
        
        socket.send(JSON.stringify(message));
        messageInput.value = '';
      }
    });
    
    function appendMessage(message) {
      const isOwnMessage = message.senderId === parseInt('${currentUser.id}');
      const messageDiv = document.createElement('div');
      messageDiv.className = `message ${isOwnMessage ? 'own-message' : ''}`;
      
      messageDiv.innerHTML = `
        <div class="message-avatar">
          <img src="${pageContext.request.contextPath}/assets/img/avatars/${message.senderAvatar}" alt="${message.sender}">
        </div>
        <div class="message-content">
          <div class="message-header">
            <span class="message-sender">${message.sender}</span>
            <span class="message-time">${message.formattedTime}</span>
          </div>
          <div class="message-body">
            ${message.content}
          </div>
        </div>
      `;
      
      messageContainer.appendChild(messageDiv);
      messageContainer.scrollTop = messageContainer.scrollHeight;
    }
    
    // Channel creation
    const createChannelBtn = document.getElementById('createChannelBtn');
    if (createChannelBtn) {
      createChannelBtn.addEventListener('click', function() {
        const channelName = prompt('Enter channel name:');
        if (channelName) {
          // Submit channel creation request
          fetch('${pageContext.request.contextPath}/chat?action=createChannel', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'name=' + encodeURIComponent(channelName)
          })
          .then(response => response.json())
          .then(data => {
            if (data.success) {
              window.location.href = '${pageContext.request.contextPath}/chat?channel=' + data.channelId;
            } else {
              alert(data.message || 'Failed to create channel');
            }
          })
          .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while creating the channel');
          });
        }
      });
    }
  });
</script>