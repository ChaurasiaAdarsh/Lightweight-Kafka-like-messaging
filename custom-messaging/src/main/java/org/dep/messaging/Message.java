package org.dep.messaging;

public record Message(
        String topic,
        Object payload
) {
}