package service;

import dataaccess.*;
import models.AuthData;
import models.UserData;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import requests.ResponseException;

import java.util.Objects;
import java.util.UUID;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(RegisterRequest registerRequest) throws ResponseException {
        registerRequest.assertGood();
        UserData userData = this.makeUser(registerRequest);
        try {
            userDAO.insertUser(userData);
        } catch (DataAccessException e) {
            throw new ResponseException("Error: username already taken", 403);
        }
        return this.makeAuth(userData);
    }

    public AuthData login(LoginRequest loginRequest) throws ResponseException {
        loginRequest.assertGood();
        UserData userData = userDAO.getUser(loginRequest.username());
        if (userData == null || !Objects.equals(userData.password(), loginRequest.password())) {
            throw new ResponseException("Error: unauthorized", 401);
        }
        return this.makeAuth(userData);
    }

    public void logout(LogoutRequest logoutRequest) throws ResponseException {
        try {
            authDAO.deleteAuth(logoutRequest.authToken());
        } catch (DataAccessException e) {
            throw new ResponseException("Error: unauthorized", 401);
        }
    }

    private AuthData makeAuth(UserData userData) {
        AuthData authData = new AuthData(generateAuthToken(), userData.username());
        authDAO.insertAuth(authData);
        return authData;
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    private UserData makeUser(RegisterRequest registerRequest) {
        return new UserData(registerRequest.username(), registerRequest.password(),
                registerRequest.email());
    }
}
