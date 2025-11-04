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
    private Process musicProcess;

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
        frame.setSize(450, 250);
        frame.setLayout(new BorderLayout());

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                stopMusic();
            }
        });
        
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
        JButton presentationButton = new JButton("Открыть презентацию");
        JButton musicButton = new JButton("Включить музыку");
        JButton stopMusicButton = new JButton("Выключить музыку");
        
        buttonPanel.add(translateButton);
        buttonPanel.add(excelButton);
        buttonPanel.add(webButton);
        buttonPanel.add(presentationButton);
        buttonPanel.add(musicButton);
        buttonPanel.add(stopMusicButton);
        
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

        presentationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openPresentation();
            }
        });

        musicButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                playMusic();
            }
        });

        stopMusicButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopMusic();
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

    private void openPresentation() {
        try {
            java.net.URL presentationUrl = getClass().getClassLoader().getResource("prez.pptx");
            if (presentationUrl == null) {
                JOptionPane.showMessageDialog(frame,
                        "Файл презентации не найден в ресурсах: prez.pptx");
                return;
            }

            String presentationPath = new java.io.File(presentationUrl.toURI()).getAbsolutePath();

            ProcessBuilder pb = new ProcessBuilder(
                    "cmd.exe", "/c", "start", "\"\"", "powerpnt", "/s", "\"" + presentationPath + "\""
            );
            Process process = pb.start();


        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Ошибка при открытии презентации: " + ex.getMessage() +
                    "\nУбедитесь, что PowerPoint установлен на вашем компьютере.");
        }
    }

    private void playMusic() {
        try {
            stopMusic();

            java.net.URL musicUrl = getClass().getClassLoader().getResource("sun.mp3");
            if (musicUrl == null) {
                System.out.println("Файл музыки не найден в ресурсах: sun.mp3");
                return;
            }

            String musicPath = new java.io.File(musicUrl.toURI()).getAbsolutePath();

            ProcessBuilder pb = new ProcessBuilder(
                    "cmd.exe", "/c", "start", "\"\"", "\"" + musicPath + "\""
            );
            musicProcess = pb.start();

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Ошибка при воспроизведении музыки: " + ex.getMessage() +
                    "\nУбедитесь, что у вас есть программа для воспроизведения аудио.");
        }
    }

    private void stopMusic() {
        if (musicProcess != null && musicProcess.isAlive()) {
            try {
                musicProcess.destroy();
                musicProcess = null;
                System.out.println("Музыка остановлена");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}