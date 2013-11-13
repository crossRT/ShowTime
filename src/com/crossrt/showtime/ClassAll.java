package com.crossrt.showtime;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ClassAll extends Main
{
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		writeToTable();
    }
	public boolean onCreateOptionsMenu(Menu menu) 
    {
    	MenuInflater inflater = getSupportMenuInflater();
    	if(Main.theme.contains("Holo"))
    	{
    		inflater.inflate(R.menu.home_menu_holo, menu);
    	}else
    		inflater.inflate(R.menu.home_menu, menu);
        return true;
    }
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    		case R.id.menu_home:
    		{
    			finish();
    			return true;
    		}
    	}
    	return super.onOptionsItemSelected(item);
    }
	@Override
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
				
		//If there is no class after filter, show message
		if(!haveClass)
		{
			LayoutInflater inflater = getLayoutInflater();
			View emptyTable = inflater.inflate(R.layout.empty_table,null);
			TextView emptyMessage = (TextView)emptyTable.findViewById(R.id.empty_message);
			emptyMessage.setText(R.string.today_no_class);
			ll.addView(emptyTable,lp);
		}
	}
}
