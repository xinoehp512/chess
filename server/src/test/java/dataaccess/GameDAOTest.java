package dataaccess;

import models.GameData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameDAOTest {

    private static List<Class<? extends GameDAO>> provideClasses() {
        return List.of(MemoryGameDAO.class, DatabaseGameDAO.class);
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void getNullGame(Class<? extends GameDAO> gameDAOClass) throws Exception {
        var gameDAO = gameDAOClass.getDeclaredConstructor().newInstance();
        assertNull(gameDAO.getGame(0));
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void insertGame(Class<? extends GameDAO> gameDAOClass) throws Exception {
        var gameDAO = gameDAOClass.getDeclaredConstructor().newInstance();
        var gameData = new GameData(0, "white", "black", "game1", null);
        var gameID = gameDAO.insertGame(gameData);
        var storedGameData = gameDAO.getGame(gameID);
        assertEquals(gameData.addID(gameID), storedGameData);
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void deleteGame(Class<? extends GameDAO> gameDAOClass) throws Exception {
        var gameDAO = gameDAOClass.getDeclaredConstructor().newInstance();
        var gameData = new GameData(0, "white", "black", "game1", null);
        var gameID = gameDAO.insertGame(gameData);
        gameDAO.deleteGame(gameID);
        assertNull(gameDAO.getGame(gameID));
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void deleteNoGame(Class<? extends GameDAO> gameDAOClass) throws Exception {
        var gameDAO = gameDAOClass.getDeclaredConstructor().newInstance();
        assertThrows(DataAccessException.class, () -> gameDAO.deleteGame(0));
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
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
    @MethodSource("provideClasses")
    void updateGame(Class<? extends GameDAO> gameDAOClass) throws Exception {
        var gameDAO = gameDAOClass.getDeclaredConstructor().newInstance();
        var gameData = new GameData(0, null, null, "null", null);
        var gameID = gameDAO.insertGame(gameData);
        var gameData2 = new GameData(gameID, "white", "black", "game1", null);
        gameDAO.updateGame(gameData2);
        assertEquals(gameData2, gameDAO.getGame(gameID));
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void updateNoGame(Class<? extends GameDAO> gameDAOClass) throws Exception {
        var gameDAO = gameDAOClass.getDeclaredConstructor().newInstance();
        var gameData = new GameData(0, "white", "black", "game1", null);
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(gameData));
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void getAll(Class<? extends GameDAO> gameDAOClass) throws Exception {
        var gameDAO = gameDAOClass.getDeclaredConstructor().newInstance();

        String[] whiteUsernames = {"white1", "white2", "white3"};
        String[] blackUsernames = {"black1", "black2", "black3"};
        String[] gameNames = {"game1", "game2", "game3"};

        var gameData = new HashSet<>();
        int[] gameIDs = new int[3];
        for (var i = 0; i < 3; i++) {
            var gameDatum = new GameData(0, whiteUsernames[i], blackUsernames[i], gameNames[i],
                    null);
            gameIDs[i] = gameDAO.insertGame(gameDatum);
            gameData.add(gameDatum.addID(gameIDs[i]));
        }

        var gameDataRetrieved = new HashSet<>(gameDAO.getAll());
        assertEquals(gameData, gameDataRetrieved);
    }
}