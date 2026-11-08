package backend.academy.linktracker.scrapper.repository.mapper;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import backend.academy.linktracker.scrapper.model.entity.TrackedLink;
import backend.academy.linktracker.scrapper.model.repository.jpa.entity.SubscriptionJpaEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component 
public class JpaEntityToTrackedLinkMapper {
    
    public TrackedLink subscriptionJpaEntityToTrackedLink(SubscriptionJpaEntity subscriptionJpaEntity) {
        List<String> tags = subscriptionJpaEntity.getTags() == null ? List.of() : List.copyOf(subscriptionJpaEntity.getTags());
        List<String> filters = subscriptionJpaEntity.getFilters() == null ? List.of() : List.copyOf(subscriptionJpaEntity.getFilters());
        return TrackedLink.builder()
            .id(subscriptionJpaEntity.getLink().getId())
            .url(URI.create(subscriptionJpaEntity.getLink().getUrl()))
            .tags(tags)
            .filters(filters)
            .build();
    }
}
