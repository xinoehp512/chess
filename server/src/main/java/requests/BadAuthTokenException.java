package requests;

public class BadAuthTokenException extends ResponseException {

    public BadAuthTokenException(String message, int statusCode) {
        super(message, statusCode);
    }
}
