package com.mbx.settingsmbox;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.SystemWriteManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManagerPolicy;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OobeActivity extends Activity implements OnItemClickListener,
		OnClickListener, OnFocusChangeListener {
	private static final String TAG = "OobeActivity";
	private String SYSTEM_PROP = "persist.sys.server_ip";
    private final static String DISPLAY_MODE_SYSFS = "/sys/class/display/mode";
    private static final String PREFERENCE_BOX_SETTING = "preference_box_settings";
    private final static int UPDATE_AP_LIST = 100;
    private final static int UPDATE_OUTPUT_MODE_UI = 101;
    private final static int UPDATE_ETH_STATUS = 102;
    private static final String eth_device_sysfs = "/sys/class/ethernet/linkspeed";
    
    private static final String GOOGLE_VOICE_TYPE="com.android.tts/com.google.android.voicesearch.GoogleRecognitionService";
    private static final String XUNFEI_VOICE_TYPE="com.iflytek.speechcloud/com.iflytek.iatservice.SpeechService";
    
    private final int MAX_Height = 100;
    private final int MIN_Height = 80;
    
    public final static int OOBE_WELCOME = 0;
	public final static int OOBE_LANGUAGE = 1;
	public final static int OOBE_NETWORK = 2;
	public final static int OOBE_SCREEN_ADJUST = 3;
	public final static  int OOBE_VOICE_SEARCH=4;
	public final static  int OOBE_TIME_ZONE=5;
	public final static  int OOBE_OTA_UPDATE_STTINT=6;

    private int mCuttentView = 0;

	public static final int ETH_STATE_UNKNOWN = 0;
	public static final int ETH_STATE_DISABLED = 1;
	public static final int ETH_STATE_ENABLED = 2;

	private boolean isDisplayView = false;
	private int screen_rate = MIN_Height;
	ImageView img_num_hundred = null;
	ImageView img_num_ten = null;
	ImageView img_num_unit = null;
	ImageButton btn_position_zoom_out = null;
	ImageView img_progress_bg;
	ScreenPositionManager mScreenPositionManager = null;

	ImageButton btn_position_zoom_in = null;

	private final static int DALAY_TIME = 10000;
	private final Context mContext = this;
	View oobe_welcome;
	View oobe_language;
	View oobe_time_zone;
	View oobe_net;
	View oobe_screenadjust;
	View oobe_ota_update_setting;

	LinearLayout top_welcome = null;
	LinearLayout top_language = null;
	LinearLayout top_time_zone = null;
	LinearLayout top_network = null;
	LinearLayout top_screenadjust = null;
	LinearLayout top_ota_update_setting = null;
	LinearLayout top_voice_search =null;

	Button button_welcome_skip = null;
	Button button_welcome_next = null;
	Button button_language_previous = null;
	Button button_language_next = null;
	Button button_time_zone_select = null;
	Button button_time_zone_previous = null;
	Button button_time_zone_next = null;
	Button button_net_previous = null;
	Button button_net_finish = null;
	Button button_screen_previous = null;
	Button button_screen_next = null;
	Button button_update_setting_previous = null;
	Button button_update_setting_finish = null;
	Button button_net_next=null;
	Button button_voice_previous=null;
	Button button_voice_finish=null;
	
    private int mCurrentLanguage = -1;   //  0: chinese simple , 1: english ,2: chinese taiwan

    private LinearLayout layoutTimeZone = null;
    private EditText editTextUpdateIp;
	//GridAdapter mlanguageAdapter = null;

    //============network 
	private AccessPointListAdapter oobe_mAccessPointListAdapter = null;
	private Timer timer = null;
	private TimerTask task = null;
    private LinearLayout oobe_wifi_connected;
	private LinearLayout oobe_wifi_input_password;

	private LinearLayout oobe_wifi_not_connect;

	private TextView oobe_wifi_slect_tip;

	private EditText oobe_password_editview;

	private TextView oobe_wifi_listview_tip;
	private ListView oobe_mAcessPointListView = null;
	

	private TextView oobe_wifi_ssid_value;
	private TextView oobe_ip_address_value;

	private TextView oobe_select_wifi;

	private TextView oobe_select_ethernet;

	private TextView oobe_wifi_connected_tip;
	
	private TextView oobe_select_google ;
	private TextView oobe_select_xunfei;
	

	private LinearLayout oobe_root_eth_view;
    private LinearLayout oobe_net_root_view;
	private LinearLayout oobe_root_wifi_view;
	private LinearLayout oobe_voice_search;
	

	private EthernetManager oobe_mEthernetManager;
	private WifiManager oobe_mWifiManager;
    private TextView oobe_eth_IP_value = null;
	private TextView oobe_eth_connected_tip = null;
	private LinearLayout oobe_eth_ip_layout = null;
	
	NetworkStateReceiver mNetworkStateReceiver = null;
    private MyHandle mHander = null;
    //============end 
	

    private SystemWriteManager sw = null;

	RelativeLayout top_welcome_layout = null;
	RelativeLayout top_language_layout;
	RelativeLayout top_time_zone_layout;
	RelativeLayout top_network_layout;
	RelativeLayout top_screen_layout;
	RelativeLayout top_update_setting_layout;
	ImageView around_line;
    private ImageView select_cn = null;
    private ImageView select_english = null;
    private ImageView select_tw = null;
	private int Num[] = { R.drawable.ic_num0, R.drawable.ic_num1,
			R.drawable.ic_num2, R.drawable.ic_num3, R.drawable.ic_num4,
			R.drawable.ic_num5, R.drawable.ic_num6, R.drawable.ic_num7,
			R.drawable.ic_num8, R.drawable.ic_num9 };
	private int progressNum[] = { R.drawable.ic_per_81, R.drawable.ic_per_82,
			R.drawable.ic_per_83, R.drawable.ic_per_84, R.drawable.ic_per_85,
			R.drawable.ic_per_86, R.drawable.ic_per_87, R.drawable.ic_per_88,
			R.drawable.ic_per_89, R.drawable.ic_per_90, R.drawable.ic_per_91,
			R.drawable.ic_per_92, R.drawable.ic_per_93, R.drawable.ic_per_94,
			R.drawable.ic_per_95, R.drawable.ic_per_96, R.drawable.ic_per_97,
			R.drawable.ic_per_98, R.drawable.ic_per_99, R.drawable.ic_per_100 };
    
    private static boolean isSupportEthernet = true ;
    private static boolean isGotoLanguageView = false ;
    private SharedPreferences sharepreference = null;
    private static boolean isFirstStartActivity = true;

    private OobeDisplayConfirmDialog dialog = null ;
    private TextView oobe_show_password = null;
    private long startTime = 0;
    private long endTime = 0;
    private final int SECURITY_WPA = 1;
    
    private GridView langGrd;
    private TextView textViewZimeZone;
    
    public static final int UPDATE_TIME_ZONE = 0x50;
    public Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
			case UPDATE_TIME_ZONE:
				oobe_time_zone.setVisibility(View.VISIBLE);
				layoutTimeZone.setVisibility(View.GONE);
				updateTimeZoneTip();
				break;
			default:
				break;
			}
    	};
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oobe);
        
        // Add a persistent setting to allow other apps to know the device has been provisioned.
        Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
        sharepreference = getSharedPreferences(PREFERENCE_BOX_SETTING,Context.MODE_PRIVATE);
        sw = (SystemWriteManager) mContext.getSystemService("system_write");
        
        oobe_mEthernetManager = (EthernetManager) mContext.getSystemService("ethernet");
        oobe_mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mHander = new MyHandle();
        HandlerManager.putHandler(handler);
		initNetView();

		oobe_welcome = (LinearLayout) findViewById(R.id.oobe_welcome);
		oobe_language = (LinearLayout) findViewById(R.id.oobe_language);
		oobe_time_zone = (LinearLayout) findViewById(R.id.oobe_time_zone);
		oobe_net = (LinearLayout) findViewById(R.id.oobe_net);
		oobe_screenadjust = (LinearLayout) findViewById(R.id.oobe_screenadjust);
		oobe_ota_update_setting = (LinearLayout) findViewById(R.id.oobe_ota_update_setting);

		layoutTimeZone = (LinearLayout)findViewById(R.id.fragment_time_zone);
		textViewZimeZone = (TextView)findViewById(R.id.time_zone_tip);
		editTextUpdateIp = (EditText)findViewById(R.id.update_setting_et);
		editTextUpdateIp.setOnFocusChangeListener(this);
		String serverIp = sw.getPropertyString(SYSTEM_PROP,"192.168.1.1");
		if(serverIp != null && !"".equals(serverIp)) {
			//设置IP
			editTextUpdateIp.setText(serverIp.trim());
		}
		
		top_welcome = (LinearLayout) findViewById(R.id.top_welcome);
		top_welcome.setOnClickListener(this);

		top_language = (LinearLayout) findViewById(R.id.top_language);
		top_language.setOnClickListener(this);
		
		top_time_zone = (LinearLayout) findViewById(R.id.top_time_zone);
		top_time_zone.setOnClickListener(this);
		
		top_network = (LinearLayout) findViewById(R.id.top_network);
		top_network.setOnClickListener(this);
		
		top_screenadjust = (LinearLayout) findViewById(R.id.top_screen);
		top_screenadjust.setOnClickListener(this);
		
		top_ota_update_setting = (LinearLayout) findViewById(R.id.top_ota_update_settings);
		top_ota_update_setting.setOnClickListener(this);

		button_welcome_skip = (Button) findViewById(R.id.button_welcome_skip);
		button_welcome_skip.setOnClickListener(this);
		button_welcome_next = (Button) findViewById(R.id.button_welcome_next);
		button_welcome_next.setOnClickListener(this);
		button_language_previous = (Button) findViewById(R.id.button_language_previous);
		button_language_previous.setOnClickListener(this);
		button_language_next = (Button) findViewById(R.id.button_language_next);
		button_language_next.setOnClickListener(this);
		button_time_zone_select = (Button) findViewById(R.id.button_select);
		button_time_zone_select.setOnClickListener(this);
		button_time_zone_previous = (Button) findViewById(R.id.button_time_zone_previous);
		button_time_zone_previous.setOnClickListener(this);
		button_time_zone_next = (Button) findViewById(R.id.button_time_zone_next);
		button_time_zone_next.setOnClickListener(this);
		button_net_previous = (Button) findViewById(R.id.button_net_previous);
		button_net_previous.setOnClickListener(this);
		button_net_next = (Button) findViewById(R.id.button_net_next);
		button_net_next.setOnClickListener(this);
		button_update_setting_previous = (Button) findViewById(R.id.button_update_setting_previous);
		button_update_setting_previous.setOnClickListener(this);
		button_update_setting_finish = (Button) findViewById(R.id.button_update_setting_next);
		button_update_setting_finish.setOnClickListener(this);

		button_screen_previous = (Button) findViewById(R.id.button_screen_previous);
		button_screen_previous.setOnClickListener(this);

		button_screen_next = (Button) findViewById(R.id.button_screen_next);
		button_screen_next.setOnClickListener(this);

		top_welcome_layout = (RelativeLayout) findViewById(R.id.top_welcome_layout);
		top_language_layout = (RelativeLayout) findViewById(R.id.top_language_layout);
		top_time_zone_layout = (RelativeLayout) findViewById(R.id.top_time_zone_layout);
		top_network_layout = (RelativeLayout) findViewById(R.id.top_network_layout);
		top_screen_layout = (RelativeLayout) findViewById(R.id.top_screen_layout);
		top_update_setting_layout = (RelativeLayout) findViewById(R.id.top_update_settings_layout);
		
		top_voice_search=(LinearLayout) findViewById(R.id.top_voice_search);
		button_net_next=(Button) findViewById(R.id.button_net_next);
		oobe_voice_search=(LinearLayout) findViewById(R.id.oobe_voice_search);
		button_voice_previous=(Button) findViewById(R.id.button_voice_previous);
		button_voice_finish=(Button) findViewById(R.id.button_voice_finish);
		oobe_select_google=(TextView) findViewById(R.id.oobe_select_google);
		oobe_select_xunfei=(TextView) findViewById(R.id.oobe_select_xunfei);
		oobe_select_google.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				setSelectGoogle();
			}
		});
		oobe_select_xunfei.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				setSelectXunFei();
			}
		});
		button_voice_previous.setOnClickListener(this);
		button_voice_finish.setOnClickListener(this);
		if(!SystemProperties.getBoolean("persist.sys.voice.search", false)){
			top_voice_search.setVisibility(View.GONE);
		}else{
//			button_net_finish.setVisibility(View.GONE);
//			button_net_finish.setOnClickListener(null);
			button_net_next.setVisibility(View.VISIBLE);
			button_net_next.setOnClickListener(this);
			String currentVoiceInput = Settings.Secure.getString(getContentResolver(), "voice_recognition_service");
			if (currentVoiceInput != null) {
				if (currentVoiceInput.equals(GOOGLE_VOICE_TYPE)) {
					oobe_select_google.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
					oobe_select_xunfei.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_uncheck, 0, 0, 0);
				} else if (currentVoiceInput.equals(XUNFEI_VOICE_TYPE)) {
					oobe_select_google.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_uncheck, 0, 0, 0);
					oobe_select_xunfei.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
				}
			}
		}
		openWelcomView();
		setCurrentViewSelected(R.id.oobe_welcome);

		around_line = (ImageView) findViewById(R.id.oobe_screen_adjust_line);
