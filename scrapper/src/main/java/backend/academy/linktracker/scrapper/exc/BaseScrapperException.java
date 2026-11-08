package backend.academy.linktracker.scrapper.exc;

public class BaseScrapperException extends RuntimeException {
    
    public BaseScrapperException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BaseScrapperException(String message) {
        super(message);
    }

    public BaseScrapperException(Throwable cause) {
        super(cause);
    }
}
