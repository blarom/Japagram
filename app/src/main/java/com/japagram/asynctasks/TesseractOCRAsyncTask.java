package com.japagram.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.japagram.R;
import com.japagram.data.Word;
import com.japagram.resources.Utilities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TesseractOCRAsyncTask extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> contextRef;
    public TesseractOCRAsyncResponseHandler listener;
    final TessBaseAPI mTess;
    private boolean mAllowLoaderStart;

    public TesseractOCRAsyncTask(Context context, TessBaseAPI mTess, TesseractOCRAsyncResponseHandler listener) {
        contextRef = new WeakReference<>(context);
        this.listener = listener;
        this.mTess = mTess;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected String doInBackground(Void... voids) {

        String result = mTess.getUTF8Text();
        //String result = mTess.getHOCRText(0);
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        listener.onTesseractOCRAsyncTaskResultFound(result);
    }

    public interface TesseractOCRAsyncResponseHandler {
        void onTesseractOCRAsyncTaskResultFound(String result);
    }
}
