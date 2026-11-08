package backend.academy.linktracker.scrapper.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.academy.linktracker.scrapper.exc.apiException.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exc.apiException.ChatNotFoundException;
import backend.academy.linktracker.scrapper.repository.chatRepository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.linkRepository.LinkRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    private final LinkRepository linkRepository;
    
    @Transactional
    public void registerChat(Long chatId) {
        if (!chatRepository.save(chatId)) {
            throw new ChatAlreadyExistsException(String.valueOf(chatId));
        }
    }

    @Transactional
    public void deleteChat(Long chatId) {
        if (!chatRepository.delete(chatId)) {
            throw new ChatNotFoundException(String.valueOf(chatId));
        }

        linkRepository.deleteAllLinksByChatId(chatId);
    }
}
