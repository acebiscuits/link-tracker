package backend.academy.linktracker.bot.client;

import java.util.List;

import backend.academy.linktracker.bot.model.dto.AddLinkRequestDto;
import backend.academy.linktracker.bot.model.dto.LinkResponseDto;
import backend.academy.linktracker.bot.model.dto.ListLinksResponseDto;
import backend.academy.linktracker.bot.model.dto.RemoveLinkRequestDto;

public interface ScrapperClient {

    public void registerChat(Long chatId);

    public void deleteChat(Long chatId);

    public LinkResponseDto addLink(Long chatId, AddLinkRequestDto request);

    public LinkResponseDto removeLink(Long chatId, RemoveLinkRequestDto request);

    public ListLinksResponseDto getLinks(Long chatId, List<String> tags);
}