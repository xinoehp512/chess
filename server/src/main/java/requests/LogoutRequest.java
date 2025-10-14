package requests;

public record LogoutRequest(String authToken) implements AuthorizedRequest {

    @Override
    public void assertGood() throws ResponseException {

    }
}
