package backend.academy.linktracker.scrapper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Validated 
@ConfigurationProperties(prefix = "app.scrapper")
public record ScrapperProperties(

    @NotNull 
    @Min(50)
    @Max(500)
    int batchSize,

    @NotNull 
    @Min(1)
    int previewLength,

    @Min(1)
    long checkIntervalMs
) {
    
}
