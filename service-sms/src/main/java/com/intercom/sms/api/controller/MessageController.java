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

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for SMS message operations
 */
@Path("/v1/messages")
@Tag(name = "SMS Messages", description = "SMS message operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessageController {

    private static final Logger LOG = Logger.getLogger(MessageController.class);

    @Inject
    MessageService messageService;

    @POST
    @Operation(summary = "Send SMS message", description = "Send a new SMS message")
    @APIResponse(
        responseCode = "202", 
        description = "Message accepted for processing",
        content = @Content(schema = @Schema(implementation = MessageResponse.class))
    )
    @APIResponse(responseCode = "400", description = "Invalid request")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response sendMessage(@Valid SendMessageRequest request) {
        LOG.infof("Received SMS request from %s to %s", request.getSender(), request.getRecipient());
        
        try {
            MessageResponse response = messageService.sendMessage(request);
            
            // Return 202 Accepted with Location header
            URI location = URI.create("/v1/messages/" + response.getId());
            
            return Response.accepted(response)
                    .location(location)
                    .build();
                    
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send SMS message");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Failed to process SMS request"))
                    .build();
        }
    }

    @GET
    @Path("/{messageId}")
    @Operation(summary = "Get message by ID", description = "Retrieve a specific SMS message by its ID")
    @APIResponse(
        responseCode = "200", 
        description = "Message found",
        content = @Content(schema = @Schema(implementation = MessageResponse.class))
    )
    @APIResponse(responseCode = "404", description = "Message not found")
    public Response getMessage(
            @Parameter(description = "Message ID", required = true)
            @PathParam("messageId") UUID messageId) {
        
        LOG.debugf("Retrieving message with ID: %s", messageId);
        
        Optional<MessageResponse> message = messageService.getMessage(messageId);
        
        if (message.isPresent()) {
            return Response.ok(message.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("MESSAGE_NOT_FOUND", "Message not found: " + messageId))
                    .build();
        }
    }

    @GET
    @Operation(summary = "List messages", description = "Get a paginated list of all messages")
    @APIResponse(
        responseCode = "200", 
        description = "Messages retrieved successfully",
        content = @Content(schema = @Schema(implementation = MessageListResponse.class))
    )
    public Response listMessages(
            @Parameter(description = "Page number (0-based)")
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            
            @Parameter(description = "Page size")
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size,
            
            @Parameter(description = "Filter by status")
            @QueryParam("status") MessageStatus status) {
        
        LOG.debugf("Listing messages: page=%d, size=%d, status=%s", page, size, status);
        
        // For now, return empty list as we need user context
        MessageListResponse response = new MessageListResponse();
        return Response.ok(response).build();
    }

    @GET
    @Path("/stats")
    @Operation(summary = "Get message statistics", description = "Get statistics about message processing")
    @APIResponse(
        responseCode = "200", 
        description = "Statistics retrieved successfully",
        content = @Content(schema = @Schema(implementation = MessageStatsResponse.class))
    )
    public Response getMessageStats() {
        LOG.debug("Retrieving message statistics");
        
        MessageStatsResponse stats = messageService.getMessageStats();
        return Response.ok(stats).build();
    }

    @GET
    @Path("/failed")
    @Operation(summary = "Get failed messages", description = "Get a list of failed messages for analysis")
    @APIResponse(
        responseCode = "200", 
        description = "Failed messages retrieved successfully",
        content = @Content(schema = @Schema(implementation = MessageListResponse.class))
    )
    public Response getFailedMessages(
            @Parameter(description = "Page number (0-based)")
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            
            @Parameter(description = "Page size")
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size) {
        
        LOG.debugf("Retrieving failed messages: page=%d, size=%d", page, size);
        
        MessageListResponse response = messageService.getFailedMessages(page, size);
        return Response.ok(response).build();
    }
}
