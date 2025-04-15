<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="common/header.jsp">
  <jsp:param name="pageTitle" value="Login | Task Management System" />
</jsp:include>

<main class="auth-page">
  <div class="container">
    <div class="auth-card">
      <div class="auth-header">
        <h1>Login</h1>
        <p>Welcome back! Please login to your account.</p>
      </div>

      <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">${errorMessage}</div>
      </c:if>

      <form class="auth-form" action="${pageContext.request.contextPath}/login" method="post" id="loginForm">
        <div class="form-group">
          <label for="username">Email or Username</label>
          <input
            type="text"
            id="username"
            name="username"
            required
            class="form-control ${not empty usernameError ? 'is-invalid' : ''}"
            value="${cookie.rememberedUsername.value != null ? cookie.rememberedUsername.value : ''}"
          />
          <c:if test="${not empty usernameError}">
            <div class="invalid-feedback">${usernameError}</div>
          </c:if>
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <div class="password-input">
            <input type="password" id="password" name="password" required class="form-control ${not empty passwordError ? 'is-invalid' : ''}" />
            <button type="button" class="toggle-password" aria-label="Toggle Password Visibility"></button>
          </div>
          <c:if test="${not empty passwordError}">
            <div class="invalid-feedback">${passwordError}</div>
          </c:if>
        </div>

        <div class="form-options">
          <div class="remember-me">
            <input type="checkbox" id="rememberMe" name="rememberMe" ${cookie.rememberedUsername.value != null ? 'checked' : ''}>
            <label for="rememberMe">Remember me</label>
          </div>
          <a href="${pageContext.request.contextPath}/password-reset" class="forgot-password">Forgot Password?</a>
        </div>

        <button type="submit" class="btn btn-primary btn-block">Login</button>
      </form>

      <div class="auth-footer">
        <p>Don't have an account? <a href="register.jsp">Register</a></p>
      </div>
    </div>
  </div>
</main>

<jsp:include page="common/footer.jsp" />

