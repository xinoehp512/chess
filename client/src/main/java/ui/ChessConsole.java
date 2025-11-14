package ui;

import chess.ChessGame;
import exception.ResponseException;
import models.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;
import server.ServerFacade;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessConsole {
    private boolean isAuthenticated = false;
    private String authToken = null;
    private final ServerFacade server;

    public ChessConsole(String serverURL) {
        server = new ServerFacade(serverURL);
    }

    public void run() {
        System.out.println("Welcome to ♕ 240 Chess Client.");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!Objects.equals(result, "quit")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Exception e) {
                System.out.print(e.toString());
            }
        }
    }

    private String help() {
        if (isAuthenticated) {
            return """
                      create <NAME> - a game
                      list - games
                      join <ID> [WHITE|BLACK] - a game
                      observe <ID> - a game
                      logout - when you are done
                      quit - playing chess
                      help - with possible commands
                    """;
        } else {
            return """
                      register - <USERNAME> <PASSWORD> <EMAIL> - to create an account
                      login - <USERNAME> <PASSWORD> - to play chess
                      quit - playing chess
                      help - with possible commands
                    """;
        }
    }

    private String eval(String command) {
        try {
            String[] tokens = command.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observe(params);
                case "logout" -> logout();
                case "help" -> help();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        } catch (InputException e) {
            return SET_TEXT_COLOR_RED + "Malformed command: " + e.getMessage() + RESET_TEXT_COLOR;
        }
    }

    private String logout() throws ResponseException, InputException {
        assertAuthState(true);
        server.logout(authToken);
        authToken = null;
        isAuthenticated = false;
        return "Logged out successfully.";
    }

    private String observe(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 1);
        assertAuthState(true);
        try {
            int listID = Integer.parseInt(params[0]);
            GameData game = getGameByListID(listID);
            return displayBoard(game, "WHITE");

        } catch (NumberFormatException e) {
            throw new InputException(params[0] + " is not a number.");
        }
    }

    private String joinGame(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 2);
        assertAuthState(true);
        try {
            int listID = Integer.parseInt(params[0]);
            String color = params[1].toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                throw new InputException(color + " is not black or white.");
            }
            GameData game = getGameByListID(listID);
            server.joinGame(new JoinGameRequest(color, game.gameID()), authToken);
            return displayBoard(game, color);

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

    private String displayBoard(GameData game, String colorPerspective) {
        displayBoardTemp(colorPerspective.equals("WHITE") ? ChessGame.TeamColor.WHITE :
                ChessGame.TeamColor.BLACK);

        return RESET_BG_COLOR + RESET_TEXT_COLOR;
    }

    public static void displayBoardTemp(ChessGame.TeamColor colorPerspective) {
        var boardStr = new StringBuilder();

        var board = switch (colorPerspective) {
            case WHITE -> new String[]{"rnbqkbnr", "pppppppp", "        ", "        ", "        ",
                    "   " + "     ", "PPPPPPPP", "RNBQKBNR",};

            case BLACK -> new String[]{"RNBKQBNR", "PPPPPPPP", "        ", "        ", "        ",
                    "   " + "   " + "  ", "pppppppp", "rnbkqbnr",};

        };

        var edgeRow = switch (colorPerspective) {
            case WHITE -> " abcdefgh ";
            case BLACK -> " hgfedcba ";
        };

        var edgeCol = switch (colorPerspective) {
            case WHITE -> "87654321";
            case BLACK -> "12345678";
        };


        boardStr.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK);
        for (int i = 0; i < edgeRow.length(); i++) {
            char character = edgeRow.charAt(i);
            boardStr.append(String.format(" %c ", character));
        }

        ChessGame.TeamColor squareColor = ChessGame.TeamColor.WHITE;
        boardStr.append(RESET_BG_COLOR + "\n");
        for (int i = 0; i < board.length; i++) {
            String row = board[i];
            boardStr.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK);
            boardStr.append(String.format(" %c ", edgeCol.charAt(i)));
            if (i < 4) {
                switch (colorPerspective) {
                    case WHITE -> {
                        boardStr.append(SET_TEXT_COLOR_BLUE);
                    }
                    case BLACK -> {
                        boardStr.append(SET_TEXT_COLOR_RED);
                    }
                }
            } else {
                switch (colorPerspective) {
                    case WHITE -> {
                        boardStr.append(SET_TEXT_COLOR_RED);
                    }
                    case BLACK -> {
                        boardStr.append(SET_TEXT_COLOR_BLUE);
                    }
                }
            }
            for (int j = 0; j < row.length(); j++) {
                char character = row.charAt(j);
                switch (squareColor) {
                    case WHITE -> {
                        boardStr.append(SET_BG_COLOR_WHITE);
                        squareColor = ChessGame.TeamColor.BLACK;
                    }
                    case BLACK -> {
                        boardStr.append(SET_BG_COLOR_BLACK);
                        squareColor = ChessGame.TeamColor.WHITE;
                    }
                }
                boardStr.append(String.format(" %c ", character));
            }
            boardStr.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK);
            boardStr.append(String.format(" %c ", edgeCol.charAt(i)));
            boardStr.append(RESET_BG_COLOR + "\n");
            switch (squareColor) {
                case WHITE -> {
                    squareColor = ChessGame.TeamColor.BLACK;
                }
                case BLACK -> {
                    squareColor = ChessGame.TeamColor.WHITE;
                }
            }
        }
        boardStr.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK);
        for (int i = 0; i < edgeRow.length(); i++) {
            char character = edgeRow.charAt(i);
            boardStr.append(String.format(" %c ", character));
        }
        System.out.print(boardStr.toString());
    }

    private String listGames() throws ResponseException, InputException {
        assertAuthState(true);
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

    private String createGame(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 1);
        assertAuthState(true);
        String name = params[0];
        var response = server.createGame(new CreateGameRequest(name), authToken);
        return "Successfully created game named " + name;
    }

    private String login(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 2);
        assertAuthState(false);
        String username = params[0];
        String password = params[1];
        var response = server.login(new LoginRequest(username, password));
        authToken = response.authToken();
        isAuthenticated = true;
        return "Logged in as " + response.username();
    }

    private String register(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 3);
        assertAuthState(false);
        String username = params[0];
        String password = params[1];
        String email = params[2];
        var response = server.register(new RegisterRequest(username, password, email));
        authToken = response.authToken();
        isAuthenticated = true;
        return "Logged in as " + response.username();
    }

    private void assertAuthState(boolean expectedState) throws InputException {
        if (isAuthenticated != expectedState) {
            var message = "Can't execute that command while " +
                          (isAuthenticated ? "logged in." : "logged out.");
            throw new InputException(message);
        }
    }

    private void assertParamCount(String[] params, int paramCount) throws InputException {
        if (params.length != paramCount) {
            var message = String.format("Command expected %d arguments, received %d.", paramCount
                    , params.length);
            throw new InputException(message);
        }
    }

    private void printPrompt() {
        System.out.printf(
                RESET_TEXT_COLOR + "\n[%s] >>> ", isAuthenticated ? "LOGGED IN" : "LOGGED OUT");
    }
}
