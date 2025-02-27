package com.mbx.settingsmbox;

import android.preference.PreferenceFrameLayout;
import android.app.SystemWriteManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.IWindowManager;
import android.widget.ListView;
import android.widget.TabWidget;
import android.content.Context;
import android.content.res.Resources;
import android.os.RemoteException;
import android.util.AndroidException;
import android.os.ServiceManager;
public class Utils {
	private static final String TAG = "Utils";
    public static final boolean DEBUG = true;
    private static final String mDisplayAxis1080 = " 1920 1080 ";
    private static final String mDisplayAxis720 = " 1280 720 ";
    private static final String mDisplayAxis576 = " 720 576 ";
    private static final String mDisplayAxis480 = " 720 480 ";
	public static final String blankFb0File = "/sys/class/graphics/fb0/blank";

	public static String readSysFile(SystemWriteManager sw, String path) {
		if (sw == null) {
			Log.d(TAG, "readSysFile(), sw is null !!");
			return null;
		}

		if (path == null) {
			Log.d(TAG, "readSysFile(), path is null !!");
			return null;
		}

		return sw.readSysfs(path);

	}

	public static void writeSysFile(SystemWriteManager sw, String path,String value) {

		if (sw == null) {
			Log.d(TAG, "writeSysFile(), sw is null !!");
			return;
		}

		if (path == null) {
			Log.d(TAG, "writeSysFile(), path is null !!");
			return;
		}

		if (value == null) {
			Log.d(TAG, "writeSysFile(), value is null !!");
			return;
		}

		sw.writeSysfs(path, value);
	}

	public static boolean getPropertyBoolean(SystemWriteManager sw,String prop, boolean defaultValue) {
		if (sw == null) {
			Log.d(TAG, "getPropertyBoolean(), sw is null !!");
			return defaultValue;
		}

		if (prop == null) {
			Log.d(TAG, "getPropertyBoolean(), path is null !!");
			return defaultValue;
		}

		return sw.getPropertyBoolean(prop, defaultValue);

	}

	public static String getPropertyString(SystemWriteManager sw, String prop,String defaultValue) {
		if (sw == null) {
			Log.d(TAG, "getPropertyString(), sw is null !!");
			return defaultValue;
		}

		if (prop == null) {
			Log.d(TAG, "getPropertyString(), path is null !!");
			return defaultValue;
		}

		return sw.getPropertyString(prop, defaultValue);

	}

	public static int getPropertyInt(SystemWriteManager sw, String prop,int defaultValue) {
		if (sw == null) {
			Log.d(TAG, "getPropertyInt(), sw is null !!");
			return defaultValue;
		}

		if (prop == null) {
			Log.d(TAG, "getPropertyInt(), path is null !!");
			return defaultValue;
		}
		return sw.getPropertyInt(prop, defaultValue);

	}

	public static String getBinaryString(String config) {
		String indexString = "0123456789abcdef";
		String configString = config.substring(config.length() - 1,
				config.length());
		int indexOfConfigNum = indexString.indexOf(configString);
		String ConfigBinary = Integer.toBinaryString(indexOfConfigNum);
		if (ConfigBinary.length() < 4) {
			for (int i = ConfigBinary.length(); i < 4; i++) {
				ConfigBinary = "0" + ConfigBinary;
			}
		}
		return ConfigBinary;
	}
	
	public static int[] getBinaryArray(String binaryString) {
		int[] tmp = new int[4];
		for (int i = 0; i < binaryString.length(); i++) {
			String tmpString = String.valueOf(binaryString.charAt(i));
			tmp[i] = Integer.parseInt(tmpString);
		}
		return tmp;
	}
	
	public static String arrayToString(int[] array) {
		String getIndexString = "0123456789abcdef";
		int total = 0;
		System.out.println();
		for (int i = 0; i < array.length; i++) {
			total = total
					+ (int) (array[i] * Math.pow(2, array.length - i - 1));
		}
		Log.d(TAG, "in arrayToString cecConfig is:" + total);
		String cecConfig = "cec" + getIndexString.charAt(total);
		Log.d(TAG, "in arrayToString cecConfig is:" + cecConfig);
		return cecConfig;
	}

