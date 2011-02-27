
package adiep.meemidroid.support.compatibility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * This class is custom extension of a dialog preference widget implementing a
 * {@link SeekBar}. This class is based on the work of Matthew Wiggins and it is
 * adapted for MeemiDroid project.
 * 
 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
 * @version 1.1
 */
public class SeekBarAndroidPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	/**
	 * This is the constructor that initializes the seekbar.
	 * 
	 * @param C		the current Android {@link Context}
	 * @param As	the set of attributes used to initialize the seekbar
	 */
	public SeekBarAndroidPreference(Context C, AttributeSet As) { 
		super(C, As);

		myContext = C;
		
		DialogMessage	= As.getAttributeValue(androidns, "dialogMessage");
		Suffix			= As.getAttributeValue(androidns, "text");
		DefaultValue	= As.getAttributeIntValue(androidns, "defaultValue", 0);
		MaxValue		= As.getAttributeIntValue(androidns, "max", 100);
	}
	
	/**
	 * This method traces the progress level has changed.
	 * The FromUser parameter can be used to distinguish user-initiated changes
	 * from those that occurred programmatically.
	 * 
	 * @param S			the SeekBar whose progress has changed
	 * @param Progress	the current progress level. This will be in the range
	 * 					[0 .. max] where max was set by {@link #setMax(int)}
	 * 					(The default value for max is 100)
	 * @param FromUser	true if the progress change was initiated by the user
	 */
	public void onProgressChanged(SeekBar S, int Progress, boolean FromUser) {
		String V = String.valueOf(Progress);

		myValueText.setText( Suffix == null ? V : V.concat(Suffix) );
		
		if ( shouldPersist() ) {
			persistInt(Progress);
		}
		
		callChangeListener( new Integer(Progress) );
	}

	/**
	 * This method traces the start of a touch gesture, in order to disable
	 * advancing the seekbar (if needed).
	 * At the moment this method does nothing.
	 * 
	 * @param S	the SeekBar in which the touch gesture began
	 */
	public void onStartTrackingTouch(SeekBar S) {
		// nothing to do
	}

	/**
	 * This method traces the end of a touch gesture, in order to re-enable
	 * advancing the seekbar (if needed).
	 * 
	 * @param S	the SeekBar in which the touch gesture began 
	 */
	public void onStopTrackingTouch(SeekBar S) {
		// nothing to do
	}

	/**
	 * This method sets the range of the {@link SeekBar} bar to [0 .. Max].
	 * 
	 * @param Max	the upper range of this progress bar
	 */
	public synchronized void setMax(final int Max) {
		MaxValue = Max;
	}

	/**
	 * This method returns the upper limit of the {@link SeekBar}'s range.
	 * 
	 * @return a positive integer
	 */
	public synchronized int getMax() {
		return MaxValue;
	}

	/**
	 * This method sets the current progress to the specified value.
	 * It does not do anything if the {@link SeekBar} is in indeterminate mode.
	 *
	 * @param Progress	the new progress, between 0 and getMax()
	 */
	public synchronized void setProgress(final int Progress) {
		Value = Progress;
		
		if (mySeekBar != null) {
			mySeekBar.setProgress(Progress);
		}
	}

	/**
	 * This method returns the {@link SeekBar}'s current level of progress.
	 * It returns 0 when the progress bar is in indeterminate mode.
	 * 
	 * @return the current progress, between 0 and getMax()
	 */
	public synchronized int getProgress() {
		return Value;
	}
	  
	
	/**
	 * This method creates the content view for the dialog (if a custom
	 * content view is required).
	 * This method defines the layout for the seekbar to embed into a {@link PreferenceScreen}
	 * 
	 * @return the content View for the dialog.
	 */
	@Override
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams P;
		
		LinearLayout L = new LinearLayout(myContext);
		L.setOrientation(LinearLayout.VERTICAL);
		L.setPadding(6,6,6,6);
		
		mySplashText = new TextView(myContext);
		if (DialogMessage != null) {
			String Msg = "";
			if ( DialogMessage.startsWith("@") ) {
				int Id = myContext.getResources().getIdentifier(DialogMessage.substring(1), null, null);
				if (0 != Id) {
					Msg = myContext.getString(Id);
				}
			}
			mySplashText.setText(Msg);
		}
		L.addView(mySplashText);
		
		myValueText = new TextView(myContext);
		myValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		myValueText.setTextSize(32);
		P = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		L.addView(myValueText, P);
		
		mySeekBar = new SeekBar(myContext);
		mySeekBar.setOnSeekBarChangeListener(this);
		L.addView( mySeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT) );
		
		if (shouldPersist()) {
			Value = getPersistedInt(DefaultValue);
		}
		
		mySeekBar.setMax(MaxValue);
		mySeekBar.setProgress(Value);
		
		return L;
	}
	
	/**
	 * This method binds views in the content View of the dialog to data.
	 * Make sure to call through to the superclass implementation.
	 * 
	 * @param V		the content View of the dialog, if it is custom. 
	 */
	@Override 
	protected void onBindDialogView(View V) {
		super.onBindDialogView(V);

		mySeekBar.setMax(MaxValue);
		mySeekBar.setProgress(Value);
	}

	/**
	 * This method sets the initial value of the Preference.
	 * 
	 * If Restore is true, it restores the Preference value from the
	 * {@link SharedPreferences}, otherwise it sets the Preference value to
	 * DefaultVal that is given.
	 * 
	 * @param Restore		true to restore the persisted value; false to use
	 * 						the given defaultValue
	 * @param DefaultVal	the default value for this Preference. Only use
	 * 						this if restorePersistedValue is false
	 */
	@Override
	protected void onSetInitialValue(boolean Restore, Object DefaultVal) {
		super.onSetInitialValue(Restore, DefaultValue);

		if (null != DefaultVal) {
			Value = (Integer)DefaultVal;
		}
		if (Restore) { 
			Value = shouldPersist() ? getPersistedInt(DefaultValue) : 0;
		}
	}
  
  
  private static final String androidns = "http://schemas.android.com/apk/res/android";

  
  private SeekBar mySeekBar		= null;
  private TextView mySplashText	= null;
  private TextView myValueText	= null;
  
  private Context myContext		= null;

  private String DialogMessage	= null;
  private String Suffix			= null;
  private int DefaultValue		= 0;
  private int MaxValue			= 100;
  private int Value				= 0;
}


