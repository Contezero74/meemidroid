package adiep.meemidroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.cookie.DateParseException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class contains all the code utility used in this application. 
 * 
 * @author Andrea de Iacovo, Lorenzo Mele, and Eros Pedrini
 * @version 0.5
 */
public class Utility {
	/**
	 * This method resizes the input image according to the input arguments Width, Height and Quality
	 * and returns a resized JPEG of the input image.
	 *  
	 * @param ImageBytes	the array of bytes representing the image to resize
	 * @param Width			the max width after the resize
	 * @param Height		the max height after the resize
	 * @param Quality		the quality of the output resized image
	 * 
	 * @return	an array of bytes representing the resize images. Note that if the image doesn't
	 * 			need to be resized the output image is the one used as input.
	 */
	public static byte[] resizeImage(final byte[] ImageBytes, final int Width, final int Height, final int Quality) {
		byte[] ResizedImage = ImageBytes;
		
		BitmapFactory.Options Option = null;
		Bitmap B = BitmapFactory.decodeByteArray(ImageBytes, 0, ImageBytes.length, Option);
		
		int OriginalWidth = B.getWidth();
		int OriginalHeight = B.getHeight();
		
		if (Width < OriginalWidth || Height < OriginalHeight) {
			double Scale = (double)Width / OriginalWidth;
			if (OriginalHeight > OriginalWidth) {
				Scale = (double)Height / OriginalHeight;
			}
			
			Bitmap SB = Bitmap.createScaledBitmap(B, (int)(Scale*OriginalWidth), (int)(Scale*OriginalHeight), true);
			
			ByteArrayOutputStream BAOS = new ByteArrayOutputStream();  
			SB.compress(Bitmap.CompressFormat.JPEG, Quality, BAOS);   
			ResizedImage = BAOS.toByteArray();
		} 
				
		return ResizedImage;
	}
	
	/**
	 * This method copies the input stream into an output stream.
	 * 
	 * @param is	the input stream to copy
	 * @param os	the output stream representing the stream copied
	 */
	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size=1024;
		
