package dataaccess;

import com.google.gson.Gson;
import models.UserData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class DatabaseUserDAO implements UserDAO {

    public DatabaseUserDAO() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        String createStatement = """
                CREATE TABLE IF NOT EXISTS user (
                  `username` varchar(256) NOT NULL,
                  `password` varchar(256) NOT NULL,
                  `email` varchar(256) NOT NULL,
                  PRIMARY KEY (`username`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                """;
        executeUpdate(createStatement);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var statement = "SELECT username, password, email FROM user " + "WHERE " + "username=?";
        try (Connection connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return parseUserData(resultSet);
                    }
                }
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Database Error: ", e);
        }
        return null;
    }

    private UserData parseUserData(ResultSet resultSet) throws SQLException {
        return new UserData(resultSet.getString("username"), resultSet.getString("password"),
                resultSet.getString("email"));
    }

    @Override
    public void insertUser(UserData userData) throws DataAccessException {
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        executeUpdate(statement, userData.username(), userData.password(), userData.email());
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE user";
        executeUpdate(statement);
    }

    private void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    switch (param) {
                        case String p -> preparedStatement.setString(i + 1, p);
                        case Integer p -> preparedStatement.setInt(i + 1, p);
                        case Object p -> preparedStatement.setString(i + 1, new Gson().toJson(p));
                        case null -> preparedStatement.setNull(i + 1, NULL);
                    }
                }
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database Error: ", e);
        }
    }
}
