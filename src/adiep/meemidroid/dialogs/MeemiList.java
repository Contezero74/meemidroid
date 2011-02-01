package adiep.meemidroid.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import adiep.meemidroid.dialogs.listadapters.LazyAdapterMeemisList;
import adiep.meemidroid.dialogs.listadapters.LazyAdapterMeemisList.ViewHolder;
import adiep.meemidroid.engine.MeemiEngine;
import adiep.meemidroid.engine.MeemiEngine.Callbackable;
import adiep.meemidroid.engine.MeemiEngine.MeemiEngineResult;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

/**
 * This activity represents the list of meemi of a specific lifestream. 
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.1
 */
public class MeemiList extends ListActivity implements MeemiEngine.Callbackable {
	/**
	 * This method represents the callback point for every request made to
	 * the {@link MeemiEngine} instance.
	 * In this {@link Activity}, it manages the response to the following
	 * requests:
	 * - {@link MeemiEngine#getLifeStream(String, String, int, android.content.Context, Callbackable)}
	 * 
	 * @param Result	the API call result
	 * 
	 * @see MeemiEngine#getLifeStream(String, String, int, android.content.Context, Callbackable)
	 */
	@Override
	public void onEngineExecuteResult(MeemiEngineResult Result) {
		List<TreeMap<String, String>> TmpMeemisListToAdd = MeemiEngine.parseMeemiStreamResult(Result);
		
		if (null != TmpMeemisListToAdd) {
			MeemisList.addAll(TmpMeemisListToAdd);
			
			Meemis.notifyDataSetChanged();
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
		
		CurrentUser = this.getIntent().getStringExtra(USER);
		ListType = this.getIntent().getIntExtra(TYPE, GENEREAL_LIFESTREAM);
		
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
		setContentView(R.layout.meemi_msgs_list);
		
		CurrentPage = 1;
		
		//int ResLoadExtraString = R.string.UserListItemExtraLoad;
		//int ResLoadExtraIcon = R.drawable.main_ui_followers;
		
		if (GENEREAL_LIFESTREAM == ListType) {			
			MeemiDroidApplication.Engine.getLifeStream(CurrentUser, MeemiEngine.GENERAL_LS, CurrentPage, this, this);
		} else if (PERSONAL_LIFESTREAM == ListType) {
			MeemiDroidApplication.Engine.getLifeStream(CurrentUser, MeemiEngine.PERSONAL_LS, CurrentPage, this, this);
		} // else ...
		
		Meemis = new LazyAdapterMeemisList(this, MeemisList);
		
		setListAdapter(Meemis);
		
		getListView().setOnItemClickListener( new ItemClickListener() );
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
		
		if (!TmpViewHolder.IsLoadOtherMeemi) {
			menu.add(ContextMenu.NONE, CM_SHOWUSERINFO, ContextMenu.NONE, R.string.MeemiListContextMenuShowUser);
			menu.add(ContextMenu.NONE, CM_SHOWCOMMENTS, ContextMenu.NONE, R.string.MeemiListContextMenuShowComments);
			menu.add(ContextMenu.NONE, CM_REPLAY, ContextMenu.NONE, R.string.MeemiListContextMenuReply);
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
		
		String MeemiId = (String) MeemisList.get(ItemId).get("Id");
		String UserNick = (String) MeemisList.get(ItemId).get("MeemerName");
		
		switch(item.getItemId()) {
		case CM_SHOWUSERINFO:
			Intent UserInfo = new Intent(MeemiList.this, UserScreen.class);
			UserInfo.putExtra( UserScreen.USER, UserNick );
        	
        	startActivityForResult(UserInfo, ACTIVITY_USER);
        	
			return true;
		case CM_SHOWCOMMENTS:
			Intent ShowReplyIntent = new Intent(MeemiList.this, MeemiRepliesList.class);
        	
			ShowReplyIntent.putExtra( MeemiRepliesList.REPLYUSER, UserNick );
			ShowReplyIntent.putExtra( MeemiRepliesList.REPLYMEEMEID, MeemiId );
        	
        	startActivityForResult(ShowReplyIntent, ACTIVITY_REPLY);
        	
			return true;
		case CM_REPLAY:
			Intent MeemiReply = new Intent(MeemiList.this, MeemiSendScreen.class);
			MeemiReply.putExtra( MeemiSendScreen.REPLYMEEMIID, MeemiId );
			MeemiReply.putExtra( MeemiSendScreen.REPLYMEEMER, UserNick );
			
        	startActivityForResult(MeemiReply, ACTIVITY_MEEMI);
        	
        	return true;
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
			if ( position < MeemisList.size() ) {
				String MeemiId = (String) MeemisList.get(position).get("Id");
				String UserNick = (String) MeemisList.get(position).get("MeemerName");
			
				Intent ShowRepliesIntent = new Intent(MeemiList.this, MeemiRepliesList.class);
	        	
				ShowRepliesIntent.putExtra( MeemiRepliesList.REPLYUSER, UserNick );
				ShowRepliesIntent.putExtra( MeemiRepliesList.REPLYMEEMEID, MeemiId );
	        	
	        	startActivityForResult(ShowRepliesIntent, ACTIVITY_REPLY);
			} else {
				++CurrentPage;
				
				if (GENEREAL_LIFESTREAM == ListType) {			
					MeemiDroidApplication.Engine.getLifeStream(CurrentUser, MeemiEngine.GENERAL_LS, CurrentPage, MeemiList.this, MeemiList.this);
				} else if (PERSONAL_LIFESTREAM == ListType) {
					MeemiDroidApplication.Engine.getLifeStream(CurrentUser, MeemiEngine.PERSONAL_LS, CurrentPage, MeemiList.this, MeemiList.this);
				} // else ...
			}
		}
		
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
	    		MeemisList.clear();
	    		
	    		/* I don't like this solution to move to the top of the
				 * ListView... but is the only working method I found 
				 */
				MeemiList.this.getListView().post(
					new Thread() {
						public void run() {
							MeemiList.this.getListView().setSelection(0);
						}
					}
				);
	    		
	    		CurrentPage = 1;
	    		
				if (GENEREAL_LIFESTREAM == ListType) {			
					MeemiDroidApplication.Engine.getLifeStream(CurrentUser, MeemiEngine.GENERAL_LS, CurrentPage, MeemiList.this, MeemiList.this);
				} else if (PERSONAL_LIFESTREAM == ListType) {
					MeemiDroidApplication.Engine.getLifeStream(CurrentUser, MeemiEngine.PERSONAL_LS, CurrentPage, MeemiList.this, MeemiList.this);
				} // else ...	    		
	        	
	        	break;
		  default:
			// nothing to do	  
	    }  
	    
	    return isConsumed;  
	}
	
	
	
	/**
	 * The key to be used with {@link Intent#putExtra(String, String)}
	 */
	public static final String USER = "UserNick";
	public static final String TYPE = "ListType";
	
	private static final int CM_SHOWUSERINFO = ContextMenu.FIRST;
	private static final int CM_SHOWCOMMENTS = ContextMenu.FIRST + 1;
	private static final int CM_REPLAY = ContextMenu.FIRST + 2;
	
	private static final int ACTIVITY_USER = 0;
	private static final int ACTIVITY_MEEMI = 1;
	private static final int ACTIVITY_REPLY = 2;
	
	public final static int GENEREAL_LIFESTREAM = 0;
	public final static int PERSONAL_LIFESTREAM = 1;
	
	private String CurrentUser = null;
	private int ListType = GENEREAL_LIFESTREAM;
	//private boolean isUserOwner = false;
	
	private List<TreeMap<String, String>> MeemisList = new ArrayList<TreeMap<String, String>>();
	
	private BaseAdapter Meemis = null;
	private int CurrentPage = 1;
	
}
