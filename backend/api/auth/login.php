<?php
// backend/api/auth/login.php
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if (($_SERVER['REQUEST_METHOD'] ?? '') === 'OPTIONS') {
    http_response_code(200);
    exit(0);
}

require_once __DIR__ . '/../../config/database.php';

$db = $pdo ?? $conn ?? null;

// Health-check support for diagnostics
if (($_SERVER['REQUEST_METHOD'] ?? '') === 'GET' && (isset($_GET['health']) || stripos($_SERVER['HTTP_ACCEPT'] ?? '', 'application/json') !== false)) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        "status" => "ok",
        "server" => "online",
        "database" => $db ? "connected" : "disconnected",
        "timestamp" => date('Y-m-d H:i:s')
    ]);
    exit();
}

if (!$db) {
    http_response_code(500);
    $isJson = (stripos($_SERVER['CONTENT_TYPE'] ?? '', 'application/json') !== false) ||
              (stripos($_SERVER['HTTP_ACCEPT'] ?? '', 'application/json') !== false);
    if ($isJson) {
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode([
            "status" => "error",
            "success" => false,
            "message" => "Database connection unavailable.",
            "debug" => [
                "server" => "online",
                "database" => "disconnected",
                "error_type" => "DATABASE_CONNECTION_ERROR"
            ]
        ]);
        exit();
    }
    header("Location: /DeskStream/index.php?error=" . urlencode("Database connection unavailable."));
    exit();
}

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $contentType = $_SERVER['CONTENT_TYPE'] ?? '';
    if (stripos($contentType, 'application/json') !== false) {
        $jsonInput = json_decode(file_get_contents('php://input'), true);
        $email = trim((string)($jsonInput['email'] ?? $jsonInput['username'] ?? ''));
        $password = (string)($jsonInput['password'] ?? '');
        $isJson = true;
    } else {
        $email = trim((string)($_POST['email'] ?? $_POST['username'] ?? ''));
        $password = (string)($_POST['password'] ?? '');
        $isJson = false;
    }

    if ($isJson) {
        header('Content-Type: application/json; charset=utf-8');
    }

    if (empty($email) || empty($password)) {
        if ($isJson) {
            http_response_code(400);
            echo json_encode([
                "status" => "error",
                "success" => false,
                "message" => "Both username/email and password are required.",
                "debug" => [
                    "server" => "online",
                    "database" => "connected",
                    "error_type" => "EMPTY_FIELDS"
                ]
            ]);
            exit();
        }
        header("Location: /DeskStream/index.php?error=" . urlencode("All fields are required."));
        exit();
    }

    try {
        // Find user by email or username (case-insensitive & trimmed)
        $cleanEmail = strtolower($email);
        $stmt = $db->prepare("
            SELECT id, name, username, email, password_hash, unique_user_id 
            FROM users 
            WHERE LOWER(TRIM(email)) = ? OR LOWER(TRIM(username)) = ? 
            LIMIT 1
        ");
        $stmt->execute([$cleanEmail, $cleanEmail]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$user) {
            if ($isJson) {
                http_response_code(401);
                echo json_encode([
                    "status" => "error",
                    "success" => false,
                    "message" => "Account not found for username/email '$email'.",
                    "debug" => [
                        "server" => "online",
                        "database" => "connected",
                        "user_found" => false,
                        "query_input" => $email,
                        "error_type" => "USER_NOT_FOUND"
                    ]
                ]);
                exit();
            }
            header("Location: /DeskStream/index.php?error=" . urlencode("Account not found."));
            exit();
        }

        // Verify password with multi-format support (Bcrypt, Plaintext, MD5, SHA256)
        $storedHash = (string)($user['password_hash'] ?? '');
        $isPasswordCorrect = false;

        if (!empty($storedHash)) {
            if (password_verify($password, $storedHash)) {
                $isPasswordCorrect = true;
            } elseif ($password === $storedHash) {
                // Plaintext password match (e.g. legacy or manual DB entry)
                $isPasswordCorrect = true;
                // Automatically upgrade to secure bcrypt hash
                $newHash = password_hash($password, PASSWORD_DEFAULT);
                $upStmt = $db->prepare("UPDATE users SET password_hash = ? WHERE id = ?");
                $upStmt->execute([$newHash, $user['id']]);
            } elseif (hash_equals(strtolower($storedHash), md5($password))) {
                // MD5 match
                $isPasswordCorrect = true;
                $newHash = password_hash($password, PASSWORD_DEFAULT);
                $upStmt = $db->prepare("UPDATE users SET password_hash = ? WHERE id = ?");
                $upStmt->execute([$newHash, $user['id']]);
            } elseif (hash_equals(strtolower($storedHash), hash('sha256', $password))) {
                // SHA-256 match
                $isPasswordCorrect = true;
                $newHash = password_hash($password, PASSWORD_DEFAULT);
                $upStmt = $db->prepare("UPDATE users SET password_hash = ? WHERE id = ?");
                $upStmt->execute([$newHash, $user['id']]);
            }
        }

        if (!$isPasswordCorrect) {
            if ($isJson) {
                http_response_code(401);
                echo json_encode([
                    "status" => "error",
                    "success" => false,
                    "message" => "Incorrect password for '" . ($user['username'] ?: $email) . "'.",
                    "debug" => [
                        "server" => "online",
                        "database" => "connected",
                        "user_found" => true,
                        "username" => $user['username'] ?? '',
                        "email" => $user['email'] ?? '',
                        "error_type" => "PASSWORD_MISMATCH"
                    ]
                ]);
                exit();
            }
            header("Location: /DeskStream/index.php?error=" . urlencode("Invalid email or password."));
            exit();
        }

        // Use existing permanent unique_user_id — NEVER generate a new one
        $uniqueUserId = $user['unique_user_id'];
        if (empty($uniqueUserId)) {
            // Legacy account without unique_user_id — generate one now and store permanently
            do {
                $candidate = (string) random_int(100000000, 999999999);
                $check = $db->prepare("SELECT id FROM users WHERE unique_user_id = ? LIMIT 1");
                $check->execute([$candidate]);
            } while ($check->fetch());
            $update = $db->prepare("UPDATE users SET unique_user_id = ? WHERE id = ?");
            $update->execute([$candidate, $user['id']]);
            $uniqueUserId = $candidate;
        }

        // Establish clean server session
        $_SESSION['user_id'] = (int) $user['id'];
        $_SESSION['username'] = $user['username'];
        $_SESSION['email'] = $user['email'];
        $_SESSION['unique_user_id'] = $uniqueUserId;

        // Fetch user's registered system if already registered
        $sysStmt = $db->prepare("
            SELECT *, 
                   (last_seen_at >= (NOW() - INTERVAL 30 SECOND) AND is_online = 1) AS is_live_online 
            FROM devices 
            WHERE user_id = ? 
            ORDER BY is_live_online DESC, last_seen_at DESC, id ASC 
            LIMIT 1
        ");
        $sysStmt->execute([$user['id']]);
        $userSystem = $sysStmt->fetch(PDO::FETCH_ASSOC);

        if ($isJson) {
            echo json_encode([
                "status" => "success",
                "success" => true,
                "message" => "Login successful",
                "user" => [
                    "id" => (int) $user['id'],
                    "name" => $user['name'] ?: $user['username'],
                    "username" => $user['username'],
                    "email" => $user['email'],
                    "unique_user_id" => $uniqueUserId
                ],
                "system" => $userSystem ? [
                    "system_id" => (string) ($userSystem['system_id'] ?: $userSystem['device_uid']),
                    "device_name" => $userSystem['name'],
                    "status" => !empty($userSystem['is_live_online']) ? "online" : "offline"
                ] : null,
                "debug" => [
                    "server" => "online",
                    "database" => "connected"
                ]
            ]);
            exit();
        }

        header("Location: /DeskStream/dashboard.php");
        exit();

    } catch (\PDOException $e) {
        if ($isJson) {
            http_response_code(500);
            echo json_encode([
                "status" => "error", 
                "success" => false, 
                "message" => "Database error: " . $e->getMessage(),
                "debug" => [
                    "server" => "online",
                    "database" => "error",
                    "error_details" => $e->getMessage()
                ]
            ]);
            exit();
        }
        header("Location: /DeskStream/index.php?error=" . urlencode("Database error: " . $e->getMessage()));
        exit();
    }
} else {
    header("Location: /DeskStream/index.php");
    exit();
}