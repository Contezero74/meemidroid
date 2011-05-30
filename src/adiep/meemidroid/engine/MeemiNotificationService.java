package adiep.meemidroid.engine;

/**
 * This class manages the Service intended to retreive notifications.
 * 
 * @author Lorenzo Mele, Andrea de Iacovo and Eros Pedrini
 * @version 0.1
 */

import adiep.meemidroid.MeemiDroidApplication;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MeemiNotificationService extends Service {
	private static final String TAG = "MeemiService";
	private Timer timer;
	//TBD this value should be read from preferences 
	int i = 0;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Toast.makeText(this, "Meemi Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
		
		int timerInterval;
		timerInterval = MeemiDroidApplication.Prefs.getNotificationInterval(); 
		
		if(timerInterval>0){
			
			timer = new Timer();
	
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					CheckNotification(getApplicationContext());
				}
	
			}, 0, timerInterval);
		}
}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		if(timer!=null){
			timer.cancel();
			timer = null;
		}
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		super.onStart(intent, startid);
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");

	}
	
	private void CheckNotification(Context c){
		Log.d(TAG, "Iterations "+(i++));

		try{
			//TBD: check notification, then show Notification if needed
		}catch(Exception e){
			Log.d(TAG, i+" - "+e.getMessage());
		}

	}
}