package requests;

public class NoUserException extends RuntimeException {
    public NoUserException(String message) {
        super(message);
    }
}
