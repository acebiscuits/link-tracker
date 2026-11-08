package backend.academy.linktracker.scrapper.repository.chatRepository;

import java.util.List;

public interface ChatRepository {
    
    public List<Long> getAllChats();

    public boolean exists(Long chatId);

    public boolean save(Long chatId);

    public boolean delete(Long chatId);
}
