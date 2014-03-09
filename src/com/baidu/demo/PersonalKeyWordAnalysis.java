package com.baidu.demo;

import com.baidu.demo.R;
import com.baidu.demo.utils.UploadSpeechText;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class PersonalKeyWordAnalysis extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_key_word_analysis);
		new UploadSpeechText(this).execute("hello world");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.personal_key_word_analysis, menu);
		return true;
	}

}
