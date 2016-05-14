/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.addtracelinks;

import java.util.Date;

import org.eclipse.swt.widgets.Shell;

import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.IXADLFacade;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceEndpoint;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceLink;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;

/**
 * @author Hazel
 *
 */
public class ManualLinkView extends AbstractMyxSimpleBrick implements IManualLinkView {

	public static final IMyxName INTERFACE_NAME_OUT_ADDTRACELINKS = 
		MyxUtils.createName("addtracelinks");
	public static final IMyxName INTERFACE_NAME_IN_INVOKEMANUALVIEW =
		MyxUtils.createName("invokemanualview");
	
	MyxRegistry myxr = MyxRegistry.getSharedInstance();
	//ITracelinkController tracelinkController;
	protected IXADLFacade xadlFacade;
	
	
	public void begin() {

		//tracelinkController = (ITracelinkController) MyxUtils.getFirstRequiredServiceObject(
		//		this, INTERFACE_NAME_OUT_ADDTRACELINKS);
		xadlFacade = (IXADLFacade) MyxUtils.getFirstRequiredServiceObject(
				this, INTERFACE_NAME_OUT_ADDTRACELINKS);

		myxr.register(this);
		
	}
	/* (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxProvidedServiceProvider#getServiceObject(edu.uci.isr.myx.fw.IMyxName)
	 */
	
	public Object getServiceObject(IMyxName interfaceName) {
		if(interfaceName.equals(INTERFACE_NAME_IN_INVOKEMANUALVIEW)){
			return this;
		}
		else
			return null;
	}
	
	
	public void invokeManualView(Shell shell, String archSelectedID) {
		
		System.out.println("ManualLinkView*******************");
		System.out.println("connected to : " + xadlFacade.toString());
		
		
		ManualLinkInputDialog dialog = new ManualLinkInputDialog(shell);
		String endpoint = dialog.open();
		endpoint = endpoint.trim();
		System.out.println("Adding endpoint " + endpoint);
		if (endpoint.compareTo("") != 0) {
			//create a trace link
			TraceLink link = new TraceLink();
		    link.setDescription("Manual links");
		    link.setRelationship("unknown");
	    	
		    TraceEndpoint te = new TraceEndpoint();
	    	te.setCaptureMode("manual");
	    	te.setAuthor(System.getProperty("user.name"));
	    	te.setLocationType("simple");
	    	//te.setLocationHref(tracelinkController.getEndpointHref());
	    	te.setLocationHref("#" + archSelectedID);
	    	Date timestamp = new Date();
	    	te.setTimestamp(timestamp);
	    	link.addEndpoint(te);
			
		    TraceEndpoint te2 = new TraceEndpoint();
		    te2.setCaptureMode("manual");
		    te2.setAuthor(System.getProperty("user.name"));
		    te2.setLocationType("simple");
		    te2.setLocationHref(endpoint);
	    	timestamp = new Date();
	    	te2.setTimestamp(timestamp);
	    	link.addEndpoint(te2);
			
	    	xadlFacade.addTraceLinks(link);
		}
		
	}
	

	

}
