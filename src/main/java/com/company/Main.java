package com.company;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        String INPUT_FILE = "BigExample.csv";
        String ZONES_DB = "Zones.csv";
        String NAMES_DB = "Database.csv";
        String OUTPUT_FILE = "testCSV.csv";

        try {
            List<Map<String, String>> readCsvFile = CvsIoUtility.readCsvFile(new File(INPUT_FILE));
//            System.out.println(readCsvFile.toString());
//            actual data joining
            CvsIoUtility.writeCsvFile(readCsvFile, OUTPUT_FILE);
//            System.out.println("Success");
        } catch (IOException e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }
    }
}
