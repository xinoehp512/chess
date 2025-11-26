package server;

import websocket.messages.ServerMessage;

public interface NotificationObserver {
    void notify(ServerMessage serverMessage);
}
