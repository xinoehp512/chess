package requests;

public class NoUserException extends ResponseException {
    public NoUserException(String message, int statusCode) {
        super(message, statusCode);
    }
}
