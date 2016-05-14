package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

//FIXME figure out a way to show description/relationship of endpoint's tracelink
public class TraceEndpoint
	implements ITraceEndpointModel{

	protected String author;
	protected String captureMode;
	protected String locationType;
	protected String locationHref;
	protected Date timestamp;
	protected String status;
	protected String relationship;
	protected String description;

	public String getAuthor(){
		return author;
	}

	public void setAuthor(String author){
		this.author = author;
	}

	public String getCaptureMode(){
		return captureMode;
	}

	public void setCaptureMode(String captureMode){
		this.captureMode = captureMode;
	}

	public String getLocationType(){
		return locationType;
	}

	public void setLocationType(String locationType){
		this.locationType = locationType;
	}

	public String getLocationHref(){
		return locationHref;
	}

	//modified to make sure that the href conforms to the URI format
	public void setLocationHref(String locationHref){
		
		//assume that components/connectors don't have slashes
		if ( !( (locationHref.contains("/") || (locationHref.contains("\\"))) ) ) {    
			this.locationHref = locationHref;
		}
		else {
			try {
				if (locationHref.contains(" ")) {
					locationHref = locationHref.replaceAll(" ", "%20");
				}
				URI location = new URI(locationHref);
				this.locationHref = location.toString();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				//try to cast it as a file first
				File locationFile = new File(locationHref);
				URI location = locationFile.toURI();
				this.locationHref = location.toString();
				
			}
						
		}
		

		//}

		
		
	}

	public Date getTimestamp(){
		return timestamp;
	}

	public void setTimestamp(Date timestamp){
		this.timestamp = timestamp;
	}

	public String getStatus(){
		return status;
	}

	public void setStatus(String status){
		this.status = status;
	}
	
	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean isEqual(TraceEndpoint te){
		//just check if they have the same location 
		if(this.locationHref.compareTo(te.locationHref) == 0){
			return true;
		}
		else{
			return false;
		}
	}

	
	public String toString(){
		return locationHref;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String key){
		String result = "";
		try{

			if(key.equals("traceEndpointID")){
				result = getID();
			}
			else if(key.equals("location")){
				result = getLocationHref();
			}
			else if(key.equals("author")){
				result = getAuthor();
			}
			else if(key.equals("timeStamp")){
				result = getTimestamp().toString();
			}
			else if(key.equals("captureMode")){
				result = getCaptureMode();
			}
			else if(key.equals("status")){
				result = getStatus();
			}
			else if(key.equals("relationship")) {
				result = getRelationship();
			}
			else if(key.equals("description")) {
				result = getDescription();
			}
			
		}
		catch(NullPointerException e){

		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getID()
	 */
	public String getID(){
		return getLocationHref();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getKeys()
	 */
	public Set<String> getKeys(){
		Set<String> keys = new LinkedHashSet<String>();
		//keys.add("traceEndpointID");
		keys.add("location");
		keys.add("relationship");
		keys.add("captureMode");
		keys.add("timeStamp");
		keys.add("author");
		keys.add("description");
		keys.add("status");
		return keys;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(String key){
		return getAttribute(key) != null;
	}

	/**
	 * Method checks if the specified key is found in this TraceEndpointModel
	 * 
	 * @param key
	 *            the key to check
	 * @return true if the key is found; false otherwise
	 */
	public boolean hasKey(String key){
		Set<String> allKeys = getKeys();
		for(String aKey: allKeys){
			if(aKey.compareToIgnoreCase(key) == 0){
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#setAttribute(java.lang.String,
	 *      java.lang.String)
	 */
	public void setAttribute(String key, Object value){
		System.err.println("not implemented");
		/*
		 * if (key.equals("traceEndpointID"))
		 * endpoint.setTraceEndpointID(value); else if (key.equals("location"))
		 * endpoint.getLocation().setHref(value); else if (key.equals("status"))
		 * endpoint.getStatus().setValue(value); else if (key.equals("author"))
		 * endpoint.getAuthor().setUsername(value); else if
		 * (key.equals("timeStamp"))
		 * endpoint.getTimeStamp().getDateTime().setValue(value); else if
		 * (key.equals("captureMode"))
		 * endpoint.getCaptureMode().setValue(value); //else if
		 * (key.equals("actionType")) //endpoint.addActionType() //else if
		 * (key.equals("traceLink")) //endpoint.getTraceLink().set
		 */
	}

}
