package com.baidu.demo.audioService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.baidu.demo.MainActivity;

public class PhoneReceiver extends BroadcastReceiver {
	private AudioService as;
	private MainActivity ma;
	@Override
	public void onReceive(Context context, Intent intent) {
		if (as == null) {
			return;
		}
		//如果是拨出
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			ma.startSpeechRecognizer(new Bundle());
		}
		/**
		try {
			//当拨出电话时，启动百度的语音识别器进行录音
			ma.startSpeechRecognizer(new Bundle());
			//不启动本地录音
//			as.beginRecording();
		} catch (Exception e) {
			e.printStackTrace();
		}
		**/
	}

	public void setAs(AudioService as) {
		this.as = as;
	}

	public void setMa(MainActivity ma) {
		this.ma = ma;
	}

}
