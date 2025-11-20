package com.example;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class SimpleClient extends Frame {
    private TextField connectionField;
    private Button connectButton;
    private Button disconnectButton;
    private Button requestButton;
    private TextArea logArea;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean connected = false;

    public SimpleClient() {
        setTitle("Клиент");
        setLayout(new BorderLayout());

        Panel connectionPanel = new Panel(new FlowLayout());
        connectionPanel.add(new Label("IP:порт:"));

        connectionField = new TextField("127.0.0.1:3001", 15);
        connectionPanel.add(connectionField);

        connectButton = new Button("Подключиться");
        disconnectButton = new Button("Отключиться");
        requestButton = new Button("Запрос к серверу");

        disconnectButton.setEnabled(false);
        requestButton.setEnabled(false);

        connectButton.addActionListener(e -> connectToServer());
        disconnectButton.addActionListener(e -> disconnectFromServer());
        requestButton.addActionListener(e -> sendRequest());

        connectionPanel.add(connectButton);
        connectionPanel.add(disconnectButton);
        connectionPanel.add(requestButton);

        logArea = new TextArea(15, 50);
        logArea.setEditable(false);

        add(connectionPanel, BorderLayout.NORTH);
        add(logArea, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                disconnectFromServer();
                System.exit(0);
            }
        });

        setSize(500, 300);
        setVisible(true);
    }

    private void connectToServer() {
        if (connected) return;

        String[] parts = connectionField.getText().split(":");
        if (parts.length != 2) {
            appendLog("Ошибка: неверный формат. Используйте IP:порт");
            return;
        }

        String ip = parts[0];
        int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            appendLog("Ошибка: неверный порт");
            return;
        }

        try {
            socket = new Socket(ip, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            connected = true;
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            requestButton.setEnabled(true);

            appendLog("Подключен к серверу " + ip + ":" + port);

            new Thread(this::listenToServer).start();

        } catch (IOException e) {
            appendLog("Ошибка подключения: " + e.getMessage());
        }
    }

    private void listenToServer() {
        try {
            String response;
            while (connected && (response = reader.readLine()) != null) {
                appendLog("Получено от сервера: " + response);
            }
        } catch (IOException e) {
            if (connected) {
                appendLog("Соединение с сервером разорвано: " + e.getMessage());
            }
        } finally {
            if (connected) {
                disconnectFromServer();
            }
        }
    }

    private void sendRequest() {
        if (connected && writer != null) {
            writer.println("REQUEST");
            appendLog("Запрос отправлен серверу");
        } else {
            appendLog("Ошибка: нет подключения к серверу");
        }
    }

    private void disconnectFromServer() {
        connected = false;

        try {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            appendLog("Ошибка отключения: " + e.getMessage());
        }

        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        requestButton.setEnabled(false);
        appendLog("Отключен от сервера");
    }

    private void appendLog(String message) {
        if (logArea != null) {
            logArea.append("[Клиент] " + message + "\n");
        }
    }

    public static void main(String[] args) {
        new SimpleClient();
    }
}
