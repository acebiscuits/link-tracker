package backend.academy.linktracker.scrapper.exc;

public class StackOverflowResponseException extends BaseScrapperException {
    
    public StackOverflowResponseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public StackOverflowResponseException(String message) {
        super(message);
    }

    public StackOverflowResponseException(Throwable cause) {
        super(cause);
    }
}
