package requests;

public record JoinGameRequest(String playerColor, int gameID) implements Request {
    @Override
    public void assertGood() throws ResponseException {
        if (playerColor == null) {
            throw new ResponseException("Error: bad request", 400);
        }
    }
}
