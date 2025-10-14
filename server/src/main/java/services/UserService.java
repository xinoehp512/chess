package services;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import models.AuthData;
import models.UserData;
import requests.RegisterRequest;
import requests.ResponseException;

import java.util.UUID;

public class UserService {

    private final UserDAO userDAO = new UserDAO();
    private final AuthDAO authDAO = new AuthDAO();

    public AuthData register(RegisterRequest registerRequest) throws ResponseException {
        UserData userData = this.makeUser(registerRequest);
        try {
            userDAO.insertUser(userData);
        } catch (DataAccessException e) {
            throw new ResponseException("Error: username already taken",403);
        }
        AuthData authData = this.makeAuth(userData);
        authDAO.insertAuth(authData);
        return authData;
    }

    private AuthData makeAuth(UserData userData) {
        return new AuthData(generateAuthToken(),userData.username());
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    private UserData makeUser(RegisterRequest registerRequest) throws ResponseException {
        if (registerRequest.username() == null || registerRequest.password()==null || registerRequest.email()==null) {
            throw new ResponseException("Error: bad request",400);
        }
        return new UserData(registerRequest.username(),registerRequest.password(),registerRequest.email());
    }
}
