package com.baidu.demo.provider;

import java.util.HashMap;

import com.baidu.demo.metadata.ClueProviderMetaData;
import com.baidu.demo.metadata.ClueProviderMetaData.ClueTableMetaData;
import com.baidu.demo.utils.DatabaseHelper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ClueProvider extends ContentProvider {
	private static final String TAG = "ClueProvider";
	// Provide a mechanism to identify
	// all the incoming uri patterns.
	private static final UriMatcher uriMatcher;
	private static final int INCOMING_CLUE_COLLECTION_URI_INDICATOR = 1;
	private static final int INCOMING_SINGLE_CLUE_URI_INDICATOR = 2;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(ClueProviderMetaData.AUTHORITY, "clues",
				INCOMING_CLUE_COLLECTION_URI_INDICATOR);
		uriMatcher.addURI(ClueProviderMetaData.AUTHORITY, "clues/#",
				INCOMING_SINGLE_CLUE_URI_INDICATOR);

	}
	// columns.
	private static HashMap<String, String> cluesProjectionMap;
	static {
		cluesProjectionMap = new HashMap<String, String>();
		cluesProjectionMap.put(ClueTableMetaData._ID, ClueTableMetaData._ID);

		// name, isbn, author
		cluesProjectionMap.put(ClueTableMetaData.CLUE_ADDR,
				ClueTableMetaData.CLUE_ADDR);
		cluesProjectionMap.put(ClueTableMetaData.CLUE_CONTACT_NAME,
				ClueTableMetaData.CLUE_CONTACT_NAME);
		cluesProjectionMap.put(ClueTableMetaData.CLUE_CUST_NAME,
				ClueTableMetaData.CLUE_CUST_NAME);
		cluesProjectionMap.put(ClueTableMetaData.CLUE_PHONE,
				ClueTableMetaData.CLUE_PHONE);
		cluesProjectionMap.put(ClueTableMetaData.CLUE_PROFILE,
				ClueTableMetaData.CLUE_PROFILE);
		cluesProjectionMap.put(ClueTableMetaData.CLUE_TRADE,
				ClueTableMetaData.CLUE_TRADE);
		cluesProjectionMap.put(ClueTableMetaData.CLUE_URL,
				ClueTableMetaData.CLUE_URL);

		// created date, modified date
		cluesProjectionMap.put(ClueTableMetaData.CREATED_DATE,
				ClueTableMetaData.CREATED_DATE);
		cluesProjectionMap.put(ClueTableMetaData.MODIFIED_DATE,
				ClueTableMetaData.MODIFIED_DATE);
		//group id
		cluesProjectionMap.put(ClueTableMetaData.CLUE_GROUP_ID,
				ClueTableMetaData.CLUE_GROUP_ID);
	}
	private DatabaseHelper openHelper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
		case INCOMING_CLUE_COLLECTION_URI_INDICATOR:
			count = db.delete(ClueTableMetaData.TABLE_NAME, where, whereArgs);
			break;

		case INCOMING_SINGLE_CLUE_URI_INDICATOR:
			String rowId = uri.getPathSegments().get(1);
			count = db.delete(
					ClueTableMetaData.TABLE_NAME,
					ClueTableMetaData._ID
							+ "="
							+ rowId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case INCOMING_CLUE_COLLECTION_URI_INDICATOR:
			return ClueTableMetaData.CONTENT_TYPE;
		case INCOMING_SINGLE_CLUE_URI_INDICATOR:
			return ClueTableMetaData.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (uriMatcher.match(uri) != INCOMING_CLUE_COLLECTION_URI_INDICATOR) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		Long now = Long.valueOf(System.currentTimeMillis());

		// Make sure that the fields are all set
		if (values.containsKey(ClueTableMetaData.CREATED_DATE) == false) {
			values.put(ClueTableMetaData.CREATED_DATE, now);
		}

		if (values.containsKey(ClueTableMetaData.MODIFIED_DATE) == false) {
			values.put(ClueTableMetaData.MODIFIED_DATE, now);
		}

		if (values.containsKey(ClueTableMetaData.CLUE_CUST_NAME) == false) {
			throw new SQLException(
					"Failed to insert row because Company Name is needed "
							+ uri);
		}

		if (values.containsKey(ClueTableMetaData.CLUE_CONTACT_NAME) == false) {
			values.put(ClueTableMetaData.CLUE_CONTACT_NAME,
					"Unknown contact name");
		}

		SQLiteDatabase db = openHelper.getWritableDatabase();
		long rowId = db.insert(ClueTableMetaData.TABLE_NAME,
				ClueTableMetaData.CLUE_CUST_NAME, values);

		if (rowId > 0) {
			Uri insertedClueUri = ContentUris.withAppendedId(
					ClueTableMetaData.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(insertedClueUri,
					null);

			return insertedClueUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		Log.d(TAG, "main onCreate called");
		openHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (uriMatcher.match(uri)) {
		case INCOMING_CLUE_COLLECTION_URI_INDICATOR:
			qb.setTables(ClueTableMetaData.TABLE_NAME);
			qb.setProjectionMap(cluesProjectionMap);
			break;

		case INCOMING_SINGLE_CLUE_URI_INDICATOR:
			qb.setTables(ClueTableMetaData.TABLE_NAME);
			qb.setProjectionMap(cluesProjectionMap);
			qb.appendWhere(ClueTableMetaData._ID + "="
					+ uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = ClueTableMetaData.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, orderBy);

		// example of getting a count
		int i = c.getCount();

		// Tell the cursor what uri to watch,
		// so it knows when its source data changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
		case INCOMING_CLUE_COLLECTION_URI_INDICATOR:
			count = db.update(ClueTableMetaData.TABLE_NAME, values, where,
					whereArgs);
			break;

		case INCOMING_SINGLE_CLUE_URI_INDICATOR:
			String rowId = uri.getPathSegments().get(1);
			count = db.update(
					ClueTableMetaData.TABLE_NAME,
					values,
					ClueTableMetaData._ID
							+ "="
							+ rowId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
