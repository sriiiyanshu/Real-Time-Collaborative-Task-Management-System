document.addEventListener("DOMContentLoaded", function () {
  // Edit profile functionality
  const editProfileBtn = document.getElementById("editProfileBtn");
  const cancelEditBtn = document.getElementById("cancelEditBtn");
  const profileViewMode = document.getElementById("profileViewMode");
  const profileEditMode = document.getElementById("profileEditMode");

  if (editProfileBtn) {
    editProfileBtn.addEventListener("click", function () {
      profileViewMode.style.display = "none";
      profileEditMode.style.display = "block";
    });
  }

  if (cancelEditBtn) {
    cancelEditBtn.addEventListener("click", function () {
      profileEditMode.style.display = "none";
      profileViewMode.style.display = "block";
    });
  }

  // Profile image upload
  const uploadImageBtn = document.getElementById("uploadImageBtn");
  const profileImageInput = document.getElementById("profileImageInput");
  const profileImageForm = document.getElementById("profileImageForm");

  if (uploadImageBtn && profileImageInput) {
    uploadImageBtn.addEventListener("click", function () {
      profileImageInput.click();
    });

    profileImageInput.addEventListener("change", function () {
      if (this.files && this.files[0]) {
        // Auto submit the form when a file is selected
        profileImageForm.submit();
      }
    });
  }

  // Change password modal
  const changePasswordBtn = document.getElementById("changePasswordBtn");
  const changePasswordModal = document.getElementById("changePasswordModal");
  const savePasswordBtn = document.getElementById("savePasswordBtn");

  if (changePasswordBtn) {
    changePasswordBtn.addEventListener("click", function () {
      // Using Bootstrap modal
      $("#changePasswordModal").modal("show");
    });
  }

  if (savePasswordBtn) {
    savePasswordBtn.addEventListener("click", function () {
      const form = document.getElementById("changePasswordForm");
      const newPassword = document.getElementById("newPassword").value;
      const confirmPassword = document.getElementById("confirmPassword").value;

      if (newPassword !== confirmPassword) {
        showError("Passwords do not match");
        return;
      }

      if (!validatePassword(newPassword)) {
        showError("Password does not meet the requirements");
        return;
      }

      form.submit();
    });
  }

  // Password validation
  function validatePassword(password) {
    const minLength = 8;
    const hasUppercase = /[A-Z]/.test(password);
    const hasLowercase = /[a-z]/.test(password);
    const hasNumber = /[0-9]/.test(password);
    const hasSpecial = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password);

    return password.length >= minLength && hasUppercase && hasLowercase && hasNumber && hasSpecial;
  }

  function showError(message) {
    const errorDiv = document.createElement("div");
    errorDiv.className = "alert alert-danger mb-3";
    errorDiv.textContent = message;

    const modalBody = document.querySelector("#changePasswordModal .modal-body");
    const existingError = modalBody.querySelector(".alert");

    if (existingError) {
      modalBody.removeChild(existingError);
    }

    modalBody.insertBefore(errorDiv, modalBody.firstChild);
  }
});
