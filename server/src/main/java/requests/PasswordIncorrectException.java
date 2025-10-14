package requests;

public class PasswordIncorrectException extends ResponseException {
    public PasswordIncorrectException(String message, int statusCode) {
        super(message, statusCode);
    }
}
