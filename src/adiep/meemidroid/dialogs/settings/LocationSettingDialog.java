package adiep.meemidroid.dialogs.settings;

import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import adiep.meemidroid.Utility;
import adiep.meemidroid.engine.LocationEngine;
import adiep.meemidroid.engine.MeemiPreferences;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;

/**
 * This dialog permits to the user to change her preference
 * about location services in MeemiDroid client. 
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.2
 */
public class LocationSettingDialog extends Dialog {

	/**
	 * This is the constructor of the dialog.
	 * 
	 * @param C		the context in which the dialog runs
	 * @param P		the current user preferences
	 */
	public LocationSettingDialog(Context C, MeemiPreferences P) {
		super(C);
		
		Prefs = P;
	}

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
		
		setContentView(R.layout.setting_location);
		setTitle(R.string.TitleSettingLocation);	
		
		ImageButton BtnSave = (ImageButton)findViewById(R.id.ImageButtonSettingCredentialsSave);
		BtnSave.setOnClickListener( new SaveListener() );
		
		ImageButton BtnCancel = (ImageButton)findViewById(R.id.ImageButtonSettingCredentialsCancel);
		BtnCancel.setOnClickListener( new CancelListener() );
		
		UseLocation = (CheckBox)findViewById(R.id.CheckBoxSettingLocationActivation);
		UseLocation.setOnCheckedChangeListener( new UseLocationListener() );
		
		LocationAccuracy = (Spinner)findViewById(R.id.SpinnerLocationAccuracy);
		// loading items
		ArrayAdapter<CharSequence> LocationAccuracyAdapter = ArrayAdapter.createFromResource(this.getContext(), R.array.LocationTypeItems, android.R.layout.simple_spinner_item);
		LocationAccuracyAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		LocationAccuracy.setAdapter(LocationAccuracyAdapter);
		
		// set listener for accuracy selection
		LocationAccuracy.setOnItemSelectedListener( new LocationAccuracySelectListener() );		
				
		SyncTime = (Spinner)findViewById(R.id.SpinnerLocationTimeSync);
		// loading items
		ArrayAdapter<CharSequence> SyncTimeAdapter = ArrayAdapter.createFromResource(this.getContext(), R.array.LocationSyncItems, android.R.layout.simple_spinner_item);
		SyncTimeAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		SyncTime.setAdapter(SyncTimeAdapter);

		// set listener for time range selection
		SyncTime.setOnItemSelectedListener( new SyncSelectListener() );
		
		setupDialogValues();
		
		// this is a workaround for the fact that setOnShowListener has been introduced too late (API level 8)
		setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface arg0) {
				setupDialogValues();
			}
		});
	}
	
	/**
	 * This method sets the dialog field with the values stored in the credentials
	 */
	private void setupDialogValues() {
		UseLocation.setChecked( Prefs.isLocationEnabled() );
		
		LocationAccuracy.setSelection( Prefs.getLocationAccuracy() );
		
		SyncTime.setSelection( Prefs.getLocationSyncIndex() );
		
		if (!UseLocation.isChecked() ) {
			LocationAccuracy.setEnabled(false);
			SyncTime.setEnabled(false);			
		}
	}

	/**
	 * This private class manages the sync time rage spinner selection.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class SyncSelectListener implements OnItemSelectedListener {
		
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
			try {
				LocationSyncMin = Integer.parseInt( ((String)SyncTime.getItemAtPosition(position)) );
			} catch (Exception ex) {
				LocationSyncMin = LocationEngine.ONLYDURINGMESSAGESENDING;
			}
			LocationSyncIndex = position;
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// default values
			LocationSyncMin = 5;
			LocationSyncIndex = 1;
		}
	}
	
	/**
	 * This private class manages the location accuracy selection.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class LocationAccuracySelectListener implements OnItemSelectedListener {
		
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
			LocationAccuracyIndex = position;
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// default values
			LocationAccuracyIndex = 0;
		}
	}
	
	/**
	 * This private class manages the interaction with the Save button of the
	 * dialog. It performs the credentials check, also.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class SaveListener implements android.view.View.OnClickListener {

		public void onClick(View arg0) {
			
			Prefs.setLocation( UseLocation.isChecked() );
			Prefs.setLocationAccuracy( LocationAccuracyIndex );
			Prefs.setLocationSync(LocationSyncMin, LocationSyncIndex);
						
			Prefs.save();
			
			// location sync
			if ( UseLocation.isChecked() ) {
				MeemiDroidApplication.Engine.startLocationSync(LocationSyncMin);
			} else {
				MeemiDroidApplication.Engine.stopLocationSync();
			}
			
			Utility.ShowToast(LocationSettingDialog.this.getContext(), R.string.AllertLocationOk);
				
			LocationSettingDialog.this.dismiss();
		}
	}
	
	/**
	 * This private class manages the interaction with the Cancel button of the
	 * dialog.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class CancelListener implements android.view.View.OnClickListener {

		public void onClick(View arg0) {
			LocationSettingDialog.this.dismiss();
		}
	}
	
	/**
	 * This private class manages the interaction with the checkbox for
	 * enable/disable location retrieving.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class UseLocationListener implements android.widget.CompoundButton.OnCheckedChangeListener {

		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			if ( UseLocation.isChecked() ) {
				LocationAccuracy.setEnabled(true);
				SyncTime.setEnabled(true);
			} else {
				LocationAccuracy.setEnabled(false);
				SyncTime.setEnabled(false);
			}
		}
	}
	
	private CheckBox UseLocation;
	
	private Spinner LocationAccuracy;
	private int LocationAccuracyIndex;
	
	private Spinner SyncTime;
	private int LocationSyncMin;
	private int LocationSyncIndex;
	
	private MeemiPreferences Prefs;
}
