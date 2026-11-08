package backend.academy.linktracker.scrapper.exc.apiException;

import org.springframework.http.HttpStatus;

public class ScrapperApiException extends RuntimeException {
    
    private final HttpStatus status;
    private final String description;

    public ScrapperApiException(HttpStatus status, String description) {
        super(description);
        this.status = status;
        this.description = description;
    }

    public HttpStatus status() {
        return status;
    }

    public String description() {
        return description;
    }
}
