// Side Panel UI and API controller for Chrome Extension

document.addEventListener("DOMContentLoaded", async () => {
  const sourceTextEl = document.getElementById("source-text");
  const translateBtn = document.getElementById("translate-btn");
  const btnText = document.getElementById("btn-text");
  const loadingSpinner = document.getElementById("loading-spinner");
  const resultBox = document.getElementById("result-box");
  const errorBox = document.getElementById("error-box");
  const copyBtn = document.getElementById("copy-btn");
  const speakBtn = document.getElementById("speak-btn");
  const micBtn = document.getElementById("mic-btn");
  const clearBtn = document.getElementById("clear-btn");
  const historyList = document.getElementById("history-list");
  const clearHistoryBtn = document.getElementById("clear-history-btn");

  const toggleSettingsBtn = document.getElementById("toggle-settings-btn");
  const settingsPanel = document.getElementById("settings-panel");
  const saveSettingsBtn = document.getElementById("save-settings-btn");
  const apiUrlInput = document.getElementById("api-url");
  const apiUsernameInput = document.getElementById("api-username");
  const apiPasswordInput = document.getElementById("api-password");
  const settingsMsg = document.getElementById("settings-msg");

  let currentTranslation = "";

  // Load saved settings
  const config = await chrome.storage.sync.get({
    apiUrl: "http://localhost:8080/backend/api/translator/translate",
    apiUsername: "user",
    apiPassword: "darija123"
  });

  apiUrlInput.value = config.apiUrl;
  apiUsernameInput.value = config.apiUsername;
  apiPasswordInput.value = config.apiPassword;

  // Load Translation History
  loadHistory();

  // Check for pending translation from Context Menu
  const localData = await chrome.storage.local.get(["pendingTranslation"]);
  if (localData.pendingTranslation) {
    sourceTextEl.value = localData.pendingTranslation;
    await chrome.storage.local.remove("pendingTranslation");
    performTranslation(sourceTextEl.value.trim());
  }

  // Listen for runtime messages from background service worker
  chrome.runtime.onMessage.addListener((message) => {
    if (message.action === "AUTO_TRANSLATE" && message.text) {
      sourceTextEl.value = message.text;
      performTranslation(message.text.trim());
    }
  });

  // Toggle Settings
  toggleSettingsBtn.addEventListener("click", () => {
    settingsPanel.classList.toggle("hidden");
  });

  // Save Settings
  saveSettingsBtn.addEventListener("click", async () => {
    await chrome.storage.sync.set({
      apiUrl: apiUrlInput.value.trim(),
      apiUsername: apiUsernameInput.value.trim(),
      apiPassword: apiPasswordInput.value.trim()
    });
    settingsMsg.textContent = "Settings saved successfully!";
    setTimeout(() => { settingsMsg.textContent = ""; }, 3000);
  });

  // Clear Input
  clearBtn.addEventListener("click", () => {
    sourceTextEl.value = "";
    resultBox.textContent = "الترجمة ستظهر هنا...";
    resultBox.classList.add("placeholder");
    currentTranslation = "";
    copyBtn.disabled = true;
    speakBtn.disabled = true;
    hideError();
  });

  // Translate Action
  translateBtn.addEventListener("click", () => {
    const text = sourceTextEl.value.trim();
    if (!text) {
      showError("Please enter some text to translate.");
      return;
    }
    performTranslation(text);
  });

  // Copy Translation
  copyBtn.addEventListener("click", async () => {
    if (!currentTranslation) return;
    try {
      await navigator.clipboard.writeText(currentTranslation);
      const originalText = copyBtn.textContent;
      copyBtn.textContent = "✅";
      setTimeout(() => { copyBtn.textContent = originalText; }, 2000);
    } catch (e) {
      console.error("Clipboard copy failed:", e);
    }
  });

  // Read Aloud (Text to Speech)
  speakBtn.addEventListener("click", () => {
    if (!currentTranslation) return;
    if ("speechSynthesis" in window) {
      window.speechSynthesis.cancel(); // Stop any ongoing speech
      const utterance = new SpeechSynthesisUtterance(currentTranslation);
      utterance.lang = "ar-MA"; // Moroccan Arabic dial / Arabic
      utterance.rate = 0.9;
      window.speechSynthesis.speak(utterance);
    } else {
      alert("Text-to-speech is not supported in this browser.");
    }
  });

  // Speech Input (Voice Recognition)
  let recognition = null;
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

  if (SpeechRecognition) {
    recognition = new SpeechRecognition();
    recognition.continuous = false;
    recognition.interimResults = false;
    recognition.lang = "en-US";

    recognition.onstart = () => {
      micBtn.style.backgroundColor = "#fee2e2";
      micBtn.textContent = "🔴";
    };

    recognition.onresult = (event) => {
      const transcript = event.results[0][0].transcript;
      sourceTextEl.value = transcript;
      performTranslation(transcript);
    };

    recognition.onerror = (event) => {
      console.error("Speech recognition error:", event.error);
      micBtn.style.backgroundColor = "transparent";
      micBtn.textContent = "🎤";
    };

    recognition.onend = () => {
      micBtn.style.backgroundColor = "transparent";
      micBtn.textContent = "🎤";
    };

    micBtn.addEventListener("click", () => {
      try {
        recognition.start();
      } catch (e) {
        recognition.stop();
      }
    });
  } else {
    micBtn.title = "Speech recognition is not supported in this browser environment.";
    micBtn.disabled = true;
  }

  // Clear History
  clearHistoryBtn.addEventListener("click", async () => {
    await chrome.storage.local.set({ translationHistory: [] });
    loadHistory();
  });

  // Perform Translation Request
  async function performTranslation(text) {
    hideError();
    setLoading(true);

    try {
      const settings = await chrome.storage.sync.get({
        apiUrl: "http://localhost:8080/backend/api/translator/translate",
        apiUsername: "user",
        apiPassword: "darija123"
      });

      const basicAuthHeader = "Basic " + btoa(`${settings.apiUsername}:${settings.apiPassword}`);

      const response = await fetch(settings.apiUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": basicAuthHeader
        },
        body: JSON.stringify({ text: text })
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || `Server responded with status ${response.status}`);
      }

      currentTranslation = data.translatedText;
      resultBox.textContent = currentTranslation;
      resultBox.classList.remove("placeholder");
      copyBtn.disabled = false;
      speakBtn.disabled = false;

      // Save to history
      await saveToHistory(text, currentTranslation);
    } catch (err) {
      showError(err.message || "Failed to connect to translation server.");
      resultBox.textContent = "الترجمة ستظهر هنا...";
      resultBox.classList.add("placeholder");
      copyBtn.disabled = true;
      speakBtn.disabled = true;
    } finally {
      setLoading(false);
    }
  }

  function setLoading(isLoading) {
    if (isLoading) {
      translateBtn.disabled = true;
      btnText.classList.add("hidden");
      loadingSpinner.classList.remove("hidden");
    } else {
      translateBtn.disabled = false;
      btnText.classList.remove("hidden");
      loadingSpinner.classList.add("hidden");
    }
  }

  function showError(msg) {
    errorBox.textContent = msg;
    errorBox.classList.remove("hidden");
  }

  function hideError() {
    errorBox.textContent = "";
    errorBox.classList.add("hidden");
  }

  async function saveToHistory(source, target) {
    const data = await chrome.storage.local.get({ translationHistory: [] });
    const history = data.translationHistory;

    // Add new item at the top, limit to 20
    history.unshift({
      id: Date.now(),
      source: source,
      target: target,
      timestamp: new Date().toLocaleTimeString()
    });

    if (history.length > 20) {
      history.pop();
    }

    await chrome.storage.local.set({ translationHistory: history });
    loadHistory();
  }

  async function loadHistory() {
    const data = await chrome.storage.local.get({ translationHistory: [] });
    const history = data.translationHistory;

    historyList.innerHTML = "";
    if (history.length === 0) {
      historyList.innerHTML = '<li class="empty-history">No translations yet.</li>';
      return;
    }

    history.forEach(item => {
      const li = document.createElement("li");
      li.className = "history-item";
      li.innerHTML = `
        <div class="history-source">${escapeHtml(item.source)}</div>
        <div class="history-target">${escapeHtml(item.target)}</div>
      `;
      li.addEventListener("click", () => {
        sourceTextEl.value = item.source;
        currentTranslation = item.target;
        resultBox.textContent = item.target;
        resultBox.classList.remove("placeholder");
        copyBtn.disabled = false;
        speakBtn.disabled = false;
      });
      historyList.appendChild(li);
    });
  }

  function escapeHtml(str) {
    return str.replace(/[&<>'"]/g, 
      tag => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        "'": '&#39;',
        '"': '&quot;'
      }[tag] || tag)
    );
  }
});
