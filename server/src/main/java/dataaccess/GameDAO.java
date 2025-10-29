package dataaccess;

import models.GameData;

import java.util.List;

public interface GameDAO {
    GameData getGame(int gameID) throws DataAccessException;

    int insertGame(GameData gameData) throws DataAccessException;

    void deleteGame(int gameID) throws DataAccessException;

    void clear() throws DataAccessException;

    void updateGame(GameData updatedGame) throws DataAccessException;

    List<GameData> getAll() throws DataAccessException;
}
