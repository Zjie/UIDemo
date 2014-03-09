package com.baidu.demo;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.baidu.android.speech.RecognitionListener;
import com.baidu.android.speech.SpeechConfig;
import com.baidu.android.speech.SpeechRecognizer;
import com.baidu.demo.audioService.AudioService;
import com.baidu.demo.audioService.PhoneReceiver;
import com.baidu.demo.provider.RecommendedListDataProvider;
import com.baidu.demo.utils.Alerts;
import com.baidu.demo.utils.UploadSpeechText;

public class MainActivity extends Activity {
	private String TAG = MainActivity.class.getName();
	private AudioService as;
	private PhoneReceiver pr;
	private SpeechRecognizer mSpeechRecognizer;
	private MyRecognitionListener mMyRecognitionListener;
	
	// 用户语音识别后的文本，涉及到多线程，因此要用volatile
	private volatile StringBuilder speechText;
	public volatile boolean stopListen = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 初始化百度语音识别器
		String appKey = "CTzs3b2ZOCaVx9wKSaS116l7";
		String secretKey = "SnMBmwPGoWaS1xq39rcZ3azwnfLx09ih";
		SpeechConfig.setup(this.getApplicationContext(), appKey, secretKey);
		// 初始化语音识别监听器
		mMyRecognitionListener = new MyRecognitionListener();
		mMyRecognitionListener.setMa(this);
		mSpeechRecognizer = SpeechRecognizer.getInstance(this);
		mSpeechRecognizer.setRecognitionListener(mMyRecognitionListener);
		// 初始化电话监听器
		as = new AudioService();
		as.setMa(this);
		pr = new PhoneReceiver();
		// 拨出电话时应该使用同一个录音组件
		pr.setAs(as);
		pr.setMa(this);

		speechText = new StringBuilder();
		setContentView(R.layout.activity_main);

		// 绑定按钮事件
		bindEventToButton();
	}
	/**
	 * 按下，松手，录音识别的时间监听
	 * @author zhoujie04
	 *
	 */
	private class MyOnTouchListener implements OnTouchListener{
		Context context;
		public MyOnTouchListener(Context context) {
			this.context = context;
		}
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 当按下时，开始录音
				Log.d(TAG, "press the button");
				startSpeechRecognizer(new Bundle());
				break;
			case MotionEvent.ACTION_UP:
				// 当抬起时，结束录音
				Log.d(TAG, "release the button");
				stopSpeechRecognizer();
				if (hasTextForUpload()) {
					String text = getSpeechText();
					Log.d(TAG, text);
					//上传文本到服务器
					new UploadSpeechText(context).execute(text);
				}
				break;
			}
			return true;
		}
	}
	private void bindEventToButton() {
		Button button = (Button) this
				.findViewById(R.id.offline_recommend_button);
		button.setOnTouchListener(new MyOnTouchListener(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * 通话语音交互分析推荐
	 * 
	 * @param view
	 */
	public void makeAPhoneCall(View view) {
		try {
			// 注册监听器
			// 监听拨出事件
			this.registerReceiver(pr, new IntentFilter(
					Intent.ACTION_NEW_OUTGOING_CALL));
			// 监听挂断电话事件
			TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
			tm.listen(as, PhoneStateListener.LISTEN_CALL_STATE);
			// 打开拨电话的面板
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_DIAL);
			this.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 弹出框提示用户有线索推荐
	 */
	public void popupTips() {
		Alerts.showAlert("已有相关线索匹配，请点击\"查看推荐列表\"", this);
	}

	/**
	 * 开启单机关键词推荐
	 * 
	 * @param view
	 */
	public void singleRecommand(View view) {
		Intent intent = new Intent("com.baidu.demo.Recommend");
		this.startActivity(intent);
	}

	/**
	 * 查看推荐列表
	 * 
	 * @param view
	 */
	public void viewRecommondedList(View view) {
		Intent intent = new Intent("com.baidu.demo.RecommendedList");
		this.startActivity(intent);
	}

	/**
	 * 查看个人关键词分析
	 * 
	 * @param view
	 */
	public void viewKeyWordAnalysis(View view) {
		Intent intent = new Intent("com.baidu.demo.PersonalKeyWordAnalysis");
		this.startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// as.onDestroy();
		// 关闭百度语音的识别器
		mSpeechRecognizer.destroy();
		// this.unregisterReceiver(pr);
	}

	public void startSpeechRecognizer(Bundle bundle) {
		Log.d(MainActivity.class.getName(), "start speech recognizer");
		stopListen = false;
		final Bundle fbundle = bundle;
		new Thread() {
			public void run() {
				while(!stopListen) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mSpeechRecognizer.startListening(
							SpeechRecognizer.SpeechMode.VOICE_TO_TEXT, fbundle);
				}
			}
		}.start();
	}

	public void stopSpeechRecognizer() {
		Log.d(MainActivity.class.getName(), "stop speech recognizer");
		stopListen = true;
		mSpeechRecognizer.stopListening();
	}

	/**
	 * 判断用户语音识别后的文本是否足够长来确认是否要上传到服务器
	 * 
	 * @return
	 */
	public boolean hasTextForUpload() {
		if (speechText == null || speechText.length() > 1 || speechText.toString().equals("")) {
			return false;
		}
		return true;
	}

	/**
	 * 返回这次通话语音识别后的文本，并清空以前的文本 这样用户就能多次通话，记录不同的文本
	 * 
	 * @return 此次通话语音识别后的文本
	 */
	public String getSpeechText() {
		String text = speechText.toString();
		speechText.delete(0, text.length());
		return text;
	}
	public synchronized void appendSpeechText(String text) {
		speechText.append(text);
	}
	private class MyRecognitionListener implements RecognitionListener,
			android.speech.RecognitionListener {
		private MainActivity ma;

		@Override
		public void onBeginningOfSpeech() {
			Log.d(MainActivity.class.getName(), "begin to talk");
		}

		@Override
		public void onBufferReceived(byte[] arg0) {
			Log.d(MainActivity.class.getName(), "receive data from user");
		}

		@Override
		public void onEndOfSpeech() {
			Log.d(MainActivity.class.getName(), "user stop talking");
		}

		@Override
		public void onError(int arg0) {
		}

		@Override
		public void onEvent(int arg0, Bundle arg1) {
		}

		@Override
		public void onPartialResults(Bundle arg0) {
		}

		@Override
		public void onReadyForSpeech(Bundle arg0) {
			Log.d(MainActivity.class.getName(), "ready for speech");
		}

		@Override
		public void onResults(Bundle results) {
			Log.d(MainActivity.class.getName(),
					"get result from voice recognition");
			if (results == null) {// 最后一句话识别完成后，回调到onResults的结果为null
				return;
			}
			ArrayList<String> resultList = results
					.getStringArrayList(SpeechRecognizer.EXTRA_RESULTS_RECOGNITION);
			if (resultList == null || resultList.size() < 1) {
				return;
			}
			// 返回的result是一系列可能匹配的文本，其中选出第一个，也就是最匹配的那个
			// String result = resultList.get(0);
			Log.d(MainActivity.class.getName(), resultList.get(0));
			ma.appendSpeechText(resultList.get(0));
			// 需要设置重新开启语音识别，这样才能进行长通话录音
			// ma.startSpeechRecognizer(new Bundle());
			// Alerts.showAlert(result, ma);
		}

		@Override
		public void onRmsChanged(float arg0) {
		}

		public void setMa(MainActivity ma) {
			this.ma = ma;
		}
	}
}
