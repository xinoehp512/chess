package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import models.GameData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class DatabaseGameDAO implements GameDAO {

    public DatabaseGameDAO() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        String createStatement = """
                CREATE TABLE IF NOT EXISTS  game (
                  `gameID` int NOT NULL AUTO_INCREMENT,
                  `whiteUsername` varchar(256) DEFAULT NULL,
                  `blackUsername` varchar(256) DEFAULT NULL,
                  `gameName` varchar(256) NOT NULL,
                  `game` longtext DEFAULT NULL,
                  PRIMARY KEY (`gameID`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                """;
        executeUpdate(createStatement);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game " +
                        "WHERE " + "gameID=?";
        try (Connection connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setInt(1, gameID);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return parseGameData(resultSet);
                    }
                }
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Database Error: ", e);
        }
        return null;
    }

    private GameData parseGameData(ResultSet resultSet) throws SQLException {
        return new GameData(resultSet.getInt("gameID"), resultSet.getString("whiteUsername"),
                resultSet.getString("blackUsername"), resultSet.getString("gameName"),
                readGame(resultSet.getString("game")));
    }

    private ChessGame readGame(String game) {
        return new Gson().fromJson(game, ChessGame.class);
    }

    @Override
    public int insertGame(GameData gameData) throws DataAccessException {

        var statement = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) " +
                        "VALUES (?, ?, ?, ?, ?)";
        return executeUpdate(statement, gameData.gameID(), gameData.whiteUsername(),
                gameData.blackUsername(), gameData.gameName(), gameData.game());
    }

    @Override
    public void deleteGame(int gameID) throws DataAccessException {
        if (getGame(gameID) == null) {
            throw new DataAccessException("Auth Token is bad!");
        }
        var statement = "DELETE FROM game WHERE gameID=?";
        executeUpdate(statement, gameID);
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE game";
        executeUpdate(statement);
    }

    @Override
    public void updateGame(GameData updatedGame) throws DataAccessException {
        if (getGame(updatedGame.gameID()) == null) {
            throw new DataAccessException("Auth Token is bad!");
        }
        var statement = "UPDATE game SET whiteUsername=?, blackUsername=?, gameName=?, game=? " +
                        "WHERE gameID=?";
        executeUpdate(statement, updatedGame.whiteUsername(), updatedGame.blackUsername(),
                updatedGame.gameName(), updatedGame.game(), updatedGame.gameID());
    }

    @Override
    public List<GameData> getAll() throws DataAccessException {
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game";
        List<GameData> gameList = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        gameList.add(parseGameData(resultSet));
                    }
                }
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Database Error: ", e);
        }
        return gameList;
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
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

                ResultSet rs = preparedStatement.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database Error: ", e);
        }
    }
}
