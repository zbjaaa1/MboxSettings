package com.mbx.settingsmbox;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.List;

import android.app.ActivityManager;
import android.app.SystemWriteManager;
import android.app.MboxOutputModeManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.provider.Settings;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "MboxSetting.BootReceiver";
	private static final String PREFERENCE_BOX_SETTING = "preference_box_settings";
	private SharedPreferences sharedPrefrences;
	Context mContext = null;

	private static final String FREQ_DEFAULT = "";
	private static final String FREQ_SETTING = "50hz";

	public static final boolean mAutoStartConfig = true;
	private static final String PROP_CEC_LANGUAGE_AUTO_SWITCH = "auto.swtich.language.by.cec";
	private static final String mAutoLanguagePreferencesFile = "cec_language_preferences";

	public static final int HDMICHECK_START = 18001;
	public static final int HDMICHECK_STOP = 18002;
	public static final int HDMICHECK_UNPLUGGED = 18003;
	public static final int DISABLE_OUTOUTMODE_SETTING = 18004;
	public static final int ENABLE_OUTOUTMODE_SETTING = 18005;

	public static final int EXECUTE_ONCE = 1;
	public static final int EXECUTE_MANY = 2;
	public static final int EXECUTE_UNPLUGGED = 3;
	private final boolean isForTopResolution = false;
	private final String[] mUsualResolutions = { "1080p", "1080p50hz", "1080i",
			"1080i50hz", "720p", "720p50hz", "576p", "576i", "480p", "480i" };

	private final String ACTION_OUTPUTMODE_CHANGE = "android.intent.action.OUTPUTMODE_CHANGE";
	private final String ACTION_OUTPUTMODE_SAVE = "android.intent.action.OUTPUTMODE_SAVE";
	private final String ACTION_OUTPUTMODE_CANCEL = "android.intent.action.OUTPUTMODE_CANCEL";
	private final String OUTPUT_MODE = "output_mode";
	private final String CVBS_MODE = "cvbs_mode";
	private final String mOutputStatusConfig = "/sys/class/amhdmitx/amhdmitx0/disp_cap";
	private final static String mHDMIStatusConfig = "/sys/class/amhdmitx/amhdmitx0/hpd_state";
	private final String mCurrentResolution = "/sys/class/display/mode";
	private final String mHdmiUnplugged = "/sys/class/aml_mod/mod_on";
	private final String mHdmiPlugged = "/sys/class/aml_mod/mod_off";
	private final String FreescaleFb0File = "/sys/class/graphics/fb0/free_scale";
    private static final String STR_1080SCALE = "ro.platform.has.1080scale";
    private final String VideoAxisFile = "/sys/class/video/axis";
	private final String DispFile = "/sys/class/ppmgr/disp";

    private final String ACTION_DISP_CHANGE = "android.intent.action.DISP_CHANGE";
	private final String ACTION_REALVIDEO_ON = "android.intent.action.REALVIDEO_ON";
	private final String ACTION_REALVIDEO_OFF = "android.intent.action.REALVIDEO_OFF";
	private final String ACTION_VIDEOPOSITION_CHANGE = "android.intent.action.VIDEOPOSITION_CHANGE";
	private final String ACTION_CVBSMODE_CHANGE = "android.intent.action.CVBSMODE_CHANGE";
    private final int MSG_ENABLE_OSD0_BLANK = 1;

	private SystemWriteManager sw;

	TimerTask weatherBroadcastServicesTask = null;
    WifiUtils mWifiUtils = null;
    WifiManager mWifiManager = null ;  
    String isAutoSelectOutMode = "true" ;
	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.d(TAG, "==== BootReceiver , action : " + intent.getAction());
		mContext = context;
        sw = (SystemWriteManager) mContext.getSystemService("system_write");
        sharedPrefrences = context.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        OutPutModeManager mOutputManager = new OutPutModeManager(mContext);
        String action = intent.getAction();
        
        //======================================start system boot process
		if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
            //========= for CEC
			//String isCecLanguageOpen = sharedPrefrences.getString("cec_language_open", "false");
			//if (isCecLanguageOpen.equals("true")) {
			if (Utils.DEBUG) Log.d(TAG, "===== start cec language checking service");
			Intent serviceIntent = new Intent(mContext,CecCheckingService.class);
			serviceIntent.setAction("CEC_LANGUAGE_AUTO_SWITCH");
			mContext.startService(serviceIntent);
			//}
		/* start OOBE by clei*/	
     Boolean  oobe_mode = sharedPrefrences.getBoolean("oobe_mode", true);
     Boolean showOOBE = sw.getPropertyBoolean("persist.sys.no_oobe",false);
     
         if(oobe_mode && showOOBE){
         	 Intent oobeIntent= new Intent();        
           oobeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
           oobeIntent.setComponent(new ComponentName("com.mbx.settingsmbox", "com.mbx.settingsmbox.OobeActivity"));
           mContext.startActivity(oobeIntent);
         }
            //======== for request screen
            String isRequest_screen = sw.getPropertyString("ubootenv.var.has.accelerometer","***");
            if("***".equals(isRequest_screen)){
                sw.setProperty("ubootenv.var.has.accelerometer", "false");
            }
		
            //======== for hdmi and cvbs mode check
            String currentMode = sw.readSysfs(mCurrentResolution);
            if (Utils.DEBUG) Log.d(TAG,"===== currentMode : " + currentMode);
            if(mOutputManager.isHDMIPlugged()){
                if (Utils.DEBUG) Log.d(TAG,"===== hdmi plug ");
                // we need to check wheather the tv support current resolution or not 
                // so call mOutputManager.hdmiPlugged() to check no matter hdmi auto-detection is on or off
                //if(currentMode.contains("cvbs")){;
                    mOutputManager.hdmiPlugged(); 
                //}
            }else{
                 if (Utils.DEBUG) Log.d(TAG,"===== hdmi unplug ");
                //if(sw.getPropertyBoolean("ro.platform.has.realoutputmode", false)){
                    mOutputManager.hdmiUnPlugged();  
                //}
            }

            //======== for reconecnt wifi 
            reconnectWifi();

            //======== for scrreen timeout 
        	//int timeout = sharedPrefrences.getInt("screen_timeout", -1);
            int timeout  = sharedPrefrences.getInt( "screen_timeout", Integer.MAX_VALUE);
            if (Utils.DEBUG) Log.d(TAG,"===== set timeout : " + timeout);
            Settings.System.putInt(context.getContentResolver(), "screen_off_timeout",timeout);

            //======== for Dolby and DTS
            
            if (sw.getPropertyBoolean("ro.platform.support.dolby", false)){
                initDolby();
            }
            if (sw.getPropertyBoolean("ro.platform.support.dts", false)){
                initDts();
            }
            String ipremoteBootStart = sharedPrefrences.getString("ipremote_start_bootcomplete", "false");
            if("true".equals(ipremoteBootStart)){
                Intent intent1 = new Intent("android.custom.action.BOOT_COMPLETED");
                mContext.sendBroadcast(intent1);
            }
		}
        //======================================end system boot process

        //=================for weather 
