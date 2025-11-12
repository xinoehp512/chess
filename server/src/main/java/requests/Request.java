package requests;

import exception.ResponseException;

public interface Request {

    public void assertGood() throws ResponseException;
}
