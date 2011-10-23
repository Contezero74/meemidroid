package adiep.meemidroid.engine;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import adiep.meemidroid.ImageLoader;
import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import adiep.meemidroid.Utility;
import adiep.meemidroid.engine.communication.HTTPEngine;
import adiep.meemidroid.engine.communication.HTTPMultipartPostEngine;
import adiep.meemidroid.engine.communication.HTTPPostEngine;
import adiep.meemidroid.support.compatibility.Pair;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * This class represents the engine used to access to the Meemi social network.
 * It's based upon the version 3.x of the Meemi's API.
 * 
 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
 * @version 1.4
 */
public class MeemiEngine {
	// Identifiers for method callback
	public static final int CB_NONE					= 0;
	public static final int CB_CREDENTIAL_CHECK		= 1;
	public static final int CB_USER_PROFILE			= 2;
	public static final int CB_USER_BLOCK			= 3;
	public static final int CB_USER_FOLLOW			= 4;
	public static final int CB_FOLLOWERS			= 5;
	public static final int CB_FOLLOWING			= 6;
	public static final int CB_LIFESTREAM			= 7;
	public static final int CB_REPLYSTREAM			= 8;
	public static final int CB_SINGLE_MEEME			= 9;
	public static final int CB_POST_MESSAGE			= 10;
	public static final int CB_POST_IMAGE			= 11;
	public static final int CB_REPLY_MESSAGE		= 12;
	public static final int CB_POST_LOCATION		= 13;
	public static final int CB_SEARCH				= 14;
	public static final int CB_NOTIFY_STATS			= 15;
	public static final int CB_NOTIFY_MEMES			= 16;
	public static final int CB_NOTIFY_REPLIES		= 17;
	public static final int CB_NOTIFY_PRIV_MEMES	= 18;
	public static final int CB_NOTIFY_PRIV_REPLIES	= 19;
	public static final int CB_NOTIFY_MENTIONS		= 20;
	public static final int CB_NOTIFY_FOLLOWERS		= 21;
	public static final int CB_MARK_AS_READ			= 22;
	public static final int CB_MARK_UNMARK_AS_FAV	= 23;
	
	
	/**
	 * This class represent a generic response to a Meemi API call.
	 * It encapsulates the JSON API response and contains the identifier of
	 * the original request in order to support asynchronous requests.
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 * @version 1.0
	 */
	public class MeemiEngineResult {
		public JSONObject Object	= null;
		public JSONArray Array		= null;
		public int CallbackMethod	= CB_NONE;
	}
	
	
	/**
	 * A class (principally an {@link Activity}) that wants to use
	 * {@link MeemiEngine} needs to implement this interface.
	 * In fact all the calls to Meemi's API are asynchronous, so the caller
	 * need to be called back in order to have access to the results.
	 * 
	 * The class that implements this interface can manage by itself the the
	 * result of API, returned in {@link MeemiEngineResult}, or can
	 * use the static methods with the prefix 'parse' already implemented
	 * in {@link MeemiEngine}
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 * @version 1.0
	 */
	public interface Callbackable {
		/**
		 * The callback method to implement to have access to the result
		 * of a Meemi's API call.
		 * 
		 * @param Result	the API call result
		 */
		void onEngineExecuteResult(MeemiEngineResult Result);
	}
	
	
	/**
	 * This is the class constructor and create the engine that
	 * manages the interaction with Meemi's API.
	 */
	public MeemiEngine() {
		MyCredentials = new MeemiCredentials();
		MyCredentials.load();
		
		MyLocationEngine = new LocationEngine(this);
	}
	
	/**
	 * This method clears the Avatar cache in background. If the ForegroundWaiting
	 * flag has been set to true, a waiting dialog will be show.
	 * 
	 * @param ForegroundWaiting	true if a waiting dialog is needed
	 * @param C					the current Android context
	 */
	public void clearAvatarCache(final boolean ForegroundWaiting, Context C) {
		new AsyncCommand(AsyncCommand.CLEAR_AVATAR_CACHE, ForegroundWaiting, C).execute();
	}
	
	/**
	 * This method returns the user credentials stored in
	 * {@link MeemiCredentials}.
	 * 
	 * @return user credentials
	 * 
	 * @see MeemiCredentials
	 */
	public MeemiCredentials getCredentials() {
		return MyCredentials;
	}
	
