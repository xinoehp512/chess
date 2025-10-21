package dataaccess;

import models.GameData;

public interface GameDAO {
    GameData getGame(int gameID);

    void insertGame(GameData gameData);

    void deleteGame(int gameID) throws DataAccessException;

    void clear();
}
