package com.translator.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GeminiTranslationService.
 */
public class GeminiTranslationServiceTest {

    @Test
    @DisplayName("Translate should throw IllegalArgumentException for null or empty input")
    void testTranslateEmptyInput() {
        GeminiTranslationService service = new GeminiTranslationService();
        assertThrows(IllegalArgumentException.class, () -> service.translate(null));
        assertThrows(IllegalArgumentException.class, () -> service.translate(""));
        assertThrows(IllegalArgumentException.class, () -> service.translate("   "));
    }

    @Test
    @DisplayName("Translate should throw IllegalStateException when GEMINI_API_KEY is missing")
    void testTranslateMissingApiKey() {
        GeminiTranslationService service = new GeminiTranslationService() {
            @Override
            protected String getApiKey() {
                return null;
            }
        };

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.translate("Hello"));
        assertTrue(exception.getMessage().contains("API key is not configured"));
    }

    @Test
    @DisplayName("Translate should correctly parse Gemini JSON response and return Darija translation")
    void testTranslateSuccess() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        String fakeGeminiJson = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "سلام، كيداير؟"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(fakeGeminiJson);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        GeminiTranslationService service = new GeminiTranslationService(mockHttpClient) {
            @Override
            protected String getApiKey() {
                return "test-api-key-123";
            }
        };

        String translated = service.translate("Hello, how are you?");
        assertEquals("سلام، كيداير؟", translated);
    }

    @Test
    @DisplayName("Translate should handle Gemini API error status code with detailed message")
    void testTranslateApiError() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        String errorJson = """
                {
                  "error": {
                    "code": 400,
                    "message": "API key not valid. Please pass a valid API key.",
                    "status": "INVALID_ARGUMENT"
                  }
                }
                """;

        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn(errorJson);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        GeminiTranslationService service = new GeminiTranslationService(mockHttpClient) {
            @Override
            protected String getApiKey() {
                return "invalid-api-key";
            }
        };

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.translate("Hello"));
        assertTrue(exception.getMessage().contains("API key not valid"));
    }
}
