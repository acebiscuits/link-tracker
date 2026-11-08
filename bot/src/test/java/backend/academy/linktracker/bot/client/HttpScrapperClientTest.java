package backend.academy.linktracker.bot.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.List;

import backend.academy.linktracker.bot.client.impl.HttpScrapperClient;
import backend.academy.linktracker.bot.exc.ScrapperClientException;
import backend.academy.linktracker.bot.model.dto.AddLinkRequestDto;
import backend.academy.linktracker.bot.model.dto.LinkResponseDto;
import backend.academy.linktracker.bot.model.dto.ListLinksResponseDto;
import backend.academy.linktracker.bot.model.dto.RemoveLinkRequestDto;

public class HttpScrapperClientTest {
    
    private MockRestServiceServer server;
    private HttpScrapperClient client;
    
    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://scrapper:8081");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new HttpScrapperClient(builder.build());
    }

    @Test
    @DisplayName("Check that chat registrating request processes correctly")
    void test1() {
        server.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo("http://scrapper:8081/tg-chat/4444"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withSuccess());

        client.registerChat(4444L);

        server.verify();
    }

    @Test
    @DisplayName("Should convert bad request response of chat deleting to ScrapperClientException")
    void test2() {
        server.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo("http://scrapper:8081/tg-chat/4444"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                        "description": "Invalid request parameters"
                    }
                    """));
        
        assertThatThrownBy(() -> client.registerChat(4444L))
            .isInstanceOf(ScrapperClientException.class)
            .satisfies(ex -> {
                ScrapperClientException scrapperEx = (ScrapperClientException) ex;
                assertThat(scrapperEx.getStatusCode()).isEqualTo(400);
                assertThat(scrapperEx.getResponseBody()).contains("Invalid request parameters");
            });
    }

    @Test
    @DisplayName("Check that delete chat request processes correctly")
    void test4() {
        server.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo("http://scrapper:8081/tg-chat/4444"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
            .andRespond(MockRestResponseCreators.withSuccess());

        client.deleteChat(4444L);

        server.verify();
    }

    @Test
    @DisplayName("Delete chat method should convert bad request response to ScrapperClientException")
    void test5() {
        server.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo("http://scrapper:8081/tg-chat/4444"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                        "description": "Invalid request parameters"
                    }
                    """));

        assertThatThrownBy(() -> client.deleteChat(4444L))
        .isInstanceOf(ScrapperClientException.class)
        .satisfies(ex -> {
            ScrapperClientException scrapperEx = (ScrapperClientException) ex;
            assertThat(scrapperEx.getStatusCode()).isEqualTo(400);
            assertThat(scrapperEx.getResponseBody()).contains("Invalid request parameters");
        });
    }

    @Test
    @DisplayName("Check that adding link request processes correctly")
    void test7() {
        URI testLink = URI.create("https://github.com/test/test");
        List<String> testTags = List.of("tag1", "tag2");
        List<String> testFilters = List.of("filter1", "filter2");
        AddLinkRequestDto addLinkRequestDto = new AddLinkRequestDto(testLink, testTags, testFilters);

        server.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo("http://scrapper:8081/links"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.header("Tg-Chat-Id", "4444"))
            .andExpect(MockRestRequestMatchers.content().json("""
                {
                    "link": "https://github.com/test/test",
                    "tags": ["tag1", "tag2"],
                    "filters": ["filter1", "filter2"]
                }
                """))
            .andRespond(MockRestResponseCreators.withSuccess("""
                {
                    "id": 1,
                    "url": "https://github.com/test/test",
                    "tags": ["tag1", "tag2"],
                    "filters": ["filter1", "filter2"]
                }
                """, MediaType.APPLICATION_JSON));

        LinkResponseDto response = client.addLink(4444L, addLinkRequestDto);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.url().toString()).isEqualTo("https://github.com/test/test");
        assertThat(response.tags()).containsExactly("tag1", "tag2");
        assertThat(response.filters()).containsExactly("filter1", "filter2");

        server.verify();
    }


    @Test
    @DisplayName("Should adding link bad request convert to ScrapperClientException")
    void test8() {

        URI testLink = URI.create("https://github.com/test/test");
        List<String> testTags = List.of("tag1", "tag2"); 
        List<String> testFilters = List.of("filter1", "filter2"); 
        AddLinkRequestDto addLinkRequestDto = new AddLinkRequestDto(testLink, testTags, testFilters);

        server.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo("http://scrapper:8081/links"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                        "description": "Invalid request parameters"
                    }
                    """));

        assertThatThrownBy(() -> client.addLink(4444L, addLinkRequestDto))
        .isInstanceOf(ScrapperClientException.class)
        .satisfies(ex -> {
            ScrapperClientException scrapperEx = (ScrapperClientException) ex;
            assertThat(scrapperEx.getStatusCode()).isEqualTo(400);
            assertThat(scrapperEx.getResponseBody()).contains("Invalid request parameters");
        });
    }


    @Test
    @DisplayName("Check that deleting link request processes correctly")
    void test10() {
        URI testLink = URI.create("https://github.com/test/test");
        RemoveLinkRequestDto removeLinkRequestDto = new RemoveLinkRequestDto(testLink);

        server.expect(once(), requestTo("http://scrapper:8081/links"))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header("Tg-Chat-Id", "4444"))
            .andExpect(content().json("""
                {
                    "link": "https://github.com/test/test"
                }
                """))
            .andRespond(withSuccess("""
                {
                    "id": 1,
                    "url": "https://github.com/test/test",
                    "tags": ["tag1", "tag2"],
                    "filters": ["filter1", "filter2"]
                }
                """, MediaType.APPLICATION_JSON));

        LinkResponseDto response = client.removeLink(4444L, removeLinkRequestDto);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.url().toString()).isEqualTo("https://github.com/test/test");
        assertThat(response.tags()).containsExactly("tag1", "tag2");
        assertThat(response.filters()).containsExactly("filter1", "filter2");

        server.verify();
    }

    @Test
    @DisplayName("Should deleting link bad request convert to ScrapperClientException")
    void test11() {
        URI testLink = URI.create("https://github.com/test/test");
        RemoveLinkRequestDto removeLinkRequestDto = new RemoveLinkRequestDto(testLink);

        server.expect(once(), requestTo("http://scrapper:8081/links"))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header("Tg-Chat-Id", "4444"))
            .andExpect(content().json("""
                {
                    "link": "https://github.com/test/test"
                }
                """))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                        "description": "Invalid request parameters"
                    }
                    """));

        assertThatThrownBy(() -> client.removeLink(4444L, removeLinkRequestDto))
            .isInstanceOf(ScrapperClientException.class)
            .satisfies(ex -> {
                ScrapperClientException scrapperEx = (ScrapperClientException) ex;
                assertThat(scrapperEx.getStatusCode()).isEqualTo(400);
                assertThat(scrapperEx.getResponseBody()).contains("Invalid request parameters");
            });

        server.verify();
    }

    @Test
    @DisplayName("Check that getting links request processes correctly")
    void test13() {
        List<String> testTags = List.of("tag1", "tag2");

        server.expect(once(), requestTo("http://scrapper:8081/links?tag=tag1&tag=tag2"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Tg-Chat-Id", "4444"))
            .andRespond(withSuccess("""
                {
                    "links": [
                        {
                            "id": 1,
                            "url": "https://github.com/test/test",
                            "tags": ["tag1", "tag2"],
                            "filters": ["filter1"]
                        }
                    ],
                    "size": 1
                }
                """, MediaType.APPLICATION_JSON));

        ListLinksResponseDto response = client.getLinks(4444L, testTags);

        assertThat(response).isNotNull();
        assertThat(response.links()).hasSize(1);
        assertThat(response.links().getFirst().id()).isEqualTo(1L);
        assertThat(response.links().getFirst().url().toString()).isEqualTo("https://github.com/test/test");
        assertThat(response.links().getFirst().tags()).containsExactly("tag1", "tag2");
        assertThat(response.links().getFirst().filters()).containsExactly("filter1");
        assertThat(response.links().size()).isEqualTo(1);

        server.verify();
    }

    @Test
    @DisplayName("Should getting links bad request convert to ScrapperClientException")
    void test14() {
        List<String> testTags = List.of("tag1", "tag2");

        server.expect(once(), requestTo("http://scrapper:8081/links?tag=tag1&tag=tag2"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Tg-Chat-Id", "4444"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                        "description": "Invalid request parameters"
                    }
                    """));

        assertThatThrownBy(() -> client.getLinks(4444L, testTags))
            .isInstanceOf(ScrapperClientException.class)
            .satisfies(ex -> {
                ScrapperClientException scrapperEx = (ScrapperClientException) ex;
                assertThat(scrapperEx.getStatusCode()).isEqualTo(400);
                assertThat(scrapperEx.getResponseBody()).contains("Invalid request parameters");
            });

        server.verify();
    }
}
