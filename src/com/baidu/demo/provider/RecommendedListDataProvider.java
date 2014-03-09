package com.baidu.demo.provider;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.baidu.demo.metadata.ClueProviderMetaData.ClueGroupTableMetaData;
import com.baidu.demo.metadata.ClueProviderMetaData.ClueTableMetaData;
import com.baidu.demo.utils.Clue;
import com.baidu.demo.utils.ClueGroup;

public class RecommendedListDataProvider {
	private Context context;

	public RecommendedListDataProvider(Context context) {
		this.context = context;
	}

	private String data = "{\"status\": 1, \"keywords\": [\"电脑\"], \"clue_list\": [{\"profile\": \"\", \"url\": \"\", \"cust_name\": \"东莞市盛天兴电脑经营部\", \"trade\": \"电脑硬件-电脑\", \"phone\": \"13662916100\", \"address\": \"\", \"contact_name\": \"徐鉴彬\"}, {\"profile\": \"\", \"url\": \"\", \"cust_name\": \"东莞市大朗凌讯电脑经营部\", \"trade\": \"电脑硬件-电脑\", \"phone\": \"13669806308\", \"address\": \"\", \"contact_name\": \"黄小伟先生\"}]}";

	public void insert() {
		ClueGroup clueGroup = parseResult(data);
		ContentResolver cr = context.getContentResolver();
		Uri uri = ClueGroupTableMetaData.CONTENT_URI;
		ContentValues cv = new ContentValues();
		cv.put(ClueGroupTableMetaData.CLUEGROUP_KEY_WORD, clueGroup.getKeywords());
		cv.put(ClueGroupTableMetaData.CLUEGROUP_PHONE_TIME, clueGroup.getPhoneTime());
		cv.put(ClueGroupTableMetaData.CLUEGROUP_USER_TEXT, clueGroup.getUserText());
		Uri insertUri = cr.insert(uri, cv);
		String groupId = insertUri.getPathSegments().get(1);
		for (Clue clue : clueGroup.getClues()) {
			ContentValues ccv = new ContentValues();
			ccv.put(ClueTableMetaData.CLUE_CONTACT_NAME,
					clue.getContact_name());
			ccv.put(ClueTableMetaData.CLUE_CUST_NAME, clue.getCust_name());
			ccv.put(ClueTableMetaData.CLUE_PHONE, clue.getPhone());
			ccv.put(ClueTableMetaData.CLUE_ADDR, clue.getAddr());
			ccv.put(ClueTableMetaData.CLUE_PROFILE, clue.getProfile());
			ccv.put(ClueTableMetaData.CLUE_TRADE, clue.getTrade());
			ccv.put(ClueTableMetaData.CLUE_URL, clue.getUrl());
			ccv.put(ClueTableMetaData.CLUE_GROUP_ID, groupId);
			
			ccv.put(ClueTableMetaData.MODIFIED_DATE, clueGroup.getPhoneTime());
			ccv.put(ClueTableMetaData.CREATED_DATE, clueGroup.getPhoneTime());

			Uri clueUri = ClueTableMetaData.CONTENT_URI;
			Uri clueInsertUri = cr.insert(clueUri, ccv);
		}
		Log.d("", "");
	}
	
	private ClueGroup parseResult(String result) {
		ClueGroup clueGroup = new ClueGroup();
		try {
			JSONObject jsonObject = new JSONObject(result);
			int status = jsonObject.getInt("status");
			if (status != 1) {
				return clueGroup;
			}
			//设置关键字
			JSONArray keywords = jsonObject.getJSONArray("keywords");
			StringBuilder keywordsText = new StringBuilder();
			for (int i = 0; i < keywords.length(); i++) {
				keywordsText.append(keywords.getString(i));
			}
			clueGroup.setKeywords(keywordsText.toString());
			//设置通话时间
			Long now = Long.valueOf(System.currentTimeMillis());
			clueGroup.setPhoneTime(now);
			//设置所有的线索
			clueGroup.setClues(parseClueList(jsonObject.getJSONArray("clue_list")));
			
		} catch (JSONException e) {
			Log.d("RecommendedListDataProvider", e.toString());
		}
		return clueGroup;
	}
	
	private List<Clue> parseClueList(JSONArray jsonArray) {
		List<Clue> clueList = new ArrayList<Clue>();
		Long now = Long.valueOf(System.currentTimeMillis());
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject clueJsonObject = jsonArray.getJSONObject(i);
				Clue clue = new Clue();
				clue.setAddr(clueJsonObject.getString("address"));
				clue.setContact_name(clueJsonObject.getString("contact_name"));
				clue.setCust_name(clueJsonObject.getString("cust_name"));
				clue.setPhone(clueJsonObject.getString("phone"));
				clue.setProfile(clueJsonObject.getString("profile"));
				clue.setTrade(clueJsonObject.getString("trade"));
				clue.setUrl(clueJsonObject.getString("url"));
				clue.setPhone_time(now);
				clueList.add(clue);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return clueList;
	}
}
