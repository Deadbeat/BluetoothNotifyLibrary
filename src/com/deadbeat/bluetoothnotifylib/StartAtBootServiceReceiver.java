package com.deadbeat.bluetoothnotifylib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartAtBootServiceReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d("BluetoothNotify", "--> Received BOOT_COMPLETED");
			Intent i = new Intent(context, BTNotifyService.class);
			context.startService(i);
		}
	}
}
