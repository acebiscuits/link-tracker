package backend.academy.linktracker.bot.client.impl;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.exc.ScrapperClientException;
import backend.academy.linktracker.bot.model.dto.AddLinkRequestDto;
import backend.academy.linktracker.bot.model.dto.LinkResponseDto;
import backend.academy.linktracker.bot.model.dto.ListLinksResponseDto;
import backend.academy.linktracker.bot.model.dto.RemoveLinkRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpScrapperClient implements ScrapperClient {
    
    private final RestClient scrapperRestClient;

    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";

    @Override
    public void registerChat(Long chatId) {
        try {
            scrapperRestClient.post()
                .uri("/tg-chat/{id}", chatId)
                .retrieve()
                .toBodilessEntity();
        }
        catch (RestClientResponseException ex) {
            throw toScrapperClientException(ex);
        }
    }

    @Override
    public void deleteChat(Long chatId) {
        try {
            scrapperRestClient.delete()
                .uri("/tg-chat/{id}", chatId)
                .retrieve()
                .toBodilessEntity();
        }
        catch (RestClientResponseException ex) {
            throw toScrapperClientException(ex);
        }
    }

    @Override
    public LinkResponseDto addLink(Long chatId, AddLinkRequestDto request) {
        try {
            return scrapperRestClient.post()
                .uri("/links")
                .header(TG_CHAT_ID_HEADER, String.valueOf(chatId))
                .body(request)
                .retrieve()
                .body(LinkResponseDto.class);
        }
        catch (RestClientResponseException ex) {
            throw toScrapperClientException(ex);
        }
    }

    @Override
    public LinkResponseDto removeLink(Long chatId, RemoveLinkRequestDto request) {
        try {
            return scrapperRestClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/links")
                .header(TG_CHAT_ID_HEADER, String.valueOf(chatId))
                .body(request)
                .retrieve()
                .body(LinkResponseDto.class);
        }
        catch (RestClientResponseException ex) {
            throw toScrapperClientException(ex);
        }
    }

    @Override
    public ListLinksResponseDto getLinks(Long chatId, List<String> tags) {
        try {
            return scrapperRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/links");
                    for (String tag : tags) {
                        uriBuilder.queryParam("tag", tag);
                    }
                    return uriBuilder.build();
                })
                .header(TG_CHAT_ID_HEADER, String.valueOf(chatId))
                .retrieve()
                .body(ListLinksResponseDto.class);
        }
        catch (RestClientResponseException ex) {
            throw toScrapperClientException(ex);
        }
    }

    private ScrapperClientException toScrapperClientException(RestClientResponseException ex) {
        return new ScrapperClientException(ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);
    }
}
