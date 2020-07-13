package com.company.desktop;

import com.company.CsvIoUtility;
import com.company.DataProcessing;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class App {
    private JButton resetButton;
    private JPanel mainPanel;
    private JButton inputFileButton;
    private JButton idFileButton;
    private JButton zonesFileButton;
    private JTable inputTable;
    private JButton joinButton;
    private JTable outputTable;
    private JButton saveButton;
    private JLabel inputLabel;
    private JLabel idLabel;
    private JLabel zonesLabel;
    private JButton prepareButton;
    private JRadioButton inputRadioButton;
    private JRadioButton idsRadioButton;
    private JRadioButton zonesRadioButton;
    private JLabel inputRowsLabel;
    private JLabel outputRowsLabel;
    private JLabel timerLabel;

    File inputData;
    File ids;
    File zones;
    List<Map<String, String>> data;
    JFileChooser fileChooser;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public App() {

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Comma separated values", "csv"));
        fileChooser.setCurrentDirectory(new File("C:\\Users\\lpgam\\IdeaProjects\\Datajoin Desktop"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        inputFileButton.addActionListener(e -> {
            fileChooser.setDialogTitle("Select input file");
            int result = fileChooser.showOpenDialog(mainPanel);
            if (result == JFileChooser.APPROVE_OPTION ) {
                inputLabel.setText(fileChooser.getSelectedFile().getName());
                inputRadioButton.setSelected(true);
                inputData = fileChooser.getSelectedFile();
                try {
                    data = CsvIoUtility.readCsvFile(inputData);
                    Object[] columns = data.get(0).keySet().toArray();
                    DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
                    for (Map<String, String> stringStringMap : data) {
                        tableModel.addRow(stringStringMap.values().toArray());
                    }
                    inputTable.setModel(tableModel);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                inputRowsLabel.setText("Total rows: " + inputTable.getRowCount());
                inputRowsLabel.setVisible(true);
                if (ids != null) {
                    prepareButton.setEnabled(true);
                }
            }
        });

        idFileButton.addActionListener(e -> {
            fileChooser.setDialogTitle("Select Id's file");
            int result = fileChooser.showOpenDialog(mainPanel);
            if (result == JFileChooser.APPROVE_OPTION ) {
                idLabel.setText(fileChooser.getSelectedFile().getName());
                idsRadioButton.setSelected(true);
                ids = fileChooser.getSelectedFile();
                if (inputData != null) {
                    prepareButton.setEnabled(true);
                }
            }
        });

        zonesFileButton.addActionListener(e -> {
            fileChooser.setDialogTitle("Select Zones file");
            int result = fileChooser.showOpenDialog(mainPanel);
            if (result == JFileChooser.APPROVE_OPTION ) {
                zonesLabel.setText(fileChooser.getSelectedFile().getName());
                zonesRadioButton.setSelected(true);
                zones = fileChooser.getSelectedFile();
            }
        });

        prepareButton.addActionListener(e -> {
            prepareData();
            if (zones != null) {
                joinButton.setEnabled(true);
                joinButton.setBackground(Color.green);
            }
        });

        joinButton.addActionListener(e -> {
            long startTime = System.currentTimeMillis();
            joinData();
            long endTime = System.currentTimeMillis();
            timerLabel.setText("Elapsed time: " + (endTime - startTime) + " ms" );
            Object[] columns = data.get(0).keySet().toArray();
            DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
            for (Map<String, String> stringStringMap : data) {
                tableModel.addRow(stringStringMap.values().toArray());
            }
            outputTable.setModel(tableModel);
            outputRowsLabel.setText("Total rows: " + outputTable.getRowCount());
            outputRowsLabel.setVisible(true);
            saveButton.setEnabled(true);
        });

        saveButton.addActionListener(e -> {
            if (writeData()) {
                JOptionPane.showMessageDialog(null, "Result saved");
            }
        });

        resetButton.addActionListener(e -> {
            inputTable.setModel(new DefaultTableModel());
            outputTable.setModel(new DefaultTableModel());
            data = null;
            inputRowsLabel.setText("");
            outputRowsLabel.setText("");
            zones = null;
            ids = null;
            inputData = null;
            inputLabel.setText("");
            idLabel.setText("");
            zonesLabel.setText("");
            timerLabel.setText("");
            joinButton.setBackground(null);
            idsRadioButton.setSelected(false);
            inputRadioButton.setSelected(false);
            zonesRadioButton.setSelected(false);
            joinButton.setEnabled(false);
            prepareButton.setEnabled(false);
            saveButton.setEnabled(false);
        });
    }

    boolean prepareData() {
        try {
            List<Map<String, String>> sourceData = CsvIoUtility.readCsvFile(ids);
            DataProcessing.fillNamesAndNumbersWithDateCheck(sourceData, data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean joinData() {
        try {
            List<Map<String, String>> zones = CsvIoUtility.readCsvFile(this.zones);
            data = DataProcessing.joinEvents(data, zones);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } return false;
    }

    boolean writeData() {
        try {
            CsvIoUtility.writeCsvFile(data, "Result of " + inputData.getName());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } return false;
    }
}
