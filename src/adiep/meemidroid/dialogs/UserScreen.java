package adiep.meemidroid.dialogs;

import java.util.Formatter;
import java.util.Map;

import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import adiep.meemidroid.Utility;
import adiep.meemidroid.engine.MeemiEngine;
import adiep.meemidroid.engine.MeemiEngine.MeemiEngineResult;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class represents the Activity to access the user information.
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.4
 */
public class UserScreen extends Activity implements MeemiEngine.Callbackable {
	/**
	 * This method represents the callback point for every request made to
	 * the {@link MeemiEngine} instance.
	 * In this {@link Activity}, it manages the response to the following
	 * requests:
	 * - {@link MeemiEngine#getUserProfile(String, android.content.Context, Callbackable)}
	 * - {@link MeemiEngine#setUserBlock(String, boolean, android.content.Context, Callbackable)}
	 * - {@link MeemiEngine#setUserFollow(String, boolean, android.content.Context, Callbackable)}
	 * 
	 * @param Result	the API call result
	 * 
	 * @see MeemiEngine#getUserProfile(String, android.content.Context, Callbackable)
	 * @see MeemiEngine#setUserBlock(String, boolean, android.content.Context, Callbackable)
	 * @see MeemiEngine#setUserFollow(String, boolean, android.content.Context, Callbackable)
	 */
	@Override
	public void onEngineExecuteResult(MeemiEngineResult Result) {
		switch (Result.CallbackMethod) {
		case MeemiEngine.CB_USER_BLOCK:
			boolean isBlockActionExecuted = MeemiEngine.parseResultStatus(Result);
			if (isBlockActionExecuted) {
				if (IsUserBlocked) {
					IsUserBlocked = false;
					BtnBlockUnblock.setText(R.string.BtnBlockUser);
				} else {
					IsUserBlocked = true;
					BtnBlockUnblock.setText(R.string.BtnUnblockUser);
					
					IsUserFollowed = false;
					BtnFollowUnFollow.setText(R.string.BtnAddUser);
					
					FakeUpdateFriendUI(ACTION_BLOCK_OR_UNFOLLOW);
				}
				
				/* FIXME: the following action should be done, but seem that the
				 * Users Profiles are not updated in real time; so can happens, for
				 * example, that you unblock an user, but retrieving user info, she
				 * appears still blocked.
				 */				
				//getUserInfo();
			}

			break;
			
		case MeemiEngine.CB_USER_FOLLOW:
			boolean isFollowActionExecuted = MeemiEngine.parseResultStatus(Result);
			if (isFollowActionExecuted) {
				if (IsUserFollowed) {
					IsUserFollowed = false;
					BtnFollowUnFollow.setText(R.string.BtnAddUser);
					
					FakeUpdateFriendUI(ACTION_BLOCK_OR_UNFOLLOW);
				} else {
					IsUserFollowed = true;
					BtnFollowUnFollow.setText(R.string.BtnRemoveUser);
					
					FakeUpdateFriendUI(ACTION_FOLLOW);
				}
				
				/* FIXME: the following action should be done, but seem that the
				 * Users Profiles are not updated in real time; so can happens, for
				 * example, that you unfollow an user, but retrieving user info, she
				 * appears still followed.
				 */				
				//getUserInfo();
			}
			
			break;
			
		case MeemiEngine.CB_USER_PROFILE:
			CurrentUserInfo = MeemiEngine.parseUserProfileResult(Result);
			updateUI();
			break;
			
		default:
			Log.w("UserScreen - onEngineExecuteResult", "This callback (" + Result.CallbackMethod + ") is not managed yet");
		}
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
		
		CurrentUser = this.getIntent().getStringExtra(USER);
		
		isUserOwner = MeemiDroidApplication.Engine.getCredentials().getUsername().equalsIgnoreCase(CurrentUser);
		
		setupLayout();
	}
	
	/**
	 * This method handles informs the user that an her contact is ready to be blocked.
	 */
	private void confirmBlockUserDialog() {
		showDialog(CONFIRM_BLOCK_USER);
	}
	
