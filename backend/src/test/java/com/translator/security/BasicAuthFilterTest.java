package com.translator.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BasicAuthFilter.
 */
public class BasicAuthFilterTest {

    @Test
    @DisplayName("Filter should allow OPTIONS preflight requests without checking auth")
    void testOptionsRequestPasses() throws IOException {
        BasicAuthFilter filter = new BasicAuthFilter();
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getMethod()).thenReturn("OPTIONS");

        filter.filter(context);

        verify(context, never()).abortWith(any(Response.class));
        verify(context, never()).getHeaderString(HttpHeaders.AUTHORIZATION);
    }

    @Test
    @DisplayName("Filter should reject request with 401 when Authorization header is missing")
    void testMissingAuthorizationHeader() throws IOException {
        BasicAuthFilter filter = new BasicAuthFilter();
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getMethod()).thenReturn("POST");
        when(context.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);

        filter.filter(context);

        verify(context).abortWith(responseCaptor.capture());
        Response response = responseCaptor.getValue();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertTrue(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE).contains("Basic realm"));
    }

    @Test
    @DisplayName("Filter should reject request with 401 when credentials are wrong")
    void testInvalidCredentials() throws IOException {
        BasicAuthFilter filter = new BasicAuthFilter();
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getMethod()).thenReturn("POST");

        String invalidAuth = "Basic " + Base64.getEncoder().encodeToString("user:wrongpass".getBytes(StandardCharsets.UTF_8));
        when(context.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(invalidAuth);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);

        filter.filter(context);

        verify(context).abortWith(responseCaptor.capture());
        Response response = responseCaptor.getValue();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @DisplayName("Filter should allow request and set SecurityContext when valid Basic Auth credentials are provided")
    void testValidCredentials() throws IOException {
        BasicAuthFilter filter = new BasicAuthFilter();
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getMethod()).thenReturn("POST");

        String validAuth = "Basic " + Base64.getEncoder().encodeToString("user:darija123".getBytes(StandardCharsets.UTF_8));
        when(context.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(validAuth);
        when(context.getSecurityContext()).thenReturn(mock(SecurityContext.class));

        ArgumentCaptor<SecurityContext> securityContextCaptor = ArgumentCaptor.forClass(SecurityContext.class);

        filter.filter(context);

        verify(context, never()).abortWith(any(Response.class));
        verify(context).setSecurityContext(securityContextCaptor.capture());

        SecurityContext capturedSecContext = securityContextCaptor.getValue();
        assertNotNull(capturedSecContext);
        assertEquals("user", capturedSecContext.getUserPrincipal().getName());
        assertTrue(capturedSecContext.isUserInRole("user"));
    }
}
