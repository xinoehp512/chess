package websocket.messages;

import chess.ChessGame;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    public final ServerMessageType serverMessageType;
    public final String errorMessage;
    public final String message;
    public final ChessGame game;


    public enum ServerMessageType {
        LOAD_GAME, ERROR, NOTIFICATION
    }

    public ServerMessage(ServerMessageType type, String message, ChessGame game) {
        this.serverMessageType = type;
        this.message = type == ServerMessageType.NOTIFICATION ? message : null;
        this.errorMessage = type == ServerMessageType.ERROR ? message : null;
        this.game = game;
    }

    public ServerMessage(ServerMessageType type, String message) {
        this(type, message, null);
    }

    public ServerMessage(ServerMessageType type, ChessGame game) {
        this(type, null, game);
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
