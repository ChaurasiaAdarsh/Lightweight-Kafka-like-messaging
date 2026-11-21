package org.dep.messaging.autoconfigure;

import org.dep.messaging.Publisher;
import org.dep.messaging.Subscriber;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(MessagingProperties.class)
public class AutoConfig {

    @Bean
    public Publisher publisher(MessagingProperties properties) {
        return new Publisher(properties.getBrokerUrl());
    }

    @Bean
    public Subscriber subscriber(MessagingProperties properties) {
        return new Subscriber(properties.getBrokerUrl());
    }
}