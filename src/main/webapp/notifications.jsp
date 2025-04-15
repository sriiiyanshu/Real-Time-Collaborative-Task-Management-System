<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="common/header.jsp">
  <jsp:param name="pageTitle" value="Notifications" />
</jsp:include>
<jsp:include page="common/navigation.jsp" />

<div class="container-fluid">
  <div class="row">
    <jsp:include page="common/sidebar.jsp" />

    <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <h1 class="h2">Notifications</h1>
        <div class="btn-toolbar mb-2 mb-md-0">
          <button type="button" class="btn btn-sm btn-outline-secondary" id="markAllReadBtn"><i class="bi bi-check-all"></i> Mark All as Read</button>
        </div>
      </div>

      <c:if test="${not empty successMessage}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
          ${successMessage}
          <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
      </c:if>

      <!-- Notification Filters -->
      <div class="notification-filters mb-4">
        <div class="btn-group" role="group" aria-label="Notification filters">
          <input type="radio" class="btn-check" name="notificationFilter" id="all-notifications" value="all" autocomplete="off" checked />
          <label class="btn btn-outline-primary" for="all-notifications">All</label>

          <input type="radio" class="btn-check" name="notificationFilter" id="unread-notifications" value="unread" autocomplete="off" />
          <label class="btn btn-outline-primary" for="unread-notifications">Unread</label>

          <input type="radio" class="btn-check" name="notificationFilter" id="task-notifications" value="task" autocomplete="off" />
          <label class="btn btn-outline-primary" for="task-notifications">Tasks</label>

          <input type="radio" class="btn-check" name="notificationFilter" id="project-notifications" value="project" autocomplete="off" />
          <label class="btn btn-outline-primary" for="project-notifications">Projects</label>

          <input type="radio" class="btn-check" name="notificationFilter" id="comment-notifications" value="comment" autocomplete="off" />
          <label class="btn btn-outline-primary" for="comment-notifications">Comments</label>

          <input type="radio" class="btn-check" name="notificationFilter" id="mention-notifications" value="mention" autocomplete="off" />
          <label class="btn btn-outline-primary" for="mention-notifications">Mentions</label>
        </div>
      </div>

      <div class="notification-list" id="notificationList">
        <c:choose>
          <c:when test="${empty notifications}">
            <div class="empty-state text-center py-5">
              <i class="bi bi-bell-slash display-4"></i>
              <h3 class="mt-3">No Notifications</h3>
              <p class="text-muted">You're all caught up! There are no notifications to display.</p>
            </div>
          </c:when>
          <c:otherwise>
            <c:forEach var="notification" items="${notifications}">
              <div class="notification-item ${notification.read ? '' : 'unread'}" data-notification-id="${notification.id}" data-notification-type="${notification.type}">
                <div class="notification-icon">
                  <c:choose>
                    <c:when test="${notification.type eq 'TASK_ASSIGNED'}">
                      <i class="bi bi-list-task"></i>
                    </c:when>
                    <c:when test="${notification.type eq 'TASK_DUE_SOON'}">
                      <i class="bi bi-alarm"></i>
                    </c:when>
                    <c:when test="${notification.type eq 'TASK_COMPLETED'}">
                      <i class="bi bi-check-circle"></i>
                    </c:when>
                    <c:when test="${notification.type eq 'PROJECT_UPDATED'}">
                      <i class="bi bi-folder"></i>
                    </c:when>
                    <c:when test="${notification.type eq 'COMMENT_ADDED'}">
                      <i class="bi bi-chat-left-text"></i>
                    </c:when>
                    <c:when test="${notification.type eq 'MENTION'}">
                      <i class="bi bi-at"></i>
                    </c:when>
                    <c:when test="${notification.type eq 'FILE_SHARED'}">
                      <i class="bi bi-file-earmark-arrow-up"></i>
                    </c:when>
                    <c:otherwise>
                      <i class="bi bi-bell"></i>
                    </c:otherwise>
                  </c:choose>
                </div>
                <div class="notification-content">
                  <div class="notification-header">
                    <span class="notification-title">${notification.title}</span>
                    <span class="notification-time">
                      <fmt:formatDate value="${notification.createdAt}" pattern="MMM d, yyyy h:mm a" />
                    </span>
                  </div>
                  <div class="notification-body">${notification.message}</div>
                  <c:if test="${not empty notification.actionUrl}">
                    <div class="notification-action">
                      <a href="${notification.actionUrl}" class="notification-link">
                        <c:choose>
                          <c:when test="${notification.type eq 'TASK_ASSIGNED' || notification.type eq 'TASK_DUE_SOON' || notification.type eq 'TASK_COMPLETED'}"> View Task </c:when>
                          <c:when test="${notification.type eq 'PROJECT_UPDATED'}"> View Project </c:when>
                          <c:when test="${notification.type eq 'COMMENT_ADDED' || notification.type eq 'MENTION'}"> View Comment </c:when>
                          <c:when test="${notification.type eq 'FILE_SHARED'}"> View File </c:when>
                          <c:otherwise> View Details </c:otherwise>
                        </c:choose>
                      </a>
                    </div>
                  </c:if>
                </div>
                <div class="notification-actions">
                  <button class="btn btn-light btn-sm mark-read-btn" data-notification-id="${notification.id}" title="Mark as ${notification.read ? 'unread' : 'read'}">
                    <i class="bi ${notification.read ? 'bi-envelope' : 'bi-envelope-open'}"></i>
                  </button>
                  <button class="btn btn-light btn-sm delete-notification-btn" data-notification-id="${notification.id}" title="Delete notification">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </div>
            </c:forEach>

            <!-- Pagination -->
            <c:if test="${totalPages > 1}">
              <div class="mt-4">
                <nav aria-label="Notification pagination">
                  <ul class="pagination justify-content-center">
                    <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                      <a class="page-link" href="${pageContext.request.contextPath}/notifications?page=${currentPage - 1}" aria-label="Previous">
                        <span aria-hidden="true">&laquo;</span>
                      </a>
                    </li>
                    <c:forEach begin="1" end="${totalPages}" var="pageNum">
                      <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/notifications?page=${pageNum}">${pageNum}</a>
                      </li>
                    </c:forEach>
                    <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                      <a class="page-link" href="${pageContext.request.contextPath}/notifications?page=${currentPage + 1}" aria-label="Next">
                        <span aria-hidden="true">&raquo;</span>
                      </a>
                    </li>
                  </ul>
                </nav>
              </div>
            </c:if>
          </c:otherwise>
        </c:choose>
      </div>
    </main>
  </div>
