package com.japagram.utilitiesCrossPlatform;

import android.content.Context;
import android.content.res.Resources;

import com.japagram.R;
import com.japagram.data.KanjiCharacter;
import com.japagram.data.RoomKanjiDatabase;
import com.japagram.utilitiesPlatformOverridable.OvUtilsGeneral;
import com.japagram.utilitiesPlatformOverridable.OvUtilsResources;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UtilitiesKanjiDecomposition {
    public static @NotNull List<List<String>> Decomposition(String word, @NotNull RoomKanjiDatabase mRoomKanjiDatabase) {
        KanjiCharacter mCurrentKanjiCharacter;
        String concatenated_input = UtilitiesGeneral.removeSpecialCharacters(word);
        String inputHexIdentifier = OvUtilsGeneral.convertToUTF8Index(concatenated_input).toUpperCase();
        mCurrentKanjiCharacter = mRoomKanjiDatabase.getKanjiCharacterByHexId(inputHexIdentifier);

        List<List<String>> decomposedKanji = new ArrayList<>();
        List<String> kanji_and_its_structure = new ArrayList<>();
        List<String> components_and_their_structure;

        //If decompositions don't exist in the database, then this is a basic character
        if (mCurrentKanjiCharacter ==null) {
            kanji_and_its_structure.add(word);
            kanji_and_its_structure.add("c");
            decomposedKanji.add(kanji_and_its_structure);
        }

        //Otherwise, get the decompositions
        else {

            kanji_and_its_structure.add(OvUtilsGeneral.getStringFromUTF8(mCurrentKanjiCharacter.getHexIdentifier()));
            kanji_and_its_structure.add(mCurrentKanjiCharacter.getStructure());
            decomposedKanji.add(kanji_and_its_structure);

            List<String> parsedComponents = Arrays.asList(mCurrentKanjiCharacter.getComponents().split(";"));

            String current_component;
            List<List<String>> newDecomposition;

            for (int i = 0; i < parsedComponents.size() ; i++) {
                current_component = parsedComponents.get(i);
                components_and_their_structure = new ArrayList<>();

                if (current_component.length()>0) {
                    if ((current_component.charAt(0) == '0' || current_component.charAt(0) == '1' || current_component.charAt(0) == '2' ||
                            current_component.charAt(0) == '3' || current_component.charAt(0) == '4' || current_component.charAt(0) == '5' ||
                            current_component.charAt(0) == '6' || current_component.charAt(0) == '7' || current_component.charAt(0) == '8' ||
                            current_component.charAt(0) == '9')) {

                        newDecomposition = Decomposition(current_component, mRoomKanjiDatabase);

                        // Update the component structures to include the master structure
                        for (int j=1;j<newDecomposition.size();j++) { newDecomposition.get(j).set(1,newDecomposition.get(j).get(1));}

                        // Remove the first List<String> from newDecomposition so that only the decomposed components may be added to decomposedKanji
                        newDecomposition.remove(0);
                        decomposedKanji.addAll(newDecomposition);
                    }
                    else {
                        components_and_their_structure.add(current_component);
                        components_and_their_structure.add("");
                        decomposedKanji.add(components_and_their_structure);
                    }
                }
            }
        }

        return decomposedKanji;
    }

    private static String getFormattedReadings(String readings) {

        String readingLatin;
        String[] components;
        List<String> readingsList = new ArrayList<>();
        if (readings != null) {
            for (String reading : readings.split(";")) {
                components = reading.split("\\.");
                readingLatin = UtilitiesQuery.getWaapuroHiraganaKatakana(components[0]).get(Globals.TEXT_TYPE_LATIN);
                if (components.length > 1) readingLatin +=
                        OvUtilsGeneral.concat(new String[]{"(", UtilitiesQuery.getWaapuroHiraganaKatakana(components[1]).get(Globals.TEXT_TYPE_LATIN), ")"});
                readingsList.add(readingLatin);
            }
            readingsList = UtilitiesGeneral.removeDuplicatesFromStringList(readingsList);
        }

        return (readingsList.size()>0 && !readingsList.get(0).equals(""))? OvUtilsGeneral.joinList(", ", readingsList) : "-";
    }

    public static @NotNull List<String> getKanjiDetailedCharacteristics(KanjiCharacter kanjiCharacter, String language, Context context) {

        List<String> characteristics = new ArrayList<>(Arrays.asList("", "", "", ""));
        if (kanjiCharacter == null) return characteristics;

        characteristics.set(Globals.KANJI_ON_READING, getFormattedReadings(kanjiCharacter.getOnReadings()));
        characteristics.set(Globals.KANJI_KUN_READING, getFormattedReadings(kanjiCharacter.getKunReadings()));
        characteristics.set(Globals.KANJI_NAME_READING, getFormattedReadings(kanjiCharacter.getNameReadings()));

        boolean meaningsENisEmpty = OvUtilsGeneral.isEmptyString(kanjiCharacter.getMeaningsEN());
        switch (language) {
            case Globals.LANG_STR_EN:
                characteristics.set(Globals.KANJI_MEANING, meaningsENisEmpty? "-" : kanjiCharacter.getMeaningsEN());
                break;
            case Globals.LANG_STR_FR:
                if (OvUtilsGeneral.isEmptyString(kanjiCharacter.getMeaningsFR())) {
                    if (meaningsENisEmpty) characteristics.set(Globals.KANJI_MEANING, "-");
                    else {
                        String text = OvUtilsResources.getString("english_meanings_available_only", context, Globals.RESOURCE_MAP_GENERAL, language);
                        characteristics.set(Globals.KANJI_MEANING, OvUtilsGeneral.concat(new String[]{text, " ", kanjiCharacter.getMeaningsEN()}));
                    }
                } else {
                    characteristics.set(Globals.KANJI_MEANING, kanjiCharacter.getMeaningsFR());
                }
                break;
            case Globals.LANG_STR_ES:
                if (OvUtilsGeneral.isEmptyString(kanjiCharacter.getMeaningsES())) {
                    if (meaningsENisEmpty) characteristics.set(Globals.KANJI_MEANING, "-");
                    else {
                        String text = OvUtilsResources.getString("english_meanings_available_only", context, Globals.RESOURCE_MAP_GENERAL, language);
                        characteristics.set(Globals.KANJI_MEANING, OvUtilsGeneral.concat(new String[]{text, " ", kanjiCharacter.getMeaningsEN()}));
                    }
                } else {
                    characteristics.set(Globals.KANJI_MEANING, kanjiCharacter.getMeaningsES());
                }
                break;
        }

        return characteristics;
    }

    public static @NotNull List<String> getKanjiRadicalCharacteristics(KanjiCharacter kanjiCharacter, List<String[]> mRadicalsOnlyDatabase, Context context, String language) {

        List<String> radical_characteristics = new ArrayList<>();

        if (kanjiCharacter ==null || kanjiCharacter.getRadPlusStrokes()==null) {
            radical_characteristics.add("");
        }
        else {
            List<String> parsed_list = Arrays.asList(kanjiCharacter.getRadPlusStrokes().split("\\+"));

            if (parsed_list.size()>1) {
                if (!parsed_list.get(1).equals("0")) {
                    int radical_index = -1;
                    for (int i = 0; i < mRadicalsOnlyDatabase.size(); i++) {
                        if (parsed_list.get(0).equals(mRadicalsOnlyDatabase.get(i)[Globals.RADICAL_NUM])) {
                            radical_index = i;
                            break;
                        }
                    }
                    String text = "";
                    if (radical_index != -1) {
                        text = OvUtilsResources.getString("characters_main_radical_is", context, Globals.RESOURCE_MAP_GENERAL, language) + " " +
                                mRadicalsOnlyDatabase.get(radical_index)[Globals.RADICAL_KANA] + " " +
                                "(" + OvUtilsResources.getString("number_abbrev_", context, Globals.RESOURCE_MAP_GENERAL, language) + " " +
                                parsed_list.get(0) +
                                ") "  + OvUtilsResources.getString("with", context, Globals.RESOURCE_MAP_GENERAL, language) + " " +
                                parsed_list.get(1) + " " +
                                ((Integer.parseInt(parsed_list.get(1))>1)? OvUtilsResources.getString("additional_strokes", context, Globals.RESOURCE_MAP_GENERAL, language)
                                        : OvUtilsResources.getString("additional_stroke", context, Globals.RESOURCE_MAP_GENERAL, language))
                                + ".";
                    }
                    radical_characteristics.add(text);
                }
                else {radical_characteristics.add("");}
            }
            else {radical_characteristics.add("");}

        }
        return radical_characteristics;
    }

    @Contract("_, _, _, _, _ -> new")
    public static Object @NotNull [] getRadicalInfo(String inputQuery, @NotNull List<String[]> mRadicalsOnlyDatabase, RoomKanjiDatabase mRoomKanjiDatabase, String language, Context context) {

        int radicalIndex = -1;
        int mainRadicalIndex = 0;
        List<String> currentMainRadicalDetailedCharacteristics = new ArrayList<>();

        //Find the radical index
        for (int i = 0; i< mRadicalsOnlyDatabase.size(); i++) {
            if (inputQuery.equals(mRadicalsOnlyDatabase.get(i)[Globals.RADICAL_KANA])) {
                radicalIndex = i;
            }
        }

        if (radicalIndex >= 0) {
            List<String> parsed_number = Arrays.asList(mRadicalsOnlyDatabase.get(radicalIndex)[Globals.RADICAL_NUM].split(";"));
            boolean found_main_radical = false;
            mainRadicalIndex = radicalIndex;

            if (parsed_number.size() > 1) {
                while (!found_main_radical) {
                    if (mRadicalsOnlyDatabase.get(mainRadicalIndex)[Globals.RADICAL_NUM].contains(";")) {
                        mainRadicalIndex--;
                    } else {
                        found_main_radical = true;
                    }
                }
            }

            //Get the remaining radical characteristics (readings, meanings) from the KanjiDictDatabase
            String mainRadical = mRadicalsOnlyDatabase.get(mainRadicalIndex)[Globals.RADICAL_KANA];
            String radicalHexIdentifier = OvUtilsGeneral.convertToUTF8Index(mainRadical).toUpperCase();
            KanjiCharacter kanjiCharacter = mRoomKanjiDatabase.getKanjiCharacterByHexId(radicalHexIdentifier);
            currentMainRadicalDetailedCharacteristics = getKanjiDetailedCharacteristics(kanjiCharacter, language, context);

        }
        return new Object[]{
                radicalIndex,
                mainRadicalIndex,
                currentMainRadicalDetailedCharacteristics
        };
    }
}
