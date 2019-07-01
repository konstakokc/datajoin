import com.company.CvsIoUtility;
import com.company.DataProcessing;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DataProcessingTest {
    private static final String BIG_INPUT_FILE = "BigExample.csv";
    private static final String BIG_NAMES_DB = "Database.csv";
    private static final String INPUT_FILE = "testCase.csv";
    private static final String NAMES_DB = "testdb.csv";
    private static final String ZONES_DB = "Zones.csv";
    private static final String OUTPUT_FILE = INPUT_FILE.replace(".csv", "") + "Solved.csv";

    private static List<Map<String, String>> zones = Arrays.asList(
            new HashMap<String, String>() {{
                put("Zone", "1");
                put("Device", "a");
            }},
            new HashMap<String, String>() {{
                put("Zone", "1");
                put("Device", "b");
            }},
            new HashMap<String, String>() {{
                put("Zone", "1");
                put("Device", "c");
            }},
            new HashMap<String, String>() {{
                put("Zone", "2");
                put("Device", "d");
            }},
            new HashMap<String, String>() {{
                put("Zone", "2");
                put("Device", "e");
            }},
            new HashMap<String, String>() {{
                put("Zone", "3");
                put("Device", "f");
            }}
    );

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
                    put("Name", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:20");
                    put("Device1", "");
                    put("Device2", "123");
                    put("Zone", "1");
                    put("Device", "a");
                }},
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Name", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:20:15");
                    put("Device1", "456");
                    put("Device2", "");
                    put("Zone", "1");
                    put("Device", "b");
                }}
        );

        List<Map<String, String>> testCaseSolved = Arrays.asList(
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Name", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:20");
                    put("Device1", "456");
                    put("Device2", "123");
                    put("Zone", "1");
                    put("Device", "a");
                }}
        );

        List<Map<String, String>> result = DataProcessing.joinEvents(testCase, zones);
        assertEquals(testCaseSolved, result);
    }

    @Test
    void eventJoinThreeLinesIntoOne() {
        List<Map<String, String>> testCase = Arrays.asList(
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Name", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:20");
                    put("Device1", "");
                    put("Device2", "123");
                    put("Device3", "");
                    put("Zone", "1");
                    put("Device", "a");
                }},
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Name", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:20:15");
                    put("Device1", "456");
                    put("Device2", "");
                    put("Device3", "");
                    put("Zone", "1");
                    put("Device", "b");
                }},
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Name", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:21");
                    put("Device1", "456");
                    put("Device2", "");
                    put("Device3", "789");
                    put("Zone", "1");
                    put("Device", "c");
                }}
        );
        List<Map<String, String>> testCaseSolved = Arrays.asList(
                new HashMap<String, String>() {{
                    put("Number", "Т483АС");
                    put("Name", "vasya");
                    put("Surname", "petrov");
                    put("Date", "11.06.2018");
                    put("Time", "10:20");
                    put("Device1", "456");
                    put("Device2", "123");
                    put("Device3", "789");
                    put("Zone", "1");
                    put("Device", "a");
                }}
        );
        assertEquals(testCaseSolved, DataProcessing.joinEvents(testCase, zones));
    }

    @Test
    void eventJoinTestWithFile() {
        try {
            List<Map<String, String>> exampleData = CvsIoUtility.readCsvFile(new File(BIG_INPUT_FILE));
            List<Map<String, String>> zones = CvsIoUtility.readCsvFile(new File(ZONES_DB));
            List<Map<String, String>> result = DataProcessing.joinEvents(exampleData, zones);
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
        assertEquals(LocalTime.of(10, 20), DataProcessing.getDateTimeFromMap(fourDigitTime).toLocalTime());

        Map<String, String> sixDigitTime = new HashMap<String, String>() {{
            put("Date", "11.06.2018");
            put("Time", "10:20:15");
        }};
        assertEquals(LocalTime.of(10, 20, 15), DataProcessing.getDateTimeFromMap(sixDigitTime).toLocalTime());

        Map<String, String> threeDigitTime = new HashMap<String, String>() {{
            put("Date", "11.06.2018");
            put("Time", "9:20");
        }};
        assertEquals(LocalTime.of(9, 20), DataProcessing.getDateTimeFromMap(threeDigitTime).toLocalTime());

        Map<String, String> fiveDigitTime = new HashMap<String, String>() {{
            put("Date", "11.06.2018");
            put("Time", "9:20:15");
        }};
        assertEquals(LocalTime.of(9, 20, 15), DataProcessing.getDateTimeFromMap(fiveDigitTime).toLocalTime());
    }

    @Test
    void dateTimeParsingTest() {
        Map<String, String> date = new HashMap<String, String>() {{
            put("Date", "11.06.2018");
            put("Time", "10:20");
        }};
        assertEquals(LocalDate.of(2018, 6, 11), DataProcessing.getDateTimeFromMap(date).toLocalDate());

        Map<String, String> dateTime = new HashMap<String, String>() {{
            put("Date", "11.06.2018");
            put("Time", "10:20:15");
        }};
        assertEquals(LocalDateTime.of(2018, 6, 11, 10, 20, 15), DataProcessing.getDateTimeFromMap(dateTime));
    }
}
