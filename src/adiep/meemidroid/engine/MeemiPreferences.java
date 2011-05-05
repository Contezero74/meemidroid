package adiep.meemidroid.engine;

import adiep.meemidroid.MeemiDroidApplication;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * This class represents the MeemiDroid client preferences.
 * 
 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
 * @version 1.3
 */
public class MeemiPreferences {	
	/**
	 * This is the class constructor.
	 * 
	 * @see MeemiEngine
	 */
	public MeemiPreferences() {}
	
	/**
	 * This method returns true if the main dashboard has to use the flat style,
	 * otherwise it returns false.
	 * 
	 * @return	true if the main dashboard has to use the flat style, otherwise false
	 */
	public boolean isFlatDashboardEnabled() {
		return SharedPref.getBoolean(MEEMI_FLAT_DASHBOARD, true);
	}
	
	/**
	 * This method returns true if the fast scroll option for list views is enabled,
	 * otherwise it returns false.
	 * 
	 * @return	true if the fast scroll option for list views is enabled, otherwise false
	 */
	public boolean isFastScrollEnabled() {
		return SharedPref.getBoolean(MEEMI_FASTSCROLL, false);
	}
	
	/**
	 * This method returns true if the location retrieving is set in preferences.
	 * 
	 * @return true if the location retrieving is set
	 */
	public boolean isLocationEnabled() {
		return SharedPref.getBoolean(MEEMI_LOCATION, false);
	}
	
	/**
	 * This method returns the index of the location accuracy according to the following schema:
	 * 0: Country
	 * 1: Region + Country
	 * 2: District + Region + Country
	 * 3: City + District + Region + Country
	 * 4: Address + City + District + Region + Country
	 * 
	 * @return the location accuracy
	 */
	public int getLocationAccuracy() {
		return SharedPref.getInt(MEEMI_LOCATION_ACCURACY, 0);
	}
	
	/**
	 * This method returns the time range used to sync the location with Meemi (expressed in minutes).
	 * 
	 * @return the time range used to sync the location with Meemi (expressed in minutes)
	 */
	public int getLocationSyncMin() {
		int LocationSyncMin = 5;
		
		try {
			LocationSyncMin = Integer.parseInt( SharedPref.getString(MEEMI_LOCATION_SYNC_MIN, "5") );
		} catch (Exception ex) {
			LocationSyncMin = LocationEngine.ONLYDURINGMESSAGESENDING;
		}
		
		return LocationSyncMin;
	}
	
	/**
	 * This method returns the index of the spinbox used to set the time range to sync the location
	 * with Meemi.
	 * 
	 * @return the index of the spinbox used to set the time range to sync the location with Meemi
	 */
	public int getLocationSyncIndex() {
		return SharedPref.getInt(MEEMI_LOCATION_SYNC_INDEX, 1);
	}
	
	/**
	 * This method returns the last know location saved in the preferences.
	 * 
	 * @return the last know location saved in the preferences
	 */
	public String getLastKnowLocation() {
		return LastKnowLocation;
	}
	
	/**
	 * This method saves the last know location.
	 * 
	 * @param Location	the last know location
	 * 
	 */
	public void setLastKnowLocation(final String Location) {
		LastKnowLocation = Location;
	}
	
	/**
	 * This method returns true if the auto image resize for images to upload is enabled.
	 * 
	 * @return true if the auto image resize is enabled
	 */
	public boolean isImageResizeEnabled() {
		return SharedPref.getBoolean(MEEMI_IMAGERESIZE, false);
	}
	
	/**
	 * This method returns the index of the max dimension for the images to upload according to the following schema:
	 * 0: 320x200
	 * 1: 640x400
	 * 2: 640x480
	 * 3: 800x600
	 * 4: 1024x768
	 * 5: 1280x600
	 * 6: 1280x720
	 * 7: 1920x1080
	 * 
	 * @return the index of the max dimension for the images to upload
	 */
	public int getImageResizeIndex() {
		return SharedPref.getInt(MEEMI_IMAGEDIMS, 0);
	}
	
