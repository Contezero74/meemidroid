package adiep.meemidroid.engine;

import java.security.MessageDigest;

import adiep.meemidroid.MeemiDroidApplication;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * This class represents the user credentials to access to Meemi.
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.5
 */
public class MeemiCredentials {
	
	/**
	 * This is tha class constructor; at the moment it does nothing.
	 */
	public MeemiCredentials() {
	}
	
	/**
	 * This method returns the user name stored in the credential.
	 * 
	 * @return the user name
	 */
	public String getUsername() {
		return UserName;
	}
	
	/**
	 * This method sets the user name into the credential.
	 * 
	 * @param userName the user name to set
	 */
	public void setUsername(final String userName) {
		if (null == userName) {
			UserName = "";
		}
		
		UserName = userName;
	}
	
	/**
	 * This method returns the plain password stored in the credential.
	 * 
	 * @return the plain password
	 */
	public String getPassword() {
		return Password;
	}
	
	/**
	 * This method returns the hashed (SHA256) password stored in the credential.
	 * 
	 * @return the hashed password
	 */
	public String getHashedPassword() {
		return HashedPassword;
	}
	
	/**
	 * This method sets the user password into the credential.
	 * 
	 * @param password the user password to set
	 */
	public void setPassword(final String password) {
		if (null == password) {
			Password = "";
		}
		
		Password = password;
		HashedPassword = Hash(Password);
	}
	
	/**
	 * This method return true if either user name and password are
	 * stored in the credentials.
	 * 
	 * @return true if either user name and password are stored, otherwise false
	 */
	public boolean isMemorized() {
		return ( !UserName.equals("") && !Password.equals("") );
	}
	
	/**
	 * This method retrieves the current credentials from the shared preferences
	 * system of Android.
	 */
	public void load() {
		Context C = MeemiDroidApplication.getContext();
		
		// Use MODE_WORLD_READABLE and/or MODE_WORLD_WRITEABLE to grant access to other applications
		SharedPreferences P = C.getSharedPreferences(MEEMI_CREDENTIALS_PREFS, Context.MODE_PRIVATE);
		
		UserName = P.getString(MEEMI_USERNAME, "");
		Password = P.getString(MEEMI_PASSWORD, "");
		HashedPassword = P.getString(MEEMI_HASHED_PASSWORD, "");
	}
	
	/**
	 * This method saved the current credentials into the shared preferences
	 * system of Android.
	 */
	public void save() {
		Context C = MeemiDroidApplication.getContext();
		
		// Use MODE_WORLD_READABLE and/or MODE_WORLD_WRITEABLE to grant access to other applications
		SharedPreferences P = C.getSharedPreferences(MEEMI_CREDENTIALS_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor E = P.edit();
		
		E.putString(MEEMI_USERNAME, UserName);
		E.putString(MEEMI_PASSWORD, Password);
		E.putString(MEEMI_HASHED_PASSWORD, HashedPassword);
		
		E.commit();
	}
	
	/**
	 * This method generate the SHA256 hash of the input argument.
	 * 
	 * @param P	the password to hash
	 * 
	 * @return	the hashed password
	 */
	private String Hash(final String P) {
		StringBuffer Result = new StringBuffer();
		
		try {
			MessageDigest Sha = MessageDigest.getInstance("SHA-256");
			Sha.reset();
			byte messageDigest[] = Sha.digest( P.getBytes() );
        
			String Tmp;
			for (int i = 0; i < messageDigest.length ; ++i) {
				Tmp = Integer.toHexString(0xFF & messageDigest[i]);
			
				if (Tmp.length() == 1) {
					Result.append('0');
				}

				Result.append(Tmp);
			}
        } catch (Exception ex) {
        	Log.e("MeemiCredentials.Hash", "An exception has been rised during the hash evaluation for the password * " + P + " *");
        }

        return Result.toString();
	}
	
	private static final String MEEMI_CREDENTIALS_PREFS = "MeemiCredentials";
	private static final String MEEMI_USERNAME = "Username";
	private static final String MEEMI_PASSWORD = "Password";
	private static final String MEEMI_HASHED_PASSWORD = "HashPassword";
	
	private String UserName = "";
	private String Password = "";
	private String HashedPassword = "";
}
