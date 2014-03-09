package com.baidu.demo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.baidu.demo.R;
import com.baidu.demo.metadata.ClueProviderMetaData.ClueGroupTableMetaData;
import com.baidu.demo.metadata.ClueProviderMetaData.ClueTableMetaData;

/**
 * ���������ϴ����û�������ת���ɵ��ı�����˷��������ӷ���������ȡ������浽����
 * 
 * @author zhoujie04
 * 
 */
public class UploadSpeechText extends AsyncTask<String, Integer, String> {
	private Context context;

	public UploadSpeechText(Context context) {
		this.context = context;
	}

	@Override
	protected String doInBackground(String... params) {
		return uploadSpeechText(params[0]);
		// return testData;
	}

	protected void onPostExecute(String result) {
		// ��Ҫ��һЩ���湤�����Ѻ�˴�������json�����������浽�������ݿ���
		// Ȼ��֪ͨ�û����µ������Ƽ�����
		// Log.d(this.getClass().getName(), result);
		// �Ƚ�����ClueGroup
		ClueGroup clueGroup = parseResult(result);
		if (clueGroup.getKeywords() == null
				|| clueGroup.getKeywords().equals("")
				|| clueGroup.getClues() == null
				|| clueGroup.getClues().size() == 0) {
			return;
		}
		ContentResolver cr = context.getContentResolver();
		Uri uri = ClueGroupTableMetaData.CONTENT_URI;
		ContentValues cv = new ContentValues();
		cv.put(ClueGroupTableMetaData.CLUEGROUP_KEY_WORD,
				clueGroup.getKeywords());
		cv.put(ClueGroupTableMetaData.CLUEGROUP_PHONE_TIME,
				clueGroup.getPhoneTime());
		cv.put(ClueGroupTableMetaData.CLUEGROUP_USER_TEXT,
				clueGroup.getUserText());
		// ����clueGroup
		Uri insertUri = cr.insert(uri, cv);
		String groupId = insertUri.getPathSegments().get(1);
		for (Clue clue : clueGroup.getClues()) {
			// ����ÿһ������
			ContentValues ccv = new ContentValues();
			ccv.put(ClueTableMetaData.CLUE_CONTACT_NAME, clue.getContact_name());
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
	}

	private String uploadSpeechText(String text) {
		if (text == null || text.equals("")) {
			return "";
		}
		// ��˴���������json��
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://db-crm-gy01.db01.baidu.com:8787");
		// ���ó�ʱʱ��Ϊ3��
		HttpParams params = post.getParams();
		HttpConnectionParams.setSoTimeout(params, 3000);
		post.setParams(params);
		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		postParams.add(new BasicNameValuePair("userText", text));

		HttpResponse response;
		String page = "";
		try {
			post.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));
			response = client.execute(post);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			page = sb.toString();
			Log.d(this.getClass().getName(), page);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return page;
	}

	private ClueGroup parseResult(String result) {
		ClueGroup clueGroup = new ClueGroup();
		try {
			JSONObject jsonObject = new JSONObject(result);
			int status = jsonObject.getInt("status");
			if (status != 1) {
				return clueGroup;
			}
			// ���ùؼ���
			JSONArray keywords = jsonObject.getJSONArray("keywords");
			StringBuilder keywordsText = new StringBuilder();
			for (int i = 0; i < keywords.length(); i++) {
				keywordsText.append(keywords.getString(i)).append(" ");
			}
			clueGroup.setKeywords(keywordsText.toString());
			// ����ͨ��ʱ��
			Long now = Long.valueOf(System.currentTimeMillis());
			clueGroup.setPhoneTime(now);
			// �������е�����
			clueGroup.setClues(parseClueList(jsonObject
					.getJSONArray("clue_list")));

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

	private void notifyUser() {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		CharSequence tickerText = "�����µ������Ƽ�";
		long when = System.currentTimeMillis();
		Notification notification = new Notification(R.drawable.ic_launcher,
				tickerText, when);

		// ��������֪ͨ��ʱҪչ�ֵ�������Ϣ
		CharSequence contentTitle = "�����Ƽ�";
		CharSequence contentText = "�������˴ε�ͨ����¼������Ϊ���ҵ�����ص�������������Ƽ��б�ҳ��鿴��";
		Intent notificationIntent = new Intent(context, context.getClass());
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		// ��notificationManager��notify����֪ͨ�û����ɱ�������Ϣ֪ͨ
		notificationManager.notify(1, notification);

	}
}