</div>

<!-- Notification Settings Modal -->
<div class="modal fade" id="notificationSettingsModal" tabindex="-1" aria-labelledby="notificationSettingsModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="notificationSettingsModalLabel">Notification Preferences</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="notificationSettingsForm" action="${pageContext.request.contextPath}/notification/settings" method="post">
        <div class="modal-body">
          <div class="mb-3">
            <h6>Notification Types</h6>
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="taskAssignedCheck" name="notificationTypes" value="TASK_ASSIGNED" ${userNotifSettings.taskAssigned ? 'checked' : ''}>
              <label class="form-check-label" for="taskAssignedCheck"> Tasks assigned to me </label>
            </div>
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="taskDueCheck" name="notificationTypes" value="TASK_DUE_SOON" ${userNotifSettings.taskDueSoon ? 'checked' : ''}>
              <label class="form-check-label" for="taskDueCheck"> Tasks due soon </label>
            </div>
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="taskCompletedCheck" name="notificationTypes" value="TASK_COMPLETED" ${userNotifSettings.taskCompleted ? 'checked' : ''}>
              <label class="form-check-label" for="taskCompletedCheck"> Tasks completed by team members </label>
            </div>
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="projectUpdatedCheck" name="notificationTypes" value="PROJECT_UPDATED" ${userNotifSettings.projectUpdated ? 'checked' : ''}>
              <label class="form-check-label" for="projectUpdatedCheck"> Project updates </label>
            </div>
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="commentAddedCheck" name="notificationTypes" value="COMMENT_ADDED" ${userNotifSettings.commentAdded ? 'checked' : ''}>
              <label class="form-check-label" for="commentAddedCheck"> Comments on items I'm watching </label>
            </div>
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="mentionCheck" name="notificationTypes" value="MENTION" ${userNotifSettings.mention ? 'checked' : ''}>
              <label class="form-check-label" for="mentionCheck"> Mentions (@username) </label>
            </div>
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="fileSharedCheck" name="notificationTypes" value="FILE_SHARED" ${userNotifSettings.fileShared ? 'checked' : ''}>
              <label class="form-check-label" for="fileSharedCheck"> Files shared with me </label>
            </div>
          </div>

          <div class="mb-3">
            <h6>Delivery Methods</h6>
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="emailNotificationsCheck" name="deliveryMethods" value="EMAIL" ${userNotifSettings.email ? 'checked' : ''}>
              <label class="form-check-label" for="emailNotificationsCheck"> Email </label>
            </div>
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="browserNotificationsCheck" name="deliveryMethods" value="BROWSER" ${userNotifSettings.browser ? 'checked' : ''}>
              <label class="form-check-label" for="browserNotificationsCheck"> Browser </label>
            </div>
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="inAppNotificationsCheck" name="deliveryMethods" value="IN_APP" checked disabled />
              <label class="form-check-label" for="inAppNotificationsCheck"> In-app (always enabled) </label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary">Save Preferences</button>
        </div>
      </form>
    </div>
  </div>
</div>

<jsp:include page="common/footer.jsp" />

