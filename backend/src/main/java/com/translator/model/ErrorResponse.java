package com.translator.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

/**
 * Standard error response DTO.
 */
public class ErrorResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonbProperty("error")
    private String error;

    @JsonbProperty("status")
    private int status;

    public ErrorResponse() {
    }

    public ErrorResponse(String error, int status) {
        this.error = error;
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error='" + error + '\'' +
                ", status=" + status +
                '}';
    }
}
