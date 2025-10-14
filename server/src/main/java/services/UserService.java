package services;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import models.AuthData;
import models.UserData;
import requests.LoginRequest;
import requests.RegisterRequest;
import requests.ResponseException;

import java.util.Objects;
import java.util.UUID;

public class UserService {

    private final UserDAO userDAO = new UserDAO();
    private final AuthDAO authDAO = new AuthDAO();

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

    private AuthData makeAuth(UserData userData) {
        AuthData authData = new AuthData(generateAuthToken(), userData.username());
        authDAO.insertAuth(authData);
        return authData;
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    private UserData makeUser(RegisterRequest registerRequest) {
        return new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
    }


}
