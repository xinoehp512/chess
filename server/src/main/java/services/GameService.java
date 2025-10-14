package services;


import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;

public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public void clear() {
    }
}
