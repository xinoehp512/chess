package requests;

public class ColorTakenException extends RuntimeException {
    public ColorTakenException(String message) {
        super(message);
    }
}
