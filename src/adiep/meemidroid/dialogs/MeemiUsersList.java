package adiep.meemidroid.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import adiep.meemidroid.dialogs.listadapters.LazyAdapterUsersList;
import adiep.meemidroid.dialogs.listadapters.LazyAdapterUsersList.ViewHolder;
import adiep.meemidroid.engine.LifestreamConst;
import adiep.meemidroid.engine.MeemiEngine;
import adiep.meemidroid.engine.MeemiEngine.MeemiEngineResult;
import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * This activity represents the list of users that follow or are followed
 * by a specific Meemi user. 
 * 
 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
 * @version 0.5
 */
public class MeemiUsersList extends ListActivity implements MeemiEngine.Callbackable {
	/**
	 * This method represents the callback point for every request made to
	 * the {@link MeemiEngine} instance.
	 * In this {@link Activity}, it manages the response to the following
	 * requests:
	 * - {@link MeemiEngine#getFollowers(String, int, android.content.Context, Callbackable)}
	 * - {@link MeemiEngine#getFollowings(String, int, android.content.Context, Callbackable)}
	 * 
	 * @param Result	the API call result
	 * 
	 * @see MeemiEngine#getFollowers(String, int, android.content.Context, Callbackable)
	 * @see MeemiEngine#getFollowings(String, int, android.content.Context, Callbackable)
	 */
	@Override
	public void onEngineExecuteResult(MeemiEngineResult Result) {
		List<TreeMap<String, String>> TmpUsersListToAdd = MeemiEngine.parseFriendsResult(Result);
		
		if ( null != TmpUsersListToAdd && 0 != TmpUsersListToAdd.size() ) {
			UsersList.addAll(TmpUsersListToAdd);
			
			Users.notifyDataSetChanged();
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
		
		MaxNumOfUsers = this.getIntent().getIntExtra(NUM_OF_USERS, 0);
		
		ListType = this.getIntent().getIntExtra(TYPE, FOLLOWERS);
		
		setupLayout();
		
		registerForContextMenu( this.getListView() );
	}
	
	/**
	 * This method defines the layout of the activity and links all the listeners
	 * used to interact with the user. 
	 */
	private void setupLayout() {		
		setContentView(R.layout.meemi_users_list);
		
		TextView Header = (TextView)findViewById(R.id.TextViewUSersListHeader);
		
		CurrentPage = 1;
		
		int ResLoadExtraString = R.string.UserListItemExtraLoad;
		int ResLoadExtraIcon = R.drawable.main_ui_followers;
		
		if (FOLLOWERS == ListType) {
			Header.setText( getString(R.string.UserListActivityFollowers) + " " + CurrentUser );
			
			MeemiDroidApplication.Engine.getFollowers(CurrentUser, CurrentPage, this, this);
		} else {
			Header.setText( getString(R.string.UserListActivityFollowing) + " " + CurrentUser );
			
			MeemiDroidApplication.Engine.getFollowings(CurrentUser, CurrentPage, this, this);
			
			ResLoadExtraIcon = R.drawable.main_ui_following;
		}
		
		Users = new LazyAdapterUsersList(this, UsersList, MaxNumOfUsers, ResLoadExtraString, ResLoadExtraIcon);
		
		setListAdapter(Users);
		
		getListView().setOnItemClickListener( new ItemClickListener() );
		
		// Fastscroll
		getListView().setFastScrollEnabled( MeemiDroidApplication.Prefs.isFastScrollEnabled() );
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
		
		if (!TmpViewHolder.IsLoadOtherUsers) {
			menu.add(ContextMenu.NONE, CM_SHOWUSERINFO, ContextMenu.NONE, R.string.UserListContextMenuShow);
			menu.add(ContextMenu.NONE, CM_SENDPRIVATEMSG, ContextMenu.NONE, R.string.UserListContextMenuSend);
			menu.add(ContextMenu.NONE, CM_SHOWUSERMESSAGE, ContextMenu.NONE, R.string.UserListContextMenuShowMsg);
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
		
		String UserNick = (String) UsersList.get(ItemId).get("UserId");
		
		switch(item.getItemId()) {
		case CM_SHOWUSERINFO:
			Intent UserInfo = new Intent(MeemiUsersList.this, UserScreen.class);
			UserInfo.putExtra( UserScreen.USER, UserNick );
        	
        	startActivityForResult(UserInfo, ACTIVITY_USER);
        	
			return true;
		case CM_SENDPRIVATEMSG:
			Intent PrivateMessageIntent = new Intent(MeemiUsersList.this, MeemiSendScreen.class);
        	
        	PrivateMessageIntent.putExtra( MeemiSendScreen.PRIVATEUSER, UserNick );
        	
        	startActivityForResult(PrivateMessageIntent, ACTIVITY_MEEMI);
			return true;
		case CM_SHOWUSERMESSAGE:
			Intent ShowUserMessages = new Intent(MeemiUsersList.this, MeemiLifestream.class);
			
			ShowUserMessages.putExtra(MeemiLifestream.USER, UserNick);
			ShowUserMessages.putExtra(MeemiLifestream.TYPE, LifestreamConst.PERSONAL_LS);
			
			startActivityForResult(ShowUserMessages, ACTIVITY_MESSAGES);
			return true;
		default:
			// nothing to do
		}

		// if nothing handled, let parent deal with it
		return super.onContextItemSelected(item);
	}
	
	/**
	 * This private class manages the interaction list item selection.
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	private class ItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			if ( position < UsersList.size() ) {
				String UserNick = (String) UsersList.get(position).get("UserId");
			
				Intent UserInfo = new Intent(MeemiUsersList.this, UserScreen.class);
				UserInfo.putExtra( UserScreen.USER, UserNick );
			
				startActivityForResult(UserInfo, ACTIVITY_USER);
			} else {
				++CurrentPage;
				
				if (FOLLOWERS == ListType) {
					MeemiDroidApplication.Engine.getFollowers(CurrentUser, CurrentPage, MeemiUsersList.this, MeemiUsersList.this);
				} else {
					MeemiDroidApplication.Engine.getFollowings(CurrentUser, CurrentPage, MeemiUsersList.this, MeemiUsersList.this);
				}
			}
		}
		
	}
		
	/**
	 * The key to be used with {@link Intent#putExtra(String, String)}
	 */
	public static final String USER = "UserNick";
	public static final String NUM_OF_USERS = "NumberOfUsers";
	public static final String TYPE = "ListType";
	
	private static final int CM_SHOWUSERINFO = ContextMenu.FIRST;
	private static final int CM_SENDPRIVATEMSG = ContextMenu.FIRST + 1;
	private static final int CM_SHOWUSERMESSAGE = ContextMenu.FIRST + 2;
	
	private static final int ACTIVITY_USER = 0;
	private static final int ACTIVITY_MEEMI = 1;
	private static final int ACTIVITY_MESSAGES = 2;
	
	public final static int FOLLOWERS = 0;
	public final static int FOLLOWING = 1;
	
	private String CurrentUser = null;
	private int ListType = FOLLOWERS;
	//private boolean isUserOwner = false;
	
	private List<TreeMap<String, String>> UsersList = new ArrayList<TreeMap<String, String>>();
	private int MaxNumOfUsers = 0; 
	
	private BaseAdapter Users = null;
	private int CurrentPage = 1;
	
}
