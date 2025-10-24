package dataaccess;

import models.GameData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GameDAOTest {

    @ParameterizedTest
    @ValueSource(classes = {MemoryGameDAO.class})
    void getNullGame(Class<? extends GameDAO> gameDAOClass) throws Exception {
        var gameDAO = gameDAOClass.getDeclaredConstructor().newInstance();
        assertNull(gameDAO.getGame(0));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryGameDAO.class})
    void insertGame(Class<? extends GameDAO> gameDAOClass) throws Exception {
        var gameDAO = gameDAOClass.getDeclaredConstructor().newInstance();
        var gameData = new GameData(0, "white", "black", "game1", null);
        var gameID = gameDAO.insertGame(gameData);
        var storedGameData = gameDAO.getGame(gameID);
        assertEquals(gameData.addID(gameID), storedGameData);
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryGameDAO.class})
    void deleteGame(Class<? extends GameDAO> gameDAOClass) throws Exception {
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryGameDAO.class})
    void clear(Class<? extends GameDAO> gameDAOClass) throws Exception {
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryGameDAO.class})
    void updateGame(Class<? extends GameDAO> gameDAOClass) throws Exception {
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryGameDAO.class})
    void getAll(Class<? extends GameDAO> gameDAOClass) throws Exception {
    }
}