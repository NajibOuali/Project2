package com.translator.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Jakarta RESTful Web Services Application configuration.
 * Sets the base API path to "/api".
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
}
