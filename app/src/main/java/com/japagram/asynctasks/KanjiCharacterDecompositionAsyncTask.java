package com.japagram.asynctasks;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.japagram.R;
import com.japagram.data.RoomKanjiDatabase;
import com.japagram.data.KanjiCharacter;
import com.japagram.resources.Globals;
import com.japagram.resources.LocaleHelper;
import com.japagram.resources.Utilities;
import com.japagram.ui.ConvertFragment;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class KanjiCharacterDecompositionAsyncTask extends AsyncTask<Void, Void, Object> {

    //region Parameters
    private RoomKanjiDatabase mRoomKanjiDatabase;
    private final String inputQuery;
    private KanjiCharacter mCurrentKanjiCharacter;
    private final List<String[]> mRadicalsOnlyDatabase;
    private final int radicalIteration;
    private final int kanjiListIndex;
    private Resources mLocalizedResources;
    private WeakReference<Context> contextRef;
    public KanjiCharacterDecompositionAsyncResponseHandler listener;
    //endregion

    public KanjiCharacterDecompositionAsyncTask(Context context,
                                                String inputQuery,
                                                int radicalIteration,
                                                List<String[]> mRadicalsOnlyDatabase,
                                                int kanjiListIndex,
                                                KanjiCharacterDecompositionAsyncResponseHandler listener) {
        contextRef = new WeakReference<>(context);
        this.inputQuery = inputQuery;
        this.radicalIteration = radicalIteration;
        this.mRadicalsOnlyDatabase = mRadicalsOnlyDatabase;
        this.kanjiListIndex = kanjiListIndex;
        this.listener = listener;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected Object doInBackground(Void... voids) {

        mRoomKanjiDatabase = RoomKanjiDatabase.getInstance(contextRef.get());
        mLocalizedResources = Utilities.getLocalizedResources(contextRef.get(), Locale.getDefault());

        // Search for the input in the database and retrieve the result's characteristics

        String concatenated_input = Utilities.removeSpecialCharacters(inputQuery);
        String inputHexIdentifier = Utilities.convertToUTF8Index(concatenated_input).toUpperCase();
        mCurrentKanjiCharacter = mRoomKanjiDatabase.getKanjiCharacterByHexId(inputHexIdentifier);
        List<String> currentKanjiDetailedCharacteristics = getKanjiDetailedCharacteristics(mCurrentKanjiCharacter);
        List<String> currentKanjiMainRadicalInfo = getKanjiRadicalCharacteristics(mCurrentKanjiCharacter);

        List<List<String>> decomposedKanji = Decomposition(inputQuery);
        Object[] radicalInfo = getRadicalInfo();

        return new Object[] {
                decomposedKanji,
                currentKanjiDetailedCharacteristics,
                currentKanjiMainRadicalInfo,
                inputQuery,
                radicalIteration,
                radicalInfo[0],
                radicalInfo[1],
                radicalInfo[2],
                kanjiListIndex
        };
    }

    @Override
    protected void onPostExecute(Object words) {
        super.onPostExecute(words);
        listener.onKanjiCharacterDecompositionAsyncTaskResultFound(words);
    }

    public interface KanjiCharacterDecompositionAsyncResponseHandler {
        void onKanjiCharacterDecompositionAsyncTaskResultFound(Object text);
    }

    private List<List<String>> Decomposition(String word) {

        String concatenated_input = Utilities.removeSpecialCharacters(word);
        String inputHexIdentifier = Utilities.convertToUTF8Index(concatenated_input).toUpperCase();
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

            kanji_and_its_structure.add(getStringFromUTF8(mCurrentKanjiCharacter.getHexIdentifier()));
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

                        newDecomposition = Decomposition(current_component);

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
    private String getStringFromUTF8(String word) {

        String hex = word.substring(2);
        ByteBuffer buff = ByteBuffer.allocate(hex.length()/2);
        for (int i = 0; i < hex.length(); i+=2) {
            buff.put((byte)Integer.parseInt(hex.substring(i, i+2), 16));
        }
        buff.rewind();
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = cs.decode(buff);
        String string_value_of_hex = cb.toString();

        return string_value_of_hex;
    }
    private String getFormattedReadings(String readings) {

        String readingLatin;
        String[] components;
        List<String> readingsList = new ArrayList<>();
        if (readings != null) {
            for (String reading : readings.split(";")) {
                components = reading.split("\\.");
                readingLatin = ConvertFragment.getWaapuroHiraganaKatakana(components[0]).get(Globals.TYPE_LATIN);
                if (components.length > 1) readingLatin +=
                        "(" + ConvertFragment.getWaapuroHiraganaKatakana(components[1]).get(Globals.TYPE_LATIN) + ")";
                readingsList.add(readingLatin);
            }
            readingsList = Utilities.removeDuplicatesFromList(readingsList);
        }

        return (readingsList.size()>0 && !readingsList.get(0).equals(""))? TextUtils.join(", ", readingsList) : "-";
    }
    private List<String> getKanjiDetailedCharacteristics(KanjiCharacter kanjiCharacter) {

        List<String> characteristics = new ArrayList<>(Arrays.asList("", "", "", ""));
        if (kanjiCharacter ==null) return characteristics;

        characteristics.set(Globals.KANJI_ON_READING, getFormattedReadings(kanjiCharacter.getOnReadings()));
        characteristics.set(Globals.KANJI_KUN_READING, getFormattedReadings(kanjiCharacter.getKunReadings()));
        characteristics.set(Globals.KANJI_NAME_READING, getFormattedReadings(kanjiCharacter.getNameReadings()));

        boolean meaningsENisEmpty = TextUtils.isEmpty(kanjiCharacter.getMeaningsEN());
        switch (LocaleHelper.getLanguage(contextRef.get())) {
            case "en":
                characteristics.set(Globals.KANJI_MEANING, meaningsENisEmpty? "-" : kanjiCharacter.getMeaningsEN());
                break;
            case "fr":
                characteristics.set(Globals.KANJI_MEANING, TextUtils.isEmpty(kanjiCharacter.getMeaningsFR())?
                        (meaningsENisEmpty? "-" : mLocalizedResources.getString(R.string.english_meanings_available_only) + " " + kanjiCharacter.getMeaningsEN()) : kanjiCharacter.getMeaningsFR());
                break;
            case "es":
                characteristics.set(Globals.KANJI_MEANING, TextUtils.isEmpty(kanjiCharacter.getMeaningsES())?
                        (meaningsENisEmpty? "-" : mLocalizedResources.getString(R.string.english_meanings_available_only) + " " + kanjiCharacter.getMeaningsEN()) : kanjiCharacter.getMeaningsES());
                break;
        }

        return characteristics;
    }
    private List<String> getKanjiRadicalCharacteristics(KanjiCharacter kanjiCharacter) {

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
                        text = mLocalizedResources.getString(R.string.characters_main_radical_is) + " " +
                                mRadicalsOnlyDatabase.get(radical_index)[Globals.RADICAL_KANA] + " " +
                                "(" + mLocalizedResources.getString(R.string.number_abbrev_) + " " +
                                parsed_list.get(0) +
                                ") "  + mLocalizedResources.getString(R.string.with) + " " +
                                parsed_list.get(1) + " " +
                                ((Integer.valueOf(parsed_list.get(1))>1)? mLocalizedResources.getString(R.string.aditional_strokes)
                                        : mLocalizedResources.getString(R.string.additional_stroke))
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
    private Object[] getRadicalInfo() {

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
            String radicalHexIdentifier = Utilities.convertToUTF8Index(mainRadical).toUpperCase();
            KanjiCharacter kanjiCharacter = mRoomKanjiDatabase.getKanjiCharacterByHexId(radicalHexIdentifier);
            currentMainRadicalDetailedCharacteristics = getKanjiDetailedCharacteristics(kanjiCharacter);

        }
        return new Object[]{
                radicalIndex,
                mainRadicalIndex,
                currentMainRadicalDetailedCharacteristics
        };
    }
}
