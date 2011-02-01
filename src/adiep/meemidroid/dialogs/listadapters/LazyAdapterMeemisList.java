package adiep.meemidroid.dialogs.listadapters;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import adiep.meemidroid.ImageLoader;
import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import adiep.meemidroid.R.id;
import adiep.meemidroid.Utility;
import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class implements an adapter to populate the Meemi friends lists. 
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.3
 */
public class LazyAdapterMeemisList extends BaseAdapter {
	
	/**
	 * This class represents a row of the list view containing the users list.
	 *  
	 * @author Andrea de Iacovo, and Eros Pedrini
	 * @version 0.6
	 */
	public static class ViewHolder {
		public ImageView Avatar = null;
		public TextView Nick = null;
		public TextView Time = null;
		public TextView Message = null;
		public TextView OtherInfo = null;		

		public boolean IsLoadOtherMeemi = false;
	}
	
	/**
	 * This constructor setups the list view adapters loading the users list to display.
	 * 
	 * @param A					the current Andorid activity
	 * @param MeemisData		the Meemis list to display.
	 */
	public LazyAdapterMeemisList(Activity A, List<TreeMap<String, String>> MeemisData) {
		this.MyActivity = A;
		
		this.MeemisData = MeemisData;
		
		LazyAdapterMeemisList.ViewInflater = (LayoutInflater) MyActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		MyImageLoader = new ImageLoader( A.getBaseContext(), MeemiDroidApplication.USERS_AVATARS_CACHE );
	}
	
	/**
	 * This constructor setups the list view adapters loading the users list to display.
	 * 
	 * @param A					the current Andorid activity
	 * @param UsersData			the Meemis list to display.
	 * @param ExtraRowStringID	the resource ID for the string to show in the extra "loading" row
	 */
	public LazyAdapterMeemisList(Activity A, List<TreeMap<String, String>> MeemisData, final int ExtraRowStringID) {
		this(A, MeemisData);
		
		ResIDString = ExtraRowStringID;
	}

	/**
	 * This constructor setups the list view adapters loading the users list to display.
	 * 
	 * @param A					the current Andorid activity
	 * @param UsersData			the Meemis list to display.
	 * @param ExtraRowStringID	the resource ID for the string to show in the extra "loading" row
	 * @param ExtraRowImageID	the resource ID for the icon to show in the extra  "loading" row
	 */
	public LazyAdapterMeemisList(Activity A, List<TreeMap<String, String>> UsersData, final int ExtraRowStringID, final int ExtraRowImageID) {
		this(A, UsersData, ExtraRowStringID);
		
		ResIDIcon = ExtraRowImageID;
		UseResIDIcon = true;
	}

	/**
	 * This method returns how many users have to be displayed.
	 * 
	 * @return how many users have to be displayed.
	 */
	public int getCount() {		
		if (null != MeemisData) {
			return MeemisData.size() + 1;
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
		return position;
	}

	
	/**
	 * Get the row id associated with the specified position in the list.
	 * 
	 * @param position	The position of the item within the adapter's data set whose row id we want
	 * 
	 * @return	The id of the item at the specified position
	 */
	public long getItemId(int position) {
		return position;
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
		View vi = convertView;
		ViewHolder holder;
		
		boolean IsExtraLine = position >= MeemisData.size();
		
		if (convertView == null) {
			vi = ViewInflater.inflate(R.layout.meemi_msgs_list_row, null);
			holder = new ViewHolder();
			holder.Avatar = (ImageView) vi.findViewById(R.id.UserAvatar);
			holder.Nick = (TextView) vi.findViewById(R.id.UserNick);
			holder.Time = (TextView) vi.findViewById(R.id.MeemeTime);
			holder.Message = (TextView) vi.findViewById(R.id.Message);
			holder.OtherInfo = (TextView) vi.findViewById(R.id.OtherInfo);
			vi.setTag(holder);
		} else {
			holder = (ViewHolder) vi.getTag();
		}
		
		if ( !holder.IsLoadOtherMeemi ) {
			if (IsExtraLine) {
				vi = ViewInflater.inflate(R.layout.list_row_other_loads, null);
				
				((TextView)vi.findViewById(id.RowLoadNewLabel)).setText(ResIDString);
				
				if (UseResIDIcon) {
					((ImageView)vi.findViewById(id.RowLoadNewIcon)).setImageResource(ResIDIcon);
				}
				
				holder = new ViewHolder();
				holder.IsLoadOtherMeemi = true;
				vi.setTag(holder);
			}
		} else {
			if (!IsExtraLine) {
				vi = ViewInflater.inflate(R.layout.meemi_msgs_list_row, null);
				holder = new ViewHolder();
				holder.Avatar = (ImageView) vi.findViewById(R.id.UserAvatar);
				holder.Nick = (TextView) vi.findViewById(R.id.UserNick);
				holder.Time = (TextView) vi.findViewById(R.id.MeemeTime);
				holder.Message = (TextView) vi.findViewById(R.id.Message);
				holder.OtherInfo = (TextView) vi.findViewById(R.id.OtherInfo);
				vi.setTag(holder);
			}
			
		}

		if (!IsExtraLine) {
			if (null != MeemisData) {
				Map<String, String> Item = MeemisData.get(position);
			
				holder.Avatar.setTag( (String)Item.get("MeemerAvatar") );
				holder.Nick.setText( (String)Item.get("MeemerName") );
				holder.Time.setText( (String)Item.get("Time") );
				
				String CleanText = Utility.fromMeemiToCleanText( (String)Item.get("Content") );
				holder.Message.setText( Html.fromHtml(CleanText) );
				
				holder.OtherInfo.setText( MeemiDroidApplication.getContext().getString(R.string.MsgComment) + (String)Item.get("NumOfComments") );
				MyImageLoader.DisplayImage( (String)Item.get("MeemerAvatar"), MyActivity, holder.Avatar );
			}
		}
		
		return vi;
	}
	
	
	private Activity MyActivity = null;
	
	private List<TreeMap<String, String>> MeemisData = null;
	
	private int ResIDString = R.string.ListItemExtraLoad;
	
	private boolean UseResIDIcon = false;
	private int ResIDIcon = 0;
	
	private ImageLoader MyImageLoader;
	
	private static LayoutInflater ViewInflater = null;
}
