package com.staffs.leavebooking.common.events;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "rabbitmq.outbox")
@Getter
public class RabbitOutboxRouter {

    public record Destination(String exchange, String routingKey) {}

    private final Map<String, Destination> bindings = new HashMap<>();

    public Destination resolve(Event event) {
        String className = event.getClass().getName();

        Destination dest = bindings.get(className);

        if (dest == null) {
            throw new IllegalArgumentException("No RabbitMQ destination configured for " + className);
        }

        return dest;
    }
}
