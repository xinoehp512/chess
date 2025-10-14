package requests;

public record JoinGameRequest() implements AuthorizedRequest {
    @Override
    public void assertGood() throws ResponseException {

    }
}
