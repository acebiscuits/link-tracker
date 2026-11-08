package backend.academy.linktracker.scrapper.model.dto;

import java.net.URI;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public record AddLinkRequestDto(
    @NotNull
    URI link,
    List<String> tags,
    List<String> filters
) {
}