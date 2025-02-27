package com.mbx.settingsmbox;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; 
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.UEventObserver;
import android.util.Log;

public class CecCheckingService extends Service {

	private String TAG = "CecCheckingService";
	private static final String mCECLanguageConfig = "/sys/class/switch/lang_config/state";
	public static String cec_device_file = "/sys/devices/virtual/switch/lang_config/state";
	private static final String PREFERENCE_BOX_SETTING = "preference_box_settings";
	private boolean startObServing = false; 
	private String cec_config_path = "DEVPATH=/devices/virtual/switch/lang_config";
 
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { 
     	startListenCecDev();
        return super.onStartCommand(intent, flags, startId);
    }

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public void startListenCecDev() {
		Log.d(TAG, "startListenCecDev()");
		if (new File(cec_device_file).exists() && !startObServing) {
			mCedObserver.startObserving(cec_config_path);
			startObServing = true;
		}
	}

	private UEventObserver mCedObserver = new UEventObserver() {
		@Override
		public void onUEvent(UEventObserver.UEvent event) {
			if (Utils.DEBUG) Log.d(TAG, "onUEvent()");

			SharedPreferences sharedpreference = getSharedPreferences(
					PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE);


			String mNewLanguage = event.get("SWITCH_STATE");
			if (Utils.DEBUG) Log.d(TAG, "get the language code is : " + mNewLanguage);

			String isCecLanguageOpen = sharedpreference.getString(
					"cec_language_open", "false");
			if (isCecLanguageOpen.equals("false")) {
				if (Utils.DEBUG) Log.d(TAG, "cec language not open");
				return;
			}
			int i = -1;
			String[] cec_language_list = getResources().getStringArray(
					R.array.cec_language);
			for (int j = 0; j < cec_language_list.length; j++) {
				if (mNewLanguage != null
						&& mNewLanguage.trim().equals(cec_language_list[j])) {
					i = j;
					break;
				}
			}
			if (i >= 0) {
				String able = getResources().getConfiguration().locale
						.getCountry();
				String[] language_list = getResources().getStringArray(
						R.array.language);
				String[] country_list = getResources().getStringArray(
						R.array.country);
				if (able.equals(country_list[i])) {
					if (Utils.DEBUG) Log.d(TAG, "no need to change language");
					return;
				} else {
					Locale l = new Locale(language_list[i], country_list[i]);
					if (Utils.DEBUG) Log.d(TAG, "change the language right now !!!");
					updateLanguage(l);
				}
			} else {
				Log.d(TAG, "the language code is not support right now !!!");
			}
		}
	};

	public static void updateLanguage(Locale locale) {
		try {
			Object objIActMag;
			Class clzIActMag = Class.forName("android.app.IActivityManager");
			Class clzActMagNative = Class
					.forName("android.app.ActivityManagerNative");
			Method mtdActMagNative$getDefault = clzActMagNative
					.getDeclaredMethod("getDefault");

			objIActMag = mtdActMagNative$getDefault.invoke(clzActMagNative);

			Method mtdIActMag$getConfiguration = clzIActMag
					.getDeclaredMethod("getConfiguration");
			Configuration config = (Configuration) mtdIActMag$getConfiguration
					.invoke(objIActMag);
			config.locale = locale;

			Class[] clzParams = { Configuration.class };
			Method mtdIActMag$updateConfiguration = clzIActMag
					.getDeclaredMethod("updateConfiguration", clzParams);
			mtdIActMag$updateConfiguration.invoke(objIActMag, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		Log.d(TAG,"onDestroy");
		if(startObServing){
			mCedObserver.stopObserving();
		}
		super.onDestroy();
	}

}
