package com.baidu.demo.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.demo.R;

public class RecommendedListViewAdapter extends BaseAdapter {
	private static final String TAG = "RecommendedListViewAdapter";
	private static int convertViewCounter = 0;
	private List<Map<String, Object>> data;
	private Context context;
	private LayoutInflater inflater;

	public RecommendedListViewAdapter(Context context, List<Map<String, Object>> data) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		
		if (convertView == null) {
			//先从xml里面解析出单个item的view出来
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.recommended_list_item, null);
			//然后设置里面的子组件
			holder.companyName = (TextView) convertView.findViewById(R.id.company_name);
			holder.contactName = (TextView) convertView.findViewById(R.id.contact_name);
			holder.phone = (TextView) convertView.findViewById(R.id.phone);
			convertViewCounter++;
			convertView.setTag(holder);
			convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					String phoneNum = (String) ((TextView) view.findViewById(R.id.phone)).getText();
					//如果不是有效电话，不拨打
					if (phoneNum == null || phoneNum.equals("") || !isValidatedPhoneNum(phoneNum)) {
						return;
					}
					view.findViewById(R.id.phone);
					// 打开拨电话的面板
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_DIAL);
					intent.setData(Uri.parse("tel:" + phoneNum));
					context.startActivity(intent);
				}});
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		//公司名
		holder.companyName.setText((String)data.get(position).get("company_name"));
		//联系人名
		holder.contactName.setText((String)data.get(position).get("contact_name"));
		//电话号码
		String phoneNum = (String)data.get(position).get("phone");
		if (phoneNum != null && !phoneNum.equals("")) {
			holder.phone.setText((String)data.get(position).get("phone"));
		}
		return convertView;
	}
	
	private boolean isValidatedPhoneNum(String phoneNum) {
		if (phoneNum.contains("-")) {
			phoneNum = phoneNum.replace("-", "");
		}
		try {
			Long phone = Long.parseLong(phoneNum);
		} catch (Exception e) {
			Log.d(TAG, "invalid phone num");
			return false;
		}
		return true;
	}
	static class ViewHolder {
		public TextView contactName;
		public TextView companyName;
		public TextView phone;
	}
}

