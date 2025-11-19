package com.example;

import java.io.DataInputStream;
import java.net.Socket;

class ClientThread extends Thread {
    DataInputStream dis = null;
    Socket s = null;

    public ClientThread() {
        try {
            s = new Socket("127.0.0.1", 3001);
            dis = new DataInputStream(s.getInputStream());
            System.out.println("Клиент создан и подключен к серверу");
        } catch (Exception e) {
            System.out.println("Ошибка создания клиента: " + e);
        }
    }

    public void run() {
        System.out.println("Клиент начал работу");

        while (true) {
            try {
                sleep(100);
            } catch (Exception er) {
                System.out.println("Ошибка sleep: " + er);
            }

            try {
                String msg = dis.readLine();
                if (msg == null) {
                    System.out.println("Соединение разорвано");
                    break;
                }
                System.out.println("Получено от сервера: " + msg);
            } catch (Exception e) {
                System.out.println("Ошибка чтения: " + e);
                break;
            }
        }

        try {
            if (dis != null) dis.close();
            if (s != null) s.close();
            System.out.println("Клиент завершил работу");
        } catch (Exception e) {
            System.out.println("Ошибка закрытия: " + e);
        }
    }
}