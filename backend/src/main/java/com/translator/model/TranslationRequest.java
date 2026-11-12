package com.translator.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

/**
 * Data Transfer Object for translation request payload.
 *
 * Example JSON:
 * {
 *   "text": "Hello, how are you?"
 * }
 */
public class TranslationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonbProperty("text")
    private String text;

    public TranslationRequest() {
    }

    public TranslationRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "TranslationRequest{" +
                "text='" + text + '\'' +
                '}';
    }
}
