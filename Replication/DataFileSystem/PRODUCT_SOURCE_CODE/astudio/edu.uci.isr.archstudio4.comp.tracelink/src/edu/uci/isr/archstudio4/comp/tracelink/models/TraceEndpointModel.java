/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.w3c.dom.Element;

import edu.uci.isr.xarch.tracelink.IStatusSimple;
import edu.uci.isr.xarch.tracelink.ITraceEndpoint;
import edu.uci.isr.xarch.tracelink.ITraceLink;



/**
 * @author David
 * FIXME set methods not implemented here
 */
public class TraceEndpointModel implements ITraceEndpointModel {
	
	protected ITraceEndpoint endpoint;
	protected ITraceLink parent;
	
	public TraceEndpointModel(ITraceEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	public TraceEndpointModel(ITraceEndpoint endpoint, ITraceLink parent) {
		this.endpoint = endpoint;
		this.parent = parent;
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getAttribute(java.lang.String)
	 */
	
	public Object getAttribute(String key) {
		String result = "";
		try {
			
			if (key.equals("traceEndpointID"))
				result = endpoint.getTraceEndpointID();
			else if (key.equals("location"))
				result = endpoint.getLocation().getHref();
			else if (key.equals("status"))
				result = endpoint.getStatus().getValue();
			else if (key.equals("author"))
				result = endpoint.getAuthor().getUsername();
			else if (key.equals("timeStamp"))
				result = endpoint.getTimeStamp().getDateTime().getValue();
			else if (key.equals("captureMode"))
				result = endpoint.getCaptureMode().getValue();
			//else if (key.equals("actionType"))
			//result = endpoint.addActionType()
			//TODO remove parent hack; (figure out why endpoint.getTraceLink() is not working
			else if (key.equals("description"))
				result = parent.getDescription().getValue();
			
				//result = endpoint.getTraceLink().getDescription().getValue();
			else if (key.equals("relationship"))
				result = parent.getTraceLinkRelationship().getClassification().getValue();
			
				//result = endpoint.getTraceLink().getTraceLinkRelationship().getClassification().getValue();
		
		}
		catch(NullPointerException e) {
			//System.err.println(endpoint.getTraceLink());
		
			
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getID()
	 */
	
	public String getID() {
		return endpoint.getTraceEndpointID();
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getKeys()
	 */
	
	public Set<String> getKeys() {
		Set<String> keys = new LinkedHashSet<String>();
		//keys.add("traceEndpointID");
		keys.add("location");
		keys.add("relationship");
		keys.add("captureMode");
		keys.add("timeStamp");
		keys.add("author");
		//keys.add("actionType");
		keys.add("description");
		keys.add("status");
		return keys;
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#hasAttribute(java.lang.String)
	 */
	
	public boolean hasAttribute(String key) {
		return getAttribute(key) != null;
	}
	
	public ITraceLink getParent() {
		return parent;
	}

	public void setParent(ITraceLink parent) {
		this.parent = parent;
	}

	/**
	 * Method checks if the specified key is found in this TraceEndpointModel
	 * @param key the key to check
	 * @return true if the key is found; false otherwise
	 */
	public boolean hasKey(String key) {
		Set<String> allKeys = getKeys();
		for (String aKey: allKeys) {
			if (aKey.compareToIgnoreCase(key) == 0) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#setAttribute(java.lang.String, java.lang.String)
	 * TODO implement this
	 */
	
	public void setAttribute(String key, Object value) {
		/*
		 * if (key.equals("traceEndpointID"))
			endpoint.setTraceEndpointID(value);
		else if (key.equals("location"))
			endpoint.getLocation().setHref(value);
		else if (key.equals("status"))
			endpoint.getStatus().setValue(value);
		else if (key.equals("author"))
			endpoint.getAuthor().setUsername(value);
		else if (key.equals("timeStamp"))
			endpoint.getTimeStamp().getDateTime().setValue(value);
		else if (key.equals("captureMode"))
			endpoint.getCaptureMode().setValue(value);
		//else if (key.equals("actionType"))
		//endpoint.addActionType()
		//else if (key.equals("traceLink"))
		//endpoint.getTraceLink().set
		 * 
		 */
	}
	
	public ITraceEndpoint getTraceEndpoint() {
		return endpoint;
	}
	
	public String getAuthor() {
		if (endpoint.getAuthor() != null)
			return endpoint.getAuthor().getUsername();
		else
			return "";
	}
	
	public void setAuthor(String author) {
		
	}
	
	public String getCaptureMode() {
		if (endpoint.getCaptureMode() != null)
			return endpoint.getCaptureMode().getValue();
		else
			return "";
	}
	
	public void setCaptureMode(String captureMode) {

	}
	
	public String getLocationType() {
		if (endpoint.getLocation() != null)
			return endpoint.getLocation().getType();
		else
			return "";
	}
	
	public void setLocationType(String locationType) {

	}
	
	public String getLocationHref() {
		if (endpoint.getLocation() != null)
			return endpoint.getLocation().getHref();
		else
			return "";
	}
	
	public void setLocationHref(String locationHref) {
		
	}
	
	public Date getTimestamp() {
		String dateStr = endpoint.getTimeStamp().getDateTime().getValue();
		//TODO: Replace the date to null, because if the parse fails,
		//      it returns the current date and time
		Date date = new Date();
		try {
			date = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return date; 
	}
	
	public void setTimestamp(Date timestamp) {
	
	}
	
	public String getStatus() {
		if (endpoint.getStatus() != null)
			return endpoint.getStatus().getValue();
		else
			return "";
	}
	
	public void setStatus(String status) {
		//tried to do the following, but Element does not seem to have a constructor
		//Element elt = new Element();
		//IStatusSimple iStatus = new StatusSimpleImpl(elt);
		//iStatus.setValue(status);
		
	}
	
	public String getRelationship() {
		if (parent.getTraceLinkRelationship() != null) {
			if (parent.getTraceLinkRelationship().getClassification() != null)
				return parent.getTraceLinkRelationship().getClassification().getValue();
			
		}
		//if it gets here, just return blank
		return "";

	}

	public void setRelationship(String relationship) {
		// TODO Auto-generated method stub
		
	}

	public String getDescription() {
		if (parent.getDescription() != null)
			return parent.getDescription().getValue();
		else 
			return "";
	}

	public void setDescription(String description) {
		// TODO Auto-generated method stub	
	}

	public String toString() {
		return getLocationHref();
	}


	
}