		try {
			byte[] bytes=new byte[buffer_size];
		    for(;;) {
		    	int count=is.read(bytes, 0, buffer_size);
		    	
		    	if(count==-1) { break; }
		    	
		    	os.write(bytes, 0, count);
		    }
		} catch(Exception ex) {
			// nothing to do
		}
	}

	
	/**
	 * This method shows Android Toast in the center of the screen.
	 * 
	 * @param C			the {@link Context} in which show the Toast
	 * @param ResId		the resource (string) to show
	 */
	public static void ShowToast(Context C, final int ResId) {
		ShowToast(C, ResId, Gravity.CENTER);
	}
	
	/**
	 * This method shows Android Toast in the center of the screen.
	 * 
	 * @param C			the {@link Context} in which show the Toast
	 * @param ResId		the resource (string) to show
	 * @param Gravity	the position in which show the Toast
	 */
	public static void ShowToast(Context C, final int ResId, final int Gravity) {
		Toast T = Toast.makeText(C, ResId, TOAST_TIME);
    	T.setGravity(Gravity, 0, 0);
    	T.show();
	}
	
	/**
	 * The default time used to show the Toast. Accepted values are
	 * {@link Toast#LENGTH_SHORT} and {@link Toast#LENGTH_LONG}.
	 */
	public static final int TOAST_TIME = Toast.LENGTH_LONG;
	
	/**
	 * This function transforms a message in Meemi format to a clean
	 * text message that can be inserted into a {@link TextView}.
	 * 
	 * @param Meemi	the message to transform
	 * 
	 * @return	the clean version of the message
	 */
	public static final String fromMeemiToCleanText(final String Meemi) {
		String CleanMeemi = "";
		
		// strange (and dangerous) chars... 
		CleanMeemi = Meemi.replaceAll("%", "&#037;");
		
		// carriage return
		CleanMeemi = CleanMeemi.replaceAll("(\\r\\n|\\n|\\r)", " ");
		
		// Bold		
		CleanMeemi = CleanMeemi.replaceAll("\\[b\\](.*?)\\[/b\\]", "<b>$1</b>");
		CleanMeemi = CleanMeemi.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
		
		// Italic
		CleanMeemi = CleanMeemi.replaceAll("<em>(.*?)</em>", "<i>$1</i>");
		CleanMeemi = CleanMeemi.replaceAll("\\[i\\](.*?)\\[/i\\]", "<i>$1</i>");
		CleanMeemi = CleanMeemi.replaceAll("__(.*?)__", "<i>$1</i>");
		
		// Underline
		CleanMeemi = CleanMeemi.replaceAll("\\[u\\](.*?)\\[/u\\]", "<u>$1</u>");
		
		// Stroke
		CleanMeemi = CleanMeemi.replaceAll("\\[del\\](.*?)\\[/del\\]", "<del>$1</del>");
		
		// Quote
		CleanMeemi = CleanMeemi.replaceAll("\\[quote\\](.*?)\\[/quote\\]", "$1");
		
		// Code
		CleanMeemi = CleanMeemi.replaceAll("\\[code\\](.*?)\\[/code\\]", "$1");
		
		// Link: TODO change link to meemi post view activity (when ready :O) for links to Meemi world
		CleanMeemi = CleanMeemi.replaceAll("\\[l:([^\\|]*?)\\|([^\\]]*?)\\]", "<i>$2</i>");
		
		// ScreenName 
		CleanMeemi = CleanMeemi.replaceAll("\\@(\\w{5,})", " <i>@$1</i>");
		
		return CleanMeemi;
	}
	
	/**
	 * This function transforms a message in Meemi format to HTML
	 * format.
	 * 
	 * @param Meemi	the message to transform
	 * 
	 * @return	the HTML version of the message
	 */
	public static final String fromMeemiToHTML(final String Meemi) {
		String HTML = "";
		
		// strange (and dangerous) chars... 
		HTML = Meemi.replaceAll("%", "&#037;");
		
		// carriage return
		HTML = HTML.replaceAll("(\\r\\n|\\n|\\r)", "<br />");
		
		// Bold		
		HTML = HTML.replaceAll("\\[b\\](.*?)\\[/b\\]", "<b>$1</b>");
		HTML = HTML.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
		
		// Italic
		HTML = HTML.replaceAll("<em>(.*?)</em>", "<i>$1</i>");
		HTML = HTML.replaceAll("\\[i\\](.*?)\\[/i\\]", "<i>$1</i>");
		HTML = HTML.replaceAll("__(.*?)__", "<i>$1</i>");
		
		// Underline
		HTML = HTML.replaceAll("\\[u\\](.*?)\\[/u\\]", "<u>$1</u>");
		
		// Stroke
		HTML = HTML.replaceAll("\\[del\\](.*?)\\[/del\\]", "<del>$1</del>");
		
		// Quote
		HTML = HTML.replaceAll("\\[quote\\](.*?)\\[/quote\\]", "<blockquote>$1</blockquote>");
		
		// Code
		HTML = HTML.replaceAll("\\[code\\](.*?)\\[/code\\]", "<pre><code>$1</code></pre>");
		
		// the main idea is:
		// (a) encode the links and the urls [forward transformation]
		// (b) apply the other ScreenName transformation
		// (c) decode back the links and urls [backward transformation]
		// (d) apply links and url transformations
		// TODO: this fix has a very high cost, but for the moment we don't have any better solution
		
		// forward transformation to resolve ScreenName problem
		HTML = encodeParts(HTML, "\\[l:([^\\|]*?)\\|([^\\]]*?)\\]");
		HTML = encodeParts(HTML, "(?<!href=\")(http|https|ftp|mailto)://([\\S&&[^<;|]]+)");
		
		// ScreenName
		HTML = HTML.replaceAll("\\@(\\w{5,})", " <a class=\"link\" onClick=\"window.account.clickOnAccount('$1')\" href='#'>@$1</a>");
		
		// backward transformation
		HTML = decodeParts(HTML, "\\{mds:([^\\}]*?)\\}");
		
		
		// Link - TODO: change link to meemi post view activity (when ready :O) for links to Meemi world
		HTML = HTML.replaceAll("\\[l:([^\\|]*?)\\|([^\\]]*?)\\]", "<a class=\"link\" href=\"$1\" title=\"go to $2\">$2</a>");
		
		// HTML urls (TODO: probably will be removed in future versions)
		HTML = HTML.replaceAll("(?<!href=\")(http|https|ftp|mailto)://([\\S&&[^<;|]]+)", "<a class=\"link\" href=\"$1://$2\" title=\"go to $2\">$2</a>" );
		//HTML = HTML.replaceAll("(?<!\\[l:\\s{0,10}?|\\[l:.{0,100}?\\|\\s{0,10}?)(http|https|ftp|mailto)://(\\S+)", "[l:$1://$2|$2]" );
		
		
		
		return HTML;
	}
	
	public static String encodeParts(final String Msg, final String RegEx) {
		Pattern P = Pattern.compile(RegEx, Pattern.CASE_INSENSITIVE);
			
		String R = "";
		
		Matcher M = P.matcher(Msg);
		int LastIndex = 0;
		while ( M.find() ) {
			R += Msg.substring(LastIndex, M.start()) + "{mds:";
			
			String Part2Encode = Msg.substring(M.start(), M.end());
			R += Base64.encodeToString(Part2Encode.getBytes(), Base64.DEFAULT);
			
			R += "}";
			
			LastIndex = M.end();
		}
		R += Msg.substring(LastIndex);
		
		return R;
	}
	
	public static String decodeParts(final String Msg, final String RegEx) {
		Pattern P = Pattern.compile(RegEx, Pattern.CASE_INSENSITIVE);
			
		String R = "";
		
		Matcher M = P.matcher(Msg);
		int LastIndex = 0;
		while ( M.find() ) {
			R += Msg.substring(LastIndex, M.start());
			
			String Part2Decode = Msg.substring(M.start()+5, M.end()-1);
			R += new String( Base64.decode(Part2Decode, Base64.DEFAULT) );
			
			LastIndex = M.end();
		}
		R += Msg.substring(LastIndex);
		
		return R;
	}
	
	/**
	 * This function wraps the input message into HTML standard definition.
	 * 
	 * @param HTML	the message to wrap
	 * 
	 * @return	the wrapped message
	 */
	public static final String wrapForHTML(final String HTML) {
		return "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /></head><body><div>" +
			   HTML +
			   "</div></body></html>";
	}
	
	/**
	 * This function wraps the input message into HTML standard definition.
	 * 
	 * @param HTML	the message to wrap
	 * @param CSS	the CSS for format the HTML message
	 * 
	 * @return	the wrapped message
	 */
	public static final String wrapForHTML(final String HTML, final String CSS) {
		String Result = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
						"<style type='text/css'>" + CSS + "</style></head><body><div>" +
						HTML + "</div></body></html>"; 
		return Result;
	}
	
	/**
	 * This function checks if we are near Xmas time (i.e., from the 10th of December to
	 * 10th of January)
	 *  
	 * @return true if the we are near Xmas time, otherwise returns false
	 */
	public static boolean isXmasTime() {
		boolean IsXmas = false;
		
		Calendar Today = GregorianCalendar.getInstance();
		
		int Month = Today.get(Calendar.MONTH);
		int Day = Today.get(Calendar.DAY_OF_MONTH);
		
		if ( (Calendar.DECEMBER == Month && 10 <= Day) ||
			 (Calendar.JANUARY == Month && 10 >= Day) ) {
			IsXmas = true;
		}
		
		return IsXmas;
	}

	/**
	 * This method return the video source URL from the input string, if need:
	 * for example if it's stored into an iframe.
	 * 
	 * @param string	the string containing the video source URL
	 * 
	 * @return the extracted source URL
	 */
	public static String getVideoSrc(final String Video) {
		String Result = Video;
		
		if ( Video.contains("src=\"") ) {
			Matcher M = Pattern.compile("src=\"([^\\]]*?)\"", Pattern.CASE_INSENSITIVE).matcher(Video);
			if ( M.find() ) {
				Result = M.group(1);
			}
		}
		
		return Result;
	}
	
	/**
	 * This method formats the input date according to the current localization context.
	 * 
	 * @param StringDate	the date to format
	 * @param OutputFormat		the string representing the data format
	 * 
	 * @return the formated date
	 */
	public static String formatDate(final String StringDate, final String OutputFormat, final String InputFormat) {
		String FormatedDate = "";
		
		String InputDate = StringDate.replaceAll("T", " ").replaceAll("Z", "");
		
		try {			
			if ( !("0000-00-00".equals(InputDate)) ) {
				Date JavaDate = org.apache.http.impl.cookie.DateUtils.parseDate(InputDate, new String[] {InputFormat});
			
				FormatedDate = DateFormat.format( OutputFormat, JavaDate ).toString();
			}
		} catch (DateParseException ex) {
			Log.d("Utility", "Can not format the user date", ex);
			
			FormatedDate = "";
		}
		
		return FormatedDate;
	}
	
	/**
	 * This function returns the current version of the application.
	 * 
	 * @return	the current version of the application
	 */
	public static String getVersion() {
		String Version = "";
		try {
            PackageInfo packageInfo = MeemiDroidApplication.getContext().getPackageManager().getPackageInfo(MeemiDroidApplication.getContext().getPackageName(), 0);
            
            Version = packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
            Log.w("Utility - getVersion", "Package name not found", e);
		};
		
		return Version;
	}
	
	/**
	 * This method try to load a text file from stored in the asset Android folder located
	 * in the Path argument.
	 * 
	 * @param Path the path to use to load the text file
	 * 
	 * @return the loaded text file
	 * 
	 * @throws IOException if some error raises
	 */
	public static String readTextFileFromAsset(final String Path) throws IOException {
		Log.i("Utility - readTextFileFromAsset", "Try to load " + Path);
		
		AssetManager AM = MeemiDroidApplication.getContext().getAssets();
		
		InputStream IS = AM.open(Path);
		
		ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
		
		int i;
		try {
			i = IS.read();
			while (i != -1) {
				BAOS.write(i);
				i = IS.read();
			}
		} catch (IOException e) {
			Log.d("Utility - readTextFileFromAsset", "Error during reading the asset: " + Path, e);
			
			throw e;
		} finally {
			IS.close();
		}
		
		return BAOS.toString();
	}

	/**
	 * This function creates a view layout for a standard {@link TabHost} {@link Activity}.
	 * The view is created programmatically in order to avoid a problem with the
	 * Eclipse IDE with android ADT plugin: in fact seems that there is a bug in
	 * TabHost support.
	 * 
	 * @param	A	the Activity that wants use the TabHost
	 * 
	 * @return	a view layout for a standard TabHost
	 */
	public static View createTabHostView(Activity A) {
		
		TabWidget TW = new TabWidget(A);
		TW.setId(android.R.id.tabs);
		TW.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT) );
		
		FrameLayout FL = new FrameLayout(A);
		FL.setId(android.R.id.tabcontent);
		FL.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT) );
		FL.setPadding(5, 5, 5, 5);
		
		LinearLayout LL = new LinearLayout(A);
		LL.setOrientation(LinearLayout.VERTICAL);
		LL.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT) );
		LL.setPadding(5, 5, 5, 5);
		LL.addView(TW);
		LL.addView(FL);
		
		TabHost TH = new TabHost(A);
		TH.setId(android.R.id.tabhost);
		TH.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT) );
		TH.addView(LL);
		
		return TH;
	}
	

	/**
	 * This method returns true if the terminal is connected to Internet, otherwise it return false.
	 * 
	 * @param C	the {@link Context} used to check if the terminal is connected to Internet
	 * 
	 * @return	true if the terminal is connected to Internet, otherwise it return false
	 */
	public static boolean isInternetConnected(Context C) {
		ConnectivityManager CM = (ConnectivityManager) C.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (null != CM) {
			NetworkInfo NI = CM.getActiveNetworkInfo();
			
			if (null != NI) {
				return NI.isConnected();
			}
		}
	    
		return false;

	}
}
