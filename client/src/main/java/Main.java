import client.ChessClient;
import exception.ResponseException;
import ui.ChessConsole;

public class Main {
    public static void main(String[] args) {
        var serverURL = "http://localhost:8080";
        try {
            var client = new ChessClient(serverURL);
            client.run();
        } catch (ResponseException e) {
            System.out.println(e.getMessage());
        }
    }
}