package adiep.meemidroid.dialogs.listadapters;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import adiep.meemidroid.R.id;
import adiep.meemidroid.Utility;
import adiep.meemidroid.dialogs.UserScreen;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class implements an adapter to populate the Meemi replies list. 
 * 
 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
 * @version 1.0
 */
public class LazyAdapterRepliesList extends LazyAdapterList {
	
	/**
	 * This class represents a row of the list view containing Meemi replies list.
	 *  
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 * @version 1.0
	 */
	public static class ViewHolder extends LazyAdapterList.ViewHolder {
		public TextView Nick = null;
		public TextView Time = null;
		public WebView Message = null;
		//public TextView Message = null;
		public TextView OtherInfo = null;		
		public ImageView IsFavorite = null;
		public ImageView IsPhoto = null;
		public ImageView IsVideo = null;
		public ImageView IsLink = null;
		public ImageView IsPrivate = null;

		public boolean IsLoadOtherReplies = false;
		public boolean IsOriginalMeemi = false;
	}
	
	/**
	 * This constructor setups the list view adapters loading the replies list to display.
	 * 
	 * @param A					the current Andorid activity
	 * @param MeemisData		the Meemis list to display.
	 */
	public LazyAdapterRepliesList(Activity A, List<TreeMap<String, String>> MeemisData, final int MaxNumberOfReplies) {
		super(A);
		
		this.MeemisData = MeemisData;
		this.MaxNumberOfReplies = MaxNumberOfReplies;
		
		JSAccountClick = new AccountClick(A);
	}
	
	/**
	 * This constructor setups the list view adapters loading the replies list to display.
	 * 
	 * @param A					the current Andorid activity
	 * @param MeemisData		the Meemis list to display.
	 * @param ExtraRowStringID	the resource ID for the string to show in the extra "loading" row
	 */
	public LazyAdapterRepliesList(Activity A, List<TreeMap<String, String>> MeemisData, final int MaxNumberOfReplies, final int ExtraRowStringID) {
		super(A, ExtraRowStringID);
		
		this.MeemisData = MeemisData;
		this.MaxNumberOfReplies = MaxNumberOfReplies;
		
		JSAccountClick = new AccountClick(A);
	}

	/**
	 * This constructor setups the list view adapters loading the replies list to display.
	 * 
	 * @param A					the current Andorid activity
	 * @param UsersData			the Meemis list to display.
	 * @param ExtraRowStringID	the resource ID for the string to show in the extra "loading" row
	 * @param ExtraRowImageID	the resource ID for the icon to show in the extra  "loading" row
	 */
	public LazyAdapterRepliesList(Activity A, List<TreeMap<String, String>> MeemisData, final int MaxNumberOfReplies, final int ExtraRowStringID, final int ExtraRowImageID) {
		super(A, ExtraRowStringID, ExtraRowImageID);
		
		this.MeemisData = MeemisData;
		this.MaxNumberOfReplies = MaxNumberOfReplies;
		
		JSAccountClick = new AccountClick(A);
	}

	/**
	 * This method returns how many users have to be displayed.
	 * 
	 * @return how many users have to be displayed.
	 */
	public int getCount() {	
		if (null != MeemisData) {
			// the extra row is needed in oreder to load more replies
			boolean NeedExtraRow = MeemisData.size() < MaxNumberOfReplies;
		
			return MeemisData.size() + (NeedExtraRow ? 1 : 0);
		}
		
		return 0;
	}

	/**
	 * This method is a dummy method used to mimic the BaseAdapter getItem method:
	 * to get the data item associated with the specified position in the data set.
	 * 
	 * @param position	The position of the item whose data we want within the adapter's data set
	 * 
	 * @return The data at the specified position
	 */
	public Object getItem(int position) {
		if (MeemisData.size() < position) {
			return MeemisData.get(position);
		} else {
			return null;
		}
	}	

