package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, Set<Session>> connections = new ConcurrentHashMap<>();


    public void add(int gameID, Session session) {
        connections.putIfAbsent(gameID, new HashSet<>());
        connections.get(gameID).add(session);
    }

    public void remove(int gameID, Session session) {
        if (!connections.containsKey(gameID)) {
            return;
        }
        connections.get(gameID).remove(session);
    }

    public void send(ServerMessage serverMessage, Session session) throws IOException {
        String message = new Gson().toJson(serverMessage);
        if (session.isOpen()) {
            session.getRemote().sendString(message);
        }
    }

    public void broadcast(int gameID, ServerMessage serverMessage, Session excludeSession) throws IOException {
        for (var session : connections.get(gameID)) {
            if (session.equals(excludeSession)) {
                continue;
            }
            send(serverMessage, session);
        }
    }
}
