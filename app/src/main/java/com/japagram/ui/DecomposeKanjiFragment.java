package com.japagram.ui;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.japagram.R;
import com.japagram.asynctasks.KanjiCharacterDecompositionAsyncTask;
import com.japagram.databinding.FragmentDecompositionBodyBinding;
import com.japagram.databinding.FragmentDictionaryBodyBinding;
import com.japagram.resources.LocaleHelper;
import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesCrossPlatform.UtilitiesQuery;
import com.japagram.utilitiesPlatformOverridable.OvUtilsGeneral;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

public class DecomposeKanjiFragment extends Fragment implements
        KanjiCharacterDecompositionAsyncTask.KanjiCharacterDecompositionAsyncResponseHandler {


    //region Parameters
    private FragmentDecompositionBodyBinding binding;
    private List<Object> mInputQueryKanjis;
    private List<String[]> mRadicalsOnlyDatabase;
    private boolean mAlreadyGotFirstQuery;
    private Typeface mDroidSansJapaneseTypeface;
    private String mInputQuery;
    private int mScrollY;
    private Resources mLocalizedResources;
    private KanjiCharacterDecompositionAsyncTask mKanjiCharacterDecompositionAsyncTask;
    //endregion


    //Lifecycle Functions
    @Override public void onCreate(Bundle savedInstanceState) { //instead of onActivityCreated
        super.onCreate(savedInstanceState);
        getExtras();
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_decomposition, container, false);

        //setRetainInstance(true);
        if (getContext()==null) return rootView;

        //Setting the Typeface
        AssetManager am = getContext().getApplicationContext().getAssets();
        mDroidSansJapaneseTypeface = AndroidUtilitiesPrefs.getPreferenceUseJapaneseFont(getActivity()) ?
                Typeface.createFromAsset(am, String.format(Locale.JAPAN, "fonts/%s", "DroidSansJapanese.ttf")) : Typeface.DEFAULT;

        mLocalizedResources = AndroidUtilitiesIO.getLocalizedResources(getContext(), Locale.getDefault());

        return rootView;
    }
    @Override public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentDecompositionBodyBinding.bind(view);
        getDecomposition();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        //if (getActivity()!=null && MainApplication.getRefWatcher(getActivity())!=null) MainApplication.getRefWatcher(getActivity()).watch(this);
    }
    @Override public void onDetach() {
        super.onDetach();
        if (mKanjiCharacterDecompositionAsyncTask != null) mKanjiCharacterDecompositionAsyncTask.cancel(true);
    }
    @Override public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    //Functionality Functions
    private void getExtras() {
        if (getArguments()!=null) {
            mInputQuery = getArguments().getString(getString(R.string.user_query_word));
            mRadicalsOnlyDatabase = (List<String[]>) getArguments().getSerializable(getString(R.string.rad_only_database));
        }
    }
    private void getDecomposition() {

        //Keep only the Kanjis in the input query
        mInputQueryKanjis = new ArrayList<>();
        for (int i=0; i<mInputQuery.length(); i++) {
            String currentChar = mInputQuery.substring(i,i+1);
            if (UtilitiesQuery.getTextType(currentChar) == Globals.TEXT_TYPE_KANJI) {
                Object[] elements = new Object[4];
                elements[Globals.DECOMP_KANJI_LIST_INDEX] = currentChar;
                elements[Globals.DECOMP_RADICAL_ITERATION] = 0;
                elements[Globals.DECOMP_PARENT_ALREADY_DISPLAYED] = false;
                mInputQueryKanjis.add(elements);
            }
        }

        //Checking that the input is valid and getting the decompositions. If not, return no results
        if (mInputQueryKanjis.size() > 0) {
            Object[] elements = (Object[]) mInputQueryKanjis.get(0);
            if (!TextUtils.isEmpty((String) elements[Globals.DECOMP_KANJI_LIST_INDEX])) {
                binding.decompositionHint.setVisibility(View.GONE);
                startGettingDecompositionAsynchronously((String) elements[Globals.DECOMP_KANJI_LIST_INDEX], (int) elements[1], 0);
            }
        }
        else {
            binding.decompositionHint.setVisibility(View.VISIBLE);
        }

    }
    private void setPrintableKanji(TextView tv, Object text) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (text instanceof SpannableString) {
                SpannableString span = (SpannableString) text;
                if (AndroidUtilitiesIO.isPrintable(span.toString())) {
                    tv.setText(span);
                }
                else {
                    tv.setText(" X");
                }
            }
            else {
                String string = (String) text;
                if (AndroidUtilitiesIO.isPrintable(string)) {
                    tv.setText(string);
                }
                else {
                    tv.setText(" X");
                }
            }
        }
    }
    private void displayDecomposition(final int radicalIteration,
                                      final List<List<String>> decomposedKanji,
                                      List<String> currentKanjiDetailedCharacteristics,
                                      List<String> currentKanjiMainRadicalInfo,
                                      int radicalIndex,
                                      int radicalIndexOriginal,
                                      List<String> currentMainRadicalDetailedCharacteristics,
                                      final int kanjiListIndex) {

        if (getContext()==null) return;

        //region Setting up the view
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View decompositionContainer = inflater.inflate(R.layout.list_item_decomposition, null);
        final ConstraintLayout layout = decompositionContainer.findViewById(R.id.fragment_decomposition_element_constraint_layout);
        final TextView kanjiTV = decompositionContainer.findViewById(R.id.decomposition_element_kanji);
        final ImageView removeDecompositionIV = decompositionContainer.findViewById(R.id.remove_decomposition_imageview);
        final ImageView structureIV = decompositionContainer.findViewById(R.id.decomposition_element_structure_image);
        final LinearLayout radicalGalleryLL = decompositionContainer.findViewById(R.id.decomposition_element_radical_gallery);
        final TextView structureTitleTV = decompositionContainer.findViewById(R.id.decomposition_element_structure_title);
        final TextView structureTV = decompositionContainer.findViewById(R.id.decomposition_element_structure_value);
        final TextView radicalTitleTV = decompositionContainer.findViewById(R.id.decomposition_element_radical_title);
        final TextView radicalTV = decompositionContainer.findViewById(R.id.decomposition_element_radical_value);
        final TextView onReadingTV = decompositionContainer.findViewById(R.id.decomposition_element_on_readings_value);
        final TextView kunReadingTV = decompositionContainer.findViewById(R.id.decomposition_element_kun_readings_value);
        final TextView nameReadingTV = decompositionContainer.findViewById(R.id.decomposition_element_name_readings_value);
        final TextView meaningTV = decompositionContainer.findViewById(R.id.decomposition_element_meanings_value);

        if (AndroidUtilitiesPrefs.getPreferenceShowDecompKanjiStructureInfo(getActivity())) {
            structureTitleTV.setVisibility(View.VISIBLE);
            structureTV.setVisibility(View.VISIBLE);
        } else {
            structureTitleTV.setVisibility(View.GONE);
            structureTV.setVisibility(View.GONE);
        }
        //endregion

        //region Initialization
        SpannableString clickable_text;
        Spanned text;
        String display_text;
        String[] structure_info;
        String mainKanji = decomposedKanji.get(0).get(0);

        if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());

        boolean character_not_found_in_KanjiDictDatabase = true;
        for (String characteristic : currentKanjiDetailedCharacteristics) {
            if (!TextUtils.isEmpty(characteristic)) {
                character_not_found_in_KanjiDictDatabase = false;
                break;
            }
        }
        boolean character_is_radical_or_kana = false;
        //endregion

        //region Get the radical characteristics
        String[] radical_row = null;
        if (radicalIndex != -1) {
            radical_row = mRadicalsOnlyDatabase.get(radicalIndex);
            character_is_radical_or_kana = true;
        }
        //endregion

        //region Setting the main Kanji
        kanjiTV.setTypeface(mDroidSansJapaneseTypeface);
        kanjiTV.setTextLocale(Locale.JAPAN);
        kanjiTV.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.colorPrimaryMorePronounced));
        setPrintableKanji(kanjiTV, mainKanji);
        kanjiTV.setHint(mainKanji);
        structure_info = getStructureInfo(decomposedKanji.get(0).get(1));
        structureIV.setBackgroundResource(Integer.parseInt(structure_info[1]));
        //endregion

        //region Setting Radical gallery
        LinearLayout.LayoutParams radical_gallery_layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        radical_gallery_layoutParams.gravity = Gravity.CENTER_VERTICAL|Gravity.START;
        radical_gallery_layoutParams.setMargins(10, 0, 10, 0);
        for (int i = 1; i < decomposedKanji.size(); i++) {

            if (decomposedKanji.get(i).get(0).equals("")) {break;}

            // Add the component to the layout
            final TextView tv = new TextView(getContext());
            tv.setLayoutParams(radical_gallery_layoutParams);
            display_text = decomposedKanji.get(i).get(0);
            text = OvUtilsGeneral.fromHtml("<font color='"+ AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.colorPrimaryMorePronounced)+"'>" + display_text + "</font>");
            clickable_text = new SpannableString(text);

            tv.setTypeface(mDroidSansJapaneseTypeface);
            tv.setTextLocale(Locale.JAPAN);
            setPrintableKanji(tv, clickable_text);
            tv.setHint(decomposedKanji.get(i).get(0));
            tv.setTextIsSelectable(true);
            tv.setFocusable(true);
            //tv.setFocusableInTouchMode(true);
            tv.setOnFocusChangeListener((view, b) -> {

                //Scrolling down (performed twice so that the "post" method operates after fullScroll, thereby forcing a fullScroll
                if (binding.decompositionScrollview != null) {
                    //mScrollY = binding.decompositionScrollView.getScrollY();
                    //if (kanjiListIndex < mInputQueryKanjis.size() - 1)
                    //    binding.decompositionScrollView.smoothScrollTo(0, mScrollY + 100);
                    //else binding.decompositionScrollView.fullScroll(View.FOCUS_DOWN);

//                        binding.decompositionScrollView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (kanjiListIndex < mInputQueryKanjis.size() - 1)
//                                    binding.decompositionScrollView.smoothScrollTo(0, mScrollY + 100);
//                                else binding.decompositionScrollView.fullScroll(View.FOCUS_DOWN);
//                            }
//                        });

                    //int heightPx = decompositionContainer.getHeight();
                    //int heightDpi =  Utilities.convertPxToDpi(heightPx, getContext());
                    //binding.decompositionScrollView.fullScroll(View.FOCUS_DOWN);
                    //binding.decompositionScrollView.smoothScrollTo(0, mScrollY + heightPx/2 - 50); //height divided by 2 because of double-touch caused by touch roomInstancesAsyncResponseHandler
                }
                view.performClick();
            });
            tv.setOnClickListener(view -> {

                String[] tagElements = ((String) tv.getTag()).split(";");
                final int kanjiListIndex1 = Integer.parseInt(tagElements[0]);
                int radicalIteration1 = Integer.parseInt(tagElements[1]);

                //Getting the decomposition
                startGettingDecompositionAsynchronously(tv.getHint().toString(), radicalIteration1 + 1, kanjiListIndex1);

                AndroidUtilitiesIO.showAndFadeOutAndHideImage(binding.decompositionDownArrow, 1000);

            });
            tv.setTag(kanjiListIndex +";"+ radicalIteration +";"+mainKanji);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.DecompKanjiSize));
            tv.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.colorPrimaryMorePronounced));
            tv.setPadding(10,0,10,0);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            radicalGalleryLL.addView(tv);

            if (i < decomposedKanji.size()-1) {
                TextView separatorTV = new TextView(getContext());
                separatorTV.setText("\u00B7");
                separatorTV.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.colorMonochromeContrast));
                separatorTV.setPadding(10,0,10,0);
                separatorTV.setTextSize(30);
                separatorTV.setTypeface(null, Typeface.BOLD);
                radicalGalleryLL.addView(separatorTV);
            }

        }
        //endregion

        //region Setting the characteristics
        structureTV.setText(structure_info[0]);
        if (!character_is_radical_or_kana && character_not_found_in_KanjiDictDatabase && currentKanjiMainRadicalInfo.get(0).equals("")) {
            //Display Characteristics: Meaning of non-Japanese or uncommon Japanese character
            radicalTitleTV.setText(R.string.properties_);
            radicalTV.setText(R.string.this_component_is_a_CJK_character);
        }
        else if (!character_is_radical_or_kana && character_not_found_in_KanjiDictDatabase && !currentKanjiMainRadicalInfo.get(0).equals("")) {
            //Display Main Radical Info only (not readings or meanings)
            radicalTitleTV.setText(R.string.radical_);
            radicalTV.setText(currentKanjiMainRadicalInfo.get(0));
        }
        else if (!character_is_radical_or_kana && !character_not_found_in_KanjiDictDatabase) {
            //Display Main Radical Info (if available)
            if (!currentKanjiMainRadicalInfo.get(0).equals("")) {
                radicalTitleTV.setText(getString(R.string.radical_));
                radicalTV.setText(currentKanjiMainRadicalInfo.get(0));
            }
            else {
                radicalTitleTV.setVisibility(View.GONE);
                radicalTV.setVisibility(View.GONE);
            }

            onReadingTV.setText(currentKanjiDetailedCharacteristics.get(Globals.KANJI_ON_READING));
            kunReadingTV.setText(currentKanjiDetailedCharacteristics.get(Globals.KANJI_KUN_READING));
            nameReadingTV.setText(currentKanjiDetailedCharacteristics.get(Globals.KANJI_NAME_READING));
            meaningTV.setText(currentKanjiDetailedCharacteristics.get(Globals.KANJI_MEANING));
        }
        else if (character_is_radical_or_kana) {
            //region Display Radical or Kana Meanings/Readings
            String radicalValue;
            if (radical_row[2].equals("Hiragana") || radical_row[2].equals("Katakana")) {
                radicalValue = radical_row[2] + " " + radical_row[3] + ".";
                radicalTV.setText(radicalValue);
            }
            else if (radical_row[2].equals("Special")){
                radicalValue = mLocalizedResources.getString(R.string.special_symbol_with_meaning) + " '" + radical_row[3] + "'.";
                radicalTV.setText(radicalValue);
            }
            else {
                //region Get the radical characteristics from the RadialsOnlyDatabase
                List<String> parsed_number = Arrays.asList(mRadicalsOnlyDatabase.get(radicalIndex)[Globals.RADICAL_NUM].split(";"));
                String[] main_radical_row = mRadicalsOnlyDatabase.get(radicalIndexOriginal);
                List<String> radicalVariants = new ArrayList<>();
                for (int i=radicalIndexOriginal; i<radicalIndexOriginal+10; i++) {
                    if (i > mRadicalsOnlyDatabase.size()-1 || i>radicalIndexOriginal && !mRadicalsOnlyDatabase.get(i)[Globals.RADICAL_NUM].contains("variant")) break;
                    String currentKana = mRadicalsOnlyDatabase.get(i)[Globals.RADICAL_KANA];
                    if (!currentKana.equals(mRadicalsOnlyDatabase.get(radicalIndex)[Globals.RADICAL_KANA])) {
                        radicalVariants.add(mRadicalsOnlyDatabase.get(i)[Globals.RADICAL_KANA]);
                    }
                }

                String strokes = " " + mLocalizedResources.getString(R.string.strokes) + ".";
                if (main_radical_row[4].equals("1")) { strokes = " " + mLocalizedResources.getString(R.string.stroke) + ".";}

                radicalValue = "";
                String radicalName = "";
                switch (LocaleHelper.getLanguage(getContext())) {
                    case "en": radicalName = main_radical_row[Globals.RADICAL_NAME_EN]; break;
                    case "fr": radicalName = main_radical_row[Globals.RADICAL_NAME_FR]; break;
                    case "es": radicalName = main_radical_row[Globals.RADICAL_NAME_ES]; break;
                }
                if (parsed_number.size()>1) {
                    switch (parsed_number.get(1)) {
                        case "alt":
                            radicalValue = "\"" + radicalName
                                    + "\" (" + mLocalizedResources.getString(R.string.radical) + " "
                                    + mLocalizedResources.getString(R.string.number_abbrev_) + parsed_number.get(0) + "), "
                                    + main_radical_row[Globals.RADICAL_NUM_STROKES] + strokes;
                            break;
                        case "variant":
                            radicalValue = "\"" + radicalName
                                    + "\" " + mLocalizedResources.getString(R.string.radical_variant)
                                    + " (" +  mLocalizedResources.getString(R.string.radical) + " "
                                    + mLocalizedResources.getString(R.string.number_abbrev_) + parsed_number.get(0) + ").";
                            break;
                        case "simplification":
                            radicalValue = "\"" + radicalName
                                    + "\" (" + mLocalizedResources.getString(R.string.radical) + " "
                                    + mLocalizedResources.getString(R.string.number_abbrev_) + " "
                                    + parsed_number.get(0) + ": " + mLocalizedResources.getString(R.string.simplification) + ").";
                            break;
                    }
                }
                else {
                    radicalValue = "\""+ radicalName
                            + "\" (" + mLocalizedResources.getString(R.string.radical) + " "
                            + mLocalizedResources.getString(R.string.number_abbrev_) + " "
                            + parsed_number.get(0) + "), " + main_radical_row[Globals.RADICAL_NUM_STROKES] + strokes;
                }
                if (radicalVariants.size()>0) {
                    radicalValue += "\nVariants: " + TextUtils.join(", ", radicalVariants);
                }
                radicalTV.setText(radicalValue);
                //endregion

                //region Get the remaining radical characteristics (readings, meanings) from the KanjiDictDatabase
                if (character_not_found_in_KanjiDictDatabase) {
                    currentKanjiDetailedCharacteristics = currentMainRadicalDetailedCharacteristics;
                }
                onReadingTV.setText(currentKanjiDetailedCharacteristics.get(Globals.KANJI_ON_READING));
                kunReadingTV.setText(currentKanjiDetailedCharacteristics.get(Globals.KANJI_KUN_READING));
                nameReadingTV.setText(currentKanjiDetailedCharacteristics.get(Globals.KANJI_NAME_READING));
                meaningTV.setText(currentKanjiDetailedCharacteristics.get(Globals.KANJI_MEANING));
                //endregion
            }
            //endregion
        }
        //endregion

        //region Updating mInputQueryKanjis
        Object[] elements;
        elements = (Object[]) mInputQueryKanjis.get(kanjiListIndex);
        elements[Globals.DECOMP_RADICAL_ITERATION] = radicalIteration;
        mInputQueryKanjis.set(kanjiListIndex, elements);
        //endregion

        //region Adding the decomposition at the correct position in the list while removing the subsequent decompositions
        decompositionContainer.setId(View.generateViewId());
        decompositionContainer.setTag(kanjiListIndex + ";" + radicalIteration);
        if (radicalIteration == 0) removeDecompositionIV.setVisibility(View.GONE);

        modifyViewsAtRadicalIteration(kanjiListIndex, radicalIteration, true, decompositionContainer);

        removeDecompositionIV.setOnClickListener(view -> {
            String[] tags = ((String) decompositionContainer.getTag()).split(";");
            int currentKanjiListIndex = Integer.parseInt(tags[0]);
            int currentRadicalIteration = Integer.parseInt(tags[1]);

            Object[] elements1 = (Object[]) mInputQueryKanjis.get(currentKanjiListIndex);
            elements1[Globals.DECOMP_RADICAL_ITERATION] = currentRadicalIteration - 1;
            mInputQueryKanjis.set(currentKanjiListIndex, elements1);

            View child = binding.decompositionOverallBlockContainer.findViewById(decompositionContainer.getId());
            if (child != null) {
                modifyViewsAtRadicalIteration(currentKanjiListIndex, currentRadicalIteration, false, null);
            }
        });
        //endregion

        //region Setting the border color
        if (getContext()!=null) {
            TypedValue typedValue = new TypedValue();
            if (kanjiListIndex % 2 == 0) {
                getActivity().getTheme().resolveAttribute(R.attr.decomposition_boxPrimary, typedValue, true);
            }
            else {
                getActivity().getTheme().resolveAttribute(R.attr.decomposition_boxSecondary, typedValue, true);
            }
            layout.setBackgroundResource(typedValue.resourceId);
        }
        //endregion
    }
    private void modifyViewsAtRadicalIteration(int kanjiListIndex, int radicalIteration, boolean addView, View decompositionContainer) {
        int childIndex = binding.decompositionOverallBlockContainer.getChildCount()-1;

        if (radicalIteration == 0 && addView) {
            binding.decompositionOverallBlockContainer.addView(decompositionContainer);
            return;
        }

        while (childIndex >= 0) {
            View currentCard = binding.decompositionOverallBlockContainer.getChildAt(childIndex);

            if (currentCard.getTag() == null) {
                childIndex--;
                continue;
            }
            String tagString = ((String) currentCard.getTag());
            if (TextUtils.isEmpty(tagString) || !tagString.contains(";")) {
                childIndex--;
                continue;
            }
            String[] currentCardTags = ((String) currentCard.getTag()).split(";");
            int currentKanjiListIndex = Integer.parseInt(currentCardTags[0]);
            int currentRadicalIteration = Integer.parseInt(currentCardTags[1]);

            if (currentKanjiListIndex == kanjiListIndex) {
                if (currentRadicalIteration >= radicalIteration) binding.decompositionOverallBlockContainer.removeViewAt(childIndex);
            }
            childIndex--;
        }

        if (!addView) return;

        for (int i=0; i<binding.decompositionOverallBlockContainer.getChildCount(); i++) {

            View currentCard = binding.decompositionOverallBlockContainer.getChildAt(i);
            if (currentCard.getTag() == null) continue;

            String tagString = ((String) currentCard.getTag());
            if (TextUtils.isEmpty(tagString) || !tagString.contains(";")) continue;

            String[] currentCardTags = ((String) currentCard.getTag()).split(";");
            int currentKanjiListIndex = Integer.parseInt(currentCardTags[0]);
            int currentRadicalIteration = Integer.parseInt(currentCardTags[1]);

            if (currentKanjiListIndex == kanjiListIndex && currentRadicalIteration + 1 >= radicalIteration) {
                binding.decompositionOverallBlockContainer.addView(decompositionContainer, i+1);
                break;
            }
        }
    }
    private static String[] getStructureInfo(String requested_structure) {
        String structureText = "";
        int structureImage = 0;

        char current_char = 'a';
        char last_char = 'a';
        int starting_index = 0;
        if (requested_structure.equals("c")) {
            structureText = "Character is considered a single component.";
            if (requested_structure.equals("c")) { structureText = "Character is one of the 35 basic CJK strokes.";}
            structureImage = R.drawable.colored_structure_1_original;
        }
        else if (requested_structure.equals("refh") || requested_structure.equals("refr")) {
            structureText = "Component is reflected along a vertical axis (horizontal reflection).";
            structureImage = R.drawable.colored_structure_1_reflected_left_right;
        }
        else if (requested_structure.equals("refv")) {
            structureText = "Component is reflected along a horizontal axis (vertical reflection).";
            structureImage = R.drawable.colored_structure_1_reflected_up_down;
        }
        else if (requested_structure.equals("rot")) {
            structureText = "Component is rotated 180 degrees.";
            structureImage = R.drawable.colored_structure_1_rotation_180;
        }
        else if (requested_structure.equals("w") || requested_structure.equals("wa") || requested_structure.equals("wb") || requested_structure.equals("wbl") || requested_structure.equals("wtr")
                || requested_structure.equals("wtl") || requested_structure.equals("wbr")) {
            structureText = "Second component is located within the first and/or the components overlap.";
            structureImage = R.drawable.colored_structure_2_overlapping;
        }
        else if (requested_structure.equals("a2") || requested_structure.equals("a2m") || requested_structure.equals("a2t")) {
            structureText = "Components are arranged from left to right, and may merge together or touch.";
            structureImage = R.drawable.colored_structure_2_left_right;
        }
        else if (requested_structure.equals("d2") || requested_structure.equals("d2m")|| requested_structure.equals("d2t")) {
            structureText = "Components are arranged from top to bottom, and may merge together or touch.";
            structureImage = R.drawable.colored_structure_2_up_down;
        }
        else if (requested_structure.equals("rrefd")) {
            structureText = "Component is repeated below and its repetition is reflected along a vertical axis (horizontal reflection).";
            structureImage = R.drawable.colored_structure_2_up_down_reflection_up_down;
        }
        else if (requested_structure.equals("rrefl")) {
            structureText = "Component is repeated to the left and its repetition is reflected along a vertical axis (horizontal reflection).";
            structureImage = R.drawable.colored_structure_2_left_right_reflection_on_left;
        }
        else if (requested_structure.equals("rrefr")) {
            structureText = "Component is repeated to the right and its repetition is reflected along a vertical axis (horizontal reflection).";
            structureImage = R.drawable.colored_structure_2_left_right_reflection_on_right;
        }
        else if (requested_structure.equals("rrotd")) {
            structureText = "Component is repeated below and its repetition is reflected along a horizontal axis (vertical reflection).";
            structureImage = R.drawable.colored_structure_2_up_down_rotation_180_on_bottom;
        }
        else if (requested_structure.equals("rrotu")) {
            structureText = "Component is repeated below and its repetition is reflected along a vertical axis (horizontal reflection).";
            structureImage = R.drawable.colored_structure_2_up_down_rotation_180_on_top;
        }
        else if (requested_structure.equals("rrotr")) {
            structureText = "Component is repeated to the right and its repetition is rotated 180 degrees.";
            structureImage = R.drawable.colored_structure_2_left_right_rotation_180_on_right;
        }
        else if (requested_structure.equals("a3")) {
            structureText = "Components are arranged from left to right, and may merge together or touch.";
            structureImage = R.drawable.colored_structure_3_left_center_right;
        }
        else if (requested_structure.equals("a4")) {
            structureText = "Components are arranged from left to right, and may merge together or touch.";
            structureImage = R.drawable.colored_structure_4_left_right;
        }
        else if (requested_structure.equals("d3")) {
            structureText = "Components are arranged from top to bottom, and may merge together or touch.";
            structureImage = R.drawable.colored_structure_3_up_center_down;
        }
        else if (requested_structure.equals("d4")) {
            structureText = "Components are arranged from top to bottom, and may merge together or touch.";
            structureImage = R.drawable.colored_structure_4_up_down;
        }
        else if (requested_structure.equals("r3gw")) {
            structureText = "Component is repeated three times in a triangle pointing downwards.";
            structureImage = R.drawable.colored_structure_3_downwards_triangle;
        }
        else if (requested_structure.equals("r3tr")) {
            structureText = "Component is repeated three times in a triangle pointing upwards.";
            structureImage = R.drawable.colored_structure_3_upwards_triangle;
        }
        else if (requested_structure.equals("4sq")) {
            structureText = "Component is arranged in a square";
            structureImage = R.drawable.colored_structure_4_square;
        }
        else if (requested_structure.equals("r4sq")) {
            structureText = "Component is repeated four times in a square or losange";
            structureImage = R.drawable.colored_structure_4_square_repeat;
        }
        else if (requested_structure.equals("r5")) {
            structureText = "Component is repeated five times.";
            structureImage = R.drawable.colored_structure_5_losange;
        }
        else if (requested_structure.equals("s")) {
            structureText = "First component encloses the others.";
            structureImage = R.drawable.colored_structure_2_outlining;
        }
        else if (requested_structure.equals("sb")) {
            structureText = "First component surrounds the others along the bottom.";
            structureImage = R.drawable.colored_structure_2_enclosing_bottom_to_top;
        }
        else if (requested_structure.equals("sbl")) {
            structureText = "First component surrounds the others along the bottom left.";
            structureImage = R.drawable.colored_structure_2_enclosing_bottomleft_to_topright;
        }
        else if (requested_structure.equals("sbr")) {
            structureText = "First component surrounds the others along the bottom right.";
            structureImage = R.drawable.colored_structure_2_enclosing_bottomright_to_topleft;
        }
        else if (requested_structure.equals("sl")) {
            structureText = "First component surrounds the others along the left.";
            structureImage = R.drawable.colored_structure_2_enclosing_left_to_right;
        }
        else if (requested_structure.equals("sr")) {
            structureText = "First component surrounds the others along the right.";
            structureImage = R.drawable.colored_structure_2_enclosing_right_to_left;
        }
        else if (requested_structure.equals("st") || requested_structure.equals("r3st")) {
            structureText = "First component surrounds the others along the top.";
            structureImage = R.drawable.colored_structure_2_enclosing_top_to_bottom;
        }
        else if (requested_structure.equals("stl") || requested_structure.equals("r3stl")) {
            structureText = "First component surrounds the others along the top left.";
            structureImage = R.drawable.colored_structure_2_enclosing_topleft_to_bottomright;
        }
        else if (requested_structure.equals("str") || requested_structure.equals("r3str")) {
            structureText = "First component surrounds the others along the top right.";
            structureImage = R.drawable.colored_structure_2_enclosing_topright_to_bottomleft;
        }


        if (structureText.equals("")) { structureText = "**No descriptor found**"; }

        String[] structure = {structureText,Integer.toString(structureImage)};
        return structure;
    }

    private void startGettingDecompositionAsynchronously(String inputKanjiAtIndex, int radicalIteration, int kanjiListIndex) {
        if (getActivity()!=null) {
            showLoadingIndicator();
            mKanjiCharacterDecompositionAsyncTask = new KanjiCharacterDecompositionAsyncTask(getContext(), inputKanjiAtIndex, radicalIteration, mRadicalsOnlyDatabase, kanjiListIndex, this);
            mKanjiCharacterDecompositionAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    private void showLoadingIndicator() {
        binding.decompositionLoadingIndicator.setVisibility(View.VISIBLE);
    }
    private void hideLoadingIndicator() {
        binding.decompositionLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    //Communication with AsyncTasks
    @Override public void onKanjiCharacterDecompositionAsyncTaskResultFound(Object data) {

        if (getContext() == null || data == null) return;

        mAlreadyGotFirstQuery = true;

        Object[] dataElements = (Object[]) data;

        // Search for the input in the database and retrieve the result's characteristics
        List<List<String>> decomposedKanji = (List<List<String>>) dataElements[0];
        List<String> currentKanjiDetailedCharacteristics = (List<String>) dataElements[1];
        List<String> currentKanjiMainRadicalInfo = (List<String>) dataElements[2];
        String inputQuery = (String) dataElements[3];
        int radical_iteration = (int) dataElements[4];
        int radicalIndex = (int) dataElements[5];
        int radicalIndexOriginal = (int) dataElements[6];
        List<String> currentMainRadicalDetailedCharacteristics = (List<String>) dataElements[7];
        int kanjiListIndex = (int) dataElements[8];
        hideLoadingIndicator();

        //Displaying the decomposition
        displayDecomposition(
                radical_iteration,
                decomposedKanji,
                currentKanjiDetailedCharacteristics,
                currentKanjiMainRadicalInfo,
                radicalIndex,
                radicalIndexOriginal,
                currentMainRadicalDetailedCharacteristics,
                kanjiListIndex
        );

        //Saving the kanji in the decomposition list
        Object[] elements = (Object[]) mInputQueryKanjis.get(kanjiListIndex);
        mInputQueryKanjis.set(kanjiListIndex, new Object[]{
                elements[Globals.DECOMP_KANJI_LIST_INDEX],
                elements[Globals.DECOMP_RADICAL_ITERATION],
                true
        });

        //Getting the decomposition for the next kanji if relevant
        for (int i=0; i<mInputQueryKanjis.size(); i++) {
            elements = (Object[]) mInputQueryKanjis.get(i);
            if (!((boolean) elements[Globals.DECOMP_PARENT_ALREADY_DISPLAYED])) {
                if (!TextUtils.isEmpty((String) elements[0]))
                    startGettingDecompositionAsynchronously(
                            (String) elements[Globals.DECOMP_KANJI_LIST_INDEX],
                            (int) elements[Globals.DECOMP_RADICAL_ITERATION],
                            i
                    );
                break;
            }
        }
    }

}
