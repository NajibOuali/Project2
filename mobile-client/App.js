import React, { useState, useEffect } from "react";
import {
  StyleSheet,
  Text,
  View,
  SafeAreaView,
  ScrollView,
  TextInput,
  TouchableOpacity,
  Alert,
  StatusBar,
} from "react-native";
import * as Speech from "expo-speech";
import * as Clipboard from "expo-clipboard";
import AsyncStorage from "@react-native-async-storage/async-storage";

import Header from "./src/components/Header";
import TranslationInput from "./src/components/TranslationInput";
import TranslationResult from "./src/components/TranslationResult";
import HistoryList from "./src/components/HistoryList";
import { API_CONFIG } from "./src/config/apiConfig";
import { requestTranslation } from "./src/services/translationService";
import { theme } from "./src/styles/theme";

const STORAGE_KEY_HISTORY = "@darija_translator_history";
const STORAGE_KEY_SETTINGS = "@darija_translator_settings";

export default function App() {
  const [inputText, setInputText] = useState("Hello, how are you?");
  const [translatedText, setTranslatedText] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // Settings
  const [showSettings, setShowSettings] = useState(false);
  const [apiUrl, setApiUrl] = useState(API_CONFIG.defaultBaseUrl);
  const [username, setUsername] = useState(API_CONFIG.defaultUsername);
  const [password, setPassword] = useState(API_CONFIG.defaultPassword);

  // History
  const [history, setHistory] = useState([]);

  useEffect(() => {
    loadSettings();
    loadHistory();
  }, []);

  const loadSettings = async () => {
    try {
      const saved = await AsyncStorage.getItem(STORAGE_KEY_SETTINGS);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.apiUrl) setApiUrl(parsed.apiUrl);
        if (parsed.username) setUsername(parsed.username);
        if (parsed.password) setPassword(parsed.password);
      }
    } catch (e) {
      console.warn("Failed to load settings from storage", e);
    }
  };

  const saveSettings = async () => {
    try {
      await AsyncStorage.setItem(
        STORAGE_KEY_SETTINGS,
        JSON.stringify({ apiUrl, username, password })
      );
      Alert.alert("Success", "Settings saved successfully!");
      setShowSettings(false);
    } catch (e) {
      Alert.alert("Error", "Failed to save settings.");
    }
  };

  const loadHistory = async () => {
    try {
      const saved = await AsyncStorage.getItem(STORAGE_KEY_HISTORY);
      if (saved) {
        setHistory(JSON.parse(saved));
      }
    } catch (e) {
      console.warn("Failed to load history from storage", e);
    }
  };

  const addToHistory = async (source, target) => {
    try {
      const newItem = {
        id: Date.now(),
        source,
        target,
      };
      const updated = [newItem, ...history.slice(0, 19)];
      setHistory(updated);
      await AsyncStorage.setItem(STORAGE_KEY_HISTORY, JSON.stringify(updated));
    } catch (e) {
      console.warn("Failed to save history item", e);
    }
  };

  const clearHistory = async () => {
    try {
      setHistory([]);
      await AsyncStorage.removeItem(STORAGE_KEY_HISTORY);
    } catch (e) {
      console.warn("Failed to clear history", e);
    }
  };

  const handleTranslate = async () => {
    if (!inputText || !inputText.trim()) {
      Alert.alert("Warning", "Please enter some text to translate.");
      return;
    }

    setErrorMessage("");
    setIsLoading(true);

    try {
      const result = await requestTranslation({
        text: inputText,
        apiUrl,
        username,
        password,
      });

      const translated = result.translatedText || "";
      setTranslatedText(translated);
      await addToHistory(inputText.trim(), translated);
    } catch (error) {
      setErrorMessage(error.message || "Failed to complete translation.");
      Alert.alert("Translation Failed", error.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClear = () => {
    setInputText("");
    setTranslatedText("");
    setErrorMessage("");
  };

  const handleCopy = async () => {
    if (!translatedText) return;
    await Clipboard.setStringAsync(translatedText);
    Alert.alert("Copied", "Translation copied to clipboard!");
  };

  const handleSpeak = () => {
    if (!translatedText) return;
    Speech.stop();
    Speech.speak(translatedText, {
      language: "ar-MA",
      rate: 0.85,
    });
  };

  const handleSelectHistoryItem = (item) => {
    setInputText(item.source);
    setTranslatedText(item.target);
    setErrorMessage("");
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#ffffff" />
      <ScrollView contentContainerStyle={styles.container}>
        <Header onToggleSettings={() => setShowSettings(!showSettings)} />

        {/* Collapsible Settings Panel */}
        {showSettings && (
          <View style={styles.settingsCard}>
            <Text style={styles.settingsTitle}>Server Connection Settings</Text>
            <Text style={styles.inputLabel}>API URL (use your PC's LAN IP):</Text>
            <TextInput
              style={styles.settingsInput}
              value={apiUrl}
              onChangeText={setApiUrl}
              autoCapitalize="none"
              autoCorrect={false}
            />

            <View style={styles.rowInputs}>
              <View style={styles.halfInput}>
                <Text style={styles.inputLabel}>Username:</Text>
                <TextInput
                  style={styles.settingsInput}
                  value={username}
                  onChangeText={setUsername}
                  autoCapitalize="none"
                />
              </View>
              <View style={styles.halfInput}>
                <Text style={styles.inputLabel}>Password:</Text>
                <TextInput
                  style={styles.settingsInput}
                  value={password}
                  onChangeText={setPassword}
                  secureTextEntry
                />
              </View>
            </View>

            <TouchableOpacity style={styles.saveBtn} onPress={saveSettings}>
              <Text style={styles.saveBtnText}>Save Settings</Text>
            </TouchableOpacity>
          </View>
        )}

        {errorMessage ? (
          <View style={styles.errorBanner}>
            <Text style={styles.errorText}>{errorMessage}</Text>
          </View>
        ) : null}

        <TranslationInput
          text={inputText}
          onChangeText={setInputText}
          onTranslate={handleTranslate}
          onClear={handleClear}
          isLoading={isLoading}
        />

        <TranslationResult
          translatedText={translatedText}
          onCopy={handleCopy}
          onSpeak={handleSpeak}
        />

        <HistoryList
          history={history}
          onSelectItem={handleSelectHistoryItem}
          onClearHistory={clearHistory}
        />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  container: {
    padding: theme.spacing.md,
  },
  settingsCard: {
    backgroundColor: "#f1f5f9",
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    padding: theme.spacing.md,
    marginVertical: theme.spacing.sm,
  },
  settingsTitle: {
    fontSize: 14,
    fontWeight: "700",
    color: theme.colors.textDark,
    marginBottom: theme.spacing.xs,
  },
  inputLabel: {
    fontSize: 12,
    fontWeight: "600",
    color: theme.colors.textMuted,
    marginTop: 6,
    marginBottom: 2,
  },
  settingsInput: {
    backgroundColor: "#ffffff",
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.sm,
    padding: 8,
    fontSize: 13,
  },
  rowInputs: {
    flexDirection: "row",
    gap: 8,
  },
  halfInput: {
    flex: 1,
  },
  saveBtn: {
    backgroundColor: theme.colors.secondary,
    paddingVertical: 8,
    borderRadius: theme.radius.sm,
    alignItems: "center",
    marginTop: 10,
  },
  saveBtnText: {
    color: "#ffffff",
    fontWeight: "600",
    fontSize: 13,
  },
  errorBanner: {
    backgroundColor: "#fef2f2",
    borderWidth: 1,
    borderColor: "#fecaca",
    borderRadius: theme.radius.sm,
    padding: 10,
    marginVertical: 4,
  },
  errorText: {
    color: theme.colors.danger,
    fontSize: 13,
  },
});
