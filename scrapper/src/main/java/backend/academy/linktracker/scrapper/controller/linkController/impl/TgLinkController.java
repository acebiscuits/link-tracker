package backend.academy.linktracker.scrapper.controller.linkController.impl;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.academy.linktracker.scrapper.controller.linkController.LinkController;
import backend.academy.linktracker.scrapper.model.dto.AddLinkRequestDto;
import backend.academy.linktracker.scrapper.model.dto.LinkResponseDto;
import backend.academy.linktracker.scrapper.model.dto.ListLinksResponseDto;
import backend.academy.linktracker.scrapper.model.dto.RemoveLinkRequestDto;
import backend.academy.linktracker.scrapper.service.LinkService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@RestController
@RequiredArgsConstructor
public class TgLinkController implements LinkController {
    
    private final LinkService linkService;
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";

    @Override
    @GetMapping("/links")
    public ResponseEntity<ListLinksResponseDto> getTrackedLinks(@RequestHeader(TG_CHAT_ID_HEADER) @Positive Long chatId, @RequestParam(name = "tag", required = false) List<String> tags) {
        return ResponseEntity.ok(linkService.getTrackedLinks(chatId, tags));
    }

    @Override
    @PostMapping("/links")
    public ResponseEntity<LinkResponseDto> addLinkTracking(@RequestHeader(TG_CHAT_ID_HEADER) @Positive Long chatId, @Valid @RequestBody AddLinkRequestDto request) {
        return ResponseEntity.ok(linkService.addLinkTracking(chatId, request));
    }

    @Override
    @DeleteMapping("/links")
    public ResponseEntity<LinkResponseDto> deleteTrackedLink(@RequestHeader(TG_CHAT_ID_HEADER) @Positive Long chatId, @Valid @RequestBody RemoveLinkRequestDto request) {
        return ResponseEntity.ok(linkService.deleteTrackedLink(chatId, request));
    }
}
