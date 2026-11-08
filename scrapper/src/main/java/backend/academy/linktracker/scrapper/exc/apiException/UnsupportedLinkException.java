package backend.academy.linktracker.scrapper.exc.apiException;

import org.springframework.http.HttpStatus;

public class UnsupportedLinkException extends ScrapperApiException {
    
    public UnsupportedLinkException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