/*		if ("android.amlogic.launcher.REQUEST_WEATHER".equals(action)) {
			new WeatherBroadcastThread(mContext).start();
		}

		if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
            String value = sw.getPropertyString("sys.weather.send", "false");
            if("false".equals(value)){
			    upDateWeather();
            }
		}
    removed by clei wether */
        /*=================for save wifi info 
             *   we add wifi info to sharepreferences with the format:
             *   apname:passwork,apname:passwork
             *   so put to sharepreferences format is : ("wifi_connected_info","apname:passwork,apname:passwork")
             */ 
        
        if("android.net.conn.CONNECTIVITY_CHANGE".equals(action)){
            Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,Context.MODE_PRIVATE).edit();
            boolean isWifiConnected = WifiUtils.isWifiConnected(mContext);
            if(isWifiConnected){
                String mWifiConnectedInfo = sharedPrefrences.getString("wifi_connected_info", "***");
                String password = WifiUtils.getPassWord();  
                String mApName = WifiUtils.getApName();
                if(mApName==null)
                    return ;
                mWifiUtils = new WifiUtils(mContext);
                String currentWifiName = WifiUtils.removeDoubleQuotes(mWifiUtils.getCurrentWifiInfo().getSSID());
                if(!currentWifiName.equals(mApName)){
                    return ;
                }
                String value = null;
                String temp = null;
                if("***".equals(mWifiConnectedInfo)){
                    value = mApName+":"+password+"," ;
                }else{
                    if(!mWifiConnectedInfo.contains(mApName)){
                        value = mWifiConnectedInfo + mApName+":"+password+"," ;
                    }else{
                        StringBuilder mBuilder = new StringBuilder();
                        String[] values = mWifiConnectedInfo.split(",");
                        for (int i = 0; i < values.length; i++) {
                            if (values[i].contains(mApName)) {
                                temp = mApName + ":" + password+"," ;
                                //mBuilder.append(mApName + ":" + password+",");
                            } else {
                                mBuilder.append(values[i]+",");
                            }
                        }
                        if(temp != null)
                            mBuilder.append(temp);
                        value = mBuilder.toString();
                    }
                }
                if (Utils.DEBUG) Log.d(TAG,"===== wifi_connected_info : " + value);
                editor.putString("wifi_connected_info", value);
                editor.commit();
                //======
            }               
        }

        
        if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
            //WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            //mWifiManager.reconnect();   
             //=========for wifi connect 
        } 
	}

    void reconnectWifi(){
             mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
             String isChecked = sharedPrefrences.getString("wifi_check_box", "false");
             if(getWifiCheckBoxState()){
                mWifiManager.setWifiEnabled(true);
                mWifiUtils = new WifiUtils(mContext);
                    String mWifiConnectedInfo = sharedPrefrences.getString("wifi_connected_info", "***");
                    String apName = null;
                    String apPassword = null;
                    if(!"***".equals(mWifiConnectedInfo)){
                        String[] values = mWifiConnectedInfo.split(",");
                        String[] temp = values[values.length-1].split(":");
                        apName = temp[0];
                        apPassword = temp[1];
                        //Log.d(TAG,"===== apName : " + apName +", password : " + apPassword);
                    }
                
                String name = apName;
                if (Utils.DEBUG) Log.d(TAG,"===== get ap name : " + name);
                String password = apPassword;
                if(!"***".equals(name)){
                        List<ScanResult> mScanResultList = new ArrayList<ScanResult>();
                        mScanResultList = mWifiUtils.getWifiAccessPointList();
                        ScanResult result = null;
                        for(ScanResult s : mScanResultList){
                            if(s.SSID.equals(name)){
                                result = s;
                                break;
                            }
                        }
                    if(result != null){
                        if(!"***".equals(password) ){
                            if (Utils.DEBUG) Log.d(TAG,"===== connect to : " + name);
                            mWifiUtils.connect2AccessPoint(result,password);
                        }else{
                            if (Utils.DEBUG) Log.d(TAG,"===== connect without password" );
                            mWifiUtils.connect2AccessPoint(result,null);
                        }
                    }
                    
                }
                
             }else{
                    boolean isWifiEnabled = mWifiManager.isWifiEnabled();
                    if(isWifiEnabled){
                        mWifiManager.setWifiEnabled(false);
                    }
             }
    }

	void upDateWeather() {
		State wifiState = null;
		State mobileState = null;
        State ethState = null;
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        ethState = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).getState();
		if ((wifiState != null && State.CONNECTED == wifiState) || (ethState != null && State.CONNECTED == ethState)) {
			if (Utils.DEBUG) Log.d(TAG, "wifi connect , send weather info right now !!!");
			new WeatherBroadcastThread(mContext).start();
		}
	}

	void setSharedPrefrences() {
		Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,
				Context.MODE_PRIVATE).edit();

		editor.putInt("first_start_up", 0);
		editor.commit();
	}

	private boolean isAmlogicVideoPlayerRunning() {
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
		String className = componentName.getClassName();
		String packageName = componentName.getPackageName();

		String videoPlayerClassName = "com.farcore.videoplayer.playermenu";

		if (className.equalsIgnoreCase(videoPlayerClassName)) {
			return true;
		}
		return false;
	}

    private boolean getWifiCheckBoxState(){
        int state = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.WIFI_ON, 0);
        //int state = mWifiManager.getWifiState();
        if (Utils.DEBUG) Log.d(TAG,"===== getWifiCheckBoxState() , state : " + state);
        if(state == 1){
             if (Utils.DEBUG) Log.d(TAG,"===== getWifiCheckBoxState() , true " );
            return true ;
        }else{
            if (Utils.DEBUG) Log.d(TAG,"===== getWifiCheckBoxState() , false " );
            return false;
        }       
    }

    private void initDolby(){
        MboxOutputModeManager mMboxOutputModeManager = (MboxOutputModeManager)mContext.getSystemService(Context.MBOX_OUTPUTMODE_SERVICE);
        
        String isDrcEnable = sharedPrefrences.getString("dolby_drc_enable", "false");
        mMboxOutputModeManager.enableDobly_DRC(Boolean.parseBoolean(isDrcEnable));

        String mode = sharedPrefrences.getString("dolby_drc_mode", "2");
        mMboxOutputModeManager.setDoblyMode(mode);
    }

    private void initDts(){
        MboxOutputModeManager mMboxOutputModeManager = (MboxOutputModeManager)mContext.getSystemService(Context.MBOX_OUTPUTMODE_SERVICE);
        
        String mode = sharedPrefrences.getString("dts_downmix_mode", "0");
        mMboxOutputModeManager.setDTS_DownmixMode(mode);

        String isDrcScaleEnable = sharedPrefrences.getString("dts_drc_scale", "false");
        mMboxOutputModeManager.enableDTS_DRC_scale_control(Boolean.parseBoolean(isDrcScaleEnable));

        String isDialNormEnable = sharedPrefrences.getString("dts_dial_norm", "true");
        mMboxOutputModeManager.enableDTS_Dial_Norm_control(Boolean.parseBoolean(isDialNormEnable));       
    }
}
