<?php
// /DeskStream/api/devices/heartbeat.php

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit();
}

require_once __DIR__ . '/../../config/database.php';

// Support either $pdo or $conn from database.php
$db = $pdo ?? $conn ?? null;

if (!$db) {
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "message" => "Database connection unavailable."
    ]);
    exit();
}

// Only POST is allowed
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        "status" => "error",
        "message" => "Method not allowed. Use POST."
    ]);
    exit();
}

// Read JSON request
$rawInput = file_get_contents('php://input');
$data = json_decode($rawInput, true);

if (!is_array($data)) {
    http_response_code(400);
    echo json_encode([
        "status" => "error",
        "message" => "Invalid JSON request."
    ]);
    exit();
}

/*
 * Accept the different names that the desktop agent may send.
 */
$systemId = preg_replace(
    '/[^0-9]/',
    '',
    (string) (
        $data['system_id']
        ?? $data['device_uid']
        ?? $data['device_id']
        ?? ''
    )
);

$machineId = trim(
    (string) (
        $data['machine_identifier']
        ?? $data['device_uuid']
        ?? ''
    )
);

/*
 * Optional user ID.
 * Heartbeat does NOT create a device.
 * It only keeps an already registered device online.
 */
$userId = isset($data['user_id']) && $data['user_id'] !== ''
    ? (int)$data['user_id']
    : null;

if ($systemId === '' && $machineId === '') {
    http_response_code(400);
    echo json_encode([
        "status" => "error",
        "message" => "Missing system_id or machine_identifier."
    ]);
    exit();
}

try {

    /*
     * Find the existing device.
     *
     * We match using:
     *   1. system_id
     *   2. device_uid
     *   3. machine_identifier
     */
    if ($systemId !== '' && $machineId !== '') {

        if ($userId !== null) {

            $stmt = $db->prepare("
                UPDATE devices
                SET
                    is_online = 1,
                    last_seen_at = NOW(),
                    updated_at = NOW()
                WHERE
                    user_id = ?
                    AND (
                        system_id = ?
                        OR device_uid = ?
                        OR (
                            machine_identifier IS NOT NULL
                            AND machine_identifier <> ''
                            AND machine_identifier = ?
                        )
                    )
            ");

            $stmt->execute([
                $userId,
                $systemId,
                $systemId,
                $machineId
            ]);

        } else {

            $stmt = $db->prepare("
                UPDATE devices
                SET
                    is_online = 1,
                    last_seen_at = NOW(),
                    updated_at = NOW()
                WHERE
                    system_id = ?
                    OR device_uid = ?
                    OR (
                        machine_identifier IS NOT NULL
                        AND machine_identifier <> ''
                        AND machine_identifier = ?
                    )
            ");

            $stmt->execute([
                $systemId,
                $systemId,
                $machineId
            ]);
        }

    } elseif ($systemId !== '') {

        if ($userId !== null) {

            $stmt = $db->prepare("
                UPDATE devices
                SET
                    is_online = 1,
                    last_seen_at = NOW(),
                    updated_at = NOW()
                WHERE
                    user_id = ?
                    AND (
                        system_id = ?
                        OR device_uid = ?
                    )
            ");

            $stmt->execute([
                $userId,
                $systemId,
                $systemId
            ]);

        } else {

            $stmt = $db->prepare("
                UPDATE devices
                SET
                    is_online = 1,
                    last_seen_at = NOW(),
                    updated_at = NOW()
                WHERE
                    system_id = ?
                    OR device_uid = ?
            ");

            $stmt->execute([
                $systemId,
                $systemId
            ]);
        }

    } else {

        if ($userId !== null) {

            $stmt = $db->prepare("
                UPDATE devices
                SET
                    is_online = 1,
                    last_seen_at = NOW(),
                    updated_at = NOW()
                WHERE
                    user_id = ?
                    AND machine_identifier = ?
            ");

            $stmt->execute([
                $userId,
                $machineId
            ]);

        } else {

            $stmt = $db->prepare("
                UPDATE devices
                SET
                    is_online = 1,
                    last_seen_at = NOW(),
                    updated_at = NOW()
                WHERE
                    machine_identifier = ?
            ");

            $stmt->execute([
                $machineId
            ]);
        }
    }

    $affected = $stmt->rowCount();

    /*
     * If no existing device was found, return a clear response.
     *
     * IMPORTANT:
     * Heartbeat does not INSERT a new device.
     * Registration should be handled by the device registration endpoint.
     */
    if ($affected === 0) {

        http_response_code(404);

        echo json_encode([
            "status" => "error",
            "message" => "Device is not registered.",
            "updated" => false,
            "system_id" => $systemId,
            "machine_identifier" => $machineId
        ]);

        exit();
    }

    /*
     * Successfully updated an existing device.
     */
    echo json_encode([
        "status" => "success",
        "message" => "Heartbeat received.",
        "updated" => true,
        "system_id" => $systemId,
        "machine_identifier" => $machineId,
        "is_online" => true,
        "last_seen_at" => date('Y-m-d H:i:s')
    ]);

} catch (PDOException $e) {

    http_response_code(500);

    echo json_encode([
        "status" => "error",
        "message" => "Database error.",
        "details" => $e->getMessage()
    ]);
}