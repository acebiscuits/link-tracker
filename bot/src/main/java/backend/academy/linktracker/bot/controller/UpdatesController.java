package backend.academy.linktracker.bot.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import backend.academy.linktracker.bot.model.dto.LinkUpdateRequestDto;
import backend.academy.linktracker.bot.service.LinkUpdateNotificationService;
import backend.academy.linktracker.bot.service.notification.receiver.LinkUpdatesReceiver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.notification", name = "transport", havingValue = "HTTP")
public class UpdatesController {
    
    private final LinkUpdatesReceiver updatesReceiver;

    @PostMapping("/updates")
    public ResponseEntity<Void> handleUpdate(@Valid @RequestBody LinkUpdateRequestDto request) {
        updatesReceiver.receive(request);
        return ResponseEntity.ok().build();
    }

}