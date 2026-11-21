package org.dep.broker.model;

public class Message {
    private final String topic;
    private final Object payload;

    public Message(String topic, Object payload) {
        this.topic = topic;
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public Object getPayload() {
        return payload;
    }
}
