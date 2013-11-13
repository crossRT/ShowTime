package com.crossrt.showtime;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.RemoteViews;
import android.widget.Toast;

public class Widget extends AppWidgetProvider
{	
	private Context context;
	private static DBHelper helper;
	private static SQLiteDatabase db;
	private static Cursor cursor;	
  	private AppWidgetManager appWidgetManager;
  	private int[] appWidgetIds;
  	private static SharedPreferences config;
  	private static RemoteViews widgetLayout;
  	
  	private static String today,lecture,lab,tutorial;
  	private static String[] time,clas,subj;
  	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds)
	{
		this.context = context;
		this.appWidgetManager = appWidgetManager;
		this.appWidgetIds = appWidgetIds;
				
		Intent intent = new Intent(context,ClassToday.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
				
		widgetLayout = new RemoteViews(context.getPackageName(),R.layout.widget_layout);
		widgetLayout.setOnClickPendingIntent(R.id.widget_content, pendingIntent);
		
		readData();
		writeToWidget();
	}
	public void onReceive(Context context, Intent intent)
	{
		AppWidgetManager awm = AppWidgetManager.getInstance(context);
	    int[] ids = awm.getAppWidgetIds(new ComponentName(context, Widget.class));
	    this.onUpdate(context, awm, ids);
	}

	public void readData()
	{
		try
		{
			config = context.getSharedPreferences("config",Context.MODE_PRIVATE);
			getInfo();
			helper = new DBHelper(context, "timetableData",null,1);
			db = helper.getWritableDatabase();
			cursor = db.rawQuery("SELECT * FROM timetableData WHERE date LIKE '%"+today+"%' AND (subject LIKE '%"+lecture+"' OR subject LIKE '%"+lab+"' OR subject LIKE '%"+tutorial+"' OR subject LIKE '%-L' OR subject LIKE '%-LAB' OR subject LIKE '%-T');", null);
			int totalRow = cursor.getCount();
			time = new String[totalRow];
			clas = new String[totalRow];
			subj = new String[totalRow];
		}catch(NullPointerException e)
		{
			Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show();
		}
		if(cursor != null && cursor.moveToFirst())
		{
			int ii = 0;
			do
			{
		  		time[ii] = cursor.getString(2);
		  		clas[ii] = cursor.getString(3);
		  		subj[ii] = cursor.getString(5);
		  		ii++;
			}while(cursor.moveToNext());
			helper.close();
		}
	}
	public void writeToWidget()
	{
		widgetLayout.removeAllViews(R.id.widget_content);
		
		for(int i=0;i<time.length;i++)
		{
			RemoteViews widgetTable = new RemoteViews("com.crossrt.showtime",R.layout.widget_table);
			widgetTable.setTextViewText(R.id.widget_time, time[i]);
			widgetTable.setTextViewText(R.id.widget_class, clas[i]);
			widgetTable.setTextViewText(R.id.widget_subject, subj[i]);
			widgetLayout.addView(R.id.widget_content, widgetTable);
		}
		appWidgetManager.updateAppWidget(appWidgetIds, widgetLayout);
	}
	public void getInfo()
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
