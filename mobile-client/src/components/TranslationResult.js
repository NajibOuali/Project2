import React from "react";
import { StyleSheet, Text, View, TouchableOpacity } from "react-native";
import { theme } from "../styles/theme";

export default function TranslationResult({ translatedText, onCopy, onSpeak }) {
  const hasResult = Boolean(translatedText && translatedText.trim());

  return (
    <View style={styles.card}>
      <View style={styles.cardHeader}>
        <Text style={styles.label}>Moroccan Darija (الدارجة)</Text>
        <View style={styles.actionRow}>
          <TouchableOpacity
            style={[styles.actionBtn, !hasResult && styles.actionBtnDisabled]}
            onPress={onSpeak}
            disabled={!hasResult}
          >
            <Text style={styles.actionIcon}>🔊</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.actionBtn, !hasResult && styles.actionBtnDisabled]}
            onPress={onCopy}
            disabled={!hasResult}
          >
            <Text style={styles.actionIcon}>📋</Text>
          </TouchableOpacity>
        </View>
      </View>

      <View style={styles.resultBox}>
        <Text style={[styles.resultText, !hasResult && styles.placeholderText]}>
          {hasResult ? translatedText : "الترجمة ستظهر هنا..."}
        </Text>
      </View>
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
  actionRow: {
    flexDirection: "row",
    gap: 8,
  },
  actionBtn: {
    padding: 4,
  },
  actionBtnDisabled: {
    opacity: 0.3,
  },
  actionIcon: {
    fontSize: 16,
  },
  resultBox: {
    minHeight: 80,
    backgroundColor: "#f8fafc",
    borderWidth: 1,
    borderStyle: "dashed",
    borderColor: theme.colors.border,
    borderRadius: theme.radius.sm,
    padding: 12,
    justifyContent: "center",
  },
  resultText: {
    fontSize: 18,
    color: theme.colors.textDark,
    textAlign: "right",
    writingDirection: "rtl",
    lineHeight: 26,
  },
  placeholderText: {
    color: theme.colors.textMuted,
    fontSize: 14,
    textAlign: "left",
    writingDirection: "ltr",
  },
});
