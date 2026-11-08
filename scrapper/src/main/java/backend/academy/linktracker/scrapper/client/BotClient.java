package backend.academy.linktracker.scrapper.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import backend.academy.linktracker.scrapper.model.dto.LinkUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotClient {

    private static final String UPDATES_PATH = "/updates";
    
    private final RestClient botRestClient;

    public void sendUpdate(LinkUpdateRequestDto updateRequestDto) {
        try {
            botRestClient.post().uri(UPDATES_PATH)
                .body(updateRequestDto)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.error(
                "Bot returned error. status={}, body={}, request={}",
                ex.getStatusCode().value(),
                ex.getResponseBodyAsString(),
                updateRequestDto,
                ex
            );
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to send update to bot. request={}", updateRequestDto, ex);
            throw ex;
        }
    }
}
