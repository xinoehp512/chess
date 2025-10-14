package requests;

public record RegisterRequest(String username, String password, String email) implements Request {
    @Override
    public void assertGood() throws ResponseException {
        if (username == null || password == null || email == null) {
            throw new ResponseException("Error: bad request", 400);
        }
    }
}
