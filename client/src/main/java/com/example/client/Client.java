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
        frame = new JFrame("Клиент словаря");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLayout(new FlowLayout());
        
        JButton openWebButton = new JButton("Открыть веб-интерфейс");
        
        JButton downloadExcelButton = new JButton("Загрузить Excel");
        
        frame.add(openWebButton);
        frame.add(downloadExcelButton);
        
        frame.setLocationRelativeTo(null);
        
        openWebButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openWebInterface();
            }
        });
        
        downloadExcelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadAndOpenExcel();
            }
        });
    }

    private void openWebInterface() {
        try {
            String url = "http://localhost:35886/ServletStud";
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, 
                "Ошибка при открытии веб-страницы: " + ex.getMessage());
        }
    }

    private void downloadAndOpenExcel() {
        String url = "http://localhost:35886/ServletStud/ExcelServlet";

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
}