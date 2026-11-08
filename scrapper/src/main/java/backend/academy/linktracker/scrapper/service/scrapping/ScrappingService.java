package backend.academy.linktracker.scrapper.service.scrapping;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

import backend.academy.linktracker.scrapper.model.entity.LinkCheckResult;
import backend.academy.linktracker.scrapper.model.entity.LinkState;

public interface ScrappingService {

    public boolean supports(URI url);

    public List<LinkCheckResult> fetchUpdates(List<LinkState> links, OffsetDateTime createdAt);

    default String preview(String body, int previewLength) {
        if (body == null || body.isBlank()) {
            return "";
        }

        if (body.length() <= previewLength) {
            return body;
        }

        return body.substring(0, previewLength);
    }
}
