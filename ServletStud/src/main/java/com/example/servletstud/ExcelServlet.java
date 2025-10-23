package com.example.servletstud;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

public class ExcelServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String filePath = getServletContext().getRealPath("/resources/dictionary.xlsx");
        File file = new File(filePath);
        
        if (!file.exists()) {
            createSampleExcelFile(file);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=dictionary.xlsx");
        response.setContentLength((int) file.length());

        try (FileInputStream fileInputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
    }

    private void createSampleExcelFile(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}