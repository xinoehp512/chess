import ui.ChessConsole;

public class Main {
    public static void main(String[] args) {
        var serverURL="http://localhost:8080";
        var console = new ChessConsole(serverURL);
        console.run();
    }
}