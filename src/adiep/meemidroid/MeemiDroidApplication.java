
package adiep.meemidroid;

import adiep.meemidroid.engine.MeemiEngine;
import adiep.meemidroid.engine.MeemiPreferences;
import android.app.Application;
import android.content.Context;

/**
 * This class represents is an extension of the Android Application
 * class: in this way we can use this class to manage all the global
 * object instance with a life equal to the application one.
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.2
 */
public class MeemiDroidApplication extends Application {
	
	/**
	 * Called when the application is starting, before any other application
	 * objects have been created. Implementations should be as quick as possible
	 * (for example using lazy initialization of state) since the time spent in
	 * this function directly impacts the performance of starting the first activity,
	 * service, or receiver in a process. If you override this method,
	 * be sure to call super.onCreate(). 
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		ApplicationContext = this;
		
		Engine = new MeemiEngine();
		
		Prefs = new MeemiPreferences();
		Prefs.load();
		
		if ( Prefs.isLocationEnabled() ) {
			Engine.setCurrentLocation( Prefs.getLastKnowLocation() );
			Engine.startLocationSync( Prefs.getLocationSyncMin() );
		} else {
			Engine.stopLocationSync();
		}
	}
	
	public static Context getContext() {
		return ApplicationContext;
	}

	/**
	 * The MeemiEngine use by the MeemiDroid client.
	 */
	public static MeemiEngine Engine = null;
	
	/**
	 * The MeemiDroid Client preferences.
	 */
	public static MeemiPreferences Prefs = null;
	
	public static final String USERS_AVATARS_CACHE = "MeemiDroidCache";
	
	
	private static Context ApplicationContext = null;
}