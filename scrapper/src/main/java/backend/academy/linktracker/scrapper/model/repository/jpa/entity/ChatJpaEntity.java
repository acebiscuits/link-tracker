package backend.academy.linktracker.scrapper.model.repository.jpa.entity;

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
@Table(name = "chats")
public class ChatJpaEntity {

    public ChatJpaEntity(Long telegramChatId) {
        this.telegramChatId = telegramChatId;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_chat_id", nullable = false, unique =  true)
    private Long telegramChatId;
}
