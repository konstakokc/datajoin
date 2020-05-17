import com.company.CsvIoUtility;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.company.CsvIoUtility.writeCsvFile;

public class DataGeneration {
    public static void main(String[] args) {
        DataGeneration dataGeneration = new DataGeneration();

        if (dataGeneration.generateDatabase(100, "Database"))
            System.out.println("success on db");
        if (dataGeneration.generateEvents(1000, "Events"))
            System.out.println("success on events");
    }

    boolean generateDatabase(int quant, String filename) {
        List<Map<String, String>> databaseData = new ArrayList<>();
        for (int i = 0; i < quant; i++) {
            databaseData.add(new HashMap<String, String>() {{
                put("Number", numberGenerator());
                put("Name", nameGenerator());
                put("Beg date", "11.06.2018");
                put("End date", "04.07.2018");
            }});
        }
        try {
            writeCsvFile(databaseData, filename + quant + ".csv");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    String numberGenerator() {
        StringBuilder number = new StringBuilder();
        List<String> charList = new ArrayList<>(Arrays.asList("А", "В", "Е", "К", "М", "Н", "О", "Р", "С", "Т", "У", "Х"));
        number.append(getRandom(charList));
        number.append((int)(Math.random()*10));
        number.append((int)(Math.random()*10));
        number.append((int)(Math.random()*10));
        number.append(getRandom(charList));
        number.append(getRandom(charList));
        return number.toString();
    }

    String nameGenerator() {
        List<String> nameList = new ArrayList<>(Arrays.asList(
                "Смирнов", "Иванов", "Кузнецов", "Соколов", "Попов", "Лебедев", "Козлов", "Новиков", "Морозов", "Петров", "Волков", "Соловьёв", "Васильев",
                "Зайцев", "Павлов", "Семёнов", "Голубев", "Виноградов", "Богданов", "Воробьёв", "Фёдоров", "Михайлов", "Беляев", "Тарасов",
                "Белов", "Комаров", "Орлов", "Киселёв", "Макаров", "Андреев", "Ковалёв", "Ильин", "Гусев", "Титов", "Кузьмин"));
        return getRandom(nameList);
    }

    String zoneGenerator() {
        List<String> zoneList = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5", "6", "7"));
        return getRandom(zoneList);
    }

    String deviceGenerator() {
        List<String> deviceList = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p"));
        return getRandom(deviceList);
    }

    String parameterGenerator() {
        return deviceGenerator() + deviceGenerator() + zoneGenerator() + zoneGenerator();
    }

        public static String getRandom (List<String> e) {
        return e.stream()
                .skip((int) (e.size() * Math.random()))
                .findFirst().get();
    }

    boolean generateEvents(int quant, String filename) {
        List<Map<String, String>> eventsData = new ArrayList<>();
        try {
            List<Map<String, String>> sourceData = CsvIoUtility.readCsvFile(new File("Database100.csv"));
            for (int i = 0; i < quant-3; i+=3) {
                int randNum = (int) (Math.random()*sourceData.size());
                eventsData.add(new HashMap<String, String>() {{
                    put("Number", sourceData.get(randNum).get("Number"));
                    put("Name", sourceData.get(randNum).get("Name"));
                    put("Date", sourceData.get(randNum).get("Beg date"));
                    put("Time", LocalTime.now().truncatedTo(ChronoUnit.MINUTES).toString());
                    put("Date_gen", sourceData.get(randNum).get("Beg date"));
                    put("Gen_time", LocalTime.now().truncatedTo(ChronoUnit.MINUTES).toString());
                    put("Parameter 1", parameterGenerator());
                    put("Parameter 2", "");
                    put("Parameter 3", "");
                    put("Zone", "1"); //zoneGenerator());
                    put("Device", "a"); //deviceGenerator());
                    put("Violation", Math.random() >= 0.85 ? "Yes" : "No");
                }});
                eventsData.add(new HashMap<String, String>() {{
                    put("Number", sourceData.get(randNum).get("Number"));
                    put("Name", sourceData.get(randNum).get("Name"));
                    put("Date", sourceData.get(randNum).get("Beg date"));
                    put("Time", LocalTime.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES).toString());
                    put("Date_gen", sourceData.get(randNum).get("Beg date"));
                    put("Gen_time", LocalTime.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES).toString());
                    put("Parameter 1", "");
                    put("Parameter 2", parameterGenerator());
                    put("Parameter 3", "");
                    put("Zone", "1"); //zoneGenerator());
                    put("Device", "b"); //deviceGenerator());
                    put("Violation", Math.random() >= 0.85 ? "Yes" : "No");
                }});
                eventsData.add(new HashMap<String, String>() {{
                    put("Number", sourceData.get(randNum).get("Number"));
                    put("Name", sourceData.get(randNum).get("Name"));
                    put("Date", sourceData.get(randNum).get("Beg date"));
                    put("Time", LocalTime.now().plus(2, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES).toString());
                    put("Date_gen", sourceData.get(randNum).get("Beg date"));
                    put("Gen_time", LocalTime.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES).toString());
                    put("Parameter 1", "");
                    put("Parameter 2", "");
                    put("Parameter 3", parameterGenerator());
                    put("Zone", "1"); //zoneGenerator());
                    put("Device", "c"); //deviceGenerator());
                    put("Violation", Math.random() >= 0.85 ? "Yes" : "No");
                }});
            }

            writeCsvFile(eventsData, filename + quant + ".csv");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