<style>
  /* Enhanced styles for login page */
  .auth-page {
    min-height: calc(100vh - 160px);
    display: flex;
    align-items: center;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    padding: 40px 0;
  }

  .auth-card {
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 15px 35px rgba(50, 50, 93, 0.1), 0 5px 15px rgba(0, 0, 0, 0.07);
    padding: 40px;
    max-width: 500px;
    width: 100%;
    margin: 0 auto;
    transition: transform 0.3s;
  }

  .auth-header {
    text-align: center;
    margin-bottom: 30px;
  }

  .auth-header h1 {
    color: var(--dark-color);
    font-size: 2rem;
    margin-bottom: 10px;
    font-weight: 600;
  }

  .auth-header p {
    color: #777;
    font-size: 1.1rem;
  }

  .auth-form .form-group {
    margin-bottom: 20px;
    position: relative;
  }

  .auth-form label {
    display: block;
    font-weight: 500;
    margin-bottom: 5px;
    color: #444;
  }

  .auth-form .form-control {
    background-color: #f8f9fa;
    border: 1px solid #e1e5eb;
    border-radius: 6px;
    padding: 12px 15px;
    width: 100%;
    transition: border-color 0.2s, box-shadow 0.2s;
  }

  .auth-form .form-control:focus {
    border-color: var(--primary-color);
    box-shadow: 0 0 0 0.25rem rgba(52, 152, 219, 0.25);
  }

  .auth-form .form-control.is-invalid {
    border-color: var(--danger-color);
  }

  .invalid-feedback {
    display: block;
    width: 100%;
    margin-top: 0.25rem;
    font-size: 0.875rem;
    color: var(--danger-color);
  }

  .password-input {
    position: relative;
  }

  .toggle-password {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    border: none;
    background: none;
    cursor: pointer;
    width: 24px;
    height: 24px;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'%3E%3Cpath fill='none' d='M0 0h24v24H0z'/%3E%3Cpath d='M12 3c5.392 0 9.878 3.88 10.819 9-.94 5.12-5.427 9-10.819 9-5.392 0-9.878-3.88-10.819-9C2.121 6.88 6.608 3 12 3zm0 16a9.005 9.005 0 0 0 8.777-7 9.005 9.005 0 0 0-17.554 0A9.005 9.005 0 0 0 12 19zm0-2.5a4.5 4.5 0 1 1 0-9 4.5 4.5 0 0 1 0 9zm0-2a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z' fill='rgba(75,85,99,1)'/%3E%3C/svg%3E");
  }

  .toggle-password.show {
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'%3E%3Cpath fill='none' d='M0 0h24v24H0z'/%3E%3Cpath d='M17.882 19.297A10.949 10.949 0 0 1 12 21c-5.392 0-9.878-3.88-10.819-9a10.982 10.982 0 0 1 3.34-6.066L1.392 2.808l1.415-1.415 19.799 19.8-1.415 1.414-3.31-3.31zM5.935 7.35A8.965 8.965 0 0 0 3.223 12a9.005 9.005 0 0 0 13.201 5.838l-2.028-2.028A4.5 4.5 0 0 1 8.19 9.604L5.935 7.35zm6.979 6.978l-3.242-3.242a2.5 2.5 0 0 0 3.241 3.241zm7.893 2.264l-1.431-1.43A8.935 8.935 0 0 0 20.777 12 9.005 9.005 0 0 0 9.552 5.338L7.974 3.76C9.221 3.27 10.58 3 12 3c5.392 0 9.878 3.88 10.819 9a10.947 10.947 0 0 1-2.012 4.592zm-9.084-9.084a4.5 4.5 0 0 1 4.769 4.769l-4.77-4.769z' fill='rgba(75,85,99,1)'/%3E%3C/svg%3E");
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  .remember-me {
    display: flex;
    align-items: center;
  }

  .remember-me input {
    margin-right: 8px;
  }

  .forgot-password {
    color: var(--primary-color);
    font-size: 0.9rem;
    font-weight: 500;
    text-decoration: none;
  }

  .forgot-password:hover {
    text-decoration: underline;
  }

  .btn-block {
    display: block;
    width: 100%;
    padding: 12px;
    font-size: 1rem;
    font-weight: 600;
    margin-top: 25px;
  }

  .auth-footer {
    text-align: center;
    margin-top: 25px;
    color: #555;
  }

  .auth-footer a {
    color: var(--primary-color);
    font-weight: 600;
  }

  .alert {
    padding: 12px 15px;
    border-radius: 6px;
    margin-bottom: 20px;
    font-size: 0.95rem;
  }

  .alert-danger {
    background-color: #fef2f2;
    color: #b91c1c;
    border: 1px solid #fee2e2;
  }

  @media (max-width: 768px) {
    .auth-page {
      padding: 20px 0;
    }

    .auth-card {
      padding: 25px;
    }
  }
</style>

<script>
  document.addEventListener("DOMContentLoaded", function () {
    // Password visibility toggle
    const toggleButtons = document.querySelectorAll(".toggle-password");
    toggleButtons.forEach((button) => {
      button.addEventListener("click", function () {
        const passwordInput = this.previousElementSibling;
        const type = passwordInput.getAttribute("type");
        passwordInput.setAttribute("type", type === "password" ? "text" : "password");
        this.classList.toggle("show");
      });
    });

    // Form validation - client side initial check
    const form = document.getElementById("loginForm");
    form.addEventListener("submit", function (event) {
      const username = document.getElementById("username").value.trim();
      const password = document.getElementById("password").value;

      let isValid = true;

      // Reset previous validation messages
      document.querySelectorAll(".invalid-feedback").forEach((el) => {
        if (!el.hasAttribute("data-server")) {
          el.remove();
        }
      });
      document.querySelectorAll(".is-invalid").forEach((el) => {
        el.classList.remove("is-invalid");
      });

      // Username/email validation
      if (!username) {
        isValid = false;
        displayError("username", "Username or email is required");
      }

      // Password validation
      if (!password) {
        isValid = false;
        displayError("password", "Password is required");
      }

      if (!isValid) {
        event.preventDefault();
      }
    });

    function displayError(fieldId, message) {
      const field = document.getElementById(fieldId);
      field.classList.add("is-invalid");

      const errorDiv = document.createElement("div");
      errorDiv.className = "invalid-feedback";
      errorDiv.textContent = message;

      const parentEl = field.closest(".form-group");
      parentEl.appendChild(errorDiv);
    }
  });
</script>
