package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final Set<Session> connections =
            Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());


    public void add(Session session) {
        connections.add(session);
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void send(ServerMessage serverMessage, Session session) throws IOException {
        String message = new Gson().toJson(serverMessage);
        if (session.isOpen()) {
            session.getRemote().sendString(message);
        }
    }

    public void broadcast(ServerMessage serverMessage, Session excludeSession) throws IOException {
        for (var session : connections) {
            if (session.equals(excludeSession)) {
                continue;
            }
            send(serverMessage, session);
        }
    }
}
