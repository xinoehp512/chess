package dataaccess;

import models.GameData;

import java.util.List;

public class DatabaseGameDAO implements GameDAO{
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public int insertGame(GameData gameData) throws DataAccessException {
        return 0;
    }

    @Override
    public void deleteGame(int gameID) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public void updateGame(GameData updatedGame) throws DataAccessException {

    }

    @Override
    public List<GameData> getAll() throws DataAccessException {
        return List.of();
    }
}
