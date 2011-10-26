package com.deadbeat.bluetoothnotifylib;

//public class AppSelectorPreference extends ListPreference {
//	
//	public AppSelectorPreference(Context context, AttributeSet attrs) {
//		super(context,attrs);
//		
//		PackageManager pm = context.getPackageManager();
//		List<PackageInfo> appListInfo = pm.getInstalledPackages(0);	
//		CharSequence[] entries = new CharSequence[appListInfo.size()];
//        CharSequence[] entryValues = new CharSequence[appListInfo.size()];
//        
//		try {
//			int i = 0;
//	        for (PackageInfo p : appListInfo) {
//	            if (p.applicationInfo.uid > 10000) {
//	                entries[i] = p.applicationInfo.loadLabel(pm).toString();
//	                entryValues[i] = p.applicationInfo.packageName.toString();	            
//	                Log.d(BT,"Label: " + entries[i]);
//	                Log.d(BT,"PName: " + entryValues[i]);
//	                i++;
//	            }		  
//	        }
//		} catch (Exception e) {
//    		Log.e(BT,"ER> Error starting BluetoothNotifyService");
//    		e.printStackTrace();
//    	}	
//		
//		setEntries(entries);
//		setEntryValues(entryValues);
//	}
//	
// }
