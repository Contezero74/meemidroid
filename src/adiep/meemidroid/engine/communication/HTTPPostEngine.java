/**
 * 
 */
package adiep.meemidroid.engine.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.util.Log;
import adiep.meemidroid.support.backcompatibility.Pair;

/**
 * This class represents the engine used to low level access no-multipart HTTP Post protocol.
 * This implementation relays on the Apache HTTPClient library.
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.1
 */
public class HTTPPostEngine extends HTTPEngine {
	
	/**
	 * This is the default constructor.
	 */
	public HTTPPostEngine() {
		// nothing to do
	}
	
	/**
	 * This is the HttpPost class constructor. It sets the URI (usually an URL) representing the HTTP address.
	 * 
	 * @param U	the URI representing the HTTP address
	 */
	public HTTPPostEngine(final String U) {
		setURI(U);
	}

	/**
	 * This method performs the HTTP POST request and returns the server response
	 * 
	 * @return	the server response for an HTPP POST request
	 */
	public String execute() {
		// Create the Apache HTTP client and post
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(ConnectionURI);
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
		for (Pair<String, String> A : Entities) {
			pairs.add( new BasicNameValuePair( A.first, A.second ) );
		}
		
		if ( 0 != pairs.size() ) {
			try {
				httppost.setEntity( new UrlEncodedFormEntity(pairs, HTTP.UTF_8) );
				
				//Log.i( "MeemiDroid - Info", "Post Entity encoding: " + httppost.getEntity().getContentEncoding().getValue() );
				
			} catch (UnsupportedEncodingException ex) {
				Log.d("HTTPPostEngine", "Can not add authentification params", ex);					
			}
		}
		
		String ServerResponse = "";
		try {
			// Finally, execute the request
			HttpResponse webServerAnswer = httpclient.execute(httppost);
		
			// Now we can retrieve all the response
			BufferedReader rd = new BufferedReader( new InputStreamReader( webServerAnswer.getEntity().getContent() ) );
		
			String Line;
		
			while ( null != ( Line = rd.readLine() ) ) {
				ServerResponse += Line + "\n";
			}
			
			rd.close();
		} catch (IOException ex) {
			Log.d("HTTPPostEngine", "Can not add read json response", ex);
		}
		
		return ServerResponse;
	}
}
