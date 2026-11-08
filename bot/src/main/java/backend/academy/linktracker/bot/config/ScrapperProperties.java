package backend.academy.linktracker.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.scrapper")
public class ScrapperProperties {
    
    @NotBlank
    private String url;
}
