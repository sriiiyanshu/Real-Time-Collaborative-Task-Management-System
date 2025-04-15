<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%> <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="common/header.jsp">
  <jsp:param name="pageTitle" value="Error" />
</jsp:include>

<div class="container error-page">
  <div class="row justify-content-center">
    <div class="col-md-8 col-lg-6">
      <div class="error-card">
        <c:choose>
          <c:when test="${not empty statusCode && statusCode == '404'}">
            <div class="error-icon">
              <i class="bi bi-question-circle"></i>
            </div>
            <h1>Page Not Found</h1>
            <p>We couldn't find the page you were looking for.</p>
          </c:when>

          <c:when test="${not empty statusCode && statusCode == '403'}">
            <div class="error-icon">
              <i class="bi bi-shield-exclamation"></i>
            </div>
            <h1>Access Denied</h1>
            <p>You don't have permission to access this resource.</p>
          </c:when>

          <c:when test="${not empty statusCode && statusCode == '500'}">
            <div class="error-icon">
              <i class="bi bi-exclamation-triangle"></i>
            </div>
            <h1>Internal Server Error</h1>
            <p>Something went wrong on our end. We're working to fix it.</p>
          </c:when>

          <c:otherwise>
            <div class="error-icon">
              <i class="bi bi-exclamation-circle"></i>
            </div>
            <h1>Oops! An Error Occurred</h1>
            <p>Something went wrong. Please try again later.</p>
          </c:otherwise>
        </c:choose>

        <c:if test="${not empty errorMessage}">
          <div class="error-details">
            <p>${errorMessage}</p>
          </div>
        </c:if>

        <c:if test="${pageContext.errorData.throwable != null && param.debug == 'true'}">
          <div class="error-stack-trace">
            <h3>Stack Trace:</h3>
            <pre>${pageContext.errorData.throwable}</pre>
          </div>
        </c:if>

        <div class="error-actions">
          <a href="${pageContext.request.contextPath}/" class="btn btn-primary"> <i class="bi bi-house"></i> Return to Home </a>
          <a href="javascript:history.back()" class="btn btn-outline-secondary"> <i class="bi bi-arrow-left"></i> Go Back </a>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="common/footer.jsp" />

<script>
  document.addEventListener("DOMContentLoaded", function () {
    // Automatically log errors to the server
    const errorInfo = {
      url: window.location.href,
      statusCode: "${statusCode}",
      errorMessage: "${errorMessage}",
      userAgent: navigator.userAgent,
      timestamp: new Date().toISOString(),
    };

    // Only log if there's an actual error
    if (errorInfo.statusCode || errorInfo.errorMessage) {
      fetch("${pageContext.request.contextPath}/error-logger", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(errorInfo),
      }).catch((err) => console.error("Error logging failed:", err));
    }
  });
</script>
