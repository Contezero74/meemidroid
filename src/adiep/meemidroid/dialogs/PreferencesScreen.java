package adiep.meemidroid.dialogs;

import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import adiep.meemidroid.dialogs.settings.CredentialsSettingDialog;
import adiep.meemidroid.engine.MeemiEngine;
import adiep.meemidroid.engine.MeemiNotificationService;
import adiep.meemidroid.support.compatibility.SeekBarAndroidPreference;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * This class represents the preferences screen used to configure the
 * application.
 * 
 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
 * @version 1.2
 */
public class PreferencesScreen extends PreferenceActivity {
	/**
     * This method is called when the activity is first created.
     * It setups the interface and show the splash screen
     * if the application is starting.
     * 
     * @param savedInstanceState	if the activity is being re-initialized
     * 		 						after previously being shut down then this
     * 								Bundle contains the data it most recently
     * 								supplied in {@link #onSaveInstanceState(Bundle)}.
     */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		ApplicationPreferences = PreferenceManager.getDefaultSharedPreferences( MeemiDroidApplication.getContext() );
		
		CredentialBtn = findPreference("CredentialConfigPreference");
		CredentialBtn.setSummary( MeemiDroidApplication.Engine.getCredentials().getUsername() );
		CredentialBtn.setOnPreferenceClickListener( new CredentialPreferenceClick() );
		
		// image resize		
		EnableImage = (CheckBoxPreference)findPreference("CBAutoManagementImage");
		EnableImage.setOnPreferenceChangeListener( new EnableImageChangeListener() );
		
		ImageSize = (ListPreference)findPreference("LstImageSize");
		ImageSize.setSummary( ImageSize.getValue() );
		ImageQuality = (SeekBarAndroidPreference)findPreference("JpegQuality");
		
		// we need to use Prefs workaround in order to avoid the indeterminate state of the
		// Preference Progress Screen
		ImageQuality.setSummary( Integer.toString( MeemiDroidApplication.Prefs.getImageQuality() ) );
		
		ImagePreferencesChangeListener IP = new ImagePreferencesChangeListener();
		ImageSize.setOnPreferenceChangeListener(IP);
		ImageQuality.setOnPreferenceChangeListener(IP);
				
		changeImageEnableStatus( EnableImage.isChecked() );
		
		// notification
		EnableNotification = (CheckBoxPreference)findPreference("UseNotification");
		EnableNotification.setOnPreferenceChangeListener( new EnableNotificationChangeListener() );

		
		// location
		EnableLocation = (CheckBoxPreference)findPreference("UseLocation");		
		EnableLocation.setOnPreferenceChangeListener( new EnableLocationChangeListener() );
		
		LocationAccurancy = (ListPreference)findPreference("UseLocationAccurancyList");
		LocationAccurancy.setSummary( LocationAccurancy.getValue() );
		LocationSync = (ListPreference)findPreference("LocationSyncMin");
		LocationSync.setSummary( LocationSync.getValue() );
		
		LocationListsChangeListener LL =  new LocationListsChangeListener();
		LocationAccurancy.setOnPreferenceChangeListener(LL);
		LocationSync.setOnPreferenceChangeListener(LL);
		
