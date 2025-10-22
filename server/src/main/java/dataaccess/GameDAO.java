package dataaccess;

import models.GameData;

import java.util.List;

public interface GameDAO {
    GameData getGame(int gameID);

    void insertGame(GameData gameData);

    void clear();

    void updateGame(GameData updatedGame) throws DataAccessException;

    List<GameData> getAll();
}
