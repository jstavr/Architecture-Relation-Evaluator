/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import edu.uci.isr.xarch.tracelink.ITraceEndpoint;
import edu.uci.isr.xarch.tracelink.ITraceLink;

/**
 * @author dpurpura
 */
public class TracelinkModel
	implements ITracelinkModel{

	private ITraceLink tracelink;

	public TracelinkModel(ITraceLink tracelink){
		this.tracelink = tracelink;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String key){
		Object result;

		if(key.equalsIgnoreCase("Identifier")){
			result = getID();
		}
		else if(key.equalsIgnoreCase("Description")){
			result = getDescription();
		}
		else if(key.equalsIgnoreCase("TraceLinkRelationship")){
			result = getRelationship();
		}
		else if(key.equalsIgnoreCase("TraceEndpoint")){
			result = getEndpointList();
		}
		else{
			result = null;
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getID()
	 */
	public String getID(){
		return tracelink.getTraceLinkID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getKeys()
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getKeys(){
		Set<String> keys = new LinkedHashSet<String>();
		/*
		 * List properties = tracelink.getTypeMetadata().getPropertyList(); for
		 * (Object data : properties) { if (data instanceof XArchTypeMetadata)
		 * keys.add(((XArchTypeMetadata) data).getTypeName()); }
		 */

		//keys.add("Identifier");
		keys.add("Description");
		keys.add("TraceLinkRelationship");
		keys.add("TraceEndpoint");
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#setAttribute(java.lang.String,
	 *      java.lang.String) TODO finish implementing this
	 */
	public void setAttribute(String key, Object value){
		if(key.equalsIgnoreCase("Identifier")){
			tracelink.setTraceLinkID(value.toString());
		}
		else if(key.equalsIgnoreCase("Description")){
			tracelink.getDescription().setValue(value.toString());
		}
		else if(key.equalsIgnoreCase("TraceLinkRelationship")){
			tracelink.getTraceLinkRelationship().getClassification().setValue(value.toString());
			//else if (key.equalsIgnoreCase("TraceEndpoint"))
			//tracelink.getAllTraceEndpoints().size()).toString();
		}
	}

	public void addEndpoint(ITraceEndpointModel te){
		if(te instanceof TraceEndpointModel){
			tracelink.addTraceEndpoint(((TraceEndpointModel)te).getTraceEndpoint());
		}
	}

	public String getDescription(){
		try{
			return tracelink.getDescription().getValue();
		}
		catch(NullPointerException e){
			return "";
		}
	}

	public String getRelationship(){
		try{
			return tracelink.getTraceLinkRelationship().getClassification().getValue();
		}
		catch(NullPointerException e){
			return "";
		}
	}

	public Vector<ITraceEndpointModel> getEndpointList(){
		Collection<ITraceEndpoint> endpoints = tracelink.getAllTraceEndpoints();
		Vector<ITraceEndpointModel> models = new Vector<ITraceEndpointModel>();

		for(ITraceEndpoint e: endpoints){
			models.add(new TraceEndpointModel(e));
		}

		return models;
	}

	public void setDescription(String description){
		// TODO Auto-generated method stub

	}

	public void setID(String id){
		// TODO Auto-generated method stub

	}

	public void setRelationship(String relationship){
		// TODO Auto-generated method stub

	}
}
