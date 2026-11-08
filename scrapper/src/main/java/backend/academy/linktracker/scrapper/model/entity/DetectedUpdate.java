package backend.academy.linktracker.scrapper.model.entity;

import java.time.OffsetDateTime;

import lombok.Builder;

/**
 * DetectedUpdate
 */
@Builder 
public record DetectedUpdate(
    UpdateType type,
    String title,
    String author,
    OffsetDateTime createdAt,
    String preview
) {
}