package com.japagram.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.japagram.R;
import com.japagram.asynctasks.VerbSearchAsyncTask;
import com.japagram.data.ConjugationTitle;
import com.japagram.data.InputQuery;
import com.japagram.data.Verb;
import com.japagram.data.Word;
import com.japagram.utilitiesAndroid.UtilitiesAndroidIO;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.resources.LocaleHelper;
import com.japagram.utilitiesAndroid.UtilitiesPrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.japagram.utilitiesCrossPlatform.Globals.TEXT_TYPE_KANJI;

public class ConjugatorFragment extends Fragment implements
        VerbSearchAsyncTask.VerbSearchAsyncResponseHandler {


    //region Parameters
    private static final int MAX_NUM_RESULTS_FOR_SURU_CONJ_SEARCH = 100;

    @BindView(R.id.verb_chooser_spinner) Spinner mVerbChooserSpinner;
    @BindView(R.id.conjugations_chooser_spinner) Spinner mConjugationChooserSpinner;
    @BindView(R.id.verb_hint) TextView mVerbHintTextView;
    @BindView(R.id.verb_results_loading_indicator) ProgressBar mProgressBarLoadingIndicator;
    @BindView(R.id.conjugations_container) ScrollView mConjugationsContainerScrollView;
    @BindView(R.id.radio_romaji_or_kanji) RadioGroup mRomajiOrKanjiRadioButton;
    @BindView(R.id.radio_Romaji) RadioButton mRomajiRadioButton;
    @BindView(R.id.radio_Kanji) RadioButton mKanjiRadioButton;

    @BindView(R.id.Tense0) TextView mConjugationDisplayTense0;
    @BindView(R.id.Tense1) TextView mConjugationDisplayTense1;
    @BindView(R.id.Tense2) TextView mConjugationDisplayTense2;
    @BindView(R.id.Tense3) TextView mConjugationDisplayTense3;
    @BindView(R.id.Tense4) TextView mConjugationDisplayTense4;
    @BindView(R.id.Tense5) TextView mConjugationDisplayTense5;
    @BindView(R.id.Tense6) TextView mConjugationDisplayTense6;
    @BindView(R.id.Tense7) TextView mConjugationDisplayTense7;
    @BindView(R.id.Tense8) TextView mConjugationDisplayTense8;
    @BindView(R.id.Tense9) TextView mConjugationDisplayTense9;
    @BindView(R.id.Tense10) TextView mConjugationDisplayTense10;
    @BindView(R.id.Tense11) TextView mConjugationDisplayTense11;
    @BindView(R.id.Tense12) TextView mConjugationDisplayTense12;
    @BindView(R.id.Tense13) TextView mConjugationDisplayTense13;

    @BindView(R.id.TenseLayout0) LinearLayout mConjugationDisplayTenseLayout0;
    @BindView(R.id.TenseLayout1) LinearLayout mConjugationDisplayTenseLayout1;
    @BindView(R.id.TenseLayout2) LinearLayout mConjugationDisplayTenseLayout2;
    @BindView(R.id.TenseLayout3) LinearLayout mConjugationDisplayTenseLayout3;
    @BindView(R.id.TenseLayout4) LinearLayout mConjugationDisplayTenseLayout4;
    @BindView(R.id.TenseLayout5) LinearLayout mConjugationDisplayTenseLayout5;
    @BindView(R.id.TenseLayout6) LinearLayout mConjugationDisplayTenseLayout6;
    @BindView(R.id.TenseLayout7) LinearLayout mConjugationDisplayTenseLayout7;
    @BindView(R.id.TenseLayout8) LinearLayout mConjugationDisplayTenseLayout8;
    @BindView(R.id.TenseLayout9) LinearLayout mConjugationDisplayTenseLayout9;
    @BindView(R.id.TenseLayout10) LinearLayout mConjugationDisplayTenseLayout10;
    @BindView(R.id.TenseLayout11) LinearLayout mConjugationDisplayTenseLayout11;
    @BindView(R.id.TenseLayout12) LinearLayout mConjugationDisplayTenseLayout12;
    @BindView(R.id.TenseLayout13) LinearLayout mConjugationDisplayTenseLayout13;

    @BindView(R.id.Tense0_Result) TextView mConjugationDisplayTenseResult0;
    @BindView(R.id.Tense1_Result) TextView mConjugationDisplayTenseResult1;
    @BindView(R.id.Tense2_Result) TextView mConjugationDisplayTenseResult2;
    @BindView(R.id.Tense3_Result) TextView mConjugationDisplayTenseResult3;
    @BindView(R.id.Tense4_Result) TextView mConjugationDisplayTenseResult4;
    @BindView(R.id.Tense5_Result) TextView mConjugationDisplayTenseResult5;
    @BindView(R.id.Tense6_Result) TextView mConjugationDisplayTenseResult6;
    @BindView(R.id.Tense7_Result) TextView mConjugationDisplayTenseResult7;
    @BindView(R.id.Tense8_Result) TextView mConjugationDisplayTenseResult8;
    @BindView(R.id.Tense9_Result) TextView mConjugationDisplayTenseResult9;
    @BindView(R.id.Tense10_Result) TextView mConjugationDisplayTenseResult10;
    @BindView(R.id.Tense11_Result) TextView mConjugationDisplayTenseResult11;
    @BindView(R.id.Tense12_Result) TextView mConjugationDisplayTenseResult12;
    @BindView(R.id.Tense13_Result) TextView mConjugationDisplayTenseResult13;

    private Unbinder mBinding;
    private InputQuery mInputQuery;
    private String mChosenRomajiOrKanji;
    private List<Verb> mMatchingVerbs;
    private List<Word> mWordsFromDictFragment;
    private Typeface mDroidSansJapaneseTypeface;
    private List<Object[]> mMatchingConjugationParameters;
    private VerbSearchAsyncTask mVerbSearchAsyncTask;
    //endregion


    //Lifecycle Functions
    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtras();
        initializeParameters();
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //setRetainInstance(true); //causes memory leaks
        View rootView = inflater.inflate(R.layout.fragment_conjugator, container, false);

        mBinding = ButterKnife.bind(this, rootView);

        if (mInputQuery != null && !mInputQuery.isEmpty()) SearchForConjugations();
        else showHint();

        if (getContext()!=null) {
            AssetManager am = getContext().getApplicationContext().getAssets();
            mDroidSansJapaneseTypeface = UtilitiesPrefs.getPreferenceUseJapaneseFont(getActivity()) ?
                    Typeface.createFromAsset(am, String.format(Locale.JAPAN, "fonts/%s", "DroidSansJapanese.ttf")) : Typeface.DEFAULT;
        }

        return rootView;
    }
    @Override public void onAttach(Context context) {
        super.onAttach(context);
        conjugatorFragmentOperationsHandler = (ConjugatorFragmentOperationsHandler) context;
    }
    @Override public void onDetach() {
        super.onDetach();
        if (mVerbSearchAsyncTask != null) mVerbSearchAsyncTask.cancel(true);
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        mBinding.unbind();
        //if (getActivity()!=null && MainApplication.getRefWatcher(getActivity())!=null) MainApplication.getRefWatcher(getActivity()).watch(this);
    }


	//Functionality Functions
    private void getExtras() {
        if (getArguments()!=null) {
            mInputQuery = new InputQuery(Objects.requireNonNull(requireArguments().getString(getString(R.string.user_query_word))));
            //mVerbLatinConjDatabase = (List<String[]>) getArguments().getSerializable(getString(R.string.latin_conj_database));  //Leaving this here for syntax, send serializable to here with new ArrayList<>, not List<>
            mWordsFromDictFragment = getArguments().getParcelableArrayList(getString(R.string.words_list));
        }
    }
    private void initializeParameters() {
        mMatchingVerbs = new ArrayList<>();
        mMatchingConjugationParameters = new ArrayList<>();
    }
    private void SearchForConjugations() {

        hideAll();
        startSearchingForMatchingVerbsInRoomDb();

    }
    private void startSearchingForMatchingVerbsInRoomDb() {
        if (getActivity()!=null) {
            mVerbSearchAsyncTask = new VerbSearchAsyncTask(getContext(), mInputQuery, mWordsFromDictFragment, this);
            mVerbSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            showLoadingIndicator();
        }
    }
    private void displayVerbsInVerbChooserSpinner() {

        if (getActivity()==null) return;

        if (mMatchingVerbs.size() != 0) {
            showResults();
            mVerbChooserSpinner.setAdapter(new VerbSpinnerAdapter(getContext(), R.layout.spinner_item_verb, mMatchingVerbs));
            mVerbChooserSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int verbIndex, long id) {
                    showSelectedVerbConjugations(verbIndex);
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        } else showHint();

    }
    private void showSelectedVerbConjugations(final int verbIndex) {

        if (getActivity()!=null) UtilitiesAndroidIO.hideSoftKeyboard(getActivity());
        if (mMatchingVerbs.size() == 0) return;

        //Showing the verb conjugations
        Verb verb = mMatchingVerbs.get(verbIndex);
        List<Verb.ConjugationCategory> conjugationCategories = verb.getConjugationCategories();
        List<ConjugationTitle> conjugationTitles = new ArrayList<>(Globals.GLOBAL_CONJUGATION_TITLES);
        conjugationTitles.remove(0);
        mConjugationChooserSpinner.setAdapter(new ConjugationsSpinnerAdapter(
                getContext(),
                R.layout.spinner_item_verb_conjugation_category,
                conjugationCategories,
                conjugationTitles));
        mConjugationChooserSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, final int conjugationIndex, long id) {
                showSelectedConjugationsInCategory(verbIndex, conjugationIndex);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        //Hiding the subsequent fields of there is nothing to show
        mConjugationsContainerScrollView.setVisibility(View.VISIBLE);
        if (verb.getConjugationCategories().size() == 0) {
            mConjugationsContainerScrollView.setVisibility(View.GONE);
        }

        //Setting the conjugation spinner to the position of the first item matching the user query
        int index = (int) mMatchingConjugationParameters.get(verbIndex)[Globals.MATCHING_CATEGORY_INDEX];
        mConjugationChooserSpinner.setSelection(index, false);

    }
    private void showSelectedConjugationsInCategory(final int verbIndex, final int conjugationIndex) {

        // Getting the user choice for displaying the conjugations in Romaji or Kanji

        mRomajiOrKanjiRadioButton.setOnCheckedChangeListener((group, checkedId) -> {
            if (getActivity()==null) return;

            RadioButton checkedRadioButton = getActivity().findViewById(checkedId);

            // Define an action depending on the given boolean, ie. depending on the checked RadioButton ID
            mChosenRomajiOrKanji = "";
            switch (checkedRadioButton.getId()) {
                case R.id.radio_Romaji:
                    if (checkedRadioButton.isChecked()) {
                        mChosenRomajiOrKanji = "Romaji";
                    }
                    break;
                case R.id.radio_Kanji:
                    if (checkedRadioButton.isChecked()) {
                        mChosenRomajiOrKanji = "Kanji";
                    }
                    break;
            }

            displayConjugationsOfSelectedCategory(verbIndex, conjugationIndex);
        });

        mChosenRomajiOrKanji = "Romaji";
        if (mInputQuery.getOriginalType() == TEXT_TYPE_KANJI) {
            mChosenRomajiOrKanji = "Kanji";
            mRomajiRadioButton.setChecked(false);
            mKanjiRadioButton.setChecked(true);
        } else {
            mRomajiRadioButton.setChecked(true);
            mKanjiRadioButton.setChecked(false);
        }

        displayConjugationsOfSelectedCategory(verbIndex, conjugationIndex);
    }
    private void displayConjugationsOfSelectedCategory(int verbIndex, int conjugationIndex) {
        if (getActivity()==null) return;

        List<TextView> Tense = new ArrayList<>();
        List<LinearLayout> TenseLayout = new ArrayList<>();
        List<TextView> Tense_Result = new ArrayList<>();

        Tense.add(mConjugationDisplayTense0);
        Tense.add(mConjugationDisplayTense1);
        Tense.add(mConjugationDisplayTense2);
        Tense.add(mConjugationDisplayTense3);
        Tense.add(mConjugationDisplayTense4);
        Tense.add(mConjugationDisplayTense5);
        Tense.add(mConjugationDisplayTense6);
        Tense.add(mConjugationDisplayTense7);
        Tense.add(mConjugationDisplayTense8);
        Tense.add(mConjugationDisplayTense9);
        Tense.add(mConjugationDisplayTense10);
        Tense.add(mConjugationDisplayTense11);
        Tense.add(mConjugationDisplayTense12);
        Tense.add(mConjugationDisplayTense13);

        TenseLayout.add(mConjugationDisplayTenseLayout0);
        TenseLayout.add(mConjugationDisplayTenseLayout1);
        TenseLayout.add(mConjugationDisplayTenseLayout2);
        TenseLayout.add(mConjugationDisplayTenseLayout3);
        TenseLayout.add(mConjugationDisplayTenseLayout4);
        TenseLayout.add(mConjugationDisplayTenseLayout5);
        TenseLayout.add(mConjugationDisplayTenseLayout6);
        TenseLayout.add(mConjugationDisplayTenseLayout7);
        TenseLayout.add(mConjugationDisplayTenseLayout8);
        TenseLayout.add(mConjugationDisplayTenseLayout9);
        TenseLayout.add(mConjugationDisplayTenseLayout10);
        TenseLayout.add(mConjugationDisplayTenseLayout11);
        TenseLayout.add(mConjugationDisplayTenseLayout12);
        TenseLayout.add(mConjugationDisplayTenseLayout13);

        Tense_Result.add(mConjugationDisplayTenseResult0);
        Tense_Result.add(mConjugationDisplayTenseResult1);
        Tense_Result.add(mConjugationDisplayTenseResult2);
        Tense_Result.add(mConjugationDisplayTenseResult3);
        Tense_Result.add(mConjugationDisplayTenseResult4);
        Tense_Result.add(mConjugationDisplayTenseResult5);
        Tense_Result.add(mConjugationDisplayTenseResult6);
        Tense_Result.add(mConjugationDisplayTenseResult7);
        Tense_Result.add(mConjugationDisplayTenseResult8);
        Tense_Result.add(mConjugationDisplayTenseResult9);
        Tense_Result.add(mConjugationDisplayTenseResult10);
        Tense_Result.add(mConjugationDisplayTenseResult11);
        Tense_Result.add(mConjugationDisplayTenseResult12);
        Tense_Result.add(mConjugationDisplayTenseResult13);

        for (int i=0;i<Tense.size();i++) {
            Tense.get(i).setText("");
            Tense.get(i).setPadding(0,8,0,0);
            TenseLayout.get(i).setVisibility(View.GONE);
            Tense_Result.get(i).setText("");
            if (mChosenRomajiOrKanji.equals("Romaji")) Tense_Result.get(i).setTypeface(null, Typeface.BOLD);
            else if (mDroidSansJapaneseTypeface!=null) {
                Tense_Result.get(i).setTypeface(mDroidSansJapaneseTypeface);
            }
        }

        Verb verb = mMatchingVerbs.get(verbIndex);
        Verb.ConjugationCategory conjugationCategory = verb.getConjugationCategories().get(conjugationIndex);
        List<Verb.ConjugationCategory.Conjugation> conjugations = conjugationCategory.getConjugations();

        for (int i = 0; i < conjugations.size(); i++) {

            Tense.get(i).setText(Globals.GLOBAL_CONJUGATION_TITLES.get(conjugationIndex+1).getSubtitles().get(i).getTense());

            if (mChosenRomajiOrKanji.equals("Romaji")) Tense_Result.get(i).setText(conjugations.get(i).getConjugationLatin());
            else Tense_Result.get(i).setText(conjugations.get(i).getConjugationKanji());

            TenseLayout.get(i).setVisibility(View.VISIBLE);
        }
    }
    private void showLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.VISIBLE);
    }
    private void hideLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.INVISIBLE);
    }
    private void hideAll() {
        mVerbHintTextView.setVisibility(View.GONE);
        mVerbChooserSpinner.setVisibility(View.GONE);
        mConjugationsContainerScrollView.setVisibility(View.GONE);
    }
    private void showHint() {
        mVerbHintTextView.setVisibility(View.VISIBLE);
        mVerbChooserSpinner.setVisibility(View.GONE);
        mConjugationsContainerScrollView.setVisibility(View.GONE);
    }
    private void showResults() {
        mVerbHintTextView.setVisibility(View.GONE);
        mVerbChooserSpinner.setVisibility(View.VISIBLE);
        mConjugationsContainerScrollView.setVisibility(View.VISIBLE);
    }

    private class VerbSpinnerAdapter extends ArrayAdapter<Verb> {
        // Code adapted from http://mrbool.com/how-to-customize-spinner-in-android/28286

        final List<Verb> verbs;

        VerbSpinnerAdapter(Context ctx, int txtViewResourceId, List<Verb> verbs) {
            super(ctx, txtViewResourceId, verbs);
            this.verbs = verbs;
        }
        @Override public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }
        @NonNull @Override public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }
        View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(getContext());
            View mySpinner = inflater.inflate(R.layout.spinner_item_verb, parent, false);

            String SpinnerText;
            Verb verb = verbs.get(position);

            //Setting the preposition
            if (verb.getPreposition().equals("")) SpinnerText = "";
            else SpinnerText = "[" + verb.getPreposition() + "] ";
            TextView verbchooser_Prep = mySpinner.findViewById(R.id.verbchooser_Prep);
            verbchooser_Prep.setText(SpinnerText);
            verbchooser_Prep.setTextColor(UtilitiesPrefs.getResColorValue(getContext(), R.attr.textConjugatorVerbChooserPrepositionColor));


            //Setting the Kanji and Romaji
            SpinnerText = verb.getKanji() + " (" + verb.getRomaji() + ")";
            TextView verbchooser_Kanji_and_ustem = mySpinner.findViewById(R.id.verbchooser_Kanji_and_ustem);
            verbchooser_Kanji_and_ustem.setText(SpinnerText);
            verbchooser_Kanji_and_ustem.setTextColor(UtilitiesPrefs.getResColorValue(getContext(), R.attr.textConjugatorVerbChooserKanjiRomajiColor));

            //If the verb was found by an altSpelling, update the Romaji/Kanji title
            if (!verb.getActiveAltSpelling().equals("")
                    && !(verb.getActiveAltSpelling().equals(verb.getKanji())
                        || verb.getActiveAltSpelling().equals(verb.getRomaji()))
                    ) {
                SpinnerText = SpinnerText + ": "+getString(R.string.alt_form)+" [" + verb.getActiveAltSpelling() + "]";
                verbchooser_Kanji_and_ustem.setText(SpinnerText);
            }

            //Setting the trans./intrans.
            if (!verb.getFamily().equals("")) {
                SpinnerText = verb.getFamily();
                if (!verb.getTrans().equals("") && LocaleHelper.getLanguage(getContext()).equals("en")) SpinnerText = SpinnerText + ", " + verb.getTrans();
            }
            else {
                if (!verb.getTrans().equals("")) SpinnerText = verb.getTrans();
                else { SpinnerText = ""; }
            }
            TextView verbchooser_Characteristics = mySpinner.findViewById(R.id.verbchooser_Characteristics);
            verbchooser_Characteristics.setText(SpinnerText);
            verbchooser_Characteristics.setTextColor(UtilitiesPrefs.getResColorValue(getContext(), R.attr.textConjugatorVerbChooserCharacteristicsColor));

            //Setting the meaning
            SpinnerText = verb.getMeaning();
            TextView verbchooser_LatinMeaning = mySpinner.findViewById(R.id.verbchooser_LatinMeaning);
            verbchooser_LatinMeaning.setText(SpinnerText);
            verbchooser_LatinMeaning.setTextColor(UtilitiesPrefs.getResColorValue(getContext(), R.attr.textConjugatorVerbChooserMeaningColor));

            return mySpinner;
        }
    }
    private class ConjugationsSpinnerAdapter extends ArrayAdapter<Verb.ConjugationCategory> {
        // Code adapted from http://mrbool.com/how-to-customize-spinner-in-android/28286

        final List<Verb.ConjugationCategory> conjugationCategories;
        private final List<ConjugationTitle> conjugationTitles;

        ConjugationsSpinnerAdapter(Context ctx, int txtViewResourceId,
                                   List<Verb.ConjugationCategory> conjugationCategories,
                                   List<ConjugationTitle> conjugationTitles) {
            super(ctx, txtViewResourceId, conjugationCategories);
            this.conjugationCategories = conjugationCategories;
            this.conjugationTitles = conjugationTitles;
        }

        @Override public View getDropDownView(int position, View cnvtView, @NonNull ViewGroup parent) {
            return getCustomView(position, cnvtView, parent);
        }

        @NonNull @Override public View getView(int pos, View cnvtView, @NonNull ViewGroup parent) {
            return getCustomView(pos, cnvtView, parent);
        }

        View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(getContext());
            View mySpinner = inflater.inflate(R.layout.spinner_item_verb_conjugation_category, parent, false);

            String SpinnerText;
            Verb.ConjugationCategory conjugationCategory = conjugationCategories.get(position);

            //Setting the title
            int shownPosition = position+1;
            SpinnerText = shownPosition + ". " + conjugationTitles.get(position).getTitle();
            TextView Upper_text = mySpinner.findViewById(R.id.UpperPart);
            Upper_text.setText(SpinnerText);
            Upper_text.setTypeface(mDroidSansJapaneseTypeface, Typeface.BOLD);
            Upper_text.setTextColor(UtilitiesPrefs.getResColorValue(getContext(), R.attr.textConjugatorConjugationChooserUpperColor));

            //Displaying the first element in the Conjugation, e.g. PrPlA in Simple Form
            SpinnerText = conjugationCategory.getConjugations().get(0).getConjugationLatin();
            TextView Lower_text = mySpinner.findViewById(R.id.LowerPart);
            Lower_text.setText(SpinnerText);
            Lower_text.setTextColor(UtilitiesPrefs.getResColorValue(getContext(), R.attr.textConjugatorConjugationChooserLowerColor));

            return mySpinner;
        }
    }


    //Communication with parent activity
    private ConjugatorFragmentOperationsHandler conjugatorFragmentOperationsHandler;
    interface ConjugatorFragmentOperationsHandler {
        void onMatchingVerbsFoundInConjSearch(List<Word> matchingVerbsAsWords);
    }

    //Communication with AsyncTasks
    @Override public void onVerbSearchAsyncTaskResultFound(Object[] dataElements) {

        mMatchingVerbs = (List<Verb>) dataElements[0];
        List<Word> matchingWords = (List<Word>) dataElements[1];
        mMatchingConjugationParameters = (List<Object[]>) dataElements[2];

        conjugatorFragmentOperationsHandler.onMatchingVerbsFoundInConjSearch(matchingWords);

        //Displaying the local results
        hideLoadingIndicator();
        displayVerbsInVerbChooserSpinner();
    }
}