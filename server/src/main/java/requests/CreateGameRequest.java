package requests;

public record CreateGameRequest(String gameName) implements AuthorizedRequest {
    @Override
    public void assertGood() throws ResponseException {
        if(gameName == null) {
            throw new ResponseException("Error: bad request", 400);
        }
    }
}
