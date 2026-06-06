package com.authora.authorization.server.infrastructure.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaConfig {
    @Bean
    public KafkaAdmin.NewTopics topics(){
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name("tenant.registered").partitions(1).replicas(1).build(),
                TopicBuilder.name("notification.email.verification").partitions(1).replicas(1).build()
        );
    }
}
