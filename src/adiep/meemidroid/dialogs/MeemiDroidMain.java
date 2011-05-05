package adiep.meemidroid.dialogs;

import java.util.Map;

import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import adiep.meemidroid.Utility;
import adiep.meemidroid.dialogs.settings.CredentialsSettingDialog;
import adiep.meemidroid.engine.MeemiEngine;
import adiep.meemidroid.engine.MeemiEngine.Callbackable;
import adiep.meemidroid.engine.MeemiEngine.MeemiEngineResult;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Main activity of this application: an Android client for
 * Meemi (meemi.com), an Italian social network.
 * This activity represents the main menu: the user can choose to read
 * meemis (i.e., Meemi messages), to access her profile, and so on.
 * 
 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
 * @version 0.5
 */
public class MeemiDroidMain extends Activity implements MeemiEngine.Callbackable {
	/**
	 * This method represents the callback point for every request made to
	 * the {@link MeemiEngine} instance.
	 * In this {@link Activity}, it manages the response to the following
	 * requests:
	 * - {@link MeemiEngine#getUserProfile(String, android.content.Context, Callbackable)}
	 * 
	 * @param Result	the API call result
	 * 
	 * @see MeemiEngine#getUserProfile(String, android.content.Context, Callbackable)
	 */
	@Override
	public void onEngineExecuteResult(MeemiEngineResult Result) {
		LogedUserInfo = MeemiEngine.parseUserProfileResult(Result);
		
		if ( null == LogedUserInfo || 0 == LogedUserInfo.size() ) {
			activateUI(false);
			
			if (IsFirstTimeLogin) {
				Utility.ShowToast(this, R.string.AllertWrogCredentials);
				showDialog(SETTING_CREDENTIAL_DIALOG);
			}
		} else {
			activateUI( true && Utility.isInternetConnected(this) );
			
		}
	};
	
	
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
    	Log.i("MeemiDroidMain", "onCreate()");
    	
        super.onCreate(savedInstanceState);

        setupLayout(); 
        
        // prepare splash screen
        Intent Splash = new Intent(this, SplashScreen.class);
		//IsAlreadyExitFromSplashScreen = true;
        startActivityForResult(Splash, ACTIVITY_SPLASH);
       
