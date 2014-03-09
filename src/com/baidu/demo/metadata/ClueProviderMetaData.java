 package com.baidu.demo.metadata;

import android.net.Uri;
import android.provider.BaseColumns;

public class ClueProviderMetaData {
	public static final String AUTHORITY = "com.baidu.demo.provider.ClueProvider";
	public static final String GROUPAUTHORITY = "com.baidu.demo.provider.ClueGroupProvider";
	
	public static final String DATABASE_NAME = "clue.db"; 
    public static final int DATABASE_VERSION = 7; 
    public static final String CLUES_TABLE_NAME = "clues";
    public static final String CLUEGROUP_TABLE_NAME = "clue_group";
    private ClueProviderMetaData() {}
    
    public static final class ClueTableMetaData implements BaseColumns {
    	private ClueTableMetaData() {}
    	public static final String TABLE_NAME = "clues";
    	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/clues");
    	public static final String  CONTENT_TYPE =  "vnd.android.cursor.dir/vnd.baidu.clue";
    	public static final String  CONTENT_ITEM_TYPE  =  "vnd.android.cursor.item/vnd.baidu.clue";
    	public static final String DEFAULT_SORT_ORDER = "modified DESC";
    	
    	//Additional Columns start here.
    	public static final String CLUE_PROFILE = "profile";
    	public static final String CLUE_ADDR = "addr";
    	public static final String CLUE_URL = "url";
    	public static final String CLUE_CUST_NAME = "cust_name";
    	public static final String CLUE_TRADE = "trade";
    	public static final String CLUE_PHONE = "phone";
    	public static final String CLUE_CONTACT_NAME = "contact_name";
    	
    	public static final String CLUE_GROUP_ID = "group_id";
    	//Integer from System.currentTimeMillis() 
        public static final String CREATED_DATE = "created"; 
        //Integer from System.currentTimeMillis() 
        public static final String MODIFIED_DATE = "modified";
    }

    public static final class ClueGroupTableMetaData implements BaseColumns {
    	private ClueGroupTableMetaData() {}
    	public static final String TABLE_NAME = "clue_group";
    	public static final Uri CONTENT_URI = Uri.parse("content://" + GROUPAUTHORITY + "/cluegroup");
    	public static final String  CONTENT_TYPE =  "vnd.android.cursor.dir/vnd.baidu.cluegroup";
    	public static final String  CONTENT_ITEM_TYPE  =  "vnd.android.cursor.item/vnd.baidu.cluegroup";
    	public static final String DEFAULT_SORT_ORDER = "phone_time DESC";
    	
    	public static final String CLUEGROUP_PHONE_TIME = "phone_time";
    	public static final String CLUEGROUP_KEY_WORD = "key_word";
    	public static final String CLUEGROUP_USER_TEXT = "user_text";
    }
}
