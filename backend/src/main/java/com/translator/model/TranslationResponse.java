package com.translator.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

/**
 * Data Transfer Object for translation response payload.
 *
 * Example JSON:
 * {
 *   "originalText": "Hello, how are you?",
 *   "translatedText": "سلام، كيداير؟",
 *   "sourceLanguage": "English",
 *   "targetLanguage": "Moroccan Darija"
 * }
 */
public class TranslationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonbProperty("originalText")
    private String originalText;

    @JsonbProperty("translatedText")
    private String translatedText;

    @JsonbProperty("sourceLanguage")
    private String sourceLanguage;

    @JsonbProperty("targetLanguage")
    private String targetLanguage;

    public TranslationResponse() {
        this.sourceLanguage = "English";
        this.targetLanguage = "Moroccan Darija";
    }

    public TranslationResponse(String originalText, String translatedText) {
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.sourceLanguage = "English";
        this.targetLanguage = "Moroccan Darija";
    }

    public TranslationResponse(String originalText, String translatedText, String sourceLanguage, String targetLanguage) {
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.sourceLanguage = sourceLanguage != null ? sourceLanguage : "English";
        this.targetLanguage = targetLanguage != null ? targetLanguage : "Moroccan Darija";
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    @Override
    public String toString() {
        return "TranslationResponse{" +
                "originalText='" + originalText + '\'' +
                ", translatedText='" + translatedText + '\'' +
                ", sourceLanguage='" + sourceLanguage + '\'' +
                ", targetLanguage='" + targetLanguage + '\'' +
                '}';
    }
}
