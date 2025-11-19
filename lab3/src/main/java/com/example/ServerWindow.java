package com.example;

import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;

public class ServerWindow extends Frame {
    private Button startButton;
    private Button stopButton;
    private TextArea logArea;
    private ServerLogic serverLogic;

    public ServerWindow() {
        setTitle("Сервер");
        setLayout(new BorderLayout());

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

        logArea = new TextArea(20, 60);
        logArea.setEditable(false);

        add(buttonPanel, BorderLayout.NORTH);
        add(logArea, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                if (serverLogic != null) {
                    serverLogic.stopServer();
                }
                System.exit(0);
            }
        });

        setSize(600, 400);
        setVisible(true);
    }

    private void startServer() {
        if (serverLogic == null || !serverLogic.isRunning()) {
            serverLogic = new ServerLogic(logArea);
            serverLogic.startServer();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    }

    private void stopServer() {
        if (serverLogic != null) {
            serverLogic.stopServer();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    public static void main(String[] args) {
        new ServerWindow();
    }
}
