package adiep.meemidroid.engine;

import adiep.meemidroid.MeemiDroidApplication;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class represents the MeemiDroid client preferences.
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.3
 */
public class MeemiPreferences {	
	/**
	 * This is the class constructor.
	 * 
	 * @see MeemiEngine
	 */
	public MeemiPreferences() {}
	
	/**
	 * This method returns true if the location retrieving is set in preferences.
	 * 
	 * @return true if the location retrieving is set
	 */
	public boolean isLocationEnabled() {
		return UseLocation;
	}
	
	/**
	 * This method sets the location retrieving into preferences.
	 * 
	 * @param B true if the location has to be retrieved, false otherwise
	 */
	public void setLocation(final boolean B) {
		UseLocation = B;
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
		return UseLocationAccuracy;
	}
	
	/**
	 * This method sets the index of the location accuracy according to the following schema:
	 * 0: Country
	 * 1: Region + Country
	 * 2: District + Region + Country
	 * 3: City + District + Region + Country
	 * 4: Address + City + District + Region + Country
	 * 
	 * @param A the location accuracy
	 */
	public void setLocationAccuracy(final int A) {
		UseLocationAccuracy = A;
	}
	
	/**
	 * This method returns the time range used to sync the location with Meemi (expressed in minutes).
	 * 
	 * @return the time range used to sync the location with Meemi (expressed in minutes)
	 */
	public int getLocationSyncMin() {
		return LocationSyncMin;
	}
	
	/**
	 * This method returns the index of the spinbox used to set the time range to sync the location
	 * with Meemi.
	 * 
	 * @return the index of the spinbox used to set the time range to sync the location with Meemi
	 */
	public int getLocationSyncIndex() {
		return LocationSyncIndex;
	}
	
	/**
	 * This method sets the time range (and the related spinbox index) used to sync the location with Meemi.
	 * @param Min		the time range used to sync the location with Meemi (expressed in minutes)
	 * @param Index		the index of the spinbox used to set the time range to sync the location with Meemi
	 */
	public void setLocationSync(final int Min, final int Index) {
		LocationSyncMin = Min;
		LocationSyncIndex = Index;
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
		return UseImageResize;
	}
	
	/**
	 * This method sets the auto image resize for images to upload.
	 * 
	 * @param B true if the auto image resize is enabled, false otherwise
	 */
	public void setImageResize(final boolean B) {
		UseImageResize = B;
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
		return MaxImageDimensionIndex;
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
		
		switch (MaxImageDimensionIndex) {
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
	 * This method sets the index of the max dimension for the images to upload according to the following schema:
	 * 0: 320x200
	 * 1: 640x400
	 * 2: 640x480
	 * 3: 800x600
	 * 4: 1024x768
	 * 5: 1280x600
	 * 6: 1280x720
	 * 7: 1920x1080
	 * 
	 * @param	the index of the max dimension
	 */
	public void setImageResizeIndex(final int A) {
		MaxImageDimensionIndex = A;
	}
	
	/**
	 * This method returns the quality wanted from the resized image.
	 * 
	 * @return	the quality wanted from the resized image
	 */
	public int getImageQuality() {
		return ImageQuality;
	}
	
	/**
	 * This method sets the quality wanted from the resized image
	 * 
	 * @param Q	the quality wanted from the resized image
	 */
	public void setImageQuality(final int Q) {
		ImageQuality = Q;
	}
	
	
	/**
	 * This method retrieves the current credentials from the shared preferences
	 * system of Android.
	 */
	public void load() {
		Context C = MeemiDroidApplication.getContext();
		
		// Use MODE_WORLD_READABLE and/or MODE_WORLD_WRITEABLE to grant access to other applications
		SharedPreferences P = C.getSharedPreferences(MEEMI_PREFS, Context.MODE_PRIVATE);
		
		// location
		UseLocation = P.getBoolean(MEEMI_LOCATION, false);
		
		UseLocationAccuracy = P.getInt(MEEMI_LOCATION_ACCURACY, 0);
		
		LocationSyncMin = P.getInt(MEEMI_LOCATION_SYNC_MIN, 5);
		LocationSyncIndex = P.getInt(MEEMI_LOCATION_SYNC_INDEX, 1);
		
		LastKnowLocation = P.getString(MEEMI_LAST_KNOW_LOCATION, "");
		
		// images resize
		UseImageResize = P.getBoolean(MEEMI_IMAGERESIZE, false);
		MaxImageDimensionIndex = P.getInt(MEEMI_IMAGEDIMS, 0);
		ImageQuality = P.getInt(MEEMI_JPEGQUALITY, 85);
	}
	
	/**
	 * This method saved the current credentials into the shared preferences
	 * system of Android.
	 */
	public void save() {
		Context C = MeemiDroidApplication.getContext();
		
		// Use MODE_WORLD_READABLE and/or MODE_WORLD_WRITEABLE to grant access to other applications
		SharedPreferences P = C.getSharedPreferences(MEEMI_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor E = P.edit();
		
		// location
		E.putBoolean(MEEMI_LOCATION, UseLocation);
		
		E.putInt(MEEMI_LOCATION_ACCURACY, UseLocationAccuracy);
		
		E.putInt(MEEMI_LOCATION_SYNC_MIN, LocationSyncMin);
		E.putInt(MEEMI_LOCATION_SYNC_INDEX, LocationSyncIndex);
		
		E.putString(MEEMI_LAST_KNOW_LOCATION, LastKnowLocation);
		
		// images resize
		E.putBoolean(MEEMI_IMAGERESIZE, UseImageResize);
		E.putInt(MEEMI_IMAGEDIMS, MaxImageDimensionIndex);
		E.putInt(MEEMI_JPEGQUALITY, ImageQuality);
		
		
		E.commit();
	}
	
	private static final String MEEMI_PREFS = "MeemiDroidPreferences";
	
	// location
	private static final String MEEMI_LOCATION = "UseLocation";
	private static final String MEEMI_LOCATION_ACCURACY = "UseLocationAccurqacy";
	private static final String MEEMI_LOCATION_SYNC_MIN = "LocationSyncMin";
	private static final String MEEMI_LOCATION_SYNC_INDEX = "LocationSyncIndex";
	private static final String MEEMI_LAST_KNOW_LOCATION = "LastKnowLocation";
	
	private boolean UseLocation = false;
	private int UseLocationAccuracy = 0;
	private int LocationSyncMin = 5;
	private int LocationSyncIndex = 1;
	private String LastKnowLocation = "";
	
	
	// images dimension
	private static final String MEEMI_IMAGERESIZE = "UseImageResize";
	private static final String MEEMI_IMAGEDIMS = "MaxImageDimensionIndex";
	private static final String MEEMI_JPEGQUALITY = "JpegQuality";
	
	private boolean UseImageResize = false;
	private int MaxImageDimensionIndex = 0;
	private int ImageQuality	= 85;
}
