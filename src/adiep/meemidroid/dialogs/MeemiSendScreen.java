package adiep.meemidroid.dialogs;

import java.io.File;

import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import adiep.meemidroid.Utility;
import adiep.meemidroid.dialogs.settings.CredentialsSettingDialog;
import adiep.meemidroid.engine.MeemiEngine;
import adiep.meemidroid.engine.MeemiEngine.Callbackable;
import adiep.meemidroid.engine.MeemiEngine.MeemiEngineResult;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This class represents the Activity to send a Meemi over the Internet.
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.5
 */
public class MeemiSendScreen extends Activity implements MeemiEngine.Callbackable {
	/**
	 * This method represents the callback point for every request made to
	 * the {@link MeemiEngine} instance.
	 * In this {@link Activity}, it manages the response to the following
	 * requests:
	 * - {@link MeemiEngine#isCredentialValid(android.content.Context, Callbackable)}
	 * - {@link MeemiEngine#postMessage(String, String, android.content.Context, Callbackable)}
	 * - {@link MeemiEngine#postImage(String, Uri, String, android.content.Context, Callbackable)}
	 * - {@link MeemiEngine#replyToMessage(String, String, String, booleanandroid.content.Context, Callbackable)}
	 * 
	 * @param Result	the API call result
	 * 
	 * @see MeemiEngine#isCredentialValid(android.content.Context, Callbackable)
	 * @see MeemiEngine#postMessage(String, String, android.content.Context, Callbackable)
	 * @see MeemiEngine#postImage(String, Uri, String, android.content.Context, Callbackable)
	 * @see MeemiEngine#replyToMessage(String, String, String, booleanandroid.content.Context, Callbackable)
	 */
	@Override
	public void onEngineExecuteResult(MeemiEngineResult Result) {
		if (MeemiEngine.CB_CREDENTIAL_CHECK == Result.CallbackMethod) {
			boolean isCredentialsValid = MeemiEngine.parseResultStatus(Result);
			
			if (!isCredentialsValid) {
				if (IsFirstTimeLogin) {
					Utility.ShowToast(this, R.string.AllertNoCredentials);
        	
					showDialog(SETTING_CREDENTIAL_DIALOG);
				} else {
					this.finish();
				}
			}
		} else {
			TextBox.setText("");
			MeemiType.setText("Type: Text");
		
			if (IsAReply) {
				// If we are sending a reply, after the transmission we have to close this activity ;) 
				MeemiSendScreen.this.finish();
			}
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
		super.onCreate(savedInstanceState);
		
		MyIntent = this.getIntent();
        IntentType = MyIntent.getType();
        
        MeemiReplyId = MyIntent.getStringExtra(REPLYMEEMIID);
        MeemerReplyNick = MyIntent.getStringExtra(REPLYMEEMER);
        
        IsAReply =  (null != MeemiReplyId && null != MeemerReplyNick);
        
        setupLayout();
        
        MeemiDroidApplication.Engine.isCredentialValid(this, this);        	
	}
	
	/**
	 * This method is a callback for creating dialogs that are managed (saved
	 * and restored) for you by the activity.
	 * 
	 * @param id	the id of the dialog
	 * @param args 	the dialog arguments provided to {@link #showDialog(int, Bundle)}
	 * 
	 * @return	the dialog. If you return null, the dialog will not be created.
	 */
    @Override
	protected Dialog onCreateDialog(int id) {
		Dialog D = null;
		
		switch (id) {
			case SETTING_CREDENTIAL_DIALOG:
				D = new CredentialsSettingDialog( this, MeemiDroidApplication.Engine.getCredentials(), new CredentialSettingDialogDismissing() );
				break;
			default:
				// nothing to do
		}
		
		return D;
	}
	
	/**
	 * This method defines the layout of the activity and links all the listeners
	 * used to interact with the user. 
	 */
	private void setupLayout() {		
		setContentView(R.layout.post_meemi);
		
		((Button)findViewById(R.id.ButtonPostMeemi)).setOnClickListener( new PostMessage() );
		
		MeemiType = (TextView)findViewById(R.id.TextMeemiType);
		TextBox = (EditText)findViewById(R.id.EditTextMeemi);
		UsersBox = (EditText)findViewById(R.id.EditTextPrivateUsers);	
		
		String CurrentUser = MyIntent.getStringExtra(PRIVATEUSER);
		if (null != CurrentUser) {
			UsersBox.setText(CurrentUser);
		}
		
		TextBox.requestFocus();
		
        if (null != IntentType) {
        	if ( IntentType.contains("text") ) {
        		MeemiType.setText("Type: Text");
        		TextBox.setText( MyIntent.getExtras().getString(Intent.EXTRA_TEXT) );
        		
        		Type = MessageType.MT_TEXT;
        	}
        	
        	if ( IntentType.contains("image") ) {        		
        		Uri ImageUri = (Uri) MyIntent.getExtras().getParcelable(Intent.EXTRA_STREAM);
        		File ImageFile = new File( ImageUri.getPath() );

        		MeemiType.setText("Type: Image [" + ImageFile.getName() + "]" );
        		
        		Type = MessageType.MT_IMAGE;
        	}
        }
        
        if (IsAReply) {
        	MeemiType.setText("Reply to " + MeemerReplyNick);
        	
        	Type = MessageType.MT_TEXT;
        }
	}
	
	
	/**
	 * This private class is used to manage the the send message button
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private final class PostMessage implements OnClickListener {
		public void onClick(View arg0) {
			String Message = TextBox.getText().toString();
			
			String UsersList = UsersBox.getText().toString();
			if ( 0 == UsersList.trim().length() ) {
				UsersList = null;
			}
			
			if (MessageType.MT_TEXT == Type) {
				if (!IsAReply) {
					MeemiDroidApplication.Engine.postMessage(Message , UsersList, MeemiSendScreen.this, MeemiSendScreen.this);
				} else {
					MeemiDroidApplication.Engine.replyToMessage(Message, MeemiReplyId, MeemerReplyNick, MeemiSendScreen.this, MeemiSendScreen.this);
				}
				
			} else if (MessageType.MT_IMAGE == Type) {
				Uri ImageUri = (Uri) MyIntent.getExtras().getParcelable(Intent.EXTRA_STREAM);
				
				MeemiDroidApplication.Engine.postImage(Message, ImageUri, UsersList, MeemiSendScreen.this, MeemiSendScreen.this);
			}
		}
	}
	
	/**
	 * This private class manages the dismiss of the Credential Setting Dialog Box;
	 * in order to check if the current user is logged or not.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private final class CredentialSettingDialogDismissing implements DialogInterface.OnDismissListener {
		@Override
		public void onDismiss(DialogInterface arg0) {
			IsFirstTimeLogin = false;
			
			MeemiDroidApplication.Engine.isCredentialValid(MeemiSendScreen.this, MeemiSendScreen.this);  
		}
		
	}
	
	private boolean IsFirstTimeLogin = true; 
	
	public static final String PRIVATEUSER = "PrivateUserNick";
	
	public static final String REPLYMEEMIID = "ReplyMeemiID";
	public static final String REPLYMEEMER = "ReplyMeemer";
	
	
	private static final int SETTING_CREDENTIAL_DIALOG = 0;
	
	private EditText TextBox = null;
	private EditText UsersBox = null;
	private TextView MeemiType = null;
	
	private Intent MyIntent = null;
	private String IntentType = null;
	private MessageType Type = MessageType.MT_TEXT;
	
	private boolean IsAReply = false;
	private String MeemiReplyId = null;
	private String MeemerReplyNick = null;
	
	private enum MessageType { MT_TEXT, MT_IMAGE }
}
