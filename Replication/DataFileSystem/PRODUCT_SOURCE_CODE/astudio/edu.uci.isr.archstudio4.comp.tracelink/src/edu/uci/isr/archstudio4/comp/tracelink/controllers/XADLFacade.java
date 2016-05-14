/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import edu.uci.isr.archstudio4.comp.tracelink.models.ITraceEndpointModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceEndpointModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceLink;
import edu.uci.isr.archstudio4.comp.tracelink.models.TracelinkModel;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarch.IXArch;
import edu.uci.isr.xarch.IXArchImplementation;
import edu.uci.isr.xarch.tracelink.ArchTraceLinksImpl;
import edu.uci.isr.xarch.tracelink.IArchTraceLinks;
import edu.uci.isr.xarch.tracelink.ITraceEndpoint;
import edu.uci.isr.xarch.tracelink.ITraceLink;
import edu.uci.isr.xarch.tracelink.TraceLinkImpl;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;
import edu.uci.isr.xarchflat.proxy.XArchFlatProxyUtils;

/**
 * @author dpurpura This class provides easy access to Tracelink objects within
 *         xADL. It follows the Facade design pattern All interactions with
 *         xADL/Archstudio should be done through this class
 */
public class XADLFacade
	extends AbstractMyxSimpleBrick
	implements IXADLFacade, XArchFileListener, XArchFlatListener{

	private XArchFlatInterface xArchFlat;
	private IXArchImplementation xArchImplementation;
	private ObjRef xArchRef;
	//refers to the top level element in xADL
	//private IXArch xArch;			
	protected MyxRegistry myxr = MyxRegistry.getSharedInstance();

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarchcs");
	public static final IMyxName INTERFACE_NAME_IN_FILEEVENTS = MyxUtils.createName("xarchfileevents");
	public static final IMyxName INTERFACE_NAME_IN_FLATEVENTS = MyxUtils.createName("xarchflatevents");
	public static final IMyxName INTERFACE_NAME_IN_ADDTRACELINKS = MyxUtils.createName("addtracelinks");
	public static final IMyxName INTERFACE_NAME_IN_READTRACELINKS = MyxUtils.createName("readtracelinks");
	public static final IMyxName INTERFACE_NAME_IN_EDITTRACELINKS = MyxUtils.createName("edittracelinks");
	public static final IMyxName INTERFACE_NAME_IN_ACCESSFILE = MyxUtils.createName("accessfile");

	//public XADLFacade(XArchFlatInterface xArchFlat) {
	public XADLFacade(){

		//this.xArchFlat = xArchFlat;
		/*
		 * moved the following to the begin method if (xArchFlat == null)
		 * System.err.println("xArchFlat is null"); xArchImplementation =
		 * XArchFlatProxyUtils.getXArchImplementation(xArchFlat);
		 */
		//xArch = xArchImplementation.createXArch();
	}

	
	public void begin(){
		System.out.println("BEGIN TRACELINK_CONTROLLER!!");
		myxr.register(this);

		xArchFlat = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_XARCH);
		System.out.println("****xArchFlat**** " + xArchFlat.toString());

		if(xArchFlat == null){
			System.err.println("xArchFlat is null");
		}

		xArchImplementation = XArchFlatProxyUtils.getXArchImplementation(xArchFlat);

		//xadlFacade = new XADLFacade(xArchFlat);
		//System.out.println("****xArchFlat--end***" + xadlFacade.toString());

		//xarch = 
		//	(XArchFlatInterface) MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_XARCH);
		//tracelink = new TraceLinkImpl(xarch);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.myx.fw.IMyxProvidedServiceProvider#getServiceObject(edu.uci.isr.myx.fw.IMyxName)
	 */
	
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_FILEEVENTS)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_FLATEVENTS)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_ADDTRACELINKS)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_READTRACELINKS)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_EDITTRACELINKS)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_ACCESSFILE)){
			return this;
		}
		else{
			return null;
		}
	}

	
	public void handleXArchFileEvent(XArchFileEvent evt){
		for(Object o: myxr.getObjects(this).clone()){
			if(o instanceof XArchFileListener){
				((XArchFileListener)o).handleXArchFileEvent(evt);
			}
		}
	}

	
	public void handleXArchFlatEvent(XArchFlatEvent evt){
		for(Object o: myxr.getObjects(this).clone()){
			if(o instanceof XArchFlatListener){
				((XArchFlatListener)o).handleXArchFlatEvent(evt);
			}
		}
	}

	
	/**
	 * H: 5/28/08 Method serializes tracelink objects to the xADL file
	 */
	public void addTraceLinks(ITracelinkModel tracelink){
		//xadlFacade.serializeTracelink(tracelink);
		serializeTracelink(tracelink);
	}

	/**
	 * Method adds tracelinks in bulk
	 * 
	 * @param tracelinks
	 *            vector of tracelink objects to add
	 */
	//TODO: This has not been tested
	public void addTraceLinks(Collection<TraceLink> tracelinks){
		for(TraceLink t: tracelinks){
			serializeTracelink(t);
		}
	}

	
	public Collection<ITracelinkModel> editTraceLinks(){
		return getTracelinks();
	}

	
	public Collection<ITraceEndpointModel> readTraceLinks(){

		//return elements;
		//TODO: go through the xADL file and read the tracelink items there
		return null;
	}

	public void accessFile(){
		// TODO Auto-generated method stub

	}

	/*
	 *  public void setOpenFile(String filename) { // TODO
	 * Auto-generated method stub xArchFlat.getOpenXArch(filename); }
	 */

	//public void setXArchRef(ObjRef xArchRef) {
	//	
	//	this.xArchRef = xArchRef;
	//}
	//public ObjRef setXArchRef(String filename) {
	public void setXArchRef(String filename){
		//ObjRef xArchRef = xArchFlat.getOpenXArch(filename);
		xArchRef = xArchFlat.getOpenXArch(filename);

		//return xArchRef; 
	}

	public ObjRef getXArchRef(){
		//ObjRef xArchRef = XArchFlatProxyUtils.getObjRef(xArch);
		return xArchRef;
	}

	public void setXArchFlat(XArchFlatInterface xArchFlat){
		this.xArchFlat = xArchFlat;
	}

	public IArchTraceLinks getArchTracelinks(){
		IArchTraceLinks tracelinks;

		ObjRef archTraceLinksRef = getArchTracelinksObjRef();

		tracelinks = (IArchTraceLinks)XArchFlatProxyUtils.proxy(xArchFlat, archTraceLinksRef);

		if(tracelinks == null){
			System.err.println("no tracelinks in TracelinkContext");
		}

		return tracelinks;

	}

	public Collection<ITracelinkModel> getTracelinks(){
		ArrayList<ITracelinkModel> links = new ArrayList<ITracelinkModel>();

		for(ITraceLink tracelink: getArchTracelinks().getAllTraceLinks()){
			links.add(new TracelinkModel(tracelink));
		}

		return links;
	}

	/**
	 * @return the ObjRef to the architecture's ArchTracelinks object
	 */
	public ObjRef getArchTracelinksObjRef(){
		ObjRef xArchRef = getXArchRef();
		ObjRef archTraceLinksRef = null;

		if(xArchRef != null){
			System.err.println("Walking trees in file: " + xArchFlat.getXArchURI(xArchRef));

			ObjRef tracelinkContextRef = xArchFlat.createContext(xArchRef, "tracelink");
			archTraceLinksRef = xArchFlat.getElement(tracelinkContextRef, ArchTraceLinksImpl.XSD_TYPE_NAME, xArchRef);

			if(archTraceLinksRef == null){
				System.err.println("This file does not use tracelinks");
			}

			System.err.println("Tracelinks found... continuing");
		}

		return archTraceLinksRef;
	}

	/**
	 * Method returns the description of the given arch element
	 * 
	 * @param elementID
	 *            unique ID assigned to the arch element
	 * @return description of the arch element
	 */
	public String getElementDescription(String elementID){
		ObjRef xArchRef = getXArchRef();
		ObjRef componentRef = xArchFlat.getByID(xArchRef, elementID);
		String desc = XadlUtils.getDescription(xArchFlat, componentRef);
		System.err.println("[" + elementID + "] Description: " + desc);

		return desc;

	}

	/**
	 * Returns the id of the given ObjRef
	 * 
	 * @param objRef
	 *            the ObjRef
	 * @return the id of the ObjRef
	 */
	public String getID(ObjRef objRef){
		//H: added a null pointer check
		if(XadlUtils.getID(xArchFlat, objRef) == null){
			return null;
		}
		else{
			return XadlUtils.getID(xArchFlat, objRef);
		}
	}

	
	public String getID(String selectedItem){
		//first we need to strip off the unnecessary characters
		int beginIndex = selectedItem.lastIndexOf("[") + 1;
		int lastIndex = selectedItem.indexOf("]");
		if(beginIndex != -1 && lastIndex != -1){
			String modSelectedItem = selectedItem.substring(beginIndex, lastIndex);

			//transform the string to an objref
			ObjRef selectionRef = new ObjRef(modSelectedItem);

			String objId = getID(selectionRef);
			return objId;

		}
		else{
			return null;
		}
	}

	/**
	 * Saves the open XArch document to file
	 * 
	 * @param filename
	 *            the filename to save the XArch to
	 */
	public void writeXArchToFile(String filename){
		//ObjRef xArchRef = getXArchRef();
		//write out changes to the open xADL document
		try{
			xArchFlat.writeToFile(xArchRef, filename); //update the open xADL file
		}
		catch(IOException err){
			System.err.println("error in serializing tracelink to file: " + err.getLocalizedMessage());
		}
	}

	/**
	 * Serializes the Tracelink to the xADL File
	 * 
	 * @param tracelink
	 *            the Tracelink to serialize
	 */
	public void serializeTracelink(ITracelinkModel tracelink){
		ObjRef xArchRef = getXArchRef();

		if(tracelink == null){
			return;
		}

		ObjRef tracelinkContextRef = xArchFlat.createContext(xArchRef, "tracelink");

		ObjRef archTraceLinksRef = xArchFlat.getElement(tracelinkContextRef, ArchTraceLinksImpl.XSD_TYPE_NAME, xArchRef);
		ObjRef tracelinkRef = xArchFlat.create(tracelinkContextRef, TraceLinkImpl.XSD_TYPE_NAME);
		xArchFlat.add(archTraceLinksRef, TraceLinkImpl.XSD_TYPE_NAME, tracelinkRef);
		xArchFlat.set(tracelinkRef, "traceLinkID", UIDGenerator.generateUID("TraceLink"));

		//Create the description element
		ObjRef descriptionRef = xArchFlat.create(tracelinkContextRef, "Description");
		//System.out.println("writing out description" + traceLink.getDescription());
		xArchFlat.set(descriptionRef, "value", tracelink.getDescription());
		xArchFlat.set(tracelinkRef, "Description", descriptionRef);

		serializeEndpoints(tracelink, tracelinkContextRef, tracelinkRef);

		serializeTracelinkRelationship(tracelink, tracelinkContextRef, tracelinkRef);

		//xArchFlat.serialize(xArchRef);
		writeXArchToFile(xArchFlat.getXArchURI(xArchRef));

	}

	/**
	 * Serialize the trace link relationship
	 * 
	 * @param traceLink
	 * @param tracelinkContextRef
	 * @param tracelinkRef
	 */
	private void serializeTracelinkRelationship(ITracelinkModel traceLink, ObjRef tracelinkContextRef, ObjRef tracelinkRef){
		ObjRef tracelinkRelationshipRef = xArchFlat.create(tracelinkContextRef, "TraceLinkRelationship");
		xArchFlat.set(tracelinkRef, "TraceLinkRelationship", tracelinkRelationshipRef);
		ObjRef tracelinkClassDescRef = xArchFlat.create(tracelinkContextRef, "Description");
		xArchFlat.set(tracelinkClassDescRef, "value", traceLink.getRelationship());
		xArchFlat.set(tracelinkRelationshipRef, "Classification", tracelinkClassDescRef);
	}

	/**
	 * @param traceLink
	 * @param tracelinkContextRef
	 * @param tracelinkRef
	 */
	private void serializeEndpoints(ITracelinkModel traceLink, ObjRef tracelinkContextRef, ObjRef tracelinkRef){
		//serialize the endpoints
		for(ITraceEndpointModel endpoint: traceLink.getEndpointList()){
			System.out.println("Serializing endpoint: " + endpoint);

			ObjRef traceEndpointRef = xArchFlat.create(tracelinkContextRef, "TraceEndpoint");
			xArchFlat.set(traceEndpointRef, "traceEndpointID", UIDGenerator.generateUID("TraceEndpoint"));

			ObjRef xmlLinkRef = xArchFlat.create(tracelinkContextRef, "XMLLink");
			xArchFlat.set(xmlLinkRef, "type", endpoint.getLocationType());
			xArchFlat.set(xmlLinkRef, "href", endpoint.getLocationHref());
			xArchFlat.set(traceEndpointRef, "Location", xmlLinkRef);

			ObjRef authorRef = xArchFlat.create(tracelinkContextRef, "Author");
			xArchFlat.set(authorRef, "username", endpoint.getAuthor());
			xArchFlat.set(traceEndpointRef, "Author", authorRef);

			ObjRef timeStampRef = xArchFlat.create(tracelinkContextRef, "TimeStamp");
			ObjRef dateTimeStampRef = xArchFlat.create(tracelinkContextRef, "DateTimeStamp");
			xArchFlat.set(dateTimeStampRef, "value", endpoint.getTimestamp().toString());
			xArchFlat.set(timeStampRef, "DateTime", dateTimeStampRef);
			xArchFlat.set(traceEndpointRef, "TimeStamp", timeStampRef);

			ObjRef captureModeRef = xArchFlat.create(tracelinkContextRef, "CaptureMode");
			xArchFlat.set(captureModeRef, "value", endpoint.getCaptureMode());
			xArchFlat.set(traceEndpointRef, "CaptureMode", captureModeRef);

			xArchFlat.add(tracelinkRef, "TraceEndpoint", traceEndpointRef);

			//xArchFlat.set(traceEndpointRef, "TraceLink", tracelinkRef);
		}
	}

	public void updateTraceElement(Object traceElement, String key, String property){
		ObjRef xArchRef = getXArchRef();
		if(traceElement == null){
			return;
		}

		if(traceElement instanceof ITraceEndpointModel){
			//need to create the context
			ObjRef tracelinkContextRef = xArchFlat.createContext(xArchRef, "tracelink");

			TraceEndpointModel traceEndptModel = (TraceEndpointModel)traceElement;
			ITraceEndpoint traceEndpt = traceEndptModel.getTraceEndpoint();
			ObjRef traceEndpointRef = XArchFlatProxyUtils.getObjRef(traceEndpt);

			ObjRef keyRef;
			if(key.compareToIgnoreCase("status") == 0){
				key = "Status";
				keyRef = xArchFlat.create(tracelinkContextRef, key);
				if(keyRef != null){
					keyRef.toString();
				}
				xArchFlat.set(keyRef, "value", property);
				xArchFlat.set(traceEndpointRef, key, keyRef);

			}
			//System.out.println("trace endpoint model");
		}
		writeXArchToFile(xArchFlat.getXArchURI(xArchRef));
	}
	
	public void deleteTraceElement(Object traceElement) {
		if (traceElement instanceof ITraceEndpointModel) {
			
			TraceEndpointModel traceEndptModel = (TraceEndpointModel)traceElement;
			ITraceEndpoint traceEndpt = traceEndptModel.getTraceEndpoint();
			
			ObjRef traceEndpointRef = XArchFlatProxyUtils.getObjRef(traceEndpt);
			ObjRef parentRef = xArchFlat.getParent(traceEndpointRef);
			xArchFlat.remove(parentRef, "TraceEndpoint", traceEndpointRef);
			
			writeXArchToFile(xArchFlat.getXArchURI(xArchRef));
		}
		
		else if (traceElement instanceof ITracelinkModel) {
			
		}
	}

	public Boolean hasArchTracelinks(){
		ObjRef archTracelinkRef = getArchTracelinksObjRef();
		if(archTracelinkRef == null){
			return false;
		}
		else{
			return true;
		}
	}

	public void addArchTracelinks(){

		if(xArchRef != null){
			System.out.println("add archtracelink " + xArchRef.toString() + " file: " + xArchFlat.getXArchURI(xArchRef));
			IXArch xArch = XArchFlatProxyUtils.proxy(xArchFlat, xArchRef);
			System.out.println("xArch " + xArch.toString());

			//create context 
			ObjRef tracelinkContextRef = xArchFlat.createContext(xArchRef, "tracelink");

			//create the archTraceLink node and add to the root node
			ObjRef archTraceLinksRef = xArchFlat.createElement(tracelinkContextRef, "ArchTraceLinks");
			IArchTraceLinks archTracelinks = XArchFlatProxyUtils.proxy(xArchFlat, archTraceLinksRef);
			xArch.addObject(archTracelinks);

			writeXArchToFile(xArchFlat.getXArchURI(xArchRef));

		}
	}

}
