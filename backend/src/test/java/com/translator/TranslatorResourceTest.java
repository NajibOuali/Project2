package com.translator;

import com.translator.model.ErrorResponse;
import com.translator.model.TranslationRequest;
import com.translator.model.TranslationResponse;
import com.translator.resource.TranslatorResource;
import com.translator.service.GeminiTranslationService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TranslatorResourceTest {

    private TranslatorResource resource;
    private MockGeminiTranslationService mockService;

    static class MockGeminiTranslationService extends GeminiTranslationService {
        private String mockReturnText = "سلام، كيداير؟";
        private RuntimeException errorToThrow = null;

        public void setMockReturnText(String mockReturnText) {
            this.mockReturnText = mockReturnText;
        }

        public void setErrorToThrow(RuntimeException errorToThrow) {
            this.errorToThrow = errorToThrow;
        }

        @Override
        public String translate(String text) {
            if (errorToThrow != null) {
                throw errorToThrow;
            }
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("Translation text cannot be empty or blank.");
            }
            return mockReturnText;
        }
    }

    @BeforeEach
    void setUp() {
        mockService = new MockGeminiTranslationService();
        resource = new TranslatorResource(mockService);
    }

    @Test
    void testSuccessfulTranslation() {
        TranslationRequest request = new TranslationRequest("Hello, how are you?");
        Response response = resource.translate(request);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof TranslationResponse);

        TranslationResponse entity = (TranslationResponse) response.getEntity();
        assertEquals("Hello, how are you?", entity.getOriginalText());
        assertEquals("سلام، كيداير؟", entity.getTranslatedText());
        assertEquals("English", entity.getSourceLanguage());
        assertEquals("Moroccan Darija", entity.getTargetLanguage());
    }

    @Test
    void testNullRequest() {
        Response response = resource.translate(null);

        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity() instanceof ErrorResponse);
        ErrorResponse error = (ErrorResponse) response.getEntity();
        assertEquals(400, error.getStatus());
        assertTrue(error.getError().contains("text"));
    }

    @Test
    void testEmptyTextRequest() {
        TranslationRequest request = new TranslationRequest("   ");
        Response response = resource.translate(request);

        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity() instanceof ErrorResponse);
        ErrorResponse error = (ErrorResponse) response.getEntity();
        assertEquals(400, error.getStatus());
    }

    @Test
    void testServiceFailure() {
        mockService.setErrorToThrow(new IllegalStateException("Gemini API key is not configured on the server."));

        TranslationRequest request = new TranslationRequest("Hello");
        Response response = resource.translate(request);

        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity() instanceof ErrorResponse);
        ErrorResponse error = (ErrorResponse) response.getEntity();
        assertEquals(500, error.getStatus());
        assertTrue(error.getError().contains("Gemini API key"));
    }
}
