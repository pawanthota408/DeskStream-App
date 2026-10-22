<?php
// backend/api/connections/cancel.php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if (($_SERVER['REQUEST_METHOD'] ?? '') === 'OPTIONS') {
    exit(0);
}

require_once __DIR__ . '/../../config/database.php';

if (($_SERVER['REQUEST_METHOD'] ?? '') !== 'POST') {
    http_response_code(405);
    echo json_encode(["status" => "error", "message" => "Method not allowed"]);
    exit();
}

$rawInput = file_get_contents('php://input');
$data = json_decode($rawInput, true) ?: [];

$token = trim($data['token'] ?? $data['request_token'] ?? $_POST['token'] ?? '');

if (empty($token)) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "Missing request token."]);
    exit();
}

try {
    $stmt = $pdo->prepare("UPDATE connection_requests SET status = 'cancelled' WHERE request_token = ?");
    $stmt->execute([$token]);
    echo json_encode(["status" => "success", "message" => "Connection request cancelled."]);
} catch (\PDOException $e) {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