		changeLocationEnableStatus( EnableLocation.isChecked() );
		
		
		// general
		// - Avatars
		CleanAvatarsCacheBtn = findPreference("AvatarsCleanCacheBtn");
		CleanAvatarsCacheBtn.setOnPreferenceClickListener( new CleanAvatarsCacheClick() );
	}
	
	/**
	 * This method is a callback for creating dialogs that are managed (saved
	 * and restored) for you by the activity.
	 * 
	 * @param id	the id of the dialog
	 * 
	 * @return	the dialog. If you return null, the dialog will not be created.
	 */
    @Override
	protected Dialog onCreateDialog(int id) {
		Dialog D = null;
		
		switch (id) {
			case SETTING_CREDENTIAL_DIALOG:
				/* NOTE: this is a workaround to have the possibility to have
				 * some post-processing after this dialog has been close.
				 * The right way should be to use D.setOnDismissListener, but it
				 * seems not working.
				 */
				D = new CredentialsSettingDialog( this, MeemiDroidApplication.Engine.getCredentials(), new CredentialSettingDialogDismissing() );
				//D.setOnDismissListener( new CredentialSettingDialogDismissing() );
				break;
			default:
				// nothing to do
		}
		
		return D;
	}
    
    /**
     * This method check if the location sync need to be enable or
     * disable and setup the system according.
     */
    private void enableDisableLocationSync() {
    	// location sync
        if ( EnableLocation.isChecked() ) {
                MeemiDroidApplication.Engine.startLocationSync( MeemiDroidApplication.Prefs.getLocationSyncMin() );
                
        } else {
                MeemiDroidApplication.Engine.stopLocationSync();
        }
    }
    
    /**
     * This method check if the notification need to be enable or
     * disable and setup the system according.
     */
    private void enableDisableNotificationSync(boolean newvalue) {
		SharedPreferences.Editor E = ApplicationPreferences.edit();
		
		//TBD: this functions is supposed to toggle the activation of
		//other notification settings, now it's just enabling the logging of
		//a debug message

    	// location sync
        if ( newvalue ) {
			E.putInt( "NotificationInterval", 1000*3 ); //set every 3 seconds
    		startService(new Intent(this, MeemiNotificationService.class));
        } else {
    		stopService(new Intent(this, MeemiNotificationService.class));
    		E.putInt( "NotificationInterval", 0 );
        }
		E.commit();
    }
    
    
    
	/**
	 * This method changes the enable status of the preferences widgets related
	 * with the Image settings.
	 * 
	 * @param Enabled	the new status of the widgets
	 */
	private void changeImageEnableStatus(final boolean Enabled) {
		ImageSize.setEnabled(Enabled);
		ImageQuality.setEnabled(Enabled);
	}
	
	/**
	 * This method changes the enable status of the preferences widgets related
	 * with the Location settings.
	 * 
	 * @param Enabled	the new status of the widgets
	 */
	private void changeLocationEnableStatus(final boolean Enabled) {
		LocationAccurancy.setEnabled(Enabled);
		LocationSync.setEnabled(Enabled);
	}
	
	
	/**
	 * This private class manages the interaction with the "User Credential"
	 * widget, that works as a button.
	 * This class shows the User Crfedential dialog.
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 * 
	 * @see CredentialsSettingDialog
	 */
	private final class CredentialPreferenceClick implements OnPreferenceClickListener {
		@Override
		public boolean onPreferenceClick(Preference arg0) {
			showDialog(SETTING_CREDENTIAL_DIALOG);
			
			return true;
		}
	}
	
	/**
	 * This private class manages the dismiss of the Credential Setting Dialog Box;
	 * It update the "User Credential" widget summary.
	 * 
	 *  @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 */
	private final class CredentialSettingDialogDismissing implements DialogInterface.OnDismissListener {
		@Override
		public void onDismiss(DialogInterface arg0) {
			CredentialBtn.setSummary( MeemiDroidApplication.Engine.getCredentials().getUsername() );
		}
		
	}
	
	/**
	 * This private class manages to enable/disable the widgets related with
	 * Image settings according to the user selection.
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 */
	private final class EnableImageChangeListener implements OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			// the not operator is needed because this listener has been called
			// before the real status change of the combobox
			changeImageEnableStatus( !EnableImage.isChecked() );
			
			return true;
		}
	}
	
	/**
	 * This private class manages to enable/disable the widgets related with
	 * Location settings according to the user selection.
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 */
	private final class EnableLocationChangeListener implements OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			// the not operator is needed because this listener has been called
			// before the real status change of the combobox
			changeLocationEnableStatus( !EnableLocation.isChecked() );
			
			// location sync
			enableDisableLocationSync();
			
			return true;
		}
	}
	
	/**
	 * This private class manages to enable/disable the widgets related with
	 * Notification settings according to the user selection.
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 */
	private final class EnableNotificationChangeListener implements OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			// the not operator is needed because this listener has been called
			// before the real status change of the combobox
			//changeNotificationEnableStatus( !EnableNotification.isChecked() );
			
			// location sync
			enableDisableNotificationSync(newValue.toString()=="true");
			
			return true;
		}
	}
	
	/**
	 * This private class manages the user selecion on the Image settings
	 * widget. This class transforms the list selection into a more
	 * usable representation for the MeemiDroid application.
	 * This class updates the Image widgets summary according to the user
	 * selection, too. 
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 */
	private final class LocationListsChangeListener implements OnPreferenceChangeListener  {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			SharedPreferences.Editor E = ApplicationPreferences.edit();
			
			if ( preference.getKey().equals("UseLocationAccurancyList") ) {			
				E.putInt( "UseLocationAccurancy", LocationAccurancy.findIndexOfValue( (String)newValue ) );
				LocationAccurancy.setSummary( (String)newValue );
			}
			
			if ( preference.getKey().equals("LocationSyncMin") ) {			
				E.putInt( "LocationSyncIndex", LocationSync.findIndexOfValue( (String)newValue ) );
				LocationSync.setSummary( (String)newValue );
			}
			
			E.commit();
			
			// location sync
			enableDisableLocationSync();
			
			return true;
		}
	}
	
	/**
	 * This private class manages the user selecion on the Image settings
	 * widget. This class transforms the list selection into a more
	 * usable representation for the MeemiDroid application.
	 * This class updates the Location widgets summary according to the user
	 * selection, too. 
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 */
	private final class ImagePreferencesChangeListener implements OnPreferenceChangeListener  {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			SharedPreferences.Editor E = ApplicationPreferences.edit();
			
			if ( preference.getKey().equals("LstImageSize") ) {			
				E.putInt( "MaxImageDimensionIndex", ImageSize.findIndexOfValue( (String)newValue ) );
				ImageSize.setSummary( (String)newValue );
			}
			
			if ( preference.getKey().equals("JpegQuality") ) {			
				ImageQuality.setSummary( ((Integer)newValue).toString() );
			}
			
			E.commit();
			
			return true;
		}
		
	}
	
	
	/**
	 * This private class manages the interaction with the "Clean Avatars Cache"
	 * widget, that works as a button.
	 * This class deletes the full content of the Avatars cache.
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 * 
	 * @see MeemiEngine#CleanAvatarsCaches()
	 */
	private final class CleanAvatarsCacheClick implements OnPreferenceClickListener {
		@Override
		public boolean onPreferenceClick(Preference arg0) {
			MeemiDroidApplication.Engine.clearAvatarCache(true, PreferencesScreen.this);
			
			return true;
		}
	}
	
	
	private Preference CredentialBtn = null;
	
	private CheckBoxPreference EnableImage = null;
	private ListPreference ImageSize = null;
	private SeekBarAndroidPreference ImageQuality = null;
	
	private CheckBoxPreference EnableLocation = null;
	private ListPreference LocationAccurancy = null;
	private ListPreference LocationSync = null;
	
	private CheckBoxPreference EnableNotification = null;

	private Preference CleanAvatarsCacheBtn = null;	 
	
	private static final int SETTING_CREDENTIAL_DIALOG = 0;
	
	
	private SharedPreferences ApplicationPreferences = null;
}
