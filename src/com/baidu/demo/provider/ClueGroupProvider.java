package com.baidu.demo.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.demo.metadata.ClueProviderMetaData;
import com.baidu.demo.metadata.ClueProviderMetaData.ClueGroupTableMetaData;
import com.baidu.demo.utils.DatabaseHelper;

public class ClueGroupProvider extends ContentProvider {
	private static final String TAG = "ClueGroupProvider";

	private static final UriMatcher uriMatcher;
	private static final int INCOMING_CLUEGROUP_COLLECTION_URI_INDICATOR = 1;
	private static final int INCOMING_SINGLE_CLUEGROUP_URI_INDICATOR = 2;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(ClueProviderMetaData.GROUPAUTHORITY, "cluegroup",
				INCOMING_CLUEGROUP_COLLECTION_URI_INDICATOR);
		uriMatcher.addURI(ClueProviderMetaData.GROUPAUTHORITY, "cluegroup/#",
				INCOMING_SINGLE_CLUEGROUP_URI_INDICATOR);
	}

	// columns.
	private static HashMap<String, String> clueGroupProjectionMap;
	static {
		clueGroupProjectionMap = new HashMap<String, String>();
		clueGroupProjectionMap.put(ClueGroupTableMetaData._ID, ClueGroupTableMetaData._ID);
		clueGroupProjectionMap.put(ClueGroupTableMetaData.CLUEGROUP_KEY_WORD, ClueGroupTableMetaData.CLUEGROUP_KEY_WORD);
		clueGroupProjectionMap.put(ClueGroupTableMetaData.CLUEGROUP_PHONE_TIME, ClueGroupTableMetaData.CLUEGROUP_PHONE_TIME);
		clueGroupProjectionMap.put(ClueGroupTableMetaData.CLUEGROUP_USER_TEXT, ClueGroupTableMetaData.CLUEGROUP_USER_TEXT);
	}
	private DatabaseHelper openHelper;

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
		case INCOMING_CLUEGROUP_COLLECTION_URI_INDICATOR:
			qb.setTables(ClueGroupTableMetaData.TABLE_NAME);
			qb.setProjectionMap(clueGroupProjectionMap);
			break;

		case INCOMING_SINGLE_CLUEGROUP_URI_INDICATOR:
			qb.setTables(ClueGroupTableMetaData.TABLE_NAME);
			qb.setProjectionMap(clueGroupProjectionMap);
			qb.appendWhere(ClueGroupTableMetaData._ID + "="
					+ uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = ClueGroupTableMetaData.DEFAULT_SORT_ORDER;
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
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case INCOMING_CLUEGROUP_COLLECTION_URI_INDICATOR:
			return ClueGroupTableMetaData.CONTENT_TYPE;
		case INCOMING_SINGLE_CLUEGROUP_URI_INDICATOR:
			return ClueGroupTableMetaData.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (uriMatcher.match(uri) != INCOMING_CLUEGROUP_COLLECTION_URI_INDICATOR) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = openHelper.getWritableDatabase();
		long rowId = db.insert(ClueGroupTableMetaData.TABLE_NAME,
				null, values);

		if (rowId > 0) {
			Uri insertedClueGroupUri = ContentUris.withAppendedId(
					ClueGroupTableMetaData.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(insertedClueGroupUri,
					null);

			return insertedClueGroupUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
