package uk.gov.justice.digital.ndh.config;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Application insights now controlled by the spring-boot-starter dependency.  However when the key is not specified
 * we don't get a telemetry bean and application won't start.  Therefore need this backup configuration.
 */
@Configuration
@Slf4j
public class ApplicationInsightsConfiguration {


    @Bean
    @Conditional(AppInsightKeyAbsentCondition.class)
    public TelemetryClient telemetryClient() {
        return new TelemetryClient();
    }

    public static class AppInsightKeyAbsentCondition implements Condition {

        @Override
        public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
            final var telemetryKey = context.getEnvironment().getProperty("application.insights.ikey");
            final boolean blank = Strings.isBlank(telemetryKey);
            if (blank) {
                log.warn("No application.insights.ikey found! App Insights will not work.");
            }
            return blank;
        }
    }
}