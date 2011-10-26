package com.deadbeat.bluetoothnotifylib;

import android.app.Activity;
import android.app.Service;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppDetector {
	protected boolean isAppInstalled(Service parent, String uri) {
		PackageManager pm = parent.getPackageManager();
		return isInstalledHelper(pm, uri);
	}
	
	public boolean isAppInstalled(Activity parent, String uri) {
		PackageManager pm = parent.getPackageManager();
		return isInstalledHelper(pm, uri);
	}
	
	 boolean isInstalledHelper(PackageManager pm, String uri) {
		 boolean installed = false;
		 try {
			 pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			 installed = true;
		 } catch (PackageManager.NameNotFoundException e) {
			 installed = false;
		 }
		 return installed;
	}

	/**
	 * getVersion
	 * Desc: Get the version tag of the app at specified URI
	 * @param parent
	 * @param uri
	 * @return
	 */
	public String getVersion(Activity parent, String uri) {
		String version = null;
		PackageManager pm = parent.getPackageManager();
		try {
			PackageInfo pInfo = pm.getPackageInfo(uri, PackageManager.GET_META_DATA);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			version = "0.0";
		}
		return version;
	}
}
