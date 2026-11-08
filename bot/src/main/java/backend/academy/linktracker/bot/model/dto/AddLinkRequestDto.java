package backend.academy.linktracker.bot.model.dto;

import java.net.URI;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AddLinkRequestDto(
    @NotNull
    URI link,
    List<String> tags,
    List<String> filters
) {
}