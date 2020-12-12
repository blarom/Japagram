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
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesCrossPlatform.UtilitiesQuery;
import com.japagram.utilitiesAndroid.UtilitiesPrefs;
import com.japagram.utilitiesCrossPlatform.UtilitiesDb;
import com.japagram.utilitiesCrossPlatform.UtilitiesGeneral;
import com.japagram.utilitiesPlatformOverridable.OverridableUtilitiesGeneral;
import com.japagram.utilitiesPlatformOverridable.OverridableUtilitiesResources;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

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
    private boolean[] mActiveMeaningLanguages;
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
    private String mUILanguage;

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
        this.mUILanguage = language;
        createVisibilityArray();

        mInputQueryTextType = UtilitiesQuery.getTextType(mInputQuery);
        mInputQueryFirstLetter = (mInputQuery.length()>0) ? mInputQuery.substring(0,1) : "";

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
            holder.childLinearLayout.setVisibility(View.VISIBLE);
            holder.meaningsTextView.setVisibility(View.GONE);
            holder.sourceInfoTextView.setVisibility(View.GONE);
            holder.dictItemContainer.setBackgroundColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictChildBackgroundColor));
        }
        else {
            holder.childLinearLayout.setVisibility(View.GONE);
            holder.meaningsTextView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mWordsSourceInfo.get(position))) holder.sourceInfoTextView.setVisibility(View.VISIBLE);
            holder.dictItemContainer.setBackgroundColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictParentBackgroundColor));
        }

        holder.romajiAndKanjiTextView.setOnClickListener(view -> {
            TypedValue typedValue = new TypedValue();
            if (holder.childLinearLayout.getVisibility() == View.VISIBLE) {
                holder.childLinearLayout.setVisibility(View.GONE);
                holder.meaningsTextView.setVisibility(View.VISIBLE);
                holder.dictItemContainer.setBackgroundColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictParentBackgroundColor));
                mContext.getTheme().resolveAttribute(R.attr.dictionaryDropDownArrow, typedValue, true);
                holder.dropdownArrowImageView.setImageDrawable(mContext.getResources().getDrawable(typedValue.resourceId));
                mVisibilitiesRegister[holder.getAdapterPosition()][PARENT_VISIBILITY] = false;
            }
            else {
                holder.childLinearLayout.setVisibility(View.VISIBLE);
                holder.meaningsTextView.setVisibility(View.GONE);
                holder.dictItemContainer.setBackgroundColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictChildBackgroundColor));
                mContext.getTheme().resolveAttribute(R.attr.dictionaryDropUpArrow, typedValue, true);
                holder.dropdownArrowImageView.setImageDrawable(mContext.getResources().getDrawable(typedValue.resourceId));
                mVisibilitiesRegister[holder.getAdapterPosition()][PARENT_VISIBILITY] = true;
            }
        });

        if (TextUtils.isEmpty(mWordsSourceInfo.get(position))) holder.sourceInfoTextView.setVisibility(View.GONE);
        else {
            holder.sourceInfoTextView.setVisibility(View.VISIBLE);
            holder.sourceInfoTextView.setText(mWordsSourceInfo.get(position));
        }
        //endregion

        //region Updating the parent values
        String romaji = mWordsList.get(position).getRomaji();
        String kanji = mWordsList.get(position).getKanji();
        String altSpellings = TextUtils.join(", ", mWordsList.get(position).getAltSpellings().split(Globals.DB_ELEMENTS_DELIMITER));
        int frequency = mWordsList.get(position).getFrequency();

        holder.romajiAndKanjiTextView.setText(mWordsRomajiAndKanji.get(position));
        holder.romajiAndKanjiTextView.setTypeface(mDroidSansJapaneseTypeface, Typeface.BOLD);
        holder.romajiAndKanjiTextView.setPadding(0,16,0,4);

        if (romaji.equals("") && kanji.equals("")) { holder.romajiAndKanjiTextView.setVisibility(View.GONE); }
        else { holder.romajiAndKanjiTextView.setVisibility(View.VISIBLE); }

        setMeaningsTvProperties(holder.meaningsTextView, mWordsMeaningExtract.get(position));
        //endregion

        //region Updating the child values

        //region Initialization
        holder.childElementsLinearLayout.removeAllViews();
        holder.childElementsLinearLayout.setFocusable(false);
        holder.conjugateHyperlinkKanjiChildTextView.setTextColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionaryChildItemKanjiColor));
        holder.romajiAndKanjiTextView.setTextColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.colorAccentDark));
        holder.sourceInfoTextView.setTextColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionaryChildItemKanjiColor));
        holder.meaningsTextView.setTextColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimary));
        holder.meaningsTextView.setTextColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimary));
        holder.childLinearLayout.setBackgroundColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictChildBackgroundColor));
        holder.conjugateHyperlinkRomajiChildTextView.setTextColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionaryChildItemRomajiColor));
        //endregion

        //region Handling the hyperlink values for user click
        if (mWordsTypeIsVerb.get(position) && mWordsList.get(position).getIsLocal()) {
            holder.conjugateHyperlinkRomajiChildTextView.setVisibility(View.VISIBLE);
            holder.conjugateHyperlinkKanjiChildTextView.setVisibility(View.VISIBLE);
        }
        else {
            holder.conjugateHyperlinkRomajiChildTextView.setVisibility(View.GONE);
            holder.conjugateHyperlinkKanjiChildTextView.setVisibility(View.GONE);
        }

        if (mWordsTypeIsVerb.get(position) && romaji.length() > 0 && kanji.length() > 0) {
            setHyperlinksInConjugateLine(holder.conjugateHyperlinkRomajiChildTextView, mContext.getString(R.string.conjugate)+" ", romaji, " ");
            setHyperlinksInConjugateLine(holder.conjugateHyperlinkKanjiChildTextView, "(", kanji, ").");
        }

        holder.decomposeHyperlinkChildTextView.setVisibility(View.VISIBLE);
        if (kanji.length() > 0) {
            setHyperlinkInDecomposeLine(holder.decomposeHyperlinkChildTextView, mContext.getString(R.string.decompose)+" ", kanji, ".");
        }
        //endregion

        //Setting the frequency
        String type = mWordsList.get(position).getMeaningsEN().get(0).getType();
        if (!type.equals("CE")) {
            String freqHtmlText = mContext.getString(R.string.frequency) + ": " + ((frequency == 0) ? "20001+ (uncommon)" : frequency);
            TextView freqTtv = addHeaderField(holder.childElementsLinearLayout, OverridableUtilitiesGeneral.fromHtml(freqHtmlText));
            freqTtv.setPadding(0, 16, 0, 16);
        }
        //endregion

        //region Setting the alternate spellings
        if (!TextUtils.isEmpty(altSpellings)) {
            //String htmlText = "<b>" + mContext.getString(R.string.alternate_forms_) + "</b> " + alternatespellings;
            String altSHtmlText = mContext.getString(R.string.alternate_forms) + ": " + altSpellings;
            TextView altSTv = addHeaderField(holder.childElementsLinearLayout, OverridableUtilitiesGeneral.fromHtml(altSHtmlText));
            altSTv.setPadding(0, 16, 0, 16);
        }
        //endregion

        //region Setting the wordMeaning elements
        switch (mUILanguage) {
            case Globals.LANG_STR_EN:
                if (mActiveMeaningLanguages[Globals.LANG_EN]) setMeaningsLayout(position, holder, Globals.LANG_EN);
                break;
            case Globals.LANG_STR_FR:
                if (mActiveMeaningLanguages[Globals.LANG_EN]) setMeaningsLayout(position, holder, Globals.LANG_FR);
                break;
            case Globals.LANG_STR_ES:
                if (mActiveMeaningLanguages[Globals.LANG_EN]) setMeaningsLayout(position, holder, Globals.LANG_ES);
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

        if (mWordsList == null) return;

        for (Word word : mWordsList) {

            //region Getting the word characteristics
            String romaji = word.getRomaji();
            String kanji = word.getKanji();
            String alternatespellings = word.getAltSpellings();
            String keywords = word.getExtraKeywordsEN();
            String matchingConj = word.getMatchingConj() == null? "" : word.getMatchingConj();
            String type = "";
            List<Word.Meaning> meanings = new ArrayList<>();

            String language = "";
            switch (mUILanguage) {
                case Globals.LANG_STR_EN:
                    language = mContext.getResources().getString(R.string.language_label_english);
                    meanings = word.getMeaningsEN();
                    keywords = word.getExtraKeywordsEN();
                    break;
                case Globals.LANG_STR_FR:
                    language = mContext.getResources().getString(R.string.language_label_french);
                    meanings = word.getMeaningsFR();
                    keywords = word.getExtraKeywordsFR();
                    break;
                case Globals.LANG_STR_ES:
                    language = mContext.getResources().getString(R.string.language_label_spanish);
                    meanings = word.getMeaningsES();
                    keywords = word.getExtraKeywordsES();
                    break;
            }

            String extract = "";
            if (meanings == null || meanings.size() == 0) {
                meanings = word.getMeaningsEN();
                extract += OverridableUtilitiesResources.getString("meanings_in", mContext, Globals.RESOURCE_MAP_GENERAL)
                        + " "
                        + language.toLowerCase()
                        + " "
                        + OverridableUtilitiesResources.getString("unavailable_select_word_to_see_meanings", mContext, Globals.RESOURCE_MAP_GENERAL);
            }
            else if (meanings.get(0).getMeaning().equals("*")) {
                type = meanings.get(0).getType();
                if (Globals.PARTS_OF_SPEECH.containsKey(type)) {
                    //String  currentType = Utilities.capitalizeFirstLetter(mContext.getString(GlobalConstants.TYPES.get(element)));
                    extract = OverridableUtilitiesResources.getString(Globals.PARTS_OF_SPEECH.get(type), mContext, Globals.RESOURCE_MAP_TYPES);
                }
                else {
                    extract = "*";
                }
            }
            else {
                extract += UtilitiesGeneral.removeDuplicatesFromCommaList(UtilitiesDb.getMeaningsExtract(meanings, Globals.BALANCE_POINT_REGULAR_DISPLAY));
            }
            mWordsMeaningExtract.add(OverridableUtilitiesGeneral.fromHtml(extract));


            StringBuilder cumulative_meaning_value = new StringBuilder();
            boolean wordHasPhraseConstruction = false;
            boolean typeIsVerbConjugation = false;
            boolean typeIsiAdjectiveConjugation = false;
            boolean typeIsnaAdjectiveConjugation = false;
            boolean typeIsVerb = false;
            boolean typeIsAdverb = false;
            boolean typeIsNoun = false;
            for (int j = 0; j< meanings.size(); j++) {
                cumulative_meaning_value.append(meanings.get(j).getMeaning());
                if (j< meanings.size()-1) { cumulative_meaning_value.append(", "); }
                if (j==0) {
                    type = meanings.get(j).getType();
                    typeIsVerbConjugation = type.equals("VC");
                    typeIsiAdjectiveConjugation = type.equals("iAC");
                    typeIsnaAdjectiveConjugation = type.equals("naAC");
                    String[] typeElements = type.split(";");
                    typeIsVerb = type.contains("V") && !type.equals("VC") && !Arrays.asList(typeElements).contains("V");
                    typeIsAdverb = type.contains("A");
                    typeIsNoun = type.contains("N");
                }
                if (!wordHasPhraseConstruction) wordHasPhraseConstruction = type.equals("PC");
            }
            mWordsTypeIsVerb.add(typeIsVerb);
            //endregion

            //region Updating the parent Romaji and Kanji values
            String parentRomaji;
            if (typeIsVerbConjugation && romaji.length()>3 && romaji.substring(0,3).equals("(o)")) {
                parentRomaji = "(o)["
                        + mContext.getString(R.string.verb)
                        + "] + "
                        + romaji.substring(3);
            }
            else if (typeIsVerbConjugation && romaji.length()>3 && !romaji.substring(0,3).equals("(o)")) {
                parentRomaji = "["
                        + mContext.getString(R.string.verb)
                        + "] + "
                        + romaji;
            }
            else if (typeIsiAdjectiveConjugation) parentRomaji = "["+mContext.getString(R.string.i_adj)+"] + " + romaji;
            else if (typeIsnaAdjectiveConjugation) parentRomaji = "["+mContext.getString(R.string.na_adj)+"] + " + romaji;
            else if (typeIsAdverb && !typeIsNoun && romaji.length()>2
                    && romaji.substring(romaji.length()-2).equals("ni")
                    && !romaji.substring(romaji.length()-3).equals(" ni")) parentRomaji = romaji.substring(0,romaji.length()-2) + " ni";
            else parentRomaji = romaji;

            String romajiAndKanji;
            if (romaji.equals("")) romajiAndKanji = kanji;
            else if (kanji.equals("")) romajiAndKanji = romaji;
            else romajiAndKanji = parentRomaji.toUpperCase() + " (" + kanji + ")";
            String inputQueryNoSpaces = mInputQuery.replace(" ","");
            String inputQueryLatin = UtilitiesQuery.getWaapuroHiraganaKatakana(mInputQuery).get(Globals.TYPE_LATIN);
            String romajiAndKanjiNoSpaces = romajiAndKanji.replace(" ","");
            mWordsRomajiAndKanji.add(romajiAndKanji);
            //endregion

            //region Updating the parent Source Info
            List<String> sourceInfo = new ArrayList<>();
            if (!romajiAndKanji.contains(mInputQuery)
                    && !romajiAndKanji.contains(inputQueryNoSpaces)
                    && !romajiAndKanjiNoSpaces.contains(mInputQuery)
                    && !romajiAndKanjiNoSpaces.contains(inputQueryNoSpaces)
                    && !romajiAndKanjiNoSpaces.contains(inputQueryLatin)) {

                String latin = UtilitiesQuery.getWaapuroHiraganaKatakana(romaji).get(Globals.TYPE_LATIN);
                String hiragana = UtilitiesQuery.getWaapuroHiraganaKatakana(romaji).get(Globals.TYPE_HIRAGANA);
                String katakana = UtilitiesQuery.getWaapuroHiraganaKatakana(romaji).get(Globals.TYPE_KATAKANA);

                if (!TextUtils.isEmpty(alternatespellings) && alternatespellings.contains(mInputQuery)) {
                    String[] altSpellingElements = alternatespellings.split(",");
                    boolean isExactMatch = false;
                    for (String element : altSpellingElements) {
                        if (mInputQuery.equals(element.trim())) {
                            isExactMatch = true;
                            break;
                        }
                    }
                    if (isExactMatch) sourceInfo.add(mContext.getString(R.string.from_alt_form)+" \"" + mInputQuery + "\".");
                    else sourceInfo.add(mContext.getString(R.string.from_alt_form_containing)+" \"" + mInputQuery + "\".");
                }
                else if (cumulative_meaning_value.toString().contains(mInputQuery) || cumulative_meaning_value.toString().contains(latin)) {
                    //Ignore words where the input query is included in the meaning
                }
                else if (keywords != null && (keywords.contains(mInputQuery) || keywords.contains(inputQueryLatin))) {
                    String[] keywordList = keywords.split(",");
                    for (String element : keywordList) {
                        String keyword = element.trim();
                        if (!romaji.contains(keyword) && !kanji.contains(keyword) && !alternatespellings.contains(keyword)
                                && !cumulative_meaning_value.toString().contains(keyword)) {
                            sourceInfo.add(mContext.getString(R.string.from_associated_word)+" \"" + keyword + "\".");
                            break;
                        }
                    }
                }
                else if (!TextUtils.isEmpty(matchingConj)
                        && word.getVerbConjMatchStatus() == Word.CONJ_MATCH_EXACT
                            || word.getVerbConjMatchStatus() == Word.CONJ_MATCH_CONTAINED
                        && matchingConj.contains(mInputQuery)
                            || matchingConj.contains(inputQueryNoSpaces)
                            || matchingConj.contains(inputQueryLatin)) {
                    sourceInfo.add((typeIsVerb)? mContext.getString(R.string.from_conjugated_form)+" \"" + matchingConj + "\"." : mContext.getString(R.string.from_associated_word)+" \"" + matchingConj + "\".");
                }
                else if ((mInputQueryTextType == Globals.TYPE_KANJI
                        && kanji.length() > 0 && !kanji.substring(0, 1).equals(mInputQueryFirstLetter))
                        || (mInputQueryTextType == Globals.TYPE_LATIN
                        && romaji.length() > 0 && !romaji.substring(0, 1).equals(mInputQueryFirstLetter))
                        || (mInputQueryTextType == Globals.TYPE_HIRAGANA
                        && hiragana.length() > 0 && !hiragana.substring(0, 1).equals(mInputQueryFirstLetter))
                        || (mInputQueryTextType == Globals.TYPE_KATAKANA
                        && katakana.length() > 0 && !katakana.substring(0, 1).equals(mInputQueryFirstLetter))
                ) {
                    sourceInfo.add(mContext.getString(R.string.derived_from) + " \"" + mInputQuery + "\".");
                }
            }

            if (mShowSources) {
                sourceInfo.add((word.getIsCommon())? mContext.getString(R.string.common_word) : mContext.getString(R.string.less_common_word));
                sourceInfo.add((word.getIsLocal()) ? mContext.getString(R.string.source_local_offline) : mContext.getString(R.string.source_edict_online));
            }

            mWordsSourceInfo.add(TextUtils.join(" ", sourceInfo));
            //endregion

        }
    }
    private void setMeaningsLayout(final int position, final DictItemViewHolder holder, int language) {

        String type;
        String fullType;
        String meaning;
        String antonym;
        String synonym;
        int startIndex;
        int endIndex = 0;
        List<Word.Meaning> meanings = new ArrayList<>();
        switch (language) {
            case Globals.LANG_EN:
                meanings = mWordsList.get(position).getMeaningsEN();
                break;
            case Globals.LANG_FR:
                meanings = mWordsList.get(position).getMeaningsFR();
                break;
            case Globals.LANG_ES:
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

            addMeaningsSeparator(holder.childElementsLinearLayout);

            //region Setting the type and meaning
            List<String> types = new ArrayList<>();

            for (String element : type.split(Globals.DB_ELEMENTS_DELIMITER)) {
                if (Globals.PARTS_OF_SPEECH.containsKey(element)) {
                    //String  currentType = Utilities.capitalizeFirstLetter(mContext.getString(GlobalConstants.TYPES.get(element)));
                    String currentType = OverridableUtilitiesResources.getString(Globals.PARTS_OF_SPEECH.get(element), mContext, Globals.RESOURCE_MAP_TYPES);
                    if (language != Globals.LANG_EN) {
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
                        UtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionaryPOSColor) +
                        "'>" +
                        fullType +
                        "<br>" + "</font></b></i>"  +
                        meaning;
            }
            else if (meaning.equals("*")) {
                typeAndMeaningHtml =
                        "<i><b><font color='" +
                        UtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionaryPOSColor) +
                        "'>" +
                        "Proper noun" +
                        "<br>" + "</font></b></i>"  +
                        fullType;
            }
            else {
                typeAndMeaningHtml = meaning;
            }

            Spanned type_and_meaning = OverridableUtilitiesGeneral.fromHtml(typeAndMeaningHtml);
            TextView typeAndMeaningTv = new TextView(mContext);
            setMeaningsTvProperties(typeAndMeaningTv, type_and_meaning);
            holder.childElementsLinearLayout.addView(typeAndMeaningTv);
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

                addSubHeaderField(holder.childElementsLinearLayout, fullAntonymSpannable);
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

                addSubHeaderField(holder.childElementsLinearLayout, fullSynonymSpannable);
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
                holder.childElementsLinearLayout.addView(iv);
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
                                    "<font color='" + UtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionaryRuleWhereClauseColor) + "'>" +
                                    mContext.getString(R.string._where_) +
                                    "</font>" +
                                    parsedRule[1];
                        } else {
                            typeAndMeaningHtml = ruleLine;
                        }
                        typeAndMeaningHtml_list.add(typeAndMeaningHtml);
                    }
                    spanned_rule = OverridableUtilitiesGeneral.fromHtml(TextUtils.join("<br>",typeAndMeaningHtml_list));
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
                        tv.setTextColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.colorAccent));
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

            if (currentExplanations.size()>0) holder.childElementsLinearLayout.addView(meaningExplanationsLL);
            //endregion

        }
    }

    private void setMeaningsTvProperties(@NotNull TextView tv, Spanned spanned) {
        tv.setText(spanned);
        tv.setTextColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimary));
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
        line.setBackgroundColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimaryLessPronounced));
        linearLayout.addView(line);
    }
    @NotNull
    private TextView addHeaderField(@NotNull LinearLayout linearLayout, Spanned type_and_meaning) {
        TextView tv = new TextView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(type_and_meaning);
        tv.setTextColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimaryMorePronounced));
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
        tv.setTextColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimaryDark));
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
                switch (mUILanguage) {
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
                "<font color='" + UtilitiesPrefs.getResColorValue(mContext, R.attr.colorSecondaryNormal) + "'>" +
                before +
                "</font>" +
                hyperlinkText +
                "<font color='" + UtilitiesPrefs.getResColorValue(mContext, R.attr.colorSecondaryNormal) + "'>" +
                after +
                "</font>";
        Spanned spanned_totalText = OverridableUtilitiesGeneral.fromHtml(totalText);
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
                "<font color='" + UtilitiesPrefs.getResColorValue(mContext, R.attr.colorSecondaryNormal) + "'>" +
                before +
                "</font>" +
                hyperlinkText +
                "<font color='" + UtilitiesPrefs.getResColorValue(mContext, R.attr.colorSecondaryNormal) + "'>" +
                after +
                "</font>";
        Spanned spanned_totalText = OverridableUtilitiesGeneral.fromHtml(totalText);
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
            ds.setColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionarySpanClickedColor));
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
            ds.setColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.textDictionarySpanClickedColor));
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

        @BindView(R.id.list_item_dictionary) ConstraintLayout dictItemContainer;
        @BindView(R.id.list_item_romaji_and_kanji) TextView romajiAndKanjiTextView;
        @BindView(R.id.list_item_source_info) TextView sourceInfoTextView;
        @BindView(R.id.dropdown_arrow) ImageView dropdownArrowImageView;
        @BindView(R.id.list_item_meanings) TextView meaningsTextView;
        @BindView(R.id.list_item_child_linearlayout) LinearLayout childLinearLayout;
        @BindView(R.id.list_item_conjugate_hyperlink_romaji) TextView conjugateHyperlinkRomajiChildTextView;
        @BindView(R.id.list_item_conjugate_hyperlink_kanji) TextView conjugateHyperlinkKanjiChildTextView;
        @BindView(R.id.list_item_decompose_hyperlink) TextView decomposeHyperlinkChildTextView;
        @BindView(R.id.list_item_child_elements_container) LinearLayout childElementsLinearLayout;

        DictItemViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            switchVisibilityOfChildLayout(clickedPosition);
        }

        private void switchVisibilityOfChildLayout(int clickedPosition) {
            TypedValue typedValue = new TypedValue();
            if (childLinearLayout.getVisibility() == View.VISIBLE) {
                childLinearLayout.setVisibility(View.GONE);
                meaningsTextView.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(mWordsSourceInfo.get(getAdapterPosition()))) sourceInfoTextView.setVisibility(View.VISIBLE);
                dictItemContainer.setBackgroundColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictParentBackgroundColor));

                mContext.getTheme().resolveAttribute(R.attr.dictionaryDropDownArrow, typedValue, true);
                mVisibilitiesRegister[clickedPosition][PARENT_VISIBILITY] = false;
            }
            else {
                childLinearLayout.setVisibility(View.VISIBLE);
                meaningsTextView.setVisibility(View.GONE);
                sourceInfoTextView.setVisibility(View.GONE);
                dictItemContainer.setBackgroundColor(UtilitiesPrefs.getResColorValue(mContext, R.attr.selectedDictChildBackgroundColor));
                mContext.getTheme().resolveAttribute(R.attr.dictionaryDropUpArrow, typedValue, true);
                mVisibilitiesRegister[clickedPosition][PARENT_VISIBILITY] = true;
            }
            dropdownArrowImageView.setImageDrawable(mContext.getResources().getDrawable(typedValue.resourceId));
        }
    }

    public interface DictionaryItemClickHandler {
        void onDecomposeKanjiLinkClicked(String text);
        void onVerbLinkClicked(String text);
    }
}
