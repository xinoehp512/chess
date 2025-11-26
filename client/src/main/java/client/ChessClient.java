package client;

import exception.ResponseException;
import models.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;
import server.ServerFacade;
import ui.ChessConsole;
import ui.ChessUI;
import ui.InputException;

public class ChessClient {
    private final ChessUI console;
    private final ServerFacade server;

    private String authToken = null;

    private ConsoleState state = ConsoleState.UNAUTHENTICATED;


    public enum ConsoleState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    public ChessClient(String serverURL) {
        this.console = new ChessConsole(this);
        server = new ServerFacade(serverURL);
    }

    public ConsoleState getState() {
        return state;
    }

    public void setState(ConsoleState state) {
        this.state = state;
    }

    public void run() {
        console.run();
    }

    public String createGame(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 1);
        assertAuthState(ConsoleState.AUTHENTICATED);
        String name = params[0];
        var response = server.createGame(new CreateGameRequest(name), authToken);
        return "Successfully created game named " + name;
    }

    public String login(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 2);
        assertAuthState(ConsoleState.UNAUTHENTICATED);
        String username = params[0];
        String password = params[1];
        var response = server.login(new LoginRequest(username, password));
        return "Logged in as " + response.username();
    }

    public String register(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 3);
        assertAuthState(ConsoleState.UNAUTHENTICATED);
        String username = params[0];
        String password = params[1];
        String email = params[2];
        var response = server.register(new RegisterRequest(username, password, email));
        authToken = response.authToken();
        state = ConsoleState.AUTHENTICATED;
        return "Logged in as " + response.username();
    }


    public String logout() throws ResponseException, InputException {
        assertAuthState(ConsoleState.AUTHENTICATED);
        server.logout(authToken);
        authToken = null;
        state = ConsoleState.UNAUTHENTICATED;
        return "Logged out successfully.";
    }

    public String observe(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 1);
        assertAuthState(ConsoleState.AUTHENTICATED);
        try {
            int listID = Integer.parseInt(params[0]);
            GameData game = getGameByListID(listID);
            return String.format("Observing game %d.", listID);

        } catch (NumberFormatException e) {
            throw new InputException(params[0] + " is not a number.");
        }
    }

    public String joinGame(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 2);
        assertAuthState(ConsoleState.AUTHENTICATED);
        try {
            int listID = Integer.parseInt(params[0]);
            String color = params[1].toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                throw new InputException(color + " is not black or white.");
            }
            GameData game = getGameByListID(listID);
            server.joinGame(new JoinGameRequest(color, game.gameID()), authToken);
            return String.format("Joined game %d.", listID);

        } catch (NumberFormatException e) {
            throw new InputException(params[0] + " is not a number.");
        }
    }

    private GameData getGameByListID(int listID) throws InputException, ResponseException {
        var gameDataList = server.listGames(authToken).games();
        if (listID < 1 || listID > gameDataList.size()) {
            throw new InputException(String.format("%d is out of range.", listID));
        }
        return gameDataList.get(listID - 1);
    }

    public String listGames() throws ResponseException, InputException {
        assertAuthState(ConsoleState.AUTHENTICATED);
        var gameDataList = server.listGames(authToken).games();
        var gameList = new StringBuilder();
        for (int i = 0; i < gameDataList.size(); i++) {
            var gameData = gameDataList.get(i);
            String gameName = gameData.gameName();
            String blackUsername = gameData.blackUsername();
            String whiteUsername = gameData.whiteUsername();
            String gameListing = String.format("%d: %s (Black: %s, White: %s)\n",
                    i + 1, gameName,
                    blackUsername != null ? blackUsername : "OPEN",
                    whiteUsername != null ? whiteUsername : "OPEN");
            gameList.append(gameListing);
        }
        return gameList.toString();
    }


    private void assertParamCount(String[] params, int paramCount) throws InputException {
        if (params.length != paramCount) {
            var message = String.format("Command expected %d arguments, received %d.", paramCount
                    , params.length);
            throw new InputException(message);
        }
    }

    private void assertAuthState(ConsoleState expectedState) throws InputException {
        if (state != expectedState) {
            var message = "Can't execute that command while " + switch (state) {
                case AUTHENTICATED -> "logged in.";
                case UNAUTHENTICATED -> "logged out.";
            };
            throw new InputException(message);
        }
    }
}
