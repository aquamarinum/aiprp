package websocket.chat;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint(value = "/websocket/chat")
public class ChatAnnotation {

    private static final String GUEST_PREFIX = "Guest";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Set<ChatAnnotation> connections = 
        Collections.synchronizedSet(new HashSet<>());

    private final String nickname;
    private Session session;

    public ChatAnnotation() {
        nickname = GUEST_PREFIX + connectionIds.getAndIncrement();
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        connections.add(this);
        System.out.println("New WebSocket connection: " + nickname);
        
        // Отправляем приветствие новому клиенту
        try {
            session.getBasicRemote().sendText("Welcome " + nickname + "!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        broadcast("* " + nickname + " has joined");
    }

    @OnClose
    public void onClose(Session session) {
        connections.remove(this);
        System.out.println("Connection closed: " + nickname);
        broadcast("* " + nickname + " has disconnected");
    }

    @OnMessage
    public void incoming(String message) {
        if ("time".equalsIgnoreCase(message.trim())) {
            String currentTime = getCurrentTime();
            broadcast("Current time: " + currentTime);
        } else {
            if ("html".equalsIgnoreCase(message.trim())) {
            String html = getHtml();
            broadcast(html);
        }
            else {
            String filteredMessage = String.format("%s: %s", nickname, message);
            broadcast(filteredMessage);
        }
        }
        
    }


    @OnError
    public void onError(Session session, Throwable t) {
        System.err.println("WebSocket error for " + nickname + ": " + t.getMessage());
        t.printStackTrace();
    }
    
    private String processMessage(String message) {
        String lowerMessage = message.toLowerCase().trim();
        
        if (lowerMessage.equals("time") || lowerMessage.equals("время")) {
            return new java.util.Date().toString();
        }
        else if (lowerMessage.contains("dol") || lowerMessage.contains("дол")) {
            return "Курс доллара: 90.5 руб";
        }
        else if (lowerMessage.contains("eu") || lowerMessage.contains("евро")) {
            return "Курс евро: 98.3 руб";
        }
        else if (lowerMessage.equals("help") || lowerMessage.equals("помощь")) {
            return "Команды: time, dol, eu, help";
        }
        
        return null;
    }
    
    private void sendToSession(Session session, String message) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            System.err.println("Error sending to " + nickname + ": " + e.getMessage());
        }
    }
    
    private void broadcast(String message) {
        synchronized (connections) {
            for (ChatAnnotation client : connections) {
                sendToSession(client.session, message);
            }
        }
    }
    
    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalTime.now().format(formatter);
    }
    
    private String getHtml() {
        return "<!DOCTYPE html>" + 
               "<html>" + 
               "<head>" + 
               "<title>HTML Head</title>" + 
               "</head>" + 
               "<body>" + 
               "<p>HTML Service</p>" + 
               "</body>" + 
               "</html>";
    }

}