package com.intercom.processor.client;

import com.intercom.processor.dto.DeliveryReportRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for sending delivery reports back to SMS service
 */
@RegisterRestClient(configKey = "sms-service")
@Path("/v1/internal")
@Consumes(MediaType.APPLICATION_JSON)
public interface SmsServiceClient {

    /**
     * Send delivery report to SMS service
     */
    @POST
    @Path("/delivery-report")
    void sendDeliveryReport(DeliveryReportRequest deliveryReport);
}
