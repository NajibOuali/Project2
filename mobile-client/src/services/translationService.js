/**
 * Translation API service for the React Native mobile client.
 */

// Base64 helper for React Native without external dependencies
function encodeBase64(str) {
  const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
  let output = "";
  for (let block = 0, charCode, i = 0, map = chars;
       str.charAt(i | 0) || (map = "=", i % 1);
       output += map.charAt(63 & block >> 8 - i % 1 * 8)) {
    charCode = str.charCodeAt(i += 3/4);
    if (charCode > 0xFF) {
      throw new Error("Invalid character found in string");
    }
    block = block << 8 | charCode;
  }
  return output;
}

export async function requestTranslation({ text, apiUrl, username, password }) {
  if (!text || !text.trim()) {
    throw new Error("Text to translate cannot be empty.");
  }

  const credentials = encodeBase64(`${username}:${password}`);
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 15000);

  try {
    const response = await fetch(apiUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Accept": "application/json",
        "Authorization": `Basic ${credentials}`,
      },
      body: JSON.stringify({ text: text.trim() }),
      signal: controller.signal,
    });

    clearTimeout(timeoutId);

    const data = await response.json();

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("Authentication failed (401). Check username & password.");
      }
      throw new Error(data.error || `Server returned error (${response.status})`);
    }

    return data;
  } catch (error) {
    clearTimeout(timeoutId);
    if (error.name === "AbortError") {
      throw new Error("Request timed out. Please check server IP and connectivity.");
    }
    throw error;
  }
}
