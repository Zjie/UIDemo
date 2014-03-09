package com.baidu.demo.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class Alerts {

	public static void showAlert(String message, Context ctx) {
		// Create a builder
		Builder builder = new Builder(ctx);
		builder.setTitle("ÌבÊ¾");
		builder.setMessage(message);
		// add buttons and listener
		builder.setPositiveButton("Ok", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});

		// Create the dialog
		AlertDialog ad = builder.create();

		// show
		ad.show();
	}

}