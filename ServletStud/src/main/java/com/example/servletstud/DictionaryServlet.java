package com.example.servletstud;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DictionaryServlet extends HttpServlet {
    
    private Map<String, String> dictionary;
    
    @Override
    public void init() throws ServletException {
        super.init();
        initializeDictionary();
    }
    
    private void initializeDictionary() {
        dictionary = new HashMap<>();

        dictionary.put("привет", "hello");
        dictionary.put("мир", "world");
        dictionary.put("программа", "program");
        dictionary.put("кот", "cat");
        dictionary.put("собака", "dog");
        dictionary.put("дом", "house");
        dictionary.put("солнце", "sun");
        dictionary.put("вода", "water");
        dictionary.put("книга", "book");
        dictionary.put("стол", "table");

        dictionary.put("hello", "привет");
        dictionary.put("world", "мир");
        dictionary.put("program", "программа");
        dictionary.put("cat", "кот");
        dictionary.put("dog", "собака");
        dictionary.put("house", "дом");
        dictionary.put("sun", "солнце");
        dictionary.put("water", "вода");
        dictionary.put("book", "книга");
        dictionary.put("table", "стол");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        

        String clientParam = request.getParameter("client");
        if ("true".equals(clientParam)) {
            processClientTranslation(request, response);
        } else {
            processHtmlTranslation(request, response);
        }
    }
    
    private String getFromDataBase(String word) {
        String res = "";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/aiprp2", "root", "");
            String sql = "SELECT eng FROM Translations WHERE rus= ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, word);
            
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString("eng");
            }
            conn.close();
        } catch (Exception e) {
            System.out.println("DATABASE ERROR");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return res;
    }

    private void processClientTranslation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String word = request.getParameter("txt");
        String translation = translateWord(word);

        out.print(translation);
        out.close();
    }

    private void processHtmlTranslation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String rus_word = request.getParameter("txt");
        String translation = translateWord(rus_word);

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
        out.close();
    }

    private String translateWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return "Введите слово";
        }
        
        String cleanWord = word.trim().toLowerCase();
//        String translation = dictionary.get(cleanWord);
        String translation = getFromDataBase(cleanWord);
        
        if (translation == null) {
            return "Перевод не найден";
        }
        
        return translation;
    }
}