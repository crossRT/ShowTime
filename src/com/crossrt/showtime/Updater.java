package com.crossrt.showtime;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

public class Updater extends AsyncTask<String, Integer, String>
{
	private static final String DATABASE_UPDATED = "com.crossrt.showtime.DATABASE_UPDATED";
	private static final String UPDATE_FAILED = "com.crossrt.showtime.UPDATE_FAILED";
	private static final int RETRY_LIMIT = 3;
	
	Context context;
	boolean success = false;
	boolean emptyTable = false;
	
	private Document doc = null,parseWeek = null;
    private Elements tableRow = null;
	private String weekOnly;
	
	private SharedPreferences config;
	private String intakeCode;
	
    /**
	 * AsyncTask operation, for update timetable from APU school server.
	 * <br/>
	 * @author crossRT
     * @param context Context of the application.
     * @param intakeCode The intake's timetable you want to receive.
     */
	public Updater(Context context,String intakeCode)
	{
		this.context=context;
		this.intakeCode=intakeCode;
	}
	
	/**
	 * AsyncTask operation, for update timetable from APU school server.
	 * <br/>
	 * @author crossRT
     * @param context Context of the application.
     */
	public Updater(Context context)
	{
		this.context=context;
		config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		intakeCode = config.getString("intakeCode","");
	}
	
	//Step 1: initial everything
	@Override
	protected void onPreExecute()
	{
		Toast.makeText(context, R.string.downloading, Toast.LENGTH_SHORT).show();
	}
	
	//Step 2: the whole update module, will not cause UI freeze
	@Override
	protected String doInBackground(String... params)
	{
    	int count=0;
    	while(count<RETRY_LIMIT) //Reconnect to prevent weak connection
    	{
    		try
    		{
    			//get week information
        		parseWeek = (Document)Jsoup.connect("http://webspace.apiit.edu.my/intake-timetable/").get();
        		weekOnly = parseWeek.select("select#week").select("option[value]").first().attr("value").toString();
        		
        		//get timetable page
    			doc = (Document)Jsoup
    					.connect("http://webspace.apiit.edu.my/intake-timetable/intake-result.php")
    					.data("selectIntakeAll",intakeCode)
    					.data("week",weekOnly)
    					.timeout(4000)
    					.post();
    			success = true;
                break;
    		}
    		catch(IOException e)
    		{
    			count++;
    		}
    	}
    	
    	//Execute when connection success, avoid doc.select() NullPointerException
    	if(success)
    	{	
    		DBHelper helper;
    	    SQLiteDatabase db;
    	    ContentValues values;
    		String[] date,time,clas,loca,subj,lect;
    		
    		tableRow = doc.select("table.timetable-display");
			tableRow = tableRow.select("tr:has(td)");
			if(tableRow.size()!=0)
    		{
        		//set all array size 
        		date = new String[tableRow.size()];
        		time = new String[tableRow.size()];
        		clas = new String[tableRow.size()];
        		loca = new String[tableRow.size()];
        		subj = new String[tableRow.size()];
        		lect = new String[tableRow.size()];
        		
        		//get DATA from Elements
        		for(int i=0; i<tableRow.size();i++)
        		{
        			Element currentRow = tableRow.get(i);
        			date[i] = currentRow.child(0).text();
        			time[i] = currentRow.child(1).text();
        			clas[i] = currentRow.child(2).text();
        			loca[i] = currentRow.child(3).text();
        			subj[i] = currentRow.child(4).text();
        			lect[i] = currentRow.child(5).text();
        		}
        		
        		//Store to database
        		helper = new DBHelper(context, "timetableData",null,1);
        		db = helper.getWritableDatabase();
        		db.delete("timetableData", null, null); //delete previous data
        		values = new ContentValues();			//container of per-class
        		for(int i=0;i<tableRow.size();i++)
        		{
        			values.put("date",date[i]);
        			values.put("time",time[i]);
        			values.put("class",clas[i]);
        			values.put("location",loca[i]);
        			values.put("subject",subj[i]);
        			values.put("lecturer",lect[i]);
        			db.insert("timetableData","",values);
        			values.clear();
        		}
        		helper.close();
        		
    		}else emptyTable = true;
    	}
    	return null;	
    }
	
	//Step 3: Anything will change UI put here! If not will throw RuntimeException
	@Override
	protected void onPostExecute(String result)
	{
		super.onPostExecute(result);
		if(success)
		{
			if(!emptyTable)
			{
	    		Intent updateWidget =new Intent();
	    		updateWidget.setAction(DATABASE_UPDATED);
	    		context.sendBroadcast(updateWidget);
	    		
	    		Toast.makeText(context, R.string.update_success, Toast.LENGTH_SHORT).show();
			}else
			{
				Toast.makeText(context, R.string.intake_no_class, Toast.LENGTH_SHORT).show();
			}
		}else
		{
			Intent updateAll =new Intent();
			updateAll.setAction(UPDATE_FAILED);
    		context.sendBroadcast(updateAll);
    		
			Toast toast = Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT);
            toast.show();
		}
	}
}