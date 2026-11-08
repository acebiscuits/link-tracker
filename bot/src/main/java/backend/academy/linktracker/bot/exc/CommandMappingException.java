package backend.academy.linktracker.bot.exc;

public class CommandMappingException extends BaseBotException {

    public CommandMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandMappingException(String message) {
        super(message);
    }

    public CommandMappingException( Throwable cause) {
        super(cause);
    }
}