package dataaccess;

import models.AuthData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class DatabaseAuthDAO implements AuthDAO {

    private final String[] createStatements = {"""
            CREATE TABLE IF NOT EXISTS  auth (
              `authToken` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              PRIMARY KEY (`authToken`),
              INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """};

    public DatabaseAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection connection = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = connection.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to connect to Database.", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var statement = "SELECT authToken, username FROM auth WHERE authToken=?";
        try (Connection connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return new AuthData(resultSet.getString("authToken"),
                                resultSet.getString("username"));
                    }
                }

            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Database Error: ", e);
        }
        return null;
    }

    @Override
    public void insertAuth(AuthData authData) throws DataAccessException {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (Connection connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, authData.authToken());
                preparedStatement.setString(2, authData.username());
                preparedStatement.execute();
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Database Error: ", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (getAuth(authToken) == null) {
            throw new DataAccessException("Auth Token is bad!");
        }
        var statement = "DELETE FROM auth WHERE authToken=?";
        try (Connection connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                preparedStatement.execute();
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Database Error: ", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE auth";
        try (Connection connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.execute();
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Database Error: ", e);
        }
    }

    @Override
    public boolean authIsValid(AuthData authData) {
        try {
            return Objects.equals(getAuth(authData.authToken()), authData);
        } catch (DataAccessException e) {
            return false;
        }
    }
}
