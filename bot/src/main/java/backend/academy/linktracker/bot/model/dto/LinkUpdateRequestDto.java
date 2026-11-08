package backend.academy.linktracker.bot.model.dto;

import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.validator.constraints.URL;

import backend.academy.linktracker.bot.model.entity.UpdateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LinkUpdateRequestDto (
    @NotNull
    Long id,

    @NotBlank
    @URL
    String url,

    @NotNull 
    UpdateType type,

    @NotBlank
    String title,

    String author,

    @NotNull 
    OffsetDateTime createdAt,

    @Size(max = 200)
    String preview,

    @NotEmpty
    List<@NotNull Long> tgChatIds
    )
{}