package requests;

public class AlreadyTakenException extends ResponseException {

    public AlreadyTakenException(String message, int statusCode) {
        super(message, statusCode);
    }
}
