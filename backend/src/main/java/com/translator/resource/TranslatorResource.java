package com.translator.resource;

import com.translator.model.ErrorResponse;
import com.translator.model.TranslationRequest;
import com.translator.model.TranslationResponse;
import com.translator.service.GeminiTranslationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RESTful Web Service endpoint for Darija translation.
 * Exposes POST /api/translator/translate
 */
@Path("/translator")
public class TranslatorResource {

    private static final Logger LOGGER = Logger.getLogger(TranslatorResource.class.getName());

    @Inject
    private GeminiTranslationService translationService;

    public TranslatorResource() {
    }

    public TranslatorResource(GeminiTranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * Translates input text into Moroccan Darija.
     *
     * @param request JSON payload containing text to translate
     * @return Response with TranslationResponse or ErrorResponse
     */
    @POST
    @Path("/translate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response translate(TranslationRequest request) {
        if (request == null || request.getText() == null || request.getText().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Request body must contain a non-empty 'text' field.", Response.Status.BAD_REQUEST.getStatusCode()))
                    .build();
        }

        try {
            GeminiTranslationService service = this.translationService != null ? this.translationService : new GeminiTranslationService();
            String translatedText = service.translate(request.getText());

            TranslationResponse response = new TranslationResponse(
                    request.getText(),
                    translatedText,
                    "English",
                    "Moroccan Darija"
            );

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Bad translation request: {0}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage(), Response.Status.BAD_REQUEST.getStatusCode()))
                    .build();
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "Translation service error: {0}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during translation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("An unexpected server error occurred: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
                    .build();
        }
    }
}
