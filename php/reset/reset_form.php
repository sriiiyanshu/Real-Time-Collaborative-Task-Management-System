<?php
$dsn = getenv('PHP_DB_DSN') ?: 'mysql:host=127.0.0.1;dbname=collabtask;charset=utf8mb4';
$user = getenv('PHP_DB_USER') ?: 'root';
$pass = getenv('PHP_DB_PASS') ?: 'root';
$pdo = new PDO($dsn, $user, $pass, [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]);

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $token = $_POST['token'] ?? '';
    $password = $_POST['password'] ?? '';
    $stmt = $pdo->prepare('SELECT pr.user_id FROM password_resets pr WHERE pr.token=? AND pr.expires_at > NOW() AND pr.used=0');
    $stmt->execute([$token]);
    if ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $hash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);
        $pdo->prepare('UPDATE users SET password_hash=? WHERE id=?')->execute([$hash, $row['user_id']]);
        $pdo->prepare('UPDATE password_resets SET used=1 WHERE token=?')->execute([$token]);
        echo '<p>Password updated. You can close this window and login.</p>';
        exit;
    } else {
        echo '<p>Invalid or expired token.</p>';
    }
}
$token = $_GET['token'] ?? '';
?>
<!doctype html>
<html><body>
<h3>Reset Password</h3>
<form method="post">
  <input type="hidden" name="token" value="<?php echo htmlspecialchars($token, ENT_QUOTES); ?>" />
  <input type="password" name="password" placeholder="New password" required />
  <button type="submit">Update</button>
</form>
</body></html>
