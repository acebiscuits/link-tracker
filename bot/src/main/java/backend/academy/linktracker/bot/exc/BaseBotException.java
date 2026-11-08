package backend.academy.linktracker.bot.exc;

public class BaseBotException extends RuntimeException {
    
    public BaseBotException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseBotException(String message) {
        super(message);
    }

    public BaseBotException( Throwable cause) {
        super(cause);
    }
}
