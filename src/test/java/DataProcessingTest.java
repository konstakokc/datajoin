import com.company.CvsIoUtility;
import com.company.DataProcessing;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataProcessingTest {
//    private static final String INPUT_FILE = "BigExample.csv";
//    private static final String NAMES_DB = "Database.csv";
    private static final String INPUT_FILE = "testCase.csv";
    private static final String NAMES_DB = "testdb.csv";
    private static final String OUTPUT_FILE = INPUT_FILE.replace(".csv", "") + "Solved.csv";

    @Test
    public void fillMissingFromSourceSimpleTest() {
        List<Map<String, String>> name_db = Arrays.asList(
                new HashMap<String, String>() {{
                    put("Name", "ivan");
                    put("Surname", "pupkin");
                }},
                new HashMap<String, String>() {{
                    put("Name", "vasya");
                    put("Surname", "kekin");
                }},
                new HashMap<String, String>() {{
                    put("Name", "petr");
                    put("Surname", "petrov");
                }}
        );

        List<Map<String, String>> testCase = Arrays.asList(
                new HashMap<String, String>() {{
                    put("Name", "vasya");
                    put("Surname", "NULL");
                    put("Nonsense", "123");
                }},
                new HashMap<String, String>() {{
                    put("Name", "NULL");
                    put("Surname", "petrov");
                    put("Nonsense", "456");
                }}
         );

        List<Map<String, String>> testCaseSolved = Arrays.asList(
                new HashMap<String, String>() {{
                    put("Name", "vasya");
                    put("Surname", "kekin");
                    put("Nonsense", "123");
                }},
                new HashMap<String, String>() {{
                    put("Name", "petr");
                    put("Surname", "petrov");
                    put("Nonsense", "456");
                }}
        );

        testCase = DataProcessing.fillMissingFromSource(name_db, testCase);
        assertEquals(testCase, testCaseSolved);
    }

    @Test
    public void fillMissingFromSourceSimpleTestWriteToFile() {
        try {
            List<Map<String, String>> exampleData = CvsIoUtility.readCsvFile(new File(INPUT_FILE));
            List<Map<String, String>> sourceData = CvsIoUtility.readCsvFile(new File(NAMES_DB));
            exampleData = DataProcessing.fillMissingFromSource(sourceData, exampleData);
            CvsIoUtility.writeCsvFile(exampleData, OUTPUT_FILE);
            System.out.println("Success");
        } catch (IOException e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }
    }
}
