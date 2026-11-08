package backend.academy.linktracker.scrapper.exc.apiException;

import org.springframework.http.HttpStatus;

public class LinkNotTrackedException extends ScrapperApiException{
    
    public LinkNotTrackedException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
