package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import models.AuthData;
import models.UserData;
import org.mindrot.jbcrypt.BCrypt;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import requests.ResponseException;

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
            if (userDAO.getUser(registerRequest.username()) != null) {
                throw new ResponseException("Error: username already taken", 403);
            }
            userDAO.insertUser(userData);
        } catch (DataAccessException e) {
            throw new ResponseException("Error: database", 500);
        }
        return this.makeAuth(userData);
    }

    public AuthData login(LoginRequest loginRequest) throws ResponseException {
        loginRequest.assertGood();
        UserData userData;
        try {
            userData = userDAO.getUser(loginRequest.username());
        } catch (DataAccessException e) {
            throw new ResponseException("Error: database", 500);
        }
        if (userData == null || !verifyPassword(userData.password(), loginRequest.password())) {
            throw new ResponseException("Error: unauthorized", 401);
        }
        return this.makeAuth(userData);
    }

    private boolean verifyPassword(String hashedPassword, String testPassword) {
        return BCrypt.checkpw(testPassword, hashedPassword);
    }

    public void logout(LogoutRequest logoutRequest) throws ResponseException {
        try {
            if (authDAO.getAuth(logoutRequest.authToken()) == null) {
                throw new ResponseException("Error: unauthorized", 401);
            }
            authDAO.deleteAuth(logoutRequest.authToken());
        } catch (DataAccessException e) {
            throw new ResponseException("Error: database", 500);
        }
    }

    private AuthData makeAuth(UserData userData) throws ResponseException {
        AuthData authData = new AuthData(generateAuthToken(), userData.username());
        try {
            authDAO.insertAuth(authData);
        } catch (DataAccessException e) {
            throw new ResponseException("Error: database", 500);
        }
        return authData;
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    private UserData makeUser(RegisterRequest registerRequest) {
        String hashedPassword = BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt());
        return new UserData(registerRequest.username(), hashedPassword, registerRequest.email());
    }
}
