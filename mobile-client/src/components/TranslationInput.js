import React from "react";
import { StyleSheet, Text, View, TextInput, TouchableOpacity, ActivityIndicator } from "react-native";
import { theme } from "../styles/theme";

export default function TranslationInput({ text, onChangeText, onTranslate, onClear, isLoading }) {
  return (
    <View style={styles.card}>
      <View style={styles.cardHeader}>
        <Text style={styles.label}>Source Text (English / Any)</Text>
        <TouchableOpacity onPress={onClear} style={styles.clearBtn}>
          <Text style={styles.clearText}>🗑️ Clear</Text>
        </TouchableOpacity>
      </View>

      <TextInput
        style={styles.input}
        multiline
        numberOfLines={4}
        value={text}
        onChangeText={onChangeText}
        placeholder="Enter text to translate..."
        placeholderTextColor={theme.colors.textMuted}
        textAlignVertical="top"
      />

      <TouchableOpacity
        style={[styles.translateBtn, isLoading && styles.translateBtnDisabled]}
        onPress={onTranslate}
        disabled={isLoading}
      >
        {isLoading ? (
          <ActivityIndicator color="#ffffff" size="small" />
        ) : (
          <Text style={styles.translateBtnText}>Translate to Darija ➔</Text>
        )}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: theme.colors.cardBg,
    borderRadius: theme.radius.md,
    padding: theme.spacing.md,
    borderWidth: 1,
    borderColor: theme.colors.border,
    shadowColor: "#000",
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
    marginVertical: theme.spacing.sm,
  },
  cardHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: theme.spacing.xs,
  },
  label: {
    fontSize: 13,
    fontWeight: "600",
    color: theme.colors.textMuted,
  },
  clearBtn: {
    padding: 2,
  },
  clearText: {
    fontSize: 12,
    color: theme.colors.textMuted,
  },
  input: {
    minHeight: 90,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.sm,
    padding: 10,
    fontSize: 15,
    color: theme.colors.textDark,
    marginBottom: theme.spacing.md,
  },
  translateBtn: {
    backgroundColor: theme.colors.primary,
    paddingVertical: 12,
    borderRadius: theme.radius.sm,
    alignItems: "center",
    justifyContent: "center",
  },
  translateBtnDisabled: {
    backgroundColor: theme.colors.primaryDark,
    opacity: 0.7,
  },
  translateBtnText: {
    color: "#ffffff",
    fontWeight: "700",
    fontSize: 15,
  },
});
