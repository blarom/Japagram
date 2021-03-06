package com.japagram.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.japagram.R;
import com.japagram.data.InputQuery;
import com.japagram.data.Word;
import com.japagram.databinding.ListItemDictonaryBinding;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesCrossPlatform.UtilitiesDictSearch;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;
import com.japagram.utilitiesPlatformOverridable.OvUtilsGeneral;
import com.japagram.utilitiesPlatformOverridable.OvUtilsResources;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class DictionaryRecyclerViewAdapter extends RecyclerView.Adapter<DictionaryRecyclerViewAdapter.DictItemViewHolder> {

    private static final String RULE_DELIMITER = "@";
    public static final int DETAILS_LEFT_PADDING = 12;
    public static final int EXAMPLES_LEFT_PADDING = 20;
    public static final int PARENT_VISIBILITY = 0;
    public static final int EXPLANATION_VISIBILITIES = 1;
    private final Context mContext;
    private final String mInputQuery;
    private final int mInputQueryTextType;
    private final String mInputQueryFirstLetter;
    private final String mInputQueryNoSpaces;
    private final String mInputQueryLatin;
    private final String mLanguageFromResource;
    private final boolean[] mActiveMeaningLanguages;
    private Object[][] mVisibilitiesRegister;
    private List<Word> mWordsList;
    final private DictionaryItemClickHandler mOnItemClickHandler;
    private final Typeface mDroidSansJapaneseTypeface;
    private List<String> mWordsRomajiAndKanji;
    private List<String> mWordsSourceInfo;
    private List<Spanned> mWordsMeaningExtract;
    private List<Boolean> mWordsTypeIsVerb;
    private final LinearLayout.LayoutParams mChildLineParams;
    private final LinearLayout.LayoutParams mubChildLineParams;
    private boolean mShowSources = false;
    private final String mLanguage;

    public DictionaryRecyclerViewAdapter(Context context,
                                         DictionaryItemClickHandler listener,
                                         List<Word> wordsList,
                                         @NotNull InputQuery inputQuery,
                                         String language,
                                         Typeface typeface) {
        this.mContext = context;
        this.mWordsList = wordsList;
        this.mOnItemClickHandler = listener;
        this.mInputQuery = inputQuery.getOriginal();
        this.mLanguage = language;
        createVisibilityArray();

        mInputQueryTextType = inputQuery.getOriginalType();
        mInputQueryFirstLetter = (OvUtilsGeneral.isEmptyString(inputQuery.getOriginal())) ? "" : inputQuery.getOriginal().substring(0,1);
        mInputQueryNoSpaces = inputQuery.getOriginal().replace(" ","");
        mInputQueryLatin = inputQuery.getWaapuroConversions().size() > 0? inputQuery.getWaapuroConversions().get(0): "";

        switch (mLanguage) {
            case Globals.LANG_STR_EN:
                mLanguageFromResource = mContext.getResources().getString(R.string.language_label_english);
                break;
            case Globals.LANG_STR_FR:
                mLanguageFromResource = mContext.getResources().getString(R.string.language_label_french);
                break;
            case Globals.LANG_STR_ES:
                mLanguageFromResource = mContext.getResources().getString(R.string.language_label_spanish);
                break;
            default: mLanguageFromResource = mContext.getResources().getString(R.string.language_label_english);
        }
        mDroidSansJapaneseTypeface = typeface;

        mChildLineParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, 2 );
        mChildLineParams.setMargins(0, 16, 0, 16);
        mubChildLineParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, 2 );
        mubChildLineParams.setMargins(128, 16, 128, 4);

        mActiveMeaningLanguages = new boolean[]{true, false, false};

        prepareLayoutTexts();
    }

    @NonNull @Override public DictItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_dictonary, parent, false);
        view.setFocusable(true);
        return new DictItemViewHolder(view);
    }
    @Override public void onBindViewHolder(@NonNull final DictItemViewHolder holder, int position) {

        //region Setting behavior when element is clicked
        if ((boolean) mVisibilitiesRegister[position][PARENT_VISIBILITY]) {
            holder.binding.listItemChildLinearlayout.setVisibility(View.VISIBLE);
            holder.binding.listItemMeanings.setVisibility(View.GONE);
            holder.binding.listItemSourceInfo.setVisibility(View.GONE);
            holder.binding.listItemDictionary.setBackgroundColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictChildBackgroundColor));
        }
        else {
            holder.binding.listItemChildLinearlayout.setVisibility(View.GONE);
            holder.binding.listItemMeanings.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mWordsSourceInfo.get(position))) holder.binding.listItemSourceInfo.setVisibility(View.VISIBLE);
            holder.binding.listItemDictionary.setBackgroundColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictParentBackgroundColor));
        }

        holder.binding.listItemRomajiAndKanji.setOnClickListener(view -> {
            TypedValue typedValue = new TypedValue();
            if (holder.binding.listItemChildLinearlayout.getVisibility() == View.VISIBLE) {
                holder.binding.listItemChildLinearlayout.setVisibility(View.GONE);
                holder.binding.listItemMeanings.setVisibility(View.VISIBLE);
                holder.binding.listItemDictionary.setBackgroundColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictParentBackgroundColor));
                mContext.getTheme().resolveAttribute(R.attr.dictionaryDropDownArrow, typedValue, true);
                holder.binding.dropdownArrow.setImageDrawable(mContext.getResources().getDrawable(typedValue.resourceId));
                mVisibilitiesRegister[holder.getAdapterPosition()][PARENT_VISIBILITY] = false;
            }
            else {
                holder.binding.listItemChildLinearlayout.setVisibility(View.VISIBLE);
                holder.binding.listItemMeanings.setVisibility(View.GONE);
                holder.binding.listItemDictionary.setBackgroundColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictChildBackgroundColor));
                mContext.getTheme().resolveAttribute(R.attr.dictionaryDropUpArrow, typedValue, true);
                holder.binding.dropdownArrow.setImageDrawable(mContext.getResources().getDrawable(typedValue.resourceId));
                mVisibilitiesRegister[holder.getAdapterPosition()][PARENT_VISIBILITY] = true;
            }
        });

        if (TextUtils.isEmpty(mWordsSourceInfo.get(position))) holder.binding.listItemSourceInfo.setVisibility(View.GONE);
        else {
            holder.binding.listItemSourceInfo.setVisibility(View.VISIBLE);
            holder.binding.listItemSourceInfo.setText(mWordsSourceInfo.get(position));
        }
        //endregion

        //region Updating the parent values
        String romaji = mWordsList.get(position).getRomaji();
        String kanji = mWordsList.get(position).getKanji();
        String altSpellings = TextUtils.join(", ", mWordsList.get(position).getAltSpellings().split(Globals.DB_ELEMENTS_DELIMITER));
        int frequency = mWordsList.get(position).getFrequency();

        holder.binding.listItemRomajiAndKanji.setText(mWordsRomajiAndKanji.get(position));
        holder.binding.listItemRomajiAndKanji.setTypeface(mDroidSansJapaneseTypeface, Typeface.BOLD);
        holder.binding.listItemRomajiAndKanji.setPadding(0,16,0,4);

        if (romaji.equals("") && kanji.equals("")) { holder.binding.listItemRomajiAndKanji.setVisibility(View.GONE); }
        else { holder.binding.listItemRomajiAndKanji.setVisibility(View.VISIBLE); }

        setMeaningsTvProperties(holder.binding.listItemMeanings, mWordsMeaningExtract.get(position));
        //endregion

        //region Updating the child values

        //region Initialization
        holder.binding.listItemChildElementsContainer.removeAllViews();
        holder.binding.listItemChildElementsContainer.setFocusable(false);
        holder.binding.listItemConjugateHyperlinkKanji.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionaryChildItemKanjiColor));
        holder.binding.listItemRomajiAndKanji.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorAccentDark));
        holder.binding.listItemSourceInfo.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionaryChildItemKanjiColor));
        holder.binding.listItemMeanings.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimary));
        holder.binding.listItemMeanings.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimary));
        holder.binding.listItemChildLinearlayout.setBackgroundColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictChildBackgroundColor));
        holder.binding.listItemConjugateHyperlinkRomaji.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionaryChildItemRomajiColor));
        //endregion

        //region Handling the hyperlink values for user click
        if (mWordsTypeIsVerb.get(position) && mWordsList.get(position).getIsLocal()) {
            holder.binding.listItemConjugateHyperlinkRomaji.setVisibility(View.VISIBLE);
            holder.binding.listItemConjugateHyperlinkKanji.setVisibility(View.VISIBLE);
        }
        else {
            holder.binding.listItemConjugateHyperlinkRomaji.setVisibility(View.GONE);
            holder.binding.listItemConjugateHyperlinkKanji.setVisibility(View.GONE);
        }

        if (mWordsTypeIsVerb.get(position) && romaji.length() > 0 && kanji.length() > 0) {
            setHyperlinksInConjugateLine(holder.binding.listItemConjugateHyperlinkRomaji, mContext.getString(R.string.conjugate)+" ", romaji, " ");
            setHyperlinksInConjugateLine(holder.binding.listItemConjugateHyperlinkKanji, "(", kanji, ").");
        }

        holder.binding.listItemDecomposeHyperlink.setVisibility(View.VISIBLE);
        if (kanji.length() > 0) {
            setHyperlinkInDecomposeLine(holder.binding.listItemDecomposeHyperlink, mContext.getString(R.string.decompose)+" ", kanji, ".");
        }
        //endregion

        //Setting the frequency
        String type = mWordsList.get(position).getMeaningsEN().get(0).getType();
        if (!type.equals("CE")) {
            String freqHtmlText = mContext.getString(R.string.frequency) + ": " + ((frequency == 0) ? "20001+ (uncommon)" : frequency);
            TextView freqTtv = addHeaderField(holder.binding.listItemChildElementsContainer, OvUtilsGeneral.fromHtml(freqHtmlText));
            freqTtv.setPadding(0, 16, 0, 16);
        }
        //endregion

        //region Setting the alternate spellings
        if (!TextUtils.isEmpty(altSpellings)) {
            //String htmlText = "<b>" + mContext.getString(R.string.alternate_forms_) + "</b> " + alternatespellings;
            String altSHtmlText = mContext.getString(R.string.alternate_forms) + ": " + altSpellings;
            TextView altSTv = addHeaderField(holder.binding.listItemChildElementsContainer, OvUtilsGeneral.fromHtml(altSHtmlText));
            altSTv.setPadding(0, 16, 0, 16);
        }
        //endregion

        //region Setting the wordMeaning elements
        switch (mLanguage) {
            case Globals.LANG_STR_EN:
                if (mActiveMeaningLanguages[Globals.LANG_EN]) setMeaningsLayout(position, holder, Globals.LANG_STR_EN);
                break;
            case Globals.LANG_STR_FR:
                if (mActiveMeaningLanguages[Globals.LANG_EN]) setMeaningsLayout(position, holder, Globals.LANG_STR_FR);
                break;
            case Globals.LANG_STR_ES:
                if (mActiveMeaningLanguages[Globals.LANG_EN]) setMeaningsLayout(position, holder, Globals.LANG_STR_ES);
                break;
        }
        //endregion

        //endregion

    }

    private void prepareLayoutTexts() {

        mWordsRomajiAndKanji = new ArrayList<>();
        mWordsSourceInfo = new ArrayList<>();
        mWordsMeaningExtract = new ArrayList<>();
        mWordsTypeIsVerb = new ArrayList<>();
        List<Word.Meaning> meanings;
        boolean onlyEnglishMeaningsAvailable;

        if (mWordsList == null) return;

        for (Word word : mWordsList) {

            //region Getting the word characteristics
            Object[] results =  UtilitiesDictSearch.getDisplayableMeanings(word, mLanguage);
            meanings = (List<Word.Meaning>) results[0];
            onlyEnglishMeaningsAvailable = (boolean) results[1];

            String extract = UtilitiesDictSearch.getFinalWordMeaningsExtract(onlyEnglishMeaningsAvailable, meanings, mContext, mLanguage, mLanguageFromResource);
            mWordsMeaningExtract.add(OvUtilsGeneral.fromHtml(extract));

            boolean[] types = UtilitiesDictSearch.getTypesFromWordMeanings(meanings);
            mWordsTypeIsVerb.add(types[Globals.WORD_TYPE_VERB]);
            //endregion

            //region Updating the parent Romaji and Kanji values
            String romajiAndKanji = UtilitiesDictSearch.getRomajiAndKanji(types, word, mContext, mLanguage);
            mWordsRomajiAndKanji.add(romajiAndKanji);
            //endregion

            //region Updating the parent Source Info
            List<String> sourceInfo = UtilitiesDictSearch.prepareSource(
                    mInputQuery,
                    mInputQueryTextType,
                    mInputQueryFirstLetter,
                    mInputQueryLatin,
                    mInputQueryNoSpaces,
                    romajiAndKanji,
                    word,
                    meanings,
                    types[Globals.WORD_TYPE_VERB],
                    mLanguage,
                    mContext);

            if (mShowSources) {
                sourceInfo.add((word.getIsCommon())? mContext.getString(R.string.common_word) : mContext.getString(R.string.less_common_word));
                sourceInfo.add((word.getIsLocal()) ? mContext.getString(R.string.source_local_offline) : mContext.getString(R.string.source_edict_online));
            }

            mWordsSourceInfo.add(TextUtils.join(" ", sourceInfo));
            //endregion
        }
    }

    private void setMeaningsLayout(final int position, final DictItemViewHolder holder, @NotNull String language) {

        String type;
        String fullType;
        String meaning;
        String antonym;
        String synonym;
        int startIndex;
        int endIndex = 0;
        List<Word.Meaning> meanings = new ArrayList<>();
        switch (language) {
            case Globals.LANG_STR_EN:
                meanings = mWordsList.get(position).getMeaningsEN();
                break;
            case Globals.LANG_STR_FR:
                meanings = mWordsList.get(position).getMeaningsFR();
                break;
            case Globals.LANG_STR_ES:
                meanings = mWordsList.get(position).getMeaningsES();
                break;
        }
        if (meanings==null || meanings.size()==0)  meanings = mWordsList.get(position).getMeaningsEN();
        final int numMeanings = meanings.size();

        for (int meaningIndex=0; meaningIndex<numMeanings; meaningIndex++) {

            Word.Meaning wordMeaning = meanings.get(meaningIndex);
            final int currentMeaningIndex = meaningIndex;
            meaning = wordMeaning.getMeaning();
            type = wordMeaning.getType();
            antonym = wordMeaning.getAntonym();
            synonym = wordMeaning.getSynonym();

            addMeaningsSeparator(holder.binding.listItemChildElementsContainer);

            //region Setting the type and meaning
            List<String> types = new ArrayList<>();

            for (String element : type.split(Globals.DB_ELEMENTS_DELIMITER)) {
                if (Globals.PARTS_OF_SPEECH.containsKey(element)) {
                    //String  currentType = Utilities.capitalizeFirstLetter(mContext.getString(GlobalConstants.TYPES.get(element)));
                    String currentType = OvUtilsResources.getString(Globals.PARTS_OF_SPEECH.get(element), mContext, Globals.RESOURCE_MAP_PARTS_OF_SPEECH, language);
                    if (!language.equals(Globals.LANG_STR_EN)) {
                        currentType = currentType.replace(", trans.", "").replace(", intrans.", "");
                    }
                    types.add(currentType);
                }
            }
            fullType = TextUtils.join(", ", types);
            if (fullType.equals("")) fullType = type;

            String typeAndMeaningHtml;
            if (!fullType.equals("") && !meaning.equals("*")) {
                typeAndMeaningHtml =
                        "<i><b><font color='" +
                        AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionaryPOSColor) +
                        "'>" +
                        fullType +
                        "<br>" + "</font></b></i>"  +
                        meaning;
            }
            else if (meaning.equals("*")) {
                typeAndMeaningHtml =
                        "<i><b><font color='" +
                        AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionaryPOSColor) +
                        "'>" +
                        "Proper noun" +
                        "<br>" + "</font></b></i>"  +
                        fullType;
            }
            else {
                typeAndMeaningHtml = meaning;
            }

            Spanned type_and_meaning = OvUtilsGeneral.fromHtml(typeAndMeaningHtml);
            TextView typeAndMeaningTv = new TextView(mContext);
            setMeaningsTvProperties(typeAndMeaningTv, type_and_meaning);
            holder.binding.listItemChildElementsContainer.addView(typeAndMeaningTv);
            //endregion

            //region Setting the antonym
            if (!TextUtils.isEmpty(antonym)) {
                String fullAntonym = mContext.getString(R.string.antonyms_) + " " + antonym;
                SpannableString fullAntonymSpannable = new SpannableString(fullAntonym);

                String[] antonymsList = antonym.split(",");
                for (int i = 0; i < antonymsList.length; i++) {
                    if (i == 0) {
                        startIndex = 10; // Start after "Antonyms: "
                        endIndex = startIndex + antonymsList[i].length();
                    } else {
                        startIndex = endIndex + 2;
                        endIndex = startIndex + antonymsList[i].length() - 1;
                    }
                    fullAntonymSpannable.setSpan(new KanjiClickableSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                addSubHeaderField(holder.binding.listItemChildElementsContainer, fullAntonymSpannable);
            }
            //endregion

            //regionSetting the synonym
            if (!TextUtils.isEmpty(synonym)) {
                String fullSynonym = mContext.getString(R.string.synonyms_) + " " + synonym;
                SpannableString fullSynonymSpannable = new SpannableString(fullSynonym);

                String[] synonymsList = synonym.split(",");
                for (int i = 0; i < synonymsList.length; i++) {
                    if (i == 0) {
                        startIndex = 10; // Start after "Synonyms: "
                        endIndex = startIndex + synonymsList[i].length();
                    } else {
                        startIndex = endIndex + 2;
                        endIndex = startIndex + synonymsList[i].length() - 1;
                    }
                    fullSynonymSpannable.setSpan(new KanjiClickableSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                addSubHeaderField(holder.binding.listItemChildElementsContainer, fullSynonymSpannable);
            }
            //endregion

            //regionSetting the explanations collapse/expand button
            final List<Word.Meaning.Explanation> currentExplanations = wordMeaning.getExplanations();
            if (currentExplanations == null) continue;
            final LinearLayout meaningExplanationsLL = new LinearLayout(mContext);
            meaningExplanationsLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            meaningExplanationsLL.setOrientation(LinearLayout.VERTICAL);
            meaningExplanationsLL.setClickable(false);
            meaningExplanationsLL.setFocusable(false);
            meaningExplanationsLL.setVisibility(View.GONE);

            boolean hasAtLeastOneValidExample = false;
            if (currentExplanations.get(0).getExamples() != null) {
                for (int j = 0; j < currentExplanations.get(0).getExamples().size(); j++) {
                    if (!currentExplanations.get(0).getExamples().get(j).getKanjiSentence().equals("")) {
                        hasAtLeastOneValidExample = true;
                        break;
                    }
                }
            }
            if (!currentExplanations.get(0).getExplanation().equals("")
                    || !currentExplanations.get(0).getRules().equals("")
                    || currentExplanations.get(0).getExamples() != null && currentExplanations.get(0).getExamples().size()>0 && hasAtLeastOneValidExample) {
                ImageView iv = new ImageView(mContext);
                TypedValue typedValue = new TypedValue();
                mContext.getTheme().resolveAttribute(R.attr.dictionaryDropDownArrowExplanations, typedValue, true);
                iv.setImageDrawable(mContext.getResources().getDrawable(typedValue.resourceId));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                iv.setLayoutParams(layoutParams);
                iv.setId(100*position+meaningIndex);
                iv.setClickable(true);
                iv.setFocusable(true);
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iv.setOnClickListener(view -> {
                    ImageView iv1 = (ImageView) view;

                    boolean[] explanationsVisible = (boolean[]) mVisibilitiesRegister[position][EXPLANATION_VISIBILITIES];
                    //if (explanationsVisible.length != numMeanings) return;

                    if (explanationsVisible[currentMeaningIndex]) {
                        meaningExplanationsLL.setVisibility(View.GONE);
                        mContext.getTheme().resolveAttribute(R.attr.dictionaryDropDownArrowExplanations, typedValue, true);
                        iv1.setImageDrawable(mContext.getResources().getDrawable(typedValue.resourceId));
                        explanationsVisible[currentMeaningIndex] = false;
                    }
                    else {
                        meaningExplanationsLL.setVisibility(View.VISIBLE);
                        mContext.getTheme().resolveAttribute(R.attr.dictionaryDropUpArrowExplanations, typedValue, true);
                        iv1.setImageDrawable(mContext.getResources().getDrawable(typedValue.resourceId));
                        explanationsVisible[currentMeaningIndex] = true;
                    }
                    mVisibilitiesRegister[position][EXPLANATION_VISIBILITIES] = explanationsVisible;
                });
                holder.binding.listItemChildElementsContainer.addView(iv);
                //iv.requestLayout();
            }
            //endregion

            //region Setting the explanation, rules and examples
            String explanation;
            String rules;
            List<Word.Meaning.Explanation.Example> examplesList;
            for (int i = 0; i < currentExplanations.size(); i++) {
                explanation = currentExplanations.get(i).getExplanation();
                rules = currentExplanations.get(i).getRules();

                //region Adding the explanation
                if (!explanation.equals("")) {
                    TextView explHeaderTV = addHeaderField(meaningExplanationsLL, SpannableString.valueOf(mContext.getString(R.string.explanation_)));
                    explHeaderTV.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    addSubHeaderField(meaningExplanationsLL, SpannableString.valueOf(explanation));
                }
                //endregion

                //region Adding a separator
                if (!explanation.equals("") && !rules.equals("")) {
                    addExplanationsLineSeparator(meaningExplanationsLL);
                }
                //endregion

                //region Adding the rules
                if (!rules.equals("")) {
                    TextView rulesHeaderTV = addHeaderField(meaningExplanationsLL, SpannableString.valueOf(mContext.getString(R.string.how_it_s_used_)));
                    rulesHeaderTV.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                    String[] ruleLines = rules.split("%");
                    Spanned spanned_rule;
                    List<String> typeAndMeaningHtml_list = new ArrayList<>();
                    for (String ruleLine : ruleLines) {
                        if (ruleLine.contains(RULE_DELIMITER)) {
                            String[] parsedRule = ruleLine.split(RULE_DELIMITER);
                            typeAndMeaningHtml = parsedRule[0] +
                                    "<font color='" + AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionaryRuleWhereClauseColor) + "'>" +
                                    mContext.getString(R.string._where_) +
                                    "</font>" +
                                    parsedRule[1];
                        } else {
                            typeAndMeaningHtml = ruleLine;
                        }
                        typeAndMeaningHtml_list.add(typeAndMeaningHtml);
                    }
                    spanned_rule = OvUtilsGeneral.fromHtml(TextUtils.join("<br>",typeAndMeaningHtml_list));
                    addSubHeaderField(meaningExplanationsLL, SpannableString.valueOf(spanned_rule));
                }
                //endregion

                //region Adding the examples
                examplesList = currentExplanations.get(i).getExamples();
                if (examplesList != null && examplesList.size() > 0 && hasAtLeastOneValidExample) {

                    final List<TextView> examplesTextViews = new ArrayList<>();

                    final TextView examplesShowTextView = addHeaderField(meaningExplanationsLL, SpannableString.valueOf(mContext.getString(R.string.show_examples)));
                    examplesShowTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    examplesShowTextView.setOnClickListener(view -> {
                        if (examplesTextViews.size() > 0) {
                            if (examplesTextViews.get(0).getVisibility() == View.VISIBLE) {
                                examplesShowTextView.setText(mContext.getString(R.string.show_examples));
                                for (TextView textView : examplesTextViews) {
                                    textView.setVisibility(View.GONE);
                                }
                            } else {
                                examplesShowTextView.setText(mContext.getString(R.string.HideExamples));
                                for (TextView textView : examplesTextViews) {
                                    textView.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });

                    TextView tv;
                    for (int j = 0; j < examplesList.size(); j++) {

                        if (examplesList.get(j).getKanjiSentence().equals("")) continue;

                        //Setting the English example characteristics
                        tv = addSubHeaderField(meaningExplanationsLL, SpannableString.valueOf(examplesList.get(j).getLatinSentence()));
                        tv.setPadding(EXAMPLES_LEFT_PADDING, 0, 0, 16);
                        tv.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorAccent));
                        tv.setVisibility(View.GONE);
                        examplesTextViews.add(tv);

                        //Setting the Romaji example characteristics
                        tv = addSubHeaderField(meaningExplanationsLL, SpannableString.valueOf(examplesList.get(j).getRomajiSentence()));
                        tv.setPadding(EXAMPLES_LEFT_PADDING, 0, 0, 16);
                        tv.setVisibility(View.GONE);
                        examplesTextViews.add(tv);

                        //Setting the Kanji example characteristics
                        tv = addSubHeaderField(meaningExplanationsLL, SpannableString.valueOf(examplesList.get(j).getKanjiSentence()));
                        tv.setPadding(EXAMPLES_LEFT_PADDING, 0, 0, 16);
                        tv.setVisibility(View.GONE);
                        if (j < examplesList.size() - 1)
                            tv.setPadding(DETAILS_LEFT_PADDING, 0, 0, 32);
                        examplesTextViews.add(tv);
                    }

                }
                //endregion

                //region Adding the separator line between explanations
                if (!rules.equals("") && i < currentExplanations.size() - 1) {
                    addExplanationsLineSeparator(meaningExplanationsLL);
                }
                //endregion

            }

            if (currentExplanations.size()>0) holder.binding.listItemChildElementsContainer.addView(meaningExplanationsLL);
            //endregion

        }
    }

    private void setMeaningsTvProperties(@NotNull TextView tv, Spanned spanned) {
        tv.setText(spanned);
        tv.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimary));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.DictionarySubtextSize));
        tv.setTextIsSelectable(true);
        tv.setClickable(true);
        tv.setPadding(0, 16, 0, 16);
        tv.setTypeface(mDroidSansJapaneseTypeface);
    }
    private void addMeaningsSeparator(@NotNull LinearLayout linearLayout) {
        View line = new View(mContext);
        line.setLayoutParams(mChildLineParams);
        line.setBackgroundColor(Color.WHITE);
        linearLayout.addView(line);
    }
    private void addExplanationsLineSeparator(@NotNull LinearLayout linearLayout) {
        View line = new View(mContext);
        line.setLayoutParams(mubChildLineParams);
        line.setBackgroundColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimaryLessPronounced));
        linearLayout.addView(line);
    }
    @NotNull
    private TextView addHeaderField(@NotNull LinearLayout linearLayout, Spanned type_and_meaning) {
        TextView tv = new TextView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(type_and_meaning);
        tv.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimaryMorePronounced));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.DictionarySubtextSize));
        //tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tv.setTypeface(mDroidSansJapaneseTypeface);
        tv.setTextIsSelectable(true);
        tv.setClickable(true);
        tv.setPadding(DETAILS_LEFT_PADDING, 16, 0, 16);
        linearLayout.addView(tv);
        return tv;
    }
    @NotNull
    private TextView addSubHeaderField(@NotNull LinearLayout linearLayout, SpannableString spannableString) {
        TextView tv = new TextView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(spannableString);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimaryDark));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.DictionarySubtextSize));
        tv.setTypeface(mDroidSansJapaneseTypeface);
        tv.setTextIsSelectable(true);
        tv.setClickable(true);
        tv.setPadding(DETAILS_LEFT_PADDING, 16, 0, 16);
        linearLayout.addView(tv);
        return tv;
    }

    private void createVisibilityArray() {
        if (mWordsList==null) mVisibilitiesRegister = new Object[0][2];
        else {
            mVisibilitiesRegister = new Object[mWordsList.size()][2];
            for (int i=0; i<mWordsList.size(); i++) {
                mVisibilitiesRegister[i][PARENT_VISIBILITY] = false;

                List<Word.Meaning> meanings = new ArrayList<>();
                switch (mLanguage) {
                    case Globals.LANG_STR_EN: meanings = mWordsList.get(i).getMeaningsEN(); break;
                    case Globals.LANG_STR_FR: meanings = mWordsList.get(i).getMeaningsFR(); break;
                    case Globals.LANG_STR_ES: meanings = mWordsList.get(i).getMeaningsES(); break;
                }
                if (meanings == null || meanings.size()==0) meanings = mWordsList.get(i).getMeaningsEN();
                boolean[] explanationVisibilities = new boolean[meanings.size()];
                Arrays.fill(explanationVisibilities, false);
                mVisibilitiesRegister[i][EXPLANATION_VISIBILITIES] = explanationVisibilities;
            }
        }
    }
    private void setHyperlinkInDecomposeLine(@NotNull TextView textView, @NotNull String before, String hyperlinkText, @NotNull String after) {
        String totalText = "<b>" +
                "<font color='" + AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorSecondaryNormal) + "'>" +
                before +
                "</font>" +
                hyperlinkText +
                "<font color='" + AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorSecondaryNormal) + "'>" +
                after +
                "</font>";
        Spanned spanned_totalText = OvUtilsGeneral.fromHtml(totalText);
        SpannableString spannable = new SpannableString(spanned_totalText);
        spannable.setSpan(new KanjiClickableSpan(), before.length(), spanned_totalText.length() - after.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
        textView.setTypeface(Typeface.SERIF);
        textView.setTypeface(null, Typeface.BOLD_ITALIC);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.DictionarySubtextSize));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
    private void setHyperlinksInConjugateLine(@NotNull TextView textView, @NotNull String before, String hyperlinkText, @NotNull String after) {
        String totalText = "<b>" +
                "<font color='" + AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorSecondaryNormal) + "'>" +
                before +
                "</font>" +
                hyperlinkText +
                "<font color='" + AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorSecondaryNormal) + "'>" +
                after +
                "</font>";
        Spanned spanned_totalText = OvUtilsGeneral.fromHtml(totalText);
        SpannableString spannable = new SpannableString(spanned_totalText);
        spannable.setSpan(new VerbClickableSpan(), before.length(), spanned_totalText.length() - after.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
        textView.setTypeface(Typeface.SERIF);
        textView.setTypeface(null, Typeface.BOLD_ITALIC);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.DictionarySubtextSize));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
    private class KanjiClickableSpan extends ClickableSpan {
        // code extracted from http://stackoverflow.com/questions/15475907/make-parts-of-textview-clickable-not-url
        public void onClick(@NonNull View textView) {
            // code extracted from http://stackoverflow.com/questions/19750458/android-clickablespan-get-text-onclick

            TextView text = (TextView) textView;
            Spanned s = (Spanned) text.getText();
            int start = s.getSpanStart(this);
            int end = s.getSpanEnd(this);

            String outputText = text.getText().subSequence(start, end).toString();
            mOnItemClickHandler.onDecomposeKanjiLinkClicked(outputText);

        }
        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionarySpanClickedColor));
            ds.setUnderlineText(false);
        }
    }
    private class VerbClickableSpan extends ClickableSpan {
        // code extracted from http://stackoverflow.com/questions/15475907/make-parts-of-textview-clickable-not-url
        public void onClick(@NonNull View textView) {
            // code extracted from http://stackoverflow.com/questions/19750458/android-clickablespan-get-text-onclick

            TextView text = (TextView) textView;
            Spanned s = (Spanned) text.getText();
            int start = s.getSpanStart(this);
            int end = s.getSpanEnd(this);

            String outputText = text.getText().subSequence(start, end).toString();
            mOnItemClickHandler.onVerbLinkClicked(outputText);

        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionarySpanClickedColor));
            ds.setUnderlineText(false);
        }
    }

    @Override public int getItemCount() {
        return (mWordsList == null) ? 0 : mWordsList.size();
    }
    public void setContents(List<Word> wordsList) {
        mWordsList = wordsList;
        createVisibilityArray();
        prepareLayoutTexts();
        if (mWordsList != null) {
            this.notifyDataSetChanged();
        }
    }
    public void setShowSources(boolean state) {
        mShowSources = state;
    }

    public class DictItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ListItemDictonaryBinding binding;

        DictItemViewHolder(View itemView) {
            super(itemView);

            binding = ListItemDictonaryBinding.bind(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            switchVisibilityOfChildLayout(clickedPosition);
        }

        private void switchVisibilityOfChildLayout(int clickedPosition) {
            TypedValue typedValue = new TypedValue();
            if (binding.listItemChildLinearlayout.getVisibility() == View.VISIBLE) {
                binding.listItemChildLinearlayout.setVisibility(View.GONE);
                binding.listItemMeanings.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(mWordsSourceInfo.get(getAdapterPosition()))) binding.listItemSourceInfo.setVisibility(View.VISIBLE);
                binding.listItemDictionary.setBackgroundColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictParentBackgroundColor));

                mContext.getTheme().resolveAttribute(R.attr.dictionaryDropDownArrow, typedValue, true);
                mVisibilitiesRegister[clickedPosition][PARENT_VISIBILITY] = false;
            }
            else {
                binding.listItemChildLinearlayout.setVisibility(View.VISIBLE);
                binding.listItemMeanings.setVisibility(View.GONE);
                binding.listItemSourceInfo.setVisibility(View.GONE);
                binding.listItemDictionary.setBackgroundColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictChildBackgroundColor));
                mContext.getTheme().resolveAttribute(R.attr.dictionaryDropUpArrow, typedValue, true);
                mVisibilitiesRegister[clickedPosition][PARENT_VISIBILITY] = true;
            }
            binding.dropdownArrow.setImageDrawable(mContext.getResources().getDrawable(typedValue.resourceId));
        }
    }

    public interface DictionaryItemClickHandler {
        void onDecomposeKanjiLinkClicked(String text);
        void onVerbLinkClicked(String text);
    }
}
