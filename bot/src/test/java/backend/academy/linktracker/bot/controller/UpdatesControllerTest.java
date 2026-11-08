package backend.academy.linktracker.bot.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import backend.academy.linktracker.bot.model.dto.LinkUpdateRequestDto;
import backend.academy.linktracker.bot.model.entity.UpdateType;
import backend.academy.linktracker.bot.service.LinkUpdateNotificationService;

@ExtendWith(MockitoExtension.class)
public class UpdatesControllerTest {
    
    @Mock
    private LinkUpdateNotificationService linkUpdateNotificationService;

    @InjectMocks
    private UpdatesController updatesController;

    @Test
    @DisplayName("Should delegate processing to linkUpdateNotificationService and return ok")
    void test1() {
        OffsetDateTime now = OffsetDateTime.now();

        LinkUpdateRequestDto request = new LinkUpdateRequestDto(1L, "https://test", UpdateType.GITHUB_ISSUE, "test-title", "author",  now, "pewview", List.of(1L));

        ResponseEntity<Void> response = updatesController.handleUpdate(request);

        verify(linkUpdateNotificationService).process(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
