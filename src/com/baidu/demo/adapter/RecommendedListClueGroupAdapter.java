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
			// �ȴ�xml�������������item��view����
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.recommended_list_group,
					null);
			// Ȼ����������������
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
		// �ؼ���
		holder.keyWord.setText("�ؼ���:" + (String) data.get(position).get("keyWord"));
		// ����
		holder.date.setText((String) data.get(position).get("date"));
		// �б�
		parseClueToView((List<Clue>) data.get(position).get("clueGroup"),
				holder.clueGroup);
		return convertView;
	}

	/**
	 * ��ÿ���������Ⱦ��һ��list��������
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
		// ����߶�
		int totalHeight = 0;
		for (int i = 0; i < adapter.getCount(); i++) {
			View listItem = adapter.getView(i, null, clueGroup);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		// �Ƽ��б�
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
