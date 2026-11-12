<?php
/**
 * Translation backend script using PHP cURL to call the Jakarta REST API
 */

header('Content-Type: application/json; charset=UTF-8');

$config = require __DIR__ . '/config.php';

// Accept JSON or POST Form data
$inputData = json_decode(file_get_contents('php://input'), true);
$text = null;

if (isset($inputData['text'])) {
    $text = trim($inputData['text']);
} elseif (isset($_POST['text'])) {
    $text = trim($_POST['text']);
}

// Allow user-overridden credentials from request if provided
$username = isset($inputData['username']) && $inputData['username'] !== '' ? $inputData['username'] : $config['username'];
$password = isset($inputData['password']) && $inputData['password'] !== '' ? $inputData['password'] : $config['password'];
$apiUrl   = isset($inputData['api_url'])  && $inputData['api_url'] !== ''  ? $inputData['api_url']  : $config['api_url'];

if (empty($text)) {
    http_response_code(400);
    echo json_encode([
        'error' => 'Please provide non-empty text to translate.',
        'status' => 400
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$payload = json_encode(['text' => $text], JSON_UNESCAPED_UNICODE);

// Initialize cURL
$ch = curl_init();
curl_setopt_array($ch, [
    CURLOPT_URL => $apiUrl,
    CURLOPT_POST => true,
    CURLOPT_POSTFIELDS => $payload,
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_TIMEOUT => $config['timeout'],
    CURLOPT_USERPWD => $username . ':' . $password,
    CURLOPT_HTTPHEADER => [
        'Content-Type: application/json; charset=UTF-8',
        'Accept: application/json'
    ]
]);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$curlError = curl_error($ch);
curl_close($ch);

if ($curlError) {
    http_response_code(500);
    echo json_encode([
        'error' => 'Connection error: ' . $curlError,
        'status' => 500
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

http_response_code($httpCode);
echo $response;
