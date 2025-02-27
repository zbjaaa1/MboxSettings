package com.mbx.settingsmbox;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class HdmiinSetOutputDialog extends Dialog {
	private String TAG = "HdmiinSetOutputDialog";
	private Handler mHandle = null ;
	Runnable run = null;
    Context mContext = null;
    private final int DELAY_TIME = 5000 ;
    private boolean isNeedStop = false;
    
	public HdmiinSetOutputDialog(Context context) {
		super(context, R.style.style_dialog);
        mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hdmiin_set_output_dialog);

		run = new Runnable() {
			@Override
			public void run() {
			    synchronized(this){
                    if(!isNeedStop){
                        dismiss();
                    }
                }
			}
		};

		Button button_ok = (Button) findViewById(R.id.ok);
		button_ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
                run = null;
			}

		});
	}

	@Override
	public void show() {
		super.show();
        mHandle = new Handler();
		mHandle.postDelayed(run,DELAY_TIME);
	}

    public void dismissAndStop(){
        mHandle.removeCallbacks(run);
        isNeedStop = true;
        dismiss();
    }

	@Override
	public void dismiss() {
	    mHandle.removeCallbacks(run);
		super.dismiss();
	}
}
