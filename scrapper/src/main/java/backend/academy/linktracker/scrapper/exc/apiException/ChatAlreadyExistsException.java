package backend.academy.linktracker.scrapper.exc.apiException;

import org.springframework.http.HttpStatus;

public class ChatAlreadyExistsException extends ScrapperApiException {
    
    public ChatAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
