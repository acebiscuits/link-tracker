package backend.academy.linktracker.scrapper.exc;

public class GitHubResponseException extends BaseScrapperException {

    public GitHubResponseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public GitHubResponseException(String message) {
        super(message);
    }

    public GitHubResponseException(Throwable cause) {
        super(cause);
    }
}
