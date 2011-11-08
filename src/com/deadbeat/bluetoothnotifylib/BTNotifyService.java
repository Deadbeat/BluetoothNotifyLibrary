package com.deadbeat.bluetoothnotifylib;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BTNotifyService extends Service {

	// Context and Constants

	public class BTNotifyBinder extends Binder {
		BTNotifyService getService() {
			return BTNotifyService.this;
		}
	}

	private boolean freeVersion;
	private Globals globals;
	private Intent intent;

	private final Handler mHandler = new Handler();
	// Create BroadcastReceiver - We will listen for connect/disconnect for BT
	// devices
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String mAction = intent.getAction();
			Bundle mExtra = intent.getExtras();

			if (mAction.equals(getGlobals().getActionACLConnected())
					|| mAction.equals(getGlobals().getActionACLDisconnected())) {
				String device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(
						mExtra.get(getGlobals().getExtraDevice()).toString()).getAddress();
				getWorker().doLog("--> State changed for device (" + device + ")");
				getWorker().deviceStateChanged(device, mAction);
			}
		}
	};
	private BTNotifyServiceWorker worker;

	public Globals getGlobals() {
		return this.globals;
	}

	public Intent getIntent() {
		return this.intent;
	}

	public BTNotifyServiceWorker getWorker() {
		return this.worker;
	}

	public boolean isFreeVersion() {
		return this.freeVersion;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		// Create
		super.onCreate();
		Log.d("BluetoothNotify", ">>> onCreate()");
		setFreeVersion(getResources().getBoolean(R.bool.freeVersion));
		setGlobals(new Globals());
		setWorker(new BTNotifyServiceWorker(this));
		getWorker().doLog(getWorker().getTimestamp() + " Service Worker Set and Active");
		getWorker().doLog("FreeVersion = " + isFreeVersion());

		// Register BroadcastReceiver with connect and disconnect actions
		IntentFilter intentToReceiveFilter = new IntentFilter();
		intentToReceiveFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
		intentToReceiveFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
		registerReceiver(this.mIntentReceiver, intentToReceiveFilter, null, this.mHandler);
		getWorker().doLog("Bluetooth State Receiver registered");

		AppDetector detect = new AppDetector();

		if (detect.isAppInstalled(this, "com.deadbeat.bluetoothnotifyfree") == true) {
			getWorker().shutdownOnConflict();
		}
	}

	@Override
	public void onDestroy() {
		getWorker().doLog("Service Destroyed :(\n");
		unregisterReceiver(this.mIntentReceiver);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("BluetoothNotify", ">>> onStartCommand()");
		// setIntent(intent);
		// // Continue running until explicitly stopped - set sticky
		// Bundle extras = getIntent().getExtras();
		// if (extras != null) {
		// setGlobals((Globals) extras.getSerializable("Globals"));
		// }
		// if (getGlobals() == null) {
		// Log.e("BluetoothNotify",
		// "!!! Call an ambulance!  Service has no globals!");
		// }
		// Log.i(getGlobals().getLogPrefix(),
		// ">>> Bluetooth Notify Service starting up");

		return START_STICKY;
	}

	public void setFreeVersion(boolean freeVersion) {
		this.freeVersion = freeVersion;
	}

	private void setGlobals(Globals globals) {
		this.globals = globals;
	}

	// private void setIntent(Intent intent) {
	// this.intent = intent;
	// }

	private void setWorker(BTNotifyServiceWorker worker) {
		this.worker = worker;
	}

}
