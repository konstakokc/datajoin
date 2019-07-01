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
        String OUTPUT_FILE = "Result.csv";

        try {
            List<Map<String, String>> exampleData = CvsIoUtility.readCsvFile(new File(INPUT_FILE));
            List<Map<String, String>> sourceData = CvsIoUtility.readCsvFile(new File(NAMES_DB));
            List<Map<String, String>> zones = CvsIoUtility.readCsvFile(new File(ZONES_DB));

            DataProcessing.fillNamesAndNumbersWithDateCheck(sourceData, exampleData);
            exampleData = DataProcessing.joinEvents(exampleData, zones);

            CvsIoUtility.writeCsvFile(exampleData, OUTPUT_FILE);
        } catch (IOException e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }
    }
}
