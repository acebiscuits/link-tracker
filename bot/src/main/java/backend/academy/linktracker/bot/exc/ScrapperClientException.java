package backend.academy.linktracker.bot.exc;

import lombok.Getter;

public class ScrapperClientException extends RuntimeException {
    
    @Getter
    private final int statusCode;

    @Getter
    private final String responseBody;

    public ScrapperClientException(int statusCode, String responseBody, Throwable cause) {
        super("Scrapper api error. Status: " + statusCode + ". Response body: " + responseBody, cause);

        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
}
