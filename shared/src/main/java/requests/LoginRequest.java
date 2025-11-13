package requests;

import exception.ResponseException;

public record LoginRequest(String username, String password) implements Request {
    @Override
    public void assertGood() throws ResponseException {
        if (username == null || password == null) {
            throw new ResponseException("Error: bad request", 400);
        }
    }
}
