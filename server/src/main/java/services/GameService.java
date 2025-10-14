package services;


import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;

public class GameService {
    private final GameDAO gameDAO = new MemoryGameDAO();

    public void clear() {
    }
}
