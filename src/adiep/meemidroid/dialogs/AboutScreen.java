package adiep.meemidroid.dialogs;

import java.io.IOException;

import adiep.meemidroid.R;
import adiep.meemidroid.Utility;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * This activity represents a configurable about box based showing
 * an HTML file stored in the asset folder of the application.
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.1
 */
public class AboutScreen extends Activity {
	
	/**
     * This method is called when the activity is first created.
     * The default HTML page containing the "About Box" information
     * has to be stored in "asset/about/about.html" within all 
     * extra files needed. If you want change the location and/or
     * the name of the file you can use {@link Intent#putExtra(String, String)}
     * with the key ABOUT_PATH to pass the correct path without the "asset/" part. 
     * 
     * @param savedInstanceState	if the activity is being re-initialized
     * 		 						after previously being shut down then this
     * 								Bundle contains the data it most recently
     * 								supplied in {@link #onSaveInstanceState(Bundle)}.
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("AboutScreen", "onCreate()");
		
		super.onCreate(savedInstanceState);
		
		boolean IsXmas = Utility.isXmasTime();
		
		String AboutPath = this.getIntent().getStringExtra(ABOUT_PATH);
		if (null == AboutPath) {
			AboutPath = DEFAULT_PATH;
			
			if (IsXmas) {
				AboutPath = DEFAULT_XMAS_PATH;
			}
		}
		
		setContentView(R.layout.about);
		WebView mWebView = (WebView) findViewById(R.id.WebView01);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        
        if (IsXmas) {
        	webSettings.setJavaScriptEnabled(true); // only for xmas ;)
        } else {
        	webSettings.setJavaScriptEnabled(false);
        }
        
        webSettings.setSupportZoom(false);

        String CurrentVersion = Utility.getVersion();
        
        try {
        	String AboutHTML = Utility.readTextFileFromAsset(AboutPath);
        	
        	AboutHTML = AboutHTML.replaceAll("<!-- <h2>%%VERSION%%</h2>  -->", "<h2>" + CurrentVersion + "</h2>");
        	
        	mWebView.loadDataWithBaseURL("", AboutHTML, "text/html", "utf-8", "");
        } catch (IOException e) {
			Log.d("AboutScreen - onCreate", "Error during loading the About HTML screen", e);
			
            mWebView.loadUrl("file:///android_asset/" + AboutPath);
		}      
	}
	
	/**
	 * The key to be used with {@link Intent#putExtra(String, String)}
	 */
	public static final String ABOUT_PATH = "AboutPath";
	
	// default value
	private static final String DEFAULT_PATH = "about/about.html";
	private static final String DEFAULT_XMAS_PATH = "about/about_xmas.html";
}
