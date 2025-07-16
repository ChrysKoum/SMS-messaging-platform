package com.intercom.sms.api.controller;

import com.intercom.sms.api.dto.DeliveryReportRequest;
import com.intercom.sms.api.dto.ErrorResponse;
import com.intercom.sms.service.MessageService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * Internal REST controller for callbacks from the processor service
 */
@Path("/v1/internal")
@Tag(name = "Internal Callbacks", description = "Internal service-to-service communication")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InternalController {

    private static final Logger LOG = Logger.getLogger(InternalController.class);

    @Inject
    MessageService messageService;

    @POST
    @Path("/delivery-report")
    @Operation(summary = "Process delivery report", 
               description = "Internal callback endpoint for processing delivery reports from processor service")
    @APIResponse(responseCode = "200", description = "Delivery report processed successfully")
    @APIResponse(responseCode = "400", description = "Invalid delivery report")
    @APIResponse(responseCode = "404", description = "Message not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response processDeliveryReport(@Valid DeliveryReportRequest deliveryReport) {
        LOG.infof("Received delivery report for message %s with status %s", 
                 deliveryReport.getMessageId(), deliveryReport.getStatus());
        
        try {
            boolean processed = messageService.processDeliveryReport(deliveryReport);
            
            if (processed) {
                LOG.infof("Delivery report processed successfully for message %s", 
                         deliveryReport.getMessageId());
                return Response.ok().build();
            } else {
                LOG.warnf("Message not found for delivery report: %s", deliveryReport.getMessageId());
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("MESSAGE_NOT_FOUND", 
                                "Message not found: " + deliveryReport.getMessageId()))
                        .build();
            }
            
        } catch (Exception e) {
            LOG.errorf(e, "Failed to process delivery report for message %s", 
                      deliveryReport.getMessageId());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("PROCESSING_ERROR", 
                            "Failed to process delivery report"))
                    .build();
        }
    }
}
