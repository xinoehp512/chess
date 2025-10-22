package dataaccess;

import models.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> table = new HashMap<>();

    @Override
    public GameData getGame(int gameID) {
        return table.get(gameID);
    }

    @Override
    public void insertGame(GameData gameData) {
        table.put(gameData.gameID(), gameData);
    }

    @Override
    public void clear() {
        table.clear();
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        if (getGame(gameData.gameID()) == null) {
            throw new DataAccessException("Can't update a game that doesn't exist!");
        }
        table.put(gameData.gameID(), gameData);
    }

    @Override
    public List<GameData> getAll() {
        return new ArrayList<>(table.values());
    }
}