<script>
  document.addEventListener("DOMContentLoaded", function () {
    // Mark as read/unread
    const markReadButtons = document.querySelectorAll(".mark-read-btn");
    markReadButtons.forEach((button) => {
      button.addEventListener("click", function () {
        const notificationId = this.dataset.notificationId;
        const notificationItem = document.querySelector(`.notification-item[data-notification-id="${notificationId}"]`);
        const isRead = notificationItem.classList.contains("unread") ? false : true;

        fetch("${pageContext.request.contextPath}/notifications/mark-read", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: `id=${notificationId}&read=${!isRead}`,
        })
          .then((response) => response.json())
          .then((data) => {
            if (data.success) {
              notificationItem.classList.toggle("unread");
              this.innerHTML = `<i class="bi ${!isRead ? "bi-envelope" : "bi-envelope-open"}"></i>`;
              this.title = `Mark as ${!isRead ? "unread" : "read"}`;

              // Update notification counter in the header
              const notifCounter = document.getElementById("notificationCounter");
              if (notifCounter) {
                let count = parseInt(notifCounter.textContent || "0");
                count = isRead ? count + 1 : count - 1;
                if (count <= 0) {
                  notifCounter.style.display = "none";
                } else {
                  notifCounter.style.display = "inline-block";
                  notifCounter.textContent = count;
                }
              }
            }
          })
          .catch((error) => console.error("Error:", error));
      });
    });

    // Delete notification
    const deleteButtons = document.querySelectorAll(".delete-notification-btn");
    deleteButtons.forEach((button) => {
      button.addEventListener("click", function () {
        const notificationId = this.dataset.notificationId;
        const notificationItem = document.querySelector(`.notification-item[data-notification-id="${notificationId}"]`);

        if (confirm("Are you sure you want to delete this notification?")) {
          fetch("${pageContext.request.contextPath}/notifications/delete", {
            method: "POST",
            headers: {
              "Content-Type": "application/x-www-form-urlencoded",
            },
            body: `id=${notificationId}`,
          })
            .then((response) => response.json())
            .then((data) => {
              if (data.success) {
                notificationItem.remove();

                // If no notifications left, show empty state
                const notificationList = document.getElementById("notificationList");
                if (notificationList.children.length === 0) {
                  notificationList.innerHTML = `
                  <div class="empty-state text-center py-5">
                    <i class="bi bi-bell-slash display-4"></i>
                    <h3 class="mt-3">No Notifications</h3>
                    <p class="text-muted">You're all caught up! There are no notifications to display.</p>
                  </div>
                `;
                }
              }
            })
            .catch((error) => console.error("Error:", error));
        }
      });
    });

    // Mark all as read
    const markAllReadBtn = document.getElementById("markAllReadBtn");
    if (markAllReadBtn) {
      markAllReadBtn.addEventListener("click", function () {
        fetch("${pageContext.request.contextPath}/notifications/mark-all-read", {
          method: "POST",
        })
          .then((response) => response.json())
          .then((data) => {
            if (data.success) {
              document.querySelectorAll(".notification-item.unread").forEach((item) => {
                item.classList.remove("unread");
              });

              document.querySelectorAll(".mark-read-btn").forEach((button) => {
                button.innerHTML = '<i class="bi bi-envelope"></i>';
                button.title = "Mark as unread";
              });

              // Update notification counter in the header
              const notifCounter = document.getElementById("notificationCounter");
              if (notifCounter) {
                notifCounter.style.display = "none";
                notifCounter.textContent = "0";
              }
            }
          })
          .catch((error) => console.error("Error:", error));
      });
    }

    // Notification filters
    const filterButtons = document.querySelectorAll('input[name="notificationFilter"]');
    filterButtons.forEach((button) => {
      button.addEventListener("change", function () {
        const filterValue = this.value;
        const notificationItems = document.querySelectorAll(".notification-item");

        notificationItems.forEach((item) => {
          if (filterValue === "all") {
            item.style.display = "";
          } else if (filterValue === "unread") {
            item.style.display = item.classList.contains("unread") ? "" : "none";
          } else {
            const type = item.dataset.notificationType;
            let showItem = false;

            switch (filterValue) {
              case "task":
                showItem = type === "TASK_ASSIGNED" || type === "TASK_DUE_SOON" || type === "TASK_COMPLETED";
                break;
              case "project":
                showItem = type === "PROJECT_UPDATED";
                break;
              case "comment":
                showItem = type === "COMMENT_ADDED";
                break;
              case "mention":
                showItem = type === "MENTION";
                break;
              default:
                showItem = true;
            }

            item.style.display = showItem ? "" : "none";
          }
        });
      });
    });

    // Setup notification settings
    const notificationSettingsModal = document.getElementById("notificationSettingsModal");
    if (notificationSettingsModal) {
      const modalInstance = new bootstrap.Modal(notificationSettingsModal);

      // Open modal when clicking the settings button from the header
      const notifSettingsBtn = document.getElementById("notificationSettingsBtn");
      if (notifSettingsBtn) {
        notifSettingsBtn.addEventListener("click", function (e) {
          e.preventDefault();
          modalInstance.show();
        });
      }
    }

    // Enable browser notifications
    const browserNotificationsCheck = document.getElementById("browserNotificationsCheck");
    if (browserNotificationsCheck && browserNotificationsCheck.checked) {
      if ("Notification" in window) {
        if (Notification.permission !== "granted" && Notification.permission !== "denied") {
          Notification.requestPermission();
        }
      }
    }
  });
</script>
