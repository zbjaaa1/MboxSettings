package com.mbx.settingsmbox;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.SystemWriteManager;
import android.app.MboxOutputModeManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.LinkProperties;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManagerPolicy;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;
import android.os.UserHandle ;
import android.os.SystemProperties;
import java.net.InetAddress;
import java.util.Iterator;
import java.net.Inet4Address;
import java.util.Locale;



public class SettingsMboxActivity extends Activity implements OnClickListener, View.OnFocusChangeListener ,
		OnItemClickListener {

	private String TAG = "SettingsMboxActivity";
	private ActivityManager mAm;
    private final int TIME_4_MIN = 4 * 60 * 1000;
    private final int TIME_8_MIN = 8 * 60 * 1000;
    private final int TIME_12_MIN = 12 * 60 * 1000;
    private final int TIME_MAX_MIN = Integer.MAX_VALUE;
    
    private final int MAX_Height = 100;
    private final int MIN_Height = 80;

	private static final String PREFERENCE_BOX_SETTING = "preference_box_settings";
	private static final String CEC_CONFIG = "ubootenv.var.cecconfig";
	private static final String writeCecConfig = "/sys/class/amhdmitx/amhdmitx0/cec_config"; 
	private static final String eth_device_sysfs = "/sys/class/ethernet/linkspeed";
    private final static String DISPLAY_MODE_SYSFS = "/sys/class/display/mode";
	private final Context mContext = this;
	private SystemWriteManager sw = null;
    private final static int UPDATE_AP_LIST = 100;
    private final static int UPDATE_OUTPUT_MODE_UI = 101;
    private final static int SHOW_CONFIRM_DIALOG = 102; 
    private final static int UPDATE_ETH_STATUS = 103;

	public final static int VIEW_NETWORK = 0;
	public final static int VIEW_DISPLAY = 1;
	public final static int VIEW_MORE = 2;
	public final static int VIEW_OTHER = 3;
	public final static int VIEW_SCREEN_ADJUST = 4;

	public static int mCurrentContentNum = 0;
	private static final int ETH_STATE_UNKNOWN = 0;
	private static final int ETH_STATE_DISABLED = 1;
	private static final int ETH_STATE_ENABLED = 2;
    
    // Combo scans can take 5-6s to complete - set to 10s.
    private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;
    private WifiScanner mWifiScanner;
    
	private static final String HDMIIN_SWITCH_FULL_PROP = "mbx.hdmiin.switchfull";
	private static final String HDMIIN_MAIN_WINDOW_FULL_PROP = "mbx.hdmiin.mwfull";

	private int screen_rate = MIN_Height;

	private LinearLayout wifi_connected;
	private LinearLayout wifi_input_password;

	private LinearLayout wifi_not_connect;

	private TextView wifi_slect_tip;

	private EditText password_editview;

	private TextView wifi_listview_tip;
	private ListView mAcessPointListView = null;

	private AccessPointListAdapter mAccessPointListAdapter = null;
	private MyHandle mHander = null;

	private TextView wifi_ssid_value;
	private TextView ip_address_value;
	
	private TextView select_wifi;

	private TextView select_ethernet;

	private TextView wifi_connected_tip;

	private LinearLayout root_eth_view;
    private LinearLayout net_root_view;
	private LinearLayout root_wifi_view;

	private EthernetManager mEthernetManager;
    private OutPutModeManager mOutPutModeManager;

	private WifiManager mWifiManager;
    private TextView eth_IP_value = null;

	private TextView eth_connected_tip = null;
	private LinearLayout eth_ip_layout = null;
    
	private BroadcastReceiver myReceiver = null;

	private LinearLayout setting_network_layout;
	

	RelativeLayout settingsTopView_01;
	RelativeLayout settingsTopView_02;
	RelativeLayout settingsTopView_03;
	RelativeLayout settingsTopView_04;

	private LinearLayout settingsContentLayout_01;
	private LinearLayout settingsContentLayout_02;
	private ScrollView settingsContentLayout_03;
	private LinearLayout settingsContentLayout_04;

	private LinearLayout screen_self_set = null;
    private LinearLayout cvbs_screen_self_set = null;
    private LinearLayout secreen_auto = null;

    private LinearLayout voice_auto = null;
    private LinearLayout voice_setting = null;
    private LinearLayout dolby_setting = null;
    private LinearLayout dts_setting = null;
    private LinearLayout dts_mul_asset = null;
    private LinearLayout dts_trans_setting = null;
    
	private LinearLayout settings_content_postion = null;
	private LinearLayout button_scrren_adjust;

	private LinearLayout wifi_direct = null;

	private ImageButton btn_position_zoom_out = null;
	private ImageButton btn_position_zoom_in = null;
	private ImageView img_num_hundred = null;
	private ImageView img_num_ten = null;
	private ImageView img_num_unit = null;

	private ImageView img_progress_bg;
	private ScreenPositionManager mScreenPositionManager = null;

	private TextView screen_time_01;
	private TextView screen_time_02;
	private TextView screen_time_03;
	private TextView screen_time_04;
	private TextView[] mScreenKeepTimes;

	private LinearLayout screen_keep = null;
	private static int mCurrentScreenKeepIndex = 0;
	private boolean mEthConnectingFlag = false;
    
	public static int Num[] = { R.drawable.ic_num0, R.drawable.ic_num1,
			R.drawable.ic_num2, R.drawable.ic_num3, R.drawable.ic_num4,
			R.drawable.ic_num5, R.drawable.ic_num6, R.drawable.ic_num7,
			R.drawable.ic_num8, R.drawable.ic_num9 };
	public static int progressNum[] = { R.drawable.ic_per_81,
			R.drawable.ic_per_82, R.drawable.ic_per_83, R.drawable.ic_per_84,
			R.drawable.ic_per_85, R.drawable.ic_per_86, R.drawable.ic_per_87,
			R.drawable.ic_per_88, R.drawable.ic_per_89, R.drawable.ic_per_90,
			R.drawable.ic_per_91, R.drawable.ic_per_92, R.drawable.ic_per_93,
			R.drawable.ic_per_94, R.drawable.ic_per_95, R.drawable.ic_per_96,
			R.drawable.ic_per_97, R.drawable.ic_per_98, R.drawable.ic_per_99,
			R.drawable.ic_per_100 };

	private PopupWindow popupWindow = null;
	private TextView current_mode_value = null;
    private TextView cvbs_current_mode_value = null;
	private TextView self_select_mode = null;
    private TextView cvbs_self_select_mode = null;
	private TextView auto_set_screen = null;
	private SharedPreferences sharepreference = null;

	private TextView hide_status_bar = null;

	private TextView requestScreen = null;
	private TextView screen_land = null;

	private TextView miracast;
	private TextView remoteControl;
	private TextView ipremoteTV;
	private RelativeLayout cec_main;
	private RelativeLayout cec_play;
	private RelativeLayout cec_power;
	private RelativeLayout cec_language;

	private ImageView imageview_cec_main;
	private ImageView imageview_cec_play;
	private ImageView imageview_cec_power;
	private ImageView imageview_cec_language;

	private LinearLayout city_select = null;
	private TextView province_view;
	private TextView city_view;
	private int mCrrentLocationfocus = 0;

    private String string_pcm =  null;
    private String string_spdif =  null;
	private String string_hdmi =  null;

	private WifiP2pDevice mThisDevice = null;
	private MboxOutputModeManager mMboxOutputModeManager = null;
    private static RelativeLayout preView = null;
    private TextView miracast_name = null;
    private TextView remoteControlIp = null;
    public static OnSharedPreferenceChangeListener listener = null ;

    private static boolean isSupportEthernet = true ;
    public static int mCurrentViewNum = -1;
    public static String oldMode = null;
    public static DisplayConfirmDialog dialog = null;
    public static HdmiinSetOutputDialog mHdmiinDialog = null;
    private RelativeLayout preFocusView = null ;
    private boolean isOpenAdjustScreenView = false;
    private static boolean isNeedShowConfirmDialog = false;
    private TextView show_password = null;
    private long startTime = 0;
    private long endTime = 0;
    private final int SECURITY_WPA = 1;
    private static int goToIndex = 0;
    private static String currentAP = null;
    private ScanResult currentSelectAccessPoint = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
        Log.d(TAG, "===== onCreate()");
		setContentView(R.layout.settings_main);
        mAm = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		sw = (SystemWriteManager) mContext.getSystemService("system_write");
        mEthernetManager = (EthernetManager) mContext.getSystemService("ethernet");
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mOutPutModeManager = new OutPutModeManager(this);
        mHander = new MyHandle();

		settingsContentLayout_01 = (LinearLayout) findViewById(R.id.settingsContent01);
		settingsContentLayout_02 = (LinearLayout) findViewById(R.id.settingsContent02);
		settingsContentLayout_03 = (ScrollView) findViewById(R.id.settingsContent03);
		settingsContentLayout_04 = (LinearLayout) findViewById(R.id.settingsContent04);
		
		screen_self_set = (LinearLayout) findViewById(R.id.screen_self_set);
		screen_self_set.setOnClickListener(this);
		self_select_mode = (TextView) findViewById(R.id.self_select_mode);
		auto_set_screen = (TextView) findViewById(R.id.auto_set_screen);

		current_mode_value = (TextView) findViewById(R.id.current_mode_value);
        current_mode_value.setText(mOutPutModeManager.getCurrentOutPutModeTitle(1));
		cvbs_current_mode_value = (TextView) findViewById(R.id.cvbs_current_mode_value);
        miracast_name = (TextView) findViewById(R.id.miracast_name);
		sharepreference = getSharedPreferences(PREFERENCE_BOX_SETTING,
				Context.MODE_PRIVATE);
        remoteControlIp = (TextView) findViewById(R.id.remoteControl_ip);
        listener = new OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
			    if (Utils.DEBUG) Log.d(TAG,"===== onSharedPreferenceChanged() , key :" + key);
				if(key.equals("open_remote_control")){
                     String value = pref.getString("open_remote_control","false");
                     
                     if(value.equals("true")){
                            remoteControlIp.setVisibility(View.VISIBLE);
                            String ip = getDeviceIpAddress();
                            if(ip!=null){
                                remoteControlIp.setText(ip);
                            }
                     }else{
                            remoteControlIp.setVisibility(View.INVISIBLE);
                     }                   
                }else if(key.equals("open_mirrcast")){
                    
                   
                    String value = pref.getString("open_mirrcast","false");
                    if ("true".equals(value)) {
                        miracast_name.setVisibility(View.VISIBLE);
                        if (mThisDevice != null) {
                            String name = mContext.getResources().getString(R.string.miracast_server_name)+ " " + mThisDevice.deviceName;
                            miracast_name.setText(name);
                            if (Utils.DEBUG) Log.d(TAG, "===== mThisDevice name is  "+ mThisDevice.deviceName);
                        } 
                    }else{
                        miracast_name.setVisibility(View.INVISIBLE);
                    }   
                }
			}
		};
        
        sharepreference.registerOnSharedPreferenceChangeListener(listener);
        
		secreen_auto = (LinearLayout) findViewById(R.id.secreen_auto);

		secreen_auto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (SystemProperties.getBoolean(HDMIIN_SWITCH_FULL_PROP, false) && !SystemProperties.getBoolean(HDMIIN_MAIN_WINDOW_FULL_PROP, true))
			        showHdmiinPromptDialog();
				else
			        setAutoOutModeSwitch();
			}
		});

        cvbs_screen_self_set = (LinearLayout) findViewById(R.id.cvbs_screen_self_set);
		cvbs_screen_self_set.setOnClickListener(this);
		cvbs_self_select_mode = (TextView) findViewById(R.id.cvbs_self_select_mode); 
        
		upDateOutModeUi();

		hide_status_bar = (TextView) findViewById(R.id.hide_status_bar);
		upDateStatusBarUi();

		LinearLayout screen_hide_bar = (LinearLayout) findViewById(R.id.screen_hide_bar);
		screen_hide_bar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setStatusBarSwitch();
			}
		});

		settingsTopView_01 = (RelativeLayout) findViewById(R.id.settingsTopView_01);
        settingsTopView_01.setOnClickListener(this);
        settingsTopView_01.setOnFocusChangeListener(this);
		settingsTopView_02 = (RelativeLayout) findViewById(R.id.settingsTopView_02);
        settingsTopView_02.setOnClickListener(this);
        settingsTopView_02.setOnFocusChangeListener(this);
		settingsTopView_03 = (RelativeLayout) findViewById(R.id.settingsTopView_03);
        settingsTopView_03.setOnClickListener(this);
        settingsTopView_03.setOnFocusChangeListener(this);
		settingsTopView_04 = (RelativeLayout) findViewById(R.id.settingsTopView_04);
        settingsTopView_04.setOnClickListener(this);
        settingsTopView_04.setOnFocusChangeListener(this);

		settings_content_postion = (LinearLayout) findViewById(R.id.settings_content_postion);
		button_scrren_adjust = (LinearLayout) findViewById(R.id.button_scrren_adjust);
		button_scrren_adjust.setOnClickListener(this);

		wifi_direct = (LinearLayout) findViewById(R.id.wifi_direct);
		wifi_direct.setNextFocusUpId(R.id.settingsTopView_03);

		img_num_hundred = (ImageView) findViewById(R.id.img_num_hundred);
		img_num_ten = (ImageView) findViewById(R.id.img_num_ten);
		img_num_unit = (ImageView) findViewById(R.id.img_num_unit);
		img_progress_bg = (ImageView) findViewById(R.id.img_progress_bg);

		screen_keep = (LinearLayout) findViewById(R.id.screen_keep);

		screen_time_01 = (TextView) findViewById(R.id.screen_time_01);
        screen_time_01.setOnClickListener(this);
		screen_time_02 = (TextView) findViewById(R.id.screen_time_02);
        screen_time_02.setOnClickListener(this);
		screen_time_03 = (TextView) findViewById(R.id.screen_time_03);
        screen_time_03.setOnClickListener(this);
		screen_time_04 = (TextView) findViewById(R.id.screen_time_04);
        screen_time_04.setOnClickListener(this);
		mScreenKeepTimes = new TextView[4];
		mScreenKeepTimes[0] = screen_time_01;
		mScreenKeepTimes[1] = screen_time_02;
		mScreenKeepTimes[2] = screen_time_03;
		mScreenKeepTimes[3] = screen_time_04;

        int time = Settings.System.getInt(getContentResolver(), "screen_off_timeout",TIME_MAX_MIN);
        switch(time){
            case  TIME_MAX_MIN :
                mCurrentScreenKeepIndex = 0;
                break;
            case  TIME_4_MIN :
                mCurrentScreenKeepIndex = 1;
                break;
            case  TIME_8_MIN :
                mCurrentScreenKeepIndex = 2;
                break;
            case  TIME_12_MIN :
                mCurrentScreenKeepIndex = 3;
                break;
            default :
                mCurrentScreenKeepIndex = 0;
                break;
        }
        slectKeepScreenIndex(mCurrentScreenKeepIndex);

		Button system_update = (Button) findViewById(R.id.system_update);
		system_update.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goToIndex = 3 ;
				Intent i = new Intent();
				ComponentName componetName = new ComponentName(
						"com.geniatech.upgrade",
						"com.geniatech.upgrade.Upgrade");/* modifed by clei */
				i.setComponent(componetName);
				mContext.startActivity(i);

			}
		});

		Button more_settings = (Button) findViewById(R.id.more_settings);
		more_settings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goToIndex = 3 ;
				Intent i = new Intent();
				ComponentName componetName = new ComponentName("com.android.settings", "com.android.settings.Settings");
				i.setComponent(componetName);
				mContext.startActivity(i);
			}
		});

		LinearLayout request_screen = (LinearLayout) findViewById(R.id.request_screen);
		requestScreen = (TextView) findViewById(R.id.requestScreen);
		upDateRequestScreen();
		request_screen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setRequestScreenSwitch();
			}
		});

		LinearLayout keep_screen_land = (LinearLayout) findViewById(R.id.keep_screen_land);
		screen_land = (TextView) findViewById(R.id.screen_land);
		upDateKeepScreenLandUi();
		keep_screen_land.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setKeepScreenLandSwitch();
			}
		});

        voice_auto = (LinearLayout) findViewById(R.id.voice_auto);
		voice_auto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setAutoVoice();
			}
		});

		voice_setting = (LinearLayout) findViewById(R.id.voice_setting);
		voice_setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openVoicePopupWindow();

			}
		});

        dolby_setting = (LinearLayout) findViewById(R.id.dolby_setting);
        if (sw.getPropertyBoolean("ro.platform.support.dolby", false))
            dolby_setting.setVisibility(View.VISIBLE);
		dolby_setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openDolbyPopupWindow();

			}
		});

        dts_setting = (LinearLayout) findViewById(R.id.dts_setting);
        if (sw.getPropertyBoolean("ro.platform.support.dts", false))
            dts_setting.setVisibility(View.VISIBLE);
		dts_setting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openDtsPopupWindow();

			}
		});

        dts_mul_asset = (LinearLayout) findViewById(R.id.dts_mul_asset);
        if (sw.getPropertyBoolean("ro.platform.support.dtsmulasset", false))
            dts_mul_asset.setVisibility(View.VISIBLE);
        dts_mul_asset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setDtsMulAsset();
			}
		});
        updateDtsMulAssetUi();

        dts_trans_setting = (LinearLayout) findViewById(R.id.dts_trans_setting);
        if (sw.getPropertyBoolean("ro.platform.support.dtstrans", false))
            dts_trans_setting.setVisibility(View.VISIBLE);
		dts_trans_setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openDtsTransPopupWindow();
			}
		});
        getDtsTransInit();
        
        mMboxOutputModeManager = (MboxOutputModeManager)mContext.getSystemService(Context.MBOX_OUTPUTMODE_SERVICE);
        updateVoiceUi();

		LinearLayout wifi_direct = (LinearLayout) findViewById(R.id.wifi_direct);
		
		miracast = (TextView) findViewById(R.id.miracast);
		upDateMirrcastUi();
		wifi_direct.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                goToIndex = 2 ;
                Intent i = new Intent();
                ComponentName componetName = new ComponentName("com.amlogic.miracast", "com.amlogic.miracast.WiFiDirectMainActivity");
                i.setComponent(componetName);
				mContext.startActivity(i);
				//setMiracastSwitch();
			}
		});

		LinearLayout remote_control = (LinearLayout) findViewById(R.id.remote_control);
		remoteControl = (TextView) findViewById(R.id.remoteControl);
        
		upDateRemoteControlUi();
		remote_control.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setRemoteControlSwitch();

			}
		});

		
		LinearLayout ipremote = (LinearLayout) findViewById(R.id.ipremote_control);
		ipremoteTV = (TextView) findViewById(R.id.ipremote);

		upDateIpremoteUi();
		ipremote.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setIPremoteSwitch();
			}
		});

		LinearLayout cec_control = (LinearLayout) findViewById(R.id.cec_control);
		cec_control.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openCECPopupWindow();
			}
		});
		//Rony add 
		LinearLayout hdmi_mute = (LinearLayout) findViewById(R.id.hdmi_mute);
		updateHdmiOutputMuteUi();
		hdmi_mute.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setHdmiOutputMuteSwitch();
			}
			
		});
		
		LinearLayout spdif_mute = (LinearLayout) findViewById(R.id.spdif_mute);
		updateSpdifOutputMuteUi();
		spdif_mute.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setSpdifOutputMuteSwitch();
			}
			
		});
		//Rony add end

		city_select = (LinearLayout) findViewById(R.id.city_select);
		province_view = (TextView) findViewById(R.id.province);
        province_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openLocationPopupWindow(0);
			}
		});
		city_view = (TextView) findViewById(R.id.city);
        city_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openLocationPopupWindow(1);
			}
		});
		city_select.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					province_view
							.setBackgroundResource(R.drawable.select_focused);

				} else {
					province_view.setBackgroundResource(Color.TRANSPARENT);
					city_view.setBackgroundResource(Color.TRANSPARENT);
				}

			}
		});

		city_select.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openLocationPopupWindow(mCrrentLocationfocus);
			}
		});

    city_select.setVisibility(View.GONE);//clei for hide city
    request_screen.setVisibility(View.GONE);
    keep_screen_land.setVisibility(View.GONE);    
    dts_setting.setVisibility(View.GONE);
    dts_mul_asset.setVisibility(View.GONE);
    dts_trans_setting.setVisibility(View.GONE);
    hdmi_mute.setVisibility(View.GONE);
    spdif_mute.setVisibility(View.GONE);
		sharepreference = getSharedPreferences(PREFERENCE_BOX_SETTING,
				Context.MODE_PRIVATE);
		String p = sharepreference.getString("settings_province",
				getResources().getString(R.string.province));
		province_view.setText(p);
		String c = sharepreference.getString("settings_city", getResources()
				.getString(R.string.city));
		city_view.setText(c);

		eth_IP_value = (TextView) findViewById(R.id.eth_IP_value);

		eth_connected_tip = (TextView) findViewById(R.id.eth_connected_tip);
		eth_ip_layout = (LinearLayout) findViewById(R.id.eth_ip_layout);

        String temp = null;
        TextView model_number_value = (TextView) findViewById(R.id.model_number_value);
        String productModel = SystemInfoManager.getModelNumber();
        model_number_value.setText(productModel);
        String hasEthernet = sw.getPropertyString("hw.hasethernet" , "false");
        if ("false".equals(hasEthernet)){
            isSupportEthernet = false;
            Log.d(TAG,"===== not support Ethernet!");
        }
        
        TextView firmware_version_value = (TextView) findViewById(R.id.firmware_version_value);
        temp =  SystemInfoManager.getAndroidVersion();
        firmware_version_value.setText(temp);
        
        
        TextView build_number_value = (TextView) findViewById(R.id.build_number_value);
        
        build_number_value.setText( SystemInfoManager.getBuildNumber());
        
        TextView kernel_version_value = (TextView) findViewById(R.id.kernel_version_value);
        temp = SystemInfoManager.getKernelVersion();
        kernel_version_value.setText(temp);

        initNetView();

        IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        filter.addAction(WindowManagerPolicy.ACTION_HDMI_HW_PLUGGED);
        filter.addAction(WindowManagerPolicy.ACTION_HDMI_MODE_CHANGED);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);

        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        filter.addAction("action.show.dialog");
		
	    myReceiver = new MyReceiver();
		registerReceiver(myReceiver, filter);

        string_pcm = mContext.getResources().getString(R.string.voices_settings_pcm);
        string_spdif = mContext.getResources().getString(R.string.voices_settings_spdif);
        string_hdmi = mContext.getResources().getString(R.string.voices_settings_hdmi);
	}

	private void getOpenId(){
		int index =getIntent().getIntExtra("Tab", -1);	
		goToIndex = index == -1 ? goToIndex :index;
		getIntent().putExtra("Tab", -1);
	}
		private void upDateIpremoteUi() {

		String isIpremoteBootStart = sharepreference.getString("ipremote_start_bootcomplete", "false");
		if (isIpremoteBootStart.equals("true")) {
			ipremoteTV.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.on, 0);
       	} else {
			ipremoteTV.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.off, 0);
		}

	}

	private void setIPremoteSwitch() {
		String isIpremoteBootStart = sharepreference.getString("ipremote_start_bootcomplete", "false");

		Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,
				Context.MODE_PRIVATE).edit();

		if (!isIpremoteBootStart.equals("true")) {
			editor.putString("ipremote_start_bootcomplete", "true");
			editor.commit();
			Intent intent = new Intent("android.custom.action.BOOT_COMPLETED");
            mContext.sendBroadcast(intent);
			ipremoteTV.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.on, 0);
		} else {
			mAm.forceStopPackage("com.google.tv.discovery");
			mAm.forceStopPackage("com.google.tv.ipremote");
			editor.putString("ipremote_start_bootcomplete", "false");
			editor.commit();
			ipremoteTV.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.off, 0);
		}
	}


	private void upDateRemoteControlUi() {

		String isOpenRemoteContrl = sharepreference.getString("open_remote_control", "false");
		if (isOpenRemoteContrl.equals("true")) {
			remoteControl.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.on, 0);
            remoteControlIp.setVisibility(View.VISIBLE);
            String ip = getDeviceIpAddress();
            if(ip!=null){
                remoteControlIp.setText(ip);
            }
		} else {
			remoteControl.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.off, 0);
            remoteControlIp.setVisibility(View.INVISIBLE);
		}

	}

	private void setRemoteControlSwitch() {
		String isOpenRemoteContrl = sharepreference.getString(
				"open_remote_control", "false");
		Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,
				Context.MODE_PRIVATE).edit();
		if (isOpenRemoteContrl.equals("true")) {
			editor.putString("open_remote_control", "false");
			editor.commit();
			Intent i = new Intent();
			i.setAction("com.amlogic.remoteControl.RC_STOP");
			//SettingsMboxActivity.this.sendBroadcast(i);
            mContext.sendBroadcastAsUser(i, UserHandle.ALL ); 
			Log.d(TAG,"===== send broadcast stop remote service");
		} else {
			editor.putString("open_remote_control", "true");
			editor.commit();
			Intent i = new Intent();
			i.setAction("com.amlogic.remoteControl.RC_START");
			//SettingsMboxActivity.this.sendBroadcast(i);
			mContext.sendBroadcastAsUser(i, UserHandle.ALL); 
			Log.d(TAG,"===== send broadcast start remote service");
		}
		upDateRemoteControlUi();
	}

	private void setMiracastSwitch() {
      
		String isOpenMiracast = sharepreference.getString("open_mirrcast","false");
		Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,Context.MODE_PRIVATE).edit();
		if (isOpenMiracast.equals("true")) {
			editor.putString("open_mirrcast", "false");
			editor.commit();
			Intent i = new Intent();
			i.setAction("com.amlogic.miracast.MIRACAST_BKSTOP");
			//mContext.sendBroadcast(i);
			mContext.sendBroadcastAsUser(i, UserHandle.ALL ); 
            //com.amlogic.miracast   WiFiDirectMainActivity
			Log.d(TAG,"===== send broadcast stop miracast");
		} else {
			editor.putString("open_mirrcast", "true");
			editor.commit();
			Intent i = new Intent();
			i.setAction("com.amlogic.miracast.MIRACAST_BKSTART");
			//mContext.sendBroadcast(i);
            mContext.sendBroadcastAsUser(i, UserHandle.ALL ); 
			Log.d(TAG,"===== send broadcast start miracast");
		}
		upDateMirrcastUi();
	}

	private void upDateMirrcastUi() {
		String isOpenMirrcast = sharepreference.getString("open_mirrcast",
				"false");
		if (isOpenMirrcast.equals("true")) {
			//miracast.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.on, 0);
            miracast_name.setVisibility(View.VISIBLE);
            String name = sharepreference.getString("miracast_device_name","NULL");
            if("NULL".equals(name)){
                miracast_name.setVisibility(View.INVISIBLE);
            }else{
                miracast_name.setText(name);
            }
            
		} else {
		    miracast_name.setVisibility(View.INVISIBLE);
			//miracast.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.off, 0);
		}

	}

	private void setKeepScreenLandSwitch() {
		String istKeepScreenLand = sharepreference.getString(
				"keep_screen_land", "true");
		Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,
				Context.MODE_PRIVATE).edit();
		if ("true".equals(istKeepScreenLand)) {
			editor.putString("keep_screen_land", "false");
			editor.commit();
			sw.setProperty("sys.keeplauncher.landcape", "false");

		} else {
			editor.putString("keep_screen_land", "true");
			editor.commit();
			sw.setProperty("sys.keeplauncher.landcape", "true");
		}

		upDateKeepScreenLandUi();

	}

	private void upDateKeepScreenLandUi() {

		String vlaue = sharepreference.getString("keep_screen_land", "true");
		if ("true".equals(vlaue)) {
			screen_land.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_checked, 0);
		} else {
			screen_land.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_uncheck, 0);
		}

	}

	private void setRequestScreenSwitch() {

		String vlaue = sw.getPropertyString("ubootenv.var.has.accelerometer" , "***");
		if ("true".equals(vlaue)) {
			sw.setProperty("ubootenv.var.has.accelerometer", "false");
		} else if("false".equals(vlaue)){
			sw.setProperty("ubootenv.var.has.accelerometer", "true");
		}else{
            sw.setProperty("ubootenv.var.has.accelerometer", "false");
        }

		upDateRequestScreen();
	}

	private void upDateRequestScreen() {
		String isRequest_screen = sw.getPropertyString("ubootenv.var.has.accelerometer","***");
		if (Utils.DEBUG) Log.d(TAG, "====== isRequest_screen: " + isRequest_screen);
		if ("true".equals(isRequest_screen)) {
			requestScreen.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_checked, 0);
		} else if("false".equals(isRequest_screen)){
			requestScreen.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_uncheck, 0);
		}
	}

	private void setStatusBarProperty(String value) {
		sw.setProperty("persist.sys.hideStatusBar", value);
	}

	private String getStatusBarProperty() {
		return sw.getPropertyString("persist.sys.hideStatusBar", "true");
	}

	void setStatusBarSwitch() {
		String isHideStatusBar = sw.getPropertyString("persist.sys.hideStatusBar", "true");/* modifed by clei */
		Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,Context.MODE_PRIVATE).edit();

		if (isHideStatusBar.equals("true")) {
			setStatusBarProperty("false");
			editor.putString("hide_status_bar", "false");
			editor.commit();
		} else {
			setStatusBarProperty("true");
			editor.putString("hide_status_bar", "true");
			editor.commit();
		}
		upDateStatusBarUi();
		restartActivitySelf();
	}

	void upDateStatusBarUi() {
		String isHideStatusBar = sw.getPropertyString("persist.sys.hideStatusBar", "true");/* modifed by clei */
		
		if ("true".equals(isHideStatusBar)) {
			hide_status_bar.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.on, 0);
			setStatusBarProperty("true");
		} else {
			hide_status_bar.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.off, 0);
			setStatusBarProperty("false");
		}

	}

    boolean getAutoHDMIMode() {
        boolean isAutoHdmiMode = true;
        try {
            isAutoHdmiMode = ((0 == Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.DISPLAY_OUTPUTMODE_AUTO))?false:true) ;
        } catch (Settings.SettingNotFoundException se) {
            Log.d(TAG, "Error: "+se);
        }
        return isAutoHdmiMode;
    }
    
    void upDateOutModeUi() {
        boolean isAutoHdmiMode = getAutoHDMIMode();
        if (isAutoHdmiMode) {
            screen_self_set.setFocusable(false);
            screen_self_set.setClickable(false);
            self_select_mode.setTextColor(Color.GRAY);
            current_mode_value.setTextColor(Color.GRAY);
            auto_set_screen.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.on, 0);
            //Message msg = mHander.obtainMessage();
            //msg.what = UPDATE_OUTPUT_MODE_UI ;
            //mHander.sendMessage(msg);

        } else {
            auto_set_screen.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.off, 0);
            screen_self_set.setFocusable(true);
            screen_self_set.setClickable(true);
            self_select_mode.setTextColor(Color.WHITE);
            current_mode_value.setTextColor(Color.WHITE);
        }
       
        boolean isDualOutPutMode = sw.getPropertyBoolean("ro.platform.has.cvbsmode", false);
        if(!isDualOutPutMode){
            if(mOutPutModeManager.isHDMIPlugged()){
                secreen_auto.setFocusable(true);
                secreen_auto.setClickable(true);
                auto_set_screen.setTextColor(Color.WHITE);
                cvbs_screen_self_set.setVisibility(View.GONE);
                if (Utils.DEBUG) Log.d(TAG,"===== hdmi mode : " +  mOutPutModeManager.getCurrentOutPutModeTitle(1));
                current_mode_value.setText(mOutPutModeManager.getCurrentOutPutModeTitle(1));
            }else{
                cvbs_screen_self_set.setVisibility(View.VISIBLE);
                secreen_auto.setFocusable(false);
                secreen_auto.setClickable(false);
                screen_self_set.setFocusable(false);
                screen_self_set.setClickable(false);
                self_select_mode.setTextColor(Color.GRAY);
                current_mode_value.setTextColor(Color.GRAY);
                auto_set_screen.setTextColor(Color.GRAY);
                cvbs_current_mode_value.setText(mOutPutModeManager.getCurrentOutPutModeTitle(0));
            }

        }else{
            cvbs_screen_self_set.setVisibility(View.VISIBLE);
            cvbs_current_mode_value.setText(mOutPutModeManager.getCurrentOutPutModeTitle(0));
            current_mode_value.setText(mOutPutModeManager.getCurrentOutPutModeTitle(1));
        }       
    }

    void setAutoOutModeSwitch() {
        boolean isAutoHdmiMode = getAutoHDMIMode();
        if (isAutoHdmiMode) {
            Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.DISPLAY_OUTPUTMODE_AUTO, 0);
        } else {
            Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.DISPLAY_OUTPUTMODE_AUTO, 1);
            mOutPutModeManager.hdmiPlugged();
        }
        upDateOutModeUi();
    }

	private void initNetView() {
		mCurrentContentNum = VIEW_NETWORK;
		wifi_ssid_value = (TextView) findViewById(R.id.wifi_ssid_value);
		ip_address_value = (TextView) findViewById(R.id.ip_address_value);
		
		select_wifi = (TextView) findViewById(R.id.select_wifi);        
		select_wifi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setWifiCheckBoxSwitch();
			}
		});
		select_ethernet = (TextView) findViewById(R.id.select_ethernet);
		isSupportEthernet = sw.getPropertyBoolean("hw.hasethernet" , false);
        if(!isSupportEthernet){
            TextView no_network = (TextView)findViewById(R.id.no_network);
            no_network.setText(mContext.getResources().getString(R.string.no_network_wifi_only));
            select_ethernet.setVisibility(View.INVISIBLE);
        }
		select_ethernet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setEthCheckBoxSwitch(true);
			}
		});
		select_ethernet.setNextFocusUpId(R.id.settingsTopView_01);
        select_wifi.setNextFocusLeftId(R.id.select_ethernet);
		select_wifi.setNextFocusRightId(R.id.select_ethernet);
		wifi_connected_tip = (TextView) findViewById(R.id.wifi_connected_tip);

		root_eth_view = (LinearLayout) findViewById(R.id.root_eth_view);
        net_root_view = (LinearLayout) findViewById(R.id.net_root_view);
		root_wifi_view = (LinearLayout) findViewById(R.id.root_wifi_view);
		root_wifi_view.setVisibility(View.GONE);
		root_eth_view.setVisibility(View.VISIBLE);

		
        mWifiScanner = new WifiScanner();
		wifi_connected = (LinearLayout) findViewById(R.id.wifi_connected);
		wifi_input_password = (LinearLayout) findViewById(R.id.wifi_input_password);

		wifi_not_connect = (LinearLayout) findViewById(R.id.wifi_not_connect);

		wifi_slect_tip = (TextView) findViewById(R.id.wifi_slect_tip);

		password_editview = (EditText) findViewById(R.id.password_input);
        password_editview.setInputType(
                InputType.TYPE_CLASS_TEXT | (getShowPasswordState() ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));

		wifi_listview_tip = (TextView) findViewById(R.id.wifi_listview_tip);
		// password_editview.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        show_password = (TextView)findViewById(R.id.show_password);
        updateShowPasswordBoxUI();
        show_password.setOnClickListener(new OnClickListener() {
            
			@Override
			public void onClick(View v) {
                if(getShowPasswordState()){
                    setShowPasswordState(false);
                }else{
                    setShowPasswordState(true);
                }

                password_editview.setInputType(
                InputType.TYPE_CLASS_TEXT | (getShowPasswordState() ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));
			}
		});
		final Button password_connect = (Button) findViewById(R.id.password_connect);
		password_connect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String password = password_editview.getText().toString();
                WifiUtils.setPassWord(password);                    
                WifiUtils.setApName(currentAP);
				String connectSsid = mWifiManager.getConnectionInfo().getWifiSsid().toString();
				ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (password != null) {
					if (currentAP.equals(connectSsid) && wifi.isConnected()){
                        showWifiConnectedView();
                    }else {
                        if(mAccessPointListAdapter.getCurrentAPSecurityType()== SECURITY_WPA){
                            if(password.length()<8 || password.length() >63){
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.password_length_error),3000).show();
                                return ;
                            }
                        }
                        if (Utils.DEBUG) Log.d(TAG,"====== connect now!");
						showConnectingView();                    
						mAccessPointListAdapter.connect2AccessPoint(currentSelectAccessPoint,password);
                        startTime = System.currentTimeMillis();
					}
				} else {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.passwork_input_notice),3000).show();
				}

			}
		});

		mAcessPointListView = (ListView) findViewById(R.id.wifiListView);
        mAcessPointListView.setNextFocusRightId(R.id.password_input);
	
		mAcessPointListView.setOnItemClickListener(this);

		mAccessPointListAdapter = new AccessPointListAdapter(this);
		mAcessPointListView.setAdapter(mAccessPointListAdapter);

	}

    private void wifiResume(){
        if(getEthCheckBoxState()){ 
            if(isEthDeviceAdded()) {
               // mWifiManager.setWifiEnabled(false);removed by clei for wifi and eth
                mEthernetManager.setEthEnabled(true);
                updateNetWorkUI(2);
            }else{
                //mEthernetManager.setEthEnabled(false); 
                if(getWifiCheckBoxState()){
                    mWifiManager.setWifiEnabled(true);
                    mWifiScanner.resume(); 
                    updateNetWorkUI(1);
                } else {
                    updateNetWorkUI(2);
                }
            }
        }else{
            if(getWifiCheckBoxState()){
                mWifiManager.setWifiEnabled(true);
                mWifiScanner.resume(); 
                updateNetWorkUI(1);
            }else{
                updateNetWorkUI(0);
            }                
        }               
        updateEthCheckBoxUI();
        upDateWifiCheckBoxUI();

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

    private void setEthCheckBoxSwitch(boolean openEthernet){ 
        if(getEthCheckBoxState()) {
            enableEthernetView(false);
            mHander.removeMessages(UPDATE_ETH_STATUS);
            mEthConnectingFlag = false;
        } else {
            if(openEthernet && isEthDeviceAdded()){           
                enableEthernetView(true);
                mEthConnectingFlag = true;
            }else{
                mEthernetManager.setEthEnabled(false); 
                Toast.makeText(mContext, mContext.getResources().getString(R.string.ethernet_inplug_notice), 4000).show(); 
                if(!getWifiCheckBoxState())
                    updateNetWorkUI(0);
            } 
        }
         updateEthCheckBoxUI();   
         upDateWifiCheckBoxUI();
    }
  
    private void enableEthernetView(boolean able){                     
        if(able){ 
            updateNetWorkUI(2);
            eth_connected_tip.setText(R.string.ethernet_connectting);
            mEthernetManager.setEthEnabled(true);   
            //mWifiManager.setWifiEnabled(false);removed by clei for wifi and eth
        }else{
        	if(!getWifiCheckBoxState())//removed by clei for wifi 
              updateNetWorkUI(0);
           else{
             updateNetWorkUI(1);
             if(!WifiUtils.isWifiConnected(mContext))
                showConnectingView();
             mAcessPointListView.setVisibility(View.VISIBLE);
           }
            mEthernetManager.setEthEnabled(false);
        }       
    }
    
    private void enableWifiView(boolean able){
        if(able){ 
            //if(isEthDeviceAdded()){
              //  mEthernetManager.setEthEnabled(false);removed by clei for wifi and eth
            //} 
            mHander.removeMessages(UPDATE_ETH_STATUS);
            
            int wifiApState = mWifiManager.getWifiApState();
            if ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) ||
                    (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED)) {
                mWifiManager.setWifiApEnabled(null, false);
            }
            mAcessPointListView.setVisibility(View.GONE);
            wifi_listview_tip.setVisibility(View.VISIBLE);
            mWifiManager.setWifiEnabled(true);
            mWifiScanner.resume(); 
            updateNetWorkUI(1);
        }else{          
            mWifiManager.setWifiEnabled(false);
            if(!getEthCheckBoxState())
            updateNetWorkUI(0);
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
            show_password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
         }else{
            show_password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_uncheck, 0, 0, 0);
         }
    }
    
    private void updateEthCheckBoxUI(){         
         if(getEthCheckBoxState()){
            select_ethernet.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
         }else{
            select_ethernet.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_uncheck, 0, 0, 0);
         }
    }
    private void setWifiCheckBoxSwitch(){
       
        if(getWifiCheckBoxState()){
            WifiUtils.setPassWord(null);                    
            WifiUtils.setApName(null);
            enableWifiView(false);             
        }else{
            enableWifiView(true);
        }
	    upDateWifiCheckBoxUI();
	    select_wifi.setEnabled(false);//clei for wifi
        updateEthCheckBoxUI();
    }
    private void upDateWifiCheckBoxUI(){
         if(getWifiCheckBoxState()){
            select_wifi.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
         }else{
            select_wifi.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_uncheck, 0, 0, 0);
         }
         select_wifi.setEnabled(true);//clei for wifi
         
    }
    private void updateNetWorkUI(int type){
        if (Utils.DEBUG) Log.d(TAG,"===== updateNetWorkUI() 001");
        if(type == 0){
            if (Utils.DEBUG) Log.d(TAG,"===== updateNetWorkUI() 002");
            net_root_view.setVisibility(View.VISIBLE);
            root_eth_view.setVisibility(View.GONE);
            root_wifi_view.setVisibility(View.GONE);
        }else if(type == 1){
            if (Utils.DEBUG) Log.d(TAG,"===== updateNetWorkUI() 003");
            net_root_view.setVisibility(View.GONE);
            root_eth_view.setVisibility(View.GONE);
            root_wifi_view.setVisibility(View.VISIBLE);
              
            if(mWifiManager.isWifiEnabled()){
                showWifiConnectedView();
            }else{
                showWifiDisconnectedView();
            }            
        }else if(type == 2){
            if (Utils.DEBUG) Log.d(TAG,"===== updateNetWorkUI() 004");
            net_root_view.setVisibility(View.GONE);
            root_eth_view.setVisibility(View.VISIBLE);
            root_wifi_view.setVisibility(View.GONE);
            upDateEthernetInfo();
        }else{
            if (Utils.DEBUG) Log.d(TAG,"===== updateNetWorkUI() 005");
            net_root_view.setVisibility(View.VISIBLE);
            root_eth_view.setVisibility(View.GONE);
            root_wifi_view.setVisibility(View.GONE);
        }

    }
    
    private class WifiScanner extends Handler {
        private int mRetry = 0;

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            if (!mWifiManager.isWifiEnabled())
                return;
            
            if (mWifiManager.startScan()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }

    private void updateWifiState(int state) {
        Log.d(TAG, "######################### state="+ state);
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLED:
                mWifiScanner.resume();
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                break;
            case WifiManager.WIFI_STATE_DISABLED:
               select_wifi.setEnabled(true);//clei for wifi
                break;
        }
    }
	private void showWifiConnectedView() {
		wifi_listview_tip.setVisibility(View.GONE);
		mAcessPointListView.setVisibility(View.VISIBLE);
        mAccessPointListAdapter.updateAccesspointList();

        boolean isWifiConnected = WifiUtils.isWifiConnected(mContext);
        if(isWifiConnected){
    		wifi_input_password.setVisibility(View.GONE);
    		wifi_not_connect.setVisibility(View.GONE);
    		wifi_connected.setVisibility(View.VISIBLE);

    		DhcpInfo mDhcpInfo = mWifiManager.getDhcpInfo();
    		WifiInfo mWifiinfo = mWifiManager.getConnectionInfo();

    		if (mWifiinfo != null) {
    			wifi_ssid_value.setVisibility(View.VISIBLE);
    			String wifi_name = mWifiinfo.getSSID().substring(1,mWifiinfo.getSSID().length() - 1);
    			wifi_ssid_value.setText(wifi_name);
    			ip_address_value.setText(int2ip(mWifiinfo.getIpAddress()));
    			//mAccessPointListAdapter.setCurrentConnectedItemBySsid(mWifiinfo.getSSID());
                mAccessPointListAdapter.setCurrentConnectItemSSID(mWifiinfo.getSSID());
    		}
        }
	}

    private String getDeviceIpAddress(){
        boolean isEthConnected = WifiUtils.isEthConnected(mContext);
        boolean isWifiConnected = WifiUtils.isWifiConnected(mContext);
        if(isWifiConnected){
            //WifiInfo mWifiinfo = mWifiManager.getConnectionInfo();
            String ipAddress = null;
            //if(mWifiinfo != null){
                //ipAddress = int2ip(mWifiinfo.getIpAddress());
                //Log.d(TAG,"==== wifi ipAddress : " + ipAddress);
            //}
            ipAddress = getLocalIpAddress();
            return ipAddress;   
        }else if(isEthConnected){
            String ipAddress = getLocalIpAddress();
            //DhcpInfo mDhcpInfo = mEthernetManager.getDhcpInfo();
            //if (mDhcpInfo != null) {
				//int ip = mDhcpInfo.ipAddress;
                //String ipAddress = int2ip(ip);
               
				//Log.d(TAG,"==== wifi ipAddress : " + ipAddress);
				return ipAddress;
			//}
        }
            return null;
    }

	@Override
	protected void onResume() {
		super.onResume();
        wifiResume();
        getOpenId();
        if (Utils.DEBUG) Log.d(TAG,"===== goToIndex : " + goToIndex);
        goToSpecifiedScreen();

        Log.d(TAG,"===== onResume()");
        boolean value = sharepreference.getBoolean("resume_show_dialog",false);
        if(value){
            showDisplayDialog();
            setSharedPrefrences("resume_show_dialog",false);
        }
		
		
	}

	public String int2ip(long ipInt) {
		StringBuilder sb = new StringBuilder();
		sb.append(ipInt & 0xFF).append(".");
		sb.append((ipInt >> 8) & 0xFF).append(".");
		sb.append((ipInt >> 16) & 0xFF).append(".");
		sb.append((ipInt >> 24) & 0xFF);
		return sb.toString();
	}

	private void upDateEthernetInfo() {
		if (Utils.DEBUG) Log.d(TAG, "===== update ethernet info ");
        boolean isEthConnected = WifiUtils.isEthConnected(mContext);
        
		if (isEthConnected){
            if(eth_ip_layout != null && wifi_connected != null){
                eth_ip_layout.setVisibility(View.VISIBLE);
                wifi_connected.setVisibility(View.VISIBLE);
            }
			   
			//DhcpInfo mDhcpInfo = mEthernetManager.getDhcpInfo();
			//if (mDhcpInfo != null) {
				//int ip = mDhcpInfo.ipAddress;
                if(eth_connected_tip != null){
				    eth_connected_tip.setText(R.string.eth_connectd);
                }
				if(eth_IP_value != null){
				    //eth_IP_value.setText(int2ip(ip));
				    eth_IP_value.setText(getLocalIpAddress());
                }
				else {
					Log.d(TAG,"=====  eth_IP_value is null !!!");
				}	
			//}
		} else {
		    if(eth_connected_tip != null && eth_ip_layout != null){
			    eth_connected_tip.setText(R.string.ethernet_error);
			    eth_ip_layout.setVisibility(View.GONE);
            }
		}

	}

	private void showPasswordView() {
		wifi_connected.setVisibility(View.GONE);
		wifi_not_connect.setVisibility(View.GONE);
		wifi_input_password.setVisibility(View.VISIBLE);
		password_editview.requestFocus();

	}

	private void showConnectingView() {
        select_wifi.requestFocus();
		wifi_connected.setVisibility(View.GONE);
		wifi_input_password.setVisibility(View.GONE);
		wifi_not_connect.setVisibility(View.VISIBLE);
		wifi_slect_tip.setText(R.string.connectting_wifi_tips);
	}

	private void showWifiDisconnectedView() {
        mAcessPointListView.setVisibility(View.GONE);
		wifi_connected.setVisibility(View.GONE);
		wifi_input_password.setVisibility(View.GONE);
		wifi_not_connect.setVisibility(View.VISIBLE);
		mAccessPointListAdapter.updateAccesspointList();
		wifi_slect_tip.setText(R.string.wifi_ap_select);
	}

    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
        Log.d(TAG,"===== onConfigurationChanged(), newConfig:"+newConfig.toString());
	}

	@Override
	protected void onDestroy() {
	    if (myReceiver != null) {
            unregisterReceiver(myReceiver);
            myReceiver = null;
	    }
	    if(popupWindow!=null){
    	    popupWindow.dismiss();
            popupWindow = null;
        }
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
        dismissDisplayDiglog();
        mWifiScanner.pause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onClick(View v) {
        if (Utils.DEBUG) Log.d(TAG,"===== onClick()");
        
        if (mOutPutModeManager.ifModeIsSetting()){
            return;
        }
        
		int id = v.getId();

		if (v instanceof TextView) {
            if(id == R.id.screen_time_01){
                slectKeepScreenIndex(0);
            }else if(id == R.id.screen_time_02){
                slectKeepScreenIndex(1);
            }else if(id == R.id.screen_time_03){
                slectKeepScreenIndex(2);
            }else if(id == R.id.screen_time_04){
                slectKeepScreenIndex(3);
            }
		}

		if (v instanceof LinearLayout) {
			if (id == R.id.button_scrren_adjust) {
                if (SystemProperties.getBoolean(HDMIIN_SWITCH_FULL_PROP, false) && !SystemProperties.getBoolean(HDMIIN_MAIN_WINDOW_FULL_PROP, true))
                    showHdmiinPromptDialog();
                else
                    openScreenAdjustLayout();
            }else if (id == R.id.screen_self_set) {
                if (SystemProperties.getBoolean(HDMIIN_SWITCH_FULL_PROP, false) && !SystemProperties.getBoolean(HDMIIN_MAIN_WINDOW_FULL_PROP, true))
                    showHdmiinPromptDialog();
                else
                    openOutPutModePopupWindow("hdmi");
            }else if (id == R.id.cvbs_screen_self_set){
                openOutPutModePopupWindow("cvbs");
            }
		}

        if (v instanceof RelativeLayout) {
            if( !isOpenAdjustScreenView)
                setViewVisable((RelativeLayout)v);			
		}

        if (v instanceof ImageButton) {
			if (id == R.id.btn_position_zoom_in) {
                if (screen_rate > MIN_Height) {
                    showProgressUI(-1);
					//mScreenPositionManager.zoomOut();
					mScreenPositionManager.zoomByPercent(screen_rate);
				}               
			}else if(id == R.id.btn_position_zoom_out){
                if(screen_rate < MAX_Height){
				    showProgressUI(1);
			        //mScreenPositionManager.zoomIn();
			        mScreenPositionManager.zoomByPercent(screen_rate);
                }
            }
		}

	}

	private void openScreenAdjustLayout() {
        isOpenAdjustScreenView = true ; 
		mCurrentContentNum = VIEW_SCREEN_ADJUST;
		//preFocusView = settingsTopView_02;
		//settingsTopView_02.requestFocus();
		settingsContentLayout_02.setVisibility(View.GONE);

		settings_content_postion.setVisibility(View.VISIBLE);
		btn_position_zoom_out = (ImageButton) findViewById(R.id.btn_position_zoom_out);
        btn_position_zoom_out.setOnClickListener(this);
		btn_position_zoom_in = (ImageButton) findViewById(R.id.btn_position_zoom_in);
        btn_position_zoom_in.setOnClickListener(this);
        TextView screen_tip_01 = (TextView)findViewById(R.id.screen_tip_01);
		screen_tip_01.requestFocus();
        screen_tip_01.requestFocusFromTouch();
		// btn_position_zoom_out.setBackgroundResource(R.drawable.plus_focus);
		mScreenPositionManager = new ScreenPositionManager(this);
		mScreenPositionManager.initPostion();
		screen_rate = mScreenPositionManager.getRateValue();
		showProgressUI(0);
		//ImageView around_line = (ImageView) findViewById(R.id.screen_adjust_line);
		//around_line.setVisibility(View.VISIBLE);
         
	}

	private void closeScreenAdjustLayout() {
        isOpenAdjustScreenView = false ; 
		
		mCurrentContentNum = VIEW_DISPLAY;
		settingsTopView_02.requestFocus();
		settings_content_postion.setVisibility(View.GONE);
		settingsContentLayout_02.setVisibility(View.VISIBLE);
		button_scrren_adjust.requestFocus();
		//ImageView around_line = (ImageView) findViewById(R.id.screen_adjust_line);
		//around_line.setVisibility(View.GONE);
        if(mScreenPositionManager.isScreenPositionChanged()){
            mScreenPositionManager.savePostion();
            ScreenPositionManager.mIsOriginWinSet = false;    //user has changed&save postion,reset this prop to default 
            //restartActivitySelf();
        }
       
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (Utils.DEBUG) Log.d(TAG, "onKeyDown(),keyCode : " + keyCode);
        if (Utils.DEBUG) Log.d(TAG, "isOpenAdjustScreenView : " + isOpenAdjustScreenView);

        if (mOutPutModeManager.ifModeIsSetting()){
            return true;
        }
        
		if (isOpenAdjustScreenView) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				btn_position_zoom_in.setBackgroundResource(R.drawable.minus_unfocus);
				btn_position_zoom_out.setBackgroundResource(R.drawable.plus_focus);
                if(screen_rate < MAX_Height){
                    if (Utils.DEBUG) Log.d(TAG,"==== zoomIn ,screen_rate="+screen_rate);
				    showProgressUI(1);
			        //mScreenPositionManager.zoomIn();
                    mScreenPositionManager.zoomByPercent(screen_rate);
				}

			} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				if (screen_rate > MIN_Height) {
				    if (Utils.DEBUG) Log.d(TAG,"==== zoomOut,screen_rate="+screen_rate);
                    showProgressUI(-1);
					//mScreenPositionManager.zoomOut();
					mScreenPositionManager.zoomByPercent(screen_rate);
				}
				btn_position_zoom_in.setBackgroundResource(R.drawable.minus_focus);
				btn_position_zoom_out.setBackgroundResource(R.drawable.plus_unfocus);
			} else if (keyCode == KeyEvent.KEYCODE_BACK
					|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
				closeScreenAdjustLayout();
			}else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                return true;
            }
			return true;
		}

		if (screen_keep.isFocused()) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				slectKeepScreenIndex(true);
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				slectKeepScreenIndex(false);
				return true;
			}
		}

		if (city_select.isFocused()) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
					|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				if (mCrrentLocationfocus == 0) {
					mCrrentLocationfocus = 1;
					province_view.setBackgroundResource(Color.TRANSPARENT);
					city_view.setBackgroundResource(R.drawable.select_focused);
				} else {
					mCrrentLocationfocus = 0;
					city_view.setBackgroundResource(Color.TRANSPARENT);
					province_view.setBackgroundResource(R.drawable.select_focused);
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setScreenTimeOut(int index) {
		int value = 0;
		if (index == 0) {
			value = TIME_MAX_MIN;
		} else if (index == 1) {
			value = TIME_4_MIN;
		} else if (index == 2) {
			value = TIME_8_MIN;
		} else if (index == 3) {
			value = TIME_12_MIN;
		}
		if (Utils.DEBUG) Log.d(TAG, "===== set time out is : " + value);
		Settings.System.putInt(getContentResolver(), "screen_off_timeout",value);
        Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
        editor.putInt("screen_timeout", value);
	    editor.commit();   
    }

    private void slectKeepScreenIndex(int index){
        mCurrentScreenKeepIndex = index;
        for(int i=0 ; i<=3 ;i++){
            if(i == index){
                mScreenKeepTimes[i].setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_checked, 0);
            }else{
                    mScreenKeepTimes[i].setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_uncheck, 0);
            }  
        }
        setScreenTimeOut(mCurrentScreenKeepIndex);
    }

	private void slectKeepScreenIndex(boolean isLeft) {
		if (isLeft) {
			if (mCurrentScreenKeepIndex == 0) {

				mScreenKeepTimes[0].setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_uncheck, 0);
				mScreenKeepTimes[mScreenKeepTimes.length - 1]
                    .setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_checked, 0);
				mCurrentScreenKeepIndex = mScreenKeepTimes.length - 1;

			} else {

				mScreenKeepTimes[mCurrentScreenKeepIndex - 1]
						.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_checked, 0);
				mScreenKeepTimes[mCurrentScreenKeepIndex]
						.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_uncheck, 0);
				--mCurrentScreenKeepIndex;
			}

		} else {

			if (mCurrentScreenKeepIndex == mScreenKeepTimes.length - 1) {
				mScreenKeepTimes[0].setCompoundDrawablesWithIntrinsicBounds(0,
						0, R.drawable.ic_checked, 0);
				mScreenKeepTimes[mScreenKeepTimes.length - 1]
						.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_uncheck, 0);
				mCurrentScreenKeepIndex = 0;
			} else {

				mScreenKeepTimes[mCurrentScreenKeepIndex + 1]
						.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_checked, 0);
				mScreenKeepTimes[mCurrentScreenKeepIndex]
						.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_uncheck, 0);
				++mCurrentScreenKeepIndex;
			}

		}
		setScreenTimeOut(mCurrentScreenKeepIndex);
	}

	private void showProgressUI(int step) {
        screen_rate = screen_rate + step;
        if(screen_rate >MAX_Height){
            screen_rate = MAX_Height;
        }
        if(screen_rate <MIN_Height){
            screen_rate = MIN_Height ;
        }
        if (Utils.DEBUG) Log.d(TAG,"===== showProgressUI() ,screen_rate="+ screen_rate);
		if (screen_rate ==100) {
			int hundred = Num[(int) screen_rate / 100];
			img_num_hundred.setVisibility(View.VISIBLE);
			img_num_hundred.setBackgroundResource(hundred);
            int ten = Num[(screen_rate -100)/10] ;
			img_num_ten.setBackgroundResource(ten);
            int unit = Num[(screen_rate -100)%10];
			img_num_unit.setBackgroundResource(unit);
			if (screen_rate - MIN_Height>= 0 && screen_rate - MIN_Height <= 20)
				img_progress_bg.setBackgroundResource(progressNum[screen_rate - MIN_Height-1]);
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

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (Utils.DEBUG) Log.d(TAG,"===== onKeyUp(), keyCode : " + keyCode);

        if (mOutPutModeManager.ifModeIsSetting()){
            return true;
        }
		if (mCurrentContentNum == VIEW_SCREEN_ADJUST) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				btn_position_zoom_in.setBackgroundResource(R.drawable.minus_unfocus);
				btn_position_zoom_out.setBackgroundResource(R.drawable.plus_unfocus);
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				btn_position_zoom_in.setBackgroundResource(R.drawable.minus_unfocus);
				btn_position_zoom_out.setBackgroundResource(R.drawable.plus_unfocus);
			}
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent instanceof ListView) {
			onClickAccessPoint(position);
		}

	}

	private void onClickAccessPoint(int index) {

		mAccessPointListAdapter.setCurrentSelectItem(index);
		currentAP = mAccessPointListAdapter.getCurrentAP().wifiSsid.toString();
		currentSelectAccessPoint = mAccessPointListAdapter.getCurrentAP();
		int securityType = mAccessPointListAdapter.getCurrentAccessPointSecurityType(index);
		if (Utils.DEBUG) Log.d(TAG, "===== securityType :  " + securityType);


		if (securityType == 0 || securityType == 4) {
			showConnectingView();
			mAccessPointListAdapter.connect2OpenAccessPoint();
		} else {
		    String currentApName =WifiUtils.removeDoubleQuotes( mAccessPointListAdapter.getCurrentAP().SSID);
            String mWifiConnectedInfo = sharepreference.getString("wifi_connected_info", "***");
            if (Utils.DEBUG) Log.d(TAG, "===== mWifiConnectedInfo :  " + mWifiConnectedInfo);
            password_editview.setText("");
            if(mWifiConnectedInfo.contains(currentApName)){
                String[] values = mWifiConnectedInfo.split(",");
                        for (int i = 0; i < values.length; i++) {
                            if (values[i].contains(currentApName)) {
                                String[] temp = values[i].split(":");
                                if(temp.length == 2){
                                    String apPassword = temp[1];
                                    password_editview.setText(apPassword);
                                }else{
                                    Log.d(TAG, "===== error : password is null !!!");
                                }
                                //Log.d(TAG, "===== apPassword :  " + apPassword);
                                break;
                            } 
                        }       
            }
			showPasswordView();
		}

	}

	class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,Context.MODE_PRIVATE).edit();
            if (Utils.DEBUG) Log.e(TAG, "action : " + action);
            if(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action)){
                    if (Utils.DEBUG) Log.e(TAG, "CONFIGURED_NETWORKS_CHANGED_ACTION");
                    Bundle b =	intent.getExtras();
                    WifiConfiguration reason = (WifiConfiguration) b.get("wifiConfiguration");
                    if(reason!=null){
                         int result =  reason.disableReason ;
                         if(result == 3){
                            if (Utils.DEBUG) Log.e(TAG, "connect error");
                            endTime = System.currentTimeMillis();
                            if(endTime - startTime > 10000){
                                wifi_slect_tip.setText(R.string.connect_error_tips);
                                if (Utils.DEBUG) Log.e(TAG, "show connect error notices");
                            }      
                         }
                    }   
                    
            }if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) { 
                 updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));

            }else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action) ||
                WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action) ||
                WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {
                select_wifi.setEnabled(true);//clei for wifi
                mAccessPointListAdapter.updateAccesspointList();
                if (mAccessPointListAdapter.getCount() <= 0) {
                    mAcessPointListView.setVisibility(View.GONE);
                    wifi_listview_tip.setVisibility(View.VISIBLE);
                } else {
                    wifi_listview_tip.setVisibility(View.GONE);
                    mAcessPointListView.setVisibility(View.VISIBLE);
                }
            }else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
				mThisDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
				if (Utils.DEBUG) Log.d(TAG, "miracast device name: " + mThisDevice.deviceName);
                editor.putString("miracast_device_name", mThisDevice.deviceName);
			    editor.commit(); 
                
			}else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {  

                ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (Utils.DEBUG) Log.d(TAG,"===== wifi.getState()  : " +  wifi.getState());
                int type = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, ConnectivityManager.TYPE_NONE);
                NetworkInfo info = (NetworkInfo)intent.getExtra(ConnectivityManager.EXTRA_NETWORK_INFO, null);
                if (Utils.DEBUG) Log.d(TAG, "***receive CONNECTIVITY_CHANGE extra:  type="+type+",  networkinfo="+info);
                
                //if(mCurrentContentNum == VIEW_NETWORK){
                    boolean isWifiConnected = WifiUtils.isWifiConnected(mContext);
                    boolean isEthConnected = WifiUtils.isEthConnected(mContext);
                    if (Utils.DEBUG) Log.e(TAG, "===== connectivity changed ");
                    if(isEthConnected && getEthCheckBoxState()){
                        if (Utils.DEBUG) Log.e(TAG, "===== ethernet connectd ");
                        updateNetWorkUI(2);
                        updateEthCheckBoxUI();   
                        upDateWifiCheckBoxUI();
                        mEthConnectingFlag = false;
                    }else if(isWifiConnected && getWifiCheckBoxState()){
                         updateNetWorkUI(1);
                         if (Utils.DEBUG) Log.e(TAG, "===== wifi connectd ");
                    }else if(!getWifiCheckBoxState() && ! getEthCheckBoxState()){
                         updateNetWorkUI(0);
                         mEthernetManager.setEthEnabled(false); 
                         updateEthCheckBoxUI();   
                         upDateWifiCheckBoxUI();
                         mEthConnectingFlag = false;
                         if (Utils.DEBUG) Log.e(TAG, "===== ethernet and wifi are disconnected ");
                    } else if(getEthCheckBoxState() && (type == ConnectivityManager.TYPE_ETHERNET)){
                         if((info.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) || (info.getDetailedState() == NetworkInfo.DetailedState.FAILED)) {
                            if(!mEthConnectingFlag)
                                updateNetWorkUI(2);
                            mEthConnectingFlag = false;
                         }
                    //     setEthCheckBoxSwitch();
                    }
                //}
                 if (Utils.DEBUG) Log.e(TAG, "===== onReceive() 006");
                
                //if(mCurrentContentNum == VIEW_DISPLAY){
                    String isOpenRemoteContrl = sharepreference.getString("open_remote_control", "false");
                    if("true".equals(isOpenRemoteContrl)){
                        String ip = getDeviceIpAddress();
                        if(ip!=null){
                            remoteControlIp.setText(ip);
                        }
                    }
               // }             
                 
		    }else if(WindowManagerPolicy.ACTION_HDMI_MODE_CHANGED.equals(action)){
                if (popupWindow != null)
                    popupWindow.dismiss();
                upDateOutModeUi();
                
            }else if(WindowManagerPolicy.ACTION_HDMI_HW_PLUGGED.equals(action)){
                boolean plugged = intent.getBooleanExtra(WindowManagerPolicy.EXTRA_HDMI_HW_PLUGGED_STATE, false); 
                if(plugged){
                    if (Utils.DEBUG) Log.d(TAG,"===== himi plugged");
                    secreen_auto.setFocusable(true);
                    secreen_auto.setClickable(true);
                    auto_set_screen.setTextColor(Color.WHITE);
                    boolean isAutoHdmiMode = getAutoHDMIMode();
                    if(!isAutoHdmiMode){
                        screen_self_set.setFocusable(true);
                        screen_self_set.setClickable(true);
                        self_select_mode.setTextColor(Color.WHITE);
                        current_mode_value.setTextColor(Color.WHITE);
                    }else{
                        screen_self_set.setFocusable(false);
                        screen_self_set.setClickable(false);
                        self_select_mode.setTextColor(Color.GRAY);
                        current_mode_value.setTextColor(Color.GRAY);
                    }
                    cvbs_screen_self_set.setVisibility(View.GONE);
                    current_mode_value.setText(mOutPutModeManager.getCurrentOutPutModeTitle(1)); 

                    updateVoiceUi();
                }else{
                    if (Utils.DEBUG) Log.d(TAG,"===== himi unplugged");

                    cvbs_screen_self_set.setVisibility(View.VISIBLE);
                    secreen_auto.setFocusable(false);
                    secreen_auto.setClickable(false);
                    screen_self_set.setFocusable(false);
                    screen_self_set.setClickable(false);
                    self_select_mode.setTextColor(Color.GRAY);
                    current_mode_value.setTextColor(Color.GRAY);
                    auto_set_screen.setTextColor(Color.GRAY);
                    cvbs_current_mode_value.setText(mOutPutModeManager.getCurrentOutPutModeTitle(0));     
                }
                
            }else if("action.show.dialog".equals(action)){
                if (Utils.DEBUG) Log.d(TAG,"===== action.show.dialog");
                MyHandle mHander = new MyHandle();
                Message msg = mHander.obtainMessage();
                msg.what = SHOW_CONFIRM_DIALOG ;
                mHander.sendMessage(msg);
            }
		}
	}


    void setSharedPrefrences(String key ,boolean value) {
		Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,
				Context.MODE_PRIVATE).edit();
		editor.putBoolean(key, value);
		editor.commit();
	}


	private void openOutPutModePopupWindow(final String mode) {
        
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View outPutView = (View) mLayoutInflater.inflate(R.layout.out_mode_popup_window, null, true);

		ListView listview = (ListView) outPutView.findViewById(R.id.output_list);
		final OutPutModeManager output = new OutPutModeManager(mContext, listview, mode);
        output.setHandler(mHander);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int index, long id) {
                if(index == output.getCurrentModeIndex()){
                    return ;
                }  
                popupWindow.dismiss();
                output.selectItem(index);
			}
		});

		popupWindow = new PopupWindow(outPutView, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.showAtLocation(outPutView, Gravity.CENTER, 0, 0);
		popupWindow.update();

	}

    void updateVoiceUi() {

		String isAutoVoice = sharepreference.getString("auto_voice", "false");
        TextView tx_voice_auto = (TextView)findViewById(R.id.tx_voice_auto);
        TextView tx_voice_setting = (TextView)findViewById(R.id.tx_voice_setting);
        TextView tx_voice_mode = (TextView)findViewById(R.id.tx_voice_mode);
        tx_voice_mode.setTextColor(Color.GRAY);
        
		if (isAutoVoice.equals("true")) {
			voice_setting.setFocusable(false);
			voice_setting.setClickable(false);
			tx_voice_setting.setTextColor(Color.GRAY);
			tx_voice_auto.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.on, 0);
            int mode = mMboxOutputModeManager.autoSwitchHdmiPassthough();
            
            switch (mode){
                case 0:
                    tx_voice_mode.setText("PCM");
                    break;
                case 1:
                    tx_voice_mode.setText("SPDIF passthrough");
                    break;
                case 2:
                    tx_voice_mode.setText("HDMI passthrough");
                    break;
                default:
                    tx_voice_mode.setText("PCM");
                    break; 
            }
		} else {
			tx_voice_auto.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.off, 0);
			voice_setting.setFocusable(true);
			voice_setting.setClickable(true);
			tx_voice_setting.setTextColor(Color.WHITE);

            String []value = sw.getProperty("ubootenv.var.digitaudiooutput").split(":");
            if (value[0] != null && value[0].length() != 0){
                mMboxOutputModeManager.setDigitalVoiceValue(value[0]);
                tx_voice_mode.setText(value[0]);
            }else {
                mMboxOutputModeManager.setDigitalVoiceValue("PCM");
                tx_voice_mode.setText("PCM");
            }
		}
    }

    private void setAutoVoice() {
		String isAuto = sharepreference.getString("auto_voice", "false");
		Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,
				Context.MODE_PRIVATE).edit();
		if (isAuto.equals("true")) {
			editor.putString("auto_voice", "false");
			editor.commit();
		} else {
			editor.putString("auto_voice", "true");
			editor.commit();
        }

		updateVoiceUi();
    }
    
	private void openVoicePopupWindow() {
        
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View voicePopupView = (View) mLayoutInflater.inflate(R.layout.voice_popup_window, null, true);
		upDateDigitaVoiceUi(voicePopupView);
        
		RelativeLayout pcm = (RelativeLayout) voicePopupView.findViewById(R.id.voice_pcm);
		pcm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMboxOutputModeManager.setDigitalVoiceValue(string_pcm);
				upDateDigitaVoiceUi(voicePopupView);

			}
		});
        
		RelativeLayout voice_sddif = (RelativeLayout) voicePopupView.findViewById(R.id.voice_sddif);
        boolean displaySpdif = SystemProperties.getBoolean("ro.hdmi.spdif", false);
        voice_sddif.setVisibility(displaySpdif?View.VISIBLE:View.GONE);
		voice_sddif.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { 
				mMboxOutputModeManager.setDigitalVoiceValue(string_spdif);
				upDateDigitaVoiceUi(voicePopupView);
			}
		});
        
		RelativeLayout voice_hdmi = (RelativeLayout) voicePopupView.findViewById(R.id.voice_hdmi);
		voice_hdmi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMboxOutputModeManager.setDigitalVoiceValue(string_hdmi);
				upDateDigitaVoiceUi(voicePopupView);
			}
		});

		PopupWindow voicePopupWindow = new PopupWindow(voicePopupView,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		voicePopupWindow.setBackgroundDrawable(new BitmapDrawable());
		voicePopupWindow.showAtLocation(voicePopupView, Gravity.CENTER, 0, 0);
		voicePopupWindow.update();

	}
    
    private void openDolbyPopupWindow() {
        
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View dolbyPopupView = (View) mLayoutInflater.inflate(R.layout.dolby_popup_window, null, true);
		setDolbySettingsUI(dolbyPopupView);

		RelativeLayout drc_enable = (RelativeLayout) dolbyPopupView.findViewById(R.id.dolby_drc_enable);
		drc_enable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateDolbySettingsUi(0, dolbyPopupView);

                if (Utils.DEBUG) Log.d(TAG, "@@@@@@@@@@@@@@@@@ drc_enable.setOnClickListener");
			}
		});
        
		RelativeLayout drc_mode = (RelativeLayout) dolbyPopupView.findViewById(R.id.dolby_drc_mode);
		drc_mode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { 
				updateDolbySettingsUi(1, dolbyPopupView);
                if (Utils.DEBUG) Log.d(TAG, "@@@@@@@@@@@@@@@@@ drc_mode.setOnClickListener");
			}
		});
        

		PopupWindow dolbyPopupWindow = new PopupWindow(dolbyPopupView,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		dolbyPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		dolbyPopupWindow.showAtLocation(dolbyPopupView, Gravity.CENTER, 0, 0);
		dolbyPopupWindow.update();

	}

    private void openDtsPopupWindow() {
        
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View dtsPopupView = (View) mLayoutInflater.inflate(R.layout.dts_popup_window, null, true);
		setDtsSettingsUI(dtsPopupView);

		RelativeLayout dts_downmix_mode = (RelativeLayout) dtsPopupView.findViewById(R.id.dts_downmix_mode);
		dts_downmix_mode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateDtsSettingsUi(0, dtsPopupView);

			}
		});
        
		RelativeLayout drc_scale = (RelativeLayout) dtsPopupView.findViewById(R.id.dts_drc_scale);
		drc_scale.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { 
				updateDtsSettingsUi(1, dtsPopupView);
			}
		});

        RelativeLayout dial_norm = (RelativeLayout) dtsPopupView.findViewById(R.id.dts_dial_norm);
		dial_norm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { 
				updateDtsSettingsUi(2, dtsPopupView);
			}
		});    

		PopupWindow dtsPopupWindow = new PopupWindow(dtsPopupView,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		dtsPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		dtsPopupWindow.showAtLocation(dtsPopupView, Gravity.CENTER, 0, 0);
		dtsPopupWindow.update();
	}

    private void openDtsTransPopupWindow() {
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View dtsTransPopupView = (View) mLayoutInflater.inflate(R.layout.dts_trans_popup_window, null, true);
		upDateDtsTransUi(dtsTransPopupView);
        
		RelativeLayout dts_trans_0 = (RelativeLayout) dtsTransPopupView.findViewById(R.id.dts_trans_0);
		dts_trans_0.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sw.setProperty("media.libplayer.dtsdecopt", "0");
				upDateDtsTransUi(dtsTransPopupView);

			}
		});
        
		RelativeLayout dts_trans_1 = (RelativeLayout) dtsTransPopupView.findViewById(R.id.dts_trans_1);
		dts_trans_1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { 
				sw.setProperty("media.libplayer.dtsdecopt", "1");
				upDateDtsTransUi(dtsTransPopupView);
			}
		});
        
		RelativeLayout dts_trans_2 = (RelativeLayout) dtsTransPopupView.findViewById(R.id.dts_trans_2);
		dts_trans_2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sw.setProperty("media.libplayer.dtsdecopt", "2");
				upDateDtsTransUi(dtsTransPopupView);
			}
		});

		PopupWindow dtsTransPopupWindow = new PopupWindow(dtsTransPopupView,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		dtsTransPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		dtsTransPopupWindow.showAtLocation(dtsTransPopupView, Gravity.CENTER, 0, 0);
		dtsTransPopupWindow.update();
	}

    private void setDtsMulAsset() {
        String isDtsMulAsset = sharepreference.getString("dts_mul_asset", "false");
        Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING,
        Context.MODE_PRIVATE).edit();
        if (isDtsMulAsset.equals("true")) {
            editor.putString("dts_mul_asset", "false");
            editor.commit();
        } else {
            editor.putString("dts_mul_asset", "true");
            editor.commit();
        }

        updateDtsMulAssetUi();
    }

    void updateDtsMulAssetUi() {
        String isDtsMulAsset = sharepreference.getString("dts_mul_asset", "false");
        TextView tx_dts_mul_asset = (TextView)findViewById(R.id.tx_dts_mul_asset);

        if (isDtsMulAsset.equals("true")) {
            tx_dts_mul_asset.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.on, 0);
            sw.setProperty("media.libplayer.dtsMulAsset", "true");
        } else {
            tx_dts_mul_asset.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.off, 0);
            sw.setProperty("media.libplayer.dtsMulAsset", "false");
        }
    }

    private void setDolbySettingsUI(View dolbyPopupView) {
		String enable = sharepreference.getString("dolby_drc_enable", "false");
        ImageView img_drc_enable = (ImageView)dolbyPopupView.findViewById(R.id.img_drc_enable);
        
		if (enable.equals("true")) {
            img_drc_enable.setBackgroundResource(R.drawable.on);
		} else {
            img_drc_enable.setBackgroundResource(R.drawable.off);
		}
        
        String mode = sharepreference.getString("dolby_drc_mode", "2");
        TextView tx_drc_mode = (TextView) dolbyPopupView.findViewById(R.id.tx_drc_mode);
        if (mode.equals("0")) {
            tx_drc_mode.setText(mContext.getResources().getString(R.string.dolby_drc_mode0));
        } else if (mode.equals("1")) {
            tx_drc_mode.setText(mContext.getResources().getString(R.string.dolby_drc_mode1));
        } else if (mode.equals("3")) {
            tx_drc_mode.setText(mContext.getResources().getString(R.string.dolby_drc_mode3));
        } else {
            tx_drc_mode.setText(mContext.getResources().getString(R.string.dolby_drc_mode2));
        }
        
    }

    private void setDtsSettingsUI(View dolbyPopupView) { 
        String mode = sharepreference.getString("dts_downmix_mode", "0");
        TextView tx_downmix_mode = (TextView) dolbyPopupView.findViewById(R.id.tx_downmix_mode);
        if (mode.equals("1")) {
            tx_downmix_mode.setText(mContext.getResources().getString(R.string.dts_downmix_mode1));
        } else {
            tx_downmix_mode.setText(mContext.getResources().getString(R.string.dts_downmix_mode0));
        }

        String drc_scale_enable = sharepreference.getString("dts_drc_scale", "false");
        ImageView img_drc_scale = (ImageView)dolbyPopupView.findViewById(R.id.img_drc_scale);
		if (drc_scale_enable.equals("true")) {
            img_drc_scale.setBackgroundResource(R.drawable.on);
		} else {
            img_drc_scale.setBackgroundResource(R.drawable.off);
		}

        String dial_norm_enable = sharepreference.getString("dts_dial_norm", "true");
        ImageView img_dial_norm = (ImageView)dolbyPopupView.findViewById(R.id.img_dial_norm);
		if (dial_norm_enable.equals("true")) {
            img_dial_norm.setBackgroundResource(R.drawable.on);
		} else {
            img_dial_norm.setBackgroundResource(R.drawable.off);
		}
    }

	void updateDolbySettingsUi(int option, View dolbyPopupView) {
        Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();

        if (option == 0) {
            String isDrcEnable = sharepreference.getString("dolby_drc_enable", "false");
    		mMboxOutputModeManager.enableDobly_DRC(!Boolean.parseBoolean(isDrcEnable));
             if (Utils.DEBUG) Log.d(TAG, "@@@@@@@@@@@@@@@@@ isDrcEnable="+isDrcEnable);
            
            ImageView img_drc_enable = (ImageView)dolbyPopupView.findViewById(R.id.img_drc_enable);
            if (isDrcEnable.equals("true")){
                img_drc_enable.setBackgroundResource(R.drawable.off);
                editor.putString("dolby_drc_enable", "false");
                editor.commit();
            } else {
                img_drc_enable.setBackgroundResource(R.drawable.on);
                editor.putString("dolby_drc_enable", "true");
    			editor.commit();
            }
        }else {
            String mode = sharepreference.getString("dolby_drc_mode", "2");
            if (Utils.DEBUG) Log.d(TAG, "@@@@@@@@@@@@@@@@@ mode="+mode);
            int mode_int = Integer.parseInt(mode);
            mode_int++;

            if(mode_int > 3)
                mode_int = 0;
            
    		mMboxOutputModeManager.setDoblyMode(String.valueOf(mode_int));
            
            TextView  tx_drc_enable = (TextView)dolbyPopupView.findViewById(R.id.tx_drc_mode);
             if (mode_int == 0) {
                tx_drc_enable.setText(mContext.getResources().getString(R.string.dolby_drc_mode0));
                editor.putString("dolby_drc_mode", "0");
                editor.commit();
            } else if (mode_int == 1) {
                tx_drc_enable.setText(mContext.getResources().getString(R.string.dolby_drc_mode1));
                editor.putString("dolby_drc_mode", "1");
                editor.commit();
            } else if (mode_int == 3) {
                tx_drc_enable.setText(mContext.getResources().getString(R.string.dolby_drc_mode3));
                editor.putString("dolby_drc_mode", "3");
                editor.commit();
            } else {
                tx_drc_enable.setText(mContext.getResources().getString(R.string.dolby_drc_mode2));
                editor.putString("dolby_drc_mode", "2");
                editor.commit();
            }
        }

	}

    void updateDtsSettingsUi(int option, View dtsPopupView) {
        Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();

        if (option == 0) {
            String mode = sharepreference.getString("dts_downmix_mode", "0");
            if (Utils.DEBUG) Log.d(TAG, "@@@@@@@@@@@@@@@@@ mode="+mode);
            int mode_int = Integer.parseInt(mode);
            mode_int++;

            if(mode_int > 1)
                mode_int = 0;
            
    		mMboxOutputModeManager.setDTS_DownmixMode(String.valueOf(mode_int));
            
            TextView  tx_drc_enable = (TextView)dtsPopupView.findViewById(R.id.tx_downmix_mode);
             if (mode_int == 1) {
                tx_drc_enable.setText(mContext.getResources().getString(R.string.dts_downmix_mode1));
                editor.putString("dts_downmix_mode", "1");
                editor.commit();
            } else {
                tx_drc_enable.setText(mContext.getResources().getString(R.string.dts_downmix_mode0));
                editor.putString("dts_downmix_mode", "0");
                editor.commit();
            }    
        }else if (option == 1){
            String isDrcScaleEnable = sharepreference.getString("dts_drc_scale", "false");
    		mMboxOutputModeManager.enableDTS_DRC_scale_control(!Boolean.parseBoolean(isDrcScaleEnable));
            
            ImageView img_drc_scale = (ImageView)dtsPopupView.findViewById(R.id.img_drc_scale);
            if (isDrcScaleEnable.equals("true")){
                img_drc_scale.setBackgroundResource(R.drawable.off);
                editor.putString("dts_drc_scale", "false");
                editor.commit();
            } else {
                img_drc_scale.setBackgroundResource(R.drawable.on);
                editor.putString("dts_drc_scale", "true");
    			editor.commit();
            }
        }else {
            String isDialNormEnable = sharepreference.getString("dts_dial_norm", "true");
    		mMboxOutputModeManager.enableDTS_Dial_Norm_control(!Boolean.parseBoolean(isDialNormEnable));
            
            ImageView img_dial_norm = (ImageView)dtsPopupView.findViewById(R.id.img_dial_norm);
            if (isDialNormEnable.equals("true")){
                img_dial_norm.setBackgroundResource(R.drawable.off);
                editor.putString("dts_dial_norm", "false");
                editor.commit();
            } else {
                img_dial_norm.setBackgroundResource(R.drawable.on);
                editor.putString("dts_dial_norm", "true");
    			editor.commit();
            }
        }

	}

    void upDateDtsTransUi(View dtsTransPopupView) {
        Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();

        ImageView imageview_trans_0 = (ImageView) dtsTransPopupView.findViewById(R.id.imageview_trans_0);
        ImageView imageview_trans_1 = (ImageView) dtsTransPopupView.findViewById(R.id.imageview_trans_1);
        ImageView imageview_trans_2 = (ImageView) dtsTransPopupView.findViewById(R.id.imageview_trans_2);
        TextView tx_dts_trans_mode = (TextView)findViewById(R.id.tx_dts_trans_mode);
        tx_dts_trans_mode.setTextColor(Color.GRAY);
        String value = sw.getProperty("media.libplayer.dtsdecopt");
        if ("0".equals(value)) {
            imageview_trans_0.setBackgroundResource(R.drawable.current_select);
            imageview_trans_1.setBackgroundResource(R.drawable.current_unselect);
            imageview_trans_2.setBackgroundResource(R.drawable.current_unselect);
            tx_dts_trans_mode.setText("0");
            editor.putString("dts_trans", "0");
        } else if ("1".equals(value)) {
            imageview_trans_0.setBackgroundResource(R.drawable.current_unselect);
            imageview_trans_1.setBackgroundResource(R.drawable.current_select);
            imageview_trans_2.setBackgroundResource(R.drawable.current_unselect);
            tx_dts_trans_mode.setText("1");
            editor.putString("dts_trans", "1");
        } else if ("2".equals(value)) {
            imageview_trans_0.setBackgroundResource(R.drawable.current_unselect);
            imageview_trans_1.setBackgroundResource(R.drawable.current_unselect);
            imageview_trans_2.setBackgroundResource(R.drawable.current_select);
            tx_dts_trans_mode.setText("2");
            editor.putString("dts_trans", "2");
        } else {
            sw.setProperty("media.libplayer.dtsdecopt", "0");
            tx_dts_trans_mode.setText("0");
            editor.putString("dts_trans", "0");
            imageview_trans_0.setBackgroundResource(R.drawable.current_select);
            imageview_trans_1.setBackgroundResource(R.drawable.current_unselect);
            imageview_trans_2.setBackgroundResource(R.drawable.current_unselect);
        }

        editor.commit();
    }

    void getDtsTransInit() {
        String dts_trans_mode = sharepreference.getString("dts_trans", "0");
        sw.setProperty("media.libplayer.dtsdecopt", dts_trans_mode);
        TextView tx_dts_trans_mode = (TextView)findViewById(R.id.tx_dts_trans_mode);
        tx_dts_trans_mode.setTextColor(Color.GRAY);
        tx_dts_trans_mode.setText(dts_trans_mode);
    }

    void upDateDigitaVoiceUi(View voicePopupView) {
		ImageView imageview_pcm = (ImageView) voicePopupView.findViewById(R.id.imageview_pcm);
		ImageView imageview_sddif = (ImageView) voicePopupView.findViewById(R.id.imageview_sddif);
		ImageView imageview_hdmi = (ImageView) voicePopupView.findViewById(R.id.imageview_hdmi);
		String value = sw.getProperty("ubootenv.var.digitaudiooutput");
		if ("PCM".equals(value)) {
			imageview_pcm.setBackgroundResource(R.drawable.current_select);
			imageview_sddif.setBackgroundResource(R.drawable.current_unselect);
			imageview_hdmi.setBackgroundResource(R.drawable.current_unselect);
		} else if ("SPDIF passthrough".equals(value)) {
			imageview_pcm.setBackgroundResource(R.drawable.current_unselect);
			imageview_sddif.setBackgroundResource(R.drawable.current_select);
			imageview_hdmi.setBackgroundResource(R.drawable.current_unselect);
		} else if ("HDMI passthrough".equals(value)) {
			imageview_pcm.setBackgroundResource(R.drawable.current_unselect);
			imageview_sddif.setBackgroundResource(R.drawable.current_unselect);
			imageview_hdmi.setBackgroundResource(R.drawable.current_select);
		} else {
			imageview_pcm.setBackgroundResource(R.drawable.current_select);
			imageview_sddif.setBackgroundResource(R.drawable.current_unselect);
			imageview_hdmi.setBackgroundResource(R.drawable.current_unselect);
			mMboxOutputModeManager.setDigitalVoiceValue("PCM");
		}

        updateVoiceUi();
	}



	private void openCECPopupWindow() {
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View cecView = (View) mLayoutInflater.inflate(
				R.layout.cec_popup_window, null, true);

		popupWindow = new PopupWindow(cecView, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);

		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		// popupWindow.setAnimationStyle();
		popupWindow.showAtLocation(cecView, Gravity.CENTER, 0, 0);
		popupWindow.update();

		upDateCecControlUi(cecView);

	}

	void upDateCecControlUi(final View view) {

		imageview_cec_main = (ImageView) view
				.findViewById(R.id.imageview_cec_main);
		imageview_cec_play = (ImageView) view
				.findViewById(R.id.imageview_cec_play);
		imageview_cec_power = (ImageView) view
				.findViewById(R.id.imageview_cec_power);
		imageview_cec_language = (ImageView) view
				.findViewById(R.id.imageview_cec_language);

		cec_main = (RelativeLayout) view.findViewById(R.id.cec_main);
		cec_main.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setCecMainSwitch(0);
			}
		});
		cec_play = (RelativeLayout) view.findViewById(R.id.cec_play);

		cec_play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setCecMainSwitch(1);
			}
		});

		cec_power = (RelativeLayout) view.findViewById(R.id.cec_power);
		cec_power.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setCecMainSwitch(2);
			}
		});
		cec_language = (RelativeLayout) view.findViewById(R.id.cec_language);
		cec_language.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setCecMainSwitch(3);
			}
		});

		upDateCecMainUi(0);
		upDateCecMainUi(1);
		upDateCecMainUi(2);
		upDateCecMainUi(3);

	}

	void setCecMainSwitch(int index) {

		if (index == 0) {
			String isCecOpen = sharepreference.getString("cec_open", "false");
			Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
			if (isCecOpen.equals("true")) {

				editor.putString("cec_open", "false");
				editor.putString("cec_play_open", "false");
				editor.putString("cec_power_open", "false");
				editor.putString("cec_language_open", "false");
				editor.commit();

			} else {
				editor.putString("cec_open", "true");
				editor.putString("cec_play_open", "true");
				editor.putString("cec_power_open", "true");
				editor.putString("cec_language_open", "true");
				editor.commit();
				if (!isMyServiceRunning()) {
					Intent serviceIntent = new Intent(mContext,CecCheckingService.class);
					serviceIntent.setAction("CEC_LANGUAGE_AUTO_SWITCH");
					mContext.startService(serviceIntent);
				}

			}
			upDateCecMainUi(0);

		} else if (index == 1) {
			String isCecPlayOpen = sharepreference.getString("cec_play_open","false");
			Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();

			if (isCecPlayOpen.equals("true")) {
				editor.putString("cec_play_open", "false");
				editor.commit();
			} else {
				editor.putString("cec_play_open", "true");
				editor.commit();
			}
			upDateCecMainUi(1);

		} else if (index == 2) {
			String isCecPowerOpen = sharepreference.getString("cec_power_open","false");
			Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
			if (isCecPowerOpen.equals("true")) {
				editor.putString("cec_power_open", "false");
				editor.commit();
			} else {
				editor.putString("cec_power_open", "true");
				editor.commit();
			}
			upDateCecMainUi(2);

		} else if (index == 3) {
			String isCecLanguageOpen = sharepreference.getString("cec_language_open", "false");
			Editor editor = mContext.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
			if (isCecLanguageOpen.equals("true")) {
				editor.putString("cec_language_open", "false");
				editor.commit();
			} else {
				editor.putString("cec_language_open", "true");
				editor.commit();

				if (!isMyServiceRunning()) {
					Intent serviceIntent = new Intent(mContext,CecCheckingService.class);
					serviceIntent.setAction("CEC_LANGUAGE_AUTO_SWITCH");
					mContext.startService(serviceIntent);
				}
			}
			upDateCecMainUi(3);
		}

	}

	public boolean isMyServiceRunning() {

		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.aml.settingsmbox.CecCheckingService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	void writeSysFile(final String file, final String value) {
		synchronized (file) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					sw.writeSysfs(file, value);
					Log.d(TAG, "===== write file : " + file + "  value : "+ value);
				}
			}).start();

		}

	}

	void upDateCecMainUi(int index) {
		
		String isCecLanguageOpen = sharepreference.getString(
				"cec_language_open", "false");

		if (index == 0) {
			String isCecOpen = sharepreference.getString("cec_open", "false");
			if (isCecOpen.equals("true")) {
				imageview_cec_main.setBackgroundResource(R.drawable.on);
				imageview_cec_play.setBackgroundResource(R.drawable.on);
				imageview_cec_power.setBackgroundResource(R.drawable.on);
				imageview_cec_language.setBackgroundResource(R.drawable.on);
				
				cec_play.setClickable(true);
				
				cec_power.setClickable(true);

				cec_language.setClickable(true);
				
				setCecSysfsValue(0, true);

			} else {
				imageview_cec_main.setBackgroundResource(R.drawable.off);
				imageview_cec_play.setBackgroundResource(R.drawable.off);
				imageview_cec_power.setBackgroundResource(R.drawable.off);
				imageview_cec_language.setBackgroundResource(R.drawable.off);				
				cec_play.setClickable(false);				
				cec_power.setClickable(false);
				cec_language.setClickable(false);				
				setCecSysfsValue(0, false);
			}

		} else if (index == 1) {
			String isCecPlayOpen = sharepreference.getString("cec_play_open","false");
			if (isCecPlayOpen.equals("true")) {
				imageview_cec_play.setBackgroundResource(R.drawable.on);
				setCecSysfsValue(1, true);
			} else {
				imageview_cec_play.setBackgroundResource(R.drawable.off);
				setCecSysfsValue(1, false);
			}

		} else if (index == 2) {
			String isCecPowerOpen = sharepreference.getString("cec_power_open","false");
			if (isCecPowerOpen.equals("true")) {
				imageview_cec_power.setBackgroundResource(R.drawable.on);
				setCecSysfsValue(2, true);

			} else {
				setCecSysfsValue(2, false);
				imageview_cec_power.setBackgroundResource(R.drawable.off);
			}

		} else if (index == 3) {
			if (isCecLanguageOpen.equals("true")) {
				imageview_cec_language.setBackgroundResource(R.drawable.on);
				setCecSysfsValue(3, true);
			} else {
				setCecSysfsValue(3, false);
				imageview_cec_language.setBackgroundResource(R.drawable.off);
			}

		}

		//update language immediate when cec_language_open is true 
		if (isCecLanguageOpen.equals("true")) {
			String curLanguage = Utils.readSysFile(sw,CecCheckingService.cec_device_file);
			//String curLanguage = sharepreference.getString("cec_language_switch_state",null);
	        Log.d(TAG,"update curLanguage:"+curLanguage);
			if(curLanguage==null)
				return;
			int i = -1;
			String[] cec_language_list = getResources().getStringArray(
					R.array.cec_language);
			for (int j = 0; j < cec_language_list.length; j++) {
				if (curLanguage != null
						&& curLanguage.trim().equals(cec_language_list[j])) {
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
					CecCheckingService.updateLanguage(l);
				}
			} else {
				Log.d(TAG, "the language code is not support right now !!!");
			}
		}
	}

	void setCecSysfsValue(int index, boolean value) {
		String cec_config = sw.getPropertyString(CEC_CONFIG, "cec0");
		String configString = Utils.getBinaryString(cec_config);
		int tmpArray[] = Utils.getBinaryArray(configString);

		if (index != 0) {
			if ("cec0".equals(sw.getProperty(CEC_CONFIG))) {
				return;
			}
		}

		if (index == 0) {
			if (value) {
				sw.setProperty(CEC_CONFIG, "cecf");
				writeSysFile(writeCecConfig, "f");
			} else {
				sw.setProperty(CEC_CONFIG, "cec0");
				writeSysFile(writeCecConfig, "0");
			}

		} else if (index == 1) {
			if (value) {
				tmpArray[2] = 1;
				tmpArray[0] = 1;
			} else {
				tmpArray[2] = 0;
				tmpArray[0] = 1;
			}
			String writeConfig = Utils.arrayToString(tmpArray);
			sw.setProperty(CEC_CONFIG, writeConfig);
			if (Utils.DEBUG) Log.d(TAG, "==== cec set config : " + writeConfig);
			String s = writeConfig.substring(writeConfig.length() - 1,
					writeConfig.length());

			writeSysFile(writeCecConfig, s);

		} else if (index == 2) {
			if (value) {
				tmpArray[1] = 1;
				tmpArray[0] = 1;
			} else {
				tmpArray[1] = 0;
				tmpArray[0] = 1;
			}
			String writeConfig = Utils.arrayToString(tmpArray);
			sw.setProperty(CEC_CONFIG, writeConfig);
			if (Utils.DEBUG) Log.d(TAG, "==== cec set config : " + writeConfig);
			String s = writeConfig.substring(writeConfig.length() - 1,writeConfig.length());
			writeSysFile(writeCecConfig, s);
		}else if(index == 3){
		 	String str = Utils.readSysFile(sw,writeCecConfig);
 
			str = str.substring(str.lastIndexOf(":")+3);
 			int cecValue = Integer.valueOf(str,16);

			if(value){
				 cecValue = cecValue|0x20;
			}else{
				 cecValue = cecValue&0xdf;
			} 
			str = Integer.toHexString(cecValue); 
			writeSysFile(writeCecConfig, str);
		}

	}

	void openLocationPopupWindow(final int flag) {
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View locationView = (View) mLayoutInflater.inflate(R.layout.location_popup_window, null, true);
		popupWindow = new PopupWindow(locationView, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
		final ListView listview = (ListView) locationView.findViewById(R.id.provinces_list);
		final LocationManager mLocationManager = new LocationManager(mContext);
		TextView location_title = (TextView) locationView.findViewById(R.id.location_title);
		if (flag == 0) {
			location_title.setText(R.string.provinces);
			mLocationManager.setProvinceView(listview);
		} else if (flag == 1) {
			location_title.setText(R.string.citys);
			mLocationManager.setCityView(listview);
		}

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int index, long id) {
				if (flag == 0) {
					mLocationManager.setCurrentProvinceIndex(index);
					province_view.setText(mLocationManager.getCurrentProvinceName());
					city_view.setText(mLocationManager.getCurrentCityNameNoIndex());
				} else {
				    mLocationManager.setCurrentCitysIndex(index);
					city_view.setText(mLocationManager.getCurrentCityNameByIndex(index));
				}
                popupWindow.dismiss();
			}
		});

		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		// popupWindow.setAnimationStyle();
		popupWindow.showAtLocation(locationView, Gravity.CENTER, 0, 0);
		popupWindow.update();

	}
    private void goToSpecifiedScreen(){
        if(goToIndex == 0){
            settingsTopView_01.requestFocus();
        }else if(goToIndex == 1){
            settingsTopView_02.requestFocus();
        }else if(goToIndex == 2){
            settingsTopView_03.requestFocus();
        }else if(goToIndex == 3){
            settingsTopView_04.requestFocus();
        }

        if (getCurrentFocus() == null) {
            if(goToIndex == 0){
                setViewVisable(settingsTopView_01);
            }else if(goToIndex == 1){
                setViewVisable(settingsTopView_02);
            }else if(goToIndex == 2){
                setViewVisable(settingsTopView_03);
            }else if(goToIndex == 3){
                setViewVisable(settingsTopView_04);
            }            
        }
        goToIndex = 0;
    }	
	//Rony add 
	void setHdmiOutputMuteSwitch(){
		boolean mute = false;
		String sHdmimute = sw.readSysfs("/sys/class/amhdmitx/amhdmitx0/hdmi_mute");
		if("Mute".equalsIgnoreCase(sHdmimute)){
			mute = true;
		}else if("UnMute".equalsIgnoreCase(sHdmimute)){
			mute = false;
		}else{
			Log.e(TAG, "===== hdmi output mute is format error!" + sHdmimute);
			return;
		}
		
		if(mute){
			sHdmimute = new String("UnMute");
		}else{
			sHdmimute = new String("Mute");
		}		
		
		sw.writeSysfs("/sys/class/amhdmitx/amhdmitx0/hdmi_mute", sHdmimute);
		updateHdmiOutputMuteUi();
	}
	
	void updateHdmiOutputMuteUi(){
		String sHdmimute = sw.readSysfs("/sys/class/amhdmitx/amhdmitx0/hdmi_mute");
        TextView txhdmimute = (TextView)findViewById(R.id.tx_hdmi_mute);
		if("Mute".equalsIgnoreCase(sHdmimute)){
			txhdmimute.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.on, 0);
		}else if("UnMute".equalsIgnoreCase(sHdmimute)){
			txhdmimute.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.off, 0);
		}
	}
	
	void setSpdifOutputMuteSwitch(){
		boolean mute = false;
		String sSpdifmute = sw.readSysfs("/sys/class/amhdmitx/amhdmitx0/spdif_mute");
		if("Mute".equalsIgnoreCase(sSpdifmute)){
			mute = true;
		}else if("UnMute".equalsIgnoreCase(sSpdifmute)){
			mute = false;
		}else{
			Log.e(TAG, "===== spdif output mute is format error!" + sSpdifmute);
			return;
		}
		
		if(mute){
			sSpdifmute = new String("UnMute");
		}else{
			sSpdifmute = new String("Mute");
		}		
		
		sw.writeSysfs("/sys/class/amhdmitx/amhdmitx0/spdif_mute", sSpdifmute);
		updateSpdifOutputMuteUi();
	}
	
	void updateSpdifOutputMuteUi(){
		String sSpdifmute = sw.readSysfs("/sys/class/amhdmitx/amhdmitx0/spdif_mute");
        TextView txspdifmute = (TextView)findViewById(R.id.tx_spdif_mute);
		if("Mute".equalsIgnoreCase(sSpdifmute)){
			txspdifmute.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.on, 0);
		}else if("UnMute".equalsIgnoreCase(sSpdifmute)){
			txspdifmute.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.off, 0);
		}
		
	}
	//Rony add end

    @Override
	public void onFocusChange(View v, boolean hasFocus) {
	    if(v instanceof RelativeLayout){
            int id = v.getId();
            if(isOpenAdjustScreenView){
                return  ;
            } 
            if(hasFocus){
                if (id == R.id.settingsTopView_02){
                	goToIndex = 1;
                    ((RelativeLayout) v).getChildAt(0).setBackgroundResource(R.drawable.image_top_language_select);
                }else if(id == R.id.settingsTopView_01){
                	goToIndex = 0;
                    ((RelativeLayout) v).getChildAt(0).setBackgroundResource(R.drawable.image_top_welcome_select);
                }else if(id == R.id.settingsTopView_03){
                	goToIndex = 2;
                    ((RelativeLayout) v).getChildAt(0).setBackgroundResource(R.drawable.image_top_network_select);
                }else if(id == R.id.settingsTopView_04){
                	goToIndex = 3;
                    ((RelativeLayout) v).getChildAt(0).setBackgroundResource(R.drawable.image_top_screen_select);
                }
                setViewVisable((RelativeLayout)v);
            } else{
                if (id == R.id.settingsTopView_02){
                    ((RelativeLayout) v).getChildAt(0).setBackgroundResource(R.drawable.image_top_language);
                }else if(id == R.id.settingsTopView_01){
                    ((RelativeLayout) v).getChildAt(0).setBackgroundResource(R.drawable.image_top_welcome);
                }else if(id == R.id.settingsTopView_03){
                    ((RelativeLayout) v).getChildAt(0).setBackgroundResource(R.drawable.image_top_network);
                }else if(id == R.id.settingsTopView_04){
                    ((RelativeLayout) v).getChildAt(0).setBackgroundResource(R.drawable.image_top_screen);
                }
            } 
        }
        
	}

    public void setViewVisable(RelativeLayout v) {
        if (Utils.DEBUG) Log.d(TAG,"===== setViewVisable()");
       /*
        java.util.Map<Thread, StackTraceElement[]> ts = Thread.getAllStackTraces();   
        StackTraceElement[] ste = ts.get(Thread.currentThread());   
        for (StackTraceElement s : ste) {   
            android.util.Log.e(TAG,"=====" + s.toString());  
        } */
        
        int id = v.getId();
        ScaleAnimation scaleAnimation = null;
        if (Utils.DEBUG) Log.d(TAG,"===== setViewVisable(), id=" + id);
        if(preView != null){
            if (Utils.DEBUG) Log.d(TAG,"===== preView != null");
            preView.getChildAt(0).setScaleX(1f);
            preView.getChildAt(0).setScaleY(1f);
            preView.getChildAt(1).setScaleX(1f);
            preView.getChildAt(1).setScaleY(1f);
            
            //scaleAnimation = new ScaleAnimation(1.1f, 1f,1.1f, 1f);
            //scaleAnimation.setDuration(500);
            //preView.startAnimation(scaleAnimation);
        }

        scaleAnimation = new ScaleAnimation(0.9f, 1f,0.9f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setDuration(300);
		v.startAnimation(scaleAnimation);
        
	    v.getChildAt(0).setScaleX(1.05f);
	    v.getChildAt(0).setScaleY(1.05f);
	    v.getChildAt(1).setScaleX(1.1f);
	    v.getChildAt(1).setScaleY(1.1f);

        preView = v ;
        
		if (id == R.id.settingsTopView_01) {
            if (Utils.DEBUG) Log.d(TAG,"===== setViewVisable(), settingsTopView_01");
			mCurrentContentNum = VIEW_NETWORK;
			settingsContentLayout_02.setVisibility(View.GONE);
			settingsContentLayout_03.setVisibility(View.GONE);
			settingsContentLayout_04.setVisibility(View.GONE);
			settingsContentLayout_01.setVisibility(View.VISIBLE);
            if (!mEthConnectingFlag)
                wifiResume();
		} else if (id == R.id.settingsTopView_02) {
		    if (Utils.DEBUG) Log.d(TAG,"===== setViewVisable(), settingsTopView_02");
			mCurrentContentNum = VIEW_DISPLAY;
			settingsContentLayout_01.setVisibility(View.GONE);
			settingsContentLayout_03.setVisibility(View.GONE);
			settingsContentLayout_04.setVisibility(View.GONE);
			settingsContentLayout_02.setVisibility(View.VISIBLE);
		} else if (id == R.id.settingsTopView_03) {
		    if (Utils.DEBUG) Log.d(TAG,"===== setViewVisable(), settingsTopView_03");
			mCurrentContentNum = VIEW_MORE;
			settingsContentLayout_01.setVisibility(View.GONE);
			settingsContentLayout_02.setVisibility(View.GONE);
			settingsContentLayout_04.setVisibility(View.GONE);
			settingsContentLayout_03.setVisibility(View.VISIBLE);
		} else if (id == R.id.settingsTopView_04) {
		    if (Utils.DEBUG) Log.d(TAG,"===== setViewVisable(), settingsTopView_04");
			mCurrentContentNum = VIEW_OTHER;
			settingsContentLayout_01.setVisibility(View.GONE);
			settingsContentLayout_03.setVisibility(View.GONE);
			settingsContentLayout_02.setVisibility(View.GONE);
			settingsContentLayout_04.setVisibility(View.VISIBLE);
		}
        
	}

    class MyHandle extends Handler {
		@Override
		public void handleMessage(Message msg) {
            switch(msg.what){
                case UPDATE_AP_LIST :
                        mAccessPointListAdapter.updateAccesspointList();
                        if (mAccessPointListAdapter.getCount() <= 0) {
                            mAcessPointListView.setVisibility(View.GONE);
                            wifi_listview_tip.setVisibility(View.VISIBLE);
                        } else {
                            wifi_listview_tip.setVisibility(View.GONE);
                            mAcessPointListView.setVisibility(View.VISIBLE);
                        }
                    break;
                case UPDATE_OUTPUT_MODE_UI :
                    String mode = sw.readSysfs(DISPLAY_MODE_SYSFS);
                    if(mode.contains("cvbs")){
                        cvbs_current_mode_value.setText(mOutPutModeManager.getCurrentOutPutModeTitle(0));
                    }
                    else{
                        current_mode_value.setText(mOutPutModeManager.getCurrentOutPutModeTitle(1));
                    }
                    break ;

                case SHOW_CONFIRM_DIALOG :
                        showDisplayDialog();
                    break;
                case UPDATE_ETH_STATUS:
                    if (Utils.DEBUG) Log.d(TAG, "**UPDATE_ETH_STATUS:  ");
                    mEthConnectingFlag = false;
                    if(getEthCheckBoxState()) {
                        if (!isEthDeviceAdded()){
                            setEthCheckBoxSwitch(false);
                        }
                    }
                    break;
                default:
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
        int state = mEthernetManager.getEthState();
        if (Utils.DEBUG) Log.d(TAG,"===== getEthCheckBoxState() , state : " + state);
        if(state == EthernetManager.ETH_STATE_ENABLED &&sw.getPropertyBoolean("hw.hasethernet" , false)){
            return true;
        }
        else{
            return false;
        }
    }


    private void showHdmiinPromptDialog() {
        dismissHdmiinPromptDialog();
        mHdmiinDialog = new HdmiinSetOutputDialog(mContext);
        mHdmiinDialog.show();
    }

    private void dismissHdmiinPromptDialog() {
        if (mHdmiinDialog != null) {
            mHdmiinDialog.dismissAndStop();
            mHdmiinDialog = null;
        }
    }

    private void showDisplayDialog(){
        dismissDisplayDiglog();
        dialog = new DisplayConfirmDialog(mContext, false , null);
        dialog.recordOldMode(oldMode);
        dialog.show();
        if (Utils.DEBUG) Log.d(TAG,"===== showDisplayDialog() , oldMode=" + oldMode);
    }

    private void dismissDisplayDiglog(){
        if (Utils.DEBUG) Log.d(TAG,"===== dismissDisplayDiglog()");
        if(dialog!=null){
            dialog.dismissAndStop();
            dialog = null;
        }
    }

    private void restartActivitySelf(){
        goToIndex = 1;
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);    
    }

    private String getLocalIpAddress() {
        ConnectivityManager cm = (ConnectivityManager)
this.getSystemService(Context.CONNECTIVITY_SERVICE);
        LinkProperties prop = cm.getActiveLinkProperties();
        return formatIpAddresses(prop);
    }


    private static String formatIpAddresses(LinkProperties prop) {

        if (prop == null) return null;

        Iterator<InetAddress> iter = prop.getAllAddresses().iterator();

        // If there are no entries, return null
 
        if (!iter.hasNext()) return null;

            // Concatenate all available addresses, comma separated

        String addresses = "";
        InetAddress addr;
        while (iter.hasNext()) {
            addr = iter.next();
            if(addr instanceof Inet4Address){
                addresses = addr.getHostAddress();
                break;

            }
            /*addresses += iter.next().getHostAddress();
        
    if (iter.hasNext()) addresses += "\n";*/

            }

        return addresses;

    }

}
