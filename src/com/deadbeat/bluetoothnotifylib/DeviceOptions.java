package com.deadbeat.bluetoothnotifylib;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class DeviceOptions extends PreferenceActivity {
	private CheckBoxPreference cRingtoneCheckBoxPreference;
	private CheckBoxPreference cVibrateCheckboxPreference;

	String deviceAddress;

	String deviceName;
	private CheckBoxPreference dRingtoneCheckBoxPreference;
	private CheckBoxPreference dVibrateCheckboxPreference;
	private boolean freeVersion;
	private Globals globals;
	OnSharedPreferenceChangeListener ospcListener;
	private String pConnectCustomPattern;
	// Connect variables
	private boolean pConnectEnable;
	private String pConnectLEDColor;
	private boolean pConnectLEDEnable;

	private boolean pConnectNotificationEnable;
	private String pConnectRingtone;
	private boolean pConnectRingtoneEnable;
	private boolean pConnectToastEnable;
	private boolean pConnectVibrateEnable;
	private String pConnectVibratePattern;
	// Device Enabled
	private boolean pDeviceEnabled;
	private String pDisconnectCustomPattern;
	// Disconnect Variables
	private boolean pDisconnectEnable;
	private String pDisconnectLEDColor;

	private boolean pDisconnectLEDEnable;
	private boolean pDisconnectNotificationEnable;

	private String pDisconnectRingtone;
	private boolean pDisconnectRingtoneEnable;
	private boolean pDisconnectToastEnable;
	private boolean pDisconnectVibrateEnable;
	private String pDisconnectVibratePattern;
	PreferenceActivity preferenceActivity;
	SharedPreferences prefs;
	private BluetoothNotifyWorker worker;

	public Globals getGlobals() {
		return this.globals;
	}

	private void getPrefs() {

		// Device Enabled
		this.pDeviceEnabled = this.prefs.getBoolean("pref_enable", false);

		// Connect Preferences
		this.pConnectEnable = this.prefs.getBoolean("pref_connect_enable", true);
		this.pConnectRingtoneEnable = this.prefs.getBoolean("pref_connect_ringtone_enable", true);
		this.pConnectRingtone = this.prefs.getString("pref_connect_ringtone",
				"content://settings/system/notification_sound");
		this.pConnectLEDEnable = this.prefs.getBoolean("pref_connect_led_enable", false);
		this.pConnectLEDColor = this.prefs.getString("pref_connect_led_color", "ff0000ff");
		this.pConnectNotificationEnable = this.prefs.getBoolean("pref_connect_notification_enable", false);
		this.pConnectToastEnable = this.prefs.getBoolean("pref_connect_toast_enable", false);
		this.pConnectVibrateEnable = this.prefs.getBoolean("pref_connect_vibrate_enable", false);
		this.pConnectVibratePattern = this.prefs.getString("pref_connect_vibrate_pattern", "1");
		this.pConnectCustomPattern = this.prefs.getString("pref_connect_custom_pattern", "100,50,100");

		if (getWorker().customVibePatternValid(this.pConnectCustomPattern, "connect") != true) {
			getWorker().doLog("==> Overriding connect pattern (" + this.pConnectCustomPattern + ")");
			this.pConnectCustomPattern = "250,100,250";
			Editor prefsEditor = this.prefs.edit();
			prefsEditor.putString("pref_connect_custom_pattern", this.pConnectCustomPattern);
			prefsEditor.commit();
		}

		// Disconnect Preferences
		this.pDisconnectEnable = this.prefs.getBoolean("pref_disconnect_enable", false);
		this.pDisconnectRingtoneEnable = this.prefs.getBoolean("pref_disconnect_ringtone_enable", true);
		this.pDisconnectRingtone = this.prefs.getString("pref_disconnect_ringtone",
				"content://settings/system/notification_sound");
		this.pDisconnectLEDEnable = this.prefs.getBoolean("pref_disconnect_led_enable", false);
		this.pDisconnectLEDColor = this.prefs.getString("pref_disconnect_led_color", "ff0000ff");
		this.pDisconnectNotificationEnable = this.prefs.getBoolean("pref_disconnect_notification_enable", false);
		this.pDisconnectToastEnable = this.prefs.getBoolean("pref_disconnect_toast_enable", false);
		this.pDisconnectVibrateEnable = this.prefs.getBoolean("pref_disconnect_vibrate_enable", false);
		this.pDisconnectVibratePattern = this.prefs.getString("pref_disconnect_vibrate_pattern", "1");
		this.pDisconnectCustomPattern = this.prefs.getString("pref_disconnect_custom_pattern", "100,50,100");

		if (getWorker().customVibePatternValid(this.pDisconnectCustomPattern, "connect") != true) {
			getWorker().doLog("==> Overriding connect pattern (" + this.pDisconnectCustomPattern + ")");
			this.pDisconnectCustomPattern = "250,100,250";
			Editor prefsEditor = this.prefs.edit();
			prefsEditor.putString("pref_disconnect_custom_pattern", this.pDisconnectCustomPattern);
			prefsEditor.commit();
		}
	}

	public BluetoothNotifyWorker getWorker() {
		return this.worker;
	}

	public boolean isFreeVersion() {
		return this.freeVersion;
	}

	@Override
	protected void onCreate(Bundle btNotify) {
		super.onCreate(btNotify);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			this.deviceName = extras.getString("deviceName");
			this.deviceAddress = extras.getString("deviceAddress");
			setGlobals((Globals) extras.getSerializable("Globals"));
		}

		setFreeVersion(getResources().getBoolean(R.bool.freeVersion));

		this.setWorker(new BluetoothNotifyWorker(this));

		// Strip spaces from device name for preference filename
		this.deviceName = this.deviceName.replaceAll(" ", "");
		getWorker()
				.doLog("==> Displaying preferences for device: " + this.deviceName + " (" + this.deviceAddress + ")");

		this.deviceAddress = this.deviceAddress.replaceAll(":", "-");

		getWorker().doLog("==> Setting preferenceManager.sharedPreferenceName: " + this.deviceAddress);
		getPreferenceManager().setSharedPreferencesName(this.deviceAddress);
		this.prefs = getPreferenceManager().getSharedPreferences();

		// The only way i can think to do this, is to get preferences and write
		// properties every time a pref is changed... so... hope it's not rough
		// on performance.
		this.ospcListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

				getPrefs();
				saveSharedPreferencesToFile();

				// When the user chooses an LED color, we should flash the LED
				// to give them a preview (if their phone supports it).
				// Must check for Device Enabled before hand though - if we
				// don't check
				// the led will flash when the default preferences are written
				// the first time the app
				// sees a new device.
				Boolean thisDeviceEnabled = sharedPreferences.getBoolean("pref_enable", false);

				// Only attempt to flash when the preference that was changed
				// was an LED color
				if (key.contains("_led_color") && thisDeviceEnabled == true) {

					// Set up a notification manager
					NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					Notification notification = new Notification();
					// Hack for LEDColor
					long longValue = Long.parseLong(sharedPreferences.getString(key, "ff0000ff"), 16);
					notification.ledARGB = (int) longValue;
					notification.flags = Notification.FLAG_SHOW_LIGHTS;
					notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
					notification.ledOnMS = 750;
					notification.ledOffMS = 1500;

					// Throw the notification, sleep for 1 second, and cancel it
					// This makes the LED flash only 1 time, instead of forever.
					nm.notify(675645342, notification);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					nm.cancel(675645342);
				}
				// Same concept, but preview the vibrate pattern
				else if (key.contains("_vibrate_pattern") && thisDeviceEnabled == true) {
					int vibratePattern = Integer.valueOf(sharedPreferences.getString(key, "1"));

					NotificationManager vnm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					Notification notification = new Notification();

					if (vibratePattern == getGlobals().getPrefVibrateDefault()) {
						notification.defaults |= Notification.DEFAULT_VIBRATE;
					} else if (vibratePattern == getGlobals().getPrefVibrateShort()) {
						notification.vibrate = getGlobals().getPatternVibrateShort();
					} else if (vibratePattern == getGlobals().getPrefVibrateLong()) {
						notification.vibrate = getGlobals().getPatternVibrateLong();
					} else if (vibratePattern == getGlobals().getPrefVibrateMultiShort()) {
						notification.vibrate = getGlobals().getPatternVibrateMultiShort();
					} else if (vibratePattern == getGlobals().getPrefVibrateMultiLong()) {
						notification.vibrate = getGlobals().getPatternVibrateMultiLong();
					} else if (vibratePattern == getGlobals().getPrefVibrateShortLong()) {
						notification.vibrate = getGlobals().getPatternVibrateShortLong();
					} else if (vibratePattern == getGlobals().getPrefVibrateLongShort()) {
						notification.vibrate = getGlobals().getPatternVibrateLongShort();
					}

					vnm.notify(5089250, notification);
				}
				// Preview custom vibe patterns
				else if (key.contains("_custom_pattern") && thisDeviceEnabled == true) {
					String[] pattern = sharedPreferences.getString(key, "250,100,150").split(",");
					long[] vibrateCustomPattern;
					List list = new ArrayList();
					// Verify custom vibe pattern does not include whitespace
					// and is numeric
					for (int i = 0; i < pattern.length; i++) {
						pattern[i] = pattern[i].trim();

						for (int j = 0; j < pattern[i].length(); j++) {
							int p = pattern[i].charAt(j);
							if (p < 48 || p > 57) {
								// value IS NOT numeric
								pattern[i] = "0";
							}
						}
					}
					getWorker().doLog("==> Previewing Pattern: " + Arrays.toString(pattern));

					Long init = Long.valueOf("0");
					list.add(init);
					for (int i = 0; i < pattern.length; i++) {
						Long tmp = Long.valueOf(pattern[i]);
						list.add(tmp);
					}

					Long[] vCustomPattern = (Long[]) list.toArray(new Long[0]);

					vibrateCustomPattern = new long[vCustomPattern.length];
					int i = 0;
					for (Long temp : vCustomPattern) {
						vibrateCustomPattern[i++] = temp;
					}

					NotificationManager vnm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					Notification notification = new Notification();
					notification.vibrate = vibrateCustomPattern;
					vnm.notify(5089250, notification);
				}
			}
		};

		// Listen for preference changes
		this.prefs.registerOnSharedPreferenceChangeListener(this.ospcListener);
		addPreferencesFromResource(R.xml.devicepreferences);

		getPrefs();

		// If this is the free version - force disable audio notification
		if (isFreeVersion() == true) {
			// Disable connect ringtone
			this.cRingtoneCheckBoxPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(
					"pref_connect_ringtone_enable");
			this.cRingtoneCheckBoxPreference
					.setSummary("Disabled in free version.\nGet the full version to enable this option.");
			this.cRingtoneCheckBoxPreference.setSelectable(false);
			this.cRingtoneCheckBoxPreference.setChecked(false);

			// Disable disconnect ringtone
			this.dRingtoneCheckBoxPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(
					"pref_disconnect_ringtone_enable");
			this.dRingtoneCheckBoxPreference
					.setSummary("Disabled in free version.\nGet the full version to enable this option.");
			this.dRingtoneCheckBoxPreference.setSelectable(false);
			this.dRingtoneCheckBoxPreference.setChecked(false);

			// Disable connect vibrate (oh they gonna be so pissed)
			this.cVibrateCheckboxPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(
					"pref_connect_vibrate_enable");
			this.cVibrateCheckboxPreference
					.setSummary("Disabled in the free version.\nGet the full version to enable this option.");
			this.cVibrateCheckboxPreference.setSelectable(false);
			this.cVibrateCheckboxPreference.setChecked(false);

			this.dVibrateCheckboxPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(
					"pref_disconnect_vibrate_enable");
			this.dVibrateCheckboxPreference
					.setSummary("Disabled in the free version.\nGet the full version to enable this option.");
			this.dVibrateCheckboxPreference.setSelectable(false);
			this.dVibrateCheckboxPreference.setChecked(false);

			Editor prefsEditor = this.prefs.edit();
			prefsEditor.putBoolean("pref_connect_ringtone_enable", false);
			prefsEditor.putBoolean("pref_disconnect_ringtone_enable", false);
			prefsEditor.putBoolean("pref_connect_vibrate_enable", false);
			prefsEditor.putBoolean("pref_disconnect_vibrate_enable", false);
			prefsEditor.commit();
		}

	}

	@Override
	protected void onDestroy() {
		// When we leave the preferences screen, save all prefs one last time,
		// just to be sure.
		super.onDestroy();
		getPrefs();
		saveSharedPreferencesToFile();
		this.prefs.unregisterOnSharedPreferenceChangeListener(this.ospcListener);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Just to make sure we get all prefs written - we'll write them when we
		// leave the pref screen
		getPrefs();
		saveSharedPreferencesToFile();

		// Stop listening for changes when not on this screen
		this.prefs.unregisterOnSharedPreferenceChangeListener(this.ospcListener);
		getWorker().doLog("==> Unregister Change Listener");

	}

	@Override
	protected void onResume() {
		super.onResume();
		this.prefs.registerOnSharedPreferenceChangeListener(this.ospcListener);
	}

	private void saveSharedPreferencesToFile() {

		// Write preferences to properties file
		getWorker().doLog("==> Setting properties");
		Properties properties = new Properties();

		// Device Enabled
		properties.setProperty("pDeviceEnabled", new Boolean(this.pDeviceEnabled).toString());

		// Connect Properties
		properties.setProperty("pConnectEnable", new Boolean(this.pConnectEnable).toString());
		properties.setProperty("pConnectRingtoneEnable", new Boolean(this.pConnectRingtoneEnable).toString());
		properties.setProperty("pConnectRingtone", this.pConnectRingtone);
		properties.setProperty("pConnectLEDEnable", new Boolean(this.pConnectLEDEnable).toString());
		properties.setProperty("pConnectLEDColor", this.pConnectLEDColor);
		properties.setProperty("pConnectNotificationEnable", new Boolean(this.pConnectNotificationEnable).toString());
		properties.setProperty("pConnectToastEnable", new Boolean(this.pConnectToastEnable).toString());
		properties.setProperty("pConnectVibrateEnable", new Boolean(this.pConnectVibrateEnable).toString());
		properties.setProperty("pConnectVibratePattern", this.pConnectVibratePattern);
		properties.setProperty("pConnectCustomPattern", this.pConnectCustomPattern);

		// Disconnect Properties
		properties.setProperty("pDisconnectEnable", new Boolean(this.pDisconnectEnable).toString());
		properties.setProperty("pDisconnectRingtoneEnable", new Boolean(this.pDisconnectRingtoneEnable).toString());
		properties.setProperty("pDisconnectRingtone", this.pDisconnectRingtone);
		properties.setProperty("pDisconnectLEDEnable", new Boolean(this.pDisconnectLEDEnable).toString());
		properties.setProperty("pDisconnectLEDColor", this.pDisconnectLEDColor);
		properties.setProperty("pDisconnectNotificationEnable", new Boolean(this.pDisconnectNotificationEnable)
				.toString());
		properties.setProperty("pDisconnectToastEnable", new Boolean(this.pDisconnectToastEnable).toString());
		properties.setProperty("pDisconnectVibrateEnable", new Boolean(this.pDisconnectVibrateEnable).toString());
		properties.setProperty("pDisconnectVibratePattern", this.pDisconnectVibratePattern);
		properties.setProperty("pDisconnectCustomPattern", this.pDisconnectCustomPattern);

		try {
			String propertiesFileName = this.deviceAddress + ".properties";
			getWorker().doLog("==> Setting file name: " + propertiesFileName);
			FileOutputStream fileOut = openFileOutput(propertiesFileName, Context.MODE_WORLD_READABLE);
			getWorker().doLog("==> Writing properties");
			properties.storeToXML(fileOut, this.deviceName);
			fileOut.close();
		} catch (FileNotFoundException e) {
			Log.e(getGlobals().getLogPrefix(), "ER> (DeviceOptions) Preferences File not found for device: "
					+ this.deviceAddress);
			Toast.makeText(this, "Error writing device options, please email the developer!", Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
			finish();
		} catch (IOException e) {
			Toast.makeText(this, "Error writing device options, please email the developer!", Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
			finish();
		}

	}

	public void setFreeVersion(boolean freeVersion) {
		this.freeVersion = freeVersion;
	}

	public void setGlobals(Globals globals) {
		this.globals = globals;
	}

	public void setWorker(BluetoothNotifyWorker worker) {
		this.worker = worker;
	}
}
