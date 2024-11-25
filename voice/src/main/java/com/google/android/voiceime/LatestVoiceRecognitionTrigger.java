

package com.google.android.voiceime;

import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.InputMethodSubtype;


public class LatestVoiceRecognitionTrigger {

    private final InputMethodService mInputMethodService;

    private LatestVoiceTrigger mLatestVoiceTrigger;

    private LatestVoiceImeVoiceTrigger mVoiceImeTrigger;
    private LatestVoiceApiVoiceTrigger mVoiceApiTrigger;

    public LatestVoiceRecognitionTrigger(InputMethodService inputMethodService) {
        mInputMethodService = inputMethodService;
        mLatestVoiceTrigger = getTrigger();
    }

    private LatestVoiceTrigger getTrigger() {
        if (LatestVoiceImeVoiceTrigger.isInstalled(mInputMethodService)) {
            return getImeTrigger();
        } else if (LatestVoiceApiVoiceTrigger.isInstalled(mInputMethodService)) {
            return getIntentTrigger();
        } else {
            return null;
        }
    }

    private LatestVoiceTrigger getIntentTrigger() {
        if (mVoiceApiTrigger == null) {
            mVoiceApiTrigger = new LatestVoiceApiVoiceTrigger(mInputMethodService);
        }
        return mVoiceApiTrigger;
    }

    private LatestVoiceTrigger getImeTrigger() {
        if (mVoiceImeTrigger == null) {
            mVoiceImeTrigger = new LatestVoiceImeVoiceTrigger(mInputMethodService);
        }
        return mVoiceImeTrigger;
    }

    public boolean isInstalled() {
        return mLatestVoiceTrigger != null;
    }

    public boolean isEnabled() {
        return true;
    }

    /**
     * Starts a voice recognition
     *
     * @param language The language in which the recognition should be done. If the recognition is
     *                 done through the Google voice typing, the parameter is ignored and the recognition is
     *                 done using the locale of the calling IME.
     * @see InputMethodSubtype
     */
    public void startVoiceRecognition(String language) {
        if (mLatestVoiceTrigger != null) {
            mLatestVoiceTrigger.startVoiceRecognition(language);
        }
    }

    public void onStartInputView() {
        if (mLatestVoiceTrigger != null) {
            mLatestVoiceTrigger.onStartInputView();
        }

        // The trigger is refreshed as the system may have changed in the meanwhile.
        mLatestVoiceTrigger = getTrigger();
    }
}
