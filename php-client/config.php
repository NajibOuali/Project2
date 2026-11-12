<?php
/**
 * Configuration for PHP Darija Translator Client
 */

return [
    'api_url' => getenv('TRANSLATOR_API_URL') ?: 'http://localhost:8080/backend/api/translator/translate',
    'username' => getenv('TRANSLATOR_API_USER') ?: 'user',
    'password' => getenv('TRANSLATOR_API_PASS') ?: 'darija123',
    'timeout'  => 15
];
