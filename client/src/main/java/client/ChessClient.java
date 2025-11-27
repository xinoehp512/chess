package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import exception.ResponseException;
import models.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;
import server.NotificationObserver;
import server.ServerFacade;
import ui.ChessConsole;
import ui.ChessUI;
import ui.InputException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import static chess.ChessGame.parseColor;
import static ui.ChessConsole.assertParamCount;
import static ui.ChessConsole.posFromString;

public class ChessClient implements NotificationObserver {
    private final ChessUI console;
    private final ServerFacade server;

    private String authToken = null;

    private GameData currentGameData;
    private ChessGame.TeamColor userColor;

    private ConsoleState state = ConsoleState.UNAUTHENTICATED;

    public enum ConsoleState {
        AUTHENTICATED, UNAUTHENTICATED, GAMEPLAY
    }

    public ChessClient(String serverURL) throws ResponseException {
        this.console = new ChessConsole(this);
        server = new ServerFacade(serverURL, this);
    }

    public ConsoleState getState() {
        return state;
    }

    public ChessGame getCurrentGame() {
        return currentGameData.game();
    }

    public ChessGame.TeamColor getUserColor() {
        return userColor;
    }

    public void run() {
        console.run();
    }

    public String createGame(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 1);
        String name = params[0];
        var response = server.createGame(new CreateGameRequest(name), authToken);
        return "Successfully created game named " + name;
    }

    public String login(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 2);
        String username = params[0];
        String password = params[1];
        var response = server.login(new LoginRequest(username, password));
        authToken = response.authToken();
        state = ConsoleState.AUTHENTICATED;
        return "Logged in as " + response.username();
    }

    public String register(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 3);
        String username = params[0];
        String password = params[1];
        String email = params[2];
        var response = server.register(new RegisterRequest(username, password, email));
        authToken = response.authToken();
        state = ConsoleState.AUTHENTICATED;
        return "Logged in as " + response.username();
    }

    public String logout() throws ResponseException, InputException {
        server.logout(authToken);
        authToken = null;
        state = ConsoleState.UNAUTHENTICATED;
        return "Logged out successfully.";
    }

    public String observe(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 1);
        try {
            int listID = Integer.parseInt(params[0]);
            GameData gameData = getGameByListID(listID);
            server.connect(gameData.gameID(), authToken);
            state = ConsoleState.GAMEPLAY;
            userColor = null;
            currentGameData = gameData;
            return String.format("Observing game %d.", listID);

        } catch (NumberFormatException e) {
            throw new InputException(params[0] + " is not a number.");
        }
    }

    public String joinGame(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 2);
        try {
            int listID = Integer.parseInt(params[0]);
            String color = params[1].toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                throw new InputException(color + " is not black or white.");
            }
            GameData gameData = getGameByListID(listID);
            server.joinGame(new JoinGameRequest(color, gameData.gameID()), authToken);
            server.connect(gameData.gameID(), authToken);
            state = ConsoleState.GAMEPLAY;
            userColor = parseColor(color);
            currentGameData = gameData;
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

    public String listGames() throws ResponseException {
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

    public String move(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 2);
        String startSquare = params[0];
        String endSquare = params[1];
        ChessPosition startPosition = posFromString(startSquare);
        ChessPosition endPosition = posFromString(endSquare);

        var game = currentGameData.game();
        ChessPiece movedPiece = game.getBoard().getPiece(startPosition);
//        if (movedPiece.getTeamColor() != userColor) {
//            throw new InputException("Can't move a piece that isn't your color!");
//        }
        if (userColor != game.getTeamTurn()) {
            throw new InputException("Can't move when it isn't your turn!");
        }
        ChessPiece.PieceType promotionPiece = null;
        if (game.isPromotion(movedPiece, endPosition)) {
            promotionPiece = console.promptPieceSelection(game.promotionPieces(movedPiece));
            if (promotionPiece == null) {
                return "Move cancelled.";
            }
        }

        var move = new ChessMove(startPosition, endPosition, promotionPiece);
        if (!game.validMoves(startPosition).contains(move)) {
            throw new InputException("Illegal move!");
        }

        server.move(currentGameData.gameID(), authToken, move);

        return "Piece moved.";
    }

    public String leave() throws ResponseException {
        server.leave(currentGameData.gameID(), authToken);
        state = ConsoleState.AUTHENTICATED;
        userColor = null;
        currentGameData = null;
        return "Left the game.";
    }

    public String resign() throws ResponseException {
        return "Resigned.";
    }


    @Override
    public void notify(ServerMessage serverMessage) {
        switch (serverMessage.getServerMessageType()) {
            case LOAD_GAME -> {
                currentGameData = currentGameData.replaceGame(serverMessage.game);
                console.showGame();
            }
            case ERROR -> {
                console.showError(serverMessage.errorMessage);
            }
            case NOTIFICATION -> {
                console.showNotification(serverMessage.message);
            }
        }
    }
}
