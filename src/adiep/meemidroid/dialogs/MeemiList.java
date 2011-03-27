package adiep.meemidroid.dialogs;

import adiep.meemidroid.R;
import adiep.meemidroid.Utility;
import adiep.meemidroid.engine.LifestreamConst;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MeemiList extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    CurrentUser = this.getIntent().getStringExtra(USER);
	    
	    setContentView( Utility.createTabHostView(this) );

	    Resources res = getResources(); // Resource object to get Drawables
	    
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent I;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    I = new Intent().setClass(this, MeemiLifestream.class);
	    I.putExtra(MeemiLifestream.USER, CurrentUser);
		I.putExtra(MeemiLifestream.TYPE, LifestreamConst.GENERAL_LS);

	    // Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("GLS").setIndicator( getString(R.string.TabLifestreamGeneral), res.getDrawable(R.drawable.tabs_memesfera) ).setContent(I);
		
	                  
	    tabHost.addTab(spec);

	    // Do the same for the other tabs:
	    
	    // - personal
	    I = new Intent().setClass(this, MeemiLifestream.class);
	    I.putExtra(MeemiLifestream.USER, CurrentUser);
		I.putExtra(MeemiLifestream.TYPE, LifestreamConst.PERSONAL_LS);
		spec = tabHost.newTabSpec("PELS").setIndicator( getString(R.string.TabLifestreamPersonal), res.getDrawable(R.drawable.tabs_personal_ls) ).setContent(I);
	    tabHost.addTab(spec);
	    
	    // - private
	    I = new Intent().setClass(this, MeemiLifestream.class);
	    I.putExtra(MeemiLifestream.USER, CurrentUser);
		I.putExtra(MeemiLifestream.TYPE, LifestreamConst.PRIVATE_LS);
		spec = tabHost.newTabSpec("PRLS").setIndicator( getString(R.string.TabLifestreamPrivate), res.getDrawable(R.drawable.tabs_private_ls) ).setContent(I);
	    tabHost.addTab(spec);
	    
	    // - private (sent)
	    I = new Intent().setClass(this, MeemiLifestream.class);
	    I.putExtra(MeemiLifestream.USER, CurrentUser);
		I.putExtra(MeemiLifestream.TYPE, LifestreamConst.PRIVATE_SENT_LS);
		spec = tabHost.newTabSpec("PRSLS").setIndicator( getString(R.string.TabLifestreamPrivateSent), res.getDrawable(R.drawable.tabs_private_sent_ls) ).setContent(I);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(1); // set to personal
	}
	
	private String CurrentUser = null;
	
	public static final String USER = "UserNick";
}
