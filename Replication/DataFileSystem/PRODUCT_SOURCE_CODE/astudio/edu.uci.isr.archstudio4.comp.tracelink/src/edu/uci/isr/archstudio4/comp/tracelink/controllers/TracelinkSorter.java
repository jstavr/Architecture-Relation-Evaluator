/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.controllers;

import java.util.Comparator;

import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement;

/**
 * @author dpurpura
 *
 */
/**
 * Sorter for the SimpleTracelinkController that displays items of type 
 */
public class TracelinkSorter implements Comparator<ITracelinkElement> {
	
	protected String attribute;
	protected boolean isAscendingOrder;
	protected ITracelinkController controller;
	
	
	
	/**
	 * Default constructor TracelinkSorter, sorts links by first attribute
	 */
	public TracelinkSorter(ITracelinkController controller) {
		this(controller, "");
	}
	
	
	/**
	 * Creates a resource sorter that will use the given sort criteria.
	 *
	 * @param attribute the attribute by which to sort
	 */
	public TracelinkSorter(ITracelinkController controller, String attribute) {
		super();
		this.controller = controller;
		this.attribute = attribute;
		this.isAscendingOrder = true;
	}
	
	
	
	public void setAttribute(String attribute) {
		boolean isAscendingOrder = true;
		
		//FIXME fix ascending/descending sort
		
		//if (this.attribute.equals(attribute))
		//	isAscendingOrder = !this.isAscendingOrder;
		
		setAttribute(attribute, isAscendingOrder);
	}
	
	public void setAttribute(String attribute, boolean isAscendingOrder) {
		this.attribute = attribute;
		this.isAscendingOrder = isAscendingOrder;
	}
	
	
	/**
	 * Returns a number reflecting the collation order of the given links.
	 *
	 * @param link1 the first ITracelinkElement to be ordered
	 * @param link2 the second ITracelinkElement to be ordered
	 * @return a negative number if the first element is less  than the 
	 *  second element; the value <code>0</code> if the first element is
	 *  equal to the second element; and a positive number if the first
	 *  element is greater than the second element
	 */
	public int compare(ITracelinkElement link1, ITracelinkElement link2) {
		int result;
		
		if ((attribute == null) || (attribute.equals("")))
			setAttribute(controller.getAttributeNames()[0], true);
		
		String attribute1 = link1.getAttribute(attribute).toString();
		String attribute2 = link2.getAttribute(attribute).toString();
		
		if ((attribute1 != null) && (attribute2 != null)) {
			result = attribute1.compareTo(attribute2);
		}
		else
			result =  -1;
		
		
		if (isAscendingOrder) // sort in ascending or decending order
			return result;
		else
			return result * -1;
	}
	

	
}
