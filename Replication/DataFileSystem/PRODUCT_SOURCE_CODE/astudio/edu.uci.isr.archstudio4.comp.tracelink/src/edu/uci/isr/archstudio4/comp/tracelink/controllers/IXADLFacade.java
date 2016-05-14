/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.controllers;

import java.util.Collection;

import edu.uci.isr.archstudio4.comp.tracelink.models.ITraceEndpointModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkModel;
import edu.uci.isr.xarch.tracelink.IArchTraceLinks;
import edu.uci.isr.xarchflat.ObjRef;

/**
 * @author Hazel
 *
 */
public interface IXADLFacade {
	/*
	 * The following are exposed myx interfaces that other components can use
	 */
	
	//public void addTraceLinks(ITracelinkElement linkElement);
	public void addTraceLinks(ITracelinkModel linkElement);
	public Collection<ITracelinkModel> editTraceLinks();
	
	/**
	 * @return the collection of TraceEndpoints
	 */
	public Collection<ITraceEndpointModel> readTraceLinks();
	
	public void accessFile();
	
	/**
	 * Specify the open xADL file
	 * 
	 */
	//public ObjRef setXArchRef(String filename);
	public void setXArchRef(String filename);
	
	public String getElementDescription(String elementID);
	public String getID(ObjRef objRef);
	
	/**
	 * Method returns the unique id that corresponds to the selected object reference
	 * in Archipelago
	 * @param selectedItem the reference to the object selected, in the format
	 * 		[ObjRef[objref1111]]
	 * @return corresponding unique id
	 */
	public String getID(String selectedItem);
	
	/**
	 * Write the xADL to the specified filename
	 * @param filename
	 */
	public void writeXArchToFile(String filename);
	public IArchTraceLinks getArchTracelinks();
	public void serializeTracelink(ITracelinkModel tracelink);
	public Collection<ITracelinkModel> getTracelinks();
	
	/**
	 * Method updates a trace element
	 * @param traceElement element to be updated, either TraceLink or TraceEndpoint
	 * @param key the attribute or child node to update
	 * @param property the new value for the key
	 */
	public void updateTraceElement(Object traceElement, String key, String property);

	/**
	 * Method deletes the trace element from the xADL
	 * @param traceElement element to delete
	 */
	public void deleteTraceElement(Object traceElement); 
	
	public Boolean hasArchTracelinks();
	public void addArchTracelinks();
}
