package adiep.meemidroid.dialogs.settings;

import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import adiep.meemidroid.Utility;
import adiep.meemidroid.engine.MeemiCredentials;
import adiep.meemidroid.engine.MeemiEngine;
import adiep.meemidroid.engine.MeemiEngine.MeemiEngineResult;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 * This dialog permits to the user to change her access credentials
 * to Meemi (i.e., user name and password).
 * Note that in order to correctly set the new credentials the
 * Internet connection has to be available: in fact the system
 * performs a check of the user credentials. 
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.5
 */
public class CredentialsSettingDialog extends Dialog implements MeemiEngine.Callbackable {
	/**
	 * This method represents the callback point for every request made to
	 * the {@link MeemiEngine} instance.
	 * In this {@link Activity}, it manages the response to the following
	 * requests:
	 * - {@link MeemiEngine#(String, android.content.Context)}
	 * 
	 * @param Result	the API call result
	 * 
	 * @see MeemiEngine#getUserProfile(String, android.content.Context)
	 */
	@Override
	public void onEngineExecuteResult(MeemiEngineResult Result) {
		boolean isCredentialsValid = MeemiEngine.parseResultStatus(Result);
		if (isCredentialsValid) {			
			Utility.ShowToast(CredentialsSettingDialog.this.getContext(), R.string.AllertCredentialsOk);
			
			/* NOTE: this is a workaround to have the possibility to have
			 * some post-processing after this dialog has been close.
			 * The right way should be to use D.setOnDismissListener, but it
			 * seems not working.
			 */
			if (null != DismissCallback) {
				DismissCallback.onDismiss(this);
			}
			
			dismiss();
		} else {
			Utility.ShowToast(CredentialsSettingDialog.this.getContext(), R.string.AllertWrogCredentials);
		}
	};

	/**
	 * This is the constructor of the dialog.
	 * 
	 * @param context	the context in which the dialog runs
	 * @param MC		the current user credentials
	 * @param DL		the activity call back to make some post-processing work:
	 * 					this is a workaround: D.setOnDismissListener, but seems
	 * 					not working	
	 */
	public CredentialsSettingDialog(Context C, MeemiCredentials MC, DialogInterface.OnDismissListener DL) {
		super(C);
		
		this.MyCredentials = MC;
		this.MyCredentials.load();
		
		this.DismissCallback = DL;
		
		Username = MyCredentials.getUsername();
		Password = MyCredentials.getPassword();
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
		
		setContentView(R.layout.setting_credentials);
		setTitle(R.string.TitleSettingCredentials);	
		
		ImageButton BtnSave = (ImageButton)findViewById(R.id.ImageButtonSettingCredentialsSave);
		BtnSave.setOnClickListener( new SaveListener() );
		
		ImageButton BtnCancel = (ImageButton)findViewById(R.id.ImageButtonSettingCredentialsCancel);
		BtnCancel.setOnClickListener( new CancelListener() );
		
		UsernameText = (EditText)findViewById(R.id.EditTextUser);
		
		PasswordText = (EditText)findViewById(R.id.EditTextPassword);
		
		// link to meemi for signup
		((Button)findViewById(R.id.SignUpBtn)).setOnClickListener( new SignupListener() );		
		
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
		UsernameText.setText( MyCredentials.getUsername() );
		
		PasswordText.setText( MyCredentials.getPassword() );
	}
	
	/**
	 * This private class manages the interaction with the Save button of the
	 * dialog. It performs the credentials check, also.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class SaveListener implements android.view.View.OnClickListener {

		public void onClick(View arg0) {		
			MyCredentials.setUsername( UsernameText.getText().toString().trim() );
			MyCredentials.setPassword( PasswordText.getText().toString().trim() );
			MyCredentials.save();
			
			MeemiDroidApplication.Engine.isCredentialValid(MeemiDroidApplication.getContext(), CredentialsSettingDialog.this);
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
			if (null != Username && null != Password) {
				MyCredentials.setUsername(Username);
				MyCredentials.setPassword(Password);
				MyCredentials.save();
			}
			
			/* NOTE: this is a workaround to have the possibility to have
			 * some post-processing after this dialog has been close.
			 * The right way should be to use D.setOnDismissListener, but it
			 * seems not working.
			 */
			if (null != DismissCallback) {
				DismissCallback.onDismiss(CredentialsSettingDialog.this);
			}
			
			cancel();
		}
	}
	
	/**
	 * This private class manages the interaction with Signup button.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class SignupListener implements android.view.View.OnClickListener {

		public void onClick(View V) {		
			Uri SignupURI = Uri.parse( "http://meemi.com/lang/en/p/signup" );
			getContext().startActivity( new Intent( Intent.ACTION_VIEW, SignupURI ) );

		}
	}
	
	private MeemiCredentials MyCredentials;
	
	private EditText UsernameText;
	private EditText PasswordText;
	
	private DialogInterface.OnDismissListener DismissCallback = null;
	
	// user and password temp back storage
	private String Username = null;
	private String Password = null;
}
