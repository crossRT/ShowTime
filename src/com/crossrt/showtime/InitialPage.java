package com.crossrt.showtime;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class InitialPage extends SherlockActivity
{
	private Spinner intakeSpinner,lectureSpinner,labSpinner,tutorialSpinner;
	private EditText intakeText;
	private ArrayAdapter<?> intakeAdapter,lectureAdapter,labAdapter,tutorialAdapter;
	private String intakeString="";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.initial_page);
        
        intakeText = (EditText)findViewById(R.id.intake_edit);
        
        //set intakes spinner
        intakeSpinner = (Spinner)findViewById(R.id.intake_spinner);
        intakeAdapter = ArrayAdapter.createFromResource(this, R.array.intake, android.R.layout.simple_spinner_item);
        intakeSpinner.setAdapter(intakeAdapter);
        
        //set lecture spinner
        lectureSpinner = (Spinner)findViewById(R.id.lecture_spinner);
        lectureAdapter = ArrayAdapter.createFromResource(this, R.array.lecture, android.R.layout.simple_spinner_dropdown_item);
        lectureSpinner.setAdapter(lectureAdapter);
        
        //set lab spinner
        labSpinner = (Spinner)findViewById(R.id.lab_spinner);
        labAdapter = ArrayAdapter.createFromResource(this, R.array.lab, android.R.layout.simple_spinner_dropdown_item);
        labSpinner.setAdapter(labAdapter);
        
        //set tutorial spinner
        tutorialSpinner = (Spinner)findViewById(R.id.tutorial_spinner);
        tutorialAdapter = ArrayAdapter.createFromResource(this, R.array.tutorial, android.R.layout.simple_spinner_dropdown_item);
        tutorialSpinner.setAdapter(tutorialAdapter); 
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	if(!intakeSpinner.getSelectedItem().toString().equals(""))
    	{
    		intakeString = intakeSpinner.getSelectedItem().toString();
    	}else if(!intakeText.getText().toString().equals(""))
    	{
    		intakeString = intakeText.getText().toString().toUpperCase();
    	}
    	switch(item.getItemId())
    	{
    		case R.id.menu_save:
    		{
    			if(!intakeString.equals(""))
    	        {
    	    		//set the intake, lecture, tutorial,
    	    		SharedPreferences config = getSharedPreferences("config", Context.MODE_PRIVATE);
    	    		SharedPreferences.Editor editor = config.edit();
    	    		editor.putString("intakeCode",intakeString);
    	    		editor.putString("lecture", lectureSpinner.getSelectedItem().toString());
    	    		editor.putString("lab", labSpinner.getSelectedItem().toString());
    	    		editor.putString("tutorial", tutorialSpinner.getSelectedItem().toString());
    	    		editor.putString("theme", "Default");
    	    		editor.commit();
    	    		Toast.makeText(InitialPage.this, R.string.change, Toast.LENGTH_SHORT).show();
    	    		finish();
    	        }else
    	        {
    	        	final int SHORT_DELAY = 2000;
    	        	Toast.makeText(this, R.string.miss_intakecode, SHORT_DELAY).show();
    	        }
    			return true;
    		}
    	}
    	return super.onOptionsItemSelected(item);
    }
}
