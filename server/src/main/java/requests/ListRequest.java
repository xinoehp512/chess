package requests;

public record ListRequest() implements AuthorizedRequest {
    @Override
    public void assertGood() throws ResponseException {

    }
}
