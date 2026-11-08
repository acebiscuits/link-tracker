package backend.academy.linktracker.scrapper.exc.apiException;

import org.springframework.http.HttpStatus;

public class ChatNotFoundException extends ScrapperApiException {
     
    public ChatNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
