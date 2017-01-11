package io.github.robwin;

import com.fasterxml.jackson.databind.Module;
import io.github.robwin.circuitbreaker.CircuitBreakerRegistry;
import io.github.robwin.circuitbreaker.event.CircuitBreakerEvent;
import io.github.robwin.config.CircuitBreakerProperties;
import io.github.robwin.consumer.DefaultEventConsumerRegistry;
import io.github.robwin.consumer.EventConsumerRegistry;
import io.github.robwin.monitoring.health.CircuitBreakerHealthIndicator;
import javaslang.jackson.datatype.JavaslangModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.Set;

@SpringBootApplication
@EnableConfigurationProperties
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public HealthIndicator backendA(CircuitBreakerRegistry circuitBreakerRegistry,
									EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry,
									CircuitBreakerProperties circuitBreakerProperties){
		return new CircuitBreakerHealthIndicator(circuitBreakerRegistry, eventConsumerRegistry, circuitBreakerProperties, "backendA");
	}

	@Bean
	public HealthIndicator backendB(CircuitBreakerRegistry circuitBreakerRegistry,
									EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry,
									CircuitBreakerProperties circuitBreakerProperties){
		return new CircuitBreakerHealthIndicator(circuitBreakerRegistry, eventConsumerRegistry, circuitBreakerProperties, "backendB");
	}

	@Bean
	public CircuitBreakerRegistry circuitBreakerRegistry() {
		return CircuitBreakerRegistry.ofDefaults();
	}

	@Bean
	public EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry(CircuitBreakerProperties circuitBreakerProperties) {
		EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry = new DefaultEventConsumerRegistry<>();
		Set<Map.Entry<String, CircuitBreakerProperties.BackendProperties>> entries = circuitBreakerProperties.getBackends().entrySet();
		for(Map.Entry<String, CircuitBreakerProperties.BackendProperties> entry : entries){
			CircuitBreakerProperties.BackendProperties backendProperties = entry.getValue();
			eventConsumerRegistry.createEventConsumer(entry.getKey(), backendProperties.getEventConsumerBufferSize());
		}
		return eventConsumerRegistry;
	}

	@Bean
	public Module javaslangModule() {
		return new JavaslangModule();
	}
}
