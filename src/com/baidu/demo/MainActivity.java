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
	
	// �û�����ʶ�����ı����漰�����̣߳����Ҫ��volatile
	private volatile StringBuilder speechText;
	public volatile boolean stopListen = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ��ʼ���ٶ�����ʶ����
		String appKey = "CTzs3b2ZOCaVx9wKSaS116l7";
		String secretKey = "SnMBmwPGoWaS1xq39rcZ3azwnfLx09ih";
		SpeechConfig.setup(this.getApplicationContext(), appKey, secretKey);
		// ��ʼ������ʶ�������
		mMyRecognitionListener = new MyRecognitionListener();
		mMyRecognitionListener.setMa(this);
		mSpeechRecognizer = SpeechRecognizer.getInstance(this);
		mSpeechRecognizer.setRecognitionListener(mMyRecognitionListener);
		// ��ʼ���绰������
		as = new AudioService();
		as.setMa(this);
		pr = new PhoneReceiver();
		// �����绰ʱӦ��ʹ��ͬһ��¼�����
		pr.setAs(as);
		pr.setMa(this);

		speechText = new StringBuilder();
		setContentView(R.layout.activity_main);

		// �󶨰�ť�¼�
		bindEventToButton();
	}
	/**
	 * ���£����֣�¼��ʶ���ʱ�����
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
				// ������ʱ����ʼ¼��
				Log.d(TAG, "press the button");
				startSpeechRecognizer(new Bundle());
				break;
			case MotionEvent.ACTION_UP:
				// ��̧��ʱ������¼��
				Log.d(TAG, "release the button");
				stopSpeechRecognizer();
				if (hasTextForUpload()) {
					String text = getSpeechText();
					Log.d(TAG, text);
					//�ϴ��ı���������
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
	 * ͨ���������������Ƽ�
	 * 
	 * @param view
	 */
	public void makeAPhoneCall(View view) {
		try {
			// ע�������
			// ���������¼�
			this.registerReceiver(pr, new IntentFilter(
					Intent.ACTION_NEW_OUTGOING_CALL));
			// �����Ҷϵ绰�¼�
			TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
			tm.listen(as, PhoneStateListener.LISTEN_CALL_STATE);
			// �򿪲��绰�����
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_DIAL);
			this.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��������ʾ�û��������Ƽ�
	 */
	public void popupTips() {
		Alerts.showAlert("�����������ƥ�䣬����\"�鿴�Ƽ��б�\"", this);
	}

	/**
	 * ���������ؼ����Ƽ�
	 * 
	 * @param view
	 */
	public void singleRecommand(View view) {
		Intent intent = new Intent("com.baidu.demo.Recommend");
		this.startActivity(intent);
	}

	/**
	 * �鿴�Ƽ��б�
	 * 
	 * @param view
	 */
	public void viewRecommondedList(View view) {
		Intent intent = new Intent("com.baidu.demo.RecommendedList");
		this.startActivity(intent);
	}

	/**
	 * �鿴���˹ؼ��ʷ���
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
		// �رհٶ�������ʶ����
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
	 * �ж��û�����ʶ�����ı��Ƿ��㹻����ȷ���Ƿ�Ҫ�ϴ���������
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
	 * �������ͨ������ʶ�����ı����������ǰ���ı� �����û����ܶ��ͨ������¼��ͬ���ı�
	 * 
	 * @return �˴�ͨ������ʶ�����ı�
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
			if (results == null) {// ���һ�仰ʶ����ɺ󣬻ص���onResults�Ľ��Ϊnull
				return;
			}
			ArrayList<String> resultList = results
					.getStringArrayList(SpeechRecognizer.EXTRA_RESULTS_RECOGNITION);
			if (resultList == null || resultList.size() < 1) {
				return;
			}
			// ���ص�result��һϵ�п���ƥ����ı�������ѡ����һ����Ҳ������ƥ����Ǹ�
			// String result = resultList.get(0);
			Log.d(MainActivity.class.getName(), resultList.get(0));
			ma.appendSpeechText(resultList.get(0));
			// ��Ҫ�������¿�������ʶ���������ܽ��г�ͨ��¼��
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
