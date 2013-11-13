package com.crossrt.showtime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class DCwidget extends DashClockExtension
{
	protected static boolean gotClass;
	protected static String[] service_date,service_time,service_clas,service_subj;
	protected static int before,last;
	private static int hour,minute;
	private static Date currentTime,timeCompare1,timeCompare2;
	private static SimpleDateFormat inputParser = new SimpleDateFormat("HH:mm",Locale.US);
	private Intent todayIntent;
	private static SharedPreferences config;
	
	public void onCreate()
	{
		super.onCreate();
		todayIntent = new Intent(this,ClassToday.class);
		
		//Set fixed update time //12am
		Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY,0);
		calendar.set(Calendar.MINUTE,0);
		calendar.set(Calendar.SECOND,0);
		
		Intent intent = new Intent(this,AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager updateAlarm=(AlarmManager) this.getSystemService(ALARM_SERVICE);
		updateAlarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,pi);
		
		config = getSharedPreferences("config", Context.MODE_PRIVATE);
		before = Integer.parseInt(config.getString("set_time", "60"));
	}
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	protected void onInitialize(boolean isReconnect)
	{
		setUpdateWhenScreenOn(true);
	}
	
	protected void onUpdateData(int reason)
	{
		if(gotClass)
		{
			Calendar now = Calendar.getInstance();
			hour = now.get(Calendar.HOUR_OF_DAY);
			minute = now.get(Calendar.MINUTE);
			currentTime = parseDate(hour + ":" + minute);
			
			for(int i=last;i<service_time.length;i++)
			{
				timeCompare1 = parseDate(service_time[i].substring(0, 5));
				timeCompare2 = parseDate(service_time[i].substring(8,13));
				
				if(currentTime.before(timeCompare1))
				{
					long count = TimeUnit.MILLISECONDS.toMinutes(timeCompare1.getTime() - currentTime.getTime());
					if(count <= before){
						publishUpdate(new ExtensionData()
		                .visible(true)
		                .icon(R.drawable.dashclock)
		                .status(count+" Minute")
		                .expandedTitle("Next class:")
		                .expandedBody(count+" minutes left\nTime     : "+service_time[i]+"\nClass    : "+service_clas[i]+"\nSubject: "+service_subj[i])
		                .clickIntent(todayIntent));
						last=i;
						break;
					}else{publishUpdate(null);break;}
				}else if(currentTime.before(timeCompare2))
				{
					long count = timeCompare2.getTime() - currentTime.getTime();
					count = TimeUnit.MILLISECONDS.toMinutes(count);
					
					publishUpdate(new ExtensionData()
	                .visible(true)
	                .icon(R.drawable.dashclock)
	                .status(count+" Minute")
	                .expandedTitle("Current class:")
	                .expandedBody(count+" minutes to finish\nTime     : "+service_time[i]+"\nClass    : "+service_clas[i]+"\nSubject: "+service_subj[i])
	                .clickIntent(todayIntent));
					last=i;
					break;
				}else
				{
					publishUpdate(null);
				}
			}
		}else
		{
			publishUpdate(null);
		}
	}
	private Date parseDate(String date)
	{
		try
		{
			return inputParser.parse(date);
		}catch(java.text.ParseException e)
		{
			return new Date(0);
		}
	}
}
