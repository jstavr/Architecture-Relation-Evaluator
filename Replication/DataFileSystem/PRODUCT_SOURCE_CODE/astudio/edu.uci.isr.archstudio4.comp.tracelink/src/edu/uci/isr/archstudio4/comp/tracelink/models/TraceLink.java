package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

public class TraceLink
	implements ITracelinkModel{

	protected String description;
	protected String id;
	protected String relationship;
	protected Vector<ITraceEndpointModel> endpointList;

	protected Boolean hasArchElement; //this is a kludge...will go away when we have rules in place

	public TraceLink(){
		endpointList = new Vector<ITraceEndpointModel>();
		hasArchElement = false;
		id = "";
		description = "";
		relationship = "";
	}

	public void addEndpoint(ITraceEndpointModel te){
		endpointList.add(te);
	}

	public void addAllEndpoints(Collection<ITraceEndpointModel> endpoints){
		endpointList.addAll(endpoints);
	}

	public int numOfEndpoints(){
		return endpointList.size();
	}

	public TraceEndpoint getLastEndpoint(){
		int lastItem = endpointList.size();
		if(lastItem == 0){
			return null;
		}
		else{
			lastItem = lastItem - 1;
			TraceEndpoint te = (TraceEndpoint)endpointList.get(lastItem);
			return te;
		}
	}

	//TODO: kludge...delete later
	public void setHasArchElement(Boolean value){
		hasArchElement = value;
	}

	public Boolean getHasArchElement(){
		/*
		 * if (endpointList.size() > 0) { return true; } else { return false; }
		 */
		return hasArchElement;
	}

	public String getDescription(){
		return description;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getID(){
		return id;
	}

	public void setID(String id){
		this.id = id;
	}

	public String getRelationship(){
		return relationship;
	}

	public void setRelationship(String relationship){
		this.relationship = relationship;
	}

	public Vector<ITraceEndpointModel> getEndpointList(){
		return endpointList;
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
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getKeys()
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getKeys(){
		Set<String> keys = new LinkedHashSet<String>();

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
	 *      java.lang.String)
	 */
	public void setAttribute(String key, Object value){
		if(key.equalsIgnoreCase("Identifier")){
			setID(value.toString());
		}
		else if(key.equalsIgnoreCase("Description")){
			setDescription(value.toString());
		}
		else if(key.equalsIgnoreCase("TraceLinkRelationship")){
			setRelationship(value.toString());
			//else if (key.equalsIgnoreCase("TraceEndpoint"))
			//tracelink.getAllTraceEndpoints().size()).toString();
		}
	}

}