	/**
	 * This method is an utility method that return an array representing the
	 * max dimension of the image to resize according to the its argument.
	 * 
	 * @param Index	the index representing the max resize dimension
	 * 
	 * @return	an array representing the max dimension of the image to resize
	 */
	public int[] getImageResize() {
		int[] Size = new int[2];
		
		switch ( getImageResizeIndex() ) {
			case 1:	Size[0] = 640;	Size[1] = 400;	break;
			case 2:	Size[0] = 640;	Size[1] = 480;	break;
			case 3:	Size[0] = 800;	Size[1] = 600;	break;
			case 4:	Size[0] = 1024;	Size[1] = 768;	break;
			case 5:	Size[0] = 1280;	Size[1] = 600;	break;
			case 6:	Size[0] = 1280;	Size[1] = 720;	break;
			case 7:	Size[0] = 1920;	Size[1] = 1080;	break;
			default:
					Size[0] = 320;	Size[1] = 200;	break;
		}
				
		return Size; 
	}
	
	/**
	 * This method returns the quality wanted from the resized image.
	 * 
	 * @return	the quality wanted from the resized image
	 */
	public int getImageQuality() {
		return SharedPref.getInt(MEEMI_JPEGQUALITY, 85);
	}
	
	
	/**
	 * This method returns true if the Avatar caches need to be deleted at
	 * the start of MeemiDroid, otherwise returns false.
	 * 
	 * @return	true if the Avatar caches need to be deleted at the start of
	 * 			MeemiDroid
	 */
	public boolean isAvatarCacheToClean() {
		return SharedPref.getBoolean(MEEMI_CLEANAVATARS, false);
	}
	
	
	/**
	 * This method retrieves the current credentials from the shared preferences
	 * system of Android.
	 */
	public void load() {
		Context C = MeemiDroidApplication.getContext();
		
		SharedPref = PreferenceManager.getDefaultSharedPreferences( MeemiDroidApplication.getContext() );
		
		// Use MODE_WORLD_READABLE and/or MODE_WORLD_WRITEABLE to grant access to other applications
		SharedPreferences Old = C.getSharedPreferences(MEEMI_PREFS, Context.MODE_PRIVATE);
		
		// For back compatibility we have to:
		// 1) if the old preferences exist copy to the new ones
		if ( Old.getBoolean(OLD_ALREADY_REMOVED, false) ) {
			copyPreferences(SharedPref, Old);
		
			// 2) remove the oldest
			removePreferences(Old);
		}

		LastKnowLocation = SharedPref.getString(MEEMI_LAST_KNOW_LOCATION, "");
	}
	
	/**
	 * This method saved the current credentials into the shared preferences
	 * system of Android.
	 */
	public void save() {		
		// Use MODE_WORLD_READABLE and/or MODE_WORLD_WRITEABLE to grant access to other applications
		SharedPreferences P = PreferenceManager.getDefaultSharedPreferences( MeemiDroidApplication.getContext() );
		SharedPreferences.Editor E = P.edit();
		
		E.putString(MEEMI_LAST_KNOW_LOCATION, LastKnowLocation);		
		
		E.commit();
	}
	
	
	/**
	 * This method copies the data of the old preferences system into
	 * the new one.
	 * It is used to start the transition from the old preferences system
	 * to the new one.
	 * 
	 * @param New	the new {@link SharedPreferences} storage
	 * @param Old	the old {@link SharedPreferences} storage
	 */
	private static final void copyPreferences(SharedPreferences New, final SharedPreferences Old) {
		SharedPreferences.Editor E = New.edit();
		
		// location
		E.putBoolean( MEEMI_LOCATION, Old.getBoolean(OLD_MEEMI_LOCATION, false) );		
		E.putInt( MEEMI_LOCATION_ACCURACY, Old.getInt(OLD_MEEMI_LOCATION_ACCURACY, 0) );		
		E.putInt( MEEMI_LOCATION_SYNC_MIN, Old.getInt(OLD_MEEMI_LOCATION_SYNC_MIN, 5) );
		E.putInt( MEEMI_LOCATION_SYNC_INDEX, Old.getInt(OLD_MEEMI_LOCATION_SYNC_INDEX, 1) );		
		E.putString( MEEMI_LAST_KNOW_LOCATION, Old.getString(OLD_MEEMI_LAST_KNOW_LOCATION, "") );
		
		// images resize
		E.putBoolean( MEEMI_IMAGERESIZE, Old.getBoolean(OLD_MEEMI_IMAGERESIZE, false) );
		E.putInt( MEEMI_IMAGEDIMS, Old.getInt(OLD_MEEMI_IMAGEDIMS, 0) );
		E.putInt( MEEMI_JPEGQUALITY, Old.getInt(OLD_MEEMI_JPEGQUALITY, 85) );
		
		E.commit();
	}
	
