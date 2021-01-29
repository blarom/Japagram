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
import android.widget.RadioButton;
import android.widget.TextView;

import com.japagram.R;
import com.japagram.asynctasks.VerbSearchAsyncTask;
import com.japagram.data.ConjugationTitle;
import com.japagram.data.InputQuery;
import com.japagram.data.Verb;
import com.japagram.data.Word;
import com.japagram.databinding.FragmentConjugatorBodyBinding;
import com.japagram.resources.LocaleHelper;
import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;
import com.japagram.utilitiesCrossPlatform.Globals;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.japagram.utilitiesCrossPlatform.Globals.TEXT_TYPE_KANJI;

public class ConjugatorFragment extends Fragment implements
        VerbSearchAsyncTask.VerbSearchAsyncResponseHandler {


    //region Parameters
    private static final int MAX_NUM_RESULTS_FOR_SURU_CONJ_SEARCH = 100;

    private FragmentConjugatorBodyBinding binding;
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

        if (getContext()!=null) {
            AssetManager am = getContext().getApplicationContext().getAssets();
            mDroidSansJapaneseTypeface = AndroidUtilitiesPrefs.getPreferenceUseJapaneseFont(getActivity()) ?
                    Typeface.createFromAsset(am, String.format(Locale.JAPAN, "fonts/%s", "DroidSansJapanese.ttf")) : Typeface.DEFAULT;
        }

        return rootView;
    }
    @Override public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentConjugatorBodyBinding.bind(view);
        if (mInputQuery != null && !mInputQuery.isEmpty()) SearchForConjugations();
        else showHint();
    }
    @Override public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        conjugatorFragmentOperationsHandler = (ConjugatorFragmentOperationsHandler) context;
    }
    @Override public void onDetach() {
        super.onDetach();
        if (mVerbSearchAsyncTask != null) mVerbSearchAsyncTask.cancel(true);
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
            binding.conjugatorVerbChooser.setAdapter(new VerbSpinnerAdapter(getContext(), R.layout.spinner_item_verb, mMatchingVerbs));
            binding.conjugatorVerbChooser.setOnItemSelectedListener(new OnItemSelectedListener() {
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

        if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
        if (mMatchingVerbs.size() == 0) return;

        //Showing the verb conjugations
        Verb verb = mMatchingVerbs.get(verbIndex);
        List<Verb.ConjugationCategory> conjugationCategories = verb.getConjugationCategories();
        List<ConjugationTitle> conjugationTitles = new ArrayList<>(Globals.GLOBAL_CONJUGATION_TITLES);
        conjugationTitles.remove(0);
        binding.conjugatorConjugationsChooser.setAdapter(new ConjugationsSpinnerAdapter(
                getContext(),
                R.layout.spinner_item_verb_conjugation_category,
                conjugationCategories,
                conjugationTitles));
        binding.conjugatorConjugationsChooser.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, final int conjugationIndex, long id) {
                showSelectedConjugationsInCategory(verbIndex, conjugationIndex);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        //Hiding the subsequent fields of there is nothing to show
        binding.conjugatorConjugationsContainer.setVisibility(View.VISIBLE);
        if (verb.getConjugationCategories().size() == 0) {
            binding.conjugatorConjugationsContainer.setVisibility(View.GONE);
        }

        //Setting the conjugation spinner to the position of the first item matching the user query
        int index = (int) mMatchingConjugationParameters.get(verbIndex)[Globals.MATCHING_CATEGORY_INDEX];
        binding.conjugatorConjugationsChooser.setSelection(index, false);

    }
    private void showSelectedConjugationsInCategory(final int verbIndex, final int conjugationIndex) {

        // Getting the user choice for displaying the conjugations in Romaji or Kanji

        binding.conjugatorRomajiOrKanji.setOnCheckedChangeListener((group, checkedId) -> {
            if (getActivity()==null) return;

            RadioButton checkedRadioButton = getActivity().findViewById(checkedId);

            // Define an action depending on the given boolean, ie. depending on the checked RadioButton ID
            mChosenRomajiOrKanji = "";
            int id = checkedRadioButton.getId();
            if (id == R.id.conjugator_radio_romaji) {
                if (checkedRadioButton.isChecked()) {
                    mChosenRomajiOrKanji = "Romaji";
                }
            } else if (id == R.id.conjugator_radio_kanji) {
                if (checkedRadioButton.isChecked()) {
                    mChosenRomajiOrKanji = "Kanji";
                }
            }

            displayConjugationsOfSelectedCategory(verbIndex, conjugationIndex);
        });

        mChosenRomajiOrKanji = "Romaji";
        if (mInputQuery.getOriginalType() == TEXT_TYPE_KANJI) {
            mChosenRomajiOrKanji = "Kanji";
            binding.conjugatorRadioRomaji.setChecked(false);
            binding.conjugatorRadioKanji.setChecked(true);
        } else {
            binding.conjugatorRadioRomaji.setChecked(true);
            binding.conjugatorRadioKanji.setChecked(false);
        }

        displayConjugationsOfSelectedCategory(verbIndex, conjugationIndex);
    }
    private void displayConjugationsOfSelectedCategory(int verbIndex, int conjugationIndex) {
        if (getActivity()==null) return;

        List<TextView> Tense = new ArrayList<>();
        List<LinearLayout> TenseLayout = new ArrayList<>();
        List<TextView> Tense_Result = new ArrayList<>();

        Tense.add(binding.conjugatorTense0);
        Tense.add(binding.conjugatorTense1);
        Tense.add(binding.conjugatorTense2);
        Tense.add(binding.conjugatorTense3);
        Tense.add(binding.conjugatorTense4);
        Tense.add(binding.conjugatorTense5);
        Tense.add(binding.conjugatorTense6);
        Tense.add(binding.conjugatorTense7);
        Tense.add(binding.conjugatorTense8);
        Tense.add(binding.conjugatorTense9);
        Tense.add(binding.conjugatorTense10);
        Tense.add(binding.conjugatorTense11);
        Tense.add(binding.conjugatorTense12);
        Tense.add(binding.conjugatorTense13);

        TenseLayout.add(binding.conjugatorTense0Layout);
        TenseLayout.add(binding.conjugatorTense1Layout);
        TenseLayout.add(binding.conjugatorTense2Layout);
        TenseLayout.add(binding.conjugatorTense3Layout);
        TenseLayout.add(binding.conjugatorTense4Layout);
        TenseLayout.add(binding.conjugatorTense5Layout);
        TenseLayout.add(binding.conjugatorTense6Layout);
        TenseLayout.add(binding.conjugatorTense7Layout);
        TenseLayout.add(binding.conjugatorTense8Layout);
        TenseLayout.add(binding.conjugatorTense9Layout);
        TenseLayout.add(binding.conjugatorTense10Layout);
        TenseLayout.add(binding.conjugatorTense11Layout);
        TenseLayout.add(binding.conjugatorTense12Layout);
        TenseLayout.add(binding.conjugatorTense13Layout);

        Tense_Result.add(binding.conjugatorTense0Result);
        Tense_Result.add(binding.conjugatorTense1Result);
        Tense_Result.add(binding.conjugatorTense2Result);
        Tense_Result.add(binding.conjugatorTense3Result);
        Tense_Result.add(binding.conjugatorTense4Result);
        Tense_Result.add(binding.conjugatorTense5Result);
        Tense_Result.add(binding.conjugatorTense6Result);
        Tense_Result.add(binding.conjugatorTense7Result);
        Tense_Result.add(binding.conjugatorTense8Result);
        Tense_Result.add(binding.conjugatorTense9Result);
        Tense_Result.add(binding.conjugatorTense10Result);
        Tense_Result.add(binding.conjugatorTense11Result);
        Tense_Result.add(binding.conjugatorTense12Result);
        Tense_Result.add(binding.conjugatorTense13Result);

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

            Tense.get(i).setText(Globals.GLOBAL_CONJUGATION_TITLES.get(conjugationIndex+1).getSubtitles().get(i).getSubTitle());

            if (mChosenRomajiOrKanji.equals("Romaji")) Tense_Result.get(i).setText(conjugations.get(i).getConjugationLatin());
            else Tense_Result.get(i).setText(conjugations.get(i).getConjugationKanji());

            TenseLayout.get(i).setVisibility(View.VISIBLE);
        }
    }
    private void showLoadingIndicator() {
        binding.conjugatorResultsLoadingIndicator.setVisibility(View.VISIBLE);
    }
    private void hideLoadingIndicator() {
        binding.conjugatorResultsLoadingIndicator.setVisibility(View.INVISIBLE);
    }
    private void hideAll() {
        binding.conjugatorHint.setVisibility(View.GONE);
        binding.conjugatorVerbChooser.setVisibility(View.GONE);
        binding.conjugatorConjugationsContainer.setVisibility(View.GONE);
    }
    private void showHint() {
        binding.conjugatorHint.setVisibility(View.VISIBLE);
        binding.conjugatorVerbChooser.setVisibility(View.GONE);
        binding.conjugatorConjugationsContainer.setVisibility(View.GONE);
    }
    private void showResults() {
        binding.conjugatorHint.setVisibility(View.GONE);
        binding.conjugatorVerbChooser.setVisibility(View.VISIBLE);
        binding.conjugatorConjugationsContainer.setVisibility(View.VISIBLE);
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
            verbchooser_Prep.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.textConjugatorVerbChooserPrepositionColor));


            //Setting the Kanji and Romaji
            SpinnerText = verb.getKanji() + " (" + verb.getRomaji() + ")";
            TextView verbchooser_Kanji_and_ustem = mySpinner.findViewById(R.id.verbchooser_Kanji_and_ustem);
            verbchooser_Kanji_and_ustem.setText(SpinnerText);
            verbchooser_Kanji_and_ustem.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.textConjugatorVerbChooserKanjiRomajiColor));

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
            verbchooser_Characteristics.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.textConjugatorVerbChooserCharacteristicsColor));

            //Setting the meaning
            SpinnerText = verb.getMeaning();
            TextView verbchooser_LatinMeaning = mySpinner.findViewById(R.id.verbchooser_LatinMeaning);
            verbchooser_LatinMeaning.setText(SpinnerText);
            verbchooser_LatinMeaning.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.textConjugatorVerbChooserMeaningColor));

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
            Upper_text.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.textConjugatorConjugationChooserUpperColor));

            //Displaying the first element in the Conjugation, e.g. PrPlA in Simple Form
            SpinnerText = conjugationCategory.getConjugations().get(0).getConjugationLatin();
            TextView Lower_text = mySpinner.findViewById(R.id.LowerPart);
            Lower_text.setText(SpinnerText);
            Lower_text.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.textConjugatorConjugationChooserLowerColor));

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