package backend.academy.linktracker.scrapper.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bot")
    public record BotProperties(URI url) {
}