	/**
	 * This method handles informs the user that an her contact is ready to be blocked.
	 */
	private void confirmUnfollowUserDialog() {
		showDialog(CONFIRM_UNFOLLOW_USER);
	}
	
	/**
	 * This method defines the layout of the activity and links all the listeners
	 * used to interact with the user. 
	 */
	private void setupLayout() {
		getUserInfo();
	
		setContentView(R.layout.user2);
		
		if (isUserOwner) {
			((LinearLayout)findViewById(R.id.LinearLayoutBottomButtonBar)).setVisibility(View.GONE);
		} else {
			BtnPrivateMsgSent = (Button)findViewById(R.id.ButtonSendPrivateMsg);
			BtnPrivateMsgSent.setOnClickListener( new PrivateMessageSendClick() );
			
			BtnBlockUnblock = (Button)findViewById(R.id.ButtonBlockUnBlockUser);
			BtnBlockUnblock.setOnClickListener( new BlockUnblockUser() );
			
			BtnFollowUnFollow = (Button)findViewById(R.id.ButtonAddRemoveUser);
			BtnFollowUnFollow.setOnClickListener( new FollowUnFollowUser() );				
		}
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
			case CONFIRM_BLOCK_USER:
				AlertDialog.Builder DBU = new AlertDialog.Builder(this);
				
				DBU.setMessage(R.string.AllertBlockUser)
				 .setCancelable(false)
				 .setPositiveButton(R.string.TextYes, new DialogInterface.OnClickListener() {
					 public void onClick(DialogInterface dialog, int id) {
						 MeemiDroidApplication.Engine.setUserBlock(CurrentUser, true, UserScreen.this, UserScreen.this);
					 }
				 })
				 .setNegativeButton(R.string.TextNo, new DialogInterface.OnClickListener() {
					 public void onClick(DialogInterface dialog, int id) {
						 dialog.cancel();
					 }
				 });
				
				D = DBU.create();
				break;
				
			case CONFIRM_UNFOLLOW_USER:
				AlertDialog.Builder DUU = new AlertDialog.Builder(this);
				
				DUU.setMessage(R.string.AllertUnfollowUser)
				 .setCancelable(false)
				 .setPositiveButton(R.string.TextYes, new DialogInterface.OnClickListener() {
					 public void onClick(DialogInterface dialog, int id) {
						 MeemiDroidApplication.Engine.setUserFollow(CurrentUser, false, UserScreen.this, UserScreen.this);
					 }
				 })
				 .setNegativeButton(R.string.TextNo, new DialogInterface.OnClickListener() {
					 public void onClick(DialogInterface dialog, int id) {
						 dialog.cancel();
					 }
				 });
				
				D = DUU.create();
				
				break;
			default:
				// nothing to do
		}
		
