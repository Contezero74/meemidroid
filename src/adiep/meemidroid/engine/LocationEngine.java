package adiep.meemidroid.engine;

import java.util.List;
import java.util.TimerTask;

import adiep.meemidroid.MeemiDroidApplication;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * This class represents the location support engine used to manage
 * the location information retrieved from the Android device
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.1
 */
public class LocationEngine {
	/**
	 * This constructor setups the location engine.
	 * 
	 * @param ME	The reference to the MeemiEngine; in fact the location engine has to be able to
	 * 				communicate with the MeemiEngine
	 * @param C		The current Android application context
	 */
	public LocationEngine(MeemiEngine ME) {
		MyMeemiEngine = ME;
		
		// location setup
		MyLocationManager = (LocationManager)MeemiDroidApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
		
		CurrentLocation = getLastBestLocation();
		
		MyLocationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	makeUseOfNewLocation(location);
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };
		  
		  MyGeoCoder = new Geocoder( MeemiDroidApplication.getContext() );
	}
	
	/**
	 * This method stops the synchronization of the user location with the Meemi server.
	 */
	public void stopLocationSync() {
		if (null != LocationSync) {
			LocationSync.cancel();
		
			LocationSync = null;
		}
		
		MyLocationManager.removeUpdates(MyLocationListener);
	}
	
	/**
	 * This method starts the synchronization of the user location with the Meemi server,
	 * every a specified range of time.
	 * 
	 * @param Minute the range of time used for the synchronization (expressed in minutes)
	 */
	public void startLocationSync(final int Minutes) {
		stopLocationSync();
		
		int LocationProviderTimeIntervall = Minutes;
		
		if (0 < Minutes) {
			LocationSync = new TimerTask() {
				public void run() {
					String Location = getCurrentLocation();
					
					Log.i("Location Sync", "Current Location: " + Location );
					
					if (null != Location) {
						MyMeemiEngine.postLocation(Location);
					}
				};
			};
			
			MyMeemiEngine.getSyncManager().schedule(LocationSync, 0, 60000*Minutes);
		} else {
			LocationProviderTimeIntervall = 5;
		}
		
		MyLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000*LocationProviderTimeIntervall, 100, MyLocationListener);
		MyLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000*LocationProviderTimeIntervall, 100, MyLocationListener);
	}
	
	/**
	 * This method returns the string representing the current location.
	 * 
	 * @return the string representing the current location
	 */
	public String getCurrentLocation() {
		String ResponseLocation = null; 
		
		try {
			if (null != CurrentLocation) {
				List<Address> PossibleAddresses = MyGeoCoder.getFromLocation(CurrentLocation.getLatitude(), CurrentLocation.getLongitude(), 1);
			
				if ( 0 < PossibleAddresses.size() ) {
					Address A = PossibleAddresses.get(0);
					int Accuracy = MeemiDroidApplication.Prefs.getLocationAccuracy();
					
					String Location = A.getCountryName();
					
					if (0 < Accuracy) { Location = A.getAdminArea() + ", " + Location; }
					if (1 < Accuracy) { Location = A.getSubAdminArea() + ", " + Location; }
					if (2 < Accuracy) { Location = A.getLocality() + ", " + Location; }
					if (3 < Accuracy) { Location = A.getThoroughfare() + ", " + Location; }
					
					ResponseLocation = Location;
				}
			}
		} catch (Exception e) {
			Log.d( "MeemiEngine", "Error during GeoCoder: " + e.toString() );
		}
		
		return ResponseLocation;
	}
	
	public final static int ONLYDURINGMESSAGESENDING = -1;
	
	/**
	 * This method modify the last know good location according to the arguments.
	 * 
	 * @param location	the possible new location
	 */
	private void makeUseOfNewLocation(Location location) {
		if ( isBetterLocation(location) ) {
			CurrentLocation = location;
		}
	}
	
	/**
	 * This method returns the last know location, between the GPS and the Network one.
	 * For this method newer is best :)
	 * 
	 * @return the last know best location
	 */
	private Location getLastBestLocation() {
		Location LocationGPS = MyLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location LocationNet = MyLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		
		long GPSLocationTime = 0;
		if (null != LocationGPS) { GPSLocationTime = LocationGPS.getTime(); }
		
		long NetLocationTime = 0;
		if (null != LocationNet) { NetLocationTime = LocationNet.getTime(); }
		
		if ( 0 < GPSLocationTime - NetLocationTime ) {
			return LocationGPS;
		} else {
			return LocationNet;
		}
			
	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 * 
	 * @param location  The new Location that you want to evaluate
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	 */
	private boolean isBetterLocation(Location location) {
	    if (CurrentLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - CurrentLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > CHECK_TIME;
	    boolean isSignificantlyOlder = timeDelta < -CHECK_TIME;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) ( location.getAccuracy() - CurrentLocation.getAccuracy() );
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider( location.getProvider(), CurrentLocation.getProvider() );

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	private static final int CHECK_TIME = 1000 * 60 * 2;
	
	private MeemiEngine MyMeemiEngine = null;
	
	private TimerTask LocationSync = null;
	private LocationManager MyLocationManager = null;
	private Location CurrentLocation = null;
	private LocationListener MyLocationListener = null;
	private Geocoder MyGeoCoder = null;
}
