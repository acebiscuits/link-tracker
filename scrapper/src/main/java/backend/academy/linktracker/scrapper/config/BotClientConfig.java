package backend.academy.linktracker.scrapper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BotClientConfig {
    
    @Bean
    public RestClient botRestClient(BotProperties properties) {
        return RestClient.builder().baseUrl(properties.url()).build();
    }
}
