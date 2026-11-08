package backend.academy.linktracker.bot.exc;

public class NoCommandHandlerForState extends BaseBotException {

    public NoCommandHandlerForState(String message, Throwable cause) {
        super(message, cause);
    }

    public NoCommandHandlerForState(String message) {
        super(message);
    }

    public NoCommandHandlerForState( Throwable cause) {
        super(cause);
    }
}