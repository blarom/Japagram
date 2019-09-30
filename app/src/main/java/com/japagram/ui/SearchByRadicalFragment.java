package com.japagram.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.japagram.R;
import com.japagram.adapters.KanjiGridRecyclerViewAdapter;
import com.japagram.adapters.StructuresGridViewAdapter;
import com.japagram.asynctasks.ComponentGridCreationAsyncTask;
import com.japagram.asynctasks.ComponentsGridFilterAsyncTask;
import com.japagram.asynctasks.KanjiSearchAsyncTask;
import com.japagram.resources.GlobalConstants;
import com.japagram.resources.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SearchByRadicalFragment extends Fragment implements
        KanjiGridRecyclerViewAdapter.ComponentClickHandler,
        KanjiSearchAsyncTask.KanjiSearchAsyncResponseHandler,
        ComponentGridCreationAsyncTask.ComponentGridCreationAsyncResponseHandler,
        ComponentsGridFilterAsyncTask.ComponentsGridFilterAsyncResponseHandler {


    //region Parameters
    @BindView(R.id.search_by_radical_loading_indicator) ProgressBar mProgressBarLoadingIndicator;
    @BindView(R.id.search_by_radical_overall_structure_button) Button mOverallStructureButton;
    @BindView(R.id.search_by_radical_container_scrollview) NestedScrollView mOverallContainerScrollView;
    @BindView(R.id.search_by_radicals_overall_block_container) LinearLayout mOverallBlockContainerLinearLayout;
    @BindView(R.id.search_by_radical_elementA) EditText mElementAEditText;
    @BindView(R.id.search_by_radical_elementB) EditText mElementBEditText;
    @BindView(R.id.search_by_radical_elementC) EditText mElementCEditText;
    @BindView(R.id.search_by_radical_elementD) EditText mElementDEditText;
    @BindView(R.id.search_by_radical_elementA_container) FrameLayout mElementAEditTextContainer;
    @BindView(R.id.search_by_radical_elementB_container) FrameLayout mElementBEditTextContainer;
    @BindView(R.id.search_by_radical_elementC_container) FrameLayout mElementCEditTextContainer;
    @BindView(R.id.search_by_radical_elementD_container) FrameLayout mElementDEditTextContainer;
    @BindView(R.id.search_by_radical_selection_grid_container) LinearLayout mSelectionGridContainerLinearLayout;
    @BindView(R.id.search_by_radical_character_descriptor) EditText mCharacterDescriptorEditText;
    @BindView(R.id.search_by_radical_requested_component_structure) Button mComponentStructureButton;
    @BindView(R.id.search_by_radical_selection_grid_title) TextView mSelectionGridTitleTextView;
    @BindView(R.id.search_by_radical_selection_grid) RecyclerView mSelectionGridRecyclerView;
    @BindView(R.id.search_by_radical_selection_grid_no_elements_textview) TextView mNoSelectionElementsTextView;
    @BindView(R.id.search_by_radical_results_grid_container) LinearLayout mResultsGridContainerLinearLayout;
    @BindView(R.id.search_by_radical_results_grid) RecyclerView mResultsGridRecyclerView;
    @BindView(R.id.search_by_radical_selection_grid_no_results_textview) TextView mNoResultsTextView;
    private Unbinder mBinding;
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
    private List<String> mPrintableSearchResults;
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
    @Override public void onAttach(Context context) {
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

        initializeViews(rootView);

        updateInputElements(mInputQuery);
        hideAllSections();
        return rootView;
    }
    @Override public void onResume() {
        super.onResume();
        if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());
    }
    @Override public void onDetach() {
        super.onDetach();
        cancelAsyncOperations();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        mBinding.unbind();
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
        mDroidSansJapaneseTypeface = Utilities.getPreferenceUseJapaneseFont(getActivity()) ?
                Typeface.createFromAsset(am, String.format(Locale.JAPAN, "fonts/%s", "DroidSansJapanese.ttf")) : Typeface.DEFAULT;
    }
    private void initializeViews(View rootView) {

        if (getContext()==null) return;
        mBinding = ButterKnife.bind(this, rootView);

        //region Setting the Element listeners
        mElementAEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());
            if (hasFocus) mSelectedEditTextId = mElementAEditText.getId();
            drawBorderAroundThisEditText(mElementAEditText);
        });
        mElementAEditText.setOnClickListener(view -> {
            mSelectedEditTextId = mElementAEditText.getId();
            drawBorderAroundThisEditText(mElementAEditText);
        });
        mElementAEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());
            }
            return false;
        });

        mElementBEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());
            if (hasFocus) mSelectedEditTextId = mElementBEditText.getId();
            drawBorderAroundThisEditText(mElementBEditText);
        });
        mElementBEditText.setOnClickListener(view -> {
            mSelectedEditTextId = mElementBEditText.getId();
            drawBorderAroundThisEditText(mElementBEditText);
        });
        mElementBEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());
            }
            return false;
        });

        mElementCEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());
            if (hasFocus) mSelectedEditTextId = mElementCEditText.getId();
            drawBorderAroundThisEditText(mElementCEditText);
        });
        mElementCEditText.setOnClickListener(view -> {
            mSelectedEditTextId = mElementCEditText.getId();
            drawBorderAroundThisEditText(mElementCEditText);
        });
        mElementCEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());
            }
            return false;
        });

        mElementDEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());
            if (hasFocus) mSelectedEditTextId = mElementDEditText.getId();
            drawBorderAroundThisEditText(mElementDEditText);
        });
        mElementDEditText.setOnClickListener(view -> {
            mSelectedEditTextId = mElementDEditText.getId();
            drawBorderAroundThisEditText(mElementDEditText);
        });
        mElementDEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());
            }
            return false;
        });

        mSelectedEditTextId = mElementAEditText.getId();
        drawBorderAroundThisEditText(mElementAEditText);
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
        mCharacterDescriptorEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                filterComponentKanjiGridElements();
            }
            return false;
        });
        //endregion

        //Setting the recyclerview height depending on the device's display density
        mMaxRecyclerViewHeightPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAX_RECYCLERVIEW_HEIGHT_DP, getResources().getDisplayMetrics());
    }
    private void drawBorderAroundThisEditText(EditText editText) {

        if (mElementAEditText==null || mElementBEditText==null || mElementCEditText==null || mElementDEditText==null) return;

        mElementAEditTextContainer.setBackgroundResource(0);
        mElementBEditTextContainer.setBackgroundResource(0);
        mElementCEditTextContainer.setBackgroundResource(0);
        mElementDEditTextContainer.setBackgroundResource(0);

        if (editText.getId() == mElementAEditText.getId()) mElementAEditTextContainer.setBackgroundResource(R.drawable.background_search_by_radical_three_sided);
        else if (editText.getId() == mElementBEditText.getId()) mElementBEditTextContainer.setBackgroundResource(R.drawable.background_search_by_radical_three_sided);
        else if (editText.getId() == mElementCEditText.getId()) mElementCEditTextContainer.setBackgroundResource(R.drawable.background_search_by_radical_three_sided);
        else if (editText.getId() == mElementDEditText.getId()) mElementDEditTextContainer.setBackgroundResource(R.drawable.background_search_by_radical_three_sided);
    }
    private void updateInputElements(String inputQuery) {

        //region Creating the user selections list, filling it with the user input query, and updating the input elements
        user_selections = new String[4];
        for (int i=0; i<4; i++) { user_selections[i] = ""; }

        inputQuery = Utilities.removeSpecialCharacters(inputQuery);
        int userSelectionIndex = 0;
        String currentChar;
        int text_type;
        for (int i=0; i<inputQuery.length(); i++) {
            currentChar = mInputQuery.substring(i,i+1);
            text_type = ConvertFragment.getTextType(currentChar);
            if (text_type == GlobalConstants.TYPE_KANJI) {
                user_selections[userSelectionIndex] = currentChar;
                userSelectionIndex++;
            }
            if (userSelectionIndex==4) break;
        }

        if (!user_selections[0].equals("")) mElementAEditText.setText(user_selections[0]); else mElementAEditText.setText("");
        if (!user_selections[1].equals("")) mElementBEditText.setText(user_selections[1]); else mElementBEditText.setText("");
        if (!user_selections[2].equals("")) mElementCEditText.setText(user_selections[2]); else mElementCEditText.setText("");
        if (!user_selections[3].equals("")) mElementDEditText.setText(user_selections[3]); else mElementDEditText.setText("");
        //endregion
    }
    private void showStructuresDialog(final String type) {

        if (getContext()==null) return;

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

        Drawable image = getContext().getResources().getDrawable(structureIds.get(0));
        requestedStructure.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);

        StructuresGridViewAdapter gridAdapter = new StructuresGridViewAdapter(getContext(), R.layout.list_item_structures_grid, structureIds);
        structuresGrid.setAdapter(gridAdapter);
        structuresGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                if (getContext()==null) return;
                mTempSelectedStructureId = structureIds.get(pos);
                Drawable image = getContext().getResources().getDrawable(mTempSelectedStructureId);
                requestedStructure.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);
            }
        });
        //endregion

        //region Building the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (type.equals("overall")) builder.setTitle("Choose the overall structure");
        else builder.setTitle("Choose the component's structure");

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (getContext()==null || mTempSelectedStructureId==0) return;

                Drawable image = getContext().getResources().getDrawable(mTempSelectedStructureId);

                if (type.equals("overall")) {
                    mSelectedOverallStructureId = mTempSelectedStructureId;
                    mSelectedOverallStructure = setCategoryBasedOnSelectedStructureId(mSelectedOverallStructureId);
                    mOverallStructureButton.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);
                }
                else {
                    mSelectedComponentStructure = setCategoryBasedOnSelectedStructureId(mTempSelectedStructureId);
                    mComponentStructureButton.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);
                    startCreatingComponentKanjiGridElementsAsynchronously();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        //endregion

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private int setCategoryBasedOnSelectedStructureId(int selectedStructureId) {

        switch (selectedStructureId) {
            case R.drawable.colored_structure_2_overlapping: return GlobalConstants.Index_full;
            case R.drawable.colored_structure_2_left_right: return GlobalConstants.Index_across2;
            case R.drawable.colored_structure_3_left_center_right: return GlobalConstants.Index_across3;
            case R.drawable.colored_structure_4_left_right: return GlobalConstants.Index_across4;
            case R.drawable.colored_structure_2_up_down: return GlobalConstants.Index_down2;
            case R.drawable.colored_structure_3_up_center_down: return GlobalConstants.Index_down3;
            case R.drawable.colored_structure_4_up_down: return GlobalConstants.Index_down4;
            case R.drawable.colored_structure_2_enclosing_topleft_to_bottomright: return GlobalConstants.Index_topleftout;
            case R.drawable.colored_structure_2_enclosing_top_to_bottom: return GlobalConstants.Index_topout;
            case R.drawable.colored_structure_2_enclosing_topright_to_bottomleft: return GlobalConstants.Index_toprightout;
            case R.drawable.colored_structure_2_enclosing_left_to_right: return GlobalConstants.Index_leftout;
            case R.drawable.colored_structure_2_outlining: return GlobalConstants.Index_fullout;
            case R.drawable.colored_structure_2_enclosing_bottomleft_to_topright: return GlobalConstants.Index_bottomleftout;
            case R.drawable.colored_structure_2_enclosing_bottom_to_top: return GlobalConstants.Index_bottomout;
            case R.drawable.colored_structure_3_upwards_triangle: return GlobalConstants.Index_three_repeat;
            case R.drawable.colored_structure_4_square_repeat: return GlobalConstants.Index_four_repeat;
            case R.drawable.colored_structure_4_square: return GlobalConstants.Index_foursquare;
            case R.drawable.colored_structure_5_hourglass: return GlobalConstants.Index_five_repeat;
            default: return 0;
        }
    }
    private void handleComponentSelection(boolean enterPressed) {

        if (!enterPressed) {
            mSelectionGridContainerLinearLayout.setVisibility(View.GONE);
            if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());
            mSelectionGridRecyclerView.setAdapter(null);
        }

        if (!enterPressed || getView()==null) return;
        EditText edittext = getView().findViewById(mSelectedEditTextId);
        edittext.setText(mSelectedComponent);

        mOverallContainerScrollView.scrollTo(0,0);
    }
    private void showLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.VISIBLE);
    }
    private void hideLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.INVISIBLE);
    }
    private void showComponentsSelectionSection() {
        mResultsGridRecyclerView.setAdapter(null);
        if (mComponentSelectionType.equals("component")) mComponentStructureButton.setVisibility(View.VISIBLE);
        else mComponentStructureButton.setVisibility(View.GONE);
        mSelectionGridContainerLinearLayout.setVisibility(View.VISIBLE);
        mResultsGridContainerLinearLayout.setVisibility(View.GONE);
        mSelectionGridRecyclerView.setVisibility(View.GONE);
        mNoSelectionElementsTextView.setVisibility(View.GONE);
    }
    private void showResultsSection() {
        mSelectionGridRecyclerView.setAdapter(null);
        mSelectionGridContainerLinearLayout.setVisibility(View.GONE);
        mResultsGridContainerLinearLayout.setVisibility(View.VISIBLE);
        mResultsGridRecyclerView.setVisibility(View.GONE);
        mNoResultsTextView.setVisibility(View.GONE);
    }
    private void hideAllSections() {
        mSelectionGridRecyclerView.setAdapter(null);
        mResultsGridRecyclerView.setAdapter(null);
        mSelectionGridContainerLinearLayout.setVisibility(View.GONE);
        mResultsGridContainerLinearLayout.setVisibility(View.GONE);
    }
    private void showNoComponentsTextInsteadOfComponentsGrid() {
        mSelectionGridRecyclerView.setAdapter(null);
        mSelectionGridRecyclerView.setVisibility(View.GONE);
        mNoSelectionElementsTextView.setVisibility(View.VISIBLE);
    }
    private void showNoResultsTextInsteadOfResultsGrid(String text) {
        mResultsGridRecyclerView.setAdapter(null);
        mResultsGridRecyclerView.setVisibility(View.GONE);
        mNoResultsTextView.setVisibility(View.VISIBLE);
        mNoResultsTextView.setText(text);
    }
    private void createComponentsGrid() {
        //mNumberOfResultGridColumns = 7;

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), mNumberOfComponentGridColumns);
        mSelectionGridRecyclerView.setLayoutManager(layoutManager);
        if (mComponentsGridAdapter==null) mComponentsGridAdapter = new KanjiGridRecyclerViewAdapter(getContext(), this, mDisplayableComponentSelections, false, mDroidSansJapaneseTypeface);
        else mComponentsGridAdapter.setContents(mDisplayableComponentSelections);
        mSelectionGridRecyclerView.setAdapter(mComponentsGridAdapter);

        ViewGroup.LayoutParams params = mSelectionGridRecyclerView.getLayoutParams();
        if (mDisplayableComponentSelections.size() <= 56) {
            mSelectionGridRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        else {
            params.height = mMaxRecyclerViewHeightPixels;
            mSelectionGridRecyclerView.setLayoutParams(params);
        }

        mSelectionGridRecyclerView.setVisibility(View.VISIBLE);
        mNoSelectionElementsTextView.setVisibility(View.GONE);
    }
    private void createResultsGrid() {
        //mNumberOfResultGridColumns = 7;

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), mNumberOfResultGridColumns);
        mResultsGridRecyclerView.setLayoutManager(layoutManager);
        if (mResultsGridAdapter==null) mResultsGridAdapter = new KanjiGridRecyclerViewAdapter(getContext(), this, mPrintableSearchResults, true, mDroidSansJapaneseTypeface);
        else mResultsGridAdapter.setContents(mPrintableSearchResults);
        mResultsGridRecyclerView.setAdapter(mResultsGridAdapter);

        ViewGroup.LayoutParams params = mResultsGridRecyclerView.getLayoutParams();
        if (mPrintableSearchResults.size() <= 56) {
            mResultsGridRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        else {
            params.height = mMaxRecyclerViewHeightPixels;
            mResultsGridRecyclerView.setLayoutParams(params);
        }

        mResultsGridRecyclerView.setVisibility(View.VISIBLE);
        mNoResultsTextView.setVisibility(View.GONE);
    }
    private void startCreatingComponentKanjiGridElementsAsynchronously() {
        if (getActivity()!=null) {
            showLoadingIndicator();
            mComponentGridCreationAsyncTask = new ComponentGridCreationAsyncTask(
                    getContext(), mComponentSelectionType, mRadicalsOnlyDatabase, mSelectedComponentStructure, this);
            mComponentGridCreationAsyncTask.execute();
        }
    }
    private void startFilteringComponentKanjiGridElementsAsynchronously() {
        if (getActivity()!=null) {
            showLoadingIndicator();
            mComponentsGridFilterAsyncTask = new ComponentsGridFilterAsyncTask(
                    getContext(), mComponentSelectionType, mRadicalsOnlyDatabase, mKanjiCharacterNameForFilter, mUnfilteredDisplayableComponentSelections, this);
            mComponentsGridFilterAsyncTask.execute();
        }
    }
    private void startSearchingForKanjisAsynchronously(String[] elements_strings) {
        if (getActivity()!=null) {
            showLoadingIndicator();

            mKanjiSearchAsyncTask = new KanjiSearchAsyncTask(getContext(), elements_strings, mSelectedOverallStructure, mSimilarsDatabase, this);
            mKanjiSearchAsyncTask.execute();
        }
    }
    private void filterComponentKanjiGridElements() {
        if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());
        mKanjiCharacterNameForFilter = mCharacterDescriptorEditText.getText().toString();
        startFilteringComponentKanjiGridElementsAsynchronously();
    }
    private void cancelAsyncOperations() {
        if (mKanjiSearchAsyncTask != null) mKanjiSearchAsyncTask.cancel(true);
        if (mComponentGridCreationAsyncTask != null) mComponentGridCreationAsyncTask.cancel(true);
        if (mComponentsGridFilterAsyncTask != null) mComponentsGridFilterAsyncTask.cancel(true);
    }


    @OnClick (R.id.search_by_radical_overall_structure_button) void onRequestedStructureButtonClick() {
        showStructuresDialog("overall");
    }
    @OnClick (R.id.search_by_radical_button_radical) void onRadicalButtonClick() {
        if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());

        mSelectedComponent = "";
        mComponentSelectionType = "radical";
        mSelectionGridTitleTextView.setText(R.string.select_the_radical);
        showComponentsSelectionSection();

        startCreatingComponentKanjiGridElementsAsynchronously();
    }
    @OnClick (R.id.search_by_radical_button_component) void onComponentButtonClick() {
        if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());

        mSelectedComponent = "";
        mComponentSelectionType = "component";
        mSelectionGridTitleTextView.setText(R.string.select_the_component);
        showComponentsSelectionSection();

        startCreatingComponentKanjiGridElementsAsynchronously();
    }
    @OnClick (R.id.search_by_radical_button_search) void onSearchButtonClick() {

        if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());

        showResultsSection();

        String[] elements_strings = new String[4];
        elements_strings[0] = mElementAEditText.getText().toString();
        elements_strings[1] = mElementBEditText.getText().toString();
        elements_strings[2] = mElementCEditText.getText().toString();
        elements_strings[3] = mElementDEditText.getText().toString();

        startSearchingForKanjisAsynchronously(elements_strings);
    }
    @OnClick (R.id.search_by_radical_requested_component_structure) void oRequestedComponentStructureButtonClick() {
        showStructuresDialog("component");
    }
    @OnClick (R.id.search_by_radical_button_filter) void onFilterButtonClick() {
        filterComponentKanjiGridElements();
    }
    @OnClick (R.id.search_by_radical_button_selection_grid_cancel_top) void onCancelTopButtonClick() {
        handleComponentSelection(false);
    }
    @OnClick (R.id.search_by_radical_button_selection_grid_send_to_element_top) void onSendToElementTopButtonClick() {
        handleComponentSelection(true);
    }
    @OnClick (R.id.search_by_radical_button_selection_grid_send_to_input_top) void onSendToInputTopButtonClick() {
        searchByRadicalFragmentOperationsHandler.onQueryTextUpdateFromSearchByRadicalRequested(mSelectedComponent);
    }
    @OnClick (R.id.search_by_radical_button_selection_grid_cancel_bottom) void onCancelBottomButtonClick() {
        handleComponentSelection(false);
    }
    @OnClick (R.id.search_by_radical_button_selection_grid_send_to_element_bottom) void onSendToElementBottomButtonClick() {
        handleComponentSelection(true);
    }
    @OnClick (R.id.search_by_radical_button_selection_grid_send_to_input_bottom) void onSendToInputBottomButtonClick() {
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
        searchByRadicalFragmentOperationsHandler.onQueryTextUpdateFromSearchByRadicalRequested(mPrintableSearchResults.get(clickedPosition));
    }

    //Communication with AsyncTasks
    @Override public void onKanjiSearchAsyncTaskResultsFound(Object[] dataElements) {
        if (getContext()==null) return;

        List<String> search_results = (List<String>) dataElements[0];
        boolean searchTooBroad = (boolean) dataElements[1];

        hideLoadingIndicator();

        //Displaying only the search results that have a glyph in the font
        mPrintableSearchResults = new ArrayList<>();
        String value;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < search_results.size(); i++) {
                value = search_results.get(i);
                if (value.length()>0 && Utilities.isPrintable(value.substring(0, 1))) {
                    mPrintableSearchResults.add(search_results.get(i));
                }
            }
        }
        else {
            mPrintableSearchResults = search_results;
        }

        //Displaying only the first 400 values to prevent overload
        List<String> selections = new ArrayList<>();
        int display_limit = 400;
        for (int i = 0; i < mPrintableSearchResults.size(); i++) {
            selections.add(mPrintableSearchResults.get(i));
            if (i>display_limit) break;
        }
        mPrintableSearchResults = selections;

        //Displaying the results grid
        if (searchTooBroad) showNoResultsTextInsteadOfResultsGrid(getString(R.string.search_for_radical_search_too_broad));
        else if (mPrintableSearchResults.size() != 0) createResultsGrid();
        else showNoResultsTextInsteadOfResultsGrid(getString(R.string.search_by_radical_no_results_found));

        //createSearchResultsGrid(printable_search_results, searchTooBroad);
    }
    @Override public void onComponentGridCreationAsyncTaskDone(List<String> data) {
        if (getContext()==null) return;
        mUnfilteredDisplayableComponentSelections = data;
        mDisplayableComponentSelections = new ArrayList<>(mUnfilteredDisplayableComponentSelections);
        hideLoadingIndicator();
        mKanjiCharacterNameForFilter = mCharacterDescriptorEditText.getText().toString();
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