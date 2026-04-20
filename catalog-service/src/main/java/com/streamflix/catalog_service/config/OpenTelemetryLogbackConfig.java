package com.streamflix.catalog_service.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

import org.springframework.context.annotation.Configuration;

/**
 * Wires the global OpenTelemetry SDK into the Logback OpenTelemetryAppender.
 * Spring Boot 4.0 creates the OpenTelemetry bean via auto-configuration but does NOT
 * automatically call OpenTelemetryAppender.install(...), so log events would otherwise
 * be silently dropped by the appender.
 */
@Configuration
public class OpenTelemetryLogbackConfig {

    public OpenTelemetryLogbackConfig(OpenTelemetry openTelemetry) {
        OpenTelemetryAppender.install(openTelemetry);
    }
}
