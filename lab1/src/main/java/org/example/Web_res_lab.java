package org.example;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Web_res_lab extends Frame implements ActionListener{

    Button exitButton = new Button("Exit");
    Button searchButton = new Button("Search");
    TextArea textArea = new TextArea();

    public Web_res_lab() {
        super("My Window");
        setLayout(null);
        setBackground(new Color(150, 200, 100));
        setSize(450, 250);

        exitButton.setBounds(110, 190, 100, 20);
        exitButton.addActionListener(this);
        add(exitButton);

        searchButton.setBounds(110, 165, 100, 20);
        searchButton.addActionListener(this);
        add(searchButton);

        textArea.setBounds(20, 50, 300, 100);
        add(textArea);

        setLocationRelativeTo(null);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == exitButton) {
            System.exit(0);
        } else if (ae.getSource() == searchButton) {
            String[] keywords = textArea.getText().split(",");
            ArrayList<File> files = new ArrayList<>(Arrays.asList(new File("c:/code/java/aiprp/lab1/files").listFiles()));
            textArea.setText("");
            for (File file : files) {
                int coincidenceCount = testUrl(file, keywords);
                textArea.append("\n" + file + "  :" + coincidenceCount);
            }
        }
    }

    private int testUrl(File file, String[] keywords) {
        int results = 0;
        try {
            URL url = file.toURI().toURL();
            URLConnection connection = url.openConnection();
            StringBuilder htmlContent = new StringBuilder();

            try (BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
                 BufferedReader reader = new BufferedReader(new InputStreamReader(bis))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    htmlContent.append(line).append("\n");
                }
            }
            String content = htmlContent.toString().toLowerCase(); // file content in string

            for (String keyword : keywords) {
                if (content.contains(keyword.trim().toLowerCase())) {
                    results++;
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return -1;
        }
        return results;
    }

    public static void main(String[] args) {
        new Web_res_lab();
    }
}

