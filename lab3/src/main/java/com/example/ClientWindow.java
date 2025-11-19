package com.example;

import java.awt.*;
import java.awt.event.*;

public class ClientWindow extends Frame {
    private TextField connectionField;
    private Button connectButton;
    private Button disconnectButton;
    private Button requestButton;
    private TextArea logArea;
    private ClientLogic clientLogic;
    private int clientId;
    private static int clientCounter = 0;

    public ClientWindow() {
        this.clientId = ++clientCounter;
        setTitle("Клиент #" + clientId);
        setLayout(new BorderLayout());

        Panel connectionPanel = new Panel(new FlowLayout());
        connectionPanel.add(new Label("IP:порт:"));

        connectionField = new TextField("127.0.0.1:3002", 15);
        connectionPanel.add(connectionField);

        connectButton = new Button("Подключиться");
        disconnectButton = new Button("Отключиться");
        requestButton = new Button("Запрос к серверу");

        disconnectButton.setEnabled(false);
        requestButton.setEnabled(false);

        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });

        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disconnectFromServer();
            }
        });

        requestButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendRequest();
            }
        });

        connectionPanel.add(connectButton);
        connectionPanel.add(disconnectButton);
        connectionPanel.add(requestButton);

        logArea = new TextArea(15, 60);
        logArea.setEditable(false);

        add(connectionPanel, BorderLayout.NORTH);
        add(logArea, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                if (clientLogic != null) {
                    clientLogic.stopClient();
                }
                System.exit(0);
            }
        });

        setSize(600, 400);
        setVisible(true);
    }

    private void connectToServer() {
        if (clientLogic == null || !clientLogic.isRunning()) {
            String[] parts = connectionField.getText().split(":");
            if (parts.length != 2) {
                logArea.append("Ошибка: неверный формат. Используйте IP:порт\n");
                return;
            }

            String ip = parts[0];
            int port;
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                logArea.append("Ошибка: неверный порт\n");
                return;
            }

            clientLogic = new ClientLogic(logArea, clientId, ip, port);
            clientLogic.startClient();
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            requestButton.setEnabled(true);
        }
    }

    private void disconnectFromServer() {
        if (clientLogic != null) {
            clientLogic.stopClient();
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            requestButton.setEnabled(false);
        }
    }

    private void sendRequest() {
        if (clientLogic != null && clientLogic.isRunning()) {
            clientLogic.sendRequest();
        } else {
            logArea.append("Ошибка: клиент не подключен\n");
        }
    }

    public static void main(String[] args) {
        new ClientWindow();
    }
}
