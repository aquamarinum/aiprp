package com.example;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class ServerLogic {
    private ServerSocket server;
    private static int amount = 200;
    private TextArea logArea;
    private boolean running = false;
    private int connectedClients = 0;
    private static final int MAX_CLIENTS = 2;
    private Thread serverThread;
    private List<ClientHandler> clientHandlers;

    public ServerLogic(TextArea logArea) {
        this.logArea = logArea;
        this.clientHandlers = new CopyOnWriteArrayList<>();
    }

    public void startServer() {
        try {
            server = new ServerSocket(3001);
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            appendLog("Сервер запущен на " + ipAddress + ":3001");
            appendLog("Начальный счет: " + amount);
            appendLog("Максимальное количество клиентов: " + MAX_CLIENTS);

            running = true;

            serverThread = new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = server.accept();

                        if (connectedClients >= MAX_CLIENTS) {
                            appendLog("Отклонено подключение: достигнут лимит клиентов");
                            clientSocket.close();
                            continue;
                        }

                        connectedClients++;
                        appendLog("Клиент подключен: " + clientSocket.getInetAddress() +
                                " (клиентов: " + connectedClients + "/" + MAX_CLIENTS + ")");

                        ClientHandler clientHandler = new ClientHandler(clientSocket, logArea, this);
                        clientHandlers.add(clientHandler);
                        Thread handlerThread = new Thread(clientHandler);
                        handlerThread.start();

                    } catch (IOException e) {
                        if (running) {
                            appendLog("Ошибка accept: " + e.getMessage());
                        }
                    }
                }
            });
            serverThread.start();

        } catch (IOException e) {
            appendLog("Ошибка запуска сервера: " + e.getMessage());
        }
    }

    public void stopServer() {
        running = false;

        for (ClientHandler handler : clientHandlers) {
            handler.stopClientHandler();
        }
        clientHandlers.clear();
        connectedClients = 0;

        try {
            if (server != null) {
                server.close();
            }
            if (serverThread != null) {
                serverThread.interrupt();
            }
        } catch (IOException e) {
            appendLog("Ошибка закрытия сервера: " + e.getMessage());
        }
        appendLog("Сервер остановлен");
    }

    public boolean isRunning() {
        return running;
    }

    public void clientDisconnected(ClientHandler handler) {
        clientHandlers.remove(handler);
        connectedClients--;
        appendLog("Клиент отключился (клиентов: " + connectedClients + "/" + MAX_CLIENTS + ")");
    }

    private void appendLog(String message) {
        if (logArea != null) {
            logArea.append("[Сервер] " + message + "\n");
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private TextArea logArea;
        private boolean clientRunning = true;
        private ServerLogic serverLogic;

        public ClientHandler(Socket socket, TextArea logArea, ServerLogic serverLogic) {
            this.clientSocket = socket;
            this.logArea = logArea;
            this.serverLogic = serverLogic;
        }

        public void run() {
            try {
                clientSocket.setSoTimeout(1000);
                PrintStream ps = new PrintStream(clientSocket.getOutputStream(), true, "UTF-8");
                BufferedReader dis = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));

                appendLog("Обработчик клиента запущен");

                while (clientRunning && !Thread.currentThread().isInterrupted()) {
                    try {
                        String clientRequest = dis.readLine();
                        if (clientRequest == null) {
                            appendLog("Клиент отключился");
                            break;
                        }

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
                            ps.println(message);
                            ps.flush();

                            appendLog("Отправлено клиенту: " + message);
                        }

                    } catch (SocketTimeoutException e) {
                        continue;
                    } catch (IOException e) {
                        if (clientRunning) {
                            appendLog("Ошибка чтения запроса: " + e.getMessage());
                        }
                        break;
                    }
                }

            } catch (IOException e) {
                appendLog("Ошибка создания потоков: " + e.getMessage());
            } finally {
                closeClient();
            }
        }

        public void stopClientHandler() {
            clientRunning = false;
            closeClient();
        }

        private void closeClient() {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                serverLogic.clientDisconnected(this);
                appendLog("Соединение с клиентом закрыто");
            } catch (IOException e) {
                appendLog("Ошибка закрытия клиента: " + e.getMessage());
            }
        }

        private void appendLog(String message) {
            if (logArea != null) {
                logArea.append("[Обработчик] " + message + "\n");
            }
        }
    }
}
