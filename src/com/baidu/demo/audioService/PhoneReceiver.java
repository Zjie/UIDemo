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
		//����ǲ���
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			ma.startSpeechRecognizer(new Bundle());
		}
		/**
		try {
			//�������绰ʱ�������ٶȵ�����ʶ��������¼��
			ma.startSpeechRecognizer(new Bundle());
			//����������¼��
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
