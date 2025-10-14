package requests;

public class ColorTakenException extends ResponseException {

    public ColorTakenException(String message, int statusCode) {
        super(message, statusCode);
    }
}
