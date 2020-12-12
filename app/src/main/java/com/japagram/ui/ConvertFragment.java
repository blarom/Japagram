package com.japagram.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.japagram.R;
import com.japagram.data.InputQuery;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ConvertFragment extends Fragment {


    private InputQuery mInputQuery;

    // Fragment Lifecycle Functions
    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtras();
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Retain this fragment (used to save user inputs on activity creation/destruction)
        setRetainInstance(true);

        // Define that this fragment is related to fragment_conjugator.xml
        return inflater.inflate(R.layout.fragment_convert, container, false);
    }
    @Override public void onStart() {
    super.onStart();

    getConversion(mInputQuery);
}


    // Fragment Modules
    private void getExtras() {
        if (getArguments()!=null) {
            mInputQuery = new InputQuery(getArguments().getString(getString(R.string.user_query_word)));
        }
    }
    private void getConversion(final InputQuery inputQuery) {

        // Gets the output of the InputQueryFragment and makes it available to the current fragment

        if (getActivity() == null) return;

        TextView Conversion = getActivity().findViewById(R.id.transliteration);
        TextView ConversionLatin = getActivity().findViewById(R.id.conversion_waapuro);
        TextView ConversionHiragana = getActivity().findViewById(R.id.conversion_hiragana);
        TextView ConversionKatakana = getActivity().findViewById(R.id.conversion_katakana);
        TextView ResultHiragana = getActivity().findViewById(R.id.Result_hiragana);
        TextView ResultKatakana = getActivity().findViewById(R.id.Result_katakana);
        TextView transliterationWaapuro = getActivity().findViewById(R.id.Result_waapuro);
        TextView transliterationModHepburn = getActivity().findViewById(R.id.Result_mod_hepburn);
        TextView transliterationNihonShiki = getActivity().findViewById(R.id.Result_nihon_shiki);
        TextView transliterationKunreiShiki = getActivity().findViewById(R.id.Result_kunrei_shiki);

        if (inputQuery.isEmpty()) {
            Conversion.setText(getResources().getString(R.string.EnterWord));
            ConversionLatin.setText("");
            ConversionHiragana.setText("");
            ConversionKatakana.setText("");
            ResultHiragana.setText("");
            ResultKatakana.setText("");
            transliterationWaapuro.setText("");
            transliterationModHepburn.setText("");
            transliterationNihonShiki.setText("");
            transliterationKunreiShiki.setText("");
        }
        else {
            Conversion.setText(getResources().getString(R.string.transliteration));
            ConversionLatin.setText(getResources().getString(R.string.conversion_waapuro));
            ConversionHiragana.setText(getResources().getString(R.string.ConversionHiragana));
            ConversionKatakana.setText(getResources().getString(R.string.ConversionKatakana));

            ResultHiragana.setText(TextUtils.join(",\n", inputQuery.getHiraganaUniqueConversions()));
            ResultKatakana.setText(TextUtils.join(",\n", inputQuery.getKatakanaUniqueConversions()));
            transliterationWaapuro.setText(TextUtils.join(",\n", inputQuery.getWaapuroUniqueConversions()));
            transliterationModHepburn.setText(TextUtils.join(",\n", inputQuery.getUniqueConversionsMH()));
            transliterationNihonShiki.setText(TextUtils.join(",\n", inputQuery.getUniqueConversionsNS()));
            transliterationKunreiShiki.setText(TextUtils.join(",\n", inputQuery.getUniqueConversionsKS()));
        }
    }

}