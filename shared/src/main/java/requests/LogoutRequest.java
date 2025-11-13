package requests;

import exception.ResponseException;

public record LogoutRequest(String authToken) implements Request {

    @Override
    public void assertGood() throws ResponseException {
        if (authToken == null) {
            throw new ResponseException("Error: bad request", 400);
        }
    }
}
