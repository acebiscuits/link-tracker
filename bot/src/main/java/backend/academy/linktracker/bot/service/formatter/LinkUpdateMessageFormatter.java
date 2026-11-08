package backend.academy.linktracker.bot.service.formatter;

import org.springframework.stereotype.Component;

import backend.academy.linktracker.bot.model.dto.LinkUpdateRequestDto;
import backend.academy.linktracker.bot.model.entity.UpdateType;

/**
 * LinkUpdateMessageFormatter
 */
@Component 
public class LinkUpdateMessageFormatter {

    public String format(LinkUpdateRequestDto update) {
        String eventName = switch(update.type()) {
            case GITHUB_ISSUE -> "New issue";

            case GITHUB_PULL_REQUEST -> "New PR";

            case STACKOVERFLOW_ANSWER -> "New answer";

            case STACKOVERFLOW_COMMENT -> "New comment";
        };

        String author = update.author() == null || update.author().isBlank() ? "" : update.author();

        String preview = update.preview() == null || update.preview().isBlank() ? "" : update.preview();

        return """
            %s

            %s: %s
            Автор: %s
            Время: %s

            %s

            %s
            """.formatted(
                eventName,
                titleLabel(update.type()),
                update.title(),
                author,
                update.createdAt(),
                preview,
                update.url());
    }

    private String titleLabel(UpdateType type) {
        return switch (type) {
            case GITHUB_ISSUE,
                 GITHUB_PULL_REQUEST ->
                "Title";

            case STACKOVERFLOW_ANSWER,
                 STACKOVERFLOW_COMMENT ->
                "Question";
        };
    }
}