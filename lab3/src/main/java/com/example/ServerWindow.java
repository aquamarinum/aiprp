package com.example;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ServerWindow extends Frame {
    private Button startButton;
    private Button stopButton;
    private TextArea logArea;
    private ServerThread serverThread;
    private java.util.List<ServerThread.ClientHandler> clientHandlers;

    public ServerWindow() {
        setTitle("Окно сервера");
        setLayout(new BorderLayout());

        clientHandlers = new ArrayList<>();

        Panel buttonPanel = new Panel(new FlowLayout());

        startButton = new Button("Запустить сервер");
        stopButton = new Button("Остановить сервер");
        stopButton.setEnabled(false);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        logArea = new TextArea(15, 50);
        logArea.setEditable(false);

        add(buttonPanel, BorderLayout.NORTH);
        add(logArea, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                if (serverThread != null) {
                    stopServer();
                }
                dispose();
            }
        });

        setSize(500, 300);
        setVisible(true);
    }

    private void startServer() {
        if (serverThread == null || !serverThread.isAlive()) {
            serverThread = new ServerThread(logArea, this);
            serverThread.start();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            logArea.append("Сервер запущен на порту 3001\n");
        }
    }

    private void stopServer() {
        if (serverThread != null) {
            serverThread.stopServer();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            logArea.append("Сервер остановлен\n");
        }
    }

    public void addClientHandler(ServerThread.ClientHandler handler) {
        clientHandlers.add(handler);
    }

    public void removeClientHandler(ServerThread.ClientHandler handler) {
        clientHandlers.remove(handler);
    }

    public void closeAllClientHandlers() {
        for (ServerThread.ClientHandler handler : new ArrayList<>(clientHandlers)) {
            handler.stopClientHandler();
        }
        clientHandlers.clear();
    }
}
