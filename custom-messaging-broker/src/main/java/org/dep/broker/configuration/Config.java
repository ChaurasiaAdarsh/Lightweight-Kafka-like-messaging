package org.dep.broker.configuration;

import org.dep.broker.service.MessageBroker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public MessageBroker messageBroker() {
        return new MessageBroker();
    }
}
