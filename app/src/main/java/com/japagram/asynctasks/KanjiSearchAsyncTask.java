package com.japagram.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.japagram.data.RoomKanjiDatabase;
import com.japagram.data.KanjiComponent;
import com.japagram.resources.GlobalConstants;
import com.japagram.resources.Utilities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KanjiSearchAsyncTask extends AsyncTask<Void, Void, Object[]> {

    //region Parameters
    private final String[] elements_list;
    private final int mSelectedStructure;
    private final List<String[]> mSimilarsDatabase;
    private RoomKanjiDatabase mRoomKanjiDatabase;
    private boolean mSearchTooBroad;
    private WeakReference<Context> contextRef;
    //endregion
    public KanjiSearchAsyncResponseHandler listener;

    public KanjiSearchAsyncTask(Context context, String[] elements_list, int mSelectedStructure, List<String[]> mSimilarsDatabase, KanjiSearchAsyncResponseHandler listener) {
        contextRef = new WeakReference<>(context);
        this.elements_list = elements_list;
        this.mSelectedStructure = mSelectedStructure;
        this.mSimilarsDatabase = mSimilarsDatabase;
        this.listener = listener;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected Object[] doInBackground(Void... voids) {

        mRoomKanjiDatabase = RoomKanjiDatabase.getInstance(contextRef.get());
        List<String> result = findSearchResults();

        return new Object[] {result, mSearchTooBroad};
    }

    @Override
    protected void onPostExecute(Object[] data) {
        super.onPostExecute(data);
        listener.onKanjiSearchAsyncTaskResultsFound(data);
    }

    public interface KanjiSearchAsyncResponseHandler {
        void onKanjiSearchAsyncTaskResultsFound(Object[] data);
    }

    private List<String> findSearchResults() {

        //region Initialization
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

        if (    (mSelectedStructure == GlobalConstants.Index_full
                || mSelectedStructure == GlobalConstants.Index_across2
                || mSelectedStructure == GlobalConstants.Index_down2
                || mSelectedStructure == GlobalConstants.Index_across3
                || mSelectedStructure == GlobalConstants.Index_down3)
                && (elementA.equals("") && elementB.equals("") && elementC.equals("") && elementD.equals(""))) {
            mSearchTooBroad = true;
            return new ArrayList<>();
        }
        //endregion

        //region Finding the list of matches in the Full components list, that correspond to the user's input
        List<KanjiComponent.AssociatedComponent> associatedComponents = null;
        List<KanjiComponent> kanjiComponentsFull1 = mRoomKanjiDatabase.getKanjiComponentsByStructureName("full1");
        List<KanjiComponent> kanjiComponentsFull2 = mRoomKanjiDatabase.getKanjiComponentsByStructureName("full2");
        if (kanjiComponentsFull1 != null && kanjiComponentsFull1.size() > 0) {
            associatedComponents = kanjiComponentsFull1.get(0).getAssociatedComponents();
        }
        if (kanjiComponentsFull2 != null && kanjiComponentsFull2.size() > 0) {
            associatedComponents.addAll(kanjiComponentsFull2.get(0).getAssociatedComponents());
        }

        elementA = Utilities.removeSpecialCharacters(elementA);
        elementB = Utilities.removeSpecialCharacters(elementB);
        elementC = Utilities.removeSpecialCharacters(elementC);
        elementD = Utilities.removeSpecialCharacters(elementD);
        boolean checkForExactMatchesOfElementA = !elementA.equals("");
        boolean checkForExactMatchesOfElementB = !elementB.equals("");
        boolean checkForExactMatchesOfElementC = !elementC.equals("");
        boolean checkForExactMatchesOfElementD = !elementD.equals("");
        List<String> listOfMatchingResultsElementA = new ArrayList<>();
        List<String> listOfMatchingResultsElementB = new ArrayList<>();
        List<String> listOfMatchingResultsElementC = new ArrayList<>();
        List<String> listOfMatchingResultsElementD = new ArrayList<>();

        if (!checkForExactMatchesOfElementA || !checkForExactMatchesOfElementB || !checkForExactMatchesOfElementC || !checkForExactMatchesOfElementD) {
            List<String> listOfAllKanjis = mRoomKanjiDatabase.getAllKanjis();
            if (!checkForExactMatchesOfElementA) listOfMatchingResultsElementA = listOfAllKanjis;
            if (!checkForExactMatchesOfElementB) listOfMatchingResultsElementB = listOfAllKanjis;
            if (!checkForExactMatchesOfElementC) listOfMatchingResultsElementC = listOfAllKanjis;
            if (!checkForExactMatchesOfElementD) listOfMatchingResultsElementD = listOfAllKanjis;
        }

        for (KanjiComponent.AssociatedComponent associatedComponent : associatedComponents) {
            if (checkForExactMatchesOfElementA && associatedComponent.getComponent().equals(elementA)) {
                listOfMatchingResultsElementA = Arrays.asList(associatedComponent.getAssociatedComponents().split(GlobalConstants.KANJI_ASSOCIATED_COMPONENTS_DELIMITER));
                checkForExactMatchesOfElementA = false;
            }
            if (checkForExactMatchesOfElementB && associatedComponent.getComponent().equals(elementB)) {
                listOfMatchingResultsElementB = Arrays.asList(associatedComponent.getAssociatedComponents().split(GlobalConstants.KANJI_ASSOCIATED_COMPONENTS_DELIMITER));
                checkForExactMatchesOfElementB = false;
            }
            if (checkForExactMatchesOfElementC && associatedComponent.getComponent().equals(elementC)) {
                listOfMatchingResultsElementC = Arrays.asList(associatedComponent.getAssociatedComponents().split(GlobalConstants.KANJI_ASSOCIATED_COMPONENTS_DELIMITER));
                checkForExactMatchesOfElementC = false;
            }
            if (checkForExactMatchesOfElementD && associatedComponent.getComponent().equals(elementD)) {
                listOfMatchingResultsElementD = Arrays.asList(associatedComponent.getAssociatedComponents().split(GlobalConstants.KANJI_ASSOCIATED_COMPONENTS_DELIMITER));
                checkForExactMatchesOfElementD = false;
            }
            if (!checkForExactMatchesOfElementA && !checkForExactMatchesOfElementB &&!checkForExactMatchesOfElementC && !checkForExactMatchesOfElementD) break;
        }
        //endregion

        //region Getting the match intersections in the Full list
        List<String> listOfIntersectingResults = new ArrayList<>();
        if      ( elementA.equals("") &&  elementB.equals("") &&  elementC.equals("") &&  elementD.equals("")) {
            listOfIntersectingResults.addAll(listOfMatchingResultsElementA);
        }
        else if ( elementA.equals("") &&  elementB.equals("") &&  elementC.equals("") && !elementD.equals("")) {
            listOfIntersectingResults.addAll(listOfMatchingResultsElementD);
        }
        else if ( elementA.equals("") &&  elementB.equals("") && !elementC.equals("") &&  elementC.equals("")) {
            listOfIntersectingResults.addAll(listOfMatchingResultsElementC);
        }
        else if ( elementA.equals("") &&  elementB.equals("") && !elementC.equals("") && !elementD.equals("")) {
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfMatchingResultsElementC, listOfMatchingResultsElementD);
        }
        else if ( elementA.equals("") && !elementB.equals("") &&  elementC.equals("") &&  elementD.equals("")) {
            listOfIntersectingResults.addAll(listOfMatchingResultsElementB);
        }
        else if ( elementA.equals("") && !elementB.equals("") &&  elementC.equals("") && !elementD.equals("")) {
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfMatchingResultsElementB, listOfMatchingResultsElementD);
        }
        else if ( elementA.equals("") && !elementB.equals("") && !elementC.equals("") &&  elementD.equals("")) {
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfMatchingResultsElementB, listOfMatchingResultsElementC);
        }
        else if ( elementA.equals("") && !elementB.equals("") && !elementC.equals("") && !elementD.equals("")) {
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfMatchingResultsElementB, listOfMatchingResultsElementC);
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfIntersectingResults, listOfMatchingResultsElementD);
        }
        else if (!elementA.equals("") &&  elementB.equals("") &&  elementC.equals("") &&  elementD.equals("")) {
            listOfIntersectingResults.addAll(listOfMatchingResultsElementA);
        }
        else if (!elementA.equals("") &&  elementB.equals("") &&  elementC.equals("") && !elementD.equals("")) {
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementD);
        }
        else if (!elementA.equals("") &&  elementB.equals("") && !elementC.equals("") &&  elementD.equals("")) {
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementC);
        }
        else if (!elementA.equals("") &&  elementB.equals("") && !elementC.equals("") && !elementD.equals("")) {
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementC);
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfIntersectingResults, listOfMatchingResultsElementD);
        }
        else if (!elementA.equals("") && !elementB.equals("") &&  elementC.equals("") &&  elementD.equals("")) {
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementB);
        }
        else if (!elementA.equals("") && !elementB.equals("") &&  elementC.equals("") && !elementD.equals("")) {
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementB);
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfIntersectingResults, listOfMatchingResultsElementD);
        }
        else if (!elementA.equals("") && !elementB.equals("") && !elementC.equals("") &&  elementD.equals("")) {
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementB);
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfIntersectingResults, listOfMatchingResultsElementC);
        }
        else if (!elementA.equals("") && !elementB.equals("") && !elementC.equals("") && !elementD.equals("")) {
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfMatchingResultsElementA, listOfMatchingResultsElementB);
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfIntersectingResults, listOfMatchingResultsElementC);
            listOfIntersectingResults = Utilities.getIntersectionOfLists(listOfIntersectingResults, listOfMatchingResultsElementD);
        }
        //endregion

        //region Getting the subset of characters that match the user's selected structure
        List<String> listOfResultsRelevantToRequestedStructure = new ArrayList<>();
        if (mSelectedStructure != GlobalConstants.Index_full) {

            //Getting the components list relevant to the requested structure
            KanjiComponent kanjiComponentForRequestedStructure = null;
            String componentStructure = GlobalConstants.COMPONENT_STRUCTURES_MAP.get(mSelectedStructure);
            if (!TextUtils.isEmpty(componentStructure)) {
                List<KanjiComponent> kanjiComponents = mRoomKanjiDatabase.getKanjiComponentsByStructureName(componentStructure);
                if (kanjiComponents != null && kanjiComponents.size() > 0) {
                    kanjiComponentForRequestedStructure = kanjiComponents.get(0);
                    associatedComponents = kanjiComponentForRequestedStructure.getAssociatedComponents();
                }
            }
            if (kanjiComponentForRequestedStructure==null || associatedComponents==null) return new ArrayList<>();

            //Looping over all the structure's components and adding only the ones that appear in listOfIntersectingResults
            List<String> structureComponents;
            List<String> currentIntersections;
            for (KanjiComponent.AssociatedComponent associatedComponent : associatedComponents) {
                structureComponents = Arrays.asList(associatedComponent.getAssociatedComponents().split(GlobalConstants.KANJI_ASSOCIATED_COMPONENTS_DELIMITER));
                currentIntersections = Utilities.getIntersectionOfLists(listOfIntersectingResults, structureComponents);
                listOfResultsRelevantToRequestedStructure.addAll(currentIntersections);
            }
            listOfResultsRelevantToRequestedStructure = Utilities.removeDuplicatesFromList(listOfResultsRelevantToRequestedStructure);

        }
        else {
            listOfResultsRelevantToRequestedStructure = listOfIntersectingResults;
        }
        //endregion

        return listOfResultsRelevantToRequestedStructure;
    }
}
