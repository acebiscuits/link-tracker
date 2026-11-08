package backend.academy.linktracker.scrapper.model.repository.jpa.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Table(name = "subscriptions")
public class SubscriptionJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatJpaEntity chat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "link_id", nullable = false)
    private LinkJpaEntity link;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
        name = "subscription_tags",
        joinColumns = @JoinColumn(name = "subscription_id")
    )
    @Column(name = "tag", nullable = false)
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(
        name = "subscription_filters",
        joinColumns = @JoinColumn(name = "subscription_id")
    )
    @Column(name = "filter", nullable = false)
    private List<String> filters = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
