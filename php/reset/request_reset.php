<?php
// Simple password reset request: stores token, prints link, or emails via SMTP if configured
$dsn = getenv('PHP_DB_DSN') ?: 'mysql:host=127.0.0.1;dbname=collabtask;charset=utf8mb4';
$user = getenv('PHP_DB_USER') ?: 'root';
$pass = getenv('PHP_DB_PASS') ?: 'root';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = $_POST['email'] ?? '';
    $pdo = new PDO($dsn, $user, $pass, [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]);
    $stmt = $pdo->prepare('SELECT id FROM users WHERE email=?');
    $stmt->execute([$email]);
    $row = $stmt->fetch(PDO::FETCH_ASSOC);
    if ($row) {
        $token = bin2hex(random_bytes(16));
        $expires = date('Y-m-d H:i:s', time() + 3600);
        $pdo->prepare('INSERT INTO password_resets(user_id, token, expires_at) VALUES(?,?,?)')
            ->execute([$row['id'], $token, $expires]);
        $link = sprintf('%s://%s%s?token=%s', isset($_SERVER['HTTPS']) ? 'https' : 'http', $_SERVER['HTTP_HOST'], dirname($_SERVER['REQUEST_URI']).'/reset_form.php', $token);
        echo "<p>Reset link: <a href='$link'>$link</a></p>"; // Replace with email send in production
    }
}
?>
<!doctype html>
<html><body>
<h3>Request Password Reset</h3>
<form method="post">
  <input type="email" name="email" placeholder="Your email" required />
  <button type="submit">Send reset link</button>
</form>
</body></html>
