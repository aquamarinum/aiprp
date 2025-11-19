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
    private final int PORT = 3001;

    public ServerLogic(TextArea logArea) {
        this.logArea = logArea;
        this.clientSockets = new HashMap<>();
    }

    public void startServer() {
        try {
            server = new ServerSocket(PORT);
            server.setSoTimeout(1000);
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            appendLog("Сервер запущен на " + ipAddress + ":" + PORT);
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
                    try {
                        checkNewConnections();
                        checkClientRequests();
                        checkDisconnectedClients();
                    } catch (Exception e) {
                        appendLog("Неожиданная ошибка в основном цикле: " + e.getMessage());
                    }
                }
            }, 0, 500);

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

            clientSocket.setSoTimeout(1000);
            clientSockets.put(clientSocket, System.currentTimeMillis());
            connectedClients++;
            appendLog("Клиент подключен: " + clientSocket.getInetAddress() +
                    " (клиентов: " + connectedClients + "/" + MAX_CLIENTS + ")");

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (running) {
                appendLog("Ошибка при принятии подключения: " + e.getMessage());
            }
        }
    }

    private void checkClientRequests() {
        Iterator<Map.Entry<Socket, Long>> iterator = clientSockets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Socket, Long> entry = iterator.next();
            Socket clientSocket = entry.getKey();

            try {
                if (clientSocket.isClosed()) {
                    removeClient(iterator, clientSocket, "сокет закрыт");
                    continue;
                }

                InputStream input = clientSocket.getInputStream();
                if (input.available() > 0) {
                    BufferedReader dis = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                    String clientRequest = dis.readLine();

                    if (clientRequest != null) {
                        if (clientRequest.equals("REQUEST")) {
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
                }

            } catch (SocketTimeoutException e) {
                continue;
            } catch (IOException e) {
                removeClient(iterator, clientSocket, "ошибка ввода-вывода: " + e.getMessage());
            }
        }
    }

    private void checkDisconnectedClients() {
        long currentTime = System.currentTimeMillis();
        long timeout = 10000;

        Iterator<Map.Entry<Socket, Long>> iterator = clientSockets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Socket, Long> entry = iterator.next();
            Socket clientSocket = entry.getKey();

            if (currentTime - entry.getValue() > timeout) {
                try {
                    OutputStream output = clientSocket.getOutputStream();
                    output.write(0);
                    output.flush();

                    entry.setValue(currentTime);

                } catch (IOException e) {
                    removeClient(iterator, clientSocket, "таймаут неактивности");
                }
            }
        }
    }

    private void removeClient(Iterator<Map.Entry<Socket, Long>> iterator, Socket clientSocket, String reason) {
        iterator.remove();
        connectedClients--;
        appendLog("Клиент отключился (" + reason + ")");
        appendLog("Клиентов: " + connectedClients + "/" + MAX_CLIENTS);
        try {
            clientSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
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