package com.example;

import java.awt.*;
import java.awt.event.*;

public class Main extends Frame {
    private Button serverButton;
    private Button clientButton;
    private static ServerWindow serverWindow;
    private static int clientCount = 0;
    private static final int MAX_CLIENTS = 5;

    public Main() {
        setTitle("Главное окно - Управление сервером и клиентами");
        setLayout(new FlowLayout());

        serverButton = new Button("Сервер");
        clientButton = new Button("Клиент");

        serverButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openServerWindow();
            }
        });

        clientButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openClientWindow();
            }
        });

        add(serverButton);
        add(clientButton);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

        setSize(300, 100);
        setVisible(true);
    }

    private void openServerWindow() {
        if (serverWindow == null) {
            serverWindow = new ServerWindow();
            serverWindow.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    serverWindow = null;
                }
            });
        }
    }

    private void openClientWindow() {
        if (clientCount < MAX_CLIENTS) {
            clientCount++;
            ClientWindow clientWindow = new ClientWindow(clientCount);
            clientWindow.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    clientCount--;
                }
            });
        } else {
            System.out.println("Достигнуто максимальное количество клиентов: " + MAX_CLIENTS);
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}