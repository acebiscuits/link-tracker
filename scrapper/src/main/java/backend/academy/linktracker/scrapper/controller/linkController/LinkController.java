package backend.academy.linktracker.scrapper.controller.linkController;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import backend.academy.linktracker.scrapper.model.dto.AddLinkRequestDto;
import backend.academy.linktracker.scrapper.model.dto.LinkResponseDto;
import backend.academy.linktracker.scrapper.model.dto.ListLinksResponseDto;
import backend.academy.linktracker.scrapper.model.dto.RemoveLinkRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

public interface LinkController {

    public ResponseEntity<ListLinksResponseDto> getTrackedLinks(@RequestHeader @Positive Long chatId, @RequestParam(name = "tag", required = false) List<String> tags);

    public ResponseEntity<LinkResponseDto> addLinkTracking(@RequestHeader @Positive Long chatId, @Valid @RequestBody AddLinkRequestDto request);

    public ResponseEntity<LinkResponseDto> deleteTrackedLink(@RequestHeader @Positive Long chatId, @Valid @RequestBody RemoveLinkRequestDto request);
}