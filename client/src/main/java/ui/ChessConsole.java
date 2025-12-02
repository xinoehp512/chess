package ui;

import chess.ChessGame.TeamColor;
import chess.ChessPiece;
import chess.ChessPosition;
import client.ChessClient;
import exception.ResponseException;

import java.util.*;

import static ui.EscapeSequences.*;

public class ChessConsole implements ChessUI {


    private final ChessClient client;
    private Scanner scanner;

    public ChessConsole(ChessClient client) {
        this.client = client;
    }

    public void run() {
        System.out.println("Welcome to ♕ 240 Chess Client.");
        System.out.print(help());

        scanner = new Scanner(System.in);
        String result = "";
        while (!Objects.equals(result, "quit")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result + RESET_TEXT_COLOR + "\n");
            } catch (Exception e) {
                System.out.print(SET_TEXT_COLOR_RED + e.toString() + RESET_TEXT_COLOR + "\n");
            }
        }
    }

    private String help() {
        return switch (client.getState()) {
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
            case GAMEPLAY -> """ 
                      Squares are input <FILE><RANK>, e.g a1, e4.
                      redraw - the chess board
                      highlight <SQUARE> - legal moves
                      move <START_SQUARE> <END_SQUARE> [PROMOTION_PIECE] - a piece
                      leave - the game
                      resign - the game
                      help - with possible commands
                    """;
        };
    }

    private String eval(String command) {
        try {
            String[] tokens = command.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (client.getState()) {
                case AUTHENTICATED -> evalAuthenticated(cmd, params);
                case UNAUTHENTICATED -> evalUnauthenticated(cmd, params);
                case GAMEPLAY -> evalGameplay(cmd, params);
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
            case "create" -> client.createGame(params);
            case "list" -> client.listGames();
            case "join" -> client.joinGame(params);
            case "observe" -> client.observe(params);
            case "logout" -> client.logout();
            case "help" -> help();
            case "quit" -> "quit";
            case "register", "login" ->
                    throw new InputException("Can't use that command while logged in.");
            case "redraw", "highlight", "move", "leave", "resign" ->
                    throw new InputException("Can't use that command outside of a game.");
            default -> throw new InputException("Unknown command " + cmd);
        };
    }

    private String evalUnauthenticated(String cmd, String... params) throws ResponseException,
            InputException {
        return switch (cmd) {
            case "register" -> client.register(params);
            case "login" -> client.login(params);
            case "help" -> help();
            case "quit" -> "quit";
            case "redraw", "highlight", "move", "leave", "resign" ->
                    throw new InputException("Can't use that command outside of a game.");
            case "create", "list", "join", "observe", "logout" ->
                    throw new InputException("Can't use that command while logged out.");
            default -> throw new InputException("Unknown command " + cmd);
        };
    }

    private String evalGameplay(String cmd, String[] params) throws InputException,
            ResponseException {
        return switch (cmd) {
            case "redraw" -> redraw();
            case "highlight" -> highlight(params);
            case "move" -> client.move(params);
            case "leave" -> client.leave();
            case "resign" -> client.resign();
            case "help" -> help();
            case "register", "login" ->
                    throw new InputException("Can't use that command while logged in.");
            case "create", "list", "join", "observe", "logout", "quit" ->
                    throw new InputException("Can't use that command while in a game.");
            default -> throw new InputException("Unknown command " + cmd);
        };
    }

    private String displayBoard() {
        return displayBoard(null);
    }

    private String displayBoard(ChessPosition highlightedSquare) {

        TeamColor colorPerspective = client.getUserColor();
        var game = client.getCurrentGame();
        var board = game.getBoard().getBoard();

        var legalMoveSquares = new HashSet<ChessPosition>();
        if (highlightedSquare != null) {
            var legalMoves = game.validMoves(highlightedSquare);
            if (legalMoves != null) {
                for (var move : legalMoves) {
                    legalMoveSquares.add(move.getEndPosition());
                }
            }
        }

        var boardStr = new StringBuilder();

        var edgeRow = " abcdefgh ";
        var edgeCol = "12345678";

        String whiteTileColor = SET_BG_COLOR_LIGHT_GREEN;
        String blackTileColor = SET_BG_COLOR_DARK_GREEN;

        String whiteHighlightColor = SET_BG_COLOR_BLUE;
        String blackHiglightColor = SET_BG_COLOR_RED;

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

                ChessPosition pos = Convert.toChessPosition(colIdx, rowIdx);
                if (legalMoveSquares.contains(pos)) {
                    bgColor = squareColor ==
                              TeamColor.WHITE ? whiteHighlightColor : blackHiglightColor;
                }
                if (Objects.equals(pos, highlightedSquare)) {
                    bgColor = SET_BG_COLOR_MAGENTA;
                }
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

    private String highlight(String[] params) throws InputException {
        assertParamCount(params, 1);
        String square = params[0];
        ChessPosition highlightedPosition = posFromString(square);
        return displayBoard(highlightedPosition);
    }

    private String redraw() {
        return displayBoard();
    }

    private void printPrompt() {
        System.out.printf("[%s] >>> ", switch (client.getState()) {
            case AUTHENTICATED -> "LOGGED IN";
            case UNAUTHENTICATED -> "LOGGED OUT";
            case GAMEPLAY -> "PLAYING";
        });
    }


    @Override
    public void showGame() {
        System.out.println();
        System.out.print(displayBoard() + RESET_TEXT_COLOR);
        System.out.println();
        printPrompt();
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println(errorMessage);
        printPrompt();
    }

    @Override
    public void showNotification(String message) {
        System.out.println(message);
        printPrompt();
    }

    @Override
    public ChessPiece.PieceType promptPieceSelection(Set<ChessPiece.PieceType> pieceTypes) {
        while (true) {
            System.out.println("Choose a piece to promote to:");
            List<ChessPiece.PieceType> sortedPieceTypes = pieceTypes.stream().sorted().toList();
            for (int i = 0; i < sortedPieceTypes.size(); i++) {
                System.out.println(i + ": " + sortedPieceTypes.get(i).toString());
            }
            String line = scanner.nextLine();
            if (Objects.equals(line, "quit")){
                return null;
            }
            try {
                int listID = Integer.parseInt(line);
                return sortedPieceTypes.get(listID);
            } catch (NumberFormatException e) {
                System.out.println(line + " is not a number.");
            } catch (IndexOutOfBoundsException e) {
                System.out.println(line + " is not in range.");
            }
        }
    }


    public static void assertParamCount(String[] params, int paramCount) throws InputException {
        if (params.length != paramCount) {
            var message = String.format("Command expected %d arguments, received %d.", paramCount
                    , params.length);
            throw new InputException(message);
        }
    }


    public static ChessPosition posFromString(String square) throws InputException {
        if (square.length() != 2) {
            throw new InputException("Move " + square + " is invalid.");
        }
        char file = square.charAt(0);
        char rank = square.charAt(1);
        String files = "abcdefgh";
        String ranks = "12345678";
        int x = files.indexOf(file);
        int y = ranks.indexOf(rank);
        if (x < 0 || y < 0) {
            throw new InputException("Move " + square + " is invalid.");
        }
        return Convert.toChessPosition(x, y);
    }

    private static class Convert {
        static ChessPosition toChessPosition(int x, int y) {
            return new ChessPosition(y + 1, x + 1);
        }
    }
}
