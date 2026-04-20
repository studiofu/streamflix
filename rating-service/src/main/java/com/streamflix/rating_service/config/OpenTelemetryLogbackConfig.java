package com.streamflix.rating_service.config;

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

    /**
     * This configuration class ensures that the OpenTelemetry SDK is properly registered
     * with Logback's OpenTelemetryAppender at application startup.
     * 
     * Logback will drop log events unless OpenTelemetryAppender.install(OpenTelemetry)
     * is explicitly called with the active OpenTelemetry instance. This is required
     * because the appender itself does not pick up the OpenTelemetry bean automatically
     * from Spring Boot's context.
     * 
     * By invoking install() in the constructor (triggered by Spring instantiating this
     * configuration class and injecting the OpenTelemetry bean), the application's logs
     * are reliably forwarded to the OpenTelemetry Collector per logback-spring.xml settings.
     */
    public OpenTelemetryLogbackConfig(OpenTelemetry openTelemetry) {
        OpenTelemetryAppender.install(openTelemetry);
    }
}
