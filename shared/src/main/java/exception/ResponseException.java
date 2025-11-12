package exception;

import com.google.gson.Gson;

import java.util.Map;

public class ResponseException extends Exception {
    private final int statusCode;

    public ResponseException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", getStatusCode()));
    }
}


