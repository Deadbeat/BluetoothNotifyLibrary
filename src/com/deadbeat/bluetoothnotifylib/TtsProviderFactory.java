package com.deadbeat.bluetoothnotifylib;

import android.content.Context;
import android.os.Build;

public abstract class TtsProviderFactory {

    private static TtsProviderFactory sInstance;

    public static TtsProviderFactory getInstance() {
        if (sInstance == null) {
            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
            if (sdkVersion < Build.VERSION_CODES.DONUT) {
                return null;
            }

            try {
                String className = "BTNotifyTTSEngine";
                Class<? extends TtsProviderFactory> clazz = Class.forName(
                        TtsProviderFactory.class.getPackage().getName() + "." + className).asSubclass(
                        TtsProviderFactory.class);
                sInstance = clazz.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return sInstance;
    }

    public abstract void init(Context context);

    public abstract void say(String sayThis);

    public abstract void shutdown();
}
