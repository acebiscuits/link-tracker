package backend.academy.linktracker.scrapper.model.entity;

import java.net.URI;
import java.time.OffsetDateTime;

import lombok.Builder;

@Builder 
public record LinkState(
    Long id,
    URI url,
    OffsetDateTime lastCheckedAt
) {
    
}
