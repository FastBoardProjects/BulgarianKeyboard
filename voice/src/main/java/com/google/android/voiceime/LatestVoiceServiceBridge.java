
package com.google.android.voiceime;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

class LatestVoiceServiceBridge {

    @SuppressWarnings("unused")
    private static final String TAG = "ServiceBridge";

    private final LatestVoiceApiVoiceTrigger.Callback mCallback;

    public LatestVoiceServiceBridge() {
        this(null);
    }

    public LatestVoiceServiceBridge(LatestVoiceApiVoiceTrigger.Callback callback) {
        mCallback = callback;
    }

    /** Start a voice search recognition. */
    public void startVoiceRecognition(final Context context, final String languageCode) {
        final ConnectionRequest conReq = new ConnectionRequest(languageCode);
        conReq.setServiceCallback(
                new LatestVoiceServiceHelper.Callback() {

                    @Override
                    public void onResult(final String recognitionResult) {
                        mCallback.onRecognitionResult(recognitionResult);
                        try {
                            context.unbindService(conReq);
                        } catch (IllegalArgumentException e) {
                            // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/432
                            Log.d(TAG, "Failed to unbind from service! Swallowing.", e);
                        }
                    }
                });

        context.bindService(
                new Intent(context, LatestVoiceServiceHelper.class), conReq, Context.BIND_AUTO_CREATE);
    }

    public void notifyResult(Context context, String recognitionResult) {
        ServiceConnection conn = new ConnectionResponse(context, recognitionResult);
        context.bindService(
                new Intent(context, LatestVoiceServiceHelper.class), conn, Context.BIND_AUTO_CREATE);
    }

    /** Service connection for requesting a recognition. */
    private static class ConnectionRequest implements ServiceConnection {

        private final String mLanguageCode;

        private LatestVoiceServiceHelper.Callback mServiceCallback;

        private ConnectionRequest(String languageCode) {
            mLanguageCode = languageCode;
        }

        private void setServiceCallback(LatestVoiceServiceHelper.Callback callback) {
            mServiceCallback = callback;
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LatestVoiceServiceHelper latestVoiceServiceHelper =
                    ((LatestVoiceServiceHelper.ServiceHelperBinder) service).getService();
            latestVoiceServiceHelper.startRecognition(mLanguageCode, mServiceCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // Empty
        }
    }

    /** Service connection for notifying a recognition result. */
    private static class ConnectionResponse implements ServiceConnection {

        private final String mRecognitionResult;
        private final Context mContext;

        private ConnectionResponse(Context context, String recognitionResult) {
            mRecognitionResult = recognitionResult;
            mContext = context;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Empty
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LatestVoiceServiceHelper latestVoiceServiceHelper =
                    ((LatestVoiceServiceHelper.ServiceHelperBinder) service).getService();
            latestVoiceServiceHelper.notifyResult(mRecognitionResult);
            mContext.unbindService(this);
        }
    }
}
