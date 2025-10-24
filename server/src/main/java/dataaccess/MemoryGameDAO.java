package dataaccess;

import models.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> table = new HashMap<>();
    private int id = 1;

    @Override
    public GameData getGame(int gameID) {
        return table.get(gameID);
    }

    @Override
    public int insertGame(GameData gameData) {
        var gameID=generateGameID();
        table.put(gameID, gameData.addID(gameID));
        return gameID;
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


    private int generateGameID() {
        return id++;
    }

}
