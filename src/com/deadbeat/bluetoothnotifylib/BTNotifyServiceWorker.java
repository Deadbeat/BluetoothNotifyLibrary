package com.deadbeat.bluetoothnotifylib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class BTNotifyServiceWorker extends Activity {

	private boolean barEnabled;

	private String deviceName = "Unknown Device";
	// Properties for device
	private boolean enabled;
	private Globals globals;

	private boolean ledEnabled;
	private Notification ledNotification = new Notification();
	private String ledOption;
	private NotificationManager nManager;
	private Notification notification;
	private Service parent;
	private Properties properties = new Properties();

	private boolean ringEnabled;
	private String ringOption;
	private boolean toastEnabled;
	private long[] vibrateCustomPattern;
	private boolean vibrateEnabled;

	private int vibrateOption;

	/**
	 * Constructor
	 */
	protected BTNotifyServiceWorker(Service parent) {
		setGlobals(new Globals());
		setParent(parent);
	}

	@SuppressWarnings("unchecked")
	private void constructCustomVibratePattern(String type) {
		String property;
		String[] pattern;
		List list = new ArrayList();

		doLog("==> Building custom vibrate pattern");
		if (type.equals(getGlobals().getActionACLConnected())) {
			property = "pConnectCustomPattern";
		} else {
			property = "pDisconnectCustomPattern";
		}
		pattern = this.getStringProperty(property).split(",");

		// Verify custom vibe pattern does not include whitespace and is numeric
		for (int i = 0; i < pattern.length; i++) {
			pattern[i] = pattern[i].trim();

			for (int j = 0; j < pattern[i].length(); j++) {
				int p = pattern[i].charAt(j);
				if (p < 48 || p > 57) {
					// value IS NOT numeric
					doLog("Value: " + pattern[i].toString() + " is not numeric.  Forcing 0");
					pattern[i] = "0";
				}
			}
		}
		doLog("String Pattern: " + Arrays.toString(pattern));

		Long init = Long.valueOf("0");
		list.add(init);
		for (int i = 0; i < pattern.length; i++) {
			Long tmp = Long.valueOf(pattern[i]);
			list.add(tmp);
		}

		Long[] vCustomPattern = (Long[]) list.toArray(new Long[0]);

		this.vibrateCustomPattern = new long[vCustomPattern.length];
		int i = 0;
		for (Long temp : vCustomPattern) {
			this.vibrateCustomPattern[i++] = temp;
		}
		doLog("Long Pattern: " + Arrays.toString(this.vibrateCustomPattern));
	}

	/**
	 * createNotificationObject Desc: Creates the Notification object and
	 * assigns optional flags based on properties.
	 */
	private void createNotificationObject(String type) {
		// Toast is easy, just throw it...
		if (this.toastEnabled) {
			sendToastNotification(type);
		}

		/*
		 * Now it gets tricky. We have to create different Notification objects
		 * depending on what notifications we have to throw.
		 */

		// If the notification bar is enabled, we have to add all sorts of shit
		// to the object when it's created
		if (this.barEnabled) {
			int notifyIcon = R.drawable.icon;

			nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

			CharSequence tickerText = this.deviceName
					+ (type.equals(getGlobals().getActionACLConnected()) ? " connected" : " disconnected");
			long when = System.currentTimeMillis();
			this.notification = new Notification(notifyIcon, tickerText, when);
			this.notification.flags |= Notification.FLAG_AUTO_CANCEL;
			doLog("Created notification object incl. Status Bar");

			// Now that we've made the object, we need to throw all the shit
			// into it.
			Context context = this.parent.getApplicationContext();
			CharSequence contentTitle = "Bluetooth Notify";
			CharSequence contentText = tickerText;
			Intent notificationIntent = new Intent(this.parent, BTNotifyService.class);
			notificationIntent.putExtra("notificationId", getGlobals().getBtNotification());

			PendingIntent contentIntent = PendingIntent.getActivity(this.parent, 0, notificationIntent,
					Intent.FLAG_ACTIVITY_NEW_TASK);
			//this.notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			//Notification.Builder builder = new Notification.Builder(this.parent);

			doLog("Updated notification object");
		}
		// Otherwise, we can just make a simple object.
		else {
			this.notification = new Notification();
			doLog("Created notification object w/o Status Bar");
		}

		/*
		 * Ok - Now all that's done, so lets start adding on all the other types
		 * of notification the user may want.
		 */

		// Add Ringtone
		if (this.ringEnabled) {
			this.notification.sound = Uri.parse(this.ringOption);
		}

		// Add Vibration
		if (this.vibrateEnabled) {
			setVibrateNotification(this.vibrateOption);
		}

		// Set LED parameters in the LED Notification Object
		if (this.ledEnabled) {
			setLEDNotification(this.ledOption);
		}
	}

	/**
	 * deviceStateChanged Desc: Called when device state changes.
	 */
	protected void deviceStateChanged(String deviceAddress, String stateChange) {

		// On state change, read properties file
		final String devicePropertiesFileName = deviceAddress.replaceAll(":", "-");
		this.getPropertiesForDevice(devicePropertiesFileName);

		// Now let's check to see if we need to do anything.
		if (getBooleanProperty("pDeviceEnabled") == true) {
			doLog("This device is enabled");

			/*
			 * In theory, this should fix the reported issue on the Droid of
			 * multiple notifications being thrown at the same time... Using
			 * this method, we're only able to send notification one time per
			 * execution - this eliminates possible duplicates that may have
			 * been caused by odd Broadcast messages... maybe?
			 */
			// Set globals from properties
			this.setGlobalsForDevice(stateChange, deviceAddress);

			// Create notification object
			this.createNotificationObject(stateChange);

			// Make it so (yeah, Star Trek comment FTW!)
			this.sendNotification();
		}
	}

	/**
	 * Logging
	 */
	protected void doLog(String msg) {
		if (getGlobals().isLoggingEnabled() == true) {
			Log.d(getGlobals().getLogPrefix(), ">>> " + msg);
		}
	}

	/**
	 * getBooleanProperty Returns: Boolean <property value>
	 */
	private Boolean getBooleanProperty(String propertyKey) {
		// doLog("Property Requested: "+propertyKey);
		if (this.properties.containsKey(propertyKey)) {
			if (this.properties.getProperty(propertyKey).equals("true")) {
				doLog("Property (" + propertyKey + ") true.");
				return true;
			} else {
				doLog("Property (" + propertyKey + ") false.");
				return false;
			}
		} else {
			doLog("Requested device property (" + propertyKey + ") was not found");
			return false;
		}
	}

	public Globals getGlobals() {
		return this.globals;
	}

	/**
	 * getPropertiesForDevice Desc: Loads XML properties file for specified
	 * device
	 */
	private void getPropertiesForDevice(final String dAddress) {
		this.doLog("Getting properties for device: " + dAddress);

		String propertiesFileName = dAddress + ".properties";
		this.doLog("Setting file name: " + propertiesFileName);
		this.doLog("Create input stream...");
		try {
			FileInputStream fileIn = this.parent.openFileInput(propertiesFileName);
			doLog("Reading properties from file...");
			this.properties.loadFromXML(fileIn);
			doLog("Done.  Closing stream.");
			fileIn.close();

		} catch (FileNotFoundException e) {
			Log.e(getGlobals().getLogPrefix(),
					"ER> Bluetooth device connected, but no device config file found.  Ignoring device.");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(getGlobals().getLogPrefix(), "ER> IOException reading device properties");
			e.printStackTrace();
		}
	}

	/**
	 * getStringProperty Returns: String <property value>
	 */
	private String getStringProperty(String propertyKey) {
		// doLog("Property Requested: "+propertyKey);
		if (this.properties.containsKey(propertyKey)) {
			String propertyValue = this.properties.getProperty(propertyKey);
			doLog("Property (" + propertyKey + ") found: " + propertyValue);
			return propertyValue;
		} else {
			doLog("Requested device property (" + propertyKey + ") was not found");
			return null;
		}
	}

	public String getTimestamp() {
		Date todaysDate = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy/HH:mm:ss");
		String formattedDate = formatter.format(todaysDate);
		return formattedDate;
	}

	/**
	 * sendNotification Desc: Sends completed notification to the user.
	 */
	private void sendNotification() {
		// Set up a notification manager
		NotificationManager nm = (NotificationManager) this.parent.getSystemService(NOTIFICATION_SERVICE);

		// Send out the notification
		this.doLog("Sending main notification");
		nm.notify(getGlobals().getBtNotification(), this.notification);
		this.doLog("Notification sent");
		// LED Notification sent separately, for single flash
		if (this.ledEnabled) {
			this.doLog("Sending LED notification");
			nm.notify(getGlobals().getLedNotification(), this.ledNotification);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			nm.cancel(getGlobals().getLedNotification());
		}
		this.doLog("Done with all notifications");
	}

	/**
	 * sendToastNotification Desc: Simply sends the toast notification based on
	 * <type> when called
	 */
	private void sendToastNotification(String type) {
		final String message = (type.equals(getGlobals().getActionACLConnected()) ? "connected" : "disconnected");
		doLog("Sending Toast notification");
		Toast.makeText(this.parent, this.deviceName + " " + message + "!", Toast.LENGTH_LONG).show();
	}

	public void setGlobals(Globals globals) {
		this.globals = globals;
	}

	private void setGlobalsForDevice(String type, String address) {
		this.doLog("Setting globals...");
		this.doLog(type.toString());
		// First, we find the properties for the state change type
		// (connect/disconnect)
		if (type.equals(getGlobals().getActionACLConnected())) {
			// Is this device enabled?
			this.enabled = this.getBooleanProperty("pConnectEnable");
			// If yes, get all other boolean properties
			if (this.enabled) {
				this.ledEnabled = this.getBooleanProperty("pConnectLEDEnable");
				this.ringEnabled = this.getBooleanProperty("pConnectRingtoneEnable");
				this.toastEnabled = this.getBooleanProperty("pConnectToastEnable");
				this.barEnabled = this.getBooleanProperty("pConnectNotificationEnable");
				this.vibrateEnabled = this.getBooleanProperty("pConnectVibrateEnable");
			}

			// Grab necessary options for properties that have them
			if (this.ledEnabled) {
				this.ledOption = this.getStringProperty("pConnectLEDColor");
			}
			if (this.ringEnabled) {
				this.ringOption = this.getStringProperty("pConnectRingtone");
			}
			if (this.vibrateEnabled) {
				this.vibrateOption = Integer.valueOf(this.getStringProperty("pConnectVibratePattern"));
				if (this.vibrateOption == getGlobals().getPrefVibrateCustom()) {
					constructCustomVibratePattern(type);
				}
			}
		} else if (type.equals(getGlobals().getActionACLDisconnected())) {
			// Is this device enabled?
			this.enabled = this.getBooleanProperty("pDisconnectEnable");
			// If yes, get all other boolean properties
			if (this.enabled) {
				this.ledEnabled = this.getBooleanProperty("pDisconnectLEDEnable");
				this.ringEnabled = this.getBooleanProperty("pDisconnectRingtoneEnable");
				this.toastEnabled = this.getBooleanProperty("pDisconnectToastEnable");
				this.barEnabled = this.getBooleanProperty("pDisconnectNotificationEnable");
				this.vibrateEnabled = this.getBooleanProperty("pDisconnectVibrateEnable");
			}

			// Grab necessary options for properties that have them
			if (this.ledEnabled) {
				this.ledOption = this.getStringProperty("pDisconnectLEDColor");
			}
			if (this.ringEnabled) {
				this.ringOption = this.getStringProperty("pDisconnectRingtone");
			}
			if (this.vibrateEnabled) {
				this.vibrateOption = Integer.valueOf(this.getStringProperty("pDisconnectVibratePattern"));
				if (this.vibrateOption == getGlobals().getPrefVibrateCustom()) {
					constructCustomVibratePattern(type);
				}
			}
		}

		if (this.toastEnabled || this.barEnabled) {
			// BluetoothDevice btd = btAdapter.getRemoteDevice(address);
			this.deviceName = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address).getName();
			this.doLog("Got device name: " + this.deviceName);
		}
	}

	/**
	 * setLEDNotification Desc: Flashes the LED one time in the specified color
	 */
	private void setLEDNotification(String LEDColor) {
		// Hack for LEDColor
		long longValue = Long.parseLong(LEDColor, 16);
		this.ledNotification.ledARGB = (int) longValue;
		this.ledNotification.flags = Notification.FLAG_SHOW_LIGHTS;
		this.ledNotification.ledOnMS = 750;
		this.ledNotification.ledOffMS = 5000;
	}

	public void setParent(Service parent) {
		this.parent = parent;
	}

	/**
	 * setVibrateNotification Desc: Sets vibrate pattern for notification in
	 * Notification object
	 */
	private void setVibrateNotification(int vibratePattern) {

		if (vibratePattern == getGlobals().getPrefVibrateDefault()) {
			this.notification.defaults |= Notification.DEFAULT_VIBRATE;
		} else if (vibratePattern == getGlobals().getPrefVibrateShort()) {
			this.notification.vibrate = getGlobals().getPatternVibrateShort();
		} else if (vibratePattern == getGlobals().getPrefVibrateLong()) {
			this.notification.vibrate = getGlobals().getPatternVibrateLong();
		} else if (vibratePattern == getGlobals().getPrefVibrateMultiShort()) {
			this.notification.vibrate = getGlobals().getPatternVibrateMultiShort();
		} else if (vibratePattern == getGlobals().getPrefVibrateMultiLong()) {
			this.notification.vibrate = getGlobals().getPatternVibrateMultiLong();
		} else if (vibratePattern == getGlobals().getPrefVibrateShortLong()) {
			this.notification.vibrate = getGlobals().getPatternVibrateShortLong();
		} else if (vibratePattern == getGlobals().getPrefVibrateLongShort()) {
			this.notification.vibrate = getGlobals().getPatternVibrateLongShort();
		} else if (vibratePattern == getGlobals().getPrefVibrateCustom()) {
			this.notification.vibrate = this.vibrateCustomPattern;
		}
	}

	/**
	 * shutdownOnConflict() Desc: Shutdown service if multiple version are
	 * detected by AppDetector
	 */
	protected void shutdownOnConflict() {
		// We need to exit the service if other version (free/paid) are
		// installed.
		doLog("Service shutdown: Multiple versions installed.");
		this.parent.stopSelf();
	}
}
