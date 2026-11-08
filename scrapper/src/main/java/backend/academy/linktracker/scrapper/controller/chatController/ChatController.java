package backend.academy.linktracker.scrapper.controller.chatController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.constraints.Positive;

public interface ChatController {
    
    public ResponseEntity<Void> registerChat(@PathVariable @Positive Long id);

    public ResponseEntity<Void> deleteChat(@PathVariable @Positive Long id);
}
