package com.japagram.adapters;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.japagram.R;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class KanjiGridRecyclerViewAdapter extends RecyclerView.Adapter<KanjiGridRecyclerViewAdapter.ComponentViewHolder> {

    private final Context mContext;
    private final Typeface mDroidSansJapaneseTypeface;
    private List<String> mKanjis;
    final private ComponentClickHandler mOnItemClickHandler;
    private boolean[] mKanjiIsSelected;
    private final boolean isResultsGrid;

    public KanjiGridRecyclerViewAdapter(Context context, ComponentClickHandler listener , List<String> components, boolean isResultsGrid, Typeface typeface) {
        this.mContext = context;
        this.mKanjis = components;
        this.mOnItemClickHandler = listener;
        this.isResultsGrid = isResultsGrid;
        createSelectedArray();

        //Setting the Typeface
        AssetManager am = mContext.getApplicationContext().getAssets();
        mDroidSansJapaneseTypeface = typeface;
    }

    @NonNull @Override public ComponentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_kanji_grid, parent, false);
        view.setFocusable(true);
        return new ComponentViewHolder(view);
    }
    @Override public void onBindViewHolder(@NonNull final ComponentViewHolder holder, int position) {

        String kanji = mKanjis.get(position);
        final TextView tv = holder.kanjiTextView;

        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.searchByRadical_kanjiGrid_itemBackground, typedValue, true);
        if (mKanjiIsSelected[position]) tv.setBackgroundResource(typedValue.resourceId);
        else tv.setBackgroundResource(0);

        tv.setText(kanji);
        tv.setTextLocale(Locale.JAPAN);
        tv.setTypeface(mDroidSansJapaneseTypeface);

        if (kanji.contains("0") || kanji.contains("1") || kanji.contains("2") || kanji.contains("3")
                || kanji.contains("4") || kanji.contains("5") || kanji.contains("6")
                || kanji.contains("7") || kanji.contains("8") || kanji.contains("9")) {
            tv.setTextSize(26);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorSecondaryNormal));
        }
        else if (kanji.contains("variant")) {
            tv.setTextSize(28);
            tv.setText(kanji.substring(0,1));
            tv.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimaryLessPronounced));
        }
        else {
            tv.setTextSize(isResultsGrid? 32 : 28);
            tv.setText(kanji);
            tv.setTypeface(null, Typeface.NORMAL);
            tv.setTextColor(AndroidUtilitiesPrefs.getResColorValue(mContext, R.attr.colorPrimaryMorePronounced));
        }

        tv.setOnClickListener(v -> {
            int position1 = holder.getAdapterPosition();
            setItemAsSelected(position1, tv);

            if (isResultsGrid) mOnItemClickHandler.onSearchResultClicked(position1);
            else mOnItemClickHandler.onComponentClicked(position1);
        });

    }

    private void setItemAsSelected(int position, TextView tv) {

        notifyDataSetChanged();

        if (mKanjiIsSelected[position]) {
            tv.setBackgroundResource(0);
            mKanjiIsSelected[position] = false;
        }
        else {
            createSelectedArray();
            tv.setBackgroundResource(R.drawable.background_kanji_grid_item_daybluegreen);
            mKanjiIsSelected[position] = true;
        }
    }
    private void createSelectedArray() {
        mKanjiIsSelected = new boolean[mKanjis ==null? 0 : mKanjis.size()];
        Arrays.fill(mKanjiIsSelected, false);
    }

    @Override public int getItemCount() {
        return (mKanjis == null) ? 0 : mKanjis.size();
    }
    public void setContents(List<String> components) {
        this.mKanjis = components;
        createSelectedArray();
        if (components != null) {
            this.notifyDataSetChanged();
        }
    }

    public class ComponentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_item_kanji_textview) TextView kanjiTextView;
        @BindView(R.id.list_item_kanji_container) LinearLayout container;

        ComponentViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            if (isResultsGrid) mOnItemClickHandler.onSearchResultClicked(clickedPosition);
            else mOnItemClickHandler.onComponentClicked(clickedPosition);
        }
    }

    public interface ComponentClickHandler {
        void onComponentClicked(int clickedPosition);
        void onSearchResultClicked(int clickedPosition);
    }
}
