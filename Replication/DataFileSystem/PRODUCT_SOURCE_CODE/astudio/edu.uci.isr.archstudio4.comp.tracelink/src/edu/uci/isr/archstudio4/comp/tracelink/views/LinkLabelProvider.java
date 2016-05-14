/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement;

	/**
	 * Label provider for the TableViewer
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider 
	 * @author David A. Purpura
	 */
public class LinkLabelProvider 
	extends LabelProvider implements ITableLabelProvider {
		
		// Names of images used to represent checkboxes
		public static final String CHECKED_IMAGE 	= "checkmark";
		public static final String UNCHECKED_IMAGE  = "x";
		
		private ITracelinkController controller;
		
		public LinkLabelProvider(ITracelinkController controller) {
			this.controller = controller;
		}
		
		/**
		 * For the checkbox images
		 * 
		 * Note: An image registry owns all of the image objects registered with it,
		 * and automatically disposes of them when the SWT Display is disposed.
		 */ 
		private ImageRegistry imageRegistry = new ImageRegistry();
		
		public LinkLabelProvider() {
			String iconPath = "../../res/img/"; 
			imageRegistry.put(CHECKED_IMAGE, 
					ImageDescriptor.createFromFile(LinkTableView.class,
							iconPath + CHECKED_IMAGE + ".bmp"));
			imageRegistry.put(UNCHECKED_IMAGE,
					ImageDescriptor.createFromFile(LinkTableView.class,
							iconPath + UNCHECKED_IMAGE + ".bmp"));	
		}
		
		/**
		 * Returns the image with the given key, or <code>null</code> if not found.
		 */
		private Image getImage(boolean isQualityLink) {
			String key = (isQualityLink)? CHECKED_IMAGE : UNCHECKED_IMAGE;
			return  imageRegistry.get(key);
		}
		
		/**
		 * @see org.eclipse.jface.viewers.
		 * ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			ITracelinkElement link = (ITracelinkElement) element;
			String text;
			
			String columnName = controller.getAttributeNames()[columnIndex];
			
			//TODO determine way to not display text for quality
			if ((link.hasAttribute(columnName)) 
					&& (!columnName.equals("quality")))
				text = link.getAttribute(columnName).toString();
			else
				text = "";
			
			return text;
		}
		
		/**
		 * @see org.eclipse.jface.viewers.
		 * ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			ITracelinkElement link = (ITracelinkElement) element;
			
			String columnName = controller.getAttributeNames()[columnIndex];
			
			//TODO determine way to display image for quality
			if ((columnName.equalsIgnoreCase("quality")) 
					&& (link.hasAttribute(columnName))) {
				int quality = Integer.parseInt(link.getAttribute(columnName).toString());
				
				return getImage(quality >= 0);
				
			}
			else {
				return null;
			}
		}
		
	}
	

