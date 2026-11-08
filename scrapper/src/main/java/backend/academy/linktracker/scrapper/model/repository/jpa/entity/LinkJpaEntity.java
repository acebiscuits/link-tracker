package backend.academy.linktracker.scrapper.model.repository.jpa.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Getter
@Table(name = "links")
public class LinkJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "url")
    private String url;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_checked_at", nullable = false)
    private OffsetDateTime lastCheckedAt;

    public LinkJpaEntity(String url) {
        this.url = url;
        this.lastCheckedAt = OffsetDateTime.now();
    }
}
