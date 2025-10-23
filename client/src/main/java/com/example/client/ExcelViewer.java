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
        DefaultTableModel model = new DefaultTableModel();
        
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                model.addColumn(getCellValue(headerRow.getCell(i)));
            }
        }
        
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Object[] rowData = new Object[row.getLastCellNum()];
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    rowData[j] = getCellValue(row.getCell(j));
                }
                model.addRow(rowData);
            }
        }
        
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        
        table.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()));
        
        return table;
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
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
}