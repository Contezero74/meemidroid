package adiep.meemidroid.dialogs.listadapters;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import adiep.meemidroid.MeemiDroidApplication;
import adiep.meemidroid.R;
import adiep.meemidroid.R.id;
import adiep.meemidroid.Utility;
import android.app.Activity;
import android.text.Html;
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
public class LazyAdapterMeemisList extends LazyAdapterList {
	
	/**
	 * This class represents a row of the list view containing the users list.
	 *  
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 * @version 1.0
	 */
	public static class ViewHolder extends LazyAdapterList.ViewHolder {
		public TextView Nick = null;
		public TextView Time = null;
		public TextView Message = null;
		public TextView OtherInfo = null;
		public ImageView IsFavorite = null;
		public ImageView IsPhoto = null;
		public ImageView IsVideo = null;
		public ImageView IsLink = null;
		public ImageView IsPrivate = null;

		public boolean IsLoadOtherMeemi = false;
	}
	
	/**
	 * This constructor setups the list view adapters loading the users list to display.
	 * 
	 * @param A					the current Andorid activity
	 * @param MeemisData		the Meemis list to display.
	 */
	public LazyAdapterMeemisList(Activity A, List<TreeMap<String, String>> MeemisData) {
		super(A, R.string.ListItemExtraLoad);
		
		this.MeemisData = MeemisData;
	}
	
	/**
	 * This constructor setups the list view adapters loading the users list to display.
	 * 
	 * @param A					the current Andorid activity
	 * @param UsersData			the Meemis list to display.
	 * @param ExtraRowStringID	the resource ID for the string to show in the extra "loading" row
	 */
	public LazyAdapterMeemisList(Activity A, List<TreeMap<String, String>> MeemisData, final int ExtraRowStringID) {
		super(A, ExtraRowStringID);
		
		this.MeemisData = MeemisData;
	}

	/**
	 * This constructor setups the list view adapters loading the users list to display.
	 * 
	 * @param A					the current Andorid activity
	 * @param MeemisData		the Meemis list to display.
	 * @param ExtraRowStringID	the resource ID for the string to show in the extra "loading" row
	 * @param ExtraRowImageID	the resource ID for the icon to show in the extra  "loading" row
	 */
	public LazyAdapterMeemisList(Activity A, List<TreeMap<String, String>> MeemisData, final int ExtraRowStringID, final int ExtraRowImageID) {
		super(A, ExtraRowStringID, ExtraRowImageID);
		
		this.MeemisData = MeemisData;
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
		View vi = convertView;
		ViewHolder holder;
		
		boolean IsExtraLine = position >= MeemisData.size();
		
		if (convertView == null) {
			vi = ViewInflater.inflate(R.layout.meemi_msgs_list_row, null);
			holder = new ViewHolder();
			holder.Image = (ImageView) vi.findViewById(R.id.UserAvatar);
			holder.Nick = (TextView) vi.findViewById(R.id.UserNick);
			holder.Time = (TextView) vi.findViewById(R.id.MeemeTime);
			holder.Message = (TextView) vi.findViewById(R.id.Message);
			holder.OtherInfo = (TextView) vi.findViewById(R.id.OtherInfo);
			holder.IsFavorite = (ImageView) vi.findViewById(R.id.FavImage);
			holder.IsPhoto = (ImageView) vi.findViewById(R.id.PhotoImage);
			holder.IsVideo = (ImageView) vi.findViewById(R.id.VideoImage);
			holder.IsLink = (ImageView) vi.findViewById(R.id.Linklmage);
			holder.IsPrivate = (ImageView) vi.findViewById(R.id.LockImage);
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
				holder.Image = (ImageView) vi.findViewById(R.id.UserAvatar);
				holder.Nick = (TextView) vi.findViewById(R.id.UserNick);
				holder.Time = (TextView) vi.findViewById(R.id.MeemeTime);
				holder.Message = (TextView) vi.findViewById(R.id.Message);
				holder.OtherInfo = (TextView) vi.findViewById(R.id.OtherInfo);
				holder.IsFavorite = (ImageView) vi.findViewById(R.id.FavImage);
				holder.IsPhoto = (ImageView) vi.findViewById(R.id.PhotoImage);
				holder.IsVideo = (ImageView) vi.findViewById(R.id.VideoImage);
				holder.IsLink = (ImageView) vi.findViewById(R.id.Linklmage);
				holder.IsPrivate = (ImageView) vi.findViewById(R.id.LockImage);
				vi.setTag(holder);
			}
			
		}

		if (!IsExtraLine) {
			if (null != MeemisData) {
				Map<String, String> Item = MeemisData.get(position);
			
				holder.Image.setTag( (String)Item.get("MeemerAvatar") );
				holder.Nick.setText( (String)Item.get("MeemerName") );
				holder.Time.setText( (String)Item.get("Time") );
				
				holder.IsPhoto.setVisibility(View.GONE);
				holder.IsVideo.setVisibility(View.GONE);
				holder.IsLink.setVisibility(View.GONE);
				if ( Item.containsKey("Image") ) {
					holder.IsPhoto.setVisibility(View.VISIBLE);
				} else if ( Item.containsKey("Video") ) {
					holder.IsVideo.setVisibility(View.VISIBLE);
				} else if ( Item.containsKey("Link") ) {
					holder.IsLink.setVisibility(View.VISIBLE);
				}
				
				holder.IsFavorite.setVisibility(View.GONE);
				if ( "1".equals( (String)Item.get("IsFavorite") ) ) {
					holder.IsFavorite.setVisibility(View.VISIBLE);
				}
				
				String CleanText = "";
				
				/*
				if ( Item.containsKey("ExtraContent") ) {
					CleanText = (String)Item.get("ExtraContent") + " ";
				}
				*/
				
				CleanText += Utility.fromMeemiToCleanText( (String)Item.get("Content") );
				holder.Message.setText( Html.fromHtml(CleanText) );
				
				holder.OtherInfo.setText( MeemiDroidApplication.getContext().getString(R.string.MsgComment) + (String)Item.get("NumOfComments") );
				MyImageLoader.DisplayImage( (String)Item.get("MeemerAvatar"), MyActivity, holder.Image );
			}
		}
		
		return vi;
	}
	
	
	private List<TreeMap<String, String>> MeemisData = null;	
}
