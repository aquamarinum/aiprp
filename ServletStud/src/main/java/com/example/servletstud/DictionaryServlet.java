package com.example.servletstud;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
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
        
        // Русско-английский словарь
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
        
        // Англо-русский словарь
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
        
        // Проверяем, запрашивается ли перевод для клиента
        String clientParam = request.getParameter("client");
        if ("true".equals(clientParam)) {
            // Возвращаем только перевод для клиентского приложения
            processClientTranslation(request, response);
        } else {
            // Возвращаем HTML страницу для браузера
            processHtmlTranslation(request, response);
        }
    }

    private void processClientTranslation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String word = request.getParameter("txt");
        String translation = translateWord(word);
        
        // Возвращаем просто перевод (без HTML)
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
        String translation = dictionary.get(cleanWord);
        
        if (translation == null) {
            return "Перевод не найден";
        }
        
        return translation;
    }
}