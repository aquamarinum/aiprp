package com.example;

import java.io.*;
import java.net.*;
import java.awt.*;

public class ClientLogic {
    private TextArea logArea;
    private int clientId;
    private String serverIp;
    private int serverPort;
    private boolean running = false;
    private Socket socket;
    private BufferedReader dis;
    private PrintStream ps;
    private Thread clientThread;

    public ClientLogic(TextArea logArea, int clientId, String serverIp, int serverPort) {
        this.logArea = logArea;
        this.clientId = clientId;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public void startClient() {
        running = true;

        clientThread = new Thread(() -> {
            appendLog("Клиент запущен");

            try {
                socket = new Socket(serverIp, serverPort);
                socket.setSoTimeout(1000);
                dis = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                ps = new PrintStream(socket.getOutputStream(), true, "UTF-8");
                appendLog("Подключен к серверу " + serverIp + ":" + serverPort);

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
        });
        clientThread.start();
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
        if (clientThread != null) {
            clientThread.interrupt();
        }
        closeResources();
    }

    public boolean isRunning() {
        return running;
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
