package adiep.meemidroid.engine.communication;

import java.util.ArrayList;
import java.util.List;

import adiep.meemidroid.support.backcompatibility.Pair;

/**
 * This abstract class represents the engine used to low level access to the HTTP Post protocol.
 * 
 * @author Andrea de Iacovo, and Eros Pedrini
 * @version 0.1
 */
public abstract class HTTPEngine {
	/**
	 * This method sets the URI (usually an URL) representing the HTTP address.
	 * 
	 * @param U	the URI representing the HTTP address
	 */
	public void setURI(final String U) {
		this.ConnectionURI = U;
	}

	/**
	 * This method gets the URI set for the HTTP POST request.
	 * 
	 * @return	the URI set for the HTTP POST request.
	 */
	public String getURI() {
		return ConnectionURI;
	}

	/**
	 * This method adds a new pair <Name, Value> to the list of Entities that has to be sent
	 * within the URI set by {@link #setURI(String)}.
	 * 
	 * @param E	the entity to add
	 * 
	 * @see #setURI(String)
	 * 
	 * @return	the number of entities already added
	 */
	public int addEntityPair(final Pair<String, String> E) {
		Entities.add(E);
		
		return Entities.size();
	}

	/**
	 * This method returns the number of entities ready to be sent within the HTTP POST request
	 * 
	 * @return	the number of entities ready to be sent
	 */
	public int getNumberOfEntities() {
		return Entities.size();
	}

	/**
	 * This method removes the entity associated with the index used as argument-
	 * 
	 * @param Index the index of the entity to remove
	 * 
	 * @return	the number of entities stored
	 */
	public int removeEntity(final int Index) {
		Entities.remove(Index);
		
		return Entities.size();

	}
	
	/**
	 * This method removes all the entities stored.
	 */
	public void clearEntities() {
		Entities.clear();
	}
	
	/**
	 * This method returns the full list of set entities.
	 * 
	 * @return	the full list of set entities
	 */
	public List<Pair<String, String>> getEntities() {
		return Entities;
	}
	
	/**
	 * This method performs the HTTP POST request and returns the server response
	 * 
	 * @return	the server response for an HTPP POST request
	 */
	public abstract String execute();
	
	protected String ConnectionURI = null;
	protected List<Pair<String, String>> Entities = new ArrayList<Pair<String,String>>();
}
