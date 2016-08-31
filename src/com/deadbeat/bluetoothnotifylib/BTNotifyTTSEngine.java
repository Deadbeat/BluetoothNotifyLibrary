package com.deadbeat.bluetoothnotifylib;

import java.util.Locale;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class BTNotifyTTSEngine extends TtsProviderFactory implements TextToSpeech.OnInitListener {

    private AudioManager am;
    private TextToSpeech tts;

    @Override
    public void init(Context context) {
        if (this.tts == null) {
            this.tts = new TextToSpeech(context, this);
        }
        if (this.am == null) {
            this.am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        }
    }

    @Override
    public void onInit(int status) {
        Locale loc = new Locale("en_us", "", "");
        if (this.tts.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE) {
            this.tts.setLanguage(loc);
        }
    }

    @Override
    public void say(String sayThis) {

        // We must force the speakerphone on, or the TTS will
        // attempt to route through the BT device on disconnect.
        Log.d("BluetoothNotify", ">>> Routing TTS to Speakerphone");
        this.am.setSpeakerphoneOn(true);
        Log.d("BluetoothNotify", ">>> Speakerphone: " + this.am.isSpeakerphoneOn());
        Log.d("BluetoothNotify", ">>> Attempting to say: " + sayThis);

        this.tts.speak(sayThis, TextToSpeech.QUEUE_FLUSH, null);

        Log.d("BluetoothNotify", ">>> Turning off speakerphone");
        this.am.setSpeakerphoneOn(false);
    }

    @Override
    public void shutdown() {
        this.tts.shutdown();
    }
}