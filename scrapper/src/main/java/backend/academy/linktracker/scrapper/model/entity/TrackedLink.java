package backend.academy.linktracker.scrapper.model.entity;

import java.net.URI;
import java.util.List;

import lombok.Builder;

@Builder
public record TrackedLink(
    Long id,
    URI url,
    List<String> tags,
    List<String> filters
) {
}