package dataaccess;

import models.GameData;

import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO{
    private final Map<Integer, GameData> table = new HashMap<>();

    @Override
    public GameData getGame(int gameID) {
        return table.get(gameID);
    }

    @Override
    public void insertGame(GameData gameData) {
        table.put(gameData.gameID(),gameData);
    }

    @Override
    public void deleteGame(int gameID) throws DataAccessException {
        if (table.remove(gameID) == null) {
            throw new DataAccessException("Auth Token is bad!");
        }
    }

    @Override
    public void clear() {
        table.clear();
    }
}
