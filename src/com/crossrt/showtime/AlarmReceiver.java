package com.crossrt.showtime;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver
{
	private static DBHelper helper;
	private static SQLiteDatabase db;
	private static Cursor cursor;
	private int totalRow;
	private SharedPreferences config;
	private static String today,lecture,lab,tutorial;
  	
	@Override
	public void onReceive(Context context,Intent intent)
	{
		config = context.getSharedPreferences("config",Context.MODE_PRIVATE);
		getInfo();
		try
		{
			helper = new DBHelper(context, "timetableData",null,1);
			db = helper.getWritableDatabase();
			cursor = db.rawQuery("SELECT * FROM timetableData WHERE date LIKE '%"+today+"%' AND (subject LIKE '%"+lecture+"' OR subject LIKE '%"+lab+"' OR subject LIKE '%"+tutorial+"');", null);
			totalRow = cursor.getCount();
		}catch(NullPointerException e)
		{
			Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show();
		}
			
		if(cursor != null && cursor.moveToFirst())
		{
			DCwidget.last=0;
			DCwidget.gotClass=true;
			DCwidget.service_time = new String[totalRow];
			DCwidget.service_clas = new String[totalRow];
			DCwidget.service_subj = new String[totalRow];
			int i = 0;
			do
			{
				DCwidget.service_time[i] = cursor.getString(2);
				DCwidget.service_clas[i] = cursor.getString(3);
				DCwidget.service_subj[i] = cursor.getString(5);
			 	i++;
			}while(cursor.moveToNext());
		}else
		{
			DCwidget.gotClass=false;
		}
		helper.close();
	}
	
	private void getInfo()
	{
		today = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
		lecture = config.getString("lecture", "");
		lab = config.getString("lab", "");
		tutorial = config.getString("tutorial", "");
		if(!lecture.equals("") || !lab.equals("") || !tutorial.equals(""))
		{
			if(lecture.equals(""))
			{
				lecture = "-L";
			}
			if(lab.equals(""))
			{
				lab = "-LAB";
			}
			if(tutorial.equals(""))
			{
				tutorial = "-T";
			}
		}
	}
}
