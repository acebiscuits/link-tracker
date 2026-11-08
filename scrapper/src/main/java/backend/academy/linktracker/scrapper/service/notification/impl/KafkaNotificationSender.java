package backend.academy.linktracker.scrapper.service.notification.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import backend.academy.linktracker.scrapper.config.KafkaProperties;
import backend.academy.linktracker.scrapper.model.dto.LinkUpdateRequestDto;
import backend.academy.linktracker.scrapper.service.notification.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service 
@RequiredArgsConstructor 
@ConditionalOnProperty(prefix = "app.notification", name = "transport", havingValue = "KAFKA", matchIfMissing = true)
public class KafkaNotificationSender implements NotificationSender {

    private final KafkaTemplate<String, LinkUpdateRequestDto> kafkaTemplate;
    private final KafkaProperties properties;
    
    @Override 
    public void send(LinkUpdateRequestDto update) {
        log.info("sending to kafka, topic: " + properties.topic());
        kafkaTemplate.send(properties.topic(),
            update.id().toString(),
            update);
    }
}
