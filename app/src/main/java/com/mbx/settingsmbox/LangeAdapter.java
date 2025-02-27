package com.mbx.settingsmbox;

import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LangeAdapter extends BaseAdapter {
	private static final String TAG = "LangeAdapter";
	
    private LayoutInflater mInflater;
    private int mSelected = -1;
    private TypedArray langImg;
    private String[] langStr;
    private Context mcontext;
    
    
	public LangeAdapter(Context context) {
	  super();
	  mcontext= context;
      mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);       
      langImg = context.getResources().obtainTypedArray(R.array.default_lang);
      langStr = context.getResources().getStringArray(R.array.default_lang_des);
	}
 
	public void setSelected(int position) {
		mSelected = position;
	}
	
	int lang = -1;
	public void setLangSelected(int position) {
		lang = position;
	}
 
	 /**
     * @see android.widget.Adapter#getView(int, View, ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) { 
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.oobe_language_item, null); 	
			holder.langTxt = (TextView)convertView.findViewById(R.id.language_item_txt);
			holder.langImg = (ImageView)convertView.findViewById(R.id.language_item_img);
			holder.langImgSel= (ImageView)convertView.findViewById(R.id.language_item_selcted);
			holder.langImgBg = (RelativeLayout)convertView.findViewById(R.id.language_item_img_bg);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		if(position == mSelected){
			holder.langImgBg.setBackgroundResource(R.drawable.language_seclect_focused);
		}else{
			holder.langImgBg.setBackgroundResource(R.drawable.transparent);
		}
		
		if(lang == position){
			holder.langImgSel.setBackgroundResource(R.drawable.language_current);
		}else{
			holder.langImgSel.setBackgroundResource(R.drawable.transparent);
		}

		holder.langTxt.setText(langStr[position]);
		holder.langImg.setBackgroundResource(langImg.getResourceId(position,0));
		return convertView;
    }
    
 
	public class ViewHolder {
		public TextView langTxt;
		public ImageView langImg;
		public RelativeLayout langImgBg;
		public ImageView langImgSel;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return langStr.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return langStr[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	} 
}
