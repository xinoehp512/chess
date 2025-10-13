package requests;

public class AlreadyTakenException extends RuntimeException {
    public AlreadyTakenException(String message) {
        super(message);
    }
}
