package com.japagram.utilitiesAndroid;

import android.content.Context;
import android.text.TextUtils;

import com.japagram.R;
import com.japagram.data.Word;
import com.japagram.utilitiesCrossPlatform.UtilitiesQuery;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesPlatformOverridable.OverridableUtilitiesGeneral;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class UtilitiesWeb {
    private static int runningIndex = 0;
    private static String websiteCodeString = "";

    @NotNull
    public static List<Word> getWordsFromJishoOnWeb(String word, final Context context) {

        if (TextUtils.isEmpty(word)) {
            return new ArrayList<>();
        }

        //region Preparing the word to be included in the url
        StringBuilder prepared_word;
        if (UtilitiesQuery.getTextType(word) == Globals.TEXT_TYPE_KANJI) {
            String converted_word = OverridableUtilitiesGeneral.convertToUTF8Index(word);
            converted_word = converted_word.substring(2);
            prepared_word = new StringBuilder();
            for (int i = 0; i < converted_word.length() - 1; i = i + 2) {
                prepared_word.append("%").append(converted_word, i, i + 2);
            }
        } else {
            prepared_word = new StringBuilder(word);
        }
        //endregion

        //Getting the Jisho.org website code
        String website_code = getWebsiteXml(context.getString(R.string.jisho_website_url) + prepared_word);

        //Returning nothing if there was a problem getting results
        if ((website_code != null && website_code.equals(""))
                || website_code == null
                || website_code.length() == 0
                || website_code.contains("Sorry, couldn't find anything matching")
                || website_code.contains("Sorry, couldn't find any words matching")
                || (website_code.contains("Searched for") && website_code.contains("No matches for"))) {
            return new ArrayList<>();
        }

        //Parsing the website code and mapping it to a List<Word>
        List<Object> parsedData = parseJishoWebsiteToTree(website_code);
        List<Word> wordsList = adaptJishoTreeToWordsList(parsedData);

        return wordsList;
    }

    @NotNull
    public static List<Word> removeEdictExceptionsFromJisho(@NotNull List<Word> words) {

        List<Word> nonExceptionWords = new ArrayList<>();
        boolean isException;
        for (Word word : words) {
            isException = false;
            for (String[] romajiKanji : Globals.EDICT_EXCEPTIONS) {
                if (word.getKanji().equals(romajiKanji[1]) && (romajiKanji[0].equals("*") || word.getRomaji().equals(romajiKanji[0]))) {
                    isException = true;
                }
            }
            if (!isException) nonExceptionWords.add(word);
        }
        return nonExceptionWords;
    }

    @NotNull
    public static List<Word> cleanUpProblematicWordsFromJisho(@NotNull List<Word> words) {

        List<Word> cleanWords = new ArrayList<>();
        //Clean up problematic words (e.g. that don't include a meaning)
        for (Word word : words) {
            if (word.getMeaningsEN().size() > 0) cleanWords.add(word);
        }
        return cleanWords;
    }

    @Nullable
    private static String getWebsiteXml(String websiteUrl) {

        StringBuilder responseString = new StringBuilder();
        String inputLine;
        HttpURLConnection connection = null;

        try {
            //https://stackoverflow.com/questions/35568584/android-studio-deprecated-on-httpparams-httpconnectionparams-connmanagerparams
            //String current_url = "https://www.google.co.il/search?dcr=0&source=hp&q=" + prepared_word;
            URL dataUrl = new URL(websiteUrl);
            connection = (HttpURLConnection) dataUrl.openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setInstanceFollowRedirects(true);
            // optional default is GET
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                responseString = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                    responseString.append(inputLine).append('\n');
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            OverridableUtilitiesGeneral.printLog("Diagnosis Time", "Failed to access online resources.");
            return null;
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace(); //If you want further info on failure...
            }
        }
        return responseString.toString();
    }

    private static List<Object> parseJishoWebsiteToTree(@NotNull String website_code) {

        runningIndex = 0;
        int initial_offset = 15; //Skips <!DOCTYPE html>
        websiteCodeString = website_code.substring(initial_offset);
        List<Object> parsedWebsiteTree = new ArrayList<>();
        try {
            parsedWebsiteTree = getChildren();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return parsedWebsiteTree;
    }

    private static List<Object> getChildren() {

        if (runningIndex < 0) return new ArrayList<>();

        List<Object> currentParent = new ArrayList<>();

        if (runningIndex > websiteCodeString.length() - 1) {
            currentParent.add("");
            return currentParent;
        }
        String remainingWebsiteCodeString = websiteCodeString.substring(runningIndex);

        if (!remainingWebsiteCodeString.contains("<")) {
            currentParent.add(remainingWebsiteCodeString);
            return currentParent;
        }

        while (0 <= runningIndex && runningIndex < websiteCodeString.length()) {

            //Getting the next header characteristics
            int nextHeaderStart = websiteCodeString.indexOf("<", runningIndex);
            if (nextHeaderStart == -1) return currentParent;
            int nextHeaderEnd = websiteCodeString.indexOf(">", nextHeaderStart);
            String currentHeader = websiteCodeString.substring(nextHeaderStart + 1, nextHeaderEnd);

            //UtilitiesGeneral.printLog("Diagnosis Time", "Current child: " + runningIndex + ", " + currentHeader);

            //If there is String text before the next header, add it to the list and continue to the header
            if (nextHeaderStart != runningIndex) {
                String currentText = websiteCodeString.substring(runningIndex, nextHeaderStart);
                StringBuilder validText = new StringBuilder();
                for (int i = 0; i < currentText.length(); i++) {
                    if (i < currentText.length() - 1 && currentText.substring(i, i + 1).equals("\n")) {
                        i++;
                        continue;
                    }
                    validText.append(currentText.charAt(i));
                }
                String validTextString = validText.toString().trim();
//                boolean isOnlyWhiteSpace = true;
//                for (int i=0; i<validTextString.length(); i++) {
//                    if (!Character.isWhitespace(validTextString.charAt(i))) {isOnlyWhiteSpace = false; break;}
//                }
                if (!TextUtils.isEmpty(validTextString)) currentParent.add(validTextString);
                runningIndex = nextHeaderStart;
            }

            //If the header is of type "<XXX/>" then there is no subtree. In this case add the header to the tree and move to next subtree.
            if (websiteCodeString.substring(nextHeaderEnd - 1, nextHeaderEnd + 1).equals("/>")) {
                currentParent.add(currentHeader);
                runningIndex = nextHeaderEnd + 1;
            }

            //If the header is of type "<XXX>" then:
            // - if the header is <br> there is no substree and the header should be treated as text
            else if (currentHeader.equals("br")) {
                currentParent.add("<br>");
                runningIndex = nextHeaderEnd + 1;
            }
            // - if the header is a tail, move up the stack
            else if (currentHeader.substring(0, 1).equals("/")) {
                int endOfTail = websiteCodeString.indexOf(">", nextHeaderStart);
                runningIndex = endOfTail + 1;
                return currentParent;
            }
            // - if the header is <!-- XXX> then this is a comment and should be ignored
            else if (currentHeader.contains("!--")) {
                int endOfComment = websiteCodeString.indexOf("-->", runningIndex);
                runningIndex = endOfComment + 3;
            }
            //If the subtree is valid and is not the <head> subtree, add it to the tree
            else if (currentHeader.equals("head")) {
                currentParent.add(currentHeader);
                currentParent.add("");
                runningIndex = websiteCodeString.indexOf("</head>") + 7;
            }
            // - if the header is not <br> then there is a subtree and the methods recurses
            else {
                currentParent.add(currentHeader);
                runningIndex = nextHeaderEnd + 1;
                List<Object> subtree = getChildren();
                currentParent.add(subtree);
            }

        }

        return currentParent;
    }

    @NotNull
    private static List<Word> adaptJishoTreeToWordsList(@NotNull List<Object> parsedData) {

        List<Word> wordsList = new ArrayList<>();

        //Getting to the relevant tree section
        if (parsedData.size() < 1) return new ArrayList<>();
        List<Object> htmlData = (List<Object>) parsedData.get(1);
        if (htmlData == null || htmlData.size() < 3) return new ArrayList<>();
        List<Object> bodyData = (List<Object>) htmlData.get(3);
        List<Object> pageContainerData = (List<Object>) getElementAtHeader(bodyData, "page_container");
        if (pageContainerData == null) return new ArrayList<>();
        List<Object> large12ColumnsData = (List<Object>) getElementAtHeader(pageContainerData, "large-12 columns");
        if (large12ColumnsData == null) return new ArrayList<>();
        List<Object> mainResultsData = (List<Object>) getElementAtHeader(large12ColumnsData, "main_results");
        if (mainResultsData == null) return new ArrayList<>();
        List<Object> rowData = (List<Object>) getElementAtHeader(mainResultsData, "row");
        if (rowData == null) return new ArrayList<>();
        List<Object> primaryData = (List<Object>) getElementAtHeader(rowData, "primary");
        if (primaryData == null) return new ArrayList<>();

        List<Object> exactBlockData = (List<Object>) getElementAtHeader(primaryData, "exact_block");
        List<Object> conceptsBlockData;
        if (exactBlockData == null) {

            conceptsBlockData = (List<Object>) getElementAtHeader(primaryData, "concepts");
            if (conceptsBlockData == null) return wordsList;
            if (conceptsBlockData.size() > 2) wordsList.addAll(addWordsFromBigBlock(conceptsBlockData, 1));

            return wordsList;
        } else if (exactBlockData.size() > 2) {
            wordsList.addAll(addWordsFromBigBlock(exactBlockData, 3));

            conceptsBlockData = (List<Object>) getElementAtHeader(primaryData, "concepts");
            if (conceptsBlockData == null) return wordsList;
            if (conceptsBlockData.size() > 2) wordsList.addAll(addWordsFromBigBlock(conceptsBlockData, 1));
        }

        return wordsList;
    }

    @NotNull
    private static List<Word> addWordsFromBigBlock(@NotNull List<Object> bigBlockData, int startingSubBlock) {

        if (startingSubBlock >= bigBlockData.size()) return new ArrayList<>();

        List<Word> wordsList = new ArrayList<>();
        StringBuilder kanji;
        StringBuilder romaji;
        List<String> meaningTagsFromTree;
        List<String> meaningsFromTree;
        for (int i = startingSubBlock; i < bigBlockData.size(); i = i + 2) {

            Word currentWord = new Word();

            if (!(bigBlockData.get(i) instanceof List)) break;
            List<Object> conceptLightClearFixData = (List<Object>) bigBlockData.get(i);
            if (!(conceptLightClearFixData.get(1) instanceof List)) continue;
            List<Object> conceptLightWrapperData = (List<Object>) conceptLightClearFixData.get(1);
            List<Object> conceptLightReadingsData = (List<Object>) conceptLightWrapperData.get(1);
            List<Object> conceptLightRepresentationData = (List<Object>) conceptLightReadingsData.get(1);

            //region Extracting the kanji
            kanji = new StringBuilder();
            List<Object> TextData = (List<Object>) getElementAtHeader(conceptLightRepresentationData, "text");
            if (TextData != null && TextData.size() > 1) {
                kanji = new StringBuilder();
                for (int j = 0; j < TextData.size(); j++) {
                    String currentText;
                    currentText = "";
                    if (TextData.get(j) instanceof List) {
                        List<Object> list = (List<Object>) TextData.get(j);
                        if (list.size() > 0) currentText = (String) list.get(0);
                    } else {
                        currentText = (String) TextData.get(j);
                        if (currentText.equals("span")) currentText = "";
                    }
                    kanji.append(currentText);
                }
            } else if (TextData != null && TextData.size() > 0) kanji = new StringBuilder((String) TextData.get(0));
            currentWord.setKanji(kanji.toString());
            //endregion

            //region Extracting the romaji
            romaji = new StringBuilder();
            List<Object> furiganaData = (List<Object>) conceptLightRepresentationData.get(1);
            for (int j = 1; j < furiganaData.size(); j = j + 2) {
                List<Object> kanji1UpData = (List<Object>) furiganaData.get(j);
                if (kanji1UpData.size() > 0) romaji.append((String) kanji1UpData.get(0));
            }

            int textType = UtilitiesQuery.getTextType(kanji.toString());
            if (romaji.length() != 0 && (textType == Globals.TEXT_TYPE_HIRAGANA || textType == Globals.TEXT_TYPE_KATAKANA)) {
                //When the word is originally katakana only, the website does not display hiragana. This is corrected here.
                romaji = new StringBuilder(UtilitiesQuery.getWaapuroHiraganaKatakana(kanji.toString()).get(0));
            }

            List<Object> conceptLightStatusData = (List<Object>) getElementAtHeader(conceptLightWrapperData, "concept_light-status");
            if (conceptLightStatusData == null) conceptLightStatusData = (List<Object>) getElementAtHeader(conceptLightClearFixData, "concept_light-status");
            if (conceptLightStatusData != null) {
                List<Object> ulClassData = (List<Object>) getElementAtHeader(conceptLightStatusData, "ul class");
                if (ulClassData != null) {
                    for (int j = 1; j < ulClassData.size(); j = j + 2) {
                        List<Object> li = (List<Object>) ulClassData.get(j);
                        List<Object> aRef = (List<Object>) li.get(1);
                        String sentenceSearchFor = (String) aRef.get(0);
                        String currentValue = "";
                        if (sentenceSearchFor.length() > 20 && sentenceSearchFor.contains("Sentence search for")) {
                            currentValue = sentenceSearchFor.substring(20);
                        }

                        textType = UtilitiesQuery.getTextType(currentValue);
                        if (currentValue.length() != 0 &&
                                (textType == Globals.TEXT_TYPE_HIRAGANA || textType == Globals.TEXT_TYPE_KATAKANA)) {
                            //When the word is originally katakana only, the website does not display hiragana. This is corrected here.
                            romaji = new StringBuilder(UtilitiesQuery.getWaapuroHiraganaKatakana(currentValue).get(0));
                            break;
                        }
                    }
                }
            }
            currentWord.setRomaji(UtilitiesQuery.getWaapuroHiraganaKatakana(romaji.toString()).get(0));
            //endregion

            currentWord.setUniqueIdentifier(currentWord.getRomaji() + "-" + kanji);

            //region Extracting the Common Word status
            if (conceptLightStatusData != null) {
                List<Object> conceptLightCommonSuccess = (List<Object>) getElementAtHeader(conceptLightStatusData, "common success label");
                if (conceptLightCommonSuccess != null && conceptLightCommonSuccess.size() > 0) {
                    String value = (String) conceptLightCommonSuccess.get(0);
                    if (!TextUtils.isEmpty(value) && value.equalsIgnoreCase("Common word")) {
                        currentWord.setIsCommon(true);
                    } else currentWord.setIsCommon(false);
                } else currentWord.setIsCommon(false);
            }
            //endregion

            //region Extracting the meanings (types, meanings, altSpellings)

            List<Object> conceptLightMeaningsData = (List<Object>) getElementAtHeader(conceptLightClearFixData, "concept_light-meanings medium-9 columns");
            if (conceptLightMeaningsData == null) continue;
            List<Object> meaningsWrapperData = (List<Object>) conceptLightMeaningsData.get(1);

            String currentHeader = "";
            String meaningTag = "";
            String meaning;
            meaningTagsFromTree = new ArrayList<>();
            meaningsFromTree = new ArrayList<>();
            for (int j = 0; j < meaningsWrapperData.size(); j++) {

                if (j % 2 == 0) {
                    currentHeader = (String) meaningsWrapperData.get(j);
                    continue;
                }

                if (currentHeader.contains("meaning-tags")) {
                    List<Object> meaningsTagsData = (List<Object>) meaningsWrapperData.get(j);
                    meaningTag = "";
                    if (meaningsTagsData.size() > 0) meaningTag = (String) meaningsTagsData.get(0);
                }
                if (meaningTag.contains("Wikipedia") || meaningTag.contains("Notes")) continue;
                if (currentHeader.contains("meaning-wrapper")) {
                    if (meaningTag.contains("Other forms")) {
                        List<Object> meaningWrapperData = (List<Object>) meaningsWrapperData.get(j);
                        List<Object> meaningDefinitionData = (List<Object>) meaningWrapperData.get(1);
                        List<Object> meaningMeaningData = (List<Object>) getElementAtHeader(meaningDefinitionData, "meaning-meaning");
                        if (meaningMeaningData == null || meaningMeaningData.size() == 0) break;

                        //Getting the altSpellings container bock to extract from
                        StringBuilder altSpellingsContainer = new StringBuilder();
                        for (Object element : meaningMeaningData) {
                            if (element instanceof List) {
                                List<String> elementList = (List<String>) element;
                                if (elementList.size() > 0) {
                                    altSpellingsContainer.append(elementList.get(0));
                                }
                            }
                        }

                        //Extracting the altSpellings using regex
                        List<String> altSpellings = new ArrayList<>();
                        Matcher m = Pattern.compile("\\b(\\w+)\\s【(\\w+)】").matcher(altSpellingsContainer.toString());
                        while (m.find()) {
                            if (!m.group(1).equals(currentWord.getKanji())) altSpellings.add(m.group(1).trim());
                            String convertedMatch = UtilitiesQuery.getWaapuroHiraganaKatakana(m.group(2)).get(Globals.TEXT_TYPE_LATIN);
                            if (!convertedMatch.equals(currentWord.getRomaji())) altSpellings.add(convertedMatch.trim());
                        }
                        altSpellings = com.japagram.utilitiesCrossPlatform.UtilitiesGeneral.removeDuplicatesFromStringList(altSpellings);
                        currentWord.setAltSpellings(OverridableUtilitiesGeneral.joinList(", ", altSpellings));
                        break;
                    } else {
                        List<Object> meaningWrapperData = (List<Object>) meaningsWrapperData.get(j);
                        List<Object> meaningDefinitionData = (List<Object>) meaningWrapperData.get(1);
                        List<Object> meaningMeaningData = (List<Object>) getElementAtHeader(meaningDefinitionData, "meaning-meaning");
                        meaningTagsFromTree.add(meaningTag);
                        meaning = "";
                        if (meaningMeaningData != null && meaningMeaningData.size() > 0) meaning = (String) meaningMeaningData.get(0);
                        meaningsFromTree.add(reformatMeanings(meaning));
                    }
                }
            }

            List<Word.Meaning> wordMeaningsList = new ArrayList<>();
            for (int j = 0; j < meaningsFromTree.size(); j++) {

                Word.Meaning wordMeaning = new Word.Meaning();

                //Getting the Meaning value
                String matchingWordMeaning = meaningsFromTree.get(j);
                wordMeaning.setMeaning(matchingWordMeaning);

                //Getting the Type value
                String matchingWordType = meaningTagsFromTree.get(j);

                if (matchingWordType.contains("verb") && !matchingWordType.contains("Suru") && !matchingWordType.contains("Kuru")) {
                    if (matchingWordType.contains("su ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VsuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VsuT";
                        else matchingWordType = "VsuI";
                    } else if (matchingWordType.contains("ku ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VkuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VkuT";
                        else matchingWordType = "VkuI";
                    } else if (matchingWordType.contains("gu ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VguI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VguT";
                        else matchingWordType = "VguI";
                    } else if (matchingWordType.contains("mu ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VmuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VmuT";
                        else matchingWordType = "VmuI";
                    } else if (matchingWordType.contains("bu ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VbuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VbuT";
                        else matchingWordType = "VbuI";
                    } else if (matchingWordType.contains("nu ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VnuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VnuT";
                        else matchingWordType = "VnuI";
                    } else if (matchingWordType.contains("ru ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VrugI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VrugT";
                        else matchingWordType = "VrugI";
                    } else if (matchingWordType.contains("tsu ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VtsuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VtsuT";
                        else matchingWordType = "VtsuI";
                    } else if (matchingWordType.contains("u ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VuT";
                        else matchingWordType = "VuI";
                    } else if (matchingWordType.contains("Ichidan")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VruiI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VruiT";
                        else matchingWordType = "VruiI";
                    }
                } else {
                    String[] typeElements = matchingWordType.split(", ");
                    List<String> typesAsLegend = new ArrayList<>();
                    for (String typeElement : typeElements) {

                        if (typeElement.contains("Expression")) {
                            typesAsLegend.add("CE");
                        } else if (typeElement.equals("Adverb")) {
                            typesAsLegend.add("A");
                        } else if (typeElement.equals("Noun")) {
                            typesAsLegend.add("N");
                        } else if (typeElement.equals("Place")) {
                            typesAsLegend.add("Pl");
                        } else if (typeElement.equals("Temporal noun")) {
                            typesAsLegend.add("T");
                        } else if (typeElement.equals("Proper noun")) {
                            typesAsLegend.add("Ne");
                        } else if (typeElement.equals("Numeric")) {
                            typesAsLegend.add("num");
                        } else if (typeElement.equals("Counter")) {
                            typesAsLegend.add("C");
                        } else if (typeElement.contains("Suffix, Counter")) {
                            typesAsLegend.add("C");
                        } else if (typeElement.contains("Suffix") || matchingWordType.contains("suffix")) {
                            typesAsLegend.add("Sx");
                        } else if (typeElement.contains("Prefix") || matchingWordType.contains("prefix")) {
                            typesAsLegend.add("Px");
                        } else if (typeElement.contains("I-adjective") || matchingWordType.contains("i-adjective")) {
                            typesAsLegend.add("Ai");
                        } else if (typeElement.contains("Na-adjective") || matchingWordType.contains("na-adjective")) {
                            typesAsLegend.add("Ana");
                        } else if (typeElement.contains("No-adjective") || matchingWordType.contains("na-adjective")) {
                            typesAsLegend.add("Ano");
                        } else if (typeElement.contains("adjective") || matchingWordType.contains("Adjective")) {
                            typesAsLegend.add("Aj");
                        } else if (typeElement.contains("Pre-noun adjectival") || matchingWordType.contains("Pronoun")) {
                            typesAsLegend.add("P");
                        } else if (typeElement.contains("Auxiliary verb")) {
                            typesAsLegend.add("Vx");
                        } else if (typeElement.contains("Auxiliary adjective")) {
                            typesAsLegend.add("Ax");
                        } else if (typeElement.contains("Particle") || matchingWordType.contains("Preposition")) {
                            typesAsLegend.add("PP");
                        } else if (typeElement.contains("Conjunction")) {
                            typesAsLegend.add("CO");
                        } else if (typeElement.contains("Suru verb")) {
                            if (matchingWordType.contains("intransitive")) typesAsLegend.add("VsuruI");
                            if (matchingWordType.contains("Transitive")) typesAsLegend.add("VsuruT");
                            else typesAsLegend.add("Vsuru"); //TODO: this line prevents "Suru verb, intrans." from appearing in dict results, may want to improve this
                        } else if (typeElement.contains("Kuru verb")) {
                            if (matchingWordType.contains("intransitive")) typesAsLegend.add("VkuruI");
                            if (matchingWordType.contains("Transitive")) typesAsLegend.add("VkuruT");
                            else typesAsLegend.add("Vkuru");
                        }
                    }
                    matchingWordType = OverridableUtilitiesGeneral.joinList(Globals.DB_ELEMENTS_DELIMITER, typesAsLegend);
                }
                wordMeaning.setType(matchingWordType);

                //Getting the Opposite value
                String matchingWordOpposite = ""; //TODO: See if this can be extracted from the site
                wordMeaning.setAntonym(matchingWordOpposite);

                //Getting the Synonym value
                String matchingWordSynonym = ""; //TODO: See if this can be extracted from the site
                wordMeaning.setSynonym(matchingWordSynonym);

                //Getting the set of Explanations
                List<Word.Meaning.Explanation> explanationsList = new ArrayList<>();
                Word.Meaning.Explanation explanation = new Word.Meaning.Explanation();

                //Getting the Explanation value
                String matchingWordExplanation = "";
                explanation.setExplanation(matchingWordExplanation);

                //Getting the Rules value
                String matchingWordRules = "";
                explanation.setRules(matchingWordRules);

                //Getting the examples
                List<Word.Meaning.Explanation.Example> examplesList = new ArrayList<>();
                explanation.setExamples(examplesList);

                explanationsList.add(explanation);

                wordMeaning.setExplanations(explanationsList);
                wordMeaningsList.add(wordMeaning);
            }

            currentWord.setMeaningsEN(wordMeaningsList);
            //endregion

            wordsList.add(currentWord);
        }

        return wordsList;
    }

    @Nullable
    private static Object getElementAtHeader(@NotNull List<Object> list, String header) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (i % 2 == 0 && ((String) list.get(i)).contains(header)) return list.get(i + 1);
        }
        return null;
    }

    @NotNull
    private static String reformatMeanings(@NotNull String meaningsOriginal) {

        String meanings_commas = meaningsOriginal.replace(Globals.DB_ELEMENTS_DELIMITER, ",");
        meanings_commas = OverridableUtilitiesGeneral.fromHtml(meanings_commas).toString();
        meanings_commas = meanings_commas.replaceAll("',", "'");
        meanings_commas = meanings_commas.replaceAll("\",", "\"");
        meanings_commas = meanings_commas.replaceAll(",0", "'0"); //Fixes number display problems
        return meanings_commas;
    }

    @NotNull
    public static String createQueryOnJMDict(String word) {
        //inspired by: https://stackoverflow.com/questions/38220828/an-htmlunit-alternative-for-android
        //inspired by: https://stackoverflow.com/questions/15805771/submit-form-using-httpurlconnection
        //inspired by: https://stackoverflow.com/questions/9767952/how-to-add-parameters-to-httpurlconnection-using-post-using-namevaluepair

        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL("https://www.edrdg.org/cgi-bin/wwwjdic/wwwjdic?HF");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));

                String request = URLEncoder.encode("NAME", "UTF-8") + "=" + URLEncoder.encode("dsrchkey", "UTF-8") +
                        "&" + URLEncoder.encode("VALUE", "UTF-8") + "=" + URLEncoder.encode(word, "UTF-8") +
                        "&" + URLEncoder.encode("NAME", "UTF-8") + "=" + URLEncoder.encode("dicsel", "UTF-8") +
                        "&" + URLEncoder.encode("SELECTED VALUE", "UTF-8") + "=" + URLEncoder.encode("H", "UTF-8");
                writer.write(request);

                writer.flush();
                writer.close();
                os.close();
            }
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            } else {
                response = new StringBuilder();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }
}
