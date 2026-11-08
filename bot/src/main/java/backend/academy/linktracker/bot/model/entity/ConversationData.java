package backend.academy.linktracker.bot.model.entity;

import java.net.URI;
import java.util.List;

import lombok.Builder;
import lombok.Singular;

@Builder(toBuilder = true)
public record ConversationData(
    ConversationState state,
    String commandName,
    URI url,

    @Singular("tag")
    List<String> tags,
    @Singular("filter")
    List<String> filters
) {
    public static ConversationData idle() {
        return ConversationData.builder()
            .state(ConversationState.IDLE)
            .build();
    }
}