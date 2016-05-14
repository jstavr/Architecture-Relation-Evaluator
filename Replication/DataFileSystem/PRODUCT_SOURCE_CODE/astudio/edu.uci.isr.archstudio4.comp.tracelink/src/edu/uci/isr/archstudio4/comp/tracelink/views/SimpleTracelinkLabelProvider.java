/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.views;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement;

/**
 * @author David
 */
public class SimpleTracelinkLabelProvider
	extends LabelProvider
	implements ITableLabelProvider{

	protected ITracelinkController controller;

	public SimpleTracelinkLabelProvider(ITracelinkController controller){
		this.controller = controller;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
	 *      int)
	 */
	public Image getColumnImage(Object element, int columnIndex){
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
	 *      int)
	 */
	public String getColumnText(Object element, int columnIndex){
		String text = "";
		if(element instanceof ITracelinkElement){
			ITracelinkElement elem = (ITracelinkElement)element;

			String[] attributes = controller.getDisplayedAttributeNames();

			if(attributes.length <= 0){
				text = "none";
			}
			else{
				String columnName = attributes[columnIndex];

				if(elem.hasAttribute(columnName)){
					text = elem.getAttribute(columnName).toString();
				}
			}
		}

		return text;
	}

}
