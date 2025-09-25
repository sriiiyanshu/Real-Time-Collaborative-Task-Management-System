<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<title>CollabTask - Sign In</title>
	<link rel="stylesheet" href="assets/css/style.css" />
</head>
<body class="auth">
<div class="container">
	<div class="card">
		<h1>CollabTask</h1>
		<p class="subtitle">Real-time collaborative task management</p>
		<% String error = request.getParameter("error"); if (error != null) { %>
		<div class="alert"><%= error %></div>
		<% } %>
		<form method="post" action="login">
			<input type="email" name="email" placeholder="Email" required />
			<input type="password" name="password" placeholder="Password" required />
			<button type="submit">Sign In</button>
		</form>
		<div class="divider">or</div>
		<form method="post" action="register">
			<input type="text" name="name" placeholder="Full name" required />
			<input type="email" name="email" placeholder="Email" required />
			<input type="password" name="password" placeholder="Create password" required />
			<button type="submit" class="secondary">Create Account</button>
		</form>
		<p class="muted"><a href="php/reset/request_reset.php">Forgot password?</a></p>
	</div>
</div>
</body>
</html>
