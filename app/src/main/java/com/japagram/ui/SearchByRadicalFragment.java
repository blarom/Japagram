package com.japagram.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.japagram.R;
import com.japagram.adapters.KanjiGridRecyclerViewAdapter;
import com.japagram.adapters.StructuresGridViewAdapter;
import com.japagram.asynctasks.ComponentGridCreationAsyncTask;
import com.japagram.asynctasks.ComponentsGridFilterAsyncTask;
import com.japagram.asynctasks.KanjiSearchAsyncTask;
import com.japagram.databinding.FragmentSearchByRadicalBodyBinding;
import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesCrossPlatform.UtilitiesGeneral;
import com.japagram.utilitiesCrossPlatform.UtilitiesQuery;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

public class SearchByRadicalFragment extends Fragment implements
        KanjiGridRecyclerViewAdapter.ComponentClickHandler,
        KanjiSearchAsyncTask.KanjiSearchAsyncResponseHandler,
        ComponentGridCreationAsyncTask.ComponentGridCreationAsyncResponseHandler,
        ComponentsGridFilterAsyncTask.ComponentsGridFilterAsyncResponseHandler {


    //region Parameters
    private FragmentSearchByRadicalBodyBinding binding;
    private static final int MAX_RECYCLERVIEW_HEIGHT_DP = 320;
    private int mSelectedOverallStructure;
    private int mSelectedComponentStructure;
    private String mComponentSelectionType;
    private String[] user_selections;
    private List<String> mDisplayableComponentSelections;
    private String mInputQuery;
    private List<String[]> mRadicalsOnlyDatabase;
    private String mKanjiCharacterNameForFilter;
    private int mNumberOfComponentGridColumns;
    private List<String> mUnfilteredDisplayableComponentSelections;
    private List<String[]> mSimilarsDatabase;
    private int mSelectedEditTextId;
    private int mSelectedOverallStructureId;
    private int mTempSelectedStructureId;
    private List<String> mSearchResultsFinal;
    private KanjiGridRecyclerViewAdapter mComponentsGridAdapter;
    private KanjiGridRecyclerViewAdapter mResultsGridAdapter;
    private int mNumberOfResultGridColumns;
    private String mSelectedComponent;
    private int mMaxRecyclerViewHeightPixels;
    private Typeface mDroidSansJapaneseTypeface;
    private KanjiSearchAsyncTask mKanjiSearchAsyncTask;
    private ComponentGridCreationAsyncTask mComponentGridCreationAsyncTask;
    private ComponentsGridFilterAsyncTask mComponentsGridFilterAsyncTask;
    //endregion


    //Lifecycle methods
    @Override public void onAttach(@NotNull Context context) {
        super.onAttach(context);

        searchByRadicalFragmentOperationsHandler = (SearchByRadicalFragmentOperationsHandler) context;
    }
    @Override public void onCreate(Bundle savedInstanceState) { //instead of onActivityCreated
        super.onCreate(savedInstanceState);

        getExtras();
        initializeParameters();
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_search_by_radical, container, false);
        //setRetainInstance(true);
        return rootView;
    }
    @Override public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentSearchByRadicalBodyBinding.bind(view);
        initializeViews();
        updateInputElements(mInputQuery);
        hideAllSections();
    }
    @Override public void onResume() {
        super.onResume();
        if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
    }
    @Override public void onDetach() {
        super.onDetach();
        cancelAsyncOperations();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        //if (getActivity()!=null && MainApplication.getRefWatcher(getActivity())!=null) MainApplication.getRefWatcher(getActivity()).watch(this);
    }


    // Functionality Functions
    private void getExtras() {
        if (getArguments()!=null) {
            mInputQuery = getArguments().getString(getString(R.string.user_query_word));
            mRadicalsOnlyDatabase = (List<String[]>) getArguments().getSerializable(getString(R.string.rad_only_database));
            mSimilarsDatabase = (List<String[]>) getArguments().getSerializable(getString(R.string.similars_database));
        }
    }
    private void initializeParameters() {
        mSelectedComponent = "";

        //Setting the Typeface
        if (getContext()==null) return;
        AssetManager am = getContext().getApplicationContext().getAssets();
        mDroidSansJapaneseTypeface = AndroidUtilitiesPrefs.getPreferenceUseJapaneseFont(getActivity()) ?
                Typeface.createFromAsset(am, String.format(Locale.JAPAN, "fonts/%s", "DroidSansJapanese.ttf")) : Typeface.DEFAULT;
    }
    private int setCategoryBasedOnSelectedStructureId(int selectedStructureId) {

        if (selectedStructureId == R.drawable.colored_structure_2_overlapping) {
            return Globals.INDEX_FULL;
        } else if (selectedStructureId == R.drawable.colored_structure_2_left_right) {
            return Globals.INDEX_ACROSS_2;
        } else if (selectedStructureId == R.drawable.colored_structure_3_left_center_right) {
            return Globals.INDEX_ACROSS_3;
        } else if (selectedStructureId == R.drawable.colored_structure_4_left_right) {
            return Globals.INDEX_ACROSS_4;
        } else if (selectedStructureId == R.drawable.colored_structure_2_up_down) {
            return Globals.INDEX_DOWN_2;
        } else if (selectedStructureId == R.drawable.colored_structure_3_up_center_down) {
            return Globals.INDEX_DOWN_3;
        } else if (selectedStructureId == R.drawable.colored_structure_4_up_down) {
            return Globals.INDEX_DOWN_4;
        } else if (selectedStructureId == R.drawable.colored_structure_2_enclosing_topleft_to_bottomright) {
            return Globals.INDEX_TOPLEFTOUT;
        } else if (selectedStructureId == R.drawable.colored_structure_2_enclosing_top_to_bottom) {
            return Globals.INDEX_TOPOUT;
        } else if (selectedStructureId == R.drawable.colored_structure_2_enclosing_topright_to_bottomleft) {
            return Globals.INDEX_TOPRIGHTOUT;
        } else if (selectedStructureId == R.drawable.colored_structure_2_enclosing_left_to_right) {
            return Globals.INDEX_LEFTOUT;
        } else if (selectedStructureId == R.drawable.colored_structure_2_outlining) {
            return Globals.INDEX_FULLOUT;
        } else if (selectedStructureId == R.drawable.colored_structure_2_enclosing_bottomleft_to_topright) {
            return Globals.INDEX_BOTTOMLEFTOUT;
        } else if (selectedStructureId == R.drawable.colored_structure_2_enclosing_bottom_to_top) {
            return Globals.INDEX_BOTTOMOUT;
        } else if (selectedStructureId == R.drawable.colored_structure_3_upwards_triangle) {
            return Globals.INDEX_THREE_REPEAT;
        } else if (selectedStructureId == R.drawable.colored_structure_4_square_repeat) {
            return Globals.INDEX_FOUR_REPEAT;
        } else if (selectedStructureId == R.drawable.colored_structure_4_square) {
            return Globals.INDEX_FOURSQUARE;
        } else if (selectedStructureId == R.drawable.colored_structure_5_hourglass) {
            return Globals.INDEX_FIVE_REPEAT;
        }
        return 0;
    }
    private void startCreatingComponentKanjiGridElementsAsynchronously() {
        if (getActivity()!=null) {
            showLoadingIndicator();
            mComponentGridCreationAsyncTask = new ComponentGridCreationAsyncTask(
                    getContext(), mComponentSelectionType, mRadicalsOnlyDatabase, mSelectedComponentStructure, this);
            mComponentGridCreationAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    private void startFilteringComponentKanjiGridElementsAsynchronously() {
        if (getActivity()!=null) {
            showLoadingIndicator();
            mComponentsGridFilterAsyncTask = new ComponentsGridFilterAsyncTask(
                    getContext(), mComponentSelectionType, mRadicalsOnlyDatabase, mKanjiCharacterNameForFilter, mUnfilteredDisplayableComponentSelections, this);
            mComponentsGridFilterAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    private void startSearchingForKanjisAsynchronously(String[] elements_strings) {
        if (getActivity()!=null) {
            showLoadingIndicator();
            boolean getOnlyCharsUsedInJapanese = AndroidUtilitiesPrefs.getAppPreferenceShowOnlyJapCharacters(getActivity());
            mKanjiSearchAsyncTask = new KanjiSearchAsyncTask(getContext(), elements_strings, mSelectedOverallStructure, mSimilarsDatabase, getOnlyCharsUsedInJapanese, this);
            mKanjiSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    private void filterComponentKanjiGridElements() {
        if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
        mKanjiCharacterNameForFilter = binding.searchByRadicalCharacterDescriptor.getText().toString();
        startFilteringComponentKanjiGridElementsAsynchronously();
    }
    private void cancelAsyncOperations() {
        if (mKanjiSearchAsyncTask != null) mKanjiSearchAsyncTask.cancel(true);
        if (mComponentGridCreationAsyncTask != null) mComponentGridCreationAsyncTask.cancel(true);
        if (mComponentsGridFilterAsyncTask != null) mComponentsGridFilterAsyncTask.cancel(true);
    }

    //UI Functions
    private void initializeViews() {

        if (getContext()==null) return;

        binding.searchByRadicalOverallStructureButton.setOnClickListener(view1 -> onRequestedStructureButtonClick());
        binding.searchByRadicalButtonRadical.setOnClickListener(view1 -> onRadicalButtonClick());
        binding.searchByRadicalButtonComponent.setOnClickListener(view1 -> onComponentButtonClick());
        binding.searchByRadicalButtonSearch.setOnClickListener(view1 -> onSearchButtonClick());
        binding.searchByRadicalRequestedComponentStructure.setOnClickListener(view1 -> onRequestedComponentStructureButtonClick());
        binding.searchByRadicalButtonFilter.setOnClickListener(view1 -> onFilterButtonClick());
        binding.searchByRadicalButtonSelectionGridCancelTop.setOnClickListener(view1 -> onCancelTopButtonClick());
        binding.searchByRadicalButtonSelectionGridSendToElementTop.setOnClickListener(view1 -> onSendToElementTopButtonClick());
        binding.searchByRadicalButtonSelectionGridSendToInputTop.setOnClickListener(view1 -> onSendToInputTopButtonClick());
        binding.searchByRadicalButtonSelectionGridCancelBottom.setOnClickListener(view1 -> onCancelBottomButtonClick());
        binding.searchByRadicalButtonSelectionGridSendToElementBottom.setOnClickListener(view1 -> onSendToElementBottomButtonClick());
        binding.searchByRadicalButtonSelectionGridSendToInputBottom.setOnClickListener(view1 -> onSendToInputBottomButtonClick());
        binding.searchByRadicalButtonSelectionGridCancelTop.setOnClickListener(view1 -> onCancelTopButtonClick());
        binding.searchByRadicalButtonSelectionGridCancelTop.setOnClickListener(view1 -> onCancelTopButtonClick());
        binding.searchByRadicalButtonSelectionGridCancelTop.setOnClickListener(view1 -> onCancelTopButtonClick());
        binding.searchByRadicalButtonSelectionGridCancelTop.setOnClickListener(view1 -> onCancelTopButtonClick());
        binding.searchByRadicalButtonSelectionGridCancelTop.setOnClickListener(view1 -> onCancelTopButtonClick());

        //region Setting the Element listeners
        binding.searchByRadicalElementA.setOnFocusChangeListener((v, hasFocus) -> {
            if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
            if (hasFocus) mSelectedEditTextId = binding.searchByRadicalElementA.getId();
            drawBorderAroundThisEditText(binding.searchByRadicalElementA);
        });
        binding.searchByRadicalElementA.setOnClickListener(view -> {
            mSelectedEditTextId = binding.searchByRadicalElementA.getId();
            drawBorderAroundThisEditText(binding.searchByRadicalElementA);
        });
        binding.searchByRadicalElementA.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
            }
            return false;
        });

        binding.searchByRadicalElementB.setOnFocusChangeListener((v, hasFocus) -> {
            if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
            if (hasFocus) mSelectedEditTextId = binding.searchByRadicalElementB.getId();
            drawBorderAroundThisEditText(binding.searchByRadicalElementB);
        });
        binding.searchByRadicalElementB.setOnClickListener(view -> {
            mSelectedEditTextId = binding.searchByRadicalElementB.getId();
            drawBorderAroundThisEditText(binding.searchByRadicalElementB);
        });
        binding.searchByRadicalElementB.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
            }
            return false;
        });

        binding.searchByRadicalElementC.setOnFocusChangeListener((v, hasFocus) -> {
            if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
            if (hasFocus) mSelectedEditTextId = binding.searchByRadicalElementC.getId();
            drawBorderAroundThisEditText(binding.searchByRadicalElementC);
        });
        binding.searchByRadicalElementC.setOnClickListener(view -> {
            mSelectedEditTextId = binding.searchByRadicalElementC.getId();
            drawBorderAroundThisEditText(binding.searchByRadicalElementC);
        });
        binding.searchByRadicalElementC.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
            }
            return false;
        });

        binding.searchByRadicalElementD.setOnFocusChangeListener((v, hasFocus) -> {
            if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
            if (hasFocus) mSelectedEditTextId = binding.searchByRadicalElementD.getId();
            drawBorderAroundThisEditText(binding.searchByRadicalElementD);
        });
        binding.searchByRadicalElementD.setOnClickListener(view -> {
            mSelectedEditTextId = binding.searchByRadicalElementD.getId();
            drawBorderAroundThisEditText(binding.searchByRadicalElementD);
        });
        binding.searchByRadicalElementD.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
            }
            return false;
        });

        mSelectedEditTextId = binding.searchByRadicalElementA.getId();
        drawBorderAroundThisEditText(binding.searchByRadicalElementA);
        //endregion

        //region Setting the number of grid columns
        mNumberOfComponentGridColumns = 7;
        mNumberOfResultGridColumns = 7;
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            if (width<1000) {
                mNumberOfComponentGridColumns = 5;
                mNumberOfResultGridColumns = 5;
            }
        }
        //mSelectionGridView.setNumColumns(mNumberOfComponentGridColumns);
        //endregion

        //region Initializing the component structure value
        mTempSelectedStructureId = R.drawable.colored_structure_2_overlapping;
        mSelectedOverallStructureId = mTempSelectedStructureId;
        mSelectedOverallStructure = setCategoryBasedOnSelectedStructureId(mSelectedOverallStructureId);
        mSelectedComponentStructure = setCategoryBasedOnSelectedStructureId(R.drawable.colored_structure_2_left_right);
        //endregion

        //region Setting the filter roomInstancesAsyncResponseHandler
        binding.searchByRadicalCharacterDescriptor.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                filterComponentKanjiGridElements();
            }
            return false;
        });
        //endregion

        //Setting the recyclerview height depending on the device's display density
        mMaxRecyclerViewHeightPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAX_RECYCLERVIEW_HEIGHT_DP, getResources().getDisplayMetrics());
    }
    private void showLoadingIndicator() {
        binding.searchByRadicalLoadingIndicator.setVisibility(View.VISIBLE);
    }
    private void hideLoadingIndicator() {
        binding.searchByRadicalLoadingIndicator.setVisibility(View.INVISIBLE);
    }
    private void showComponentsSelectionSection() {
        binding.searchByRadicalResultsGrid.setAdapter(null);
        if (mComponentSelectionType.equals("component")) binding.searchByRadicalRequestedComponentStructure.setVisibility(View.VISIBLE);
        else binding.searchByRadicalRequestedComponentStructure.setVisibility(View.GONE);
        binding.searchByRadicalSelectionGridContainer.setVisibility(View.VISIBLE);
        binding.searchByRadicalResultsGridContainer.setVisibility(View.GONE);
        binding.searchByRadicalSelectionGrid.setVisibility(View.GONE);
        binding.searchByRadicalSelectionGridNoElements.setVisibility(View.GONE);
    }
    private void updateInputElements(String inputQuery) {

        //region Creating the user selections list, filling it with the user input query, and updating the input elements
        user_selections = new String[4];
        for (int i=0; i<4; i++) { user_selections[i] = ""; }

        inputQuery = UtilitiesGeneral.removeSpecialCharacters(inputQuery);
        int userSelectionIndex = 0;
        String currentChar;
        int text_type;
        for (int i=0; i<inputQuery.length(); i++) {
            currentChar = mInputQuery.substring(i,i+1);
            text_type = UtilitiesQuery.getTextType(currentChar);
            if (text_type == Globals.TEXT_TYPE_KANJI) {
                user_selections[userSelectionIndex] = currentChar;
                userSelectionIndex++;
            }
            if (userSelectionIndex==4) break;
        }

        if (!user_selections[0].equals("")) binding.searchByRadicalElementA.setText(user_selections[0]); else binding.searchByRadicalElementA.setText("");
        if (!user_selections[1].equals("")) binding.searchByRadicalElementB.setText(user_selections[1]); else binding.searchByRadicalElementB.setText("");
        if (!user_selections[2].equals("")) binding.searchByRadicalElementC.setText(user_selections[2]); else binding.searchByRadicalElementC.setText("");
        if (!user_selections[3].equals("")) binding.searchByRadicalElementD.setText(user_selections[3]); else binding.searchByRadicalElementD.setText("");
        //endregion
    }
    private void showResultsSection() {
        binding.searchByRadicalSelectionGrid.setAdapter(null);
        binding.searchByRadicalSelectionGridContainer.setVisibility(View.GONE);
        binding.searchByRadicalResultsGridContainer.setVisibility(View.VISIBLE);
        binding.searchByRadicalResultsGrid.setVisibility(View.GONE);
        binding.searchByRadicalSelectionGridNoResults.setVisibility(View.GONE);
    }
    private void hideAllSections() {
        binding.searchByRadicalSelectionGrid.setAdapter(null);
        binding.searchByRadicalResultsGrid.setAdapter(null);
        binding.searchByRadicalSelectionGridContainer.setVisibility(View.GONE);
        binding.searchByRadicalResultsGridContainer.setVisibility(View.GONE);
    }
    private void createComponentsGrid() {
        //mNumberOfResultGridColumns = 7;

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), mNumberOfComponentGridColumns);
        binding.searchByRadicalSelectionGrid.setLayoutManager(layoutManager);
        if (mComponentsGridAdapter==null) mComponentsGridAdapter = new KanjiGridRecyclerViewAdapter(getContext(), this, mDisplayableComponentSelections, false, mDroidSansJapaneseTypeface);
        else mComponentsGridAdapter.setContents(mDisplayableComponentSelections);
        binding.searchByRadicalSelectionGrid.setAdapter(mComponentsGridAdapter);

        ViewGroup.LayoutParams params = binding.searchByRadicalSelectionGrid.getLayoutParams();
        if (mDisplayableComponentSelections.size() <= 56) {
            binding.searchByRadicalSelectionGrid.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        else {
            params.height = mMaxRecyclerViewHeightPixels;
            binding.searchByRadicalSelectionGrid.setLayoutParams(params);
        }

        binding.searchByRadicalSelectionGrid.setVisibility(View.VISIBLE);
        binding.searchByRadicalSelectionGridNoElements.setVisibility(View.GONE);
    }
    private void createResultsGrid() {
        //mNumberOfResultGridColumns = 7;

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), mNumberOfResultGridColumns);
        binding.searchByRadicalResultsGrid.setLayoutManager(layoutManager);
        if (mResultsGridAdapter==null) mResultsGridAdapter = new KanjiGridRecyclerViewAdapter(getContext(), this, mSearchResultsFinal, true, mDroidSansJapaneseTypeface);
        else mResultsGridAdapter.setContents(mSearchResultsFinal);
        binding.searchByRadicalResultsGrid.setAdapter(mResultsGridAdapter);

        ViewGroup.LayoutParams params = binding.searchByRadicalResultsGrid.getLayoutParams();
        if (mSearchResultsFinal.size() <= 56) {
            binding.searchByRadicalResultsGrid.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        else {
            params.height = mMaxRecyclerViewHeightPixels;
            binding.searchByRadicalResultsGrid.setLayoutParams(params);
        }

        binding.searchByRadicalResultsGrid.setVisibility(View.VISIBLE);
        binding.searchByRadicalSelectionGridNoResults.setVisibility(View.GONE);
    }
    private void showNoComponentsTextInsteadOfComponentsGrid() {
        binding.searchByRadicalSelectionGrid.setAdapter(null);
        binding.searchByRadicalSelectionGrid.setVisibility(View.GONE);
        binding.searchByRadicalSelectionGridNoElements.setVisibility(View.VISIBLE);
    }
    private void showNoResultsTextInsteadOfResultsGrid(String text) {
        binding.searchByRadicalResultsGrid.setAdapter(null);
        binding.searchByRadicalResultsGrid.setVisibility(View.GONE);
        binding.searchByRadicalSelectionGridNoResults.setVisibility(View.VISIBLE);
        binding.searchByRadicalSelectionGridNoResults.setText(text);
    }
    private void handleComponentSelection(boolean enterPressed) {

        if (!enterPressed) {
            binding.searchByRadicalSelectionGridContainer.setVisibility(View.GONE);
            if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
            binding.searchByRadicalSelectionGrid.setAdapter(null);
        }

        if (!enterPressed || getView()==null) return;
        EditText edittext = getView().findViewById(mSelectedEditTextId);
        edittext.setText(mSelectedComponent);

        binding.searchByRadicalContainerScrollview.scrollTo(0,0);
    }
    private void showStructuresDialog(final String type) {

        if (getContext()==null || getContext().getResources() == null) return;

        //region Get the dialog view
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_kanji_structures, null);
        final GridView structuresGrid = dialogView.findViewById(R.id.dialog_kanji_structures_gridview);
        final TextView requestedStructure = dialogView.findViewById(R.id.dialog_kanji_structures_requested_structure);
        //endregion

        //region Populating the grid
        final List<Integer> structureIds = new ArrayList<>();
        if (type.equals("overall")) structureIds.add(R.drawable.colored_structure_2_overlapping);
        structureIds.add(R.drawable.colored_structure_2_left_right);
        structureIds.add(R.drawable.colored_structure_3_left_center_right);
        structureIds.add(R.drawable.colored_structure_4_left_right);
        structureIds.add(R.drawable.colored_structure_2_up_down);
        structureIds.add(R.drawable.colored_structure_3_up_center_down);
        structureIds.add(R.drawable.colored_structure_4_up_down);
        structureIds.add(R.drawable.colored_structure_2_enclosing_topleft_to_bottomright);
        structureIds.add(R.drawable.colored_structure_2_enclosing_top_to_bottom);
        structureIds.add(R.drawable.colored_structure_2_enclosing_topright_to_bottomleft);
        structureIds.add(R.drawable.colored_structure_2_enclosing_left_to_right);
        structureIds.add(R.drawable.colored_structure_2_outlining);
        structureIds.add(R.drawable.colored_structure_2_enclosing_bottomleft_to_topright);
        structureIds.add(R.drawable.colored_structure_2_enclosing_bottom_to_top);
        structureIds.add(R.drawable.colored_structure_3_upwards_triangle);
        structureIds.add(R.drawable.colored_structure_4_square_repeat);
        structureIds.add(R.drawable.colored_structure_4_square);

        Drawable image = ResourcesCompat.getDrawable(getContext().getResources(), structureIds.get(0), null);
        requestedStructure.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);

        StructuresGridViewAdapter gridAdapter = new StructuresGridViewAdapter(getContext(), R.layout.list_item_structures_grid, structureIds);
        structuresGrid.setAdapter(gridAdapter);
        structuresGrid.setOnItemClickListener((adapterView, view, pos, id) -> {
            if (getContext()==null) return;
            mTempSelectedStructureId = structureIds.get(pos);
            Drawable image1 = ResourcesCompat.getDrawable(getContext().getResources(), mTempSelectedStructureId, null);
            requestedStructure.setCompoundDrawablesWithIntrinsicBounds(null, null, image1, null);
        });
        //endregion

        //region Building the dialog
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogStyle).create();
        Window window = alertDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        alertDialog.setTitle(type.equals("overall")? R.string.choose_overall_structure : R.string.choose_component_structure);
        alertDialog.setView(dialogView);
        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.ok),
                (dialog, which) -> {
                    if (getContext()==null || mTempSelectedStructureId==0) return;

                    Drawable imageLocal = ResourcesCompat.getDrawable(getContext().getResources(), mTempSelectedStructureId, null);

                    if (type.equals("overall")) {
                        mSelectedOverallStructureId = mTempSelectedStructureId;
                        mSelectedOverallStructure = setCategoryBasedOnSelectedStructureId(mSelectedOverallStructureId);
                        binding.searchByRadicalOverallStructureButton.setCompoundDrawablesWithIntrinsicBounds(null, null, imageLocal, null);
                    }
                    else {
                        mSelectedComponentStructure = setCategoryBasedOnSelectedStructureId(mTempSelectedStructureId);
                        binding.searchByRadicalRequestedComponentStructure.setCompoundDrawablesWithIntrinsicBounds(null, null, imageLocal, null);
                        startCreatingComponentKanjiGridElementsAsynchronously();
                    }
                    dialog.dismiss();
                });
        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                (dialog, which) -> dialog.dismiss()
        );
        alertDialog.show();
        //endregion

    }
    private void drawBorderAroundThisEditText(EditText editText) {

        if (getActivity() == null) return;
        if (binding.searchByRadicalElementA==null || binding.searchByRadicalElementB==null || binding.searchByRadicalElementC==null || binding.searchByRadicalElementD==null) return;

        binding.searchByRadicalElementAContainer.setBackgroundResource(0);
        binding.searchByRadicalElementBContainer.setBackgroundResource(0);
        binding.searchByRadicalElementCContainer.setBackgroundResource(0);
        binding.searchByRadicalElementDContainer.setBackgroundResource(0);

        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.searchByRadical_three_sided_background, typedValue, true);
        if (editText.getId() == binding.searchByRadicalElementA.getId()) binding.searchByRadicalElementAContainer.setBackgroundResource(typedValue.resourceId);
        else if (editText.getId() == binding.searchByRadicalElementB.getId()) binding.searchByRadicalElementBContainer.setBackgroundResource(typedValue.resourceId);
        else if (editText.getId() == binding.searchByRadicalElementC.getId()) binding.searchByRadicalElementCContainer.setBackgroundResource(typedValue.resourceId);
        else if (editText.getId() == binding.searchByRadicalElementD.getId()) binding.searchByRadicalElementDContainer.setBackgroundResource(typedValue.resourceId);
    }

    void onRequestedStructureButtonClick() {
        showStructuresDialog("overall");
    }
    void onRadicalButtonClick() {
        if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());

        mSelectedComponent = "";
        mComponentSelectionType = "radical";
        binding.searchByRadicalSelectionGridTitle.setText(R.string.select_the_radical);
        showComponentsSelectionSection();

        startCreatingComponentKanjiGridElementsAsynchronously();
    }
    void onComponentButtonClick() {
        if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());

        mSelectedComponent = "";
        mComponentSelectionType = "component";
        binding.searchByRadicalSelectionGridTitle.setText(R.string.select_the_component);
        showComponentsSelectionSection();

        startCreatingComponentKanjiGridElementsAsynchronously();
    }
    void onSearchButtonClick() {

        if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());

        showResultsSection();

        String[] elements_strings = new String[4];
        elements_strings[0] = binding.searchByRadicalElementA.getText().toString();
        elements_strings[1] = binding.searchByRadicalElementB.getText().toString();
        elements_strings[2] = binding.searchByRadicalElementC.getText().toString();
        elements_strings[3] = binding.searchByRadicalElementD.getText().toString();

        startSearchingForKanjisAsynchronously(elements_strings);
    }
    void onRequestedComponentStructureButtonClick() {
        showStructuresDialog("component");
    }
    void onFilterButtonClick() {
        filterComponentKanjiGridElements();
    }
    void onCancelTopButtonClick() {
        handleComponentSelection(false);
    }
    void onSendToElementTopButtonClick() {
        handleComponentSelection(true);
    }
    void onSendToInputTopButtonClick() {
        searchByRadicalFragmentOperationsHandler.onQueryTextUpdateFromSearchByRadicalRequested(mSelectedComponent);
    }
    void onCancelBottomButtonClick() {
        handleComponentSelection(false);
    }
    void onSendToElementBottomButtonClick() {
        handleComponentSelection(true);
    }
    void onSendToInputBottomButtonClick() {
        searchByRadicalFragmentOperationsHandler.onQueryTextUpdateFromSearchByRadicalRequested(mSelectedComponent);
    }


    //Communication with parent activity
    private SearchByRadicalFragmentOperationsHandler searchByRadicalFragmentOperationsHandler;
    interface SearchByRadicalFragmentOperationsHandler {
        void onQueryTextUpdateFromSearchByRadicalRequested(String selectedWordString);
    }

    //Communication with KanjiComponentsGridRecyclerViewAdapter
    @Override public void onComponentClicked(int clickedPosition) {
        mSelectedComponent = mDisplayableComponentSelections.get(clickedPosition).substring(0,1);
    }
    @Override public void onSearchResultClicked(int clickedPosition) {
        searchByRadicalFragmentOperationsHandler.onQueryTextUpdateFromSearchByRadicalRequested(mSearchResultsFinal.get(clickedPosition));
    }

    //Communication with AsyncTasks
    @Override public void onKanjiSearchAsyncTaskResultsFound(Object[] dataElements) {
        if (getContext()==null) return;

        List<String> searchResultsRaw = (List<String>) dataElements[0];
        int searchResultType = (int) dataElements[1];

        hideLoadingIndicator();

        //Displaying only the search results that have a glyph in the font
        mSearchResultsFinal = new ArrayList<>();
        List<String> searchResultsPrintable = new ArrayList<>();
        String value;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < searchResultsRaw.size(); i++) {
                value = searchResultsRaw.get(i);
                if (value.length()>0 && AndroidUtilitiesIO.isPrintable(value.substring(0, 1))) {
                    searchResultsPrintable.add(searchResultsRaw.get(i));
                }
            }
        }
        else {
            searchResultsPrintable = searchResultsRaw;
        }

        //Displaying only the first 400 values to prevent overload
        List<String> selections = new ArrayList<>();
        int display_limit = 400;
        for (int i = 0; i < searchResultsPrintable.size(); i++) {
            selections.add(searchResultsPrintable.get(i));
            if (i>display_limit) break;
        }
        mSearchResultsFinal = selections;

        //Displaying the results grid
        if (searchResultType == Globals.KANJI_SEARCH_RESULT_SEARCH_TOO_BROAD) showNoResultsTextInsteadOfResultsGrid(getString(R.string.search_for_radical_search_too_broad));
        else if (searchResultType == Globals.KANJI_SEARCH_RESULT_DEFAULT) createResultsGrid();
        else if (searchResultType == Globals.KANJI_SEARCH_RESULT_NO_JAP_RESULTS) {
            showNoResultsTextInsteadOfResultsGrid(getString(R.string.search_by_radical_no_jap_results_found));
        } else if (searchResultType == Globals.KANJI_SEARCH_RESULT_NO_JAP_NO_PRINTABLE_RESULTS) {
            showNoResultsTextInsteadOfResultsGrid(getString(R.string.search_by_radical_no_jap_no_printable_results_found));
        } else if (searchResultType == Globals.KANJI_SEARCH_RESULT_NO_RESULTS){
            showNoResultsTextInsteadOfResultsGrid(getString(R.string.search_by_radical_no_results_found));
        }

    }
    @Override public void onComponentGridCreationAsyncTaskDone(List<String> data) {
        if (getContext()==null) return;
        mUnfilteredDisplayableComponentSelections = data;
        mDisplayableComponentSelections = new ArrayList<>(mUnfilteredDisplayableComponentSelections);
        hideLoadingIndicator();
        mKanjiCharacterNameForFilter = binding.searchByRadicalCharacterDescriptor.getText().toString();
        if (TextUtils.isEmpty(mKanjiCharacterNameForFilter)) {
            if (mDisplayableComponentSelections.size() != 0) createComponentsGrid();
            else showNoComponentsTextInsteadOfComponentsGrid();
        }
        else startFilteringComponentKanjiGridElementsAsynchronously();
    }
    @Override public void onComponentsGridFilterAsyncTaskDone(List<String> data) {
        if (getContext()==null) return;
        mDisplayableComponentSelections = data;
        hideLoadingIndicator();

        if (mDisplayableComponentSelections.size() != 0) createComponentsGrid();
        else showNoComponentsTextInsteadOfComponentsGrid();
    }
}