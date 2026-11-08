package backend.academy.linktracker.scrapper.client;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import backend.academy.linktracker.scrapper.exc.StackOverflowResponseException;
import backend.academy.linktracker.scrapper.model.dto.StackOverflowAnswersResponseDto;
import backend.academy.linktracker.scrapper.model.dto.StackOverflowCommentsResponseDto;
import backend.academy.linktracker.scrapper.model.dto.StackOverflowQuestionResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StackOverflowClient {

    private static final int PAGE_SIZE = 100;

    private final RestClient stackOverflowRestClient;
    
    public StackOverflowQuestionResponseDto fetchQuestions(List<Long> questionIds) {

        String ids = idsToPath(questionIds);

        try {
            StackOverflowQuestionResponseDto response = stackOverflowRestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/2.3/questions/{questionIds}")
                .queryParam("site", "stackoverflow")
                .queryParam("pagesize", PAGE_SIZE)
                .build(ids)
            )
            .retrieve()
            .body(StackOverflowQuestionResponseDto.class);
            
            if (response == null || response.items() == null || response.items().isEmpty()) {
                throw new StackOverflowResponseException( "StackOverflow questions response is invalid");
            }
            
            return response;
        } catch(RestClientException ex) {
            throw new StackOverflowResponseException("StackOverflow questions request failed");
        }
    }

    public StackOverflowAnswersResponseDto fetchAnswers(
        List<Long> questionIds,
        long fromDate,
        long toDate,
        int page
    ) {
        String ids = idsToPath(questionIds);

        try {
            StackOverflowAnswersResponseDto response =
                stackOverflowRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/2.3/questions/{questionIds}/answers")
                        .queryParam("site", "stackoverflow")
                        .queryParam("sort", "creation")
                        .queryParam("order", "desc")
                        .queryParam("filter", "withbody")
                        .queryParam("fromdate", fromDate)
                        .queryParam("todate", toDate)
                        .queryParam("pagesize", PAGE_SIZE)
                        .queryParam("page", page)
                        .build(ids)
                    )
                    .retrieve()
                    .body(StackOverflowAnswersResponseDto.class);

            if (response == null || response.answers() == null) {
                throw new StackOverflowResponseException(
                    "StackOverflow answers response is invalid"
                );
            }

            return response;
        } catch (RestClientException ex) {
            throw new StackOverflowResponseException(
                "StackOverflow answers request failed"
            );
        }
    }


    public StackOverflowAnswersResponseDto fetchAnswers(Long questionId) {
        StackOverflowAnswersResponseDto response = stackOverflowRestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/2.3/questions/{questionId}/answers")
                .queryParam("site", "stackoverflow")
                .queryParam("sort", "creation")
                .queryParam("order", "desc")
                .queryParam("filter", "withbody")
                .build(questionId)
            )
            .retrieve()
            .body(StackOverflowAnswersResponseDto.class);

        if (response == null || response.answers() == null) {
            throw new StackOverflowResponseException(
                "StackOverflow answers response error for questionId=" + questionId
            );
        }

        return response;
    }

    public StackOverflowCommentsResponseDto fetchComments(List<Long> questionIds, long fromDate, long toDate, int page) {
        
        String ids = idsToPath(questionIds);
        
        try {

            StackOverflowCommentsResponseDto response = stackOverflowRestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/2.3/questions/{questionId}/comments")
                .queryParam("site", "stackoverflow")
                .queryParam("sort", "creation")
                .queryParam("order", "desc")
                .queryParam("filter", "withbody")
                .queryParam("fromdate", fromDate)
                .queryParam("todate", toDate)
                .queryParam("pagesize", PAGE_SIZE)
                .queryParam("page", page)
                .build(ids)
            )
            .retrieve()
            .body(StackOverflowCommentsResponseDto.class);
            
            if (response == null || response.items() == null) {
                throw new StackOverflowResponseException(
                    "StackOverflow comments response is invalid"
                );
            }
            
            return response;
        } catch(RestClientException  ex) {
            throw new StackOverflowResponseException("StackOverflow comments request failed");
        }
    }

    private String idsToPath(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(
                "Question ids must not be empty"
            );
        }

        if (ids.size() > 100) {
            throw new IllegalArgumentException(
                "StackOverflow API supports maximum 100 ids per request"
            );
        }

        return ids.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(";"));
    }
}
