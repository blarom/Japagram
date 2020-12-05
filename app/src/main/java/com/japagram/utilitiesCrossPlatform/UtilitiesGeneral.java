package com.japagram.utilitiesCrossPlatform;

import android.os.Build;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class UtilitiesGeneral {
    //String manipulation utilities
    @NotNull
    public static String convertToUTF8Index(String input_string) {

        byte[] byteArray;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            byteArray = input_string.getBytes(StandardCharsets.UTF_8);
            StringBuilder prepared_word = new StringBuilder("1.");
            for (byte b : byteArray) {
                prepared_word.append(Integer.toHexString(b & 0xFF));
            }
            return prepared_word.toString();
        }
        return "";
    }

    @NotNull
    public static String convertFromUTF8Index(@NotNull String inputHex) {

        //inspired by: https://stackoverflow.com/questions/15749475/java-string-hex-to-string-ascii-with-accentuation
        if (inputHex.length() < 4) return "";
        inputHex = inputHex.toLowerCase().substring(2, inputHex.length());

        ByteBuffer buff = ByteBuffer.allocate(inputHex.length() / 2);
        for (int i = 0; i < inputHex.length(); i += 2) {
            buff.put((byte) Integer.parseInt(inputHex.substring(i, i + 2), 16));
        }
        buff.rewind();
        Charset cs;
        CharBuffer cb;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            cs = StandardCharsets.UTF_8;
            cb = cs.decode(buff);
            return cb.toString();
        }
        return "";
    }

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
        List<String> parsed_cumulative_meaning_value = Arrays.asList(splitAtCommasOutsideParentheses(input_list));
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
        return com.japagram.utilitiesPlatformOverridable.UtilitiesGeneral.joinList(", ", final_cumulative_meaning_value_array);
    }

    @NotNull
    public static List<String> combineLists(List<String> list1, List<String> list2) {
        List<String> total = new ArrayList<>(list1);
        total.addAll(list2);
        return removeDuplicatesFromStringList(total);
    }

    @NotNull
    public static List<String> getIntersectionOfLists(@NotNull List<String> A, List<String> B) {
        //https://stackoverflow.com/questions/2400838/efficient-intersection-of-component_substructures[2]-liststring-in-java
        List<String> rtnList = new LinkedList<>();
        for (String dto : A) {
            if (B.contains(dto)) {
                rtnList.add(dto);
            }
        }
        return rtnList;
    }

    @NotNull
    @Contract("_ -> new")
    public static List<String> removeDuplicatesFromStringList(@NotNull List<String> list) {

        //https://stackoverflow.com/questions/14040331/remove-duplicate-strings-in-a-list-in-java

        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Iterator<String> i = list.iterator();
        while (i.hasNext()) {
            String s = i.next();
            if (set.contains(s)) {
                i.remove();
            } else {
                set.add(s);
            }
        }

        return new ArrayList<>(set);
    }

    public static List<Long> removeDuplicatesFromLongList(@NotNull List<Long> list) {

        List<Long> newList = new ArrayList<>();
        for (Long id : list) {
            if (!newList.contains(id)) newList.add(id);
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
    public static String[] splitAtCommasOutsideParentheses(@NotNull String text) {
        // https://stackoverflow.com/questions/9030036/regex-to-match-only-commas-not-in-parentheses
        return text.split(",(?![^(]*\\))(?![^\"']*[\"'](?:[^\"']*[\"'][^\"']*[\"'])*[^\"']*$)");
    }

    @NotNull
    public static List<long[]> bubbleSortForThreeIntegerList(@NotNull List<long[]> MatchList) {

        // Sorting the results according to the shortest keyword as found in the above search

        // Computing the value length
        int list_size = MatchList.size();
        long[][] matches = new long[list_size][3];
        for (int i = 0; i < list_size; i++) {
            matches[i][0] = MatchList.get(i)[0];
            matches[i][1] = MatchList.get(i)[1];
            matches[i][2] = MatchList.get(i)[2];
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

        List<long[]> sortedMatchList = new ArrayList<>();
        long[] element;
        for (int i = 0; i < list_size; i++) {
            element = new long[3];
            element[0] = matches[i][0];
            element[1] = matches[i][1];
            element[2] = matches[i][2];
            sortedMatchList.add(element);
        }

        return sortedMatchList;
    }

}
