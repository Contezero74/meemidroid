package adiep.meemidroid.dialogs;

import adiep.meemidroid.R;
import adiep.meemidroid.Utility;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

/**
 * This activity represents a configurable splash screen:
 * i.e., an ImageView showed centered on the screen for a
 * specific amount of time.
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.1
 */
public class SplashScreen extends Activity {
	
	/**
     * This method is called when the activity is first created.
     * The default ImageView layout has to be named "splash".
     * If you want change the layout, you can use {@link Intent#putExtra(String, int)}
     * to pass the new layout id. The key to use is SPLASH_LAYOUT.
     * If you want change the showing time (in milliseconds) of the splash screen, you
     * can use {@link Intent#putExtra(String, long)} with the key SPLASH_TIME.
     *  
     * @param savedInstanceState	if the activity is being re-initialized
     * 		 						after previously being shut down then this
     * 								Bundle contains the data it most recently
     * 								supplied in {@link #onSaveInstanceState(Bundle)}.
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("SplashScreen", "onCreate()");
		
		super.onCreate(savedInstanceState);
		
		int SplashLayout = this.getIntent().getIntExtra(SPLASH_LAYOUT, DEFAULT_LAYOUT);

		setContentView(SplashLayout);
		
		boolean IsXMas = Utility.isXmasTime();
		
		if (IsXMas) {
			ImageView I = (ImageView)findViewById(R.id.ImageViewSplash);
			I.setImageResource(R.drawable.meemidroid_xmas);
		}
		
		long SplashTime = this.getIntent().getLongExtra(SPLASH_TIME, DEFAULT_TIME);
		
		Message msg = new Message();
		msg.what = STOPSPLASH;
		splashHandler.sendMessageDelayed(msg, SplashTime);
	}
	
	/* handler for splash screen: close the splash screen after the correct amount of
	 * time.
	 */
	private Handler splashHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case STOPSPLASH:
					finish();
					break;
			}

			super.handleMessage(msg);
		}
	};
	
	/**
	 * The key to be used with {@link Intent#putExtra(String, int)} to
	 * change the layout to use.
	 */
	public static final String SPLASH_LAYOUT = "SplashLayout";
	
	/**
	 * The key to be used with {@link Intent#putExtra(String, long)} to
	 * change the showing time of the splash screen.
	 */
	public static final String SPLASH_TIME = "SplashTime";
	
	// default splash layout id
	private static final int DEFAULT_LAYOUT = R.layout.splash;
	
	// default time in milliseconds
	private static final long DEFAULT_TIME = 3000;
	
	// message for closing the splash
	private static final int STOPSPLASH = 0;
}
