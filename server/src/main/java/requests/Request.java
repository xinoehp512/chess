package requests;

import exception.ResponseException;

public interface Request {

    void assertGood() throws ResponseException;
}
