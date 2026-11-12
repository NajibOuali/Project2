package com.translator.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service responsible for communicating with the Google Gemini Generative Language API.
 */
@ApplicationScoped
public class GeminiTranslationService {

    private static final Logger LOGGER = Logger.getLogger(GeminiTranslationService.class.getName());

    private static final String DEFAULT_MODEL = "gemini-3.1-flash-lite";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private static final String SYSTEM_INSTRUCTION =
            "You are a professional Moroccan Darija translator. " +
            "Translate the provided text into authentic Moroccan Arabic Darija. " +
            "Use natural expressions commonly spoken in Morocco rather than Modern Standard Arabic. " +
            "Preserve the original meaning and tone. " +
            "Return only the translation without explanations.";

    private final HttpClient httpClient;

    public GeminiTranslationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    public GeminiTranslationService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Translates input text into Moroccan Darija using Google Gemini API.
     *
     * @param text Original text to translate
     * @return Translated text in Moroccan Darija
     * @throws IllegalArgumentException if input is empty
     * @throws IllegalStateException if API key is missing or API returns an error
     */
    public String translate(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Translation text cannot be empty or blank.");
        }

        String apiKey = getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            LOGGER.severe("GEMINI_API_KEY environment variable is not configured.");
            throw new IllegalStateException("Gemini API key is not configured on the server.");
        }

        String model = getModelName();
        String endpointUrl = String.format(GEMINI_API_URL, model, apiKey);

        try {
            String requestPayload = buildRequestPayload(text.trim());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.log(Level.WARNING, "Gemini API returned non-200 status code: {0}", response.statusCode());
                String errorMessage = extractErrorMessage(response.body(), response.statusCode());
                throw new IllegalStateException("Translation service failed: " + errorMessage);
            }

            return extractTranslationFromResponse(response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Gemini API request interrupted", e);
            throw new IllegalStateException("Translation request was interrupted.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error communicating with Gemini API", e);
            throw new IllegalStateException("Unable to communicate with translation service: " + e.getMessage());
        }
    }

    /**
     * Builds the JSON request payload for Gemini API with system instructions.
     */
    protected String buildRequestPayload(String text) {
        JsonObject payload = Json.createObjectBuilder()
                .add("system_instruction", Json.createObjectBuilder()
                        .add("parts", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder()
                                        .add("text", SYSTEM_INSTRUCTION))))
                .add("contents", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("parts", Json.createArrayBuilder()
                                        .add(Json.createObjectBuilder()
                                                .add("text", text)))))
                .add("generationConfig", Json.createObjectBuilder()
                        .add("temperature", 0.3)
                        .add("maxOutputTokens", 1024))
                .build();

        return payload.toString();
    }

    /**
     * Parses the JSON response and extracts the generated translation text.
     */
    protected String extractTranslationFromResponse(String responseBody) {
        try (JsonReader reader = Json.createReader(new StringReader(responseBody))) {
            JsonObject root = reader.readObject();
            JsonArray candidates = root.getJsonArray("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                JsonObject firstCandidate = candidates.getJsonObject(0);
                JsonObject content = firstCandidate.getJsonObject("content");
                if (content != null) {
                    JsonArray parts = content.getJsonArray("parts");
                    if (parts != null && !parts.isEmpty()) {
                        String translatedText = parts.getJsonObject(0).getString("text", "").trim();
                        if (!translatedText.isEmpty()) {
                            return translatedText;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse Gemini API response JSON", e);
        }

        throw new IllegalStateException("Received empty translation from Gemini API.");
    }

    /**
     * Extracts readable error messages from the response body.
     */
    private String extractErrorMessage(String responseBody, int statusCode) {
        try (JsonReader reader = Json.createReader(new StringReader(responseBody))) {
            JsonObject root = reader.readObject();
            if (root.containsKey("error")) {
                JsonObject errorObj = root.getJsonObject("error");
                String message = errorObj.getString("message", null);
                if (message != null && !message.isEmpty()) {
                    return message;
                }
            }
        } catch (Exception ignored) {
        }
        return "HTTP Error " + statusCode;
    }

    protected String getApiKey() {
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }
        return System.getProperty("GEMINI_API_KEY");
    }

    protected String getModelName() {
        String envModel = System.getenv("GEMINI_MODEL");
        if (envModel != null && !envModel.trim().isEmpty()) {
            return envModel.trim();
        }
        String propModel = System.getProperty("GEMINI_MODEL");
        if (propModel != null && !propModel.trim().isEmpty()) {
            return propModel.trim();
        }
        return DEFAULT_MODEL;
    }
}
