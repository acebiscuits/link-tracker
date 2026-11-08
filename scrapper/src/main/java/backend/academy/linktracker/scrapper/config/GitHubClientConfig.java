package backend.academy.linktracker.scrapper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class GitHubClientConfig {

    @Bean
    public RestClient githubRestClient(GithubProperties properties) {
        return RestClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader("Accept", "application/vnd.github+json")
            .defaultHeader("Authorization", "Bearer " + properties.getToken())
            .build();
    }
}
