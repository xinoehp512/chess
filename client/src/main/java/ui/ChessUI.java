package ui;

import websocket.messages.ServerMessage;

public interface ChessUI {
    void run();

    void showGame();

    void showError(String errorMessage);

    void showNotification(String message);
}
