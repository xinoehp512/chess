package requests;

public class NoGameException extends ResponseException {
    public NoGameException(String message, int statusCode) {
        super(message, statusCode);
    }
}
