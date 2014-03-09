package com.baidu.demo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.widget.ListView;

import com.baidu.demo.adapter.RecommendedListClueGroupAdapter;
import com.baidu.demo.adapter.RecommendedListViewAdapter;
import com.baidu.demo.metadata.ClueProviderMetaData.ClueGroupTableMetaData;
import com.baidu.demo.metadata.ClueProviderMetaData.ClueTableMetaData;
import com.baidu.demo.utils.Clue;
import com.baidu.demo.utils.ClueGroup;

public class RecommendedList extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recommended_list);
		// 从数据库里面检索出数据
		List<ClueGroup> clueGroup = getClueGroup();
		// 建线索按时间分组，这个时间也就是每次通话的时间
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		int i = 0;
		for (ClueGroup cg : clueGroup) {
			List<Clue> value = cg.getClues();
			Map<String, Object> row = new HashMap<String, Object>();
			row.put("keyWord", cg.getKeywords());
			row.put("date", DateFormat.format("EEEE, MMMM dd, yyyy h:mmaa",
					new Date(cg.getPhoneTime())));
			row.put("clueGroup", value);
			data.add(row);
			if (++i > 3) {
				break;
			}
		}

		RecommendedListClueGroupAdapter rcga = new RecommendedListClueGroupAdapter(
				this, data);
		ListView rcList = (ListView) this.findViewById(R.id.listView1);
		rcList.setAdapter(rcga);
		// 把数据渲染到界面上
		// parseClueToView(clues);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recommended_list, menu);
		return true;
	}

	private List<ClueGroup> getClueGroup() {
		ContentResolver cr = this.getContentResolver();
		Uri uri = ClueGroupTableMetaData.CONTENT_URI;
		String[] projection = { ClueGroupTableMetaData._ID,
				ClueGroupTableMetaData.CLUEGROUP_KEY_WORD,
				ClueGroupTableMetaData.CLUEGROUP_PHONE_TIME,
				ClueGroupTableMetaData.CLUEGROUP_USER_TEXT};
		Cursor cursor = cr.query(uri, projection, null, null, null);

		List<ClueGroup> clueGroups = new ArrayList<ClueGroup>();
		
		int idIdx = cursor.getColumnIndex(ClueGroupTableMetaData._ID);
		int keyWordIdx = cursor.getColumnIndex(ClueGroupTableMetaData.CLUEGROUP_KEY_WORD);
		int phoneTimeIdx = cursor.getColumnIndex(ClueGroupTableMetaData.CLUEGROUP_PHONE_TIME);
		int userTextIdx = cursor.getColumnIndex(ClueGroupTableMetaData.CLUEGROUP_USER_TEXT);
		
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			ClueGroup clueGroup = new ClueGroup();
			clueGroup.setId(cursor.getString(idIdx));
			clueGroup.setKeywords(cursor.getString(keyWordIdx));
			clueGroup.setPhoneTime(cursor.getLong(phoneTimeIdx));
			clueGroup.setUserText(cursor.getString(userTextIdx));
			clueGroup.setClues(getClues(clueGroup.getId()));
			clueGroups.add(clueGroup);
		}
		return clueGroups;
	}

	private List<Clue> getClues(String groupId) {
		List<Clue> clues = new ArrayList<Clue>();
		ContentResolver cr = this.getContentResolver();
		Uri uri = ClueTableMetaData.CONTENT_URI;
		Cursor cursor = cr.query(uri, null, "group_id=?", new String[]{groupId}, null);
		int profileIdx = cursor.getColumnIndex("profile");
		int addrIdx = cursor.getColumnIndex("addr");
		int urlIdx = cursor.getColumnIndex("url");
		int custNameIdx = cursor.getColumnIndex("cust_name");
		int tradeIdx = cursor.getColumnIndex("trade");
		int phoneIdx = cursor.getColumnIndex("phone");
		int contactNameIdx = cursor.getColumnIndex("contact_name");
		int groupIdIdx = cursor.getColumnIndex("group_id");

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Clue clue = new Clue();
			clue.setAddr(cursor.getString(addrIdx));
			clue.setContact_name(cursor.getString(contactNameIdx));
			clue.setCust_name(cursor.getString(custNameIdx));
			clue.setPhone(cursor.getString(phoneIdx));
			clue.setProfile(cursor.getString(profileIdx));
			clue.setTrade(cursor.getString(tradeIdx));
			clue.setUrl(cursor.getString(urlIdx));
			clue.setGroupId(cursor.getString(groupIdIdx));
			clues.add(clue);
		}
		return clues;
	}

	/**
	 * 把每组的线索渲染成一个list到界面上
	 * 
	 * @param clues
	 */
	private void parseClueToView(List<Clue> clues) {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Date date = new Date(clues.get(0).getPhone_time());
		for (Clue clue : clues) {
			Map<String, Object> row = new HashMap<String, Object>();
			row.put("company_name", clue.getCust_name());
			row.put("contact_name", clue.getContact_name());
			row.put("phone", clue.getPhone());
			data.add(row);
		}
		RecommendedListViewAdapter adapter = new RecommendedListViewAdapter(
				this, data);
		// 推荐列表
		ListView rcList = (ListView) this.findViewById(R.id.listView1);
		// LayoutParams params = rcList.getLayoutParams();
		// params.height = 500;
		// rcList.setLayoutParams(params);

		rcList.setAdapter(adapter);
	}

	/**
	 * 根据线索生成的时间来分组，用于分组显示
	 * 
	 * @return
	 */
	private Map<Long, List<Clue>> divideCluesByTime(List<Clue> clues) {
		Map<Long, List<Clue>> clueGroup = new TreeMap<Long, List<Clue>>(
				new Comparator<Long>() {
					@Override
					public int compare(Long o1, Long o2) {
						if (o1 == null || o2 == null)
							return 0;
						if (o1 < o2) {
							return 1;
						} else if (o1 > o2) {
							return -1;
						} else {
							return 0;
						}
					}
				});
		for (Clue clue : clues) {
			if (clueGroup.get(clue.getPhone_time()) == null) {
				clueGroup.put(clue.getPhone_time(), new ArrayList<Clue>());
			}
			clueGroup.get(clue.getPhone_time()).add(clue);
		}
		return clueGroup;
	}
}
