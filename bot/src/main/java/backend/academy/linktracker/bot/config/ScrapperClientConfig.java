package backend.academy.linktracker.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ScrapperClientConfig {
    
    @Bean
    public RestClient scrapperRestClient(ScrapperProperties properties) {
        return RestClient.builder()
            .baseUrl(properties.getUrl())
            .build();
    }
}
