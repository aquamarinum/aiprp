package com.example;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class SimpleServer extends Frame {
    private Button startButton;
    private Button stopButton;
    private TextArea logArea;
    private ServerSocket server;
    private Socket clientSocket;
    private boolean running = false;
    private int amount = 200;

    public SimpleServer() {
        setTitle("Сервер");
        setLayout(new BorderLayout());

        Panel buttonPanel = new Panel(new FlowLayout());

        startButton = new Button("Запустить сервер");
        stopButton = new Button("Остановить сервер");
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        logArea = new TextArea(15, 50);
        logArea.setEditable(false);

        add(buttonPanel, BorderLayout.NORTH);
        add(logArea, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                stopServer();
                System.exit(0);
            }
        });

        setSize(500, 300);
        setVisible(true);
    }

    private void startServer() {
        if (running) return;

        try {
            server = new ServerSocket(3001);
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            appendLog("Сервер запущен на " + ipAddress + ":3001");
            appendLog("Ожидание подключения клиента...");

            running = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);

            new Thread(this::waitForClient).start();

        } catch (IOException e) {
            appendLog("Ошибка запуска сервера: " + e.getMessage());
        }
    }

    private void waitForClient() {
        try {
            clientSocket = server.accept();
            appendLog("Клиент подключен: " + clientSocket.getInetAddress());

            listenToClient();

        } catch (IOException e) {
            if (running) {
                appendLog("Ошибка при ожидании клиента: " + e.getMessage());
            }
        }
    }

    private void listenToClient() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);

            appendLog("Начало прослушивания сообщений от клиента...");

            String message;
            while (running && (message = reader.readLine()) != null) {
                if (message.equals("REQUEST")) {
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

                    String response = "Счет:" + amount + " (" + operation + " " + amountCur + ")";
                    writer.println(response);
                    appendLog("Отправлено клиенту: " + response);
                }
            }

        } catch (IOException e) {
            if (running) {
                appendLog("Клиент отключился: " + e.getMessage());
            }
        } finally {
            closeClientConnection();
        }
    }

    private void closeClientConnection() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            appendLog("Соединение с клиентом закрыто");
        } catch (IOException e) {
            appendLog("Ошибка закрытия соединения: " + e.getMessage());
        }
    }

    private void stopServer() {
        running = false;

        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            appendLog("Ошибка остановки сервера: " + e.getMessage());
        }

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        appendLog("Сервер остановлен");
    }

    private void appendLog(String message) {
        if (logArea != null) {
            logArea.append("[Сервер] " + message + "\n");
        }
    }

    public static void main(String[] args) {
        new SimpleServer();
    }
}