	/**
	 * This method removes the old preferences data stored within the application.
	 * This method is used after the execution of
	 * {@link MeemiPreferences#copyPreferences(SharedPreferences, SharedPreferences)}
	 * in order to close the transition to the new preferences system.
	 * 
	 * @param P	the {@link SharedPreferences} to remove
	 */
	private static final void removePreferences(final SharedPreferences P) {
		SharedPreferences.Editor E = P.edit();
		
		// location
		E.remove(OLD_MEEMI_LOCATION);
		E.remove(OLD_MEEMI_LOCATION_ACCURACY);
		E.remove(OLD_MEEMI_LOCATION_SYNC_MIN);
		E.remove(OLD_MEEMI_LOCATION_SYNC_INDEX);
		
		// image resize
		E.remove(OLD_MEEMI_IMAGERESIZE);
		E.remove(OLD_MEEMI_IMAGEDIMS);
		E.remove(OLD_MEEMI_JPEGQUALITY);
		
		// set that we have removed the old preferences system
		E.putBoolean(OLD_ALREADY_REMOVED, true);
		
		E.commit();
	}
	
	private SharedPreferences SharedPref = null;
	
	private static final String MEEMI_PREFS = "MeemiDroidPreferences";
	
	// location
	private static final String MEEMI_LOCATION = "UseLocation";
	private static final String MEEMI_LOCATION_ACCURACY = "UseLocationAccurancy";
	private static final String MEEMI_LOCATION_SYNC_MIN = "LocationSyncMin";
	private static final String MEEMI_LOCATION_SYNC_INDEX = "LocationSyncIndex";
	private static final String MEEMI_LAST_KNOW_LOCATION = "LastKnowLocation";
	
	private String LastKnowLocation = "";
	
	
	// images dimension
	private static final String MEEMI_IMAGERESIZE = "CBAutoManagementImage";
	private static final String MEEMI_IMAGEDIMS = "MaxImageDimensionIndex";
	private static final String MEEMI_JPEGQUALITY = "JpegQuality";
	
	// general
	// - Avatars
	private static final String MEEMI_CLEANAVATARS = "CBAutoCleanAvatars";
	// - UI
	private static final String MEEMI_FLAT_DASHBOARD = "CBUseFlatDashboard";
	private static final String MEEMI_FASTSCROLL = "CBActiveFastScrollListView";
	
	
	// old Preferences:
	// location
	private static final String OLD_MEEMI_LOCATION = "UseLocation";
	private static final String OLD_MEEMI_LOCATION_ACCURACY = "UseLocationAccurqacy";
	private static final String OLD_MEEMI_LOCATION_SYNC_MIN = "LocationSyncMin";
	private static final String OLD_MEEMI_LOCATION_SYNC_INDEX = "LocationSyncIndex";
	private static final String OLD_MEEMI_LAST_KNOW_LOCATION = "LastKnowLocation";
	// images dimension
	private static final String OLD_MEEMI_IMAGERESIZE = "UseImageResize";
	private static final String OLD_MEEMI_IMAGEDIMS = "MaxImageDimensionIndex";
	private static final String OLD_MEEMI_JPEGQUALITY = "JpegQuality";
	
	private static final String OLD_ALREADY_REMOVED = "OldPreferencesAlreadyRemoved";
}
