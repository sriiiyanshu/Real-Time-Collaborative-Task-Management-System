<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${param.pageTitle != null ? param.pageTitle : 'Task Management System'}</title>
    <link rel="icon" href="${pageContext.request.contextPath}/assets/img/favicon.ico">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/responsive.css">
    <c:if test="${param.customCss != null}">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/${param.customCss}">
    </c:if>
</head>
<body>
    <header class="header">
        <div class="container">
            <div class="logo">
                <a href="${pageContext.request.contextPath}/dashboard.jsp">
                    <img src="${pageContext.request.contextPath}/assets/img/logo.png" alt="Task Management System">
                </a>
            </div>
            
            <div class="search-bar">
                <form action="${pageContext.request.contextPath}/search" method="get">
                    <input type="text" name="query" placeholder="Search tasks, projects, team members...">
                    <button type="submit"><i class="search-icon"></i></button>
                </form>
            </div>
            
            <div class="user-nav">
                <c:if test="${sessionScope.user != null}">
                    <div class="notification-icon">
                        <a href="${pageContext.request.contextPath}/notifications.jsp">
                            <i class="notification-bell"></i>
                            <span class="notification-count">${sessionScope.notificationCount}</span>
                        </a>
                    </div>
                    
                    <div class="user-profile">
                        <a href="${pageContext.request.contextPath}/profile.jsp" class="user-info">
                            <img src="${pageContext.request.contextPath}/assets/img/user-default.png" alt="User Avatar">
                            <span>${sessionScope.user.firstName}</span>
                        </a>
                    </div>
                </c:if>
                <c:if test="${sessionScope.user == null}">
                    <div class="auth-buttons">
                        <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-login">Login</a>
                        <a href="${pageContext.request.contextPath}/register.jsp" class="btn btn-register">Register</a>
                    </div>
                </c:if>
            </div>
        </div>
    </header>