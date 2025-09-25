<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Error - CollabTask</title>
    <link rel="stylesheet" href="assets/css/style.css" />
</head>
<body class="auth">
<div class="container">
    <div class="card">
        <h1>Oops! Something went wrong</h1>
        <p class="subtitle">We encountered an error while processing your request.</p>
        <div class="alert">
            <%= request.getAttribute("jakarta.servlet.error.message") != null ? 
                request.getAttribute("jakarta.servlet.error.message") : "An unexpected error occurred" %>
        </div>
        <a href="/index.jsp" class="button">Go Home</a>
    </div>
</div>
</body>
</html>
