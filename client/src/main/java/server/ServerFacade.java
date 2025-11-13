package server;

import requests.LoginRequest;
import response.LoginResponse;

import java.net.http.HttpClient;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        return null;
    }


}