    /*!!!!!!if you want to change outpumode by display-size,pls set density first .
                    Because density is a factor in parsing dimens in xml  !!!!!!!*/
    public static void setDisplaySize(int w,int h){
    	 IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.checkService(
                Context.WINDOW_SERVICE));
        if (wm == null) {
            Log.d(TAG, "Can't connect to window manager; is the system running?");
            return;
        }

        try {
            if (w >= 0 && h >= 0) {
                // TODO(multidisplay): For now Configuration only applies to main screen.
                wm.setForcedDisplaySize(Display.DEFAULT_DISPLAY, w, h);
            } else {
                wm.clearForcedDisplaySize(Display.DEFAULT_DISPLAY);
            }
        } catch (RemoteException e) {
        }
    }
    public static void setDisplaySize(String width,String height){
        int w =Integer.parseInt(width);
        int h =Integer.parseInt(height);
        setDisplaySize(w,h);
    }
    public static String getDisplayAxisByMode(String mode){
        if(mode.indexOf("1080") >= 0)
            return mDisplayAxis1080;
        else if(mode.indexOf("720") >= 0)
            return mDisplayAxis720;
        else if(mode.indexOf("576") >= 0)
            return mDisplayAxis576;
        else 
            return mDisplayAxis480;
    }

    public static void setDensity(String mode){
        int density = 240;

        if(mode.equals("4k2knative"))
            density = 480;
        
        IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.checkService(
                Context.WINDOW_SERVICE));
        if (wm == null) {
            Log.d(TAG, "Can't connect to window manager; is the system running?");
            return;
        }

        try {
            if (density > 0) {
                // TODO(multidisplay): For now Configuration only applies to main screen.
                wm.setForcedDisplayDensity(Display.DEFAULT_DISPLAY, density);
            } else {
                wm.clearForcedDisplayDensity(Display.DEFAULT_DISPLAY);
            }
        } catch (RemoteException e) {
        }
    }

    

	public static void shadowScreen(final SystemWriteManager sw, final int time){
		sw.writeSysfs(blankFb0File, "1");
		Log.d(TAG,"===== beging shadowScreen()");
		Thread task = new Thread(new Runnable() {

		@Override
		public void run() {
		try {
			synchronized (this) {
				Log.d(TAG,"===== close osd");
				Thread.sleep(time);
				sw.writeSysfs(blankFb0File, "0");
				Log.d(TAG,"===== open osd");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		}
	});
		task.start();
	}
	
    public static void forcePrepareCustomPreferencesList(
            ViewGroup parent, View child, ListView list, boolean ignoreSidePadding) {
        list.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        list.setClipToPadding(false);
        prepareCustomPreferencesList(parent, child, list, ignoreSidePadding);
    }

    /**
     * Prepare a custom preferences layout, moving padding to {@link ListView}
     * when outside scrollbars are requested. Usually used to display
     * {@link ListView} and {@link TabWidget} with correct padding.
     */
    public static void prepareCustomPreferencesList(
            ViewGroup parent, View child, View list, boolean ignoreSidePadding) {
        final boolean movePadding = list.getScrollBarStyle() == View.SCROLLBARS_OUTSIDE_OVERLAY;
        if (movePadding && parent instanceof PreferenceFrameLayout) {
            ((PreferenceFrameLayout.LayoutParams) child.getLayoutParams()).removeBorders = true;

            final Resources res = list.getResources();
            final int paddingSide = res.getDimensionPixelSize(R.dimen.settings_side_margin);
            final int paddingBottom = res.getDimensionPixelSize(
                   R.dimen.preference_fragment_padding_bottom);

            final int effectivePaddingSide = ignoreSidePadding ? 0 : paddingSide;
            list.setPaddingRelative(effectivePaddingSide, 0, effectivePaddingSide, paddingBottom);
        }
    }
}
