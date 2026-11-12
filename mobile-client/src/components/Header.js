import React from "react";
import { StyleSheet, Text, View, TouchableOpacity } from "react-native";
import { theme } from "../styles/theme";

export default function Header({ onToggleSettings }) {
  return (
    <View style={styles.container}>
      <View style={styles.titleContainer}>
        <Text style={styles.flag}>🇲🇦</Text>
        <Text style={styles.title}>Darija Translator</Text>
      </View>
      <TouchableOpacity style={styles.settingsBtn} onPress={onToggleSettings}>
        <Text style={styles.settingsIcon}>⚙️</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingVertical: theme.spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  titleContainer: {
    flexDirection: "row",
    alignItems: "center",
  },
  flag: {
    fontSize: 24,
    marginRight: 8,
  },
  title: {
    fontSize: 20,
    fontWeight: "700",
    color: theme.colors.primary,
  },
  settingsBtn: {
    padding: theme.spacing.xs,
  },
  settingsIcon: {
    fontSize: 20,
  },
});