	/**
	 * Get a View that displays the data at the specified position in the data set.
	 * 
	 * @param position		The position of the item within the adapter's data set of the item whose view we want
	 * @param convertView	The old view to reuse, if possible.
	 * @param parent		The parent that this view will eventually be attached to
	 * 
	 * @return	A View corresponding to the data at the specified position
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		// FIXME: this code is really poor: please fix me :)
		// FIXME: (the revenge) to avoid problem with WebView I need to avoid
		//        its caching :( the code (how wrote today can use too much memory.
		//        Need extra code cleanup.
		View vi = convertView;
		ViewHolder holder;
		
		boolean IsExtraLine = position >= MeemisData.size();
		
		//if (convertView == null) {
			vi = ViewInflater.inflate(R.layout.meemi_reply_list_row_2, null);
			holder = new ViewHolder();
			holder.Image = (ImageView) vi.findViewById(R.id.UserAvatar);
			holder.Nick = (TextView) vi.findViewById(R.id.UserNick);
			holder.Time = (TextView) vi.findViewById(R.id.MeemeTime);
			holder.Message = (WebView) vi.findViewById(R.id.Message);
			//holder.Message = (TextView) vi.findViewById(R.id.Message);
			holder.OtherInfo = (TextView) vi.findViewById(R.id.OtherInfo);
			holder.IsFavorite = (ImageView) vi.findViewById(R.id.FavImage);
			holder.IsPhoto = (ImageView) vi.findViewById(R.id.PhotoImage);
			holder.IsVideo = (ImageView) vi.findViewById(R.id.VideoImage);
			holder.IsLink = (ImageView) vi.findViewById(R.id.Linklmage);
			holder.IsPrivate = (ImageView) vi.findViewById(R.id.LockImage);
			vi.setTag(holder);
		//} else {
		//	holder = (ViewHolder) vi.getTag();
		//}
		
		if ( !holder.IsLoadOtherReplies ) {
			if (IsExtraLine) {
				vi = ViewInflater.inflate(R.layout.list_row_other_loads, null);
				
				((TextView)vi.findViewById(id.RowLoadNewLabel)).setText(ResIDString);
				
				if (UseResIDIcon) {
					((ImageView)vi.findViewById(id.RowLoadNewIcon)).setImageResource(ResIDIcon);
				}
				
				holder = new ViewHolder();
				holder.IsLoadOtherReplies = true;
				vi.setTag(holder);
			}
		}
		/*else {
			if (!IsExtraLine) {
				vi = ViewInflater.inflate(R.layout.meemi_reply_list_row_2, null);
				holder = new ViewHolder();
				holder.Avatar = (ImageView) vi.findViewById(R.id.UserAvatar);
				holder.Nick = (TextView) vi.findViewById(R.id.UserNick);
				holder.Time = (TextView) vi.findViewById(R.id.MeemeTime);
				holder.Message = (WebView) vi.findViewById(R.id.Message);
				//holder.Message = (TextView) vi.findViewById(R.id.Message);
				holder.OtherInfo = (TextView) vi.findViewById(R.id.OtherInfo);
				vi.setTag(holder);
			}	
		}
		*/

