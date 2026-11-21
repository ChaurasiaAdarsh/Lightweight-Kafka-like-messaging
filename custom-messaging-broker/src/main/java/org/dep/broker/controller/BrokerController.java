package org.dep.broker.controller;

import org.dep.broker.model.Message;
import org.dep.broker.model.PublishRequest;
import org.dep.broker.service.MessageBroker;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topics")
public class BrokerController {
    private final MessageBroker broker;

    public BrokerController(MessageBroker broker) {
        this.broker = broker;
    }

    @PostMapping("/{topicName}")
    public String createTopic(@PathVariable String topicName) {

        broker.createTopic(topicName);

        return "Topic created: " + topicName;
    }

    @PostMapping("/{topicName}/messages")
    public String publishMessage(@PathVariable String topicName, @RequestBody PublishRequest request) {

        broker.publish(
                topicName,
                request.getPayload()
        );

        return "Message published to topic: " + topicName;
    }

    @PostMapping("/{topicName}/subscribers/{subscriberId}")
    public String subscribe(
            @PathVariable String topicName,
            @PathVariable String subscriberId) {

        broker.subscribe(
                topicName,
                subscriberId
        );

        return "Subscriber registered: " + subscriberId;
    }

    @GetMapping("/{topicName}/subscribers/{subscriberId}/messages")
    public Message getMessage(
            @PathVariable String topicName,
            @PathVariable String subscriberId) {

        return broker.getNextMessage(
                topicName,
                subscriberId
        );
    }
}
