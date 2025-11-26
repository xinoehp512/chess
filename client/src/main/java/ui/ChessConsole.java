package ui;

import chess.ChessGame.TeamColor;
import chess.ChessPiece;
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


    private ConsoleState state = ConsoleState.UNAUTHENTICATED;
    private String authToken = null;
    private final ServerFacade server;

    public enum ConsoleState {
        AUTHENTICATED, UNAUTHENTICATED, GAMEPLAY
    }

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
        return switch (state) {
            case AUTHENTICATED -> """
                      create <NAME> - a game
                      list - games
                      join <ID> [WHITE|BLACK] - a game
                      observe <ID> - a game
                      logout - when you are done
                      quit - playing chess
                      help - with possible commands
                    """;
            case UNAUTHENTICATED -> """
                      register - <USERNAME> <PASSWORD> <EMAIL> - to create an account
                      login - <USERNAME> <PASSWORD> - to play chess
                      quit - playing chess
                      help - with possible commands
                    """;
            case GAMEPLAY -> "null";
        };
    }

    private String eval(String command) {
        try {
            String[] tokens = command.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (state) {
                case AUTHENTICATED -> evalAuthenticated(cmd, params);
                case UNAUTHENTICATED -> evalUnauthenticated(cmd, params);
                case GAMEPLAY -> null;
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        } catch (InputException e) {
            return SET_TEXT_COLOR_RED + "Malformed command: " + e.getMessage() + RESET_TEXT_COLOR;
        }
    }

    private String evalAuthenticated(String cmd, String[] params) throws ResponseException,
            InputException {
        return switch (cmd) {
            case "create" -> createGame(params);
            case "list" -> listGames();
            case "join" -> joinGame(params);
            case "observe" -> observe(params);
            case "logout" -> logout();
            case "help" -> help();
            case "quit" -> "quit";
            default -> throw new InputException("Unknown command " + cmd);
        };
    }

    private String evalUnauthenticated(String cmd, String... params) throws ResponseException,
            InputException {
        return switch (cmd) {
            case "register" -> register(params);
            case "login" -> login(params);
            case "help" -> help();
            case "quit" -> "quit";
            default -> throw new InputException("Unknown command " + cmd);
        };
    }


    private String logout() throws ResponseException, InputException {
        assertAuthState(ConsoleState.AUTHENTICATED);
        server.logout(authToken);
        authToken = null;
        state = ConsoleState.UNAUTHENTICATED;
        return "Logged out successfully.";
    }

    private String observe(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 1);
        assertAuthState(ConsoleState.AUTHENTICATED);
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
        assertAuthState(ConsoleState.AUTHENTICATED);
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

    private String displayBoard(GameData gameData, String colorPerspectiveStr) {
        TeamColor colorPerspective = colorPerspectiveStr.equals("WHITE") ? TeamColor.WHITE :
                TeamColor.BLACK;
        var board = gameData.game().getBoard().getBoard();
        var boardStr = new StringBuilder();

        var edgeRow = " abcdefgh ";
        var edgeCol = "12345678";

        String whiteTileColor = SET_BG_COLOR_LIGHT_GREEN;
        String blackTileColor = SET_BG_COLOR_DARK_GREEN;

        String whitePieceColor = SET_TEXT_COLOR_WHITE;
        String blackPieceColor = SET_TEXT_COLOR_BLACK;

        String borderFormat = SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK;

        boolean reversed = (colorPerspective == TeamColor.BLACK);


        boardStr.append(borderFormat);
        for (int i = 0; i < edgeRow.length(); i++) {
            var idx = reversed ? edgeRow.length() - i - 1 : i;
            char character = edgeRow.charAt(idx);
            boardStr.append(String.format(" %c ", character));
        }

        TeamColor squareColor = TeamColor.WHITE;
        boardStr.append(RESET_BG_COLOR + "\n");
        for (int i = 0; i < board.length; i++) {
            var rowIdx = reversed ? i : board.length - i - 1;
            ChessPiece[] row = board[rowIdx];
            boardStr.append(borderFormat);
            boardStr.append(String.format(" %c ", edgeCol.charAt(rowIdx)));
            for (int j = 0; j < row.length; j++) {
                var colIdx = !reversed ? j : row.length - j - 1;
                var bgColor = squareColor == TeamColor.WHITE ? whiteTileColor : blackTileColor;
                squareColor = squareColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
                boardStr.append(bgColor);

                ChessPiece chessPiece = row[colIdx];
                char character = ' ';
                if (chessPiece != null) {
                    character = chessPiece.getChar();
                    character = Character.toUpperCase(character);
                    var textColor = chessPiece.getTeamColor() ==
                                    TeamColor.WHITE ? whitePieceColor : blackPieceColor;
                    boardStr.append(textColor);

                }
                boardStr.append(String.format(" %c ", character));
            }
            boardStr.append(borderFormat);
            boardStr.append(String.format(" %c ", edgeCol.charAt(rowIdx)));
            boardStr.append(RESET_BG_COLOR + "\n");
            squareColor = squareColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
        }
        boardStr.append(borderFormat);
        for (int i = 0; i < edgeRow.length(); i++) {
            var idx = reversed ? edgeRow.length() - i - 1 : i;
            char character = edgeRow.charAt(idx);
            boardStr.append(String.format(" %c ", character));
        }
        System.out.print(boardStr.toString());

        return RESET_BG_COLOR + RESET_TEXT_COLOR;
    }

    private String listGames() throws ResponseException, InputException {
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

    private String createGame(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 1);
        assertAuthState(ConsoleState.AUTHENTICATED);
        String name = params[0];
        var response = server.createGame(new CreateGameRequest(name), authToken);
        return "Successfully created game named " + name;
    }

    private String login(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 2);
        assertAuthState(ConsoleState.UNAUTHENTICATED);
        String username = params[0];
        String password = params[1];
        var response = server.login(new LoginRequest(username, password));
        authToken = response.authToken();
        state = ConsoleState.AUTHENTICATED;
        return "Logged in as " + response.username();
    }

    private String register(String[] params) throws ResponseException, InputException {
        assertParamCount(params, 3);
        assertAuthState(ConsoleState.AUTHENTICATED);
        String username = params[0];
        String password = params[1];
        String email = params[2];
        var response = server.register(new RegisterRequest(username, password, email));
        authToken = response.authToken();
        state = ConsoleState.AUTHENTICATED;
        return "Logged in as " + response.username();
    }

    private void assertAuthState(ConsoleState expectedState) throws InputException {
        if (state != expectedState) {
            var message = "Can't execute that command while " + switch (state) {
                case AUTHENTICATED -> "logged in.";
                case UNAUTHENTICATED -> "logged out.";
                case GAMEPLAY -> "playing a game.";
            };
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
        System.out.printf(RESET_TEXT_COLOR + "\n[%s] >>> ", switch (state) {
            case AUTHENTICATED -> "LOGGED IN";
            case UNAUTHENTICATED -> "LOGGED OUT";
            case GAMEPLAY -> "IN GAME";
        });
    }
}
