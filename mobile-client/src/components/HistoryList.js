import React from "react";
import { StyleSheet, Text, View, FlatList, TouchableOpacity } from "react-native";
import { theme } from "../styles/theme";

export default function HistoryList({ history, onSelectItem, onClearHistory }) {
  if (!history || history.length === 0) {
    return (
      <View style={styles.emptyContainer}>
        <Text style={styles.emptyText}>No translation history yet.</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Recent Translations</Text>
        <TouchableOpacity onPress={onClearHistory}>
          <Text style={styles.clearBtnText}>Clear</Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={history}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => (
          <TouchableOpacity style={styles.item} onPress={() => onSelectItem(item)}>
            <Text style={styles.sourceText} numberOfLines={1}>{item.source}</Text>
            <Text style={styles.targetText} numberOfLines={1}>{item.target}</Text>
          </TouchableOpacity>
        )}
        scrollEnabled={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginTop: theme.spacing.md,
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: theme.spacing.xs,
  },
  headerTitle: {
    fontSize: 14,
    fontWeight: "600",
    color: theme.colors.textMuted,
  },
  clearBtnText: {
    fontSize: 12,
    color: theme.colors.primary,
  },
  item: {
    backgroundColor: theme.colors.cardBg,
    borderRadius: theme.radius.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    padding: theme.spacing.sm,
    marginBottom: theme.spacing.xs,
  },
  sourceText: {
    fontSize: 13,
    color: theme.colors.textMuted,
  },
  targetText: {
    fontSize: 15,
    fontWeight: "600",
    color: theme.colors.textDark,
    textAlign: "right",
    writingDirection: "rtl",
    marginTop: 2,
  },
  emptyContainer: {
    padding: theme.spacing.lg,
    alignItems: "center",
  },
  emptyText: {
    fontSize: 13,
    color: theme.colors.textMuted,
  },
});
