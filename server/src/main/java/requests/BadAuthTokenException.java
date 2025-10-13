package requests;

public class BadAuthTokenException extends RuntimeException {
    public BadAuthTokenException(String message) {
        super(message);
    }
}
