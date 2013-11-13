package com.crossrt.showtime;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Main extends SherlockActivity
{
	private static final String DATABASE_UPDATED = "com.crossrt.showtime.DATABASE_UPDATED";
	private static final String UPDATE_FAILED = "com.crossrt.showtime.UPDATE_FAILED";
	
	private SharedPreferences config;
	private DBHelper helper;
	private SQLiteDatabase db;
	private Cursor cursor;
	private Updater updater;
	private BroadcastReceiver receiveUpdate;
	
	protected static String intakeCode,theme; 							//Settings
	protected static String filter_lecture,filter_lab,filter_tutorial; 	//Filter Settings
	protected ArrayList<Perclass> classes = new ArrayList<Perclass>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		config = getSharedPreferences("config", Context.MODE_PRIVATE);
		
		//Theme initialize
		theme = config.getString("theme", "Default");
		setTheme();
		getOverflowMenu();
		
		//Setup view layout
		setContentView(R.layout.main);
		
		//Show changelog when new version installed
		ChangeLog changelog = new ChangeLog(this);
		if (changelog.firstRun())
		{
			changelog.getLogDialog().show();
		}
	}
	@Override
	public void onResume()
	{
		super.onResume();
		
		//Make sure timetable is correct even preferences changed
		intakeCode = config.getString("intakeCode","");
		filter_lecture = config.getString("lecture", null);
		filter_lab = config.getString("lab", null);
		filter_tutorial = config.getString("tutorial", null);
		
		//Check whether first time or the intakeCode is empty
		if(config.getBoolean("first", true) || config.getString("intakeCode", "").equals(""))
		{
			Intent intent = new Intent(this,InitialPage.class);
			startActivity(intent);
			SharedPreferences.Editor editor = config.edit();
			editor.putBoolean("first", false);
			editor.commit();
		}else
		{
			//Attempt to read timetable from database
			if(readData())
			{
				writeToTable();
			}else
			{
				LinearLayout ll = (LinearLayout)findViewById(R.id.main_table);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				lp.topMargin = 10; lp.bottomMargin = 5;
				ll.removeAllViews();
				
				//Print a message if no timetable found
				LayoutInflater inflater = getLayoutInflater();
				View emptyTable = inflater.inflate(R.layout.empty_table,null);
				TextView emptyMessage = (TextView)emptyTable.findViewById(R.id.empty_message);
				emptyMessage.setText(R.string.no_class_found);
				ll.addView(emptyTable,lp);
			}
		}
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		if(updater!=null && updater.getStatus()!=AsyncTask.Status.FINISHED)
		{
			updater.cancel(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		if(theme.contains("Holo"))
		{
			inflater.inflate(R.menu.main_menu_holo, menu);
		}else
			inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_update:
			{
				pendingUpdate();
				return true;
			}
			case R.id.menu_preferences:
			{
				Intent intent = new Intent(this, Preferences.class);
				startActivity(intent);
				return true;
			}
			case R.id.menu_today:
			{
				Intent intent = new Intent(this, ClassToday.class);
				startActivity(intent);
				return true;
			}
			case R.id.menu_allclass:
			{
				Intent intent = new Intent(this, ClassAll.class);
				startActivity(intent);
				return true;
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}
	
	private void getOverflowMenu()
	{
		try
		{
			ViewConfiguration ViewConfig = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if(menuKeyField != null)
			{
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(ViewConfig, false);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private boolean isNetworkAvailable()
	{
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null;
	}
	
	private void setTheme()
	{
		if(Main.theme.equals("Red"))
		{
			setTheme(R.style.ThemeRed);
		}else if(Main.theme.equals("Yellow"))
		{
			setTheme(R.style.ThemeYellow);
		}else if(Main.theme.equals("Blue"))
		{
			setTheme(R.style.ThemeBlue);
		}else if(Main.theme.equals("Green"))
		{
			setTheme(R.style.ThemeGreen);
		}else if(Main.theme.equals("Brown"))
		{
			setTheme(R.style.ThemeBrown);
		}else if(Main.theme.equals("Holo Red"))
		{
			setTheme(R.style.HoloRed);
		}
		else if(Main.theme.equals("Holo Yellow"))
		{
			setTheme(R.style.HoloYellow);
		}
		else if(Main.theme.equals("Holo Blue"))
		{
			setTheme(R.style.HoloBlue);
		}
		else if(Main.theme.equals("Holo Green"))
		{
			setTheme(R.style.HoloGreen);
		}
	}
	
	public void pendingUpdate()
	{
		if(isNetworkAvailable())
		{
			if(!intakeCode.equals(""))
			{
				if(updater!=null && updater.getStatus()!=AsyncTask.Status.FINISHED)
				{
					updater.cancel(true);
				}
				
				//Update current timetable in layout when the database is updated.
				receiveUpdate = new BroadcastReceiver()
					{
						public void onReceive(Context context, Intent intent)
						{
							if(intent.getAction().equals(DATABASE_UPDATED))
							{
								readData();
								writeToTable();
								unregisterReceiver(this);
							}else
								unregisterReceiver(this);
						}
					};
				IntentFilter filter = new IntentFilter();
				filter.addAction(DATABASE_UPDATED);
				filter.addAction(UPDATE_FAILED);
				registerReceiver(receiveUpdate,filter);
				
				//Update timetable
				updater = new Updater(getApplicationContext(),intakeCode);
				updater.execute();
			}else
			{
				Toast.makeText(Main.this, R.string.set_intake, Toast.LENGTH_SHORT).show();
			}
		}else
		{
			Toast.makeText(Main.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Used to load the timetable from database to application variable.
	 * <br/>
	 * @return Status of reading data from database
	 */
	public boolean readData()
	{
		helper = new DBHelper(this, "timetableData",null,1);
		db = helper.getWritableDatabase();
		cursor = db.rawQuery("SELECT * FROM timetableData;", null);
		classes.clear();
		
		//If cursor!=null means there is timetable in database
		if(cursor != null && cursor.moveToFirst())
		{
			do
			{
				String date = cursor.getString(1);
				String time = cursor.getString(2);
				String classs = cursor.getString(3);
				String location = cursor.getString(4);
				String subject = cursor.getString(5);
				String lecturer = cursor.getString(6);
				
				Perclass perclass = new Perclass(date,time,classs,location,subject,lecturer);
				classes.add(perclass);
				
			}while(cursor.moveToNext());
			
			helper.close();
			return true;
		}else return false;
	}
	
	/**
	 * Used to write the timetable from variable to application layout.<br/>
	 * Any filter in derived class should override this function.
	 */
	public void writeToTable()
	{
		//Initialize
		boolean haveClass = false;	//Use to identify is that schedule available
		LinearLayout ll = (LinearLayout)findViewById(R.id.main_table);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.topMargin = 10; lp.bottomMargin = 5;
		ll.removeAllViews();
		
		if(!filter_lecture.equals("") || !filter_lab.equals("") || !filter_tutorial.equals(""))
		{
			if(filter_lecture.equals(""))
			{
				filter_lecture = "-L";
			}
			if(filter_lab.equals(""))
			{
				filter_lab = "-LAB";
			}
			if(filter_tutorial.equals(""))
			{
				filter_tutorial = "-T";
			}
		}
		
		//set table layout
		for(int i=0;i<classes.size();i++)
		{
			//Filter to match lecture/lab/tutorial
			String subject = classes.get(i).getSubject();
			if(
				subject.matches(".*"+filter_lecture)||
				subject.matches(".*"+filter_lab)||
				subject.matches(".*"+filter_tutorial)||
				subject.matches(".*-L")||
				subject.matches(".*-LAB")||
				subject.matches(".*-T") )//end if
			{
				LayoutInflater inflater = getLayoutInflater();
				View perClassTable = inflater.inflate(R.layout.perclass_table, null);
				
				TextView table_day = (TextView)perClassTable.findViewById(R.id.table_day);
				TextView table_date = (TextView)perClassTable.findViewById(R.id.table_date);
				TextView table_time = (TextView)perClassTable.findViewById(R.id.table_time);
				TextView table_location = (TextView)perClassTable.findViewById(R.id.table_location);
				TextView table_class = (TextView)perClassTable.findViewById(R.id.table_class);
				TextView table_subject = (TextView)perClassTable.findViewById(R.id.table_subject);
				TextView table_lecturer = (TextView)perClassTable.findViewById(R.id.table_lecturer);
				
				table_day.setText(classes.get(i).getDate().substring(0, 3));
				table_date.setText(classes.get(i).getDate().substring(5,11));
				table_time.setText(classes.get(i).getTime());
				table_location.setText(classes.get(i).getLocation());
				table_class.setText(classes.get(i).getClasses());
				table_subject.setText(classes.get(i).getSubject());
				table_lecturer.setText(classes.get(i).getLecturer());
		
				//add to LinearLayout
				ll.addView(perClassTable,lp);
				haveClass=true;
			}
		}
		
		//If there is no class after filter, show message
		if(!haveClass)
		{
			LayoutInflater inflater = getLayoutInflater();
			View emptyTable = inflater.inflate(R.layout.empty_table,null);
			TextView emptyMessage = (TextView)emptyTable.findViewById(R.id.empty_message);
			emptyMessage.setText(R.string.no_class_filter);
			ll.addView(emptyTable,lp);
		}
	}
}
