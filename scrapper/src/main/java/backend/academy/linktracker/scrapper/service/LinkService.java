package backend.academy.linktracker.scrapper.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.academy.linktracker.scrapper.exc.apiException.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exc.apiException.LinkNotTrackedException;
import backend.academy.linktracker.scrapper.model.dto.AddLinkRequestDto;
import backend.academy.linktracker.scrapper.model.dto.LinkResponseDto;
import backend.academy.linktracker.scrapper.model.dto.ListLinksResponseDto;
import backend.academy.linktracker.scrapper.model.dto.RemoveLinkRequestDto;
import backend.academy.linktracker.scrapper.model.entity.TrackedLink;
import backend.academy.linktracker.scrapper.repository.chatRepository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.linkRepository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    
    @Transactional
    public ListLinksResponseDto getTrackedLinks(Long chatId, List<String> tags) {
        checkAndSaveChat(chatId);
        List<TrackedLink> trackedLinks = linkRepository.findByChatIdAndTags(chatId, tags);
        
        return new ListLinksResponseDto(
            trackedLinks.stream().map(this::toResponse).toList(), 
            trackedLinks.size()    
        );
    }

    @Transactional
    public LinkResponseDto addLinkTracking(Long chatId, AddLinkRequestDto link) {
        checkAndSaveChat(chatId);

        if (linkRepository.existsByChatIdAndUrl(chatId, link.link())) {
            throw new LinkAlreadyTrackedException("Link already tracked error: " + link.link());
        }
        TrackedLink trackedLink = linkRepository.save(chatId, link.link(), link.tags(), link.filters());
        return toResponse(trackedLink);
    }

    @Transactional
    public LinkResponseDto deleteTrackedLink(Long chatId, RemoveLinkRequestDto link) {
        checkAndSaveChat(chatId);

        if (!linkRepository.existsByChatIdAndUrl(chatId, link.link())) {
            throw new LinkNotTrackedException("Link " + link.link() + "not tracked for chatId: " + chatId);
        }
        TrackedLink trackedLink = linkRepository.deleteByChatIdAndUrl(chatId, link.link())
            .orElseThrow(() -> new LinkNotTrackedException("Link not tracked: " + link.link() + " for chatId: " + chatId));

        return toResponse(trackedLink);
    }

    private void checkAndSaveChat(Long chatId) {
        if (!chatRepository.exists(chatId)) {
            chatRepository.save(chatId);
        }
    }

    private LinkResponseDto toResponse(TrackedLink link) {
        return new LinkResponseDto(
            link.id(),
            link.url(),
            link.tags(),
            link.filters()
        );
    }
}
