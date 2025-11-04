package com.example.client;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ExcelViewer {

    public static void showExcelInFrame(byte[] excelData, String title) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelData);
             Workbook workbook = new XSSFWorkbook(bis)) {
            
            JDialog dialog = new JDialog();
            dialog.setTitle(title);
            dialog.setModal(true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setLayout(new BorderLayout());
            
            JTabbedPane tabbedPane = new JTabbedPane();
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                JTable table = createTableFromSheet(sheet);
                JScrollPane scrollPane = new JScrollPane(table);
                tabbedPane.addTab(sheet.getSheetName(), scrollPane);
            }
            
            JButton closeButton = new JButton("Закрыть");
            closeButton.addActionListener(e -> dialog.dispose());
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            
            dialog.add(tabbedPane, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Ошибка при открытии Excel файла: " + e.getMessage(),
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static JTable createTableFromSheet(Sheet sheet) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        
        int columnCount = 0;
        Row headerRow = sheet.getRow(0);
        
        if (headerRow != null) {
            columnCount = headerRow.getLastCellNum();
            for (int i = 0; i < columnCount; i++) {
                Cell cell = headerRow.getCell(i);
                String header = (cell != null) ? getCellValue(cell) : "Столбец " + (i + 1);
                model.addColumn(header);
            }
        } else {
            columnCount = 10;
            for (int i = 0; i < columnCount; i++) {
                model.addColumn("Столбец " + (i + 1));
            }
        }
        
        int startRow = (headerRow != null) ? 1 : 0;
        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Object[] rowData = new Object[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    Cell cell = row.getCell(j);
                    rowData[j] = (cell != null) ? getCellValue(cell) : "";
                }
                model.addRow(rowData);
            } else {
                Object[] emptyRow = new Object[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    emptyRow[j] = "";
                }
                model.addRow(emptyRow);
            }
        }
        
        if (model.getRowCount() == 0) {
            for (int i = 0; i < 20; i++) {
                Object[] emptyRow = new Object[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    emptyRow[j] = "";
                }
                model.addRow(emptyRow);
            }
        }
        
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        
        table.setDefaultEditor(Object.class, new FormulaCellEditor());
        
        return table;
    }
    
    static class FormulaCellEditor extends DefaultCellEditor {
        public FormulaCellEditor() {
            super(new JTextField());
        }
        
        @Override
        public Object getCellEditorValue() {
            String value = (String) super.getCellEditorValue();
            
            if (value != null && value.startsWith("=СУММ(") && value.endsWith(")")) {
                try {
                    String content = value.substring(6, value.length() - 1);
                    String[] numbers = content.split(";");
                    
                    double sum = 0;
                    for (String num : numbers) {
                        sum += Double.parseDouble(num.trim());
                    }
                    
                    return sum;
                    
                } catch (Exception e) {
                    return "Ошибка";
                }
            }
            
            return value;
        }
    }
    
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        return String.valueOf((int) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return "=" + cell.getCellFormula();
            default:
                return "";
        }
    }
}

/*Расширь функционал клиента. Добавив кнопку, по нажатию на которую будет открываться презентация  в режиме выполнения сразу (я ее заготовил уже и поместил в папку)*/