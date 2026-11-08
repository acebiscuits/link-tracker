package backend.academy.linktracker.scrapper.exc.apiException;

import org.springframework.http.HttpStatus;

public class LinkAlreadyTrackedException extends ScrapperApiException {
    
    public LinkAlreadyTrackedException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
