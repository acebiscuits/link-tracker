package backend.academy.linktracker.scrapper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SrackOverflowClientConfig {
    
    @Bean
    public RestClient stackOverflowRestClient(StackOverflowProperties properties) {
        return RestClient.builder()
            .baseUrl("https://api.stackexchange.com")
            .defaultHeader("Accept", "application/json")
            .defaultHeader("Authorization", "Bearer " + properties.getAccessToken())
            .build();
    }
}
