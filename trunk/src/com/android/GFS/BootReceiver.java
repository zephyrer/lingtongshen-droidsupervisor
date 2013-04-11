package com.android.GFS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
	private static final String ACTION_SMS  = "android.provider.Telephony.SMS_RECEIVED";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		 if (intent.getAction().equals(ACTION_BOOT))
         {
		        Intent intent1=new Intent();
		        intent1.setClass(context, GoogleFrameWorkService.class);
		        context.startService(intent1); 
         }
		 else if(intent.getAction().equals(ACTION_SMS)) {
			 
			}

	}

}
