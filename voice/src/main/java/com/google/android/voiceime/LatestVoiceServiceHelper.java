
package com.google.android.voiceime;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class LatestVoiceServiceHelper extends Service {

    private static final String TAG = "ServiceHelper";

    private final IBinder mBinder = new ServiceHelperBinder();

    private Callback mCallback;

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "#onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "#onDestroy");
    }

    public void startRecognition(String languageLocale, Callback callback) {
        Log.i(TAG, "#startRecognition");
        mCallback = callback;
        Intent intent = new Intent(this, LatestVoiceHelper.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void notifyResult(String recognitionResult) {
        if (mCallback != null) {
            mCallback.onResult(recognitionResult);
        }
    }

    public interface Callback {
        void onResult(String recognitionResult);
    }

    public class ServiceHelperBinder extends Binder {
        LatestVoiceServiceHelper getService() {
            return LatestVoiceServiceHelper.this;
        }
    }
}
