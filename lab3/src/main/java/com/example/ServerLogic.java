package com.example;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;

public class ServerLogic {
    private ServerSocket server;
    private static int amount = 200;
    private TextArea logArea;
    private boolean running = false;
    private int connectedClients = 0;
    private static final int MAX_CLIENTS = 2;
    private Map<Socket, Long> clientSockets;
    private Timer timer;

    public ServerLogic(TextArea logArea) {
        this.logArea = logArea;
        this.clientSockets = new HashMap<>();
    }

    public void startServer() {
        try {
            server = new ServerSocket(3001);
            server.setSoTimeout(100);
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            appendLog("Сервер запущен на " + ipAddress + ":3001");
            appendLog("Начальный счет: " + amount);
            appendLog("Максимальное количество клиентов: " + MAX_CLIENTS);

            running = true;

            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    if (!running) {
                        timer.cancel();
                        return;
                    }
                    checkNewConnections();
                    checkClientRequests();
                    checkDisconnectedClients();
                }
            }, 0, 100);

        } catch (IOException e) {
            appendLog("Ошибка запуска сервера: " + e.getMessage());
        }
    }

    private void checkNewConnections() {
        try {
            Socket clientSocket = server.accept();

            if (connectedClients >= MAX_CLIENTS) {
                appendLog("Отклонено подключение: достигнут лимит клиентов");
                clientSocket.close();
                return;
            }

            clientSocket.setSoTimeout(100);
            clientSockets.put(clientSocket, System.currentTimeMillis());
            connectedClients++;
            appendLog("Клиент подключен: " + clientSocket.getInetAddress() +
                    " (клиентов: " + connectedClients + "/" + MAX_CLIENTS + ")");

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (running) {
                appendLog("Ошибка accept: " + e.getMessage());
            }
        }
    }

    private void checkClientRequests() {
        Iterator<Map.Entry<Socket, Long>> iterator = clientSockets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Socket, Long> entry = iterator.next();
            Socket clientSocket = entry.getKey();

            try {
                if (clientSocket.isClosed() || !clientSocket.isConnected()) {
                    iterator.remove();
                    connectedClients--;
                    appendLog("Клиент отключился (определено по состоянию сокета)");
                    appendLog("Клиентов: " + connectedClients + "/" + MAX_CLIENTS);
                    continue;
                }

                clientSocket.sendUrgentData(0xFF);

                InputStream input = clientSocket.getInputStream();
                if (input.available() > 0) {
                    BufferedReader dis = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                    String clientRequest = dis.readLine();

                    if (clientRequest != null && clientRequest.equals("REQUEST")) {
                        appendLog("Получен запрос от клиента");

                        int amountCur = (int)(Math.random() * 1000);
                        String operation;

                        if (Math.random() > 0.5) {
                            amount -= amountCur;
                            operation = "Снятие";
                        } else {
                            amount += amountCur;
                            operation = "Пополнение";
                        }

                        String message = "Счет:" + amount + " (" + operation + " " + amountCur + ")";
                        PrintStream ps = new PrintStream(clientSocket.getOutputStream(), true, "UTF-8");
                        ps.println(message);

                        appendLog("Отправлено клиенту: " + message);
                    }

                    entry.setValue(System.currentTimeMillis());
                }

            } catch (IOException e) {
                iterator.remove();
                connectedClients--;
                appendLog("Клиент отключился (ошибка соединения: " + e.getMessage() + ")");
                appendLog("Клиентов: " + connectedClients + "/" + MAX_CLIENTS);
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void checkDisconnectedClients() {
        long currentTime = System.currentTimeMillis();
        long timeout = 5000;

        Iterator<Map.Entry<Socket, Long>> iterator = clientSockets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Socket, Long> entry = iterator.next();
            Socket clientSocket = entry.getKey();

            if (currentTime - entry.getValue() > timeout) {
                try {
                    clientSocket.sendUrgentData(0xFF);
                    entry.setValue(currentTime);
                } catch (IOException e) {
                    iterator.remove();
                    connectedClients--;
                    appendLog("Клиент отключился по таймауту");
                    appendLog("Клиентов: " + connectedClients + "/" + MAX_CLIENTS);
                    try {
                        clientSocket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public void stopServer() {
        running = false;

        if (timer != null) {
            timer.cancel();
        }

        for (Socket socket : clientSockets.keySet()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        clientSockets.clear();
        connectedClients = 0;

        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            appendLog("Ошибка закрытия сервера: " + e.getMessage());
        }
        appendLog("Сервер остановлен");
    }

    public boolean isRunning() {
        return running;
    }

    private void appendLog(String message) {
        if (logArea != null) {
            logArea.append("[Сервер] " + message + "\n");
        }
    }
}
