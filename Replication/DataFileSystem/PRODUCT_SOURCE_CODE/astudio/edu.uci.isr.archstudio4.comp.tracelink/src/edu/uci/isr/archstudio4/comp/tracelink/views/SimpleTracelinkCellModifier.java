/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.views;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement;

/**
 * @author dpurpura This class implements an ICellModifier An ICellModifier is
 *         called when the user modifes a cell in the tableViewer
 */
public class SimpleTracelinkCellModifier
	implements ICellModifier{

	protected ITracelinkController controller;
	protected Table table;

	public SimpleTracelinkCellModifier(ITracelinkController controller, Table table){
		this.controller = controller;
		this.table = table;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object,
	 *      java.lang.String)
	 */
	public boolean canModify(Object element, String property){
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object,
	 *      java.lang.String)
	 */
	public Object getValue(Object element, String property){
		Object result;
		ITracelinkElement link = (ITracelinkElement)element;

		if(link.hasAttribute(property)){
			result = link.getAttribute(property);
		}
		else{
			result = "";
		}

		/*
		 * try { URL urlLink = new URL(result.toString()); return urlLink; }
		 * catch (MalformedURLException e) { }
		 */

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object,
	 *      java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value){
		if(element instanceof TableItem){
			//FIXME change modify to change based on id
			int index = table.indexOf((TableItem)element);

			ITracelinkElement link = controller.getFilteredElements().get(index);
			String valueString = value.toString().trim();

			if(link.hasAttribute(property)){
				link.setAttribute(property, valueString);
			}

			//TODO allow modify to add nodes/elements

			controller.updateViews();
		}
	}

}
