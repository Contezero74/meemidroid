package adiep.meemidroid.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import adiep.meemidroid.dialogs.listadapters.LazyAdapterRepliesList;
import adiep.meemidroid.dialogs.listadapters.LazyAdapterRepliesList.ViewHolder;
import adiep.meemidroid.engine.MeemiEngine;
import adiep.meemidroid.engine.MeemiEngine.Callbackable;
import adiep.meemidroid.engine.MeemiEngine.MeemiEngineResult;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;

/**
 * This activity represents the list of  meemi representing the replies to a specific meemi. 
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.5
 */
public class MeemiRepliesList extends ListActivity implements MeemiEngine.Callbackable {
	/**
	 * This method represents the callback point for every request made to
	 * the {@link MeemiEngine} instance.
	 * In this {@link Activity}, it manages the response to the following
	 * requests:
	 * - {@link MeemiEngine#getSingleMeeme(String, String, android.content.Context, Callbackable)}
	 * - {@link MeemiEngine#getReplies(String, String, int, android.content.Context, Callbackable)}
	 * 
	 * @param Result	the API call result
	 * 
	 * @see MeemiEngine#getSingleMeeme(String, String, android.content.Context, Callbackable)
	 * @see MeemiEngine#getReplies(String, String, int, android.content.Context, Callbackable)
	 */
	@Override
	public void onEngineExecuteResult(MeemiEngineResult Result) {
		switch (Result.CallbackMethod) {
		case MeemiEngine.CB_SINGLE_MEEME:
			OriginalMeeme = MeemiEngine.parseSingleMeemeResult(Result);
			
			// here we finalize the initialization of this Activity loading all the replies:
			RepliesList.clear();
			RepliesList.add(OriginalMeeme);
			
			MeemiDroidApplication.Engine.getReplies(Meemer, MeemeID, 1, this, this);			
			break;
			
		case MeemiEngine.CB_REPLYSTREAM:
			List<TreeMap<String, String>> TmpRepliesList = MeemiEngine.parseMeemiStreamResult(Result);
			
			if (null != TmpRepliesList) {
				RepliesList.addAll(TmpRepliesList);
			}
			
			if (null == Replies) {
				Replies = new LazyAdapterRepliesList( this, RepliesList, Integer.parseInt( (String)OriginalMeeme.get("NumOfComments") ) );
				
				setListAdapter(Replies);
			}
			
			Replies.notifyDataSetChanged();
			break;
			
		default:
			Log.w("MeemiRepliesList - onEngineExecuteResult", "This callback (" + Result.CallbackMethod + ") is not managed yet");
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
		
		Meemer = this.getIntent().getStringExtra(REPLYUSER);
		MeemeID = this.getIntent().getStringExtra(REPLYMEEMEID);
		
		setupLayout();
		
		registerForContextMenu( this.getListView() );
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
        inflater.inflate(R.menu.meemislistmenu, menu);
        
        return true;
    }
	
	/**
	 * This method defines the layout of the activity and links all the listeners
	 * used to interact with the user. 
	 */
	private void setupLayout() {		
		setContentView(R.layout.meemi_reply_list);
		
		//int ResLoadExtraString = R.string.UserListItemExtraLoad;
		//int ResLoadExtraIcon = R.drawable.main_ui_followers;
		
		// first of all we retrieve the original meemi ...
		MeemiDroidApplication.Engine.getSingleMeeme(Meemer, MeemeID, this, this);
		
		// ... and continue the rest of the setup into the callback (onEngineExecuteResult)
		
		getListView().setOnItemClickListener( new ItemClickListener() );
		
		((Button)findViewById(R.id.ReplyBtn)).setOnClickListener( new ReplyClickListener() );
	}
	
	/**
	 * This method setups the context menu (long press) for select what you can do with a contact
	 * in the users list.
	 * 
	 * @param menu		The menu that we want create
	 * @param v			The current view
	 * @param menuInfo	The metadata information about the menu that we want create
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterContextMenuInfo TmpMenuInfo = (AdapterContextMenuInfo)menuInfo;
		ViewHolder TmpViewHolder = (ViewHolder)TmpMenuInfo.targetView.getTag();
		
		if (!TmpViewHolder.IsLoadOtherReplies) {
			menu.add(ContextMenu.NONE, CM_SHOWUSERINFO, ContextMenu.NONE, R.string.MeemiListContextMenuShowUser);
			
			if (!TmpViewHolder.IsOriginalMeemi) {
				menu.add(ContextMenu.NONE, CM_SHOWCOMMENTS, ContextMenu.NONE, R.string.MeemiListContextMenuShowComments);
				menu.add(ContextMenu.NONE, CM_REPLAY, ContextMenu.NONE, R.string.MeemiListContextMenuReply);
			}
		}
	}
	
	/**
	 * This method manages the user selection on the context menu.
	 * 
	 * @param item	The selected item of the context menu by the user
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int ItemId = ((AdapterContextMenuInfo)item.getMenuInfo()).position;
		
		String MeemiId = (String) RepliesList.get(ItemId).get("Id");
		String UserNick = (String) RepliesList.get(ItemId).get("MeemerName");
		
		switch(item.getItemId()) {
		case CM_SHOWUSERINFO:
			Intent UserInfo = new Intent(MeemiRepliesList.this, UserScreen.class);
			UserInfo.putExtra( UserScreen.USER, UserNick );
        	
        	startActivityForResult(UserInfo, ACTIVITY_USER);
        	
			return true;
		case CM_SHOWCOMMENTS:
			Intent ShowRepliesIntent = new Intent(MeemiRepliesList.this, MeemiRepliesList.class);
        	
			ShowRepliesIntent.putExtra( MeemiRepliesList.REPLYUSER, UserNick );
			ShowRepliesIntent.putExtra( MeemiRepliesList.REPLYMEEMEID, MeemiId );
        	
        	startActivityForResult(ShowRepliesIntent, ACTIVITY_REPLY);
        	
        	return true;
		case CM_REPLAY:
			Intent MeemiReply = new Intent(MeemiRepliesList.this, MeemiSendScreen.class);
			MeemiReply.putExtra( MeemiSendScreen.REPLYMEEMIID, MeemiId );
			MeemiReply.putExtra( MeemiSendScreen.REPLYMEEMER, UserNick );
			
        	startActivityForResult(MeemiReply, ACTIVITY_MEEMI);
        	
        	return true;
		}

		// if nothing handled, let parent deal with it
		return super.onContextItemSelected(item);
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
	    	case R.id.itemRefresh:
	    		refreshRepliesList();
	        	break;
		  default:
			// nothing to do	  
	    }  
	    
	    return isConsumed;  
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
			case ACTIVITY_REPLY:		        
				refreshRepliesList();
		        
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	/**
	 * This method refresh the current replies list reloading the first 20th meemis.
	 */
	private void refreshRepliesList() {
		RepliesList.clear();
		RepliesList.add(OriginalMeeme);
		
		/* I don't like this solution to move to the top of the
		 * ListView... but is the only working method I found 
		 */
		MeemiRepliesList.this.getListView().post(
			new Thread() {
				public void run() {
					MeemiRepliesList.this.getListView().setSelection(0);
				}
			}
		);
		
		MeemiDroidApplication.Engine.getReplies(Meemer, MeemeID, 1, this, this);
	}
    
	
	/**
	 * This private class manages the interaction list item selection.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class ItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			if ( position < RepliesList.size() && 0 != position) {
				String MeemiId = (String) RepliesList.get(position).get("Id");
				String UserNick = (String) RepliesList.get(position).get("MeemerName");
			
				Intent ShowRepliesIntent = new Intent(MeemiRepliesList.this, MeemiRepliesList.class);
	        	
				ShowRepliesIntent.putExtra( MeemiRepliesList.REPLYUSER, UserNick );
				ShowRepliesIntent.putExtra( MeemiRepliesList.REPLYMEEMEID, MeemiId );
	        	
	        	startActivityForResult(ShowRepliesIntent, ACTIVITY_REPLY);
			} else if (0 != position) {
				int StartingFrom = RepliesList.size();
				
				MeemiDroidApplication.Engine.getReplies(Meemer, MeemeID, StartingFrom, MeemiRepliesList.this, MeemiRepliesList.this);
			}
		}
		
	}
	
	/**
	 * This private class manages reply button.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class ReplyClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			Intent MeemiReply = new Intent(MeemiRepliesList.this, MeemiSendScreen.class);
			MeemiReply.putExtra( MeemiSendScreen.REPLYMEEMIID, MeemeID );
			MeemiReply.putExtra( MeemiSendScreen.REPLYMEEMER, Meemer );
			
        	startActivityForResult(MeemiReply, ACTIVITY_MEEMI);
		}
	}	
	
	
	/**
	 * The key to be used with {@link Intent#putExtra(String, String)}
	 */
	public static final String REPLYUSER = "ReplyNick";
	public static final String REPLYMEEMEID = "ReplyMeemeId";
	
	private static final int CM_SHOWUSERINFO = ContextMenu.FIRST;
	private static final int CM_SHOWCOMMENTS = ContextMenu.FIRST + 1;
	private static final int CM_REPLAY = ContextMenu.FIRST + 2;
	
	private static final int ACTIVITY_USER = 0;
	private static final int ACTIVITY_MEEMI = 1;
	private static final int ACTIVITY_REPLY = 2;
	
	private String Meemer = null;
	private String MeemeID = null;
	private TreeMap<String,String> OriginalMeeme = null;
	//private boolean isUserOwner = false;
	
	private List<TreeMap<String, String>> RepliesList = new ArrayList<TreeMap<String, String>>();
	
	private BaseAdapter Replies = null;	
}
