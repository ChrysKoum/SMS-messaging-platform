package com.intercom.processor.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Health check for processor service readiness
 */
@Readiness
@ApplicationScoped
public class ProcessorReadinessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("processor-readiness")
            .status(true)
            .withData("status", "ready")
            .build();
    }
}
