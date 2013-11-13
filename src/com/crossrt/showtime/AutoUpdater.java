package com.crossrt.showtime;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

public class AutoUpdater extends Service
{
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	public int onStartCommand(Intent intent,int flags,int startId)
	{
		if(isConnected(this))
		{
			Updater updater = new Updater(this);
			updater.execute();
			stopSelf();
		}else
		{
			BroadcastReceiver pendingUpdate = new BroadcastReceiver()
				{
					public void onReceive(Context context, Intent intent)
					{
						if(isConnected(context))
						{
							Updater updater = new Updater(AutoUpdater.this);
							updater.execute();
							unregisterReceiver(this);
							stopSelf();
						}
					}
				};
			registerReceiver(pendingUpdate,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
		return START_STICKY;
	}
	
	public static boolean isConnected(Context context)
	{
	    ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = null;
	    if (connectivityManager != null)
	    {
	        networkInfo = connectivityManager.getActiveNetworkInfo();
	    }
	    return networkInfo == null ? false : networkInfo.getState() == NetworkInfo.State.CONNECTED;
	}
}
