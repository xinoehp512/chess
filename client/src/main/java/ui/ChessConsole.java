package ui;

import exception.ResponseException;
import server.ServerFacade;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;

public class ChessConsole {
    private boolean isAuthenticated = false;
    private final ServerFacade server;

    public ChessConsole(String serverURL) {
        server = new ServerFacade(serverURL);
    }

    public void run() {
        System.out.println("Welcome to ♕ 240 Chess Client.");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        String result= "";
        while (!Objects.equals(result, "quit")){
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE+result);
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
                case "error" -> throw new ResponseException("Error",500);
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private void printPrompt() {
        System.out.printf("[%s]>>>", isAuthenticated ? "LOGGED IN" : "LOGGED OUT");
    }
}
