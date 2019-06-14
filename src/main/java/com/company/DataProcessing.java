package com.company;

import java.util.*;

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

    private static String getMissingValue(List<Map<String, String>> source, String missingKey, String presentKey, String presentValue) {
        for (Map<String, String> mappedValues : source) {
            if (mappedValues.get(presentKey).equals(presentValue)){
                return mappedValues.get(missingKey);
            }
        }
        return "NULL";
    }
}
