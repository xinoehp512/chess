package requests;

public record RegisterRequest(String username, String password, String email) implements Request {
}
