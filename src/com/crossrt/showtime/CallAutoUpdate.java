package com.crossrt.showtime;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class CallAutoUpdate extends BroadcastReceiver
{
	public void onReceive(Context context,Intent intent)
	{
		if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
		{
			SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
			boolean autoupdate = config.getBoolean("autoupdate", false);
			if(autoupdate)
			{
				Calendar calendar=Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_WEEK,7);
				calendar.set(Calendar.HOUR_OF_DAY,13);
				calendar.set(Calendar.MINUTE,0);
				calendar.set(Calendar.SECOND,0);
				Intent i = new Intent(context,CallAutoUpdate.class);
				i.setAction("AUTOUPDATE");
				PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
				AlarmManager updateAlarm=(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				updateAlarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,pi);
			}
		}else if(intent.getAction().equals("AUTOUPDATE"))
		{
			Intent i = new Intent(context,AutoUpdater.class);
			context.startService(i);
		}
	}	
}
