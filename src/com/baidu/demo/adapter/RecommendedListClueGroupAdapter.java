package com.baidu.demo.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.demo.R;
import com.baidu.demo.utils.Clue;

public class RecommendedListClueGroupAdapter extends BaseAdapter {
	private static final String TAG = RecommendedListClueGroupAdapter.class
			.getName();
	private static int convertViewCounter = 0;
	private List<Map<String, Object>> data;
	private Context context;
	private LayoutInflater inflater;

	public RecommendedListClueGroupAdapter(Context context,
			List<Map<String, Object>> data) {
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

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		if (convertView == null) {
			// 先从xml里面解析出单个item的view出来
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.recommended_list_group,
					null);
			// 然后设置里面的子组件
			holder.keyWord = (TextView) convertView
					.findViewById(R.id.recommended_list_keyword);
			holder.date = (TextView) convertView
					.findViewById(R.id.recommended_list_date);
			holder.clueGroup = (ListView) convertView
					.findViewById(R.id.recommended_list_clue_group);
			convertViewCounter++;
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// 关键词
		holder.keyWord.setText("关键词:" + (String) data.get(position).get("keyWord"));
		// 日期
		holder.date.setText((String) data.get(position).get("date"));
		// 列表
		parseClueToView((List<Clue>) data.get(position).get("clueGroup"),
				holder.clueGroup);
		return convertView;
	}

	/**
	 * 把每组的线索渲染成一个list到界面上
	 * 
	 * @param clues
	 */
	private void parseClueToView(List<Clue> clues, ListView clueGroup) {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		for (Clue clue : clues) {
			Map<String, Object> row = new HashMap<String, Object>();
			row.put("company_name", clue.getCust_name());
			row.put("contact_name", clue.getContact_name());
			row.put("phone", clue.getPhone());
			data.add(row);
		}
		RecommendedListViewAdapter adapter = new RecommendedListViewAdapter(
				this.context, data);

		clueGroup.setAdapter(adapter);
		// 计算高度
		int totalHeight = 0;
		for (int i = 0; i < adapter.getCount(); i++) {
			View listItem = adapter.getView(i, null, clueGroup);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		// 推荐列表
		LayoutParams params = clueGroup.getLayoutParams();
		params.height = totalHeight
				+ (clueGroup.getDividerHeight() * (adapter.getCount() - 1)) + 3;

//		((MarginLayoutParams) params).setMargins(10, 10, 10, 10);

		clueGroup.setLayoutParams(params);
	}

	static class ViewHolder {
		public TextView keyWord;
		public TextView date;
		public ListView clueGroup;
	}

}
