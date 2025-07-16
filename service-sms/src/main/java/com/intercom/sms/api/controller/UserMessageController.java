package com.intercom.sms.api.controller;

import com.intercom.sms.api.dto.*;
import com.intercom.sms.domain.model.MessageStatus;
import com.intercom.sms.service.MessageService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST controller for user-specific message operations
 */
@Path("/v1/users")
@Tag(name = "User Messages", description = "User-specific SMS message operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserMessageController {

    private static final Logger LOG = Logger.getLogger(UserMessageController.class);

    @Inject
    MessageService messageService;

    @GET
    @Path("/{userId}/messages")
    @Operation(summary = "Get user messages", description = "Get a paginated list of messages for a specific user")
    @APIResponse(
        responseCode = "200", 
        description = "User messages retrieved successfully",
        content = @Content(schema = @Schema(implementation = MessageListResponse.class))
    )
    @APIResponse(responseCode = "400", description = "Invalid user ID format")
    public Response getUserMessages(
            @Parameter(description = "User phone number", required = true, example = "+1234567890")
            @PathParam("userId") 
            @Pattern(regexp = "\\+?[1-9]\\d{1,14}", message = "Invalid phone number format")
            String userId,
            
            @Parameter(description = "Page number (0-based)")
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            
            @Parameter(description = "Page size")
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size,
            
            @Parameter(description = "Filter by message status")
            @QueryParam("status") MessageStatus status) {
        
        LOG.debugf("Retrieving messages for user %s: page=%d, size=%d, status=%s", 
                  userId, page, size, status);
        
        try {
            MessageListResponse response = messageService.getUserMessages(userId, page, size, status);
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOG.errorf(e, "Failed to retrieve messages for user %s", userId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to retrieve user messages"))
                    .build();
        }
    }
}
