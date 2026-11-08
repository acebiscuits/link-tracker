package backend.academy.linktracker.bot.exc;

public class NoSuitabldeCommandResponseException extends BaseBotException {
    
    public NoSuitabldeCommandResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuitabldeCommandResponseException(String message) {
        super(message);
    }

    public NoSuitabldeCommandResponseException( Throwable cause) {
        super(cause);
    }
}
