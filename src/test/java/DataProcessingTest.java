import com.company.CvsIoUtility;
import com.company.DataProcessing;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataProcessingTest {
    private static final String BIG_INPUT_FILE = "BigExample.csv";
    private static final String BIG_NAMES_DB = "Database.csv";
    private static final String INPUT_FILE = "testCase.csv";
    private static final String NAMES_DB = "testdb.csv";
    private static final String OUTPUT_FILE = INPUT_FILE.replace(".csv", "") + "Solved.csv";

    @Test
    void fillMissingFromSourceSimpleTest() {
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
    void fillMissingFromSourceSimpleTestWriteToFile() {
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

    @Test
    void eventJoinTwoLinesIntoOne() {
        List<Map<String, String>> testCase = Arrays.asList(
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Family", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:20");
                    put("Device1", "");
                    put("Device2", "123");
                }},
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Family", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:20:15");
                    put("Device1", "456");
                    put("Device2", "");
                }}
        );

        List<Map<String, String>> testCaseSolved = Arrays.asList(
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Family", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:20");
                    put("Device1", "456");
                    put("Device2", "123");
                }}
        );

        List<Map<String, String>> result = DataProcessing.joinEvents(testCase);
        assertEquals(testCaseSolved, result);
    }

    @Test
    void eventJoinThreeLinesIntoOne() {
        List<Map<String, String>> testCase = Arrays.asList(
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Family", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:20");
                    put("Device1", "");
                    put("Device2", "123");
                    put("Device3", "");
                }},
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Family", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:20:15");
                    put("Device1", "456");
                    put("Device2", "");
                    put("Device3", "");
                }},
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Family", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:21");
                    put("Device1", "456");
                    put("Device2", "");
                    put("Device3", "789");
                }}
        );
        List<Map<String, String>> testCaseSolved = Arrays.asList(
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Family", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:20");
                    put("Device1", "456");
                    put("Device2", "123");
                    put("Device3", "789");
                }}
        );
        assertEquals(testCaseSolved, DataProcessing.joinEvents(testCase));
    }

    @Test
    void eventJoinTestWithFile() {
        try {
            List<Map<String, String>> exampleData = CvsIoUtility.readCsvFile(new File(BIG_INPUT_FILE));
            List<Map<String, String>> result = DataProcessing.joinEvents(exampleData);
            CvsIoUtility.writeCsvFile(result, OUTPUT_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void timeParsingTest() {
        Map<String, String> fourDigitTime = new HashMap<String, String>() {{
            put("Date", "11.06.2018");
            put("Time", "10:20");
        }};
        assertEquals(LocalTime.of(10, 20), DataProcessing.getTimeFromMap(fourDigitTime).toLocalTime());

        Map<String, String> sixDigitTime = new HashMap<String, String>() {{
            put("Date", "11.06.2018");
            put("Time", "10:20:15");
        }};
        assertEquals(LocalTime.of(10, 20, 15), DataProcessing.getTimeFromMap(sixDigitTime).toLocalTime());

        Map<String, String> threeDigitTime = new HashMap<String, String>() {{
            put("Date", "11.06.2018");
            put("Time", "9:20");
        }};
        assertEquals(LocalTime.of(9, 20), DataProcessing.getTimeFromMap(threeDigitTime).toLocalTime());

        Map<String, String> fiveDigitTime = new HashMap<String, String>() {{
            put("Date", "11.06.2018");
            put("Time", "9:20:15");
        }};
        assertEquals(LocalTime.of(9, 20, 15), DataProcessing.getTimeFromMap(fiveDigitTime).toLocalTime());
    }

    @Test
    void dateTimeParsingTest() {
        Map<String, String> date = new HashMap<String, String>() {{
            put("Date", "11.06.2018");
            put("Time", "10:20");
        }};
        assertEquals(LocalDate.of(2018, 6, 11), DataProcessing.getTimeFromMap(date).toLocalDate());

        Map<String, String> dateTime = new HashMap<String, String>() {{
            put("Date", "11.06.2018");
            put("Time", "10:20:15");
        }};
        assertEquals(LocalDateTime.of(2018, 6, 11, 10, 20, 15), DataProcessing.getTimeFromMap(dateTime));
    }
}