		return D;
	}
    
    private void getUserInfo() {
    	MeemiDroidApplication.Engine.getUserProfile(CurrentUser, this, this);
    }
    
    private void updateUI() {
    	String TmpHTML = formatHTMLTemplate(new String[]{
    			CurrentUserInfo.get("avatar_url"),
    			CurrentUserInfo.get("screen_name"),
    			CurrentUserInfo.get("real_name"),
    			CurrentUserInfo.get("birth"),
    			CurrentUserInfo.get("current_location"),
    			Utility.fromMeemiToHTML( CurrentUserInfo.get("profile") ),
    			Utility.fromMeemiToHTML( CurrentUserInfo.get("description") )	 
    		});
    	
    	WebView ShortDescription = (WebView)findViewById(R.id.UserInfo);
		ShortDescription.loadData( Utility.wrapForHTML( TmpHTML, CSS ), "text/html", "utf-8");
		ShortDescription.addJavascriptInterface(new AccountClick(), "account");
		ShortDescription.getSettings().setJavaScriptEnabled(true);

		if (isUserOwner) {
			((TextView)findViewById(R.id.TextViewFollowing)).setText(R.string.UserListFollowing);
			((TextView)findViewById(R.id.TextViewFollowers)).setText(R.string.UsersListFollowers);
		} else {
			((TextView)findViewById(R.id.TextViewFollowing)).setText(R.string.OtherUserListFollowing);
			((TextView)findViewById(R.id.TextViewFollowers)).setText(R.string.OtherUserListFollowers);
		}
		
		if ( !isUserOwner ) {
			if ( CurrentUserInfo.get("is_followed").equals("0") ) {
				BtnFollowUnFollow.setText(R.string.BtnAddUser);
				IsUserFollowed = false;
			} else {
				BtnFollowUnFollow.setText(R.string.BtnRemoveUser);
				IsUserFollowed = true;
			}
			
			if ( CurrentUserInfo.get("is_blocked").equals("0") ) {
				BtnBlockUnblock.setText(R.string.BtnBlockUser);
				IsUserBlocked = false;
			} else {
				BtnBlockUnblock.setText(R.string.BtnUnblockUser);
				IsUserBlocked = true;
			}
		}
		
		((TextView)findViewById(R.id.TextViewFollowingNumber)).setText( CurrentUserInfo.get("followings") );
		((TextView)findViewById(R.id.TextViewFollowersNumber)).setText( CurrentUserInfo.get("followers") );
		
		((ImageView)findViewById(R.id.ImageViewFollowers)).setOnClickListener(new FollowerClick() );
		((ImageView)findViewById(R.id.ImageViewFollowing)).setOnClickListener(new FollowingClick() );
    	
    }
    
    private static final String formatHTMLTemplate(final String[] Elements) {
		return new Formatter().format(HTMLTemplate, (Object[])Elements).toString();
	}
    
	
	/**
	 * This private class is used to manage the block/unblock of the users.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	final class BlockUnblockUser implements OnClickListener {
		public void onClick(View arg0) {
			if (IsUserBlocked) {
				MeemiDroidApplication.Engine.setUserBlock(CurrentUser, false, UserScreen.this, UserScreen.this);
			} else {
				confirmBlockUserDialog();
			}
		}
	} 
	
	
	/**
	 * This private class is used to manage the follow/unfollow of the users.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	final class FollowUnFollowUser implements OnClickListener {
		public void onClick(View arg0) {
			if (!IsUserFollowed) {
				MeemiDroidApplication.Engine.setUserFollow(CurrentUser, true, UserScreen.this, UserScreen.this);
			} else {
				confirmUnfollowUserDialog();
			}
		}
	} 
	
	
	/**
	 * This private class is used to send a private message to the user.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	final class PrivateMessageSendClick implements OnClickListener {
        public void onClick(View arg0) {       	
        	Intent PrivateMessageIntent = new Intent(UserScreen.this, MeemiSendScreen.class);
        	
        	PrivateMessageIntent.putExtra( MeemiSendScreen.PRIVATEUSER, CurrentUser );
        	
        	startActivityForResult(PrivateMessageIntent, ACTIVITY_MEEMI);
        }
    }
		
	
	/**
	 * This private class is used to manage the interaction with the {@link WebView} when
	 * the user select a ScreenName (e.g., @capobecchino)
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	final class AccountClick {
		public void clickOnAccount(String Name) {       	
        	Intent UserInfo = new Intent(UserScreen.this, UserScreen.class);
			UserInfo.putExtra( UserScreen.USER, Name );
        	
        	startActivityForResult(UserInfo, ACTIVITY_USER);

        }
    }
	
	
	/**
	 * This private class is used to manage the click on the follower image.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	final class FollowerClick implements OnClickListener {
        public void onClick(View arg0) {       	
        	Intent UserInfo = new Intent(UserScreen.this, MeemiUsersList.class);
			UserInfo.putExtra( MeemiUsersList.USER, CurrentUser );
			UserInfo.putExtra( MeemiUsersList.NUM_OF_USERS, Integer.parseInt( CurrentUserInfo.get("followers") ) );
			UserInfo.putExtra( MeemiUsersList.TYPE, MeemiUsersList.FOLLOWERS);
        	
        	startActivityForResult(UserInfo, ACTIVITY_USERSLIST);
        }
    }
	
	/**
	 * This private class is used to manage the click on the following image.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	final class FollowingClick implements OnClickListener {
        public void onClick(View arg0) {       	
        	Intent UserInfo = new Intent(UserScreen.this, MeemiUsersList.class);
			UserInfo.putExtra( MeemiUsersList.USER, CurrentUser );
			UserInfo.putExtra( MeemiUsersList.NUM_OF_USERS, Integer.parseInt( CurrentUserInfo.get("followings") ) );
			UserInfo.putExtra( MeemiUsersList.TYPE, MeemiUsersList.FOLLOWING);
        	
        	startActivityForResult(UserInfo, ACTIVITY_USERSLIST);
        }
    }

	
	private String CurrentUser = null;
	private boolean isUserOwner = false;
	private Map<String, String> CurrentUserInfo = null;
	
	private Button BtnPrivateMsgSent = null;
	
	private Button BtnBlockUnblock = null;
	private boolean IsUserBlocked = false;
	
	private Button BtnFollowUnFollow = null;
	private boolean IsUserFollowed = false;
	
	/* FIXME: the following action should be done, but seem that the
	 * Users Profiles are not updated in real time; so can happens, for
	 * example, that you unblock an user, but retrieving user info, she
	 * appears still blocked.
	 * 
	 * This property can have 3 values (-1, 0, +1) and its status
	 * can change according to the following actions:
	 * just blocked or unfollowed: -1
	 * just followed: +1
	 * 
	 * Note that a blocking action implies a unfollow action.
	 */
	private int IsJustChangeBlockFollowStatus = 0;
	
	private static final int ACTION_BLOCK_OR_UNFOLLOW = -1;
	private static final int ACTION_FOLLOW = +1;
	
	/**
	 * This method is used to have a fast UI update when a follow/unfollow
	 * or block/unblock action has been executed. This is useful because
	 * seem that the  Users Profiles are not updated in real time.
	 * 
	 * @param Action	 the kind of action executed
	 * 
	 * @see UserScreen#ACTION_BLOCK_OR_UNFOLLOW
	 * @see UserScreen#ACTION_FOLLOW
	 */
	private void FakeUpdateFriendUI(final int Action) {
		IsJustChangeBlockFollowStatus += Action;
		
		try {
			int NewFollowersQnt = Integer.parseInt( CurrentUserInfo.get("followers") ) + IsJustChangeBlockFollowStatus;
			((TextView)findViewById(R.id.TextViewFollowersNumber)).setText( Integer.toString(NewFollowersQnt) );
		} catch (Exception es) {
			// nothing to do
		}
	}
	
	
	/**
	 * The key to be used with {@link Intent#putExtra(String, String)}
	 */
	public static final String USER = "UserNick";
	
	private static final int ACTIVITY_USER = 0;
	private static final int ACTIVITY_MEEMI = 1;
	private static final int ACTIVITY_USERSLIST = 2;
	
	private static final int CONFIRM_BLOCK_USER = 10;
	private static final int CONFIRM_UNFOLLOW_USER = 11;
	
	
	private static final String CSS =
		"body { background-color: black; color: white; } " +
		"#userimage { float: left; } " +
		".avatar { max-width: 120px; max-height: 120px; } " +
		"#userinfo { text-align: justify; margin-left: 10px; } " +
		"#userprofile { clear:both; margin-top: 5px; } " +
		"#userinfo h1, #userinfo h2 { font-size: 13pt; } " +
		"a.link:link, a.link:visited, a.link:hover, a.link:active { color:aqua; text-decoration:underline; } " +
		"a.meemiaccount:link, a.meemiaccount:visited, a.meemiaccount:hover, a.meemiaccount:active { color:dodgerblue; text-decoration:underline; } ";
	
	private static final String HTMLTemplate =
		"<div id='usermaininfo'>" +
		"<div id='userimage'>" +
		"<img class='avatar' src='%s' />" +
		"</div>" +
		"<div id='userinfo'>" +
		"<h1>%s</h1>" +
		"<h2>%s</h2>" +
		"<p>%s - %s</p>" +
		"</div>" +
		"<div id='userprofile'>%s</div>" +
		"</div>" +
		"<div id='userdescription'>%s</div>";
}
