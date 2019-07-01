package com.company;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.temporal.ChronoField.*;

public class DataProcessing {
    public static List<Map<String, String>> fillMissingFromSource(List<Map<String, String>> source, List<Map<String, String>> destination) {
        List<String> commonKeySet = new ArrayList<>();
        for (String key : source.get(0).keySet()) {
            if (destination.get(0).keySet().contains(key)) {
                commonKeySet.add(key);
            }
        }

        if (commonKeySet.size() > 1) {
            for (Map<String, String> mappedValues : destination) {
                for (int i = 0; i < commonKeySet.size(); i++) {
                    if (mappedValues.get(commonKeySet.get(i)).equals("NULL")) {
                        mappedValues.replace(commonKeySet.get(i), getMissingValue(source,
                                                                                  commonKeySet.get(i),
                                                                                  commonKeySet.get((i+1) % commonKeySet.size()),
                                                                                  mappedValues.get(commonKeySet.get((i+1) % commonKeySet.size()))));
                    }
                }
            }
        }
        return destination;
    }

    public static void fillNamesAndNumbersWithDateCheck(List<Map<String, String>> source, List<Map<String, String>> destination) {
        for (Map<String, String> map : destination) {
            if (map.get("Name").equals("NULL") ^ map.get("Number").equals("NULL")) {
                String missingColumn;
                String presentColumn;
                String replacement = "NULL";
                if (map.get("Name").equals("NULL")) {
                    missingColumn = "Name";
                    presentColumn = "Number";
                } else {
                    missingColumn = "Number";
                    presentColumn = "Name";
                }
                LocalDate date = getDateTimeFromMap(map).toLocalDate();
                boolean unique = false;
                for (Map<String, String> sourceMap : source) {
                    if (map.get(presentColumn).equals(sourceMap.get(presentColumn))) {
                        if (date.isAfter(getDateFromMapByFieldName(sourceMap, "Beg date")) && date.isBefore(getDateFromMapByFieldName(sourceMap, "End date"))) {
                            if (unique) {
                                unique = false;
                                break;
                            } else {
                                unique = true;
                                replacement = sourceMap.get(missingColumn);
                            }
                        }
                    }
                }
                if (unique) {
                    map.replace(missingColumn, replacement);
                }
            }
        }
    }

    private static String getMissingValue(List<Map<String, String>> source, String missingKey, String presentKey, String presentValue) {
        for (Map<String, String> mappedValues : source) {
            if (mappedValues.get(presentKey).equals(presentValue)){
                return mappedValues.get(missingKey);
            }
        }
        return "NULL";
    }

    public static List<Map<String, String>> joinEvents(List<Map<String, String>> listOfMaps) {
        List<Map<String, String>> joinedList = new ArrayList<>();

        for (int i = 0; i < listOfMaps.size(); i++) {
            Map<String, String> currentElement = new LinkedHashMap<>(listOfMaps.get(i));

            if (i + 1 == listOfMaps.size()) {
                joinedList.add(listOfMaps.get(i));
            } else {
                while (isSingleEvent(currentElement, listOfMaps.get(i + 1))) {
                    for (String key : currentElement.keySet()) {
                        currentElement.merge(key, listOfMaps.get(i + 1).get(key), (oldVal, newVal) -> oldVal.isEmpty() ? newVal : oldVal);
                    }
                    i++;
                    if (i + 1 == listOfMaps.size()) {
                        break;
                    }
                }
                joinedList.add(currentElement);
            }
        }
        return joinedList;
    }

    private static boolean isSingleEvent(Map<String, String> firstReading, Map<String, String> secondReading) {
        //        (timeСheck + idCheck ) => join
        //        Zone/device-Check ? License-expiration Check?
        return timeCheck(firstReading, secondReading, 300) && idCheck(firstReading, secondReading);
    }

    private static boolean timeCheck(Map<String, String> firstReading, Map<String, String> secondReading, int eventDurationInSeconds) {
        return Math.abs(ChronoUnit.SECONDS.between(getDateTimeFromMap(firstReading), getDateTimeFromMap(secondReading)))  <= eventDurationInSeconds;
    }

    public static LocalDateTime getDateTimeFromMap(Map<String, String> map) {
        DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY, 1, 2, SignStyle.NEVER)
                .appendLiteral(":")
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(":")
                .appendValue(SECOND_OF_MINUTE, 2)
                .toFormatter();

        if (!map.get("Date").isEmpty() && !map.get("Time").isEmpty()) {
            return LocalDateTime.of(getDateFromMapByFieldName(map, "Date"), LocalTime.parse(map.get("Time"), timeFormatter));
        } else
            return LocalDateTime.of(getDateFromMapByFieldName(map, "Date_gen"), LocalTime.parse(map.get("Gen_time"), timeFormatter));
    }

    private static LocalDate getDateFromMapByFieldName(Map<String, String> map, String dateFieldName) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.parse(map.get(dateFieldName), dateFormatter);
    }

    private static boolean idCheck(Map<String, String> firstReading, Map<String, String> secondReading) {
        return (firstReading.get("Number").equals(secondReading.get("Number")) && firstReading.get("Name").equals(secondReading.get("Name")));
    }
}
