package com.japagram.utilitiesCrossPlatform;

import android.os.Build;

import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.data.KanjiCharacter;
import com.japagram.data.KanjiComponent;
import com.japagram.data.RoomKanjiDatabase;
import com.japagram.utilitiesPlatformOverridable.OvUtilsGeneral;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UtilitiesKanjiSearch {
    private static @NotNull List<String> getSimilarComponents(String component, @NotNull List<String[]> mSimilarsDatabase) {
        List<String> similarComponents = new ArrayList<>();
        similarComponents.add(component);
        for (int i=0; i < mSimilarsDatabase.size(); i++) {
            if (mSimilarsDatabase.get(i)[1].equals(component)) {
                similarComponents.add(mSimilarsDatabase.get(i)[0]);
            }
        }
        return similarComponents;
    }

    public static @NotNull Object[] findKanjis(String @NotNull [] elements_list, List<String[]> mSimilarsDatabase, int mSelectedStructure, RoomKanjiDatabase mRoomKanjiDatabase, boolean showOnlyJapCharacters) {
        int mSearchInfoMessage = Globals.KANJI_SEARCH_RESULT_DEFAULT;

        //region Replacing similar elements and initializing
        for (int j=0; j<elements_list.length; j++) {
            if (!elements_list[j].equals("")) {
                for (int i=0; i<mSimilarsDatabase.size(); i++) {
                    if (elements_list[j].equals(mSimilarsDatabase.get(i)[0])) {
                        elements_list[j] = mSimilarsDatabase.get(i)[1];
                        break;
                    }
                }
            }
        }

        String elementA = elements_list[0];
        String elementB = elements_list[1];
        String elementC = elements_list[2];
        String elementD = elements_list[3];

        if (    (mSelectedStructure == Globals.INDEX_FULL
                || mSelectedStructure == Globals.INDEX_ACROSS_2
                || mSelectedStructure == Globals.INDEX_DOWN_2
                || mSelectedStructure == Globals.INDEX_ACROSS_3
                || mSelectedStructure == Globals.INDEX_DOWN_3)
                && (elementA.equals("") && elementB.equals("") && elementC.equals("") && elementD.equals(""))) {
            mSearchInfoMessage = Globals.KANJI_SEARCH_RESULT_DEFAULT;
            return new Object[]{new ArrayList<>(), mSearchInfoMessage};
        }
        //endregion

        //region Finding the list of matches in the Full components list, that correspond to the user's input
        List<KanjiComponent.AssociatedComponent> associatedComponents = null;
        List<KanjiComponent> kanjiComponentsFull1 = mRoomKanjiDatabase.getKanjiComponentsByStructureName("full1");
        List<KanjiComponent> kanjiComponentsFull2 = mRoomKanjiDatabase.getKanjiComponentsByStructureName("full2");
        if (kanjiComponentsFull1 != null && kanjiComponentsFull1.size() > 0) {
            associatedComponents = kanjiComponentsFull1.get(0).getAssociatedComponents();
        }
        if (associatedComponents != null && kanjiComponentsFull2 != null && kanjiComponentsFull2.size() > 0) {
            associatedComponents.addAll(kanjiComponentsFull2.get(0).getAssociatedComponents());
        }
        if (associatedComponents == null) return new Object[]{new ArrayList<>(), mSearchInfoMessage};

        elementA = UtilitiesGeneral.removeSpecialCharacters(elementA);
        elementB = UtilitiesGeneral.removeSpecialCharacters(elementB);
        elementC = UtilitiesGeneral.removeSpecialCharacters(elementC);
        elementD = UtilitiesGeneral.removeSpecialCharacters(elementD);
        boolean elementAisEmpty = elementA.equals("");
        boolean elementBisEmpty = elementB.equals("");
        boolean elementCisEmpty = elementC.equals("");
        boolean elementDisEmpty = elementD.equals("");
        List<String> listOfMatchingResultsElementA = new ArrayList<>();
        List<String> listOfMatchingResultsElementB = new ArrayList<>();
        List<String> listOfMatchingResultsElementC = new ArrayList<>();
        List<String> listOfMatchingResultsElementD = new ArrayList<>();

        if (elementAisEmpty || elementBisEmpty || elementCisEmpty || elementDisEmpty) {
            List<String> listOfAllKanjis = mRoomKanjiDatabase.getAllKanjis(showOnlyJapCharacters);
            if (elementAisEmpty) listOfMatchingResultsElementA = listOfAllKanjis;
            if (elementBisEmpty) listOfMatchingResultsElementB = listOfAllKanjis;
            if (elementCisEmpty) listOfMatchingResultsElementC = listOfAllKanjis;
            if (elementDisEmpty) listOfMatchingResultsElementD = listOfAllKanjis;
        }

        if (!elementAisEmpty) {
            for (String component : getSimilarComponents(elementA, mSimilarsDatabase)) {
                for (KanjiComponent.AssociatedComponent associatedComponent : associatedComponents) {
                    if (associatedComponent.getComponent().equals(component)) {
                        listOfMatchingResultsElementA.addAll(Arrays.asList(associatedComponent.getAssociatedComponents().split(Globals.KANJI_ASSOCIATED_COMPONENTS_DELIMITER)));
                    }
                }
            }
        }
        if (!elementBisEmpty) {
            for (String component : getSimilarComponents(elementB, mSimilarsDatabase)) {
                for (KanjiComponent.AssociatedComponent associatedComponent : associatedComponents) {
                    if (associatedComponent.getComponent().equals(component)) {
                        listOfMatchingResultsElementB.addAll(Arrays.asList(associatedComponent.getAssociatedComponents().split(Globals.KANJI_ASSOCIATED_COMPONENTS_DELIMITER)));
                    }
                }
            }
        }
        if (!elementCisEmpty) {
            for (String component : getSimilarComponents(elementC, mSimilarsDatabase)) {
                for (KanjiComponent.AssociatedComponent associatedComponent : associatedComponents) {
                    if (associatedComponent.getComponent().equals(component)) {
                        listOfMatchingResultsElementC.addAll(Arrays.asList(associatedComponent.getAssociatedComponents().split(Globals.KANJI_ASSOCIATED_COMPONENTS_DELIMITER)));
                    }
                }
            }
        }
        if (!elementDisEmpty) {
            for (String component : getSimilarComponents(elementD, mSimilarsDatabase)) {
                for (KanjiComponent.AssociatedComponent associatedComponent : associatedComponents) {
                    if (associatedComponent.getComponent().equals(component)) {
                        listOfMatchingResultsElementD.addAll(Arrays.asList(associatedComponent.getAssociatedComponents().split(Globals.KANJI_ASSOCIATED_COMPONENTS_DELIMITER)));
                    }
                }
            }
        }
        //endregion

        //region Getting the match intersections in the Full list
        List<String> listOfIntersectingResults = new ArrayList<>();
        if      ( elementAisEmpty &&  elementBisEmpty &&  elementCisEmpty &&  elementDisEmpty) {
            listOfIntersectingResults.addAll(listOfMatchingResultsElementA);
        }
        else if ( elementAisEmpty &&  elementBisEmpty &&  elementCisEmpty && !elementDisEmpty) {
            listOfIntersectingResults.addAll(listOfMatchingResultsElementD);
        }
        else if ( elementAisEmpty &&  elementBisEmpty && !elementCisEmpty &&  elementCisEmpty) {
            listOfIntersectingResults.addAll(listOfMatchingResultsElementC);
        }
        else if ( elementAisEmpty &&  elementBisEmpty && !elementCisEmpty && !elementDisEmpty) {
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfMatchingResultsElementC, listOfMatchingResultsElementD);
        }
        else if ( elementAisEmpty && !elementBisEmpty &&  elementCisEmpty &&  elementDisEmpty) {
            listOfIntersectingResults.addAll(listOfMatchingResultsElementB);
        }
        else if ( elementAisEmpty && !elementBisEmpty &&  elementCisEmpty && !elementDisEmpty) {
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfMatchingResultsElementB, listOfMatchingResultsElementD);
        }
        else if ( elementAisEmpty && !elementBisEmpty && !elementCisEmpty &&  elementDisEmpty) {
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfMatchingResultsElementB, listOfMatchingResultsElementC);
        }
        else if ( elementAisEmpty && !elementBisEmpty && !elementCisEmpty && !elementDisEmpty) {
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfMatchingResultsElementB, listOfMatchingResultsElementC);
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfIntersectingResults, listOfMatchingResultsElementD);
        }
        else if (!elementAisEmpty &&  elementBisEmpty &&  elementCisEmpty &&  elementDisEmpty) {
            listOfIntersectingResults.addAll(listOfMatchingResultsElementA);
        }
        else if (!elementAisEmpty &&  elementBisEmpty &&  elementCisEmpty && !elementDisEmpty) {
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementD);
        }
        else if (!elementAisEmpty &&  elementBisEmpty && !elementCisEmpty &&  elementDisEmpty) {
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementC);
        }
        else if (!elementAisEmpty &&  elementBisEmpty && !elementCisEmpty && !elementDisEmpty) {
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementC);
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfIntersectingResults, listOfMatchingResultsElementD);
        }
        else if (!elementAisEmpty && !elementBisEmpty &&  elementCisEmpty &&  elementDisEmpty) {
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementB);
        }
        else if (!elementAisEmpty && !elementBisEmpty &&  elementCisEmpty && !elementDisEmpty) {
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementB);
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfIntersectingResults, listOfMatchingResultsElementD);
        }
        else if (!elementAisEmpty && !elementBisEmpty && !elementCisEmpty &&  elementDisEmpty) {
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementB);
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfIntersectingResults, listOfMatchingResultsElementC);
        }
        else if (!elementAisEmpty && !elementBisEmpty && !elementCisEmpty && !elementDisEmpty) {
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementB);
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfIntersectingResults, listOfMatchingResultsElementC);
            listOfIntersectingResults = UtilitiesGeneral.getIntersectionOfLists(listOfIntersectingResults, listOfMatchingResultsElementD);
        }
        //endregion

        //region Getting the subset of characters that match the user's selected structure
        List<String> listOfResultsRelevantToRequestedStructure = new ArrayList<>();
        if (mSelectedStructure != Globals.INDEX_FULL) {

            //Getting the components list relevant to the requested structure
            KanjiComponent kanjiComponentForRequestedStructure = null;
            String componentStructure = Globals.COMPONENT_STRUCTURES_MAP.get(mSelectedStructure);
            if (!OvUtilsGeneral.isEmptyString(componentStructure)) {
                List<KanjiComponent> kanjiComponents = mRoomKanjiDatabase.getKanjiComponentsByStructureName(componentStructure);
                if (kanjiComponents != null && kanjiComponents.size() > 0) {
                    kanjiComponentForRequestedStructure = kanjiComponents.get(0);
                    associatedComponents = kanjiComponentForRequestedStructure.getAssociatedComponents();
                }
            }
            if (kanjiComponentForRequestedStructure==null || associatedComponents==null) return new Object[]{new ArrayList<>(), mSearchInfoMessage};

            //Looping over all the structure's components and adding only the ones that appear in listOfIntersectingResults
            List<String> structureComponents;
            List<String> currentIntersections;
            for (KanjiComponent.AssociatedComponent associatedComponent : associatedComponents) {
                structureComponents = Arrays.asList(associatedComponent.getAssociatedComponents().split(Globals.KANJI_ASSOCIATED_COMPONENTS_DELIMITER));
                currentIntersections = UtilitiesGeneral.getIntersectionOfLists(listOfIntersectingResults, structureComponents);
                listOfResultsRelevantToRequestedStructure.addAll(currentIntersections);
            }
            listOfResultsRelevantToRequestedStructure = UtilitiesGeneral.removeDuplicatesFromStringList(listOfResultsRelevantToRequestedStructure);

        }
        else {
            listOfResultsRelevantToRequestedStructure = listOfIntersectingResults;
        }
        //endregion

        //region If relevant, filtering the list for characters used only in Japanese
        List<String> finalList = new ArrayList<>();
        int size = listOfResultsRelevantToRequestedStructure.size();
        int CHUNK_SIZE = 400;
        if (size == 0) mSearchInfoMessage = Globals.KANJI_SEARCH_RESULT_NO_RESULTS;
        if (showOnlyJapCharacters) {
            //Splitting the results into chunks to prevent SQL overload
            List<List<String>> chunks = new ArrayList<>();
            int numChunks = size / CHUNK_SIZE + ((size % CHUNK_SIZE == 0)? 0: 1);
            for (int i=0; i<numChunks; i++) {
                int minIndex = i*CHUNK_SIZE;
                int maxIndex = (i+1)*CHUNK_SIZE;
                if (maxIndex > size) maxIndex = size;
                List<String> chunk = listOfResultsRelevantToRequestedStructure.subList(minIndex, maxIndex);
                List<String> hexIds = new ArrayList<>();
                for (String character : chunk) {
                    if (!character.equals("")) hexIds.add(OvUtilsGeneral.getHexId(character));
                }
                chunks.add(hexIds);
            }
            List<String> nonJapChars = new ArrayList<>();
            for (List<String> hexIds : chunks) {
                List<KanjiCharacter> chars = mRoomKanjiDatabase.getKanjiCharactersByHexIdList(hexIds);
                for (KanjiCharacter character : chars) {
                    if (character.getUsedInJapanese() == 1) finalList.add(character.getKanji());
                    else nonJapChars.add(character.getKanji());
                }
            }

            if (finalList.size() == 0 && size > 0) {
                List<String> nonJapCharsPrintable = new ArrayList<>();
                String value;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    for (int i = 0; i < nonJapChars.size(); i++) {
                        value = nonJapChars.get(i);
                        if (value.length()>0 && AndroidUtilitiesIO.isPrintable(value.substring(0, 1))) {
                            nonJapCharsPrintable.add(nonJapChars.get(i));
                        }
                    }
                }
                else {
                    nonJapCharsPrintable = nonJapChars;
                }
                mSearchInfoMessage = (nonJapCharsPrintable.size() > 0)? Globals.KANJI_SEARCH_RESULT_NO_JAP_RESULTS :  Globals.KANJI_SEARCH_RESULT_NO_JAP_NO_PRINTABLE_RESULTS;
            }
        } else {
            finalList = listOfResultsRelevantToRequestedStructure;
        }
        return new Object[]{finalList, mSearchInfoMessage};
    }
}
