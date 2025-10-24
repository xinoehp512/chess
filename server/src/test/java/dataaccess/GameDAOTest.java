package dataaccess;

import models.AuthData;
import models.GameData;
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
        var gameDAO = gameDAOClass.getDeclaredConstructor().newInstance();
        var gameData = new GameData(0, "white", "black", "game1", null);
        var gameID = gameDAO.insertGame(gameData);
        gameDAO.deleteGame(gameID);
        assertNull(gameDAO.getGame(gameID));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryGameDAO.class})
    void clear(Class<? extends GameDAO> gameDAOClass) throws Exception {
        var gameDAO = gameDAOClass.getDeclaredConstructor().newInstance();

        int[] gameIDs = new int[3];
        for (var i = 0; i < 3; i++) {
            var gameData = new GameData(0, "white", "black", "game1", null);
            gameIDs[i] = gameDAO.insertGame(gameData);
        }
        gameDAO.clear();
        for (var i = 0; i < 3; i++) {
            assertNull(gameDAO.getGame(gameIDs[i]));
        }
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