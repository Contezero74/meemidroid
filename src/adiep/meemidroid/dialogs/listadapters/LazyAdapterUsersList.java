package adiep.meemidroid.dialogs.listadapters;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import adiep.meemidroid.R;
import adiep.meemidroid.R.id;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class implements an adapter to populate the Meemi friends lists. 
 * 
 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
 * @version 1.0
 */
public class LazyAdapterUsersList extends LazyAdapterList {
	
	/**
	 * This class represents a row of the list view containing the users list.
	 *  
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 * @version 1.0
	 */
	public static class ViewHolder extends LazyAdapterList.ViewHolder {
		public TextView Text = null;

		public boolean IsLoadOtherUsers = false;
	}
	
	/**
	 * This constructor setups the list view adapters loading the users list to display.
	 * 
	 * @param A					the current Andorid activity
	 * @param UsersData			the Users list to display.
	 * @param MaxNumberOfUsers	the max number of followers/following stored in current user profile
	 */
	public LazyAdapterUsersList(Activity A, List<TreeMap<String, String>> UsersData, final int MaxNumberOfUsers) {
		super(A);
		
		this.UsersData = UsersData;
		this.MaxNumberOfUsers = MaxNumberOfUsers;
	}
	
	/**
	 * This constructor setups the list view adapters loading the users list to display.
	 * 
	 * @param A					the current Andorid activity
	 * @param UsersData			the Users list to display.
	 * @param MaxNumberOfUsers	the max number of followers/following stored in current user profile
	 * @param ExtraRowStringID	the resource ID for the string to show in the extra "loading" row
	 */
	public LazyAdapterUsersList(Activity A, List<TreeMap<String, String>> UsersData, final int MaxNumberOfUsers, final int ExtraRowStringID) {
		super(A, ExtraRowStringID);

		this.UsersData = UsersData;
		this.MaxNumberOfUsers = MaxNumberOfUsers;
	}

	/**
	 * This constructor setups the list view adapters loading the users list to display.
	 * 
	 * @param A					the current Andorid activity
	 * @param UsersData			the Users list to display.
	 * @param MaxNumberOfUsers	the max number of followers/following stored in current user profile
	 * @param ExtraRowStringID	the resource ID for the string to show in the extra "loading" row
	 * @param ExtraRowImageID	the resource ID for the icon to show in the extra  "loading" row
	 */
	public LazyAdapterUsersList(Activity A, List<TreeMap<String, String>> UsersData, final int MaxNumberOfUsers, final int ExtraRowStringID, final int ExtraRowImageID) {
		super(A, ExtraRowStringID, ExtraRowImageID);

		this.UsersData = UsersData;
		this.MaxNumberOfUsers = MaxNumberOfUsers;
	}

	/**
	 * This method returns how many users have to be displayed.
	 * 
	 * @return how many users have to be displayed.
	 */
	public int getCount() {
		if (null != UsersData) {
			// the extra row is needed in oreder to load more users
			boolean NeedExtraRow = UsersData.size() < MaxNumberOfUsers;
		
			return UsersData.size() + (NeedExtraRow ? 1 : 0);
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
		if (UsersData.size() < position) {
			return UsersData.get(position);
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
		View vi = convertView;
		ViewHolder Holder;
		
		boolean IsExtraLine = position >= UsersData.size();
		
		if (convertView == null) {
			vi = ViewInflater.inflate(R.layout.meemi_users_list_row, null);
			Holder = new ViewHolder();
			Holder.Image = (ImageView) vi.findViewById(R.id.UserRowAvatar);
			Holder.Text = (TextView) vi.findViewById(R.id.UserRowNick);
			vi.setTag(Holder);
		} else {
			Holder = (ViewHolder) vi.getTag();
		}
		
		if ( !Holder.IsLoadOtherUsers ) {
			if (IsExtraLine) {
				vi = ViewInflater.inflate(R.layout.list_row_other_loads, null);
				
				((TextView)vi.findViewById(id.RowLoadNewLabel)).setText(ResIDString);
				
				if (UseResIDIcon) {
					((ImageView)vi.findViewById(id.RowLoadNewIcon)).setImageResource(ResIDIcon);
				}
				
				Holder = new ViewHolder();
				Holder.IsLoadOtherUsers = true;
				vi.setTag(Holder);
			}
		} else {
			if (!IsExtraLine) {
				vi = ViewInflater.inflate(R.layout.meemi_users_list_row, null);
				Holder = new ViewHolder();
				Holder.Image = (ImageView) vi.findViewById(R.id.UserRowAvatar);
				Holder.Text = (TextView) vi.findViewById(R.id.UserRowNick);
				vi.setTag(Holder);
			}
			
		}

		if (!IsExtraLine) {
			if (null != UsersData) {
				Map<String, String> Item = UsersData.get(position);
			
				Holder.Text.setText( (String)Item.get("UserId") );
				Holder.Image.setTag( (String)Item.get("Avatar") );
				MyImageLoader.DisplayImage( (String)Item.get("Avatar"), MyActivity, Holder.Image );
			}
		}
		
		return vi;
	}
	
	
	
	private List<TreeMap<String, String>> UsersData = null;
	private int MaxNumberOfUsers = 0;
}
