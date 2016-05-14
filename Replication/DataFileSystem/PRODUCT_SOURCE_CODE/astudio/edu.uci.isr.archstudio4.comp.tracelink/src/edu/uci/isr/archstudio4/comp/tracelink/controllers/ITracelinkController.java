/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.controllers;

import java.util.Collection;
import java.util.Vector;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.widgets.Table;

import edu.uci.isr.archstudio4.comp.tracelink.models.ITraceEndpointModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkModel;
import edu.uci.isr.archstudio4.comp.tracelink.views.IWidget;
import edu.uci.isr.xarchflat.ObjRef;

/**
 * @author dpurpura
 *
 *  TODO this component seems to have exploded; this perhaps should be changed to 
 *  multiple components in the future.  Perhaps, two would suffice: one to control 
 *  the GUI and one to manage the collection of Tracelinks
 */
//public interface ITracelinkController extends XArchFileListener, XArchFlatListener{
public interface ITracelinkController {

	
	/**
	 * Registers a widget to be updated to when any changes to the 
	 * filtered links occur
	 * @param widget the view to be updated
	 * @return true if the view is successfully registered, otherwise false.
	 */
	public boolean registerView(IWidget widget);
	
	/**
	 * Stops the widget from being updated when changes to the filtered links occur
	 * @param widget the widget to be removed
	 * @return false if the widget was not registered, otherwise returns true 
	 */
	public boolean removeView(IWidget widget);
	
	/**
	 * Updates the links in the registered views to display the desired links,
	 * filtered, and sorted
	 */
	public void updateViews();
	
	public boolean add(ITracelinkElement element);
	
	public boolean remove(ITracelinkElement element);
	
	/**
	 * Returns the index of the endpoint with the given id within 
	 * getElements/getFiliteredElements
	 * 
	 * @param id  the id of the endpoint 
	 * @return the index of the endpoint
	 */
	public int indexOf(String id);
	
	/**
	 * 
	 * @return the collection of endpoints stored in the controller, filtered 
	 *  according to the class' filter conditions
	 */
	//public Vector<ITracelinkElement> getFilteredElements();
	public Vector<ITraceEndpointModel> getFilteredElements();
	
	
	/**
	 * Returns the list of the ITableElements
	 * @param isFiltered if true returns the list with the filter applied; 
	 * 		otherwise returns all of the Elements
	 * @return the list of ITableElements
	 */
	//public Vector<ITracelinkElement> getElements(boolean isFiltered);
	public Vector<ITraceEndpointModel> getElements(boolean isFiltered);
	
	/**
	 * Sets the controller's endpoints to the array of endpoints provided
	 * @param elements  the endpoints to add to the collection
	 */
	public void setLinks(ITracelinkElement[] elements);
	
	/**
	 * Sets the controller's endpoints to those provided by the collection
	 * @param elements
	 */
	public void setLinks(Collection<ITracelinkElement> elements);
		
	/**
	 * Sets the href for the Endpoint currently being displayed in the views
	 * @param endpointHref the href of the Endpoint's location 
	 */
	public void setEndpointHref(String endpointHref);
	
	/**
	 * Method returns the endpoint href, which is in the form "#<endpointID>"
	 */
	public String getEndpointHref();
	
	/**
	 * Method returns the endpoint ID of the selected element in Archipelago
	 * @return unique identifier of the selected element
	 */
	public String getEndpointID();
	
	/**
	 * Method returns the description of the endpointHref
	 * @param endpointID unique identifier of an arch element (i.e. component/connector ID
	 * @return description of the arch element
	 */
	public String getEndpointDesc(String endpointID);
	
	public String[] getAttributeNames();
	
	public String[] getDisplayedAttributeNames();
	
	public String getDescription();
	
	public String getClassification();
	
	public IStructuredContentProvider getContentProvider();
	
	public ICellModifier getCellModifier(Table table);
	
	public ITableLabelProvider getTableLabelProvider();
	
	public void setFilter(String key, String property);
	
	
	public void setSortCriteria(String attribute);
	
	public void setDisplayedAttributeNames(String[] attributes);
	
	
	//public void setXArchRef(ObjRef xArchFlat);
	//public ObjRef getXArchRef();
	public IXADLFacade getXADLFacade();
	public void setXArchRef(String filename);
	public void exportLinks(String filename);
	public Boolean hasLinkContainer();
	public void addLinkContainer();
	
	/**
	 * Method returns the unique id that corresponds to the selected object reference
	 * in Archipelago
	 * @param selectionObjRef the reference to the object selected, in the format
	 * 		[ObjRef[objref1111]]
	 * @return corresponding unique id
	 */
	public String getSelectionID(String selectionObjRef);
	
	/**
	 * Method removes the trace element from the xADL
	 * @param traceElement element to remove
	 */
	public void removeElementFromXadl(Object traceElement);
	
}