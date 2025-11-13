package ui;

import exception.ResponseException;
import requests.CreateGameRequest;
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

    private String observe(String[] params) throws ResponseException {
        return null;
    }

    private String joinGame(String[] params) throws ResponseException {
        return null;
    }

    private String listGames() throws ResponseException {
        return null;
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
