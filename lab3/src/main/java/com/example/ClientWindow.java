package com.example;

import java.awt.*;
import java.awt.event.*;

public class ClientWindow extends Frame {
    private Button startButton;
    private Button stopButton;
    private Button requestButton;
    private TextArea logArea;
    private ClientThread clientThread;
    private int clientId;

    public ClientWindow(int clientId) {
        this.clientId = clientId;
        setTitle("Окно клиента #" + clientId);
        setLayout(new BorderLayout());

        Panel buttonPanel = new Panel(new FlowLayout());

        startButton = new Button("Запустить клиент");
        stopButton = new Button("Остановить клиент");
        requestButton = new Button("Запрос к серверу");

        stopButton.setEnabled(false);
        requestButton.setEnabled(false);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startClient();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopClient();
            }
        });

        requestButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendRequest();
            }
        });

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(requestButton);

        logArea = new TextArea(15, 50);
        logArea.setEditable(false);

        add(buttonPanel, BorderLayout.NORTH);
        add(logArea, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                if (clientThread != null) {
                    stopClient();
                }
                dispose();
            }
        });

        setSize(500, 300);
        setVisible(true);
    }

    private void startClient() {
        if (clientThread == null || !clientThread.isAlive()) {
            clientThread = new ClientThread(logArea, clientId);
            clientThread.start();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            requestButton.setEnabled(true);
            logArea.append("Клиент #" + clientId + " запущен\n");
        }
    }

    private void stopClient() {
        if (clientThread != null) {
            clientThread.stopClient();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            requestButton.setEnabled(false);
            logArea.append("Клиент #" + clientId + " остановлен\n");
        }
    }

    private void sendRequest() {
        if (clientThread != null && clientThread.isAlive()) {
            clientThread.sendRequest();
            logArea.append("Клиент #" + clientId + " отправил запрос к серверу\n");
        } else {
            logArea.append("Ошибка: клиент не запущен\n");
        }
    }
}
