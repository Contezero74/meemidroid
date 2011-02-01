package adiep.meemidroid.engine.communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.util.Log;
import adiep.meemidroid.support.backcompatibility.Pair;

/**
 * This class represents the engine used to low level access multipart HTTP Post protocol.
 * This implementation relays on standard Java communication protocol, because at the
 * current time Android (2.2) doesn't support multipart message.
 * One possible solution is to use a set of external library provided by Apache, but
 * we want a light-impact solution.
 * This class is a refactoring of the original version od Andrea de Iacovo implementation. 
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.1
 */
public class HTTPMultipartPostEngine extends HTTPEngine {

	/**
	 * This is the default constructor.
	 */
	public HTTPMultipartPostEngine() {
		// nothing to do
	}
	
	/**
	 * This is the HttpPost class constructor. It sets the URI (usually an URL) representing the HTTP address.
	 * 
	 * @param U	the URI representing the HTTP address
	 */
	public HTTPMultipartPostEngine(final String U) {
		setURI(U);
	}
	
	/**
	 * This is the HttpPost class constructor. It sets the URI (usually an URL) representing the HTTP address.
	 * 
	 * @param U				the URI representing the HTTP address
	 * @param ObjectURI		the Object to send via HTTP Post
	 * @param ObjectType	the type of the object to send
	 * @param C				the application context (it's used to retrieve the stream of the object to send)
	 */
	public HTTPMultipartPostEngine(final String U, final byte[] ObjectBytes, final int ObjectSize, final String ObjectFileName, final String ObjectType, Context C) {
		setURI(U);
		
		this.ObjectBytes = ObjectBytes;
		this.ObjectSize = ObjectSize;
		this.ObjectFileName = ObjectFileName;
		this.ObjectType = ObjectType;
		this.MyContext = C;
	}

	/**
	 * This method performs the HTTP POST request and returns the server response
	 * 
	 * @return	the server response for an HTPP POST request
	 */
	public String execute() {
		String ServerResponse = "";
		
		URLConnection Connection	= null;
		DataOutputStream Writer		= null;
		try {
			Connection = new URL(ConnectionURI).openConnection();
			Connection.setDoOutput(true);
			Connection.setDoInput(true);
			Connection.setRequestProperty("Content-type", "multipart/form-data; boundary=data_boundary");
			
			Writer = new DataOutputStream( Connection.getOutputStream() );
			
			setUrlEncodedFormEntities(Entities, Writer, HTTP.UTF_8);
			
			if (null != ObjectBytes && null != ObjectType && null != MyContext) {
				setObject(Writer);
			}
			
			Writer.close();
			
			
		} catch (Exception ex) {
			Log.d("HTTPMultipartPostEngine", "Can not execute the Server call", ex);
			
			if (null != Writer) {
				try {
					Writer.close();
				} catch (IOException e) {
					// nothing to do
				}
			}
		}
		
		try{
			BufferedReader rd = new BufferedReader( new InputStreamReader( Connection.getInputStream() ) );
         
			String Line;

			while ( null != ( Line = rd.readLine() ) ) {
				 ServerResponse += Line + "\n";
			}
         
			rd.close();
		}catch (Exception ex){
			Log.d("HTTPMultipartPostEngine", "Can not add read json response", ex);
		}

        return ServerResponse;
	}
	
	private void setUrlEncodedFormEntities(final List<Pair<String, String>> Entities, DataOutputStream Writer, final String EncodeType) throws IOException {
		String UrlEncodeData = MP_FIRST_BOUNDARY;
		
		for (Pair<String, String> A : Entities) {
			String Name = A.first;
			
			if (null != Name && "" != Name) {
				String Value = A.second;
				
				Value = (null != Value) ? Value : "";
			
				UrlEncodeData += "Content-Disposition: form-data; name=\"" + Name + "\"" + MP_NEWLINE + MP_NEWLINE +
							 	 Value + MP_BOUNDARY;
			}
		}
		
		Writer.write( UrlEncodeData.getBytes(EncodeType) );
	}
	
	private void setObject(DataOutputStream Writer) throws IOException {
		if ( ObjectType.contains("img_pc") ) {
			String Data = "Content-Disposition: file; filename=\"" + ObjectFileName + "\"; name=\"img_pc\"" + MP_NEWLINE + MP_NEWLINE;
            Writer.writeBytes(Data);
            
            Writer.write(ObjectBytes, 0, ObjectSize);
            Writer.writeBytes(MP_BOUNDARY);
		}
	}
	
	
	private byte[] ObjectBytes		= null;
	private int ObjectSize			= 0;
	private String ObjectFileName	= null;
	private String ObjectType		= null;
	private Context MyContext		= null;
	
	private final String MP_FIRST_BOUNDARY = "--data_boundary\r\n";
	private final String MP_BOUNDARY = "\r\n--data_boundary\r\n";
	private final String MP_NEWLINE = "\r\n";

}
