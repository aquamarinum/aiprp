package com.example.servletstud;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class DictionaryServlet extends HttpServlet {
    
    private static final String URL = "jdbc:mysql://localhost:3307/dictionary_db?useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        
        PrintWriter out = response.getWriter();
        
        String rus_word = request.getParameter("txt");
        String translation = "";

        if (rus_word != null && !rus_word.trim().isEmpty()) {
            translation = translateWord(rus_word.trim());
        }

        try {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet DictionaryServlet</title>");
            out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
            out.println("</head>");
            out.println("<body bgcolor='#aaccff'>");
            out.println("<form>");
            out.println("<h2> Результат перевода:</h2><br><br>");
            out.println("<Font color='blue' size='6'>Русское слово: " + rus_word + "</Font><br>");
            out.println("<Font color='blue' size='6'>Перевод: " + translation + "</Font><br><br>");
            out.println("<a href='dictionary.html'>Вернуться к словарю</a>");
            out.println("</form>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        
        PrintWriter out = response.getWriter();
        
        String russianWord = request.getParameter("russianWord");
        String englishWord = request.getParameter("englishWord");
        String message = "";

        if (russianWord != null && englishWord != null && 
            !russianWord.trim().isEmpty() && !englishWord.trim().isEmpty()) {
            
            if (addWordToDictionary(russianWord.trim(), englishWord.trim())) {
                message = "Слово успешно добавлено в словарь!";
            } else {
                message = "Ошибка при добавлении слова в словарь";
            }
        } else {
            message = "Оба поля должны быть заполнены!";
        }

        try {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet DictionaryServlet</title>");
            out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
            out.println("</head>");
            out.println("<body bgcolor='#aaccff'>");
            out.println("<form>");
            out.println("<h2> " + message + "</h2><br><br>");
            out.println("<a href='dictionary.html'>Вернуться к словарю</a>");
            out.println("</form>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }
    }

    private String translateWord(String word) {
        String translation = "Перевод не найден";
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            return "Ошибка: драйвер БД не найден";
        }
        
        String sql = "SELECT english_word FROM translations WHERE russian_word = ?";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, word);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                translation = rs.getString("english_word");
            } else {
                sql = "SELECT russian_word FROM translations WHERE english_word = ?";
                try (PreparedStatement stmtReverse = conn.prepareStatement(sql)) {
                    stmtReverse.setString(1, word);
                    ResultSet rsReverse = stmtReverse.executeQuery();
                    if (rsReverse.next()) {
                        translation = rsReverse.getString("russian_word");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка базы данных: " + e.getMessage();
        }
        
        return translation;
    }

    private boolean addWordToDictionary(String russian, String english) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO translations (russian_word, english_word) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE english_word = VALUES(english_word)")) {
                
                stmt.setString(1, russian);
                stmt.setString(2, english);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
                
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}