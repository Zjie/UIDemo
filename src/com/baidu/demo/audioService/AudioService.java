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
				//ֹͣ�ٶ�����ʶ��ķ���
				ma.stopSpeechRecognizer();
				// �ҶϺ�ص�����״̬
//				stopRecording();
//				onDestroy();
				//�˴���ʼ���жϣ����¼������10�룬��ʼʹ�ðٶ�����sdk��ת������Ϊ�ı�
				//ת����ɺ󣬰ѵõ����ı����ط�������Ȼ��ӷ������������
				//�����Ⱦ��������
				String text = ma.getSpeechText();
				Log.d(TAG, text);
				//�ϴ��ı���������
				new UploadSpeechText(ma).execute(text);
				
			case TelephonyManager.CALL_STATE_RINGING:
				// ����״̬
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				// ����״̬
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
