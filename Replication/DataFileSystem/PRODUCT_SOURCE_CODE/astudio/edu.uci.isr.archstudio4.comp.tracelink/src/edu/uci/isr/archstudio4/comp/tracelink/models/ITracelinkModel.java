/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.util.Vector;


/**
 * @author dpurpura
 *
 */
public interface ITracelinkModel extends ITracelinkElement {

	public void addEndpoint(ITraceEndpointModel te);

	public String getDescription();
	
	public void setDescription(String description);
	
	public void setID(String id);
	
	public String getID();
	
	public String getRelationship();
	
	public void setRelationship(String relationship);

	public Vector<ITraceEndpointModel> getEndpointList();

}
