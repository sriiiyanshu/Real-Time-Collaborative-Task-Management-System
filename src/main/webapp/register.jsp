<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="common/header.jsp">
  <jsp:param name="pageTitle" value="Register | Task Management System" />
</jsp:include>

<main class="auth-page">
  <div class="container">
    <div class="auth-card">
      <div class="auth-header">
        <h1>Create an Account</h1>
        <p>Join our task management platform today!</p>
      </div>

      <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">${errorMessage}</div>
      </c:if>

      <form class="auth-form" action="${pageContext.request.contextPath}/register" method="post" id="registerForm">
        <div class="form-row">
          <div class="form-group col-md-6">
            <label for="firstName">First Name</label>
            <input
              type="text"
              id="firstName"
              name="firstName"
              required
              class="form-control ${not empty firstNameError ? 'is-invalid' : ''}"
              value="${firstName != null ? firstName : param.firstName}"
            />
            <c:if test="${not empty firstNameError}">
              <div class="invalid-feedback">${firstNameError}</div>
            </c:if>
          </div>

          <div class="form-group col-md-6">
            <label for="lastName">Last Name</label>
            <input type="text" id="lastName" name="lastName" required class="form-control ${not empty lastNameError ? 'is-invalid' : ''}" value="${lastName != null ? lastName : param.lastName}" />
            <c:if test="${not empty lastNameError}">
              <div class="invalid-feedback">${lastNameError}</div>
            </c:if>
          </div>
        </div>

        <div class="form-group">
          <label for="email">Email</label>
          <input type="email" id="email" name="email" required class="form-control ${not empty emailError ? 'is-invalid' : ''}" value="${email != null ? email : param.email}" />
          <c:if test="${not empty emailError}">
            <div class="invalid-feedback">${emailError}</div>
          </c:if>
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <div class="password-input">
            <input type="password" id="password" name="password" required class="form-control ${not empty passwordError ? 'is-invalid' : ''}" minlength="8" />
            <button type="button" class="toggle-password" aria-label="Toggle Password Visibility"></button>
          </div>
          <c:if test="${not empty passwordError}">
            <div class="invalid-feedback">${passwordError}</div>
          </c:if>
          <small class="form-text text-muted"> Password must be at least 8 characters long, contain uppercase and lowercase letters, at least one number and one special character. </small>
        </div>

        <div class="form-group">
          <label for="confirmPassword">Confirm Password</label>
          <div class="password-input">
            <input type="password" id="confirmPassword" name="confirmPassword" required class="form-control ${not empty confirmPasswordError ? 'is-invalid' : ''}" minlength="8" />
            <button type="button" class="toggle-password" aria-label="Toggle Password Visibility"></button>
          </div>
          <c:if test="${not empty confirmPasswordError}">
            <div class="invalid-feedback">${confirmPasswordError}</div>
          </c:if>
        </div>

        <div class="form-group terms">
          <input type="checkbox" id="terms" name="terms" required class="${not empty termsError ? 'is-invalid' : ''}" />
          <label for="terms">I agree to the <a href="#">Terms of Service</a> and <a href="#">Privacy Policy</a></label>
          <c:if test="${not empty termsError}">
            <div class="invalid-feedback">${termsError}</div>
          </c:if>
        </div>

        <button type="submit" class="btn btn-primary btn-block">Create Account</button>
      </form>

      <div class="auth-footer">
        <p>Already have an account? <a href="login.jsp">Login</a></p>
      </div>
    </div>
  </div>
</main>

<jsp:include page="common/footer.jsp" />

<style>
  /* Enhanced styles for registration page */
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
    max-width: 600px;
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

  .auth-form .form-row {
    display: flex;
    margin: 0 -10px;
  }

  .auth-form .col-md-6 {
    padding: 0 10px;
    width: 50%;
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

  .form-text {
    font-size: 0.875rem;
    margin-top: 5px;
  }

  .terms {
    display: flex;
    align-items: flex-start;
  }

  .terms input {
    margin-top: 5px;
    margin-right: 10px;
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

  @media (max-width: 768px) {
    .auth-page {
      padding: 20px 0;
    }

    .auth-card {
      padding: 25px;
    }

    .auth-form .form-row {
      flex-direction: column;
    }

    .auth-form .col-md-6 {
      width: 100%;
      padding: 0;
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
    const form = document.getElementById("registerForm");
    form.addEventListener("submit", function (event) {
      const firstName = document.getElementById("firstName").value.trim();
      const lastName = document.getElementById("lastName").value.trim();
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;
      const confirmPassword = document.getElementById("confirmPassword").value;
      const terms = document.getElementById("terms").checked;

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

      // First Name validation
      if (!firstName) {
        isValid = false;
        displayError("firstName", "First name is required");
      }

      // Last Name validation
      if (!lastName) {
        isValid = false;
        displayError("lastName", "Last name is required");
      }

      // Email validation
      if (!email) {
        isValid = false;
        displayError("email", "Email is required");
      } else if (!isValidEmail(email)) {
        isValid = false;
        displayError("email", "Please enter a valid email address");
      }

      // Password validation
      if (!password) {
        isValid = false;
        displayError("password", "Password is required");
      } else if (!isValidPassword(password)) {
        isValid = false;
        displayError("password", "Password must meet the requirements");
      }

      // Confirm Password validation
      if (password !== confirmPassword) {
        isValid = false;
        displayError("confirmPassword", "Passwords do not match");
      }

      // Terms validation
      if (!terms) {
        isValid = false;
        displayError("terms", "You must agree to the Terms of Service");
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

      if (fieldId === "terms") {
        field.parentNode.appendChild(errorDiv);
      } else {
        const parentEl = field.closest(".form-group");
        parentEl.appendChild(errorDiv);
      }
    }

    function isValidEmail(email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(email);
    }

    function isValidPassword(password) {
      // At least 8 characters, one uppercase, one lowercase, one number, one special character
      const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
      return passwordRegex.test(password);
    }
  });
</script>
