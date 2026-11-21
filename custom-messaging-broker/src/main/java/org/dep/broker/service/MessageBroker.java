package org.dep.broker.service;

import org.dep.broker.model.Message;
import org.dep.broker.model.Topic;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageBroker {

    private final Map<String, Topic> topics =
            new ConcurrentHashMap<>();

    public void createTopic(String topicName) {

        topics.putIfAbsent(
                topicName,
                new Topic(topicName)
        );
    }

    public Topic getTopic(String topicName) {

        return topics.get(topicName);
    }

    public void publish(String topicName, Object payload) {

        Topic topic = topics.get(topicName);

        if (topic == null) {
            throw new IllegalArgumentException(
                    "Topic does not exist: " + topicName
            );
        }

        Message message =
                new Message(topicName, payload);

        topic.publish(message);
    }
    public void subscribe(String topicName, String subscriberId) {

        Topic topic = topics.get(topicName);

        if (topic == null) {
            throw new IllegalArgumentException(
                    "Topic does not exist: " + topicName
            );
        }

        BlockingQueue<Message> queue =
                new LinkedBlockingQueue<>();

        topic.addSubscriber(subscriberId, queue);
    }
    public Message getNextMessage(
            String topicName,
            String subscriberId) {

        Topic topic = topics.get(topicName);

        if (topic == null) {
            throw new IllegalArgumentException(
                    "Topic does not exist: " + topicName
            );
        }

        BlockingQueue<Message> queue =
                topic.getSubscriberQueue(subscriberId);

        if (queue == null) {
            throw new IllegalArgumentException(
                    "Subscriber does not exist: " + subscriberId
            );
        }

        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Subscriber interrupted", e
            );
        }
    }
}