//        IntentFilter filter = new IntentFilter();
//		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
//		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
//		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
//        filter.addAction(WindowManagerPolicy.ACTION_HDMI_HW_PLUGGED);
//        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//        filter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
//
//        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
//        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
//        filter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
//
//		if (mNetworkStateReceiver == null)
//			mNetworkStateReceiver = new NetworkStateReceiver();
//		registerReceiver(mNetworkStateReceiver, filter);
		updateTimeZoneTip();
	}

	private Calendar mDummyDate = Calendar.getInstance();;
    private void updateTimeZoneTip() {
    	java.text.DateFormat shortDateFormat = DateFormat.getDateFormat(this);
        final Calendar now = Calendar.getInstance();
        mDummyDate.setTimeZone(now.getTimeZone());
        mDummyDate.set(now.get(Calendar.YEAR), 11, 31, 13, 0, 0);
        Date dummyDate = mDummyDate.getTime();
        textViewZimeZone.setText(getTimeZoneText(now.getTimeZone())+"");
    }
    
    private static String getTimeZoneText(TimeZone tz) {
        SimpleDateFormat sdf = new SimpleDateFormat("ZZZZ, zzzz");
        sdf.setTimeZone(tz);
        return sdf.format(new Date());
    }
    
    private boolean isEthDeviceAdded(){
        String str = Utils.readSysFile(sw,eth_device_sysfs);
        if(str == null)
        {  str = Utils.readSysFile(sw,"/sys/class/net/usbnet0/link_mode");//added by clei for detect usbnet
        	  if(str == null)
            return false ;
         }
        if (Utils.DEBUG) Log.d(TAG,"==== isEthDeviceAdded() , str="+str);
        if(str.contains("unlink")){
            if (Utils.DEBUG) Log.d(TAG,"==== isEthDeviceAdded() , false");
            return false;
        }else{
            if (Utils.DEBUG) Log.d(TAG,"==== isEthDeviceAdded() , true");
            return true;
        }    
    }

	private void initNetView() {
        oobe_eth_ip_layout  = (LinearLayout) findViewById(R.id.oobe_eth_ip_layout);
        oobe_wifi_connected = (LinearLayout) findViewById(R.id.oobe_wifi_connected);
		oobe_wifi_ssid_value = (TextView) findViewById(R.id.oobe_wifi_ssid_value);
		oobe_ip_address_value = (TextView) findViewById(R.id.oobe_ip_address_value);

        oobe_eth_connected_tip = (TextView) findViewById(R.id.oobe_eth_connected_notic);
        oobe_eth_IP_value = (TextView) findViewById(R.id.oobe_eth_IP_value);
        
		oobe_select_wifi = (TextView) findViewById(R.id.oobe_select_wifi);
		oobe_select_wifi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setWifiCheckBoxSwitch();
			}
		});

   
		oobe_select_ethernet = (TextView) findViewById(R.id.oobe_select_ethernet);
        String hasEthernet = sw.getPropertyString("hw.hasethernet" , "false");
        if(hasEthernet.equals("false")){
            TextView oobe_no_network = (TextView)findViewById(R.id.oobe_no_network);
            oobe_no_network.setText(mContext.getResources().getString(R.string.no_network_wifi_only));
            oobe_select_ethernet.setVisibility(View.INVISIBLE);
        }
		oobe_select_ethernet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setEthCheckBoxSwitch(true);
			}
		});
		oobe_select_ethernet.setNextFocusUpId(R.id.settingsTopView_01);
		oobe_wifi_connected_tip = (TextView) findViewById(R.id.oobe_wifi_connected_tip);
		oobe_root_eth_view = (LinearLayout) findViewById(R.id.oobe_root_eth_view);
        oobe_net_root_view = (LinearLayout) findViewById(R.id.oobe_net_root_view);
		oobe_root_wifi_view = (LinearLayout) findViewById(R.id.oobe_root_wifi_view);
		oobe_root_wifi_view.setVisibility(View.GONE);
		oobe_root_eth_view.setVisibility(View.VISIBLE);
	
		oobe_wifi_connected = (LinearLayout) findViewById(R.id.oobe_wifi_connected);
		oobe_wifi_input_password = (LinearLayout) findViewById(R.id.oobe_wifi_input_password);
		oobe_wifi_not_connect = (LinearLayout) findViewById(R.id.oobe_wifi_not_connect);
		oobe_wifi_slect_tip = (TextView) findViewById(R.id.oobe_wifi_slect_tip);
		oobe_password_editview = (EditText) findViewById(R.id.oobe_password_input);
		oobe_password_editview.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
        oobe_password_editview.setInputType(
                InputType.TYPE_CLASS_TEXT | (getShowPasswordState() ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));
		oobe_wifi_listview_tip = (TextView) findViewById(R.id.oobe_wifi_listview_tip);

        oobe_show_password = (TextView)findViewById(R.id.oobe_show_password);
        updateShowPasswordBoxUI();
        oobe_show_password.setOnClickListener(new OnClickListener() {
            
			@Override
			public void onClick(View v) {
                if(getShowPasswordState()){
                    setShowPasswordState(false);
                }else{
                    setShowPasswordState(true);
                }

                oobe_password_editview.setInputType(
                InputType.TYPE_CLASS_TEXT | (getShowPasswordState() ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));
			}
		});

		Button oobe_password_connect = (Button) findViewById(R.id.oobe_password_connect);
		oobe_password_connect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String password = oobe_password_editview.getText().toString();
				String currentAP = oobe_mAccessPointListAdapter.getCurrentAP().wifiSsid.toString();
                WifiUtils.setPassWord(password);                    
                WifiUtils.setApName(currentAP);
				String connectSsid = oobe_mWifiManager.getConnectionInfo().getWifiSsid().toString();
				ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (password != null) {
					if (currentAP.equals(connectSsid) && wifi.isConnected()){
                        showWifiConnectedView();
                    }else {
                        if(oobe_mAccessPointListAdapter.getCurrentAPSecurityType()== SECURITY_WPA){
                            if(password.length()<8 || password.length() >63){
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.password_length_error),3000).show();
                                return ;
                            }
                        }
                        if (Utils.DEBUG) Log.d(TAG,"====== connect now!");
						showConnectingView();                    
						oobe_mAccessPointListAdapter.connect2AccessPoint(null,password);
                        startTime = System.currentTimeMillis();
					}
				} else {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.passwork_input_notice),3000).show();
				}

			}
		});

		oobe_mAcessPointListView = (ListView) findViewById(R.id.oobe_wifiListView);
		//oobe_mAcessPointListView.setNextFocusRightId(R.id.button_net_finish);
		oobe_mAcessPointListView.setOnItemClickListener(this);
		oobe_mAccessPointListAdapter = new AccessPointListAdapter(this);
		oobe_mAcessPointListView.setAdapter(oobe_mAccessPointListAdapter);             
	}

    private void wifiResume(){
        if (Utils.DEBUG) Log.d(TAG,"===== wifiResume()");
        if(getEthCheckBoxState()){ 
             if (Utils.DEBUG) Log.d(TAG,"===== wifiResume(),ethernet connect");
            if(isEthDeviceAdded()) {
               // oobe_mWifiManager.setWifiEnabled(false);removed by clei for wifi and eth
                oobe_mEthernetManager.setEthEnabled(true);
                updateNetWorkUI(2);
            }else{
                //oobe_mEthernetManager.setEthEnabled(false); 
                if(getWifiCheckBoxState()){
                    oobe_mWifiManager.setWifiEnabled(true);
                    wifiScan(); 
                    updateNetWorkUI(1);
                }
                 else {
                    updateNetWorkUI(2);
                }
            }
        }else{
            if(getWifiCheckBoxState()){
                if (Utils.DEBUG) Log.d(TAG,"===== wifiResume(),wifi connect");
                oobe_mWifiManager.setWifiEnabled(true);
                wifiScan(); 
                updateNetWorkUI(1);
            }else{
                if (Utils.DEBUG) Log.d(TAG,"===== wifiResume(),wifi and ethernt  disconnect");
                updateNetWorkUI(0);
            }                
        }               
        updateEthCheckBoxUI();
        upDateWifiCheckBoxUI();

    }

    
    private void setEthCheckBoxSwitch(boolean openEthernet){
        if(openEthernet){           
            if(getEthCheckBoxState()){            
                enableEthernetView(false);
                mHander.removeMessages(UPDATE_ETH_STATUS);

            }else{
                enableEthernetView(true);
                Message msg = mHander.obtainMessage();
                msg.what = UPDATE_ETH_STATUS;
                mHander.sendMessageDelayed(msg,9000);
            } 
        }else{
            oobe_mEthernetManager.setEthEnabled(false); 
            Toast.makeText(mContext, mContext.getResources().getString(R.string.ethernet_inplug_notice), 4000).show(); 
            if(!getWifiCheckBoxState())
                updateNetWorkUI(0);
        }
        
        updateEthCheckBoxUI();   
        upDateWifiCheckBoxUI();
    }
    
    private void enableEthernetView(boolean able){                     
        if(able){ 
            updateNetWorkUI(2);
            //oobe_mWifiManager.setWifiEnabled(false);removed by clei for wifi and eth
            oobe_eth_connected_tip.setText(R.string.ethernet_connectting);
            oobe_mEthernetManager.setEthEnabled(true);   
        }else{
        	if(!getWifiCheckBoxState())//removed by clei for wifi 
              updateNetWorkUI(0);
           else{
             updateNetWorkUI(1);
             if(!WifiUtils.isWifiConnected(mContext))
                showConnectingView();
             oobe_mAcessPointListView.setVisibility(View.VISIBLE);
           }
            oobe_mEthernetManager.setEthEnabled(false);
        }       
    }
        
        private void enableWifiView(boolean able){
            if(able){ 
                //if(isEthDeviceAdded()){
                    //oobe_mEthernetManager.setEthEnabled(false); removed by clei for wifi and eth
                //}  
                mHander.removeMessages(UPDATE_ETH_STATUS);
                oobe_mAcessPointListView.setVisibility(View.GONE);
                oobe_wifi_listview_tip.setVisibility(View.VISIBLE);
                oobe_mWifiManager.setWifiEnabled(true);
                wifiScan(); 
                updateNetWorkUI(1);
            }else{          
                oobe_mWifiManager.setWifiEnabled(false);
                //add if ethnet if open
                if(getEthCheckBoxState()) {
                	updateNetWorkUI(2);
                }else {
                	updateNetWorkUI(0);
                }
            }      
        }
        
        private void updateEthCheckBoxUI(){
            if(getEthCheckBoxState()){
                oobe_select_ethernet.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
            }else{
                oobe_select_ethernet.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_uncheck, 0, 0, 0);
            }
        }
        
        /**
         * wifi switch
         */
        private void setWifiCheckBoxSwitch(){
            if(getWifiCheckBoxState()){
                enableWifiView(false);             
            }else{
                enableWifiView(true);
            }
            upDateWifiCheckBoxUI();
           oobe_select_wifi.setEnabled(false);//clei for wifi
            updateEthCheckBoxUI();
        }
        
        private void setSelectGoogle(){
        	String currentVoiceInput=Settings.Secure.getString(getContentResolver(), "voice_recognition_service");
        	if(currentVoiceInput!=null&&currentVoiceInput.equals(GOOGLE_VOICE_TYPE)){
        		return ;
        	}
        	Settings.Secure.putString(getContentResolver(), "voice_recognition_service", GOOGLE_VOICE_TYPE);
        	oobe_select_google.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
        	oobe_select_xunfei.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_uncheck,0,0,0);
        	
        }
        private void setSelectXunFei(){
        	String currentVoiceInput=Settings.Secure.getString(getContentResolver(), "voice_recognition_service");
        	if(currentVoiceInput!=null&&currentVoiceInput.equals(XUNFEI_VOICE_TYPE)){
        		return ;
        	}
        	Settings.Secure.putString(getContentResolver(), "voice_recognition_service", XUNFEI_VOICE_TYPE);
        	oobe_select_xunfei.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
        	oobe_select_google.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_uncheck,0,0,0);
        	
        }
        private void upDateWifiCheckBoxUI(){
            if(getWifiCheckBoxState()){
                oobe_select_wifi.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
            }else{
                oobe_select_wifi.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_uncheck, 0, 0, 0);
            }
          oobe_select_wifi.setEnabled(true);//clei for wifi
        }
        private void updateNetWorkUI(int type){
            if (Utils.DEBUG) Log.d(TAG,"===== updateNetWorkUI() 001");
            if(type == 0){
                if (Utils.DEBUG) Log.d(TAG,"===== updateNetWorkUI() 002");
                oobe_net_root_view.setVisibility(View.VISIBLE);
                oobe_root_eth_view.setVisibility(View.GONE);
                oobe_root_wifi_view.setVisibility(View.GONE);
            }else if(type == 1){
                if (Utils.DEBUG) Log.d(TAG,"===== updateNetWorkUI() 003");
                oobe_net_root_view.setVisibility(View.GONE);
                oobe_root_eth_view.setVisibility(View.GONE);
                oobe_root_wifi_view.setVisibility(View.VISIBLE);

                if(oobe_mWifiManager.isWifiEnabled()){
                    showWifiConnectedView();
                }else{
                    showWifiDisconnectedView();
                }            
            }else if(type == 2){
                if (Utils.DEBUG) Log.d(TAG,"===== updateNetWorkUI() 004");
                oobe_net_root_view.setVisibility(View.GONE);
                oobe_root_eth_view.setVisibility(View.VISIBLE);
                oobe_root_wifi_view.setVisibility(View.GONE);
                upDateEthernetInfo();
            }else{
                if (Utils.DEBUG) Log.d(TAG,"===== updateNetWorkUI() 005");
                oobe_net_root_view.setVisibility(View.VISIBLE);
                oobe_root_eth_view.setVisibility(View.GONE);
                oobe_root_wifi_view.setVisibility(View.GONE);
            }
    
        }
        private void wifiScan(){  
            oobe_mAccessPointListAdapter.startScanApcessPoint(); 
        }
        private void showWifiConnectedView() {   
            oobe_wifi_listview_tip.setVisibility(View.GONE);
            oobe_mAcessPointListView.setVisibility(View.VISIBLE);
            oobe_mAccessPointListAdapter.updateAccesspointList();

            boolean isWifiConnected = WifiUtils.isWifiConnected(mContext);
            if(isWifiConnected){
                oobe_wifi_input_password.setVisibility(View.GONE);
                oobe_wifi_not_connect.setVisibility(View.GONE);
                oobe_wifi_connected.setVisibility(View.VISIBLE);
        
                DhcpInfo mDhcpInfo = oobe_mWifiManager.getDhcpInfo();
                WifiInfo mWifiinfo = oobe_mWifiManager.getConnectionInfo();
        
                if (mWifiinfo != null) {
                    oobe_wifi_ssid_value.setVisibility(View.VISIBLE);
                    String wifi_name = mWifiinfo.getSSID().substring(1,mWifiinfo.getSSID().length() - 1);
                    oobe_wifi_ssid_value.setText(wifi_name);
                    oobe_ip_address_value.setText(int2ip(mWifiinfo.getIpAddress()));
                    //oobe_mAccessPointListAdapter.setCurrentConnectedItemBySsid(mWifiinfo.getSSID());
                    oobe_mAccessPointListAdapter.setCurrentConnectItemSSID(mWifiinfo.getSSID());
                }
            }
        }
        

	void openWelcomView() {
        mCuttentView = OOBE_WELCOME;
		oobe_net.setVisibility(View.GONE);
		oobe_screenadjust.setVisibility(View.GONE);
		oobe_language.setVisibility(View.GONE);
		oobe_time_zone.setVisibility(View.GONE);
		oobe_ota_update_setting.setVisibility(View.GONE);
		oobe_welcome.setVisibility(View.VISIBLE);
		button_welcome_next.setFocusableInTouchMode(true);
		button_welcome_next.requestFocus();
		isDisplayView = false;

	}
	LangeAdapter langeAdapter;
	private void initLangeGrd(){
		langGrd = (GridView) findViewById(R.id.lang_grd);
		langeAdapter = new LangeAdapter(this);
		langGrd.setAdapter(langeAdapter);
		
		String lan = Locale.getDefault().getLanguage().toUpperCase();
	    String coun = Locale.getDefault().getCountry();
	    Log.i(TAG, "lang="+lan+";country="+coun);
	    String[] langStr = getResources().getStringArray(R.array.default_lang_acronym);
	    for (int i = 0; i < langStr.length; i++) {
			if(lan.equals(langStr[i].toUpperCase())){
				langeAdapter.setLangSelected(i);
				langGrd.setSelection(i);
				langeAdapter.notifyDataSetChanged();
				break;
			}
		}
	    
	    langGrd.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if(arg1){
					langeAdapter.setSelected(langGrd.getSelectedItemPosition());
				}else{
					langeAdapter.setSelected(-1);
				}
				langeAdapter.notifyDataSetChanged();
			}
		});
	    langGrd.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				langeAdapter.setSelected(arg2);
				langeAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	    langGrd.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				isGotoLanguageView = true ;
				langeAdapter.setSelected(arg2);
				langeAdapter.notifyDataSetChanged();
				switch (arg2) {
				case 0:
					updateLanguage(new Locale("en","US"));
					break;
				case 1:
					updateLanguage(new Locale("fr","FR"));
					break;	
				case 2:
					updateLanguage(new Locale("de","DE"));
					break;
				case 3:
					updateLanguage(new Locale("it","IT"));
					break;
				case 4:
					updateLanguage(new Locale("es","ES"));
					break;
				case 5:
					updateLanguage(new Locale("pt","PT"));
					break;	
				case 6:
					updateLanguage(new Locale("nl","NL"));
					break;
				case 7:
					updateLanguage(new Locale("sv","SE"));
					break;
				case 8:
					updateLanguage(new Locale("nb","NO"));
					break;
				case 9:
					updateLanguage(new Locale("da","DK"));
					break;
				case 10:
					updateLanguage(new Locale("pl","PL"));
					break;
				case 11:
					updateLanguage(new Locale("hu","HU"));
					break;
				case 12:
					updateLanguage(new Locale("ro","RO"));
					break;
				case 13:
					updateLanguage(new Locale("cs","CZ"));
					break;
				case 14:
					updateLanguage(new Locale("el","GR"));
					break;
				case 15:
					updateLanguage(new Locale("hr","HR"));
					break;
				
				default:
					break;
				}
				  
			}
		});
	}

	void openLanguageView() {
        mCuttentView = OOBE_LANGUAGE;
		oobe_welcome.setVisibility(View.GONE);
		oobe_time_zone.setVisibility(View.GONE);
		oobe_net.setVisibility(View.GONE);
		oobe_screenadjust.setVisibility(View.GONE);
		oobe_ota_update_setting.setVisibility(View.GONE);
		oobe_language.setVisibility(View.VISIBLE);

		initLangeGrd();
		
        button_language_next.requestFocus();
		isDisplayView = false;
	}

    private void selectLanguageByID(int id , boolean hasFocus){/*
        if (Utils.DEBUG) Log.d(TAG,"===== selectLanguageByID() , hasFocus :" + hasFocus + " , mCurrentLanguage :" +mCurrentLanguage );
        if(id == R.id.select_cn){
            if (Utils.DEBUG) Log.d(TAG,"===== select_cn,hasFocus: " + hasFocus );
            if(mCurrentLanguage == 0){
                if(hasFocus){
                    select_cn.setBackgroundResource(R.drawable.language_selected);
                }else{
                    select_cn.setBackgroundResource(R.drawable.language_current);
                }
            }else if(mCurrentLanguage == 1){
                if(hasFocus){
                    select_cn.setBackgroundResource(R.drawable.language_seclect_focused);
                }else{
                    select_cn.setBackgroundResource(Color.TRANSPARENT);
                }
            }else if(mCurrentLanguage == 2){
                if(hasFocus){
                    select_cn.setBackgroundResource(R.drawable.language_seclect_focused);
                }else{
                    select_cn.setBackgroundResource(Color.TRANSPARENT);
                }
            }
            
        }else if(id == R.id.select_english){
            if (Utils.DEBUG) Log.d(TAG,"===== select_english: " + hasFocus );
            if(mCurrentLanguage == 0){
                if(hasFocus){
                    select_english.setBackgroundResource(R.drawable.language_seclect_focused);
                }else{
                     select_english.setBackgroundResource(Color.TRANSPARENT);
                }
            }else if(mCurrentLanguage == 1){
                if(hasFocus){
                    select_english.setBackgroundResource(R.drawable.language_selected);
                }else{
                    select_english.setBackgroundResource(R.drawable.language_current);
                }
            }else if(mCurrentLanguage == 2){
                if(hasFocus){
                    select_english.setBackgroundResource(R.drawable.language_seclect_focused);
                }else{
                    select_english.setBackgroundResource(Color.TRANSPARENT);
                }
            }
                      
        }else if(id == R.id.select_tw){
            if (Utils.DEBUG) Log.d(TAG,"===== select_tw : " + hasFocus );
            if(mCurrentLanguage == 0){
                if(hasFocus){
                    select_tw.setBackgroundResource(R.drawable.language_seclect_focused);
                }else{
                    select_tw.setBackgroundResource(Color.TRANSPARENT);
                }
            }else if(mCurrentLanguage == 1){
                if(hasFocus){
                    select_tw.setBackgroundResource(R.drawable.language_seclect_focused);
                }else{
                    select_tw.setBackgroundResource(Color.TRANSPARENT);
                }
            }else if(mCurrentLanguage == 2){
                if(hasFocus){
                    select_tw.setBackgroundResource(R.drawable.language_selected);
                }else{
                    select_tw.setBackgroundResource(R.drawable.language_current);
                }
            }
           
        }
    */}

    private void updateLanguage(Locale locale) {
		try {
			//Utils.shadowScreen(sw, null);
			Object objIActMag;
			Class clzIActMag = Class.forName("android.app.IActivityManager");
			Class clzActMagNative = Class.forName("android.app.ActivityManagerNative");
			Method mtdActMagNative$getDefault = clzActMagNative.getDeclaredMethod("getDefault");
			objIActMag = mtdActMagNative$getDefault.invoke(clzActMagNative);
			Method mtdIActMag$getConfiguration = clzIActMag.getDeclaredMethod("getConfiguration");
			Configuration config = (Configuration) mtdIActMag$getConfiguration.invoke(objIActMag);
			config.locale = locale;
			Class[] clzParams = { Configuration.class };
			Method mtdIActMag$updateConfiguration = clzIActMag.getDeclaredMethod("updateConfiguration", clzParams);
			mtdIActMag$updateConfiguration.invoke(objIActMag, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    void openTimeZoneView() {
    	isDisplayView = false;
    	mCuttentView = OOBE_TIME_ZONE ;
		oobe_net.setVisibility(View.GONE);
		oobe_language.setVisibility(View.GONE);
		oobe_welcome.setVisibility(View.GONE);
		oobe_screenadjust.setVisibility(View.GONE);
		oobe_ota_update_setting.setVisibility(View.GONE);
		oobe_time_zone.setVisibility(View.VISIBLE);

		button_time_zone_next.requestFocus();
    }

	void openScreenAdjustView() {
        mCuttentView = OOBE_SCREEN_ADJUST ;
		oobe_net.setVisibility(View.GONE);
		oobe_language.setVisibility(View.GONE);
		oobe_time_zone.setVisibility(View.GONE);
		oobe_welcome.setVisibility(View.GONE);
		oobe_ota_update_setting.setVisibility(View.GONE);
		oobe_screenadjust.setVisibility(View.VISIBLE);

		button_screen_next.requestFocus();

		openScreenAdjustLayout();
		isDisplayView = true;
	}

	void openNetView() {
        mCuttentView = OOBE_NETWORK;
		isDisplayView = false;
		oobe_welcome.setVisibility(View.GONE);
		oobe_language.setVisibility(View.GONE);
		oobe_time_zone.setVisibility(View.GONE);
		oobe_screenadjust.setVisibility(View.GONE);
		oobe_ota_update_setting.setVisibility(View.GONE);
		oobe_net.setVisibility(View.VISIBLE);
		button_net_next.requestFocus();
	}
	
	void openOtaUpdateSetting() {
		mCuttentView = OOBE_OTA_UPDATE_STTINT;
		isDisplayView = false;
		oobe_welcome.setVisibility(View.GONE);
		oobe_language.setVisibility(View.GONE);
		oobe_time_zone.setVisibility(View.GONE);
		oobe_screenadjust.setVisibility(View.GONE);
		oobe_net.setVisibility(View.GONE);
		oobe_ota_update_setting.setVisibility(View.VISIBLE);
		button_update_setting_finish.requestFocus();
	}
	
	void openVoiceSearchView(){
		mCuttentView = OOBE_VOICE_SEARCH;
		oobe_net.setVisibility(View.GONE);
		oobe_voice_search.setVisibility(View.VISIBLE);
	}
	void closeVoiceSearchView(){
		oobe_voice_search.setVisibility(View.GONE);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.oobe, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (Utils.DEBUG) Log.d(TAG, "onItemClick(), position=" + position);
		if (parent instanceof GridView) {
            //isGotoLanguageView = true;
			//mlanguageAdapter.updateLanguageLocal(position);
		} else if (parent instanceof ListView) {
			onClickAccessPoint(position);
		}

	}

	private void openScreenAdjustLayout() {

		img_num_hundred = (ImageView) findViewById(R.id.oobe_img_num_hundred);
		img_num_ten = (ImageView) findViewById(R.id.oobe_img_num_ten);
		img_num_unit = (ImageView) findViewById(R.id.oobe_img_num_unit);
		img_progress_bg = (ImageView) findViewById(R.id.oobe_img_progress_bg);

		btn_position_zoom_out = (ImageButton) findViewById(R.id.oobe_btn_position_zoom_out);
        btn_position_zoom_out.setOnClickListener(this);
		btn_position_zoom_in = (ImageButton) findViewById(R.id.oobe_btn_position_zoom_in);
        btn_position_zoom_in.setOnClickListener(this);
		mScreenPositionManager = new ScreenPositionManager(this);
		mScreenPositionManager.initPostion();
		screen_rate = mScreenPositionManager.getRateValue();

		showProgressUI(0);

		//around_line.setVisibility(View.VISIBLE);

	}

	private void showProgressUI(int step) {
        screen_rate = screen_rate + step;
        if(screen_rate >MAX_Height){
            screen_rate = MAX_Height;
        }
        if(screen_rate <MIN_Height){
            screen_rate = MIN_Height ;
        }

		if (screen_rate <= MAX_Height && screen_rate >=100) {
			int hundred = Num[(int) screen_rate / 100];
			img_num_hundred.setVisibility(View.VISIBLE);
			img_num_hundred.setBackgroundResource(hundred);
            int ten = Num[(screen_rate -100)/10] ;
			img_num_ten.setBackgroundResource(ten);
            int unit = Num[(screen_rate -100)%10];
			img_num_unit.setBackgroundResource(unit);
			if (screen_rate - MIN_Height>= 0 && screen_rate - MIN_Height <= 19)
				img_progress_bg.setBackgroundResource(progressNum[screen_rate - MIN_Height]);
		} else if (screen_rate >= 10 && screen_rate <= 99) {
			img_num_hundred.setVisibility(View.GONE);
			int ten = Num[(int) (screen_rate / 10)];
			int unit = Num[(int) (screen_rate % 10)];
			img_num_ten.setBackgroundResource(ten);
			img_num_unit.setBackgroundResource(unit);
			if (screen_rate - MIN_Height >= 0 && screen_rate - MIN_Height <= 19)
				img_progress_bg.setBackgroundResource(progressNum[screen_rate - MIN_Height]);
		} else if (screen_rate >= 0 && screen_rate <= 9) {
			int unit = Num[screen_rate];
			img_num_unit.setBackgroundResource(unit);
		}

	}

	private void closeScreenAdjustLayout() {
        /*
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"===== save position now");
                    mScreenPositionManager.savePostion();
                }
            });
            t.start();
            */
        mScreenPositionManager.savePostion();
        around_line.setVisibility(View.GONE);
        ScreenPositionManager.mIsOriginWinSet = false;    //user has changed&save postion,reset this prop to default 
	}

	private void onClickAccessPoint(int index) {

		oobe_mAccessPointListAdapter.setCurrentSelectItem(index);
		int securityType = oobe_mAccessPointListAdapter
				.getCurrentAccessPointSecurityType(index);
		if (Utils.DEBUG) Log.d(TAG, "===== securityType :  " + securityType);

		if (securityType == 0 || securityType == 4) {

			showConnectingView();

			oobe_mAccessPointListAdapter.connect2OpenAccessPoint();
		} else {
		    String currentApName =WifiUtils.removeDoubleQuotes( oobe_mAccessPointListAdapter.getCurrentAP().SSID);
            String mWifiConnectedInfo = sharepreference.getString("wifi_connected_info", "***");
            //Log.d(TAG, "===== mWifiConnectedInfo :  " + mWifiConnectedInfo);
            oobe_password_editview.setText("");
            if(mWifiConnectedInfo.contains(currentApName)){
                String[] values = mWifiConnectedInfo.split(",");
                        for (int i = 0; i < values.length; i++) {
                            if (values[i].contains(currentApName)) {
                                String[] temp = values[i].split(":");
                                String apPassword = temp[1];
                                oobe_password_editview.setText(apPassword);
                                //Log.d(TAG, "===== apPassword :  " + apPassword);
                                break;
                            } 
                        }
            }
			showPasswordView();
		}

	}

	private void showPasswordView() {
		oobe_wifi_connected.setVisibility(View.GONE);
		oobe_wifi_not_connect.setVisibility(View.GONE);
		oobe_wifi_input_password.setVisibility(View.VISIBLE);
		oobe_password_editview.requestFocus();

	}

	private void showConnectingView() {
		oobe_wifi_connected.setVisibility(View.GONE);
		oobe_wifi_input_password.setVisibility(View.GONE);
		oobe_wifi_not_connect.setVisibility(View.VISIBLE);
		oobe_wifi_slect_tip.setText(R.string.wifi_connectting);
		//button_net_finish.requestFocus();
	}


    private void upDateEthernetInfo() {
		if (Utils.DEBUG) Log.d(TAG, "===== update ethernet info ");
        boolean isEthConnected = WifiUtils.isEthConnected(mContext);
		if (isEthConnected) {
            if(oobe_eth_ip_layout != null && oobe_wifi_connected != null){
                oobe_eth_ip_layout.setVisibility(View.VISIBLE);
                oobe_wifi_connected.setVisibility(View.VISIBLE);
            }
			   
			DhcpInfo mDhcpInfo = oobe_mEthernetManager.getDhcpInfo();
			if (mDhcpInfo != null) {
				int ip = mDhcpInfo.ipAddress;
                if(oobe_eth_connected_tip != null)
				    oobe_eth_connected_tip.setText(R.string.eth_connectd);
				if (Utils.DEBUG) Log.d(TAG, "====== ip  : " + ip + "   int2ip(ip) : "+ int2ip(ip));
				if(oobe_eth_IP_value != null)
				    oobe_eth_IP_value.setText(int2ip(ip));
				else {
					Log.d(TAG,"=====  eth_IP_value is null !!!");
				}	
			}

		} else {
		    if(oobe_eth_connected_tip != null && oobe_eth_ip_layout != null){
			    oobe_eth_connected_tip.setText(R.string.ethernet_error);
			    oobe_eth_ip_layout.setVisibility(View.GONE);
            }
		}

	}


	private void showWifiDisconnectedView() {
        oobe_mAcessPointListView.setVisibility(View.GONE);
		oobe_wifi_connected.setVisibility(View.GONE);
		oobe_wifi_input_password.setVisibility(View.GONE);
		oobe_wifi_not_connect.setVisibility(View.VISIBLE);
		oobe_mAccessPointListAdapter.updateAccesspointList();
		oobe_wifi_slect_tip.setText(R.string.wifi_ap_select);
	}

	public String int2ip(long ipInt) {
		StringBuilder sb = new StringBuilder();
		sb.append(ipInt & 0xFF).append(".");
		sb.append((ipInt >> 8) & 0xFF).append(".");
		sb.append((ipInt >> 16) & 0xFF).append(".");
		sb.append((ipInt >> 24) & 0xFF);
		return sb.toString();
	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(TAG, ex.toString());
		}
		return null;
	}

	@Override
	public void onResume() {
	    super.onResume();
	    if (Utils.DEBUG) Log.d(TAG,"===== isGotoLanguageView : " + isGotoLanguageView);
        wifiResume();
	    if(isGotoLanguageView){
            openLanguageView();
            isGotoLanguageView = false;
        }
        if(isNeedShowDialog())
		    showConfirmDialog();

        isFirstStartActivity = false;
        IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        filter.addAction(WindowManagerPolicy.ACTION_HDMI_HW_PLUGGED);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);

        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);

		if (mNetworkStateReceiver == null)
			mNetworkStateReceiver = new NetworkStateReceiver();
		registerReceiver(mNetworkStateReceiver, filter);
	}

    private boolean isNeedShowDialog(){
        OutPutModeManager mOutPutModeManager = new OutPutModeManager(this);
        String defaultMode = mOutPutModeManager.getBestMatchResolution();
        String currentMode = sw.readSysfs(DISPLAY_MODE_SYSFS);
        if(defaultMode.equals(currentMode) || defaultMode.contains("cvbs") || !isFirstStartActivity){
            return false;
        }else{
            return true ;
        }
    } 

    private void showConfirmDialog(){
        if (Utils.DEBUG) Log.d(TAG,"===== showConfirmDialog()");
        String mode =  sw.readSysfs(DISPLAY_MODE_SYSFS);
        if(mode.contains("cvbs")){
            if (Utils.DEBUG) Log.d(TAG,"===== start with cvbs mode,don't show dialog");
            return ;
        }
        if(dialog == null)
            dialog = new OobeDisplayConfirmDialog(this,false,null);
        dialog.show();
    }

    private void dismissDisplayDiglog(){
        if (Utils.DEBUG) Log.d(TAG,"===== dismissDisplayDiglog()");
        if(dialog!=null){
            dialog.dismissAndStop();
            dialog = null;
        }

    }

	@Override
	public void onPause() {
		super.onPause();
		if (mNetworkStateReceiver != null) {
			unregisterReceiver(mNetworkStateReceiver);
			mNetworkStateReceiver = null;
		}
	}

	@Override
	public void onStop() {
		super.onStop();
        dismissDisplayDiglog();
//		if (mNetworkStateReceiver != null) {
//			unregisterReceiver(mNetworkStateReceiver);
//			mNetworkStateReceiver = null;
//		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
        if (Utils.DEBUG) Log.d(TAG,"===== onClick(), id = " + id);

		if (v instanceof Button) {
			if (id == R.id.button_welcome_skip) {
				OobeActivity.this.finish();
			} else if (id == R.id.button_welcome_next) {
				setCurrentViewSelected(R.id.top_language);
				openLanguageView();
			} else if (id == R.id.button_language_previous) {
				setCurrentViewSelected(R.id.top_welcome);
				openWelcomView();
			} else if (id == R.id.button_language_next) {
				setCurrentViewSelected(R.id.top_time_zone);
				openTimeZoneView();
			} else if(id == R.id.button_time_zone_previous) {
				setCurrentViewSelected(R.id.top_language);
				openLanguageView();
			} else if(id == R.id.button_time_zone_next) {
				setCurrentViewSelected(R.id.top_screen);
				openScreenAdjustView();
			} else if(id == R.id.button_select) {
				//select time zone
				oobe_time_zone.setVisibility(View.GONE);
				layoutTimeZone.setVisibility(View.VISIBLE);
				
			} else if (id == R.id.button_net_previous) {
				setCurrentViewSelected(R.id.top_screen);
				openScreenAdjustView();
			} else if (id == R.id.button_screen_next) {
				setCurrentViewSelected(R.id.top_network);
				around_line.setVisibility(View.GONE);
				closeScreenAdjustLayout();
				openNetView();
                wifiResume();
			} else if (id == R.id.button_screen_previous) {
			    closeScreenAdjustLayout();
				setCurrentViewSelected(R.id.top_time_zone);
				around_line.setVisibility(View.GONE);
				openTimeZoneView();
			} else if (id == R.id.button_net_finish) {
                if (Utils.DEBUG) Log.d(TAG,"===== disable oobe setting !!!");/* removed by clei
                PackageManager pm = getPackageManager();
                ComponentName name = new ComponentName(this, OobeActivity.class);
                pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
                */
                setSharedPrefrences("oobe_mode",false);
                finish();
			}else if(id==R.id.button_net_next){
				setCurrentViewSelected(R.id.top_ota_update_settings);
				openOtaUpdateSetting();
			} else if(id == R.id.button_update_setting_previous) {
				setCurrentViewSelected(R.id.top_network);
				openNetView();
			} else if(id == R.id.button_update_setting_next) {
				//finish
				String ipStr = editTextUpdateIp.getText().toString().trim();
				if(ipStr != null && !"".equals(ipStr)) {
					if(isIpAddress(ipStr)) {
						//设置IP到系统属性中
						sw.setProperty(SYSTEM_PROP,ipStr);
						setSharedPrefrences("oobe_mode",false);
		                finish();
					}
				}else {
					//ip error
					Toast.makeText(mContext, R.string.eth_settings_error, Toast.LENGTH_LONG).show();
				}
				
			} else if(id==R.id.button_voice_previous){
				closeVoiceSearchView();
				setCurrentViewSelected(R.id.top_network);
				openNetView();
                wifiResume();
			}else if(id==R.id.button_voice_finish){
				setSharedPrefrences("oobe_mode",false);
                finish();
			}
		}

        if (v instanceof ImageButton) {
            if (id == R.id.oobe_btn_position_zoom_in) {
                if (screen_rate > MIN_Height) {
                    showProgressUI(-1);
                    //mScreenPositionManager.zoomOut();
                    mScreenPositionManager.zoomByPercent(screen_rate);
                }               
            }else if(id == R.id.oobe_btn_position_zoom_out){
                if(screen_rate < MAX_Height){
                    showProgressUI(1);
                    //mScreenPositionManager.zoomIn();
                    mScreenPositionManager.zoomByPercent(screen_rate);
                }
            }
        }
	}
	
	   private boolean isIpAddress(String value) {
	        int start = 0;
	        int end = value.indexOf('.');
	        int numBlocks = 0;

	        while (start < value.length()) {
	            if (end == -1) {
	                end = value.length();
	            }

	            try {
	                int block = Integer.parseInt(value.substring(start, end));
	                if ((block > 255) || (block < 0)) {
	                        return false;
	                }
	            } catch (NumberFormatException e) {
	                    return false;
	            }

	            numBlocks++;

	            start = end + 1;
	            end = value.indexOf('.', start);
	        }
	        return numBlocks == 4;
	    }

	class NetworkStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
            Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,Context.MODE_PRIVATE).edit();
			Log.e(TAG, "action : " + action);
            if(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action)){
                    Log.e(TAG, "CONFIGURED_NETWORKS_CHANGED_ACTION");
                    Bundle b =	intent.getExtras();
                    WifiConfiguration reason = (WifiConfiguration) b.get("wifiConfiguration");
                    if(reason!=null){
                         int result =  reason.disableReason ;
                         if(result == 3){
                            Log.e(TAG, "connect error");
                            endTime = System.currentTimeMillis();
                            if(endTime - startTime > 10000){
                                oobe_wifi_slect_tip.setText(R.string.connect_error_tips);
                                Log.e(TAG, "show connect error notices");
                            }      
                         }
                    }
                  }if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) { 
                       int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                                   WifiManager.WIFI_STATE_UNKNOWN);
                   if( WifiManager.WIFI_STATE_DISABLED == state)
                      oobe_select_wifi.setEnabled(true);//clei for wifi
              
            }else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action) ||
                WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action) ||
                WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {
                	oobe_select_wifi.setEnabled(true);//clei for wifi
                oobe_mAccessPointListAdapter.updateAccesspointList();
                if (oobe_mAccessPointListAdapter.getCount() <= 0) {
                    oobe_mAcessPointListView.setVisibility(View.GONE);
                    oobe_wifi_listview_tip.setVisibility(View.VISIBLE);
                } else {
                    oobe_wifi_listview_tip.setVisibility(View.GONE);
                    oobe_mAcessPointListView.setVisibility(View.VISIBLE);
                }

            }else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {  
                        boolean isWifiConnected = WifiUtils.isWifiConnected(mContext);
                        boolean isEthConnected = WifiUtils.isEthConnected(mContext);
                        if (Utils.DEBUG) Log.e(TAG, "===== onReceive() 002");
                    if(isEthConnected){
                        if (Utils.DEBUG) Log.e(TAG, "===== onReceive() 003");
                        updateNetWorkUI(2);
                        updateEthCheckBoxUI();   
                        upDateWifiCheckBoxUI();
                    }else if(oobe_mWifiManager.isWifiEnabled()){
                        updateNetWorkUI(1);
                        if (Utils.DEBUG) Log.e(TAG, "===== onReceive() 004");
                    }else if(!getWifiCheckBoxState() && ! getEthCheckBoxState()){
                         updateNetWorkUI(0);
                         oobe_mEthernetManager.setEthEnabled(false); 
                         updateEthCheckBoxUI();   
                         upDateWifiCheckBoxUI();;
                    }
                if (Utils.DEBUG) Log.e(TAG, "===== onReceive() 005");
            }
		}
	}

	private void setCurrentViewSelected(int id) {
        
		if (id == R.id.top_welcome) {
            top_welcome_layout.setScaleX(1.2f);
            top_welcome_layout.setScaleY(1.2f);
            top_language_layout.setScaleX(1f);
            top_language_layout.setScaleY(1f);
            top_time_zone_layout.setScaleX(1f);
            top_time_zone_layout.setScaleY(1f);
            top_network_layout.setScaleX(1f);
            top_network_layout.setScaleY(1f);
            top_screen_layout.setScaleX(1f);
            top_screen_layout.setScaleY(1f);
            top_update_setting_layout.setScaleX(1f);
            top_update_setting_layout.setScaleY(1f);
		} else if (id == R.id.top_language) {
            top_welcome_layout.setScaleX(1f);
            top_welcome_layout.setScaleY(1f);
            top_language_layout.setScaleX(1.2f);
            top_language_layout.setScaleY(1.2f);
            top_time_zone_layout.setScaleX(1f);
            top_time_zone_layout.setScaleY(1f);
            top_network_layout.setScaleX(1f);
            top_network_layout.setScaleY(1f);
            top_screen_layout.setScaleX(1f);
            top_screen_layout.setScaleY(1f);
            top_update_setting_layout.setScaleX(1f);
            top_update_setting_layout.setScaleY(1f);
			
		} else if (id == R.id.top_time_zone) {
            top_welcome_layout.setScaleX(1f);
            top_welcome_layout.setScaleY(1f);
            top_language_layout.setScaleX(1f);
            top_language_layout.setScaleY(1f);
            top_time_zone_layout.setScaleX(1.2f);
            top_time_zone_layout.setScaleY(1.2f);
            top_network_layout.setScaleX(1f);
            top_network_layout.setScaleY(1f);
            top_screen_layout.setScaleX(1f);
            top_screen_layout.setScaleY(1f);
            top_voice_search.setScaleX(1f);
            top_voice_search.setScaleY(1f);
            top_update_setting_layout.setScaleX(1f);
            top_update_setting_layout.setScaleY(1f);

		}else if (id == R.id.top_network) {
            top_welcome_layout.setScaleX(1f);
            top_welcome_layout.setScaleY(1f);
            top_language_layout.setScaleX(1f);
            top_language_layout.setScaleY(1f);
            top_time_zone_layout.setScaleX(1f);
            top_time_zone_layout.setScaleY(1f);
            top_network_layout.setScaleX(1.2f);
            top_network_layout.setScaleY(1.2f);
            top_screen_layout.setScaleX(1f);
            top_screen_layout.setScaleY(1f);
            top_voice_search.setScaleX(1f);
            top_voice_search.setScaleY(1f);
            top_update_setting_layout.setScaleX(1f);
            top_update_setting_layout.setScaleY(1f);

		} else if (id == R.id.top_screen) {
            top_welcome_layout.setScaleX(1f);
            top_welcome_layout.setScaleY(1f);
            top_language_layout.setScaleX(1f);
            top_language_layout.setScaleY(1f);
            top_time_zone_layout.setScaleX(1f);
            top_time_zone_layout.setScaleY(1f);
            top_network_layout.setScaleX(1f);
            top_network_layout.setScaleY(1f);
            top_screen_layout.setScaleX(1.2f);
            top_screen_layout.setScaleY(1.2f);
            top_update_setting_layout.setScaleX(1f);
            top_update_setting_layout.setScaleY(1f);
		}else if(id==R.id.top_ota_update_settings){
			top_welcome_layout.setScaleX(1f);
            top_welcome_layout.setScaleY(1f);
            top_language_layout.setScaleX(1f);
            top_language_layout.setScaleY(1f);
            top_time_zone_layout.setScaleX(1f);
            top_time_zone_layout.setScaleY(1f);
            top_network_layout.setScaleX(1f);
            top_network_layout.setScaleY(1f);
            top_screen_layout.setScaleX(1f);
            top_screen_layout.setScaleY(1f);
            top_update_setting_layout.setScaleX(1.2f);
            top_update_setting_layout.setScaleY(1.2f);
		}
       
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		int id = v.getId();
		if (id == R.id.top_welcome) {
			if (hasFocus) {
				top_welcome_layout.setBackgroundResource(Color.TRANSPARENT);
				top_welcome.setBackgroundResource(R.drawable.image_top_welcome_focus);
			} else {
				top_welcome_layout.setBackgroundResource(R.drawable.image_top_welcome);
				top_welcome.setBackgroundResource(Color.TRANSPARENT);
			}

		} else if (id == R.id.top_language) {
			if (hasFocus) {
				top_language_layout.setBackgroundResource(Color.TRANSPARENT);
				top_language.setBackgroundResource(R.drawable.image_top_language_focus);
			} else {
				top_language_layout.setBackgroundResource(R.drawable.image_top_language);
				top_language.setBackgroundResource(Color.TRANSPARENT);
			}

		} else if (id == R.id.top_network) {
			if (hasFocus) {
				top_network_layout.setBackgroundResource(Color.TRANSPARENT);
				top_network.setBackgroundResource(R.drawable.image_top_network_focus);
			} else {
				top_network_layout.setBackgroundResource(R.drawable.image_top_network);
				top_network.setBackgroundResource(Color.TRANSPARENT);
			}

		} else if (id == R.id.top_screen) {
			if (hasFocus) {
				top_screen_layout.setBackgroundResource(Color.TRANSPARENT);
				top_screenadjust.setBackgroundResource(R.drawable.image_top_screen_focus);
			} else {
				top_screen_layout.setBackgroundResource(R.drawable.image_top_screen);
				top_screenadjust.setBackgroundResource(Color.TRANSPARENT);
			}

		}/*else if (id == R.id.select_cn){
            selectLanguageByID(id,hasFocus);
        }else  if (id == R.id.select_english){
            selectLanguageByID(id,hasFocus);
        }else if (id == R.id.select_tw){
            selectLanguageByID(id,hasFocus);
        }*/else if(id == R.id.update_setting_et) {
        	if(hasFocus) {
        		editTextUpdateIp.setBackgroundResource(R.drawable.password_focus);
        	}else {
        		//ColorStateList color = ColorStateList.valueOf(android.R.color.white);
        		editTextUpdateIp.setBackgroundResource(R.drawable.image_button_bottom);
        		//editTextUpdateIp.setTextColor(color);
        	}
        }

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "=====  onKeyDown() , keyCode = " + keyCode);
		if (isDisplayView) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				btn_position_zoom_in.setBackgroundResource(R.drawable.minus_unfocus);
				btn_position_zoom_out.setBackgroundResource(R.drawable.plus_focus);
				if(screen_rate < MAX_Height){
				    showProgressUI(1);
			        //mScreenPositionManager.zoomIn();
			        mScreenPositionManager.zoomByPercent(screen_rate);
                }
				return true;

			} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {

				if (screen_rate > MIN_Height) {
                    showProgressUI(-1);
					//mScreenPositionManager.zoomOut();
					mScreenPositionManager.zoomByPercent(screen_rate);
				}       
				btn_position_zoom_in.setBackgroundResource(R.drawable.minus_focus);
				btn_position_zoom_out.setBackgroundResource(R.drawable.plus_unfocus);
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_BACK
					|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
				//Log.d(TAG, "===== closeScreenAdjustLayout() now !!!");
				//closeScreenAdjustLayout();
			}
		}
        if (keyCode == KeyEvent.KEYCODE_BACK){
        	if(mCuttentView == OOBE_TIME_ZONE) {
    			layoutTimeZone.setVisibility(View.GONE);
    			oobe_time_zone.setVisibility(View.VISIBLE);
    			return true;
    		}else {
    			finish();//modifed by clei
    		}
        }
		return super.onKeyDown(keyCode, event);
	}

	void setSharedPrefrences(String name, boolean value) {
		Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,Context.MODE_PRIVATE).edit();
		editor.putBoolean(name, value);
		editor.commit();
	}

	void setOobeStartProp(String value) {
		SystemWriteManager sw = (SystemWriteManager) mContext.getSystemService("system_write");
		sw.setProperty("persist.sys.oobe.start", value);
	}

   class MyHandle extends Handler {
		@Override
		public void handleMessage(Message msg) {
            switch(msg.what){
                case UPDATE_AP_LIST :
                        oobe_mAccessPointListAdapter.updateAccesspointList();
                        if (oobe_mAccessPointListAdapter.getCount() <= 0) {
                            oobe_mAcessPointListView.setVisibility(View.GONE);
                            oobe_wifi_listview_tip.setVisibility(View.VISIBLE);
                        } else {
                            oobe_wifi_listview_tip.setVisibility(View.GONE);
                            oobe_mAcessPointListView.setVisibility(View.VISIBLE);
                        }
                    break;
                case UPDATE_ETH_STATUS :
                    if (!isEthDeviceAdded()){
                        setEthCheckBoxSwitch(false);
                    }
                    break;
            }
		}
	}

       private boolean getWifiCheckBoxState(){
        int state = Settings.Global.getInt(getContentResolver(), Settings.Global.WIFI_ON, 0);
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

    private boolean getEthCheckBoxState(){
        //int state = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ETH_ON, 0);
        int state = oobe_mEthernetManager.getEthState();
        if (Utils.DEBUG) Log.d(TAG,"===== getEthCheckBoxState() , state : " + state);
        if(state == EthernetManager.ETH_STATE_ENABLED){
            return true;
        }
        else{
            return false;
        }
    }

    private void setShowPasswordState(boolean enable ){
        setSharedPrefrences("show_password",enable);
        updateShowPasswordBoxUI();
    }
    
    private boolean getShowPasswordState(){
        return sharepreference.getBoolean("show_password", false);
    }

    private void updateShowPasswordBoxUI(){         
         if(getShowPasswordState()){
            oobe_show_password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
         }else{
            oobe_show_password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_uncheck, 0, 0, 0);
         }
    }
}
