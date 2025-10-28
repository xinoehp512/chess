package dataaccess;

import models.AuthData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        try (Connection connection = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth WHERE authToken=?";
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
            throw new DataAccessException("Couldn't get Auth");
        }
        return null;
    }

    @Override
    public void insertAuth(AuthData authData) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, authData.authToken());
                preparedStatement.setString(2, authData.username());
                preparedStatement.execute();
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Couldn't insert Auth");
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            var statement = "TRUNCATE auth";
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.execute();
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Unable to clear database.");
        }
    }

    @Override
    public boolean authIsValid(AuthData authData) {
        return false;
    }
}
