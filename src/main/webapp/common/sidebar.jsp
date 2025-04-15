<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<aside class="sidebar">
  <div class="sidebar-header">
    <h3>${param.sidebarTitle != null ? param.sidebarTitle : 'Menu'}</h3>
  </div>

  <div class="sidebar-content">
    <c:choose>
      <c:when test="${param.sidebarType == 'project'}">
        <div class="sidebar-section">
          <h4>My Projects</h4>
          <ul class="sidebar-list">
            <c:forEach items="${userProjects}" var="project">
              <li class="sidebar-item ${project.id == selectedProjectId ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/project?id=${project.id}"> ${project.name} </a>
              </li>
            </c:forEach>
          </ul>
          <div class="add-new">
            <a href="${pageContext.request.contextPath}/project?action=new" class="btn btn-sm"> <i class="add-icon"></i> New Project </a>
          </div>
        </div>
      </c:when>

      <c:when test="${param.sidebarType == 'task'}">
        <div class="sidebar-section">
          <h4>Task Categories</h4>
          <ul class="sidebar-list">
            <li class="sidebar-item ${taskFilter == 'assigned' ? 'active' : ''}">
              <a href="${pageContext.request.contextPath}/task?filter=assigned"> Assigned to Me </a>
            </li>
            <li class="sidebar-item ${taskFilter == 'created' ? 'active' : ''}">
              <a href="${pageContext.request.contextPath}/task?filter=created"> Created by Me </a>
            </li>
            <li class="sidebar-item ${taskFilter == 'due-today' ? 'active' : ''}">
              <a href="${pageContext.request.contextPath}/task?filter=due-today"> Due Today </a>
            </li>
            <li class="sidebar-item ${taskFilter == 'overdue' ? 'active' : ''}">
              <a href="${pageContext.request.contextPath}/task?filter=overdue"> Overdue </a>
            </li>
          </ul>
          <div class="add-new">
            <a href="${pageContext.request.contextPath}/task?action=new" class="btn btn-sm"> <i class="add-icon"></i> New Task </a>
          </div>
        </div>
      </c:when>

      <c:when test="${param.sidebarType == 'team'}">
        <div class="sidebar-section">
          <h4>My Teams</h4>
          <ul class="sidebar-list">
            <c:forEach items="${userTeams}" var="team">
              <li class="sidebar-item ${team.id == selectedTeamId ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/team?id=${team.id}"> ${team.name} </a>
              </li>
            </c:forEach>
          </ul>
          <div class="add-new">
            <a href="${pageContext.request.contextPath}/team?action=new" class="btn btn-sm"> <i class="add-icon"></i> New Team </a>
          </div>
        </div>
      </c:when>

      <c:when test="${param.sidebarType == 'filemanager'}">
        <div class="sidebar-section">
          <h4>File Categories</h4>
          <ul class="sidebar-list">
            <li class="sidebar-item ${fileFilter == 'my-files' ? 'active' : ''}">
              <a href="${pageContext.request.contextPath}/filemanager?filter=my-files"> My Files </a>
            </li>
            <li class="sidebar-item ${fileFilter == 'shared-with-me' ? 'active' : ''}">
              <a href="${pageContext.request.contextPath}/filemanager?filter=shared-with-me"> Shared with Me </a>
            </li>
            <li class="sidebar-item ${fileFilter == 'recent' ? 'active' : ''}">
              <a href="${pageContext.request.contextPath}/filemanager?filter=recent"> Recent Files </a>
            </li>
          </ul>
          <div class="add-new">
            <a href="#" id="uploadFileBtn" class="btn btn-sm"> <i class="upload-icon"></i> Upload File </a>
          </div>
        </div>
      </c:when>
    </c:choose>
  </div>
</aside>
