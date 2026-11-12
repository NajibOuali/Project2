package com.translator.security;

import com.translator.model.ErrorResponse;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * HTTP Basic Authentication filter securing the JAX-RS REST endpoints.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class BasicAuthFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(BasicAuthFilter.class.getName());
    private static final String AUTH_SCHEME_BASIC = "Basic";
    private static final String REALM = "DarijaTranslatorRealm";
    private static final Jsonb JSONB = JsonbBuilder.create();

    private static final String DEFAULT_USERNAME = "user";
    private static final String DEFAULT_PASSWORD = "darija123";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Allow CORS preflight requests (OPTIONS) to pass through without authentication
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.regionMatches(true, 0, AUTH_SCHEME_BASIC + " ", 0, AUTH_SCHEME_BASIC.length() + 1)) {
            abortWithUnauthorized(requestContext, "Missing or invalid HTTP Basic Authorization header.");
            return;
        }

        String encodedCredentials = authHeader.substring(AUTH_SCHEME_BASIC.length()).trim();
        String credentials;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedCredentials);
            credentials = new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            abortWithUnauthorized(requestContext, "Malformed Base64 authorization token.");
            return;
        }

        int colonIndex = credentials.indexOf(':');
        if (colonIndex == -1) {
            abortWithUnauthorized(requestContext, "Invalid credentials format. Expected username:password.");
            return;
        }

        String username = credentials.substring(0, colonIndex);
        String password = credentials.substring(colonIndex + 1);

        if (!authenticate(username, password)) {
            LOGGER.warning("Authentication failed for username: " + username);
            abortWithUnauthorized(requestContext, "Invalid username or password.");
            return;
        }

        // Set custom SecurityContext on successful authentication
        final String authenticatedUser = username;
        final boolean isSecure = requestContext.getSecurityContext().isSecure();

        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> authenticatedUser;
            }

            @Override
            public boolean isUserInRole(String role) {
                return "user".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role);
            }

            @Override
            public boolean isSecure() {
                return isSecure;
            }

            @Override
            public String getAuthenticationScheme() {
                return AUTH_SCHEME_BASIC;
            }
        });
    }

    /**
     * Validates credentials against environment variables or configured defaults.
     */
    protected boolean authenticate(String username, String password) {
        String expectedUser = getExpectedUsername();
        String expectedPass = getExpectedPassword();
        return expectedUser.equals(username) && expectedPass.equals(password);
    }

    protected String getExpectedUsername() {
        String envUser = System.getenv("API_USERNAME");
        if (envUser != null && !envUser.trim().isEmpty()) {
            return envUser.trim();
        }
        String propUser = System.getProperty("API_USERNAME");
        if (propUser != null && !propUser.trim().isEmpty()) {
            return propUser.trim();
        }
        return DEFAULT_USERNAME;
    }

    protected String getExpectedPassword() {
        String envPass = System.getenv("API_PASSWORD");
        if (envPass != null && !envPass.trim().isEmpty()) {
            return envPass.trim();
        }
        String propPass = System.getProperty("API_PASSWORD");
        if (propPass != null && !propPass.trim().isEmpty()) {
            return propPass.trim();
        }
        return DEFAULT_PASSWORD;
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        ErrorResponse errorResponse = new ErrorResponse(message, Response.Status.UNAUTHORIZED.getStatusCode());
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, AUTH_SCHEME_BASIC + " realm=\"" + REALM + "\"")
                        .entity(JSONB.toJson(errorResponse))
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }
}
