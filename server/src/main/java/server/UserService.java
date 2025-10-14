package server;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import models.AuthData;
import models.UserData;
import requests.AlreadyTakenException;
import requests.RegisterRequest;

import java.util.UUID;

public class UserService {

    private final UserDAO userDAO = new UserDAO();
    private final AuthDAO authDAO = new AuthDAO();

    public AuthData register(RegisterRequest registerRequest) {
        if (userDAO.getUser(registerRequest.username()) != null) {
            throw new AlreadyTakenException("Username already taken!");
        }
        UserData userData = this.makeUser(registerRequest);
        userDAO.createUser(userData);
        AuthData authData = this.makeAuth(userData);
        authDAO.createAuth(authData);
        return authData;
    }

    private AuthData makeAuth(UserData userData) {
        return new AuthData(generateAuthToken(),userData.username());
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    private UserData makeUser(RegisterRequest registerRequest) {
        return new UserData(registerRequest.username(),registerRequest.password(),registerRequest.email());
    }
}
