package backend.academy.linktracker.scrapper.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * KafkaTopicConfig
 */
@Configuration 
public class KafkaTopicConfig {

    @Bean 
    public NewTopic linkUpdatesTopic(KafkaProperties properties) {
        return TopicBuilder
            .name(properties.topic())
            .partitions(3)
            .replicas(3)
            .build();
    }
}