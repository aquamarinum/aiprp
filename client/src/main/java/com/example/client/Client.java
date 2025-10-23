package com.example.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Client {

    private JFrame frame;
    private JTextField wordField;
    private JTextField translationField;
    private static final String SERVER_URL = "http://localhost:35886/ServletStud";

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Client window = new Client();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Client() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Переводчик");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout());
        
        // Основная панель с полями ввода
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Метки и поля
        JLabel wordLabel = new JLabel("Слово:");
        wordField = new JTextField();
        
        JLabel translationLabel = new JLabel("Перевод:");
        translationField = new JTextField();
        translationField.setEditable(false);
        
        mainPanel.add(wordLabel);
        mainPanel.add(wordField);
        mainPanel.add(translationLabel);
        mainPanel.add(translationField);
        
        // Панель с кнопками
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        
        JButton translateButton = new JButton("Перевести");
        JButton excelButton = new JButton("Открыть Excel");
        JButton webButton = new JButton("Веб-версия");
        
        buttonPanel.add(translateButton);
        buttonPanel.add(excelButton);
        buttonPanel.add(webButton);
        
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        frame.setLocationRelativeTo(null);
        
        // Обработчики событий
        translateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                translateWord();
            }
        });
        
        excelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadAndOpenExcel();
            }
        });
        
        webButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openWebInterface();
            }
        });
        
        wordField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                translateWord();
            }
        });
    }

    private void translateWord() {
        String word = wordField.getText().trim();
        if (word.isEmpty()) {
            translationField.setText("Введите слово для перевода");
            return;
        }

        // Запрашиваем перевод с сервера с флагом client=true
        try {
            String url = SERVER_URL + "/DictionaryServlet?client=true&txt=" + 
                        java.net.URLEncoder.encode(word, "UTF-8");

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                HttpResponse response = httpClient.execute(request);
                
                // Получаем просто перевод (без HTML)
                String translation = EntityUtils.toString(response.getEntity(), "UTF-8");
                translationField.setText(translation);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            translationField.setText("Ошибка подключения к серверу");
        }
    }

    private void downloadAndOpenExcel() {
        String url = SERVER_URL + "/ExcelServlet";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);
            
            byte[] excelData = EntityUtils.toByteArray(response.getEntity());
            ExcelViewer.showExcelInFrame(excelData, "Словарь Excel");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, 
                "Ошибка при загрузке Excel: " + ex.getMessage());
        }
    }

    private void openWebInterface() {
        try {
            Desktop.getDesktop().browse(new URI(SERVER_URL));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, 
                "Ошибка при открытии веб-страницы: " + ex.getMessage());
        }
    }
}