package org.dep.messaging;

import org.springframework.web.client.RestClient;

import java.util.function.Consumer;

public class Subscriber {

    private final RestClient restClient;

    public Subscriber(String brokerUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(brokerUrl)
                .build();
    }

    public void subscribe(
            String topicName,
            String subscriberId,
            Consumer<Message> handler) {

        // 1. Register subscriber with broker
        restClient.post()
                .uri(
                        "/topics/{topicName}/subscribers/{subscriberId}",
                        topicName,
                        subscriberId
                )
                .retrieve()
                .toBodilessEntity();

        // 2. Start consuming messages
        Thread consumerThread = new Thread(() -> {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    Message message = restClient.get()
                            .uri(
                                    "/topics/{topicName}/subscribers/{subscriberId}/messages",
                                    topicName,
                                    subscriberId
                            )
                            .retrieve()
                            .body(Message.class);

                    if (message != null) {
                        handler.accept(message);
                    }

                } catch (Exception e) {

                    e.printStackTrace();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

        });

        consumerThread.start();
    }
}