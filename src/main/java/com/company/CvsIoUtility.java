package com.company;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CvsIoUtility {
    public static List<Map<String, String>> readCsvFile(File file) throws IOException {
        List<Map<String, String>> listOfMappedValues = new LinkedList<>();
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(';');
        MappingIterator<Map<String, String>> iterator = mapper.readerFor(Map.class)
                .with(schema)
                .readValues(file);
        while (iterator.hasNext()) {
            listOfMappedValues.add(iterator.next());
        }
        return listOfMappedValues;
    }

    public static void writeCsvFile(List<Map<String, String>> listOfMappedValues, String fileName) throws IOException {
        CsvMapper mapper = new CsvMapper();
        PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));

        CsvSchema schema = new CsvSchema.Builder()
                .addColumns(listOfMappedValues.get(0).keySet(), CsvSchema.ColumnType.STRING)
                .build()
                .withColumnSeparator(';')
                .withoutQuoteChar()
                .withHeader();

        printWriter.print(mapper.writerFor(List.class).with(schema).writeValueAsString(listOfMappedValues));
        printWriter.close();
    }
}
