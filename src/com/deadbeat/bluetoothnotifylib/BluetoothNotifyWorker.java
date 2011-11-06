package com.deadbeat.bluetoothnotifylib;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BluetoothNotifyWorker extends Activity {

	/** Join: Join array of strings with specified delimiter */
	public static String join(String[] s, String delimiter) {
		String buffer = "";
		for (int i = 0; i < s.length; i++) {
			buffer += s[i];
			if (i != s.length - 1) {
				buffer += delimiter;
			}
		}
		return buffer;
	}

	BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
	public ArrayList<String> btDeviceAddress_ar = new ArrayList<String>();
	public ListView btDeviceList;
	public ArrayList<String> btDeviceName_ar = new ArrayList<String>();

	private boolean customVibeError = false;

	private Globals globals;

	private Activity parent;

	/** Constructor */
	public BluetoothNotifyWorker(Activity parent, Globals globals) {
		setGlobals(globals);
		this.parent = parent;
	}

	public void alertError(String error) {
		doLog("==> Alert error");
		AlertDialog.Builder builder = new AlertDialog.Builder(this.parent);
		builder.setMessage(error).setCancelable(false).setNeutralButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).show();
	}

	/** Display a clickable ListView of BT devices paired to the phone */
	public void buildDeviceListView(String version) {
		doLog("--> Display device list");
		// Display list of devices
		doLog("--> btDeviceName_ar = " + this.btDeviceName_ar.toString());
		doLog("--> btDeviceAddress_ar = " + this.btDeviceAddress_ar.toString());
		doLog("--> List = " + android.R.layout.simple_list_item_1);
		doLog("--> Parent = " + this.parent.toString());
		this.btDeviceList = (ListView) this.parent.findViewById(R.id.list_bluetooth_devices);
		this.btDeviceList.setAdapter(new ArrayAdapter<String>(this.parent, android.R.layout.simple_list_item_1,
				this.btDeviceName_ar));

		TextView versionDisplay = (TextView) this.parent.findViewById(R.id.text_main_screen_version);
		versionDisplay.setText("v." + version);
		// Make device list clickable
		this.btDeviceList.setTextFilterEnabled(true);
		this.btDeviceList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				// Do something when clicked

				Intent devicePreferencesActivity = new Intent(BluetoothNotifyWorker.this.parent.getBaseContext(),
						com.deadbeat.bluetoothnotifylib.DeviceOptions.class);
				devicePreferencesActivity.putExtra("deviceName", BluetoothNotifyWorker.this.btDeviceName_ar
						.get(position));
				devicePreferencesActivity.putExtra("deviceAddress", BluetoothNotifyWorker.this.btDeviceAddress_ar
						.get(position));
				devicePreferencesActivity.putExtra("Globals", BluetoothNotifyWorker.this.globals);
				BluetoothNotifyWorker.this.parent.startActivity(devicePreferencesActivity);

			}
		});
	}

	public boolean customVibePatternValid(String input, String type) {
		String[] pattern = input.split(",");
		// Verify custom vibe pattern does not include whitespace and is numeric
		for (int i = 0; i < pattern.length; i++) {
			pattern[i] = pattern[i].trim();

			for (int j = 0; j < pattern[i].length(); j++) {
				int p = pattern[i].charAt(j);
				if (p < 48 || p > 57) {
					// value IS NOT numeric
					doLog("==> Value: " + pattern[i].toString() + " is not numeric.  Forcing 0");
					this.customVibeError = true;
				}
			}
		}

		if (this.customVibeError == true) {
			alertError("The "
					+ type
					+ " vibrate pattern:\n\""
					+ input
					+ "\"\ncontains invalid characters.\n"
					+ "\nPlease use the format: #,#,#...\n"
					+ "ex: The pattern 250,100,250 will cause the device to vibrate for 250ms, pause for 100ms, then vibrate again for 250ms.");
			this.customVibeError = false;
			return false;
		} else {
			return true;
		}
	}

	/** Log */
	public void doLog(String string) {
		if (getGlobals().isLoggingEnabled() == true) {
			Log.d(getGlobals().getLogPrefix(), string);
		}
	}

	/** Get all paired devices and store them into an array */
	public void getBTDevices() {
		// Get paired devices
		doLog("--> Getting bonded devices");
		try {
			Set<BluetoothDevice> pairedDevices = this.BTAdapter.getBondedDevices();
			doLog("--> Found (" + pairedDevices.size() + ") devices");
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					// Store the device in an ArrayList
					doLog("--> Adding device (" + device.getName() + ") to device list");
					this.btDeviceName_ar.add(device.getName());
					this.btDeviceAddress_ar.add(device.getAddress());
				}
			}
		} catch (Exception e) {
			Log.e(getGlobals().getLogPrefix(), "ER> There was an error fetching paired devices");
			e.printStackTrace();
		}
	}

	public Globals getGlobals() {
		return this.globals;
	}

	public void setGlobals(Globals globals) {
		this.globals = globals;
	}

	/** Multiple versions installed, warn and finish() */
	public void shutdownOnConflict() {
		// We need to exit the app if the other version (free/paid) is
		// installed.
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this.parent);

		if (getGlobals().isFreeVersion() == true) {
			alertbox.setMessage("WARNING: Another version of Bluetooth Notify detected!\n\n"
					+ "You are trying to launch Bluetooth Notify (Free) while "
					+ "the full version of Bluetooth Notify is installed on this device.\n\n"
					+ "Neither version can function while both are installed at the same time.  "
					+ "It is recommended that you uninstall Bluetooth Notify (Free).");
		} else {
			alertbox.setMessage("WARNING: Another version of Bluetooth Notify detected!\n\n"
					+ "You are trying to launch Bluetooth Notify while "
					+ "the free version of Bluetooth Notify is installed on this device.\n\n"
					+ "Neither version can function while both are installed at the same time.  "
					+ "It is recommended that you uninstall Bluetooth Notify (Free).");
		}
		alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				// the button was clicked
				BluetoothNotifyWorker.this.parent.finish();
			}
		});

		alertbox.show();
	}

	/** Start Service */
	public void startBTNotifyService() {

		// Start the Notify Service
		doLog("--> Starting BTNotifyService");
		try {
			Intent svc = new Intent(this.parent.getBaseContext(), BTNotifyService.class);
			svc.putExtra("Globals", getGlobals());
			this.parent.startService(svc);
		} catch (Exception e) {
			Log.e(getGlobals().getLogPrefix(), "ER> Error starting BluetoothNotifyService");
			e.printStackTrace();
		}
	}
}
