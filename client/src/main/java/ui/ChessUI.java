package ui;

import websocket.messages.ServerMessage;

public interface ChessUI {
    void run();

    void notifyUser(ServerMessage serverMessage);
}