	/**
	 * This method checks if the the credentials passed as input argument 
	 * are valid: i.e., if it's possible to access to the Meemi social
	 * network.
	 * In order to work the Internet connection has to be available.
	 *   
	 * @param C					the Activity context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseResultStatus(MeemiEngineResult)
	 */
	public void isCredentialValid(Context C, Callbackable CallbackInstance) {
		executeCommand(USER_EXISTS, NO_CMD_ARGS, true, CB_CREDENTIAL_CHECK, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method returns the Android Timer representing the synchronization
	 * manager.
	 * 
	 * @return The Timer representing the synchronization manager
	 */
	public Timer getSyncManager() {
		return SyncManager;
	}
	
	/**
	 * This method stops the synchronization of the user location with the
	 * Meemi server.
	 */
	public void stopLocationSync() {
		MyLocationEngine.stopLocationSync();
	}
	
	/**
	 * This method starts the synchronization of the user location with the 
	 * Meemi server, every a specified range of time.
	 * 
	 * @param Minute	the range of time used for the synchronization (expressed
	 * 					in minutes)
	 */
	public void startLocationSync(final int Minutes) {
		MyLocationEngine.startLocationSync(Minutes);
		
		String CurrentLocation = MeemiDroidApplication.Prefs.getLastKnowLocation();
		
		if (null != CurrentLocation && LocationEngine.ONLYDURINGMESSAGESENDING != Minutes) {
			postLocation(CurrentLocation);
		}
	}
	
	/**
	 * This method sets the current location into Meemi.
	 * 
	 * @param Location	the location to set
	 */
	public void setCurrentLocation(final String Location) {
		postLocation( Location );
	}
	
	/**
	 * This method retrieves information about the input user.
	 *  
	 * @param User				the user ScreenName 
	 * @param C					the Activity context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 */
	public void getUserProfile(final String User, Context C, Callbackable CallbackInstance) {
		executeCommand(USER_PROFILE, new String[]{User}, true, CB_USER_PROFILE, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method sets if the current user wants to block or unblock the
	 * specified user.
	 *  
	 * @param User				the user to block/unblock
	 * @param Block				true if the user has to be blocked, otherwise
	 * 							false
	 * @param C					the Activity context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseResultStatus(MeemiEngineResult)
	 */
	public void setUserBlock(final String User, final boolean Block, Context C, Callbackable CallbackInstance) {
		String Command = UNBLOCK_USER;
		if (Block) {
			Command = BLOCK_USER;
		}
		
		executeCommand(Command, new String[]{User}, true, CB_USER_BLOCK, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method sets if the current user wants to follow or unfollow the
	 * specified user.
	 *  
	 * @param User				the user to block/unblock
	 * @param Follow			true if the user has to be blocked, otherwise
	 * 							false
	 * @param C					the Activity context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseResultStatus(MeemiEngineResult)
	 */
	public void setUserFollow(final String User, final boolean Follow, Context C, Callbackable CallbackInstance) {
		String Command = UNFOLLOW_USER;
		if (Follow) {
			Command = FOLLOW_USER;
		}
		
		executeCommand(Command, new String[]{User}, true, CB_USER_FOLLOW, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method returns the followers of the user used as input argument.
	 * 
	 * @param User				the user followed
	 * @param Page				the page to load, every page can be composed
	 * 							from 30 entry
	 * @param C					the current Android context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseFriendsResult(MeemiEngineResult)
	 */
	public void getFollowers(final String User, final int Page, Context C, Callbackable CallbackInstance) {		
		executeCommand(FOLLWERS, new String[]{User, Integer.toString(Page)}, true, CB_FOLLOWERS, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method returns the users followed by the user used as input argument.
	 * 
	 * @param User				the starting user 
	 * @param Page				the page to load
	 * @param C					the current Android context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseFriendsResult(MeemiEngineResult)
	 */
	public void getFollowings(final String User, final int Page, Context C, Callbackable CallbackInstance) {
		executeCommand(FOLLWINGS, new String[]{User, Integer.toString(Page)}, true, CB_FOLLOWING, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method returns the specified lifestream of the current user.
	 * 
	 * @param User				the current user related with the lifestream
	 * 							to retrieve
	 * @param LifeStream		the lifestream to return 
	 * @param Page				the page to load
	 * @param C					the current Android context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseMeemiStreamResult(MeemiEngineResult)
	 */
	public void getLifeStream(final String User, final int LifeStream, final int Page, Context C, Callbackable CallbackInstance) {
		String Cmd		= USERLIFESTREAM;
		String[] Args	= new String[]{User, Integer.toString(Page)};
		
		switch (LifeStream) {
		case LifestreamConst.GENERAL_LS:
			Args		= new String[]{GENERAL_LS_API, Integer.toString(Page)};
			Cmd			= LIFESTREAM;
			break;
			
		case LifestreamConst.PRIVATE_LS:
			Args		= new String[]{PRIVATE_LS_API, Integer.toString(Page)};
			Cmd			= LIFESTREAM;
			break;
		case LifestreamConst.PRIVATE_SENT_LS:
			Args		= new String[]{PRIVATE_SENT_LS_API, Integer.toString(Page)};
			Cmd			= LIFESTREAM;
			break;
		case LifestreamConst.FAVORITES_LS:
			Args		= new String[]{User, Integer.toString(Page)};
			Cmd			= USERFAVORITE;
			break;
		default:
			// nothing to do
		}
		
		executeCommand(Cmd, Args, true, CB_LIFESTREAM, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method returns the specified replies to a specific Meemi.
	 * 
	 * @param User				the original Meemer wrote the post
	 * @param MeemiId			the original Meeme Id  
	 * @param StartingReply		the starting reply meemi to download
	 * @param C					the current Android context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseMeemiStreamResult(MeemiEngineResult)
	 */
	public void getReplies(final String User, final String MeemiId, final int StartingReply, Context C, Callbackable CallbackInstance) {		
		executeCommand(REPLIES, new String[]{User, MeemiId, Integer.toString(StartingReply)}, true, CB_REPLYSTREAM, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method returns the specified single meeme.
	 * 
	 * @param User				the current user related with the lifestream
	 * 							to retrieve
	 * @param MeemeId			the original Meeme Id   
	 * @param C					the current Android context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseSingleMeemeResult(MeemiEngineResult)
	 */
	public void getSingleMeeme(final String User, final String MeemeId, Context C, Callbackable CallbackInstance) {
		executeCommand(GETSINLEMEEME, new String[]{User, MeemeId}, true, CB_SINGLE_MEEME, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method executes a search into the Meemi messages.
	 * 
	 * @param Search			the search object
	 * @param C					the current Android context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseMeemiStreamResult(MeemiEngineResult)
	 */
	public void executeSearch(final String Search, Context C, Callbackable CallbackInstance) {		
		executeCommand(SEARCH, new String[]{Search}, true, CB_SEARCH, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method posts a message to Meemi.
	 * 
	 * @param Message			the message to post
	 * @param PrivateUsersList	the comma separated list of the users that have
	 * 							to receive
	 * 							the massage (note: the message becomes
	 * 							private)
	 * @param C					the Activity context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parsePostMeemiResult(MeemiEngineResult)
	 */
	public void postMessage(final String Message, final String PrivateUsersList, Context C, Callbackable CallbackInstance) {
		List<Pair<String, String>> Args = new ArrayList<Pair<String,String>>();
		Args.add( new Pair<String, String>( "meme_type", "text") );
		Args.add( new Pair<String, String>( "text_content", prepareMessageWithLocation(Message) ) );
		
		if ( null != PrivateUsersList && 0 != PrivateUsersList.trim().length() ) {
			Args.add( new Pair<String, String>( "private_sn", PrivateUsersList.trim() ) );
		}
		
		executeCommand(POST_MESSAGE, NO_CMD_ARGS, Args, true, CB_POST_MESSAGE, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method replay to a message in Meemi.
	 * 
	 * @param Message			the message to post
	 * @param MeemiId			the original Meemi message ID
	 * @param UserNick			the Nick of the Meemer wrote the original
	 * 							message
	 * @param C					the Activity context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parsePostMeemiResult(MeemiEngineResult)
	 */
	public void replyToMessage(final String Message, final String OriginalMessageID, final String OriginalMeemer, Context C, Callbackable CallbackInstance) {
		List<Pair<String, String>> Args = new ArrayList<Pair<String,String>>();
		Args.add( new Pair<String, String>( "meme_type", "text") );
		Args.add( new Pair<String, String>( "text_content", prepareMessageWithLocation(Message) ) );
		Args.add( new Pair<String, String>( "reply_screen_name", OriginalMeemer.trim() ) );
		Args.add( new Pair<String, String>( "reply_meme_id", OriginalMessageID.trim() ) );
		
		executeCommand(REPLY_TO_MESSAGE, NO_CMD_ARGS, Args, true, CB_REPLY_MESSAGE, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method posts an image stored in the Android device to Meemi.
	 * 
	 * @param Message			the message to post
	 * @param ImageUri			the URI of the image to post
	 * @param PrivateUsersList	the comma separated list of the users that have
	 * 							to receive
	 * 							the massage (note: the message becomes private)
	 * @param C					the Activity context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parsePostMeemiResult(MeemiEngineResult)
	 */
	public void postImage(final String Message, final Uri ImageUri, final String PrivateUsersList, Context C, Callbackable CallbackInstance) {
		List<Pair<String, String>> Args = new ArrayList<Pair<String,String>>();
		Args.add( new Pair<String, String>("meme_type", "image") );
		Args.add( new Pair<String, String>( "text_content", prepareMessageWithLocation(Message) ) );
		
		if ( null != PrivateUsersList && 0 != PrivateUsersList.trim().length() ) {
			Args.add( new Pair<String, String>( "private_sn", PrivateUsersList.trim() ) );
		}
		
		executeCommand(POST_MESSAGE, NO_CMD_ARGS, Args, true, ImageUri, CB_POST_IMAGE, false, true, C, CallbackInstance);
	}
	
	/**
	 * This method posts the location to Meemi.
	 * 
	 * @param Location	the location to post
	 * 
	 * @see #parsePostMeemiResult(MeemiEngineResult)
	 */
	public void postLocation(final String Location) {
		if ( !LastSentLocation.equals(Location) ) {
			List<Pair<String, String>> Args = new ArrayList<Pair<String,String>>();
			
			if (MeemiDroidApplication.Prefs.isLocationCompatibilityModeEnabled()) {
				// this code is added as backward compatibility (probably will be deleted in future)
				Log.i("MeemiEngine - postLocation", "Location posted in compatibility mode: " + Location);
				
				Args.add( new Pair<String, String>( "meme_type", "text") );
				Args.add( new Pair<String, String>( "text_content", "(l: " + Location + ")" ) );
				
				executeCommand( POST_MESSAGE, NO_CMD_ARGS, Args, true, CB_POST_MESSAGE, false, true, MeemiDroidApplication.getContext(), null );
			} else {
				Log.i("MeemiEngine - postLocation", "Location posted: " + Location);
				Args.add( new Pair<String, String>( "location", Location ) );
				
				executeCommand( POST_LOCATION, NO_CMD_ARGS, Args, true, CB_POST_LOCATION, false, true, MeemiDroidApplication.getContext(), null );
			}
			LastSentLocation = Location;
		
			MeemiDroidApplication.Prefs.setLastKnowLocation(Location);
			MeemiDroidApplication.Prefs.save();
		}
	}
	
	/**
	 * This method returns the notification stats.
	 * This method runs in background by default.
	 * 
	 * @param C					the current Android context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseNotifyStats(MeemiEngineResult)
	 */
	public void getNotifiesStats(Context C, Callbackable CallbackInstance) {	
		executeCommand(GET_NOTIFY_STATS, NO_CMD_ARGS, true, CB_NOTIFY_STATS, false, false, C, CallbackInstance);
	}
	
	/**
	 * This method returns a set of messages representing the selected notification
	 * filter.
	 * 
	 * @param NotifyType		the notification filter to use (@see NotifiesConst)
	 * @param C					the current Android context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseMeemiStreamResult(MeemiEngineResult)
	 */
	public void getNotifies(final int NotifyType, Context C, Callbackable CallbackInstance) {
		int CallbackMathod = 0;
		String[] Args = NO_CMD_ARGS;
		boolean ExecuteCmd = true;
		
		switch (NotifyType) {
		case NotifiesConst.NOTIFY_MEMES:
			CallbackMathod = CB_NOTIFY_MEMES;
			Args = new String[]{NTF_MEMES};
			break;
			
		case NotifiesConst.NOTIFY_REPLIES:
			CallbackMathod = CB_NOTIFY_REPLIES;
			Args = new String[]{NTF_REPLIES};
			break;
			
		case NotifiesConst.NOTIFY_PRIVATE_MEMES:
			CallbackMathod = CB_NOTIFY_PRIV_MEMES;
			Args = new String[]{NTF_PRIVATE_MEMES};
			break;
			
		case NotifiesConst.NOTIFY_PRIVATE_REPLIES:
			CallbackMathod = CB_NOTIFY_PRIV_REPLIES;
			Args = new String[]{NTF_PRIVATE_REPLIES};
			break;
			
		case NotifiesConst.NOTIFY_MENTIONS:
			CallbackMathod = CB_NOTIFY_MENTIONS;
			Args = new String[]{NTF_MENTIONS};
			break;
			
		case NotifiesConst.NOTIFY_FOLLOWERS:
			CallbackMathod = CB_NOTIFY_FOLLOWERS;
			Args = new String[]{NTF_FOLLOWERS};
			break;
		
		default:
			ExecuteCmd = false;
		}
		
		if (ExecuteCmd) {
			executeCommand(NOTIFIES, Args, true, CallbackMathod, false, false, C, CallbackInstance);
		} else {
			Log.w("MeemiEngine - getNotifies", "The notification type " + NotifyType + " is not defined");
		}
	}
	
	/**
	 * This method sets a specified message as read.
	 * 
	 * @param MeemiId			the identifier of the message to set to read
	 * @param C					the current Android context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseResultStatus(MeemiEngineResult)
	 */
	public void markAsRead(final String MeemiId, Context C, Callbackable CallbackInstance) {
		executeCommand(MARK_AS_READ, new String[]{MeemiId}, true, CB_MARK_AS_READ, false, false, C, CallbackInstance);
	}
	
	/**
	 * This method marks (or unmarks) a specified message as favorite.
	 * 
	 * @param MeemiId			the identifier of the message to set to read
	 * @param MeemiUser			???
	 * @param C					the current Android context
	 * @param CallbackInstance	the {@link Callbackable} instance (can be null)
	 * 
	 * @see #parseResultStatus(MeemiEngineResult)
	 */
	public void switchAsFavorite(final String MeemiId, final String MeemiUser, Context C, Callbackable CallbackInstance) {
		executeCommand(MARK_UNMARK_AS_FAV, new String[]{MeemiUser, MeemiId}, true, CB_MARK_UNMARK_AS_FAV, false, false, C, CallbackInstance);
	}
	
	
	/**
	 * This method parses the response to the following requests and return
	 * true if the request has been accomplished, otherwise it returns false.
	 * This method can be used to parse response from:
	 * - {@link #isCredentialValid(Context, Callbackable)}
	 * - {@link #setUserBlock(String, boolean, Context, Callbackable)}
	 * - {@link #setUserFollow(String, boolean, Context, Callbackable)}
	 * - {@link #markAsRead(String, Context, Callbackable)}
	 * - {@link #switchAsFavorite(String, String, Context, Callbackable)}
	 * 
	 * @param Result	the {@link MeemiEngineResult} to parse
	 * 
	 * @return true if the request has been accomplished, otherwise false
	 * 
	 * @see #isCredentialValid(Context, Callbackable)
	 * @see #setUserBlock(String, boolean, Context, Callbackable)
	 * @see #setUserFollow(String, boolean, Context, Callbackable)
	 * @see #markAsRead(String, Context, Callbackable)
	 * @see #switchAsFavorite(String, String, Context, Callbackable)
	 */
	public static final boolean parseResultStatus(final MeemiEngineResult Result) {
		boolean IsStatusOk = false;
		
		if (null != Result) {
			if (CB_CREDENTIAL_CHECK == Result.CallbackMethod ||
				CB_USER_BLOCK == Result.CallbackMethod ||
				CB_USER_FOLLOW == Result.CallbackMethod ||
				CB_MARK_AS_READ == Result.CallbackMethod ||+
				CB_MARK_UNMARK_AS_FAV == Result.CallbackMethod) {
				
				try {
					if (null != Result.Object) {
						IsStatusOk = ( 1 == Result.Object.getJSONObject("message").getInt("status") );
					}
				} catch (JSONException ex) {
					Log.e("MeemiEngine - parseResultStatus", "Problem during response parsing", ex);
				}
					
			} else {
				Log.w("MeemiEngine - parseResultStatus", "You use the wrong 'parse' method");
			}
		}
		
		return IsStatusOk;
	}
	
	/**
	 * This method parses the response to a request for getting a Meemi user
	 * profile ({@link #getUserProfile(String, Context, Callbackable)}, and it returns user
	 * information into a {@link Map} in order to simplify the access to
	 * information.
	 * 
	 * @param Result	the {@link MeemiEngineResult} to parse
	 * 
	 * @return a {@link Map} containing the Meemi user profile information
	 * 
	 * @see #getUserProfile(String, Context, Callbackable)
	 */
	public static final Map<String, String> parseUserProfileResult(final MeemiEngineResult Result) {
		Map<String, String> Profile = new TreeMap<String, String>();
		
		if ( null != Result ) {
			if (CB_USER_PROFILE == Result.CallbackMethod) {
				try {
					if (null != Result.Object) {
						Profile.put( "screen_name", Result.Object.getString("screen_name") );
						Profile.put( "avatar_url", Result.Object.getString("avatar") );
						Profile.put( "real_name", Result.Object.getString("real_name") );
						
						String TmpFormatedDate = Utility.formatDate( Result.Object.getString("birth"), MeemiDroidApplication.getContext().getString(R.string.DateFormat), "yyyy-MM-dd" );
						Profile.put( "birth", TmpFormatedDate);
						
						Profile.put( "current_location", Result.Object.getString("current_location") );
						Profile.put( "followings", Result.Object.getString("qta_followings") );
						Profile.put( "followers", Result.Object.getString("qta_followers") );
						Profile.put( "is_followed", Result.Object.getString("you_follow") );
						Profile.put( "he_follows", Result.Object.getString("follow_you") );
						Profile.put( "is_blocked", Result.Object.getString("block_this_user") );
						Profile.put( "description", Result.Object.getString("description") );
						Profile.put( "profile", Result.Object.getString("profile") );
					}
				} catch (JSONException ex) {
					Log.e("MeemiEngine - parseUserProfileResult", "Problem during response parsing", ex);
				}
			} else {
				Log.w("MeemiEngine - parseUserProfileResult", "You use the wrong 'parse' method");
			}
		}
		
		return Profile;
	}
	
	/**
	 * This method parses the result of a
	 * {@link #getFollowers(String, int, Context, Callbackable)} or
	 * {@link #getFollowings(String, int, Context, Callbackable)} call, returning a
	 * {@link List} where each item represents a friend (follower or followed).
	 * Each item is a {@link Map} containing all friend information needed.
	 * 
	 * @param Result	the {@link MeemiEngineResult} to parse
	 * 
	 * @return a {@link List} where each item represents a friend
	 * 
	 * @see #getFollowers(String, int, Context, Callbackable)
	 * @see #getFollowings(String, int, Context, Callbackable)
	 */
	public static final List<TreeMap<String, String>> parseFriendsResult(final MeemiEngineResult Result) {
		List<TreeMap<String, String>> Friends = new ArrayList<TreeMap<String, String>>();
		
		if (null != Result) {
			if (CB_FOLLOWERS == Result.CallbackMethod ||
				CB_FOLLOWING == Result.CallbackMethod) {
				
				JSONArray Users = Result.Array;

				if (null != Users && 0 != Users.length() ) {
					for(int u=0; u < Users.length(); ++u) {
						try {
							JSONObject F = Users.getJSONObject(u);
							
							TreeMap<String, String> Item = new TreeMap<String, String>();
							Item.put( "UserId", F.getString("screen_name") );
							Item.put( "Avatar", F.getString("avatar") );
							Item.put( "Follow", F.getString("follow_you") );
							Item.put( "Followed", F.getString("you_follow") );						
							Friends.add(Item);
						} catch (JSONException ex) {
							Log.e("MeemiEngine - parseFriendsResult", "Problem during response parsing", ex);
						}
					}
				}
			} else {
				Log.w("MeemiEngine - parseFriendsResult", "You use the wrong 'parse' method");
			}
		}
		
		return Friends;		
	}
	
	/**
	 * This method parses the response to the following requests and return
	 * a {@link List} of Meemi messages. Each message is represented as a
	 * {@link Map} containing the information needed.
	 * This method can be used to parse response from:
	 * - {@link #getLifeStream(String, String, int, Context, Callbackable)}
	 * - {@link #getReplies(String, String, int, Context, Callbackable)}
	 * - {@link #executeSearch(String, Context, Callbackable)}
	 * - {@link #getNotifies(int, Context, Callbackable)}
	 * 
	 * @param Result	the {@link MeemiEngineResult} to parse
	 * 
	 * @return a list of data about each Meeme message. Each message is represented as a {@link Map}
	 * 
	 * @see #getLifeStream(String, String, int, Context, Callbackable)
	 * @see #getReplies(String, String, int, Context, Callbackable)
	 * @see #executeSearch(String, Context, Callbackable)
	 * @see #getNotifies(int, Context, Callbackable)
	 */
	public static final List<TreeMap<String, String>> parseMeemiStreamResult(final MeemiEngineResult Result) {
		List<TreeMap<String, String>> Meemis = new ArrayList<TreeMap<String, String>>();
		
		if (null != Result) {
			if (CB_LIFESTREAM == Result.CallbackMethod ||
				CB_REPLYSTREAM == Result.CallbackMethod) {
				JSONArray MeemisArray = Result.Array;
				
				if (null != MeemisArray && 0 != MeemisArray.length() ) {
					for(int u=0; u < MeemisArray.length(); ++u) {
							try {
								JSONObject M = MeemisArray.getJSONObject(u);
				
								TreeMap<String, String> Item = extractMeemiMessage(M);
				
								Meemis.add(Item);
							} catch (JSONException ex) {
								Log.e("MeemiEngine - parseMeemiStreamResult", "Problem during response parsing", ex);
							}
					}
				}
			} else {
				Log.w("MeemiEngine - parseMeemiStreamResult", "You use the wrong 'parse' method");
			}
		}
		
		return Meemis;		
	}
	
	/**
	 * This method parses the response to
	 * {@link #getSingleMeeme(String, String, Context, Callbackable)} and returns a
	 * {@link Map} containing all the message information needed
	 * 
	 * @param Result	the {@link MeemiEngineResult} to parse
	 * 
	 * @return a {@link Map} the message data 
	 * 
	 * @see #getSingleMeeme(String, String, Context, Callbackable)
	 */
	public static final TreeMap<String, String> parseSingleMeemeResult(final MeemiEngineResult Result) {
		TreeMap<String, String> Meeme = new TreeMap<String, String>();
		
		if (null != Result) {
			if (CB_SINGLE_MEEME == Result.CallbackMethod) {
				JSONArray MeemisArray = Result.Array;

				if (null != MeemisArray && 0 != MeemisArray.length() ) {
					try {
						JSONObject M = Result.Array.getJSONObject(0);
					
						Meeme = extractMeemiMessage(M);
					} catch (JSONException ex) {
						Log.e("MeemiEngine - parseSingleMeemeResult", "Problem during response parsing", ex);
					}
				}
			} else {
				Log.w("MeemiEngine - parseSingleMeemeResult", "You use the wrong 'parse' method");
			}
		}
		
		return Meeme;
	}
	
	/**
	 * This method parses the response to the following requests and return
	 * a the meeme identifier of a new post or reply.
	 * This method can be used to parse response from:
	 * - {@link #postMessage(String, String, Context)}
	 * - {@link #replyToMessage(String, String, String, Context, boolean, Callbackable)}
	 * - {@link #postImage(String, Uri, String, Context, Callbackable)}
	 * - {@link #postLocation(String)}
	 * 
	 * @param Result	the {@link MeemiEngineResult} to parse
	 * 
	 * @return the id of posted message
	 * 
	 * @see #postMessage(String, String, Context, Callbackable CallbackInstance)
	 * @see #replyToMessage(String, String, String, Context, boolean, Callbackable)
	 * @see {@link #postImage(String, Uri, String, Context, Callbackable)}
	 * @see #postLocation(String)
	 */
	public static final String parsePostMeemiResult(final MeemiEngineResult Result) {
		String MeemeId = null;
		
		if (null != Result) {
			if (CB_POST_MESSAGE == Result.CallbackMethod ||
				CB_REPLY_MESSAGE == Result.CallbackMethod ||
				CB_POST_IMAGE == Result.CallbackMethod ||
				CB_POST_MESSAGE == Result.CallbackMethod) {
				
				try {
					if (null != Result.Object) {
						MeemeId = Result.Object.getString("id_meeme");
					}
				} catch (JSONException ex) {
					Log.e("MeemiEngine - parsePostMeemiResult", "Problem during response parsing", ex);
				}
			} else {
				Log.w("MeemiEngine - parsePostMeemiResult", "You use the wrong 'parse' method");
			}
		}
		
		return MeemeId;		
	}
	
	
	/**
	 * This method parses the response to the {@link #getNotifiesStats(Context, Callbackable)}
	 * method.
	 * 
	 * @param Result	the {@link MeemiEngineResult} to parse
	 * 
	 * @return a {@link Map} representing the stats about Meemi notification 
	 * 
	 * @see #getNotifiesStats(Context, Callbackable)
	 */
	public static final TreeMap<String, String> parseNotifyStats(final MeemiEngineResult Result) {
		TreeMap<String, String> Notifies = new TreeMap<String, String>();
		
		if (null != Result) {
			if (CB_NOTIFY_STATS == Result.CallbackMethod) {
				try {
					if (null != Result.Object) {
						
						Notifies.put( "Memes", Result.Object.getString("memes") );
						Notifies.put( "Replies", Result.Object.getString("replies") );
						Notifies.put( "PrivMemes", Result.Object.getString("memes_priv") );
						Notifies.put( "PrivReplies", Result.Object.getString("replies_priv") );
						Notifies.put( "Mentions", Result.Object.getString("mentions") );
						Notifies.put( "Followers", Result.Object.getString("new_followers") );
					}
				} catch (JSONException ex) {
					Log.e("MeemiEngine - parseNotifyStats", "Problem during response parsing", ex);
				}
			}
		} else {
			Log.w("MeemiEngine - parseNotifyStats", "You use the wrong 'parse' method");
		}
		return Notifies;
	}
	
		
	/**
	 * This method executes a REST request via Meemi API.
	 * Note that the request is executed in asynchronous way.
	 * 
	 * @param Command				the command to execute
	 * @param CmdArgs				a set of arguments used to fill the Command
	 * 								(see {@link Formatter})
	 * @param UseAuhtetification	if this boolean value is true the request
	 * 								uses the Meemi authentification
	 * @param CallingMethod			the identifier of the calling method
	 * @param SyncFlag				true if execution has to be synchronized
	 * @param ForegroundFlag		true if a waiting message has to be showed
	 * 								during the process
	 * @param C						the Activity context
	 * @param CallbackInstance		the {@link Callbackable} instance (can be null)
	 * 
	 * @return	a {@link MeemiEngineResult} storing a set of pairs representing
	 * 			the API response. Note that the contents of the
	 * 			{@link MeemiEngineResult} changes according to how the API has
	 * 			been invoked. Can be <code>null</code> if the request was
	 * 			asynchronous. 
	 */	
	private final MeemiEngineResult executeCommand(final String Command, final String[] CmdArgs, final boolean UseAuhtetification, final int CallingMethod, final boolean SyncFlag, final boolean ForegroundFlag, Context C, Callbackable CallbackInstance) {
		return executeCommand(Command, CmdArgs, null, UseAuhtetification, null, CallingMethod, SyncFlag, ForegroundFlag, C, CallbackInstance);
	}
	
	/**
	 * This method executes a REST request via Meemi API.
	 * Note that the request is executed in asynchronous way.
	 * 
	 * @param Command				the command to execute
	 * @param CmdArgs				a set of arguments used to fill the Command
	 * 								(see {@link Formatter})
	 * @param Args					a list of String pair used as arguments for
	 * 								the web POST command
	 * @param UseAuhtetification	if this boolean value is true the request
	 * 								uses the Meemi authentification
	 * @param CallingMethod			the identifier of the calling method
	 * @param SyncFlag				true if execution has to be synchronized
	 * @param ForegroundFlag		true if a waiting message has to be showed
	 * 								during the process
	 * @param C						the Activity context
	 * @param CallbackInstance		the {@link Callbackable} instance (can be null)
	 * 
	 * @return	a {@link MeemiEngineResult} storing a set of pairs representing
	 * 			the API response. Note that the contents of the
	 * 			{@link MeemiEngineResult} changes according to how the API has
	 * 			been invoked. Can be <code>null</code> if the request was
	 * 			asynchronous. 
	 */	
	private final MeemiEngineResult executeCommand(final String Command, final String[] CmdArgs, final List<Pair<String, String>> Args, final boolean UseAuhtetification, final int CallingMethod, final boolean SyncFlag, final boolean ForegroundFlag, Context C, Callbackable CallbackInstance) {
		return executeCommand(Command, CmdArgs, Args, UseAuhtetification, null, CallingMethod, SyncFlag, ForegroundFlag, C, CallbackInstance);
	}

	/**
	 * This method executes a REST request via Meemi API.
	 * Note that the request is executed in asynchronous way.
	 * 
	 * @param Command				the command to execute
	 * @param CmdArgs				a set of arguments used to fill the Command
	 * 								(see {@link Formatter})
	 * @param Args					a list of String pair used as arguments for
	 * 								the web POST command
	 * @param UseAuhtetification	if this boolean value is true the request
	 * 								uses the Meemi authentification
	 * @param ImageUri				the {@link Uri} of the image to upload (it
	 * 								works only with the POST_MESSAGE message)
	 * @param CallingMethod			the identifier of the calling method
	 * @param SyncFlag				true if execution has to be synchronized
	 * @param ForegroundFlag		true if a waiting message has to be showed
	 * 								during the process
	 * @param C						the Activity context
	 * @param CallbackInstance		the {@link Callbackable} instance (can be null)
	 *  
	 * @return	a {@link MeemiEngineResult} storing a set of pairs representing
	 * 			the API response. Note that the contents of the
	 * 			{@link MeemiEngineResult} changes according to how the API has
	 * 			been invoked. Can be <code>null</code> if the request was
	 * 			asynchronous. 
	 */
	private final MeemiEngineResult executeCommand(final String Command, final String[] CmdArgs, final List<Pair<String, String>> Args, final boolean UseAuhtetification, final Uri ImageUri, final int CallingMethod, final boolean SyncFlag, final boolean ForegroundFlag, Context C, Callbackable CallbackInstance) {
		
		SenderArguments STArgs = new SenderArguments(new Formatter().format(Command, (Object[])CmdArgs).toString(), UseAuhtetification, Args, ImageUri);
		
		SenderTask ST = (SenderTask)( new SenderTask(this, CallingMethod, ForegroundFlag, C, CallbackInstance).execute(STArgs) );

		MeemiEngineResult Result = null;
		
		if (SyncFlag) {
			try {
				Result = ST.get();
			} catch (Exception ex) {
				Log.d("MeemiEngine", "Problems with AsyncTask", ex);
			}
		}
		
		return Result;
	}
	
	/**
	 * This method modify the input message adding the location part if needed.
	 * 
	 * @param Message	the original message
	 * 
	 * @return	the message with the location part if needed
	 */
	private String prepareMessageWithLocation(final String Message) {
		String Message2Send = Message.trim();
		if ( MeemiDroidApplication.Prefs.isLocationEnabled() && LocationEngine.ONLYDURINGMESSAGESENDING == MeemiDroidApplication.Prefs.getLocationSyncMin() ) {
			if ( null != MyLocationEngine.getCurrentLocation() ) {
				Message2Send += "\n\n(l: " + MyLocationEngine.getCurrentLocation() + ")";
			}
		}
		return Message2Send;
	}
	
	
	/**
	 * This is a support class used to setup the AsyncTask  request.
	 *  
	 * @author Andrea de Iacovo, and Eros Pedrini
	 * @version 1.0
	 */
	private class SenderArguments {	
		/**
		 * The SenderArguments constructor.
		 * 
		 * @param Command				the command to execute
		 * @param UseAuthentification	if this boolean value is true the request uses the Meemi authentification
		 * @param ImageUri				the URI of the image to upload
		 */
		public SenderArguments(final String Command, final boolean UseAuthentification, final List<Pair<String, String>> Args, final Uri ImageUri) {
			this.Command = Command;
			this.UseAuthentification = UseAuthentification;
			this.Arguments = Args;
			this.ImageUri = ImageUri;
		}
		
		/**
		 * The command to execute
		 */
		public String Command;
		
		/**
		 * If this boolean value is true the request uses the Meemi authentification
		 */
		public boolean UseAuthentification;
		
		/**
		 * The list of optional arguments needed by the REST command
		 */
		public List<Pair<String, String>> Arguments = null;
		
		/**
		 * The URI of the image to upload
		 */
		public Uri ImageUri = null;
	}
	
	
	/**
	 * This private class represents an asynchronous REST request.
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 * @version 1.4
	 */
	private class SenderTask extends AsyncTask< SenderArguments, Void, MeemiEngineResult > {
		/**
		 * The constructor: used to setup the asynchronous request.
		 * 
		 * @param ME	the Meemi engine, used to access to credentials and others stuff
		 * @param CM	the identifier of the method asking for the service
		 * @param FF	true if the waiting dialog has to be showed during the process
		 * @param C		the Activity context
		 */
		public SenderTask(MeemiEngine ME, final int CM, final boolean FF, Context C) {
			this(ME, CM, FF, C, null);
		}

		/**
		 * The constructor: used to setup the asynchronous request.
		 * 
		 * @param ME	the Meemi engine, used to access to credentials and others stuff
		 * @param CM	the identifier of the method asking for the service
		 * @param FF	true if the waiting dialog has to be showed during the process
		 * @param C		the Activity context
		 * @param CB	the {@link Callbackable} instance (can be null)
		 */
		public SenderTask(MeemiEngine ME, final int CM, final boolean FF, Context C, Callbackable CB) {
			this.MyEngine = ME;
			this.ForegroundFlag = FF;
			this.CallingMethod = CM;
			
			this.CurrentContext = C;
			
			this.CallbackInstance = CB;
		}
		
		/**
		 * This method performs a computation on a background thread.
		 * The specified parameters are the parameters passed to execute(Params...)  by the caller of this task.
		 * This method can call publishProgress(Progress...) to publish updates on the UI thread.
		 * 
		 * @param args0	the parameters of the task
		 * 
		 * @return	a {@link MeemiEngineResult} storing a set of pairs representing the API response. Note that
		 * 			the contents of the JSONObject changes according to the API invoked
		 */
		@Override
		protected MeemiEngineResult doInBackground(SenderArguments... Args) {
			MeemiEngineResult Result = null;
			
			SenderArguments Arg = Args[0];
			
			HTTPEngine HttpClient = null;
			if ( null != Arg.ImageUri && Arg.Command.equals(POST_MESSAGE) ) {
				File ObjectFile = new File( Arg.ImageUri.getPath() );	
				try {
					FileInputStream read = (FileInputStream)MeemiDroidApplication.getContext().getContentResolver().openInputStream(Arg.ImageUri);
				
					int bytes = read.available();
					byte ImageBytes[] = new byte[bytes];
					int Size = read.read(ImageBytes, 0, bytes);
					
					if ( MeemiDroidApplication.Prefs.isImageResizeEnabled() ) {
						int[] NewSize = MeemiDroidApplication.Prefs.getImageResize();
						int Quality = MeemiDroidApplication.Prefs.getImageQuality();
						
						ImageBytes = Utility.resizeImage(ImageBytes, NewSize[0], NewSize[1], Quality);
						
						Size = ImageBytes.length;
					}
				
					HttpClient = new HTTPMultipartPostEngine(MeemiUrl + Arg.Command + "/json", ImageBytes, Size, ObjectFile.getName(), "img_pc", CurrentContext);
				} catch (Exception ex) {
					Log.d("MeemiEngine - SenderTask", "Problem with image access: " + ex, ex);
				}
			} else {
				HttpClient = new HTTPPostEngine(MeemiUrl + Arg.Command + "/json");
			}

			if (null != HttpClient) {
				if (Arg.UseAuthentification) {
					// Add data for authentification
					HttpClient.addEntityPair( new Pair<String, String>( "app_key", MeemiAPIKey ) );
					HttpClient.addEntityPair( new Pair<String, String>( "meemi_id", MyEngine.getCredentials().getUsername() ) );
					HttpClient.addEntityPair( new Pair<String, String>( "pwd", MyEngine.getCredentials().getHashedPassword() ) );
				}
				
				if (null != Arg.Arguments) {
					for (Pair<String, String> A : Arg.Arguments) {
						HttpClient.addEntityPair(A);
					}
				}
				
				String JSonResponse = HttpClient.execute();
				
				Log.w("SenderTask", "The JSonResponse for " + Arg.Command + " is\n" + JSonResponse);
				
				try {  
			        JSONObject Tmp = new JSONObject(JSonResponse);
			        
			        Result = new MeemiEngineResult();
			        Result.Object = Tmp;
			        //Result.Type = JSONType.OBJECT;
				}  
				catch (JSONException ex1) {  
			        Log.d("SenderTask", "Is not a JSONObject... can be a JSONArray...");
			        
			        try {
			        	JSONArray Tmp = new JSONArray(JSonResponse);
			        	
			        	Result = new MeemiEngineResult();
				        Result.Array = Tmp;
				        //Result.Type = JSONType.ARRAY;			        
			        } catch (JSONException ex2) {
			        	Log.d("SenderTask", "Is not a JSONArray too", ex2);
			        	
			        	Result = new MeemiEngineResult();
			        }
				}  
			}
			
			Result.CallbackMethod = CallingMethod;
			
			return Result;
		}
			
		/**
		 * Runs on the UI thread before doInBackground(Params...).
		 */
		@Override
		protected void onPreExecute() {
			if (ForegroundFlag) {
				try {
					WaitingDialog = ProgressDialog.show(CurrentContext, "", CurrentContext.getString(R.string.AllertWaiting), true);
				} catch (Exception ex) {
					Log.d("MeemiEngine - Task", "Cannot open the Toast Dialog");
				}
			}
			
			super.onPreExecute();
		}
		
		
		/**
		 * Runs on the UI thread after doInBackground(Params...).
		 * The specified result is the value returned by doInBackground(Params...) or null
		 * if the task was canceled or an exception occurred.
		 */
		@Override
		protected void onPostExecute(MeemiEngineResult Result) {
			if (null != WaitingDialog) {
				WaitingDialog.dismiss();
			}
			
			super.onPostExecute(Result);
			
			if (null != CallbackInstance) {
				if ( CallbackInstance instanceof Callbackable ) {
					CallbackInstance.onEngineExecuteResult(Result);
				}
			}
		}

		private MeemiEngine MyEngine	= null;
		private int CallingMethod		= MeemiEngine.CB_NONE;
		private boolean ForegroundFlag	= true;
		
		private Callbackable CallbackInstance = null;
		
		private Context CurrentContext	= null;
		
		private Dialog WaitingDialog	= null;
		
		/**
		 * The Meemi API Key representing the MeemiDroid application
		 */
		private static final String MeemiAPIKey = "e0cdbf0e0f109804f903ff4c90faebfb298d3fc4"; // MeemiDroid Key
		// api v.3 alpha                           cf5557e9e1ed41683e1408aefaeeb4c6ee23096b
		// meemi client for iphone:                dd51e68acb28da24c221c8b1627be7e69c577985 
		
		/**
		 * The common part of the URL for the REST query.
		 */
		private static final String MeemiUrl = "http://meemi.com/api3/"; 
	}

	
	/**
	 * This method extracts the information stored in a Meemi message in order
	 * to be managed by the Meemi Client Application.
	 * 
	 * @param M			the {@link JSONObject} representing the Meemi message 
	 * 
	 * @return a Map storing all the information needed about the Meemi message
	 * 
	 * @throws JSONException
	 */
	private static final TreeMap<String, String> extractMeemiMessage(final JSONObject M) throws JSONException {
		TreeMap<String, String> Message = new TreeMap<String, String>();
		
		Message.put( "Id", M.getString("id") );
		
		String TmpFormatedDate = Utility.formatDate( M.getString("date_time"), MeemiDroidApplication.getContext().getString(R.string.DateHourFormat), "yyyy-MM-dd hh:mm:ss" );
		Message.put( "Time", TmpFormatedDate);
								
		Message.put( "MeemerName", M.getString("screen_name") );
		Message.put( "MeemerAvatar", M.getString("avatar") );
		Message.put( "Content", M.getString("content") );
		Message.put( "NumOfComments", M.getString("qta_replies") );
		
		boolean IsPrivate = !( "0".equals( M.getString("private") ) ); 
		
		if (!IsPrivate) {
			Message.put( "IsFavorite", M.getString("is_preferite") );
		}
		
		if ( "image".equals( M.getString("meme_type") ) ) {
			Message.put( "Image", M.getString("image") );
			Message.put( "ImageThumbnail", M.getString("image_small") );
			//Message.put( "ExtraContent", MeemiDroidApplication.getContext().getString(R.string.ImageTag) );
		}
	
		if ( "video".equals( M.getString("meme_type") ) ) {
			String VideoSrc = Utility.getVideoSrc( M.getString("video") );
			Message.put( "Video", VideoSrc );
			//Message.put( "ExtraContent", MeemiDroidApplication.getContext().getString(R.string.VideoTag) );
		}
		
		if ( "link".equals( M.getString("meme_type") ) ) {
			Message.put( "Link", M.getString("link") );
			String linkContent = "[l:" + M.getString("link") + "|" + Message.get("Content") + "]";
			Message.put( "Content", linkContent);
		}
					
		return Message;
	}
	
	
	/**
	 * This private class represents an asynchronous command to be executed.
	 * This command represent a commnad to be executed in background but internally
	 * to the MeemiDroid Application, like clear the Avatar Cache.
	 * 
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 * @version 1.0
	 */
	private class AsyncCommand extends AsyncTask< Void, Void, Void > {
		/**
		 * The constructor: used to setup the asynchronous request.
		 * 
		 * @param C		the command to execute
		 * @param FF	true if the waiting dialog has to be showed during the process
		 * @param C		the Activity context
		 */
		public AsyncCommand(final int Cmd, final boolean FF, Context C) {
			this.Command = Cmd;
			this.ForegroundFlag = FF;
			this.CurrentContext = C;
		}
		
		/**
		 * This method performs a computation on a background thread.
		 * The specified parameters are the parameters passed to execute(Params...)  by the caller of this task.
		 * This method can call publishProgress(Progress...) to publish updates on the UI thread.
		 * 
		 * @param args0	the parameters of the task
		 * 
		 * @return	null
		 */
		@Override
		protected Void doInBackground(Void... arg0) {
			switch(Command) {
			case CLEAR_AVATAR_CACHE:
				ImageLoader.getInstance().clearCache();
				break;
			default:
				// NO_COMMAND: nothing to do
			}
			return null;
		}
		
		/**
		 * Runs on the UI thread before doInBackground(Params...).
		 */
		@Override
		protected void onPreExecute() {
			if (ForegroundFlag) {
				try {
					WaitingDialog = ProgressDialog.show(CurrentContext, "", CurrentContext.getString(R.string.AllertWaiting), true);
				} catch (Exception ex) {
					Log.d("MeemiEngine - Task", "Cannot open the Toast Dialog");
				}
			}
			
			super.onPreExecute();
		}
		
		/**
		 * Runs on the UI thread after doInBackground(Params...).
		 * The specified result is the value returned by doInBackground(Params...) or null
		 * if the task was canceled or an exception occurred.
		 */
		@Override
		protected void onPostExecute(Void V) {
			if (null != WaitingDialog) {
				WaitingDialog.dismiss();
			}
			
			super.onPostExecute(V);
		}
		
		public static final int NO_COMMAND = 0;
		public static final int CLEAR_AVATAR_CACHE = 1;
		
		private int Command = NO_COMMAND;
		private boolean ForegroundFlag = false;
		private Context CurrentContext	= null;
		
		private Dialog WaitingDialog	= null;
	}
	
		
	// list of Meemi API query.
	private static final String USER_EXISTS			= "p/exists";
	private static final String USER_PROFILE		= "%s/profile";
	private static final String POST_MESSAGE		= "p/save";
	private static final String REPLY_TO_MESSAGE	= "p/reply";
	private static final String BLOCK_USER			= "p/block/%s";
	private static final String UNBLOCK_USER		= "p/unblock/%s";
	private static final String FOLLOW_USER			= "p/follow/%s";
	private static final String UNFOLLOW_USER		= "p/unfollow/%s";
	private static final String FOLLWERS			= "%s/followers/page_%s/limit_30";
	private static final String FOLLWINGS			= "%s/followings/page_%s/limit_30";
	private static final String LIFESTREAM			= "p/%s/page_%s/limit_30";
	private static final String USERLIFESTREAM		= "%s/wf/page_%s/limit_30";
	private static final String USERFAVORITE		= "%s/favourites/page_%s/limit_30";
	private static final String REPLIES				= "%s/%s/replies/%s/20";
	private static final String GETSINLEMEEME		= "%s/%s";
	private static final String SEARCH				= "p/search/%s";
	private static final String POST_LOCATION		= "p/set-location";
	private static final String GET_NOTIFY_STATS	= "p/notify";
	private static final String NOTIFIES			= "p/only_new_%s";
	private static final String MARK_AS_READ		= "p/mark/%s";
	private static final String MARK_UNMARK_AS_FAV	= "p/fav/%s/%s";
	
	// lifestream API constant
	private static final String GENERAL_LS_API		= "meme-sfera";
	private static final String PRIVATE_LS_API		= "private";
	private static final String PRIVATE_SENT_LS_API	= "private_sent";
	
	// notifies API constant
	private static final String NTF_MEMES			= "memes";
	private static final String NTF_REPLIES			= "replies";
	private static final String NTF_PRIVATE_MEMES	= "memes_priv";
	private static final String NTF_PRIVATE_REPLIES	= "replies_priv";
	private static final String NTF_MENTIONS		= "mentions";
	private static final String NTF_FOLLOWERS		= "followers";
	
	
	private static final String[] NO_CMD_ARGS = new String[]{};
	
	private MeemiCredentials MyCredentials = null;
	private Timer SyncManager = new Timer();
	
	private LocationEngine MyLocationEngine = null;
	private String LastSentLocation = "";
}
