package edu.uci.isr.archstudio4.comp.tracelink.analysis;

import java.util.ArrayList;
import java.util.Collection;

import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.IXADLFacade;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITraceEndpointModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceEndpoint;
import edu.uci.isr.archstudio4.comp.tracelink.models.TracelinkModel;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;

public class LinkDiff
	extends AbstractMyxSimpleBrick
	implements ILinkDiff{

	public static final IMyxName INTERFACE_NAME_OUT_READTRACELINKS = MyxUtils.createName("readtracelinks");
	public static final IMyxName INTERFACE_NAME_IN_GETDIFF = MyxUtils.createName("getdiff");

	MyxRegistry myxr = MyxRegistry.getSharedInstance();
	ITracelinkController tracelinkController;
	protected IXADLFacade xadlFacade;

	
	public void begin(){

		//tracelinkController = (ITracelinkController) MyxUtils.getFirstRequiredServiceObject(
		//		this, INTERFACE_NAME_OUT_READTRACELINKS);

		xadlFacade = (IXADLFacade)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_READTRACELINKS);

		myxr.register(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.myx.fw.IMyxProvidedServiceProvider#getServiceObject(edu.uci.isr.myx.fw.IMyxName)
	 */
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_GETDIFF)){
			return this;
		}
		else{
			return null;
		}
	}

	/**
	 * Returns the list of tracelinks in links2 that are absent in links1
	 * 
	 * @param links1
	 *            the current IArchTraceLinks
	 * @param links2
	 *            the IArchTraceLink to get ITraceLinks from
	 * @return list of with the missing tracelinks in links1
	 */
	public TracelinkModel[] getDiff(Collection<ITracelinkModel> links1, Collection<ITracelinkModel> links2){
		// TODO Auto-generated method stub
		System.out.println("LinkDiff*******************");
		System.out.println("connected to : " + tracelinkController.toString());

		ArrayList<ITracelinkModel> links = findMissingTraceLinks(links1, links2);
		return links.toArray(new TracelinkModel[links.size()]);
	}

	/**
	 * Returns the list of tracelinks in links2 that are absent in links1 For
	 * each tracelink, check if there is a matching tracelinkRelationship:
	 * <ol>
	 * <li> Yes: Check if the traceEndpoint locations are the same. For now, it
	 * is sufficient to treat the location as the "unique identifier" for
	 * endpoint
	 * <ol>
	 * <li> Yes: do nothing. We won't worry about differing values for status or
	 * author for now.</li>
	 * <li> No, add the traceEndpoint, along with the other values (status,
	 * author, etc.) </li>
	 * </ol>
	 * </li>
	 * <li> No: add the entire tracelink object </li>
	 * </ol>
	 * 
	 * @param links1
	 *            the current IArchTraceLinks
	 * @param links2
	 *            the IArchTraceLink to get ITraceLinks from
	 * @return list of with the missing tracelinks in links1
	 */
	private ArrayList<ITracelinkModel> findMissingTraceLinks(Collection<ITracelinkModel> links1, Collection<ITracelinkModel> links2){
		ArrayList<ITracelinkModel> tracelinks = new ArrayList<ITracelinkModel>();

		String relationship1;
		String relationship2;
		boolean hasTraceLink;

		for(ITracelinkModel link2: links2){
			hasTraceLink = false;
			relationship2 = link2.getRelationship();

			for(ITracelinkModel link1: links1){
				relationship1 = link1.getRelationship();

				if(relationship1.equals(relationship2)){
					link1 = addMissingEndpoints(link1, link2);

					hasTraceLink = true;
					break;
				}

			}
			if(!hasTraceLink){
				tracelinks.add(link2);
			}
		}

		return tracelinks;
	}

	/**
	 * Returns the list of endpoints in link2 that are absent in link1
	 * 
	 * @param link1
	 *            the current Tracelink
	 * @param link2
	 *            the Tracelink to get endpoints from
	 * @return list of with the missing endpoints in link1
	 */
	private ArrayList<ITraceEndpointModel> findMissingEndpoints(ITracelinkModel link1, ITracelinkModel link2){
		ArrayList<ITraceEndpointModel> endpoints = new ArrayList<ITraceEndpointModel>();

		boolean hasEndpoint;
		for(ITraceEndpointModel endpoint2: link2.getEndpointList()){
			hasEndpoint = false;

			for(ITraceEndpointModel endpoint1: link1.getEndpointList()){
				if(endpoint1.getLocationHref().equals(endpoint2.getLocationHref())){
					hasEndpoint = true;
					break;
				}
			}

			if(!hasEndpoint){
				endpoints.add(copyof(endpoint2));
			}
		}

		return endpoints;
	}

	/**
	 * Adds any ITraceEndpoints in link2 that are not in link1 to link1. Uses
	 * the endpoint's location href as a common identifier for the Endpoint.
	 * Endpoint id cannot be used because ids are uniquely generated.
	 * 
	 * @param link1
	 *            the Tracelink to add endpoints to
	 * @param link2
	 *            the Tracelink to get endpoints from
	 * @return link1 with the missing endpoints added
	 */
	private ITracelinkModel addMissingEndpoints(ITracelinkModel link1, ITracelinkModel link2){
		boolean hasEndpoint;
		for(ITraceEndpointModel endpoint2: link2.getEndpointList()){
			hasEndpoint = false;

			for(ITraceEndpointModel endpoint1: link1.getEndpointList()){
				if(endpoint1.getLocationHref().equals(endpoint2.getLocationHref())){
					hasEndpoint = true;
					break;
				}
			}

			if(!hasEndpoint){
				//need to make a copy in order to avoid
				//making a "org.w3c.dom.DOMException: WRONG_DOCUMENT_ERR"
				link1.addEndpoint(copyof(endpoint2));
			}
			;
		}

		return link1;
	}

	/**
	 * Creates a new instance of the ITraceEndpoint based on an existing
	 * endpoint
	 * 
	 * @param orig
	 *            the endpoint to copy
	 * @return the copied endpoint
	 */
	public ITraceEndpointModel copyof(ITraceEndpointModel orig){
		ITraceEndpointModel copy = new TraceEndpoint();

		for(String key: orig.getKeys()){
			copy.setAttribute(key, orig.getAttribute(key));
		}

		return copy;
	}

}
