package com.example;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ServerThread extends Thread {
    private ServerSocket server;
    private static int amount = 200;
    private TextArea logArea;
    private boolean running = true;
    private ServerWindow serverWindow;
    private List<ClientHandler> clientHandlers;

    public ServerThread(TextArea logArea, ServerWindow serverWindow) {
        this.logArea = logArea;
        this.serverWindow = serverWindow;
        this.clientHandlers = new ArrayList<>();
    }

    public void run() {
        try {
            server = new ServerSocket(3001);
            appendLog("Сервер запущен. Начальный счет: " + amount);
        } catch (IOException e) {
            appendLog("Ошибка запуска сервера: " + e.getMessage());
            return;
        }

        while (running) {
            try {
                Socket clientSocket = server.accept();
                appendLog("Клиент подключен: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, logArea);
                clientHandlers.add(clientHandler);
                if (serverWindow != null) {
                    serverWindow.addClientHandler(clientHandler);
                }
                clientHandler.start();

            } catch (IOException e) {
                if (running) {
                    appendLog("Ошибка accept: " + e.getMessage());
                }
            }
        }
    }

    public void stopServer() {
        running = false;

        for (ClientHandler handler : new ArrayList<>(clientHandlers)) {
            handler.stopClientHandler();
        }
        clientHandlers.clear();

        if (serverWindow != null) {
            serverWindow.closeAllClientHandlers();
        }

        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            appendLog("Ошибка закрытия сервера: " + e.getMessage());
        }
    }

    private void appendLog(String message) {
        if (logArea != null) {
            logArea.append("[Сервер] " + message + "\n");
        }
    }

    public class ClientHandler extends Thread {
        private Socket clientSocket;
        private TextArea logArea;
        private boolean clientRunning = true;

        public ClientHandler(Socket socket, TextArea logArea) {
            this.clientSocket = socket;
            this.logArea = logArea;
        }

        public void run() {
            try {
                PrintStream ps = new PrintStream(clientSocket.getOutputStream(), true, "UTF-8");
                BufferedReader dis = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));

                appendLog("Обработчик клиента запущен");

                while (clientRunning) {
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
                    appendLog("Соединение с клиентом закрыто");
                }
                clientHandlers.remove(this);
                if (serverWindow != null) {
                    serverWindow.removeClientHandler(this);
                }
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
