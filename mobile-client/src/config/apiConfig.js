/**
 * Mobile API configuration.
 * Note: Use your machine's local network IP (e.g., http://192.168.1.100:8080/backend/api)
 * instead of 'localhost' when running on a physical phone or external emulator.
 */
export const API_CONFIG = {
  // Replace 10.0.2.2 (Android emulator host loopback) or local LAN IP as appropriate
  defaultBaseUrl: "http://10.0.2.2:8080/backend/api/translator/translate",
  defaultUsername: "user",
  defaultPassword: "darija123",
  timeoutMs: 15000,
};
