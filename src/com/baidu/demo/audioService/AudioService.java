package com.baidu.demo.audioService;

import java.io.File;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.baidu.demo.MainActivity;
import com.baidu.demo.utils.UploadSpeechText;

public class AudioService extends PhoneStateListener {
	private static final String TAG = AudioService.class.getName();
	private MediaPlayer mediaPlayer;
	private MediaRecorder recorder;
	private String OUTPUT_FILE = Environment.getExternalStorageDirectory()
			+ "/demo.3gpp";
	private MainActivity ma;
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		super.onCallStateChanged(state, incomingNumber);
		try {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				//停止百度语音识别的服务
				ma.stopSpeechRecognizer();
				// 挂断后回到空闲状态
//				stopRecording();
//				onDestroy();
				//此处开始做判断，如果录音超过10秒，则开始使用百度语音sdk来转换语音为文本
				//转换完成后，把得到的文本发回服务器，然后从服务器获得数据
				//最后渲染到界面上
				String text = ma.getSpeechText();
				Log.d(TAG, text);
				//上传文本到服务器
				new UploadSpeechText(ma).execute(text);
				
			case TelephonyManager.CALL_STATE_RINGING:
				// 来电状态
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				// 接听状态
				break;
			default:
					break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean longgerThan10s() {
		File outFile = new File(OUTPUT_FILE);
		if (outFile.length() > 20*1024) {
			return true;
		} else {
			return false;
		}
	}
	public void beginRecording() throws Exception {
		killMediaRecorder();
		File outFile = new File(OUTPUT_FILE);

		if (outFile.exists()) {
			outFile.delete();
		}

		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(OUTPUT_FILE);
		recorder.prepare();
		recorder.start();
	}

	private void killMediaRecorder() {
		if (recorder != null) {
			recorder.release();
		}
	}

	public void playRecording() throws Exception {
		killMediaPlayer();

		mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(OUTPUT_FILE);

		mediaPlayer.prepare();
		mediaPlayer.start();
	}

	private void killMediaPlayer() {
		if (mediaPlayer != null) {
			try {
				mediaPlayer.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void stopRecording() throws Exception {
		if (recorder != null) {
			recorder.stop();
		}
	}

	public void stopPlayingRecording() throws Exception {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}

	public void onDestroy() {
		/**
		File outFile = new File(OUTPUT_FILE);

		if (outFile.exists()) {
			outFile.delete();
		}
		 **/
		killMediaRecorder();
		killMediaPlayer();
	}
	public void setMa(MainActivity ma) {
		this.ma = ma;
	}

}
