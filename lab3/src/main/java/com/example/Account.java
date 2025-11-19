package com.example;

import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

class Account extends Thread {
    ServerSocket server;
    String amountstring;
    static int amount = 200;

    public void run() {
        try {
            server = new ServerSocket(3001);
            System.out.println("Сервер запущен на порту 3001");
        } catch (Exception e) {
            System.out.println("Ошибка соединения: " + e);
            return;
        }

        while (true) {
            Socket s = null;
            try {
                s = server.accept();
                System.out.println("Клиент подключен");
            } catch (Exception e) {
                System.out.println("Ошибка accept: " + e);
                continue;
            }

            try {
                PrintStream ps = new PrintStream(s.getOutputStream());
                int amountcur = ((int)(Math.random() * 1000));

                if (Math.random() > 0.5) {
                    amount -= amountcur;
                    System.out.println("Снятие: " + amountcur);
                } else {
                    amount += amountcur;
                    System.out.println("Пополнение: " + amountcur);
                }

                amountstring = Integer.toString(amount);
                ps.println("Account:" + amountstring);
                ps.flush();
                s.close();

                Thread.sleep(2000);

            } catch (Exception e) {
                System.out.println("Ошибка обработки: " + e);
            }
        }
    }
}