		if (!IsExtraLine) {
			if (null != MeemisData) {
				Map<String, String> Item = MeemisData.get(position);
			
				holder.Image.setTag( (String)Item.get("MeemerAvatar") );
				holder.Nick.setText( (String)Item.get("MeemerName") );
				holder.Time.setText( (String)Item.get("Time") );
				
				String HTMLContent = "<div>" + Utility.fromMeemiToHTML( (String)Item.get("Content") ) + "</div>";
				
				holder.IsPhoto.setVisibility(View.GONE);
				holder.IsVideo.setVisibility(View.GONE);
				holder.IsLink.setVisibility(View.GONE);
				String Header = null;
				if ( Item.containsKey("Image") ) {
					holder.IsPhoto.setVisibility(View.VISIBLE);
					
					Header = "<div class=\"media\">" +
					 		 "<div>" +
							 "<a href=\"" + (String)Item.get("Image") + "\">" +
							 "<img src=\"" + (String)Item.get("ImageThumbnail") + "\" />" +
							 "</a>" +
							 "</div>" +
							 "</div>";
				} else if ( Item.containsKey("Video") ) {
					holder.IsVideo.setVisibility(View.VISIBLE);
					
					Header = "<div class=\"media\">" +
					 "<div>" +
					 "<a href=\"" + (String)Item.get("Video") + "\">" +
					 "<img src=\"file:///android_res/drawable/meemi_video_play.png\" />" +
					 "</a>" +
					 "</div>" +
					 "</div>";
				} else if ( Item.containsKey("Link") ) {
					holder.IsLink.setVisibility(View.VISIBLE);
				}
				
				if (null != Header) {
					HTMLContent = Header + "<div>" + HTMLContent + "</div>";
				}
				
				//MeemiDroidApplication.getContext().deleteDatabase("webview.db");
				//MeemiDroidApplication.getContext().deleteDatabase("webviewCache.db");
				
				//holder.Message.clearCache(false);
				//holder.Message.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
				//holder.Message.loadData(HTMLContent, "text/html", "utf-8");
				holder.Message.loadDataWithBaseURL("", Utility.wrapForHTML(HTMLContent, CSS), "text/html", "utf-8", "");
				holder.Message.addJavascriptInterface(JSAccountClick, "account");
				holder.Message.getSettings().setJavaScriptEnabled(true);
				
				//String HTML = Utility.fromMeemiToHTML( (String)Item.get("Content") );
				//holder.Message.setText( (String)Item.get("Content") );
				
				holder.IsFavorite.setVisibility(View.GONE);
				if ( "1".equals( (String)Item.get("IsFavorite") ) ) {
					holder.IsFavorite.setVisibility(View.VISIBLE);
				}			
			
				holder.OtherInfo.setText( MeemiDroidApplication.getContext().getString(R.string.MsgComment) + (String)Item.get("NumOfComments") );
				MyImageLoader.DisplayImage( (String)Item.get("MeemerAvatar"), MyActivity, holder.Image );
			}
		}
		
		holder.IsOriginalMeemi = false;
		if (0 == position) {			
			holder.IsOriginalMeemi = true;
		}
		
		return vi;
	}
	
	/**
	 * This private class is used to manage the interaction with the {@link WebView} when
	 * the user select a ScreenName (e.g., @capobecchino)
	 * 
	 * @author Andrea de Iacovo, and Eros Pedrini
	 */
	final class AccountClick {
		public AccountClick(Activity A) {
			super();
			
			BaseActivity = A;
		}
		
        public void clickOnAccount(String Name) {       	
        	Intent UserInfo = new Intent(BaseActivity, UserScreen.class);
			UserInfo.putExtra( UserScreen.USER, Name );
        	
        	BaseActivity.startActivityForResult(UserInfo, ACTIVITY_USER);

        }
        
        private Activity BaseActivity = null;
    }
	
	
	private int MaxNumberOfReplies = 0;
	private List<TreeMap<String, String>> MeemisData = null;
	
	
	private static AccountClick JSAccountClick = null;
	
	
	private static final int ACTIVITY_USER = 0;
	
	private static final String CSS =
		"body { background-color: black; color: white; } " +
		"#userimage { float: left; } " +
		".avatar { max-width: 120px; max-height: 120px; } " +
		"#userinfo { text-align: justify; margin-left: 10px; } " +
		"#userprofile { clear:both; margin-top: 5px; } " +
		"#userinfo h1, #userinfo h2 { font-size: 13pt; } " +
		"a.link:link, a.link:visited, a.link:hover, a.link:active { color:aqua; text-decoration:underline; } " +
		"a.meemiaccount:link, a.meemiaccount:visited, a.meemiaccount:hover, a.meemiaccount:active { color:dodgerblue; text-decoration:underline; } " + 
		".media { width: 100%; text-align: center; }" +
		".media img { border: 0 none; }";
}
