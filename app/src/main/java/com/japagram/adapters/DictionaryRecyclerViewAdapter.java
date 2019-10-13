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
import com.japagram.data.Word;
import com.japagram.resources.GlobalConstants;
import com.japagram.resources.Utilities;
import com.japagram.ui.ConvertFragment;

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
                                         String inputQuery,
                                         String language,
                                         Typeface typeface) {
        this.mContext = context;
        this.mWordsList = wordsList;
        this.mOnItemClickHandler = listener;
        this.mInputQuery = inputQuery;
        this.mUILanguage = language;
        createVisibilityArray();

        mInputQueryTextType = ConvertFragment.getTextType(mInputQuery);
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
            holder.parentContainer.setBackgroundColor(Utilities.getResColorValue(mContext, R.attr.colorSelectedDictResultBackgroundColor));
        }
        else {
            holder.childLinearLayout.setVisibility(View.GONE);
            holder.meaningsTextView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mWordsSourceInfo.get(position))) holder.sourceInfoTextView.setVisibility(View.VISIBLE);
            holder.parentContainer.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.romajiAndKanjiTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.childLinearLayout.getVisibility() == View.VISIBLE) {
                    holder.childLinearLayout.setVisibility(View.GONE);
                    holder.meaningsTextView.setVisibility(View.VISIBLE);
                    holder.parentContainer.setBackgroundColor(Color.TRANSPARENT);
                    holder.dropdownArrowImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_arrow_drop_down_black_24dp));
                    mVisibilitiesRegister[holder.getAdapterPosition()][PARENT_VISIBILITY] = false;
                }
                else {
                    holder.childLinearLayout.setVisibility(View.VISIBLE);
                    holder.meaningsTextView.setVisibility(View.GONE);
                    holder.parentContainer.setBackgroundColor(Utilities.getResColorValue(mContext, R.attr.colorSelectedDictResultBackgroundColor));
                    holder.dropdownArrowImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_arrow_drop_up_black_24dp));
                    mVisibilitiesRegister[holder.getAdapterPosition()][PARENT_VISIBILITY] = true;
                }
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
        String alternatespellings = mWordsList.get(position).getAltSpellings();

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
        holder.kanjiChildTextView.setTextColor(Utilities.getResColorValue(mContext, R.attr.textDictionaryChildItemKanjiColor));
        holder.romajiAndKanjiTextView.setTextColor(Utilities.getResColorValue(mContext, R.attr.colorAccentDark));
        holder.sourceInfoTextView.setTextColor(Utilities.getResColorValue(mContext, R.attr.textDictionaryChildItemKanjiColor));
        holder.meaningsTextView.setTextColor(Utilities.getResColorValue(mContext, R.attr.colorPrimary));
        holder.meaningsTextView.setTextColor(Utilities.getResColorValue(mContext, R.attr.colorPrimary));
        holder.childLinearLayout.setBackgroundColor(Utilities.getResColorValue(mContext, R.attr.colorSelectedDictResultBackgroundColor));
        holder.romajiChildTextView.setTextColor(Utilities.getResColorValue(mContext, R.attr.textDictionaryChildItemRomajiColor));
        //endregion

        //region Showing the romaji and kanji values for user click
        if (mWordsTypeIsVerb.get(position) && mWordsList.get(position).getIsLocal()) {
            holder.romajiChildTextView.setVisibility(View.VISIBLE);
            holder.kanjiChildTextView.setVisibility(View.VISIBLE);
        }
        else {
            holder.romajiChildTextView.setVisibility(View.GONE);
            holder.kanjiChildTextView.setVisibility(View.GONE);
        }

        if (romaji.length() > 0 && kanji.length() > 0) {
            if (mWordsTypeIsVerb.get(position)) {
                setHyperlinksInCopyToInputLine("verb", holder.romajiChildTextView, mContext.getString(R.string.conjugate)+" ", romaji, " ");
                setHyperlinksInCopyToInputLine("verb", holder.kanjiChildTextView, "(", kanji, ").");
            } else {
                setHyperlinksInCopyToInputLine("word", holder.romajiChildTextView, mContext.getString(R.string.copy)+" ", romaji, " ");
                setHyperlinksInCopyToInputLine("word", holder.kanjiChildTextView, "(", kanji, ") "+ mContext.getString(R.string.to_search_field)+".");
            }
        } else if (romaji.length() == 0) {
            setHyperlinksInCopyToInputLine("word", holder.romajiChildTextView, mContext.getString(R.string.copy)+" ", kanji, " "+ mContext.getString(R.string.to_search_field)+".");
        } else {
            setHyperlinksInCopyToInputLine("word", holder.romajiChildTextView, mContext.getString(R.string.copy)+" ", romaji, " "+ mContext.getString(R.string.to_search_field)+".");
        }
        //endregion

        //region Setting the alternate spellings
        if (!TextUtils.isEmpty(alternatespellings)) {
            //String htmlText = "<b>" + mContext.getString(R.string.alternate_forms_) + "</b> " + alternatespellings;
            String htmlText = mContext.getString(R.string.alternate_forms_) + " " + alternatespellings;
            TextView tv = addHeaderField(holder.childElementsLinearLayout, Utilities.fromHtml(htmlText));
            tv.setPadding(0, 16, 0, 16);
        }
        //endregion

        //region Setting the wordMeaning elements
        switch (mUILanguage) {
            case GlobalConstants.LANG_STR_EN:
                if (mActiveMeaningLanguages[GlobalConstants.LANG_EN]) setMeaningsLayout(position, holder, GlobalConstants.LANG_EN);
                break;
            case GlobalConstants.LANG_STR_FR:
                if (mActiveMeaningLanguages[GlobalConstants.LANG_EN]) setMeaningsLayout(position, holder, GlobalConstants.LANG_FR);
                break;
            case GlobalConstants.LANG_STR_ES:
                if (mActiveMeaningLanguages[GlobalConstants.LANG_EN]) setMeaningsLayout(position, holder, GlobalConstants.LANG_ES);
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
                case GlobalConstants.LANG_STR_EN:
                    language = mContext.getResources().getString(R.string.language_label_english);
                    meanings = word.getMeaningsEN();
                    keywords = word.getExtraKeywordsEN();
                    break;
                case GlobalConstants.LANG_STR_FR:
                    language = mContext.getResources().getString(R.string.language_label_french);
                    meanings = word.getMeaningsFR();
                    keywords = word.getExtraKeywordsFR();
                    break;
                case GlobalConstants.LANG_STR_ES:
                    language = mContext.getResources().getString(R.string.language_label_spanish);
                    meanings = word.getMeaningsES();
                    keywords = word.getExtraKeywordsES();
                    break;
            }

            String extract = "";
            if (meanings == null || meanings.size() == 0) {
                meanings = word.getMeaningsEN();
                extract += mContext.getString(R.string.meanings_in)
                        + " "
                        + language.toLowerCase()
                        + " "
                        + mContext.getString(R.string.unavailable_select_word_to_see_meanings);
            }
            else if (meanings.get(0).getMeaning().equals("*")) {
                type = meanings.get(0).getType();
                if (GlobalConstants.TYPES.containsKey(type)) {
                    //String  currentType = Utilities.capitalizeFirstLetter(mContext.getString(GlobalConstants.TYPES.get(element)));
                    extract = mContext.getString(GlobalConstants.TYPES.get(type));
                }
                else {
                    extract = "*";
                }
            }
            else {
                extract += Utilities.removeDuplicatesFromCommaList(Utilities.getMeaningsExtract(meanings, GlobalConstants.BALANCE_POINT_REGULAR_DISPLAY));
            }
            mWordsMeaningExtract.add(Utilities.fromHtml(extract));


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
            String inputQueryLatin = ConvertFragment.getLatinHiraganaKatakana(mInputQuery).get(GlobalConstants.TYPE_LATIN);
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

                String latin = ConvertFragment.getLatinHiraganaKatakana(romaji).get(GlobalConstants.TYPE_LATIN);
                String hiragana = ConvertFragment.getLatinHiraganaKatakana(romaji).get(GlobalConstants.TYPE_HIRAGANA);
                String katakana = ConvertFragment.getLatinHiraganaKatakana(romaji).get(GlobalConstants.TYPE_KATAKANA);

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
                else if ((mInputQueryTextType == GlobalConstants.TYPE_KANJI
                        && kanji.length() > 0 && !kanji.substring(0, 1).equals(mInputQueryFirstLetter))
                        || (mInputQueryTextType == GlobalConstants.TYPE_LATIN
                        && romaji.length() > 0 && !romaji.substring(0, 1).equals(mInputQueryFirstLetter))
                        || (mInputQueryTextType == GlobalConstants.TYPE_HIRAGANA
                        && hiragana.length() > 0 && !hiragana.substring(0, 1).equals(mInputQueryFirstLetter))
                        || (mInputQueryTextType == GlobalConstants.TYPE_KATAKANA
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
            case GlobalConstants.LANG_EN:
                meanings = mWordsList.get(position).getMeaningsEN();
                break;
            case GlobalConstants.LANG_FR:
                meanings = mWordsList.get(position).getMeaningsFR();
                break;
            case GlobalConstants.LANG_ES:
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

            for (String element : type.split(GlobalConstants.DB_ELEMENTS_DELIMITER)) {
                if (GlobalConstants.TYPES.containsKey(element)) {
                    //String  currentType = Utilities.capitalizeFirstLetter(mContext.getString(GlobalConstants.TYPES.get(element)));
                    String currentType = mContext.getString(GlobalConstants.TYPES.get(element));
                    if (language != GlobalConstants.LANG_EN) {
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
                        Utilities.getResColorValue(mContext, R.attr.textDictionaryPOSColor) +
                        "'>" +
                        fullType +
                        "<br>" + "</font></b></i>"  +
                        meaning;
            }
            else if (meaning.equals("*")) {
                typeAndMeaningHtml =
                        "<i><b><font color='" +
                        Utilities.getResColorValue(mContext, R.attr.textDictionaryPOSColor) +
                        "'>" +
                        "Proper noun" +
                        "<br>" + "</font></b></i>"  +
                        fullType;
            }
            else {
                typeAndMeaningHtml = meaning;
            }

            Spanned type_and_meaning = Utilities.fromHtml(typeAndMeaningHtml);
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
                    fullAntonymSpannable.setSpan(new WordClickableSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                    fullSynonymSpannable.setSpan(new WordClickableSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_arrow_drop_down_explanations_24dp));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                iv.setLayoutParams(layoutParams);
                iv.setId(100*position+meaningIndex);
                iv.setClickable(true);
                iv.setFocusable(true);
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ImageView iv = (ImageView) view;

                        boolean[] explanationsVisible = (boolean[]) mVisibilitiesRegister[position][EXPLANATION_VISIBILITIES];
                        //if (explanationsVisible.length != numMeanings) return;

                        if (explanationsVisible[currentMeaningIndex]) {
                            meaningExplanationsLL.setVisibility(View.GONE);
                            iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_arrow_drop_down_explanations_24dp));
                            explanationsVisible[currentMeaningIndex] = false;
                        }
                        else {
                            meaningExplanationsLL.setVisibility(View.VISIBLE);
                            iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_arrow_drop_up_explanations_24dp));
                            explanationsVisible[currentMeaningIndex] = true;
                        }
                        mVisibilitiesRegister[position][EXPLANATION_VISIBILITIES] = explanationsVisible;
                    }
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

                    String[] parsedRule = rules.split(RULE_DELIMITER);
                    Spanned spanned_rule;
                    if (parsedRule.length == 1) { // If the rule doesn't have a "where" clause
                        typeAndMeaningHtml = rules;
                        spanned_rule = Utilities.fromHtml(typeAndMeaningHtml);
                    } else {
                        typeAndMeaningHtml = parsedRule[0] +
                                "<font color='" + Utilities.getResColorValue(mContext, R.attr.textDictionaryRuleWhereClauseColor) + "'>" +
                                mContext.getString(R.string._where_) +
                                "</font>" +
                                parsedRule[1];
                        spanned_rule = Utilities.fromHtml(typeAndMeaningHtml);
                    }
                    addSubHeaderField(meaningExplanationsLL, SpannableString.valueOf(spanned_rule));
                }
                //endregion

                //region Adding the examples
                examplesList = currentExplanations.get(i).getExamples();
                if (examplesList != null && examplesList.size() > 0 && hasAtLeastOneValidExample) {

                    final List<TextView> examplesTextViews = new ArrayList<>();

                    final TextView examplesShowTextView = addHeaderField(meaningExplanationsLL, SpannableString.valueOf(mContext.getString(R.string.show_examples)));
                    examplesShowTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    examplesShowTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
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
                        }
                    });

                    TextView tv;
                    for (int j = 0; j < examplesList.size(); j++) {

                        if (examplesList.get(j).getKanjiSentence().equals("")) continue;

                        //Setting the English example characteristics
                        tv = addSubHeaderField(meaningExplanationsLL, SpannableString.valueOf(examplesList.get(j).getLatinSentence()));
                        tv.setPadding(EXAMPLES_LEFT_PADDING, 0, 0, 16);
                        tv.setTextColor(Utilities.getResColorValue(mContext, R.attr.colorAccent));
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

    private void setMeaningsTvProperties(TextView tv, Spanned spanned) {
        tv.setText(spanned);
        tv.setTextColor(Utilities.getResColorValue(mContext, R.attr.colorPrimary));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.DictionarySubtextSize));
        tv.setTextIsSelectable(true);
        tv.setClickable(true);
        tv.setPadding(0, 16, 0, 16);
        tv.setTypeface(mDroidSansJapaneseTypeface);
    }
    private void addMeaningsSeparator(LinearLayout linearLayout) {
        View line = new View(mContext);
        line.setLayoutParams(mChildLineParams);
        line.setBackgroundColor(Color.WHITE);
        linearLayout.addView(line);
    }
    private void addExplanationsLineSeparator(LinearLayout linearLayout) {
        View line = new View(mContext);
        line.setLayoutParams(mubChildLineParams);
        line.setBackgroundColor(Utilities.getResColorValue(mContext, R.attr.colorPrimaryLight));
        linearLayout.addView(line);
    }
    private TextView addHeaderField(LinearLayout linearLayout, Spanned type_and_meaning) {
        TextView tv = new TextView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(type_and_meaning);
        tv.setTextColor(Utilities.getResColorValue(mContext, R.attr.colorPrimaryDark));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.DictionarySubtextSize));
        //tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tv.setTypeface(mDroidSansJapaneseTypeface);
        tv.setTextIsSelectable(true);
        tv.setClickable(true);
        tv.setPadding(DETAILS_LEFT_PADDING, 16, 0, 16);
        linearLayout.addView(tv);
        return tv;
    }
    private TextView addSubHeaderField(LinearLayout linearLayout, SpannableString spannableString) {
        TextView tv = new TextView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(spannableString);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setTextColor(Utilities.getResColorValue(mContext, R.attr.colorPrimaryDark));
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
                    case GlobalConstants.LANG_STR_EN: meanings = mWordsList.get(i).getMeaningsEN(); break;
                    case GlobalConstants.LANG_STR_FR: meanings = mWordsList.get(i).getMeaningsFR(); break;
                    case GlobalConstants.LANG_STR_ES: meanings = mWordsList.get(i).getMeaningsES(); break;
                }
                if (meanings == null || meanings.size()==0) meanings = mWordsList.get(i).getMeaningsEN();
                boolean[] explanationVisibilities = new boolean[meanings.size()];
                Arrays.fill(explanationVisibilities, false);
                mVisibilitiesRegister[i][EXPLANATION_VISIBILITIES] = explanationVisibilities;
            }
        }
    }
    private void setHyperlinksInCopyToInputLine(String type, TextView textView, String before, String hyperlinkText, String after) {
        String totalText = "<b>" +
                "<font color='" + Utilities.getResColorValue(mContext, R.attr.appTextSecondaryColor) + "'>" +
                before +
                "</font>" +
                hyperlinkText +
                "<font color='" + Utilities.getResColorValue(mContext, R.attr.appTextSecondaryColor) + "'>" +
                after +
                "</font>";
        Spanned spanned_totalText = Utilities.fromHtml(totalText);
        SpannableString WordSpannable = new SpannableString(spanned_totalText);
        if (type.equals("word")) {
            WordSpannable.setSpan(new WordClickableSpan(), before.length(), spanned_totalText.length() - after.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            WordSpannable.setSpan(new VerbClickableSpan(), before.length(), spanned_totalText.length() - after.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(WordSpannable);
        textView.setTypeface(Typeface.SERIF);
        textView.setTypeface(null, Typeface.BOLD_ITALIC);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.DictionarySubtextSize));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
    private class WordClickableSpan extends ClickableSpan {
        // code extracted from http://stackoverflow.com/questions/15475907/make-parts-of-textview-clickable-not-url
        public void onClick(@NonNull View textView) {
            // code extracted from http://stackoverflow.com/questions/19750458/android-clickablespan-get-text-onclick

            TextView text = (TextView) textView;
            Spanned s = (Spanned) text.getText();
            int start = s.getSpanStart(this);
            int end = s.getSpanEnd(this);

            String outputText = text.getText().subSequence(start, end).toString();
            mOnItemClickHandler.onWordLinkClicked(outputText);

        }
        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setColor(Utilities.getResColorValue(mContext, R.attr.textDictionarySpanClickedColor));
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
            ds.setColor(Utilities.getResColorValue(mContext, R.attr.textDictionarySpanClickedColor));
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

        @BindView(R.id.list_item_dictionary) ConstraintLayout parentContainer;
        @BindView(R.id.list_item_romaji_and_kanji) TextView romajiAndKanjiTextView;
        @BindView(R.id.list_item_source_info) TextView sourceInfoTextView;
        @BindView(R.id.dropdown_arrow) ImageView dropdownArrowImageView;
        @BindView(R.id.list_item_meanings) TextView meaningsTextView;
        @BindView(R.id.list_item_child_linearlayout) LinearLayout childLinearLayout;
        @BindView(R.id.list_item_child_romaji) TextView romajiChildTextView;
        @BindView(R.id.list_item_child_kanji) TextView kanjiChildTextView;
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
            if (childLinearLayout.getVisibility() == View.VISIBLE) {
                childLinearLayout.setVisibility(View.GONE);
                meaningsTextView.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(mWordsSourceInfo.get(getAdapterPosition()))) sourceInfoTextView.setVisibility(View.VISIBLE);
                parentContainer.setBackgroundColor(Color.TRANSPARENT);
                dropdownArrowImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_arrow_drop_down_black_24dp));
                mVisibilitiesRegister[clickedPosition][PARENT_VISIBILITY] = false;
            }
            else {
                childLinearLayout.setVisibility(View.VISIBLE);
                meaningsTextView.setVisibility(View.GONE);
                sourceInfoTextView.setVisibility(View.GONE);
                parentContainer.setBackgroundColor(Utilities.getResColorValue(mContext, R.attr.colorSelectedDictResultBackgroundColor));
                dropdownArrowImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_arrow_drop_up_black_24dp));
                mVisibilitiesRegister[clickedPosition][PARENT_VISIBILITY] = true;
            }
        }
    }

    public interface DictionaryItemClickHandler {
        void onWordLinkClicked(String text);
        void onVerbLinkClicked(String text);
    }
}
