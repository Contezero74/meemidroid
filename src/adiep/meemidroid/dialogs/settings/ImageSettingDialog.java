package adiep.meemidroid.dialogs.settings;

import adiep.meemidroid.R;
import adiep.meemidroid.Utility;
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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * This dialog permits to the user to change her preference
 * about location services in MeemiDroid client. 
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.25
 */
public class ImageSettingDialog extends Dialog {

	/**
	 * This is the constructor of the dialog.
	 * 
	 * @param C		the context in which the dialog runs
	 * @param P		the current user preferences
	 */
	public ImageSettingDialog(Context C, MeemiPreferences P) {
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
		
		setContentView(R.layout.setting_imageshare);
		setTitle(R.string.TitleSettingImage);	
		
		ImageButton BtnSave = (ImageButton)findViewById(R.id.ImageButtonSettingImageResizeSave);
		BtnSave.setOnClickListener( new SaveListener() );
		
		ImageButton BtnCancel = (ImageButton)findViewById(R.id.ImageButtonSettingImageResizeCancel);
		BtnCancel.setOnClickListener( new CancelListener() );
		
		UseImageResize = (CheckBox)findViewById(R.id.CheckBoxSettingImageResize);
		UseImageResize.setOnCheckedChangeListener( new UseImageResizeListener() );
		
		ImageSize = (Spinner)findViewById(R.id.SpinnerImageSize);
		// loading items
		ArrayAdapter<CharSequence> ImageSizeAdapter = ArrayAdapter.createFromResource(this.getContext(), R.array.ImageSizetems, android.R.layout.simple_spinner_item);
		ImageSizeAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		ImageSize.setAdapter(ImageSizeAdapter);
		
		// set listener for image max size
		ImageSize.setOnItemSelectedListener( new ImageSizeSelectListener() );
		
		ImageQuality = (SeekBar)findViewById(R.id.SeekBarImageQuality);		
		ImageQuality.setOnSeekBarChangeListener( new ImageQualitySeekListener() );
		
		ImageQualityCheker = (TextView)findViewById(R.id.TextViewImageQualityChecker);
		
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
		UseImageResize.setChecked( Prefs.isImageResizeEnabled() );
		
		ImageSize.setSelection( Prefs.getImageResizeIndex() );
		
		Quality = Prefs.getImageQuality();
		ImageQuality.setProgress(Quality);
		ImageQualityCheker.setText( Integer.toString(Quality) ); 

		if ( !UseImageResize.isChecked() ) {
			ImageSize.setEnabled(false);		
			ImageQuality.setEnabled(false);
		} else {
			ImageSize.setEnabled(true);		
			ImageQuality.setEnabled(true);
		}
	}

	/**
	 * This private class manages the location accuracy selection.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class ImageSizeSelectListener implements OnItemSelectedListener {
		
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
			MaxImageSizeIndex = position;
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// default values
			MaxImageSizeIndex = 0;
		}
	}
	
	/**
	 * This private class manages seek bar releated to the JPEG image quality.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class ImageQualitySeekListener implements OnSeekBarChangeListener {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			progress = Math.round( (((float)progress)*100)/100 );

			Quality = progress;
			
			ImageQualityCheker.setText( Integer.toString(Quality) );
		}

		public void onStartTrackingTouch(SeekBar arg0) {
			// nothing to do
		}

		public void onStopTrackingTouch(SeekBar arg0) {
			// nothing to do			
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
			
			Prefs.setImageResize( UseImageResize.isChecked() );
			Prefs.setImageResizeIndex( MaxImageSizeIndex );
			Prefs.setImageQuality(Quality);
			
			Prefs.save();
			
			Utility.ShowToast(ImageSettingDialog.this.getContext(), R.string.AllertImageOk);
				
			ImageSettingDialog.this.dismiss();
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
			ImageSettingDialog.this.dismiss();
		}
	}
	
	/**
	 * This private class manages the interaction with the checkbox for
	 * enable/disable location retrieving.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class UseImageResizeListener implements android.widget.CompoundButton.OnCheckedChangeListener {

		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			if ( UseImageResize.isChecked() ) {
				ImageSize.setEnabled(true);
				ImageQuality.setEnabled(true);
			} else {
				ImageSize.setEnabled(false);
				ImageQuality.setEnabled(false);
			}
		}
	}
	
	private CheckBox UseImageResize = null;
	private Spinner ImageSize = null;
	private SeekBar ImageQuality = null;
	private TextView ImageQualityCheker = null;
	
	
	private int MaxImageSizeIndex;
	private int Quality;	
	
	private MeemiPreferences Prefs;
}