        LogedUserID = MeemiDroidApplication.Engine.getCredentials().getUsername();
    }
    
    /**
     * This method is called when the activity is moved into foreground.
     */
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	if ( LastTimeDashboardWasFlat != MeemiDroidApplication.Prefs.isFlatDashboardEnabled() ) {
    		setupLayout();
    	}
    	
    	activateUI( Utility.isInternetConnected(this) );
    	
		//IsAlreadyExitFromSplashScreen = false;
    }
    
    /**
    * This method inflates the activity main menu (from XML resource) and uses it
    * as activity options menu.
    * 
    * @see android.app.Activity#onCreateOptionsMenu(Menu)
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        
        return true;
    }
    
	/** 
	 * This method defines options menu action.
	 * 
	 * @param item	the menu item that was selected
	 * 
	 * @return	false to allow normal menu processing to proceed, true to
	 * 			consume it here.
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(MenuItem)
	 */
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {  
    	boolean isConsumed = false;
    	
	    switch (item.getItemId()) {
	    	case R.id.itemAbout:
	    		Intent About = new Intent(this, AboutScreen.class);
	        	
	        	startActivityForResult(About, ACTIVITY_ABOUT);
	        	isConsumed = true;
	        	
	        	break;
	        case R.id.itemExit:
	        	confirmExitDialog();
	        	isConsumed = true;
	        	
	        	break;
	        case R.id.itemSettings:
	        	Intent Settings = new Intent(MeemiDroidMain.this, PreferencesScreen.class);
				startActivityForResult(Settings, ACTIVITY_PREFERENCES);
	        	
	        	break;
		  default:
			// nothing to do	  
	    }  
	    
	    return isConsumed;  
	}
    
    /**
	 * This method is called when the activity change change orientation or the
	 * soft-keyboard appears/despairs. In order to use this method the activity
	 * has to be correctly configured in the AndroidManifest.xml:
	 * attribute android:configChanges in activity tag.
	 * 
	 * @param newConfig 	the new device configuration
	 *   
	 * @see android.app.Activity#onConfigurationChanged(Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.i("MeemiDroidMain", "onConfigurationChanged()");
		
		super.onConfigurationChanged(newConfig);
		
		setupLayout();
		
		activateUI( Utility.isInternetConnected(this) );
	}
	
	/**
	 * This method override the Activity onKeyUp to capture the Back Button:
	 * in this way we can have a consistent behavior with the Exit menu item.
	 * 
	 * @param keyCode	the value in event.getKeyCode()
	 * @param event		description of the key event
	 * 
	 * @return	true to prevent this event from being propagated further, or
	 * 			false to indicate that you have not handled this event and 
	 *			it should continue to be propagated
	 * 
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// we manage the back button here
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			confirmExitDialog();
			
	        return true;
	    }

		return super.onKeyUp(keyCode, event);
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
			case CONFIRM_EXIT_APPLICATION:
				AlertDialog.Builder B = new AlertDialog.Builder(this);
				
				B.setMessage(R.string.AllertExit)
				 .setCancelable(false)
				 .setPositiveButton(R.string.TextYes, new DialogInterface.OnClickListener() {
					 public void onClick(DialogInterface dialog, int id) {
						 MeemiDroidMain.this.finish();
					 }
				 })
				 .setNegativeButton(R.string.TextNo, new DialogInterface.OnClickListener() {
					 public void onClick(DialogInterface dialog, int id) {
						 dialog.cancel();
					 }
				 });
				
				D = B.create();
			default:
				// nothing to do
		}
		
		return D;
	}
	
    /**
     * This method is a callback called by an activity after his life cycle ends.
     * It's used to retrieve returned values or some after-process operations as in
     * this case.
     * 
     * @param requestCode 	the integer request code originally supplied to
     * 						{@link #startActivityForResult(Intent, int)}, allowing
     * 						you to identify who this result came from
	 * @param resultCode	the integer result code returned by the child activity
	 * 						through its {@link #setResult(int, Intent)}
	 * @param data			an Intent, which can return result data to the caller
	 * 						(various data can be attached to Intent "extras").
	 * 
	 * @see android.app.Activity#startActivityForResult(Intent, int) 
     */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ACTIVITY_SPLASH:				
		        if ( !MeemiDroidApplication.Engine.getCredentials().isMemorized() ) {
		        	Utility.ShowToast(this, R.string.AllertNoCredentials);
		        	
		        	showDialog(SETTING_CREDENTIAL_DIALOG);
		        }
		        
		        MeemiDroidApplication.Engine.getUserProfile(LogedUserID, this, this);
		        
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}
		
	/**
	 * This method handles the management of exit from the application
	 * confirm dialog.
	 */
	private void confirmExitDialog() {
		showDialog(CONFIRM_EXIT_APPLICATION);
	}
	
	/**
	 * This method defines the layout of the activity and links all the listeners
	 * used to interact with the user. 
	 */
	private void setupLayout() {
		Log.i("MeemiDroidMain", "setupLayout()");
		
		int CurrentLayout = R.layout.main;
		LastTimeDashboardWasFlat = MeemiDroidApplication.Prefs.isFlatDashboardEnabled();
		if (LastTimeDashboardWasFlat) {
			CurrentLayout = R.layout.main_flat;
		}
		setContentView(CurrentLayout);
        
		((Button)findViewById(R.id.ButtonReadMeemi)).setOnClickListener( new MeemisReadListener() );
		((Button)findViewById(R.id.ButtonSendMeemi)).setOnClickListener( new MeemiSendListener() );
        ((Button)findViewById(R.id.ButtonUserInfo)).setOnClickListener( new UserInfoListener() );
        ((Button)findViewById(R.id.ButtonFollowingUsers)).setOnClickListener( new UserFollowings() );
        ((Button)findViewById(R.id.ButtonFollowerUsers)).setOnClickListener( new UserFollowers() );
	}
	
	private void activateUI(final boolean Active) {
		((Button)findViewById(R.id.ButtonReadMeemi)).setEnabled(Active);
		((Button)findViewById(R.id.ButtonSendMeemi)).setEnabled(Active);
        ((Button)findViewById(R.id.ButtonUserInfo)).setEnabled(Active);
        ((Button)findViewById(R.id.ButtonFollowingUsers)).setEnabled(Active);
        ((Button)findViewById(R.id.ButtonFollowerUsers)).setEnabled(Active);
        
        if ( MeemiDroidApplication.Prefs.isFlatDashboardEnabled() ) {
        	Resources Res = getResources();
	        if (Active)  {
	        	((Button)findViewById(R.id.ButtonReadMeemi)).setCompoundDrawablesWithIntrinsicBounds(null, Res.getDrawable(R.drawable.main_ui_meemis_read), null, null);
	    		((Button)findViewById(R.id.ButtonSendMeemi)).setCompoundDrawablesWithIntrinsicBounds(null, Res.getDrawable(R.drawable.main_ui_meemis_write), null, null);
	            ((Button)findViewById(R.id.ButtonUserInfo)).setCompoundDrawablesWithIntrinsicBounds(null, Res.getDrawable(R.drawable.main_ui_user), null, null);
	            ((Button)findViewById(R.id.ButtonFollowingUsers)).setCompoundDrawablesWithIntrinsicBounds(null, Res.getDrawable(R.drawable.main_ui_following_2), null, null);
	            ((Button)findViewById(R.id.ButtonFollowerUsers)).setCompoundDrawablesWithIntrinsicBounds(null, Res.getDrawable(R.drawable.main_ui_followers_2), null, null);
	        } else {
	        	((Button)findViewById(R.id.ButtonReadMeemi)).setCompoundDrawablesWithIntrinsicBounds(null, Res.getDrawable(R.drawable.main_ui_meemis_read_gray), null, null);
	    		((Button)findViewById(R.id.ButtonSendMeemi)).setCompoundDrawablesWithIntrinsicBounds(null, Res.getDrawable(R.drawable.main_ui_meemis_write_gray), null, null);
	            ((Button)findViewById(R.id.ButtonUserInfo)).setCompoundDrawablesWithIntrinsicBounds(null, Res.getDrawable(R.drawable.main_ui_user_gray), null, null);
	            ((Button)findViewById(R.id.ButtonFollowingUsers)).setCompoundDrawablesWithIntrinsicBounds(null, Res.getDrawable(R.drawable.main_ui_following_2_gray), null, null);
	            ((Button)findViewById(R.id.ButtonFollowerUsers)).setCompoundDrawablesWithIntrinsicBounds(null, Res.getDrawable(R.drawable.main_ui_followers_2_gray), null, null);
	        }
        }
	}
	
	/**
	 * This private class manages the interaction with the "Read Meemi" button on the
	 * activity: it's show the Meemi screen to read messages stream.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private final class MeemisReadListener implements OnClickListener {
		public void onClick(View arg0) {
			//Intent Meemis = new Intent(MeemiDroidMain.this, MeemiLifestream.class);
			Intent Meemis = new Intent(MeemiDroidMain.this, MeemiList.class);
			
			Meemis.putExtra(MeemiLifestream.USER, LogedUserID);
	        
	        startActivityForResult(Meemis, ACTIVITY_MEEMISSLIST);
		}
	}
	
	/**
	 * This private class manages the interaction with the "Send Meemi" button on the
	 * activity: it's show the Meemi screen to post a new message.
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 */
	private final class MeemiSendListener implements OnClickListener {
		public void onClick(View arg0) {
			Intent Meemi = new Intent(MeemiDroidMain.this, MeemiSendScreen.class);
        	
        	startActivityForResult(Meemi, ACTIVITY_MEEMI);
		}
	}

	
	/**
	 * This private class manages the interaction with the "My Profile" button on the
	 * activity: it's show the activity responsible for show User information.
	 * 
	 * @see UserScreen
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 */
	private final class UserInfoListener implements OnClickListener {
		public void onClick(View arg0) {
			Intent UserInfo = new Intent(MeemiDroidMain.this, UserScreen.class);
			UserInfo.putExtra( UserScreen.USER, MeemiDroidApplication.Engine.getCredentials().getUsername() );
        	
			startActivityForResult(UserInfo, ACTIVITY_USER);
		}
	}
	
	/**
	 * This private class manages the interaction with followings retrieve.
	 * 
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 */
	private final class UserFollowings implements OnClickListener {
		public void onClick(View arg0) {			
			Intent UserInfo = new Intent(MeemiDroidMain.this, MeemiUsersList.class);
			
			UserInfo.putExtra( MeemiUsersList.USER, LogedUserID );
			UserInfo.putExtra( MeemiUsersList.NUM_OF_USERS, Integer.parseInt( LogedUserInfo.get("followings") ) );
			UserInfo.putExtra( MeemiUsersList.TYPE, MeemiUsersList.FOLLOWING);
        	
        	startActivityForResult(UserInfo, ACTIVITY_USERSLIST);
		}
	}
	
	/**
	 * This private class manages the interaction with followings retrieve.
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 */
	private final class UserFollowers implements OnClickListener {
		public void onClick(View arg0) {
			Intent UserInfo = new Intent(MeemiDroidMain.this, MeemiUsersList.class);
			
			UserInfo.putExtra( MeemiUsersList.USER, LogedUserID );
			UserInfo.putExtra( MeemiUsersList.NUM_OF_USERS, Integer.parseInt( LogedUserInfo.get("followers") ) );			
			UserInfo.putExtra( MeemiUsersList.TYPE, MeemiUsersList.FOLLOWERS);
        	
        	startActivityForResult(UserInfo, ACTIVITY_USERSLIST);
		}
	}
	
	/**
	 * This private class manages the dismiss of the Credential Setting Dialog Box;
	 * in order to check if the current user is logged or not.
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 */
	private final class CredentialSettingDialogDismissing implements DialogInterface.OnDismissListener {
		@Override
		public void onDismiss(DialogInterface arg0) {
			IsFirstTimeLogin = false;
			
			LogedUserID = MeemiDroidApplication.Engine.getCredentials().getUsername();
			
			MeemiDroidApplication.Engine.getUserProfile(LogedUserID, MeemiDroidMain.this, MeemiDroidMain.this);
		}
	}
	
	
	private String LogedUserID = null;
	private Map<String, String> LogedUserInfo = null;
	
	private boolean IsFirstTimeLogin = true; 
	//private boolean IsAlreadyExitFromSplashScreen = false;
	
	private boolean LastTimeDashboardWasFlat = true;
	
	private static final int SETTING_CREDENTIAL_DIALOG = 0;
	
	private static final int CONFIRM_EXIT_APPLICATION = 10;
	
	private static final int ACTIVITY_SPLASH = 0;
	private static final int ACTIVITY_ABOUT = 1;
	private static final int ACTIVITY_USER = 2;
	private static final int ACTIVITY_MEEMI = 3;
	private static final int ACTIVITY_USERSLIST = 4;
	private static final int ACTIVITY_MEEMISSLIST = 5;
	private static final int ACTIVITY_PREFERENCES = 6;
}