package dataaccess;

import models.GameData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GameDAOTest {

    @ParameterizedTest
    @ValueSource(classes = {MemoryGameDAO.class})
    void getGame(Class<? extends GameDAO> gameDAOClass) throws Exception {
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryGameDAO.class})
    void insertGame(Class<? extends GameDAO> gameDAOClass) throws Exception {
        var gameDAO = gameDAOClass.getDeclaredConstructor().newInstance();
        var gameData = new GameData(0,"white", "black", "game1", null);

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