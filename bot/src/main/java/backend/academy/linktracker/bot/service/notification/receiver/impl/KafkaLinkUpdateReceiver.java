package backend.academy.linktracker.bot.service.notification.receiver.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import backend.academy.linktracker.bot.model.dto.LinkUpdateRequestDto;
import backend.academy.linktracker.bot.service.LinkUpdateNotificationService;
import backend.academy.linktracker.bot.service.notification.receiver.LinkUpdatesReceiver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service 
@RequiredArgsConstructor 
@ConditionalOnProperty(prefix = "app.notification", name = "transport", havingValue = "KAFKA", matchIfMissing = true)
public class KafkaLinkUpdateReceiver implements LinkUpdatesReceiver {

    private final LinkUpdateNotificationService linkUpdateNotificationService;

    @Override 
    @KafkaListener(topics = "${app.kafka.topic}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void receive(LinkUpdateRequestDto update) {
        log.info("Received kafka update: id={}, type={}", update.id(), update.type());

        linkUpdateNotificationService.process(update);
    }
    
}
