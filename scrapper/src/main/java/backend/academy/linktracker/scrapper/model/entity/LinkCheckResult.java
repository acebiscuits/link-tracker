package backend.academy.linktracker.scrapper.model.entity;

import java.util.List;

public record LinkCheckResult(
    LinkState link,
    List<DetectedUpdate> updates,
    boolean successful
) {

    public static LinkCheckResult success(LinkState link, List<DetectedUpdate> updates) {
        return new LinkCheckResult(
            link,
            List.copyOf(updates),
            true
        );
    }

    public static LinkCheckResult failure(LinkState link) {
        return new LinkCheckResult(
            link,
            List.of(),
            false
        );
    }
}