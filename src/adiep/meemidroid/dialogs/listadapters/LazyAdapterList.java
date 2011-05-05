package adiep.meemidroid.dialogs.listadapters;

import adiep.meemidroid.ImageLoader;
import adiep.meemidroid.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * This class implements a new adapter associated with the {@link ImageLoader}
 * class. This adapter can be used when an adapter need an asynchronous image loader
 * to model a list view.
 * 
 *  @see LazyAdapterMeemisList
 *  @see LazyAdapterRepliesList
 *  @see LazyAdapterUsersList
 * 
 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
 * @version 0.5
 */
public abstract class LazyAdapterList extends BaseAdapter {
	
	/**
	 * This interface represents a row of the list view.
	 *  
	 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
	 * @version 0.5
	 */
	public static class ViewHolder {
		public ImageView Image = null;
	}
	
	
	/**
	 * This constructor setups the adapter binding it the activity that uses it.
	 * 
	 * @param A		the current Andorid activity
	 */
	public LazyAdapterList(Activity A) {
		this.MyActivity = A;
		
		LazyAdapterList.ViewInflater = (LayoutInflater) MyActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		MyImageLoader = ImageLoader.getInstance();
	}
	
	/**
	 * This constructor setups the adapter binding it the activity that uses it.
	 * 
	 * @param A					the current Andorid activity
	 * @param ExtraRowStringID	the resource ID for the string to show in the extra "loading" row
	 */
	public LazyAdapterList(Activity A, final int ExtraRowStringID) {
		this(A);
		
		ResIDString = ExtraRowStringID;
	}

	/**
	 * This constructor setups the adapter binding it the activity that uses it.
	 * 
	 * @param A					the current Andorid activity
	 * @param ExtraRowStringID	the resource ID for the string to show in the extra "loading" row
	 * @param ExtraRowImageID	the resource ID for the icon to show in the extra  "loading" row
	 */
	public LazyAdapterList(Activity A, final int ExtraRowStringID, final int ExtraRowImageID) {
		this(A, ExtraRowStringID);
		
		ResIDIcon = ExtraRowImageID;
		UseResIDIcon = true;
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
	
	
	protected Activity MyActivity = null;
	
	protected int ResIDString = R.string.ListItemExtraLoad;
	protected boolean UseResIDIcon = false;
	protected int ResIDIcon = 0;
	
	protected ImageLoader MyImageLoader;
	
	protected static LayoutInflater ViewInflater = null;
}
