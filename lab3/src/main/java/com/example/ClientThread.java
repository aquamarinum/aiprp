package com.example;

import java.io.*;
import java.net.*;
import java.awt.*;

public class ClientThread extends Thread {
    private TextArea logArea;
    private int clientId;
    private boolean running = true;
    private Socket socket;
    private BufferedReader dis;
    private PrintStream ps;

    public ClientThread(TextArea logArea, int clientId) {
        this.logArea = logArea;
        this.clientId = clientId;
    }

    public void run() {
        appendLog("Клиент запущен");

        try {
            socket = new Socket("127.0.0.1", 3001);
            socket.setSoTimeout(1000);
            dis = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            ps = new PrintStream(socket.getOutputStream(), true, "UTF-8");
            appendLog("Подключен к серверу");

            sendRequest();

            while (running) {
                try {
                    String response = dis.readLine();
                    if (response == null) {
                        appendLog("Сервер закрыл соединение");
                        break;
                    }
                    appendLog("Получено от сервера: " + response);
                } catch (SocketTimeoutException e) {
                    continue;
                } catch (IOException e) {
                    if (running) {
                        appendLog("Ошибка чтения: " + e.getMessage());
                    }
                    break;
                }
            }

        } catch (IOException e) {
            appendLog("Ошибка подключения: " + e.getMessage());
        } finally {
            closeResources();
            appendLog("Клиент полностью остановлен");
        }
    }

    public void sendRequest() {
        if (ps != null && running && socket != null && !socket.isClosed()) {
            try {
                ps.println("REQUEST");
                ps.flush();
                appendLog("Запрос отправлен серверу");
            } catch (Exception e) {
                appendLog("Ошибка отправки запроса: " + e.getMessage());
            }
        } else {
            appendLog("Ошибка: нет подключения к серверу");
        }
    }

    public void stopClient() {
        running = false;
        interrupt();
        closeResources();
    }

    private void closeResources() {
        try {
            if (dis != null) dis.close();
            if (ps != null) ps.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            appendLog("Ошибка закрытия: " + e.getMessage());
        }
    }

    private void appendLog(String message) {
        if (logArea != null) {
            logArea.append("[Клиент #" + clientId + "] " + message + "\n");
        }
    }
}