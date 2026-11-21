package org.dep.broker.model;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Topic {

    private final String name;

    private final Map<String, BlockingQueue<Message>> subscriberQueues =
            new ConcurrentHashMap<>();

    public Topic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addSubscriber(
            String subscriberId,
            BlockingQueue<Message> queue) {

        subscriberQueues.putIfAbsent(subscriberId, queue);
    }

    public void removeSubscriber(String subscriberId) {

        subscriberQueues.remove(subscriberId);
    }

    public void publish(Message message) {

        for (BlockingQueue<Message> queue : subscriberQueues.values()) {

            try {
                queue.put(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(
                        "Message publishing interrupted", e
                );
            }
        }
    }

    public BlockingQueue<Message> getSubscriberQueue(
            String subscriberId) {

        return subscriberQueues.get(subscriberId);
    }
}
