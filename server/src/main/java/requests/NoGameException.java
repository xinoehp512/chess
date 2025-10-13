package requests;

public class NoGameException extends RuntimeException {
    public NoGameException(String message) {
        super(message);
    }
}
