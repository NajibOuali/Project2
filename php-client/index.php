<?php
$config = require __DIR__ . '/config.php';
?>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>LLM Darija Translator - PHP Client</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="container">
    <header>
      <h1>🇲🇦 Darija Translator</h1>
      <p class="subtitle">PHP Web Client communicating with Jakarta RESTful Web Service</p>
    </header>

    <!-- Settings Collapsible -->
    <details class="card settings-card">
      <summary>⚙️ API & Authentication Configuration</summary>
      <div class="settings-grid">
        <div class="form-group">
          <label for="apiUrl">API URL</label>
          <input type="text" id="apiUrl" value="<?php echo htmlspecialchars($config['api_url']); ?>">
        </div>
        <div class="form-group">
          <label for="username">Username</label>
          <input type="text" id="username" value="<?php echo htmlspecialchars($config['username']); ?>">
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input type="password" id="password" value="<?php echo htmlspecialchars($config['password']); ?>">
        </div>
      </div>
    </details>

    <div id="errorBanner" class="error-banner hidden"></div>

    <!-- Translator Form -->
    <div class="card translator-grid">
      <!-- Input Panel -->
      <div class="panel">
        <div class="panel-header">
          <label for="inputText">English Text</label>
          <button id="clearBtn" class="icon-btn" title="Clear">🗑️ Clear</button>
        </div>
        <textarea id="inputText" rows="6" placeholder="Enter text in English (e.g. 'Hello, how are you?')"></textarea>
      </div>

      <!-- Output Panel -->
      <div class="panel">
        <div class="panel-header">
          <label>Moroccan Darija (الدارجة)</label>
          <button id="copyBtn" class="icon-btn" title="Copy translation" disabled>📋 Copy</button>
        </div>
        <div id="resultText" class="result-area placeholder">
          الترجمة ستظهر هنا...
        </div>
      </div>
    </div>

    <div class="actions">
      <button id="translateBtn" class="btn primary-btn">
        <span>Translate to Darija</span>
        <span id="spinner" class="spinner hidden"></span>
      </button>
    </div>
  </div>

  <script>
    const translateBtn = document.getElementById('translateBtn');
    const inputText = document.getElementById('inputText');
    const resultText = document.getElementById('resultText');
    const errorBanner = document.getElementById('errorBanner');
    const clearBtn = document.getElementById('clearBtn');
    const copyBtn = document.getElementById('copyBtn');
    const spinner = document.getElementById('spinner');

    clearBtn.addEventListener('click', () => {
      inputText.value = '';
      resultText.textContent = 'الترجمة ستظهر هنا...';
      resultText.classList.add('placeholder');
      copyBtn.disabled = true;
      hideError();
    });

    copyBtn.addEventListener('click', async () => {
      const text = resultText.textContent.trim();
      if (!text) return;
      await navigator.clipboard.writeText(text);
      copyBtn.textContent = '✅ Copied!';
      setTimeout(() => { copyBtn.textContent = '📋 Copy'; }, 2000);
    });

    translateBtn.addEventListener('click', async () => {
      const text = inputText.value.trim();
      if (!text) {
        showError('Please enter text to translate.');
        return;
      }

      hideError();
      translateBtn.disabled = true;
      spinner.classList.remove('hidden');

      try {
        const response = await fetch('translate.php', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            text: text,
            api_url: document.getElementById('apiUrl').value.trim(),
            username: document.getElementById('username').value.trim(),
            password: document.getElementById('password').value.trim()
          })
        });

        const data = await response.json();

        if (!response.ok) {
          throw new Error(data.error || `HTTP error ${response.status}`);
        }

        resultText.textContent = data.translatedText;
        resultText.classList.remove('placeholder');
        copyBtn.disabled = false;
      } catch (err) {
        showError(err.message || 'An error occurred during translation.');
        resultText.textContent = 'الترجمة ستظهر هنا...';
        resultText.classList.add('placeholder');
        copyBtn.disabled = true;
      } finally {
        translateBtn.disabled = false;
        spinner.classList.add('hidden');
      }
    });

    function showError(msg) {
      errorBanner.textContent = msg;
      errorBanner.classList.remove('hidden');
    }

    function hideError() {
      errorBanner.textContent = '';
      errorBanner.classList.add('hidden');
    }
  </script>
</body>
</html>
