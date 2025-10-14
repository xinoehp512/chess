package requests;

public record CreateGameRequest() implements AuthorizedRequest {
    @Override
    public void assertGood() throws ResponseException {

    }
}
