package com.baidu.demo.utils;

import com.baidu.demo.metadata.ClueProviderMetaData;
import com.baidu.demo.metadata.ClueProviderMetaData.ClueGroupTableMetaData;
import com.baidu.demo.metadata.ClueProviderMetaData.ClueTableMetaData;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = "DatabaseHelper";
	public DatabaseHelper(Context context) {
		super(context, ClueProviderMetaData.DATABASE_NAME, null,
				ClueProviderMetaData.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "inner oncreate called");
		db.execSQL("CREATE TABLE " + ClueGroupTableMetaData.TABLE_NAME + " ("
				+ ClueGroupTableMetaData._ID + " INTEGER PRIMARY KEY,"
				+ ClueGroupTableMetaData.CLUEGROUP_KEY_WORD + " TEXT,"
				+ ClueGroupTableMetaData.CLUEGROUP_PHONE_TIME + " INTEGER,"
				+ ClueGroupTableMetaData.CLUEGROUP_USER_TEXT + " TEXT" + ");");
		db.execSQL("CREATE TABLE " + ClueTableMetaData.TABLE_NAME + " ("
				+ ClueTableMetaData._ID + " INTEGER PRIMARY KEY,"
				+ ClueTableMetaData.CLUE_ADDR + " TEXT,"
				+ ClueTableMetaData.CLUE_CONTACT_NAME + " TEXT,"
				+ ClueTableMetaData.CLUE_CUST_NAME + " TEXT,"
				+ ClueTableMetaData.CLUE_PHONE + " TEXT,"
				+ ClueTableMetaData.CLUE_PROFILE + " TEXT,"
				+ ClueTableMetaData.CLUE_TRADE + " TEXT,"
				+ ClueTableMetaData.CLUE_URL + " TEXT,"

				+ ClueTableMetaData.CLUE_GROUP_ID + " TEXT,"
				+ ClueTableMetaData.CREATED_DATE + " INTEGER,"
				+ ClueTableMetaData.MODIFIED_DATE + " INTEGER" + ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "inner onupgrade called");
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + ClueGroupTableMetaData.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ClueTableMetaData.TABLE_NAME);
		onCreate(db);
	}

}
