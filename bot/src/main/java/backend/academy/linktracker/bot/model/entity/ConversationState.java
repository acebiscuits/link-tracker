package backend.academy.linktracker.bot.model.entity;

public enum ConversationState {
    IDLE,
    WAITING_LINK,
    WAITING_TAGS,
    WAITING_FILTERS,
    FINISH_STAGE
}
