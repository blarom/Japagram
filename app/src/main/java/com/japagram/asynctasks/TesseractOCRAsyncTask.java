package com.japagram.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.lang.ref.WeakReference;

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
