package org.dep.messaging;

import org.springframework.web.client.RestClient;

public class Publisher {

    private final RestClient restClient;

    public Publisher(String brokerUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(brokerUrl)
                .build();
    }

    public void publish(String topicName, Object payload) {

        restClient.post()
                .uri("/topics/{topicName}/messages", topicName)
                .body(new PublishRequest(payload))
                .retrieve()
                .toBodilessEntity();
    }

    private record PublishRequest(Object payload) {
    }
}