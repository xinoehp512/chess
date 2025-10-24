package dataaccess;

import models.GameData;

import java.util.List;

public interface GameDAO {
    GameData getGame(int gameID);

    int insertGame(GameData gameData);

    void deleteGame(int gameID) throws DataAccessException;

    void clear();

    void updateGame(GameData updatedGame) throws DataAccessException;

    List<GameData> getAll();
}
