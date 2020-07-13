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

    public static List<Map<String, String>> joinEvents(List<Map<String, String>> listOfMaps, List<Map<String, String>> zones) {
        List<Map<String, String>> joinedList = new ArrayList<>();

        for (int i = 0; i < listOfMaps.size(); i++) {
            Map<String, String> currentElement = new LinkedHashMap<>(listOfMaps.get(i));

            if (i + 1 == listOfMaps.size()) {
                joinedList.add(listOfMaps.get(i));
            } else {
                while (isSingleEvent(currentElement, listOfMaps.get(i + 1), zonesConverter(zones))) {
                    for (String key : currentElement.keySet()) {
                        currentElement.merge(key, listOfMaps.get(i + 1).get(key), (oldVal, newVal) -> oldVal.isEmpty() ? newVal : oldVal);
                    }
                    if (currentElement.containsKey("Violation")) {
                        if (currentElement.get("Violation").equals("Yes") || listOfMaps.get(i + 1).get("Violation").equals("Yes"))
                            currentElement.replace("Violation", "Yes");
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

    private static boolean isSingleEvent(Map<String, String> firstReading, Map<String, String> secondReading, Map<String, List<String>> zones) {
        return timeCheck(firstReading, secondReading, 300) &&
               idCheck(firstReading, secondReading) &&
               zoneCheck(firstReading, secondReading, zones);
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

    private static boolean zoneCheck(Map<String, String> firstReading, Map<String, String> secondReading, Map<String, List<String>> zones) {
        boolean isZoneAndDeviceValid = zones.get(firstReading.get("Zone")).contains(firstReading.get("Device")) && zones.get(secondReading.get("Zone")).contains(secondReading.get("Device"));
        boolean isSameZoneAndDifferentDevice = firstReading.get("Zone").equals(secondReading.get("Zone")) && !firstReading.get("Device").equals(secondReading.get("Device"));
        return isZoneAndDeviceValid && isSameZoneAndDifferentDevice;
    }

    private static Map<String, List<String>> zonesConverter(List<Map<String, String>> listOfZones) {
        Map<String, List<String>> mapOfZones = new HashMap<>();

        String zoneName = listOfZones.get(0).get("Zone");
        List<String> devices = new ArrayList<>();

        for (Map<String, String> zone : listOfZones) {
            if (zoneName.equals(zone.get("Zone"))) {
                devices.add(zone.get("Device"));
                mapOfZones.put(zoneName, new ArrayList<>(devices));
            } else {
                devices.clear();
                zoneName = zone.get("Zone");
                devices.add(zone.get("Device"));
                mapOfZones.put(zoneName, new ArrayList<>(devices));
            }
        }
        return mapOfZones;
    }
}
