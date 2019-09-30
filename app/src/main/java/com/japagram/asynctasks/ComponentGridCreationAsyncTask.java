package com.japagram.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.japagram.data.RoomKanjiDatabase;
import com.japagram.data.KanjiComponent;
import com.japagram.resources.GlobalConstants;
import com.japagram.resources.Utilities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ComponentGridCreationAsyncTask extends AsyncTask<Void, Void, List<String>> {

    private static final int CHARACTER_RESULTS_GRID_MAX_NUMBER = 400;
    //region Parameters
    private final String mComponentSelectionType;
    private final List<String[]> mRadicalsOnlyDatabase;
    private final int chosen_components_list;
    private List<String[]> sortedList;
    private RoomKanjiDatabase mRoomKanjiDatabase;
    //endregion
    private WeakReference<Context> contextRef;
    public ComponentGridCreationAsyncResponseHandler listener;

    public ComponentGridCreationAsyncTask(Context context,
                                          String mComponentSelectionType,
                                          List<String[]> mRadicalsOnlyDatabase,
                                          int chosen_components_list, ComponentGridCreationAsyncResponseHandler listener) {
        contextRef = new WeakReference<>(context);
        this.mComponentSelectionType = mComponentSelectionType;
        this.mRadicalsOnlyDatabase = mRadicalsOnlyDatabase;
        this.chosen_components_list = chosen_components_list;
        this.listener = listener;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected List<String> doInBackground(Void... voids) {

        mRoomKanjiDatabase = RoomKanjiDatabase.getInstance(contextRef.get());
        List<String> displayableComponentSelections = getSelectionGridElements();

        return displayableComponentSelections;
    }

    @Override
    protected void onPostExecute(List<String> words) {
        super.onPostExecute(words);
        listener.onComponentGridCreationAsyncTaskDone(words);
    }

    public interface ComponentGridCreationAsyncResponseHandler {
        void onComponentGridCreationAsyncTaskDone(List<String> text);
    }

    private List<String> getSelectionGridElements() {

        if (mComponentSelectionType.equals("")) return new ArrayList<>();

        //Creating the the list to be displayed in the dialog grid
        List<String> selections = new ArrayList<>();
        String[] currentElement;

        //region If the user clicks on the radical button, then a radical selection grid is shown
        if (mComponentSelectionType.contains("radical")) {

            String[] parsed_list;
            String last_index = "0";
            for (int i = 0; i < mRadicalsOnlyDatabase.size(); i++) {
                currentElement = mRadicalsOnlyDatabase.get(i);
                parsed_list = currentElement[GlobalConstants.RADICAL_NUM].split(";");

                //Skipping radicals not found in the CJK decompositons database
                if (parsed_list.length == 3 && parsed_list[2].equals("not found in decompositions")) continue;

                // Adding the header radical numbers to the list of radicals
                if (!currentElement[GlobalConstants.RADICAL_NUM_STROKES].equals(last_index)) {
                    if (parsed_list[0].equals("Special") || parsed_list[0].equals("Hiragana") || parsed_list[0].equals("Katakana")) {
                    } else if (parsed_list.length > 1) {
                        if (parsed_list[1].equals("variant")) {
                            selections.add(currentElement[GlobalConstants.RADICAL_NUM_STROKES]);
                            selections.add(currentElement[GlobalConstants.RADICAL_KANA] + "variant");
                            last_index = currentElement[GlobalConstants.RADICAL_NUM_STROKES];
                        }
                    } else {
                        selections.add(currentElement[GlobalConstants.RADICAL_NUM_STROKES]);
                        selections.add(currentElement[GlobalConstants.RADICAL_KANA]);
                        last_index = currentElement[GlobalConstants.RADICAL_NUM_STROKES];
                    }
                } else if (currentElement[GlobalConstants.RADICAL_NUM_STROKES].equals(last_index)) {
                    if (parsed_list.length > 1) {
                        if (parsed_list[1].equals("variant")) {
                            selections.add(currentElement[GlobalConstants.RADICAL_KANA] + "variant");
                        }
                    } else {
                        selections.add(currentElement[GlobalConstants.RADICAL_KANA]);
                    }
                    last_index = currentElement[GlobalConstants.RADICAL_NUM_STROKES];
                }
            }
        }
        //endregion

        //region If the user clicks on the component button, then then the components grid is extracted from Room
        else {
            List<String[]> fullList = new ArrayList<>();
            String[] printableResultsForCurrentElement;
            boolean containsAtLeastOnePrintableGlyph;

            //List<KanjiComponent> kanjiComponents = mJapaneseToolboxRoomDatabase.getAllKanjiComponents();
            String componentStructure = GlobalConstants.COMPONENT_STRUCTURES_MAP.get(chosen_components_list);
            if (!TextUtils.isEmpty(componentStructure)) {

                List<KanjiComponent> kanjiComponents = mRoomKanjiDatabase.getKanjiComponentsByStructureName(componentStructure);
                if (kanjiComponents != null && kanjiComponents.size()>0) {
                    KanjiComponent kanjiComponent = kanjiComponents.get(0);
                    List<KanjiComponent.AssociatedComponent> associatedComponents = kanjiComponent.getAssociatedComponents();

                    if (componentStructure.equals("full") && kanjiComponents.size()==2) {
                        associatedComponents.addAll(kanjiComponents.get(1).getAssociatedComponents());
                    }

                    for (int i = 0; i < associatedComponents.size(); i++) {
                        currentElement = new String[2];
                        currentElement[0] = associatedComponents.get(i).getComponent();
                        currentElement[1] = Integer.toString(associatedComponents.get(i).getAssociatedComponents().length());

                        containsAtLeastOnePrintableGlyph = true;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            printableResultsForCurrentElement = associatedComponents.get(i).getAssociatedComponents().split(GlobalConstants.KANJI_ASSOCIATED_COMPONENTS_DELIMITER);
                            for (String aPrintableResultsForCurrentElement : printableResultsForCurrentElement) {
                                containsAtLeastOnePrintableGlyph = false;
                                if (aPrintableResultsForCurrentElement.length() > 0 && Utilities.isPrintable(aPrintableResultsForCurrentElement.substring(0, 1))) {
                                    containsAtLeastOnePrintableGlyph = true;
                                    break;
                                }
                            }
                        }

                        if (containsAtLeastOnePrintableGlyph) { fullList.add(currentElement);}
                    }

                }
            }

            sortedList = sortAccordingToGrowingFrequency(fullList); //Output list is in order of growing frequency, next for loop inverts this
            //sorted_list = fullList;
            for (int i = 0; i< sortedList.size(); i++) {
                selections.add(sortedList.get(sortedList.size()-1-i)[0]);
            }
        }
        //endregion

        //region Displaying only the search results that have a glyph in the font
        List<String> displayableComponentSelections = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < selections.size(); i++) {
                if (selections.get(i).length() > 0 && Utilities.isPrintable(selections.get(i).substring(0, 1))) {
                    displayableComponentSelections.add(selections.get(i));
                }
            }
        }
        else {
            displayableComponentSelections = selections;
        }
        //endregion

        //region Displaying only the first XX values to prevent overload
        selections = new ArrayList<>();
        for (int i = 0; i < displayableComponentSelections.size(); i++) {
            selections.add(displayableComponentSelections.get(i));
            if (i>CHARACTER_RESULTS_GRID_MAX_NUMBER) {break;}
        }
        displayableComponentSelections = selections;
        //endregion


        return displayableComponentSelections;
    }

    // QuickSort Algorithm (adapted from http://www.vogella.com/tutorials/JavaAlgorithmsQuicksort/article.html)
    private List<String[]> sortAccordingToGrowingFrequency(List<String[]> values) {
        // check for empty or null array
        if (values ==null || values.size()==0){
            return new ArrayList<>();
        }
        this.sortedList = values;
        quicksort(0, values.size() - 1);
        return sortedList;
    }
    private void quicksort(int low, int high) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        int pivot = Integer.parseInt(sortedList.get(low + (high-low)/2)[1]);

        // Divide into component_substructures[2] lists
        while (i <= j) {
            // If the current value from the left list is smaller then the pivot
            // element then get the next element from the left list
            while (Integer.parseInt(sortedList.get(i)[1]) < pivot) {
                i++;
            }
            // If the current value from the right list is larger then the pivot
            // element then get the next element from the right list
            while (Integer.parseInt(sortedList.get(j)[1]) > pivot) {
                j--;
            }

            // If we have found a values in the left list which is larger then
            // the pivot element and if we have found a value in the right list
            // which is smaller then the pivot element then we exchange the
            // values.
            // As we are done we can increase i and j
            if (i <= j) {
                exchange(i, j);
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j)
            quicksort(low, j);
        if (i < high)
            quicksort(i, high);
    }
    private void exchange(int i, int j) {
        String[] value_to_i = {sortedList.get(j)[0], sortedList.get(j)[1]};
        String[] value_to_j = {sortedList.get(i)[0], sortedList.get(i)[1]};
        sortedList.set(i, value_to_i);
        sortedList.set(j, value_to_j);
    }
}
