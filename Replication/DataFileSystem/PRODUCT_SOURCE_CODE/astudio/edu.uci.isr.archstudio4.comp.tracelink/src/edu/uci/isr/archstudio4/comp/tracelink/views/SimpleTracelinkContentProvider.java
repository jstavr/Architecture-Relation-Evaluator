/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.views;

import java.util.Vector;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import edu.uci.isr.archstudio4.comp.tracelink.addtracelinks.RecoverLinkToolDialog;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITraceEndpointModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceEndpoint;

/**
 * @author dpurpura
 *
 */
public class SimpleTracelinkContentProvider implements IStructuredContentProvider {
	
	protected ITracelinkController controller;
	
	public SimpleTracelinkContentProvider(ITracelinkController controller) {
		this.controller = controller;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	
	public Object[] getElements(Object inputElement) {
		TraceEndpoint[] elements =
			new TraceEndpoint[controller.getFilteredElements().size()];
		//H 7/1/08: remove "\" from the elements
		//TODO: find a better way to do this.  Create a way to convert from TraceEndpointModel to 
		//       TraceEndpoint
		//TODO: move the logic of endpoint checking here over to the controller
		
		if (controller.getFilteredElements().size() > 0) {
			String s;
			String temp;
			int i = 0;
			Vector<ITraceEndpointModel> endpoints = controller.getFilteredElements();
			int counter = 0;
			for (ITraceEndpointModel e: endpoints) {
				
				
				//e.setLocationHref(s);
				elements[counter] = new TraceEndpoint();
				if (e.getAuthor() != null)
					elements[counter].setAuthor(e.getAuthor());
				if (e.getCaptureMode() != null)
					elements[counter].setCaptureMode(e.getCaptureMode());
				if (e.getLocationHref() != null) {
					s = e.getLocationHref();
					//before checking for slashes, check first if it has the following keywords
					if (s.contains(RecoverLinkToolDialog.GOOGLE_ALIAS.toLowerCase()))
						s = RecoverLinkToolDialog.GOOGLE_ALIAS + " links";
					//make sure that this is a trac query string
					else if ( (s.contains(RecoverLinkToolDialog.TRAC_ALIAS.toLowerCase())) && (s.contains("query?status=new")))
						s = RecoverLinkToolDialog.TRAC_ALIAS + " links";
					else if (s.contains("#component")) {
						//remove the "#" sign
						temp = controller.getEndpointDesc(s.substring(1));
						s = temp + " Component";
					}
					else if (s.contains("#connector")) {
						temp = controller.getEndpointDesc(s.substring(1));
						s = temp + " Connector";
					}
					
						
					else if (s.contains("\\")) {
						//get the position right after the slash, or get the whole string if no slash exists
						temp = truncatePath(s, "\\");
						s = temp;
						/*
						i = s.lastIndexOf("\\") + 1;
						//need to check if we are at the end of the string
						if (i == s.length()) {
							temp = s.substring(0, i - 2);
							i = temp.lastIndexOf("\\") + 1;
						}
						*/
							
					}
					else if (s.contains("/")) {
						//i = s.lastIndexOf("/") + 1;
						temp = truncatePath(s, "/");
						s = temp;						
						
					}
					
					//if the path contains "%23", the character equivalent of "#", replace it with a "#"
					//for some reason, it still displays %23
					//for now, just replace it with space
					//make sure to keep the s variable so that the next time the string is modified, the changes are kept
					temp = s.replaceAll("%23", "#");
					s = temp;
					
					
					
					//remove unreadable characters
					temp = s.replaceAll("%20", " "); 
					s = temp;
					//if the path contains "%2520", the character equivalent of a space, replace it with a space
					temp = s.replaceAll("%2520", " ");
					s = temp;
					
					elements[counter].setLocationHref(temp);
				}
				
				if (e.getLocationType() != null) {
					elements[counter].setLocationType(e.getLocationType());
				}
				if (e.getTimestamp() != null) 
						elements[counter].setTimestamp(e.getTimestamp());
				if (e.getStatus() != null) 
					elements[counter].setStatus(e.getStatus());
				if (e.getRelationship() != null)
					elements[counter].setRelationship(e.getRelationship());
					
				counter++;
				//reset i for the next loop
				i = 0;
			
			}
		}

		
		
		return elements;
		
		//return controller.getFilteredElements().toArray(elements);
	}
	
	/*
	 * Method truncates the endpoint location to only display
	 * the last part of the path in order to improve readability on the link table
	 */
	private String truncatePath(String location, String pathSeparator) {
		int last = location.lastIndexOf(pathSeparator) + 1;
		//need to check if we are at the end of the string
		if (last == location.length()) {
			String temp = location.substring(0, last - 2);
			int secondToLast = temp.lastIndexOf(pathSeparator) + 1;
			//only display the path between the last two slashes
			return location.substring(secondToLast, last - 1);
		}
		return location.substring(last);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	
	public void dispose() {
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if ((oldInput instanceof ITraceEndpointModel) 
				&& (newInput instanceof ITraceEndpointModel)) {
			ITraceEndpointModel newLink = (ITraceEndpointModel) newInput;
			ITraceEndpointModel oldLink = (ITraceEndpointModel) oldInput;
			
			int index = controller.indexOf(oldLink.getID()); 
			
			if (index == -1)
				controller.add(newLink);
			else
				//controller.getElements(false).set(index, newLink);
				controller.getElements(false).add(index, newLink);
		}	
	}
	
}
