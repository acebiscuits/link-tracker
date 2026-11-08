package backend.academy.linktracker.scrapper.controller.chatController.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.academy.linktracker.scrapper.controller.chatController.ChatController;
import backend.academy.linktracker.scrapper.service.ChatService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class TgChatController implements ChatController {//дабавить мб эксепшены

    private final ChatService tgChatService;
    
    @Override
    @PostMapping("/tg-chat/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable @Positive Long id) {
        tgChatService.registerChat(id);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/tg-chat/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable @Positive Long id) {
        tgChatService.deleteChat(id);
        return ResponseEntity.ok().build();
    }
}
