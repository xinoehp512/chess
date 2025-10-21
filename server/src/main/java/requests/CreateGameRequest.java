package requests;

public record CreateGameRequest(String gameName) implements Request {
    @Override
    public void assertGood() throws ResponseException {
        if(gameName == null) {
            throw new ResponseException("Error: bad request", 400);
        }
    }
}
