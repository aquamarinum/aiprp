package com.example;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class ClientLogic {
    private TextArea logArea;
    private int clientId;
    private String serverIp;
    private int serverPort;
    private boolean running = false;
    private Socket socket;
    private BufferedReader dis;
    private PrintStream ps;
    private Timer timer;

    public ClientLogic(TextArea logArea, int clientId, String serverIp, int serverPort) {
        this.logArea = logArea;
        this.clientId = clientId;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public void startClient() {
        running = true;

        try {
            socket = new Socket(serverIp, serverPort);
            socket.setSoTimeout(5000);
            dis = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            ps = new PrintStream(socket.getOutputStream(), true, "UTF-8");
            appendLog("Подключен к серверу " + serverIp + ":" + serverPort);

            sendRequest();

            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    if (!running) {
                        timer.cancel();
                        return;
                    }

                    try {
                        String response = dis.readLine();
                        if (response == null) {
                            appendLog("Сервер закрыл соединение");
                            stopClient();
                            return;
                        }
                        appendLog("Получено от сервера: " + response);
                    } catch (SocketTimeoutException e) {
                        return;
                    } catch (IOException e) {
                        if (running) {
                            appendLog("Ошибка чтения: " + e.getMessage());
                            stopClient();
                        }
                    }
                }
            }, 0, 100);

        } catch (IOException e) {
            appendLog("Ошибка подключения: " + e.getMessage());
            stopClient();
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
                stopClient();
            }
        } else {
            appendLog("Ошибка: нет подключения к серверу");
        }
    }

    public void stopClient() {
        running = false;

        if (timer != null) {
            timer.cancel();
        }

        closeResources();
        appendLog("Клиент полностью остановлен");
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
