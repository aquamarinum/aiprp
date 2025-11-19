package com.example;

import java.awt.*;
import java.io.*;
import java.net.*;

public class Serv extends Frame {

    public boolean handleEvent(Event evt) {
        if (evt.id == Event.WINDOW_DESTROY) {
            System.exit(0);
        }
        return super.handleEvent(evt);
    }

    public boolean mouseDown(Event evt, int x, int y) {
        new ClientThread().start();
        return true;
    }

    public static void main(String args[]) {
        Serv f = new Serv();
        f.setSize(400, 400);
        f.setVisible(true);
        new Account().start();
    }
}