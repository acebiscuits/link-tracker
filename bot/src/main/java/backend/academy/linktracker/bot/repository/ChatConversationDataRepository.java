package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.model.entity.ConversationData;

public interface ChatConversationDataRepository {

    public boolean checkIfChatIsRegistered(Long chatId);

    public ConversationData get(Long chatId);

    public void set(Long chatId, ConversationData conversationData);

    public void remove(Long chatId);
}