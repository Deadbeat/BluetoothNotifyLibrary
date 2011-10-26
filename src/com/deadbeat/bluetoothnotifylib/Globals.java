package com.deadbeat.bluetoothnotifylib;

import java.io.Serializable;

public class Globals implements Serializable {

	private static final long serialVersionUID = -339875996894693836L;

	// Other Globals
	private String actionACLConnected = "android.bluetooth.device.action.ACL_CONNECTED";
	private String actionACLDisconnected = "android.bluetooth.device.action.ACL_DISCONNECTED";
	// public BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	private int btNotification = 771892746;

	private String deviceConnected = "deviceConnected";
	private String deviceDisconnected = "deviceDisconnected";
	private String extraDevice = "android.bluetooth.device.extra.DEVICE";

	private boolean freeVersion;

	private int ledNotification = 279827593;

	private boolean loggingEnabled;

	private String logPrefix = "BluetoothNotify";

	private long[] patternVibrateLong = { 0, 500 };
	private long[] patternVibrateLongShort = { 0, 500, 100, 250 };
	private long[] patternVibrateMultiLong = { 0, 500, 150, 500, 150, 500 };
	private long[] patternVibrateMultiShort = { 0, 250, 150, 250, 150, 250 };
	private long[] patternVibrateShort = { 0, 250 };
	private long[] patternVibrateShortLong = { 0, 250, 150, 500 };

	private int prefVibrateCustom = 9;
	private int prefVibrateDefault = 1;
	private int prefVibrateLong = 3;
	private int prefVibrateLongShort = 8;
	private int prefVibrateMultiLong = 6;
	private int prefVibrateMultiShort = 5;
	private int prefVibrateShort = 2;
	private int prefVibrateShortLong = 7;

	public String getActionACLConnected() {
		return this.actionACLConnected;
	}

	public String getActionACLDisconnected() {
		return this.actionACLDisconnected;
	}

	// public BluetoothAdapter getBtAdapter() {
	// return this.btAdapter;
	// }

	public int getBtNotification() {
		return this.btNotification;
	}

	public String getDeviceConnected() {
		return this.deviceConnected;
	}

	public String getDeviceDisconnected() {
		return this.deviceDisconnected;
	}

	public String getExtraDevice() {
		return this.extraDevice;
	}

	public int getLedNotification() {
		return this.ledNotification;
	}

	public String getLogPrefix() {
		return this.logPrefix;
	}

	public long[] getPatternVibrateLong() {
		return this.patternVibrateLong;
	}

	public long[] getPatternVibrateLongShort() {
		return this.patternVibrateLongShort;
	}

	public long[] getPatternVibrateMultiLong() {
		return this.patternVibrateMultiLong;
	}

	public long[] getPatternVibrateMultiShort() {
		return this.patternVibrateMultiShort;
	}

	public long[] getPatternVibrateShort() {
		return this.patternVibrateShort;
	}

	public long[] getPatternVibrateShortLong() {
		return this.patternVibrateShortLong;
	}

	public int getPrefVibrateCustom() {
		return this.prefVibrateCustom;
	}

	public int getPrefVibrateDefault() {
		return this.prefVibrateDefault;
	}

	public int getPrefVibrateLong() {
		return this.prefVibrateLong;
	}

	public int getPrefVibrateLongShort() {
		return this.prefVibrateLongShort;
	}

	public int getPrefVibrateMultiLong() {
		return this.prefVibrateMultiLong;
	}

	public int getPrefVibrateMultiShort() {
		return this.prefVibrateMultiShort;
	}

	public int getPrefVibrateShort() {
		return this.prefVibrateShort;
	}

	public int getPrefVibrateShortLong() {
		return this.prefVibrateShortLong;
	}

	public boolean isFreeVersion() {
		return this.freeVersion;
	}

	public boolean isLoggingEnabled() {
		return this.loggingEnabled;
	}

	public void setFreeVersion(boolean freeVersion) {
		this.freeVersion = freeVersion;
	}

	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

}
