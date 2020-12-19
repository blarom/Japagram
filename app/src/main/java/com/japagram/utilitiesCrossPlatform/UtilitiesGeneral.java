package com.japagram.utilitiesCrossPlatform;

import com.japagram.utilitiesPlatformOverridable.OverridableUtilitiesGeneral;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class UtilitiesGeneral {

    @NotNull
    public static String removeNonSpaceSpecialCharacters(@NotNull String sentence) {
        return sentence.replaceAll("[.\\-():/]", "");
    }

    @NotNull
    public static String removeSpecialCharacters(@NotNull String sentence) {
        return sentence.replaceAll("[.\\-():/\\s]", "");
    }

    public static String removeDuplicatesFromCommaList(String input_list) {

        boolean is_repeated;
        List<String> parsed_cumulative_meaning_value = Arrays.asList(OverridableUtilitiesGeneral.splitAtCommasOutsideParentheses(input_list));
        List<String> final_cumulative_meaning_value_array = new ArrayList<>();
        String current_value;
        for (int j = 0; j < parsed_cumulative_meaning_value.size(); j++) {
            is_repeated = false;
            current_value = parsed_cumulative_meaning_value.get(j).trim();
            for (String s : final_cumulative_meaning_value_array) {
                if (s.equals(current_value)) {
                    is_repeated = true;
                    break;
                }
            }
            if (!is_repeated) final_cumulative_meaning_value_array.add(current_value);
        }
        return OverridableUtilitiesGeneral.joinList(", ", final_cumulative_meaning_value_array);
    }

    public static String[] @NotNull [] getTranspose(@NotNull List<String[]> db) {
        if (db == null || db.size() == 0) return null;
        String[][] transpose = new String[db.get(0).length][db.size()];
        String[] line;
        for (int i=0; i<db.size(); i++) {
            line = db.get(i);
            for (int j=0; j<line.length; j++) {
                transpose[j][i] = line[j];
            }
        }
        return transpose;
    }
    @NotNull
    public static List<String> combineLists(List<String> list1, List<String> list2) {
        List<String> total = new ArrayList<>(list1);
        total.addAll(list2);
        return removeDuplicatesFromStringList(total);
    }


    @NotNull
    public static List<String> getIntersectionOfLists(@NotNull List<String> listA, List<String> listB) {
        //https://stackoverflow.com/questions/2400838/efficient-intersection-of-component_substructures[2]-liststring-in-java
        List<String> rtnList = new LinkedList<>();
        for (String dto : listA) {
            if (listB.contains(dto)) {
                rtnList.add(dto);
            }
        }
        return rtnList;
    }

    @NotNull
    @Contract("_ -> new")
    public static List<String> removeDuplicatesFromStringList(@NotNull List<String> list) {

        //https://stackoverflow.com/questions/14040331/remove-duplicate-strings-in-a-list-in-java

        List<String> newList = new ArrayList<>();
        for (String item : list) {
            if (!newList.contains(item)) newList.add(item);
        }
        return newList;
    }

    public static @NotNull List<Long> removeDuplicatesFromLongList(@NotNull List<Long> list) {

        List<Long> newList = new ArrayList<>();
        for (Long item : list) {
            if (!newList.contains(item)) newList.add(item);
        }
        return newList;
    }

    @Contract("null -> null")
    public static String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    @NotNull
    public static List<long[]> bubbleSortForThreeIntegerList(@NotNull List<long[]> originalList) {

        // Sorting the results according to the shortest keyword as found in the above search

        // Computing the value length
        int list_size = originalList.size();
        long[][] matches = new long[list_size][3];
        for (int i = 0; i < list_size; i++) {
            matches[i][0] = originalList.get(i)[0];
            matches[i][1] = originalList.get(i)[1];
            matches[i][2] = originalList.get(i)[2];
        }

        // Sorting
        long tempVar0;
        long tempVar1;
        long tempVar2;
        for (int i = 0; i < list_size; i++) { //Bubble sort
            for (int t = 1; t < list_size - i; t++) {
                if (matches[t - 1][1] > matches[t][1]) {
                    tempVar0 = matches[t - 1][0];
                    tempVar1 = matches[t - 1][1];
                    tempVar2 = matches[t - 1][2];
                    matches[t - 1][0] = matches[t][0];
                    matches[t - 1][1] = matches[t][1];
                    matches[t - 1][2] = matches[t][2];
                    matches[t][0] = tempVar0;
                    matches[t][1] = tempVar1;
                    matches[t][2] = tempVar2;
                }
            }
        }

        List<long[]> sortedList = new ArrayList<>();
        long[] element;
        for (int i = 0; i < list_size; i++) {
            element = new long[3];
            element[0] = matches[i][0];
            element[1] = matches[i][1];
            element[2] = matches[i][2];
            sortedList.add(element);
        }

        return sortedList;
    }

}
