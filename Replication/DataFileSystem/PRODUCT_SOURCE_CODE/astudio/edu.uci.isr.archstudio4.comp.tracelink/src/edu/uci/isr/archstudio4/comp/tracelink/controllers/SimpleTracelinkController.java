/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.widgets.Table;

import edu.uci.isr.archstudio4.comp.tracelink.models.ITraceEndpointModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceEndpointModel;
import edu.uci.isr.archstudio4.comp.tracelink.views.IWidget;
import edu.uci.isr.archstudio4.comp.tracelink.views.SimpleTracelinkCellModifier;
import edu.uci.isr.archstudio4.comp.tracelink.views.SimpleTracelinkContentProvider;
import edu.uci.isr.archstudio4.comp.tracelink.views.SimpleTracelinkLabelProvider;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xarch.tracelink.IArchTraceLinks;
import edu.uci.isr.xarch.tracelink.ITraceEndpoint;
import edu.uci.isr.xarch.tracelink.ITraceLink;

/**
 * @author dpurpura
 */
public class SimpleTracelinkController
	extends AbstractMyxSimpleBrick
	implements ITracelinkController{

	protected IXADLFacade xadlFacade;

	private String filterKey;
	private String filterProperty;

	private Set<String> attributes;
	private Set<String> filteredAttributes;

	private TracelinkSorter sorter;

	private Vector<ITraceEndpointModel> elements;
	private Vector<ITraceEndpointModel> filteredElements;

	//private ITraceLink tracelink;
	private String endpointHref;

	private Vector<IWidget> viewers;

	//Content Providers/Modifiers
	//private TraceRecorder recorder;
	private ICellModifier cellModifier;
	private IStructuredContentProvider contentProvider;
	private ITableLabelProvider tableLabelProvider;

	/*
	 * public static final IMyxName INTERFACE_NAME_OUT_XARCH =
	 * MyxUtils.createName("xarchcs"); public static final IMyxName
	 * INTERFACE_NAME_IN_FILEEVENTS = MyxUtils.createName("xarchfileevents");
	 * public static final IMyxName INTERFACE_NAME_IN_FLATEVENTS =
	 * MyxUtils.createName("xarchflatevents"); public static final IMyxName
	 * INTERFACE_NAME_IN_ADDTRACELINKS = MyxUtils.createName("addtracelinks");
	 * public static final IMyxName INTERFACE_NAME_IN_READTRACELINKS =
	 * MyxUtils.createName("readtracelinks"); public static final IMyxName
	 * INTERFACE_NAME_IN_EDITTRACELINKS = MyxUtils.createName("edittracelinks");
	 */
	public static final IMyxName INTERFACE_NAME_IN_UPDATEVIEWS = MyxUtils.createName("updateviews");
	public static final IMyxName INTERFACE_NAME_OUT_ACCESSFILE = MyxUtils.createName("accessfile");

	protected MyxRegistry myxr = MyxRegistry.getSharedInstance();

	//protected XArchFlatInterface xArchFlat;

	public SimpleTracelinkController(){
		this(new Vector<ITraceEndpointModel>());
	}

	public SimpleTracelinkController(Vector<ITraceEndpointModel> elements){
		filterKey = "";
		filterProperty = "";

		filteredAttributes = new LinkedHashSet<String>();

		sorter = new TracelinkSorter(this);

		this.elements = elements;
		this.filteredElements = this.elements;

		this.viewers = new Vector<IWidget>();

		this.endpointHref = "";
	}

	@Override
	public void begin(){
		System.out.println("BEGIN TRACELINK_CONTROLLER!!");

		xadlFacade = (IXADLFacade)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_ACCESSFILE);

		myxr.register(this);

		/*
		 * xArchFlat = (XArchFlatInterface)
		 * MyxUtils.getFirstRequiredServiceObject( this,
		 * INTERFACE_NAME_OUT_XARCH); System.out.println("****xArchFlat**** " +
		 * xArchFlat.toString()); xadlFacade = new XADLFacade(xArchFlat);
		 * System.out.println("****xArchFlat--end***" + xadlFacade.toString());
		 */

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.myx.fw.IMyxProvidedServiceProvider#getServiceObject(edu.uci.isr.myx.fw.IMyxName)
	 */

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_UPDATEVIEWS)){
			return this;
		}
		else{
			return null;
		}
	}

	public String[] getAttributeNames(){
		attributes = new LinkedHashSet<String>();

		for(ITraceEndpointModel elem: filteredElements){
			attributes.addAll(elem.getKeys());
		}

		if(filteredAttributes.isEmpty()){
			filteredAttributes = attributes;
		}
		else{
			for(String attribute: filteredAttributes){
				if(!attributes.contains(attribute)){
					filteredAttributes = attributes;
					break;
				}
			}
		}

		//for debugging
		//Iterator elements = attributes.iterator();
		//while (elements.hasNext()) {
		//	System.out.println("get attribute names " + elements.next().toString());
		//}

		return attributes.toArray(new String[attributes.size()]);
	}

	public String[] getDisplayedAttributeNames(){
		return filteredAttributes.toArray(new String[filteredAttributes.size()]);
	}

	public void setDisplayedAttributeNames(String[] attributes){
		filteredAttributes = new LinkedHashSet<String>();
		for(String attribute: attributes){
			filteredAttributes.add(attribute);
		}
	}

	public IStructuredContentProvider getContentProvider(){
		if(contentProvider == null){
			contentProvider = new SimpleTracelinkContentProvider(this);
		}
		return contentProvider;
	}

	public ICellModifier getCellModifier(Table table){
		if(cellModifier == null){
			cellModifier = new SimpleTracelinkCellModifier(this, table);
		}
		return cellModifier;
	}

	public ITableLabelProvider getTableLabelProvider(){
		if(tableLabelProvider == null){
			tableLabelProvider = new SimpleTracelinkLabelProvider(this);
		}
		return tableLabelProvider;
	}

	/*
	 * public Vector<ITracelinkElement> getFilteredElements() { Vector<ITracelinkElement>
	 * elems = new Vector<ITracelinkElement>(); elems.addAll(filteredElements);
	 * return elems; } public Vector<ITracelinkElement> getElements(boolean
	 * isFiltered){ if (isFiltered) return getFilteredElements(); else { Vector<ITracelinkElement>
	 * elems = new Vector<ITracelinkElement>(); elems.addAll(elements); return
	 * elems; } }
	 */

	public Vector<ITraceEndpointModel> getFilteredElements(){

		return filteredElements;
	}

	public Vector<ITraceEndpointModel> getElements(boolean isFiltered){
		if(isFiltered){
			return getFilteredElements();
		}
		else{

			return elements;
		}
	}

	public boolean registerView(IWidget widget){
		return viewers.add(widget);
	}

	public boolean removeView(IWidget widget){
		return viewers.remove(widget);
	}

	public void setFilter(String key, String property){
		filterKey = key;
		filterProperty = property;

		System.err.println("setFilter (key: '" + key + "'; Property: '" + property + "')");

		updateViews();
	}

	@SuppressWarnings("unchecked")
	private Vector<ITraceEndpointModel> filter(Vector<ITraceEndpointModel> element, String key, String property){
		Vector<ITraceEndpointModel> filteredLinks = new Vector<ITraceEndpointModel>();

		if(!key.equals("") && !property.equals("")){
			for(ITraceEndpointModel link: element){
				if(link.hasAttribute(key) && link.getAttribute(key).toString().toLowerCase().contains(property.toLowerCase())){
					filteredLinks.add(link);
					//temp - for debugging
					//System.out.println("filter w/ property location " + link.getLocationHref());
				}
			}
		}
		else{
			Object clone = elements.clone();
			if(clone instanceof Vector){
				filteredLinks = (Vector<ITraceEndpointModel>)clone;
			}

			//temp - for debugging
			//for (ITraceEndpointModel link: element) {
			//	System.out.println("no filter " + link.getTimestamp().toString());
			//}

			//for (ITraceEndpointModel link: filteredLinks) {
			//	System.out.println("filter w/out property " + link.getTimestamp().toString());
			//}

		}

		return filteredLinks;
	}

	public void setLinks(ITracelinkElement[] elements){
		Vector<ITraceEndpointModel> links = new Vector<ITraceEndpointModel>();

		for(ITracelinkElement element: elements){
			if(element instanceof ITraceEndpointModel){
				links.add((ITraceEndpointModel)element);
			}
		}

		setElements(links);
	}

	public void setLinks(Collection<ITracelinkElement> elements){
		Vector<ITraceEndpointModel> links = new Vector<ITraceEndpointModel>();

		for(ITracelinkElement element: elements){
			if(element instanceof ITraceEndpointModel){
				links.add((ITraceEndpointModel)element);
				//temporary - for debugging
				//ITraceEndpointModel endpoint= (ITraceEndpointModel)element;
				//System.out.println("setLinks href " + endpoint.getLocationHref());
			}

		}

		setElements(links);
	}

	private void setElements(Vector<ITraceEndpointModel> elements){
		this.elements = elements;
		String[] attributes = getAttributeNames();

		if(attributes.length > 0){
			setSortCriteria(getAttributeNames()[0]);
			//updateViews(); cycle
		}
	}

	public void setSortCriteria(String attribute){
		sorter.setAttribute(attribute);
		System.err.println("setSortCriteria (attribute: '" + attribute + "')");
		//updateViews(); cycle
	}

	private void sort(Vector<ITraceEndpointModel> links){
		if(links.size() > 1){
			Collections.sort(links, sorter);
		}

		//for debugging
		//for (ITraceEndpointModel alink: links) {
		//	System.out.println("sort " + alink.getLocationHref());
		//}
	}

	public void updateViews(){
		System.err.println("Updating Views");

		IArchTraceLinks tracelinks = null;

		if(xadlFacade != null){
			tracelinks = xadlFacade.getArchTracelinks();
		}
		else{
			System.err.println("XADL Facade is null");
			//xadlFacade = new XADLFacade(xArchFlat);
			//tracelinks = xadlFacade.getArchTracelinks();
		}
		//Hazel temp modify
		//if (tracelinks != null)
		//	setLinks(getRelatedLinks(tracelinks, endpointHref));

		if(tracelinks != null){
			Vector<ITracelinkElement> relatedLinks = getRelatedLinks(tracelinks, endpointHref);

			//temp - for debugging
			//for (ITracelinkElement link: relatedLinks) {
			//	System.out.println("related links " + link.getAttribute("location").toString());
			//}

			setLinks(relatedLinks);
		}

		filteredElements = filter(elements, filterKey, filterProperty);
		sort(filteredElements);

		getAttributeNames(); //load attribute names cache

		//old code:
		//for (IWidget widget : viewers) {
		//	widget.update();
		//}

		for(final IWidget widget: viewers){
			//run a new thread
			SWTWidgetUtils.async(widget.getWidget(), new Runnable(){

				//SWTWidgetUtils.sync(widget.getWidget(), new Runnable() {
				public void run(){
					widget.update();
				}
			});
		}

	}

	public boolean add(ITracelinkElement element){
		if(element instanceof ITraceEndpointModel){
			return elements.add((ITraceEndpointModel)element);
		}
		else{
			return false;
		}
	}

	public int indexOf(String id){
		for(int i = 0; i < elements.size(); i++){
			if(elements.get(i).getID().equals(id)){
				return i;
			}
		}

		return -1;
	}

	public boolean remove(ITracelinkElement element){
		return elements.remove(element);
	}

	public String getClassification(){
		String result = "";

		//this code does not work
		/*
		 * if ((tracelink != null) && (tracelink.getTraceLinkRelationship() !=
		 * null) && (tracelink.getTraceLinkRelationship().getClassification() !=
		 * null)) result +=
		 * tracelink.getTraceLinkRelationship().getClassification().getValue();
		 */
		return result;
	}

	public String getDescription(){
		String result = "";

		//this code does not work
		/*
		 * if ((tracelink != null) && (tracelink.getDescription() != null))
		 * result += tracelink.getDescription().getValue();
		 */
		return result;
	}

	/**
	 * Returns a collection of endpoints that are linked to a given endpoint
	 * FIXME this should be moved to XADLFacade since it uses apigen packages
	 * 
	 * @param links
	 *            the collection of tracelinks
	 * @param endpointId
	 *            the id of the endpoint whose siblings are being search for
	 * @return a collection of endpoints that are linked to a given endpoint
	 */
	private Vector<ITracelinkElement> getRelatedLinks(IArchTraceLinks links, String endpointId){
		Vector<ITracelinkElement> endpoints = new Vector<ITracelinkElement>();

		if(links != null){
			//remember all of the Tracelinks that contain a reference to our endpoint
			ArrayList<ITraceLink> linksToShow = new ArrayList<ITraceLink>();

			Collection<ITraceLink> cTraceLink = links.getAllTraceLinks();
			for(ITraceLink link: cTraceLink){
				Collection<ITraceEndpoint> cTraceEndpoint = link.getAllTraceEndpoints();
				for(ITraceEndpoint endpoint: cTraceEndpoint){

					String href = endpoint.getLocation().getHref();

					//System.err.println("id of selected component: " + endpointHref);
					//System.err.println("id in Tracelink.Endpoint: " + href);

					if(endpointId.equals(href)){
						//String temp = "#" + href;
						//if (endpointId.equals(temp)) {
						linksToShow.add(link);

						break;
					}
				}
			}

			//FIXME store the first tracelink as the "class link"
			//H 9/25/08 - temporarily comment this out
			//if (!linksToShow.isEmpty())
			//	tracelink = linksToShow.get(0);

			//get all of the endpoints from the remembered links
			for(ITraceLink link: linksToShow){
				Collection<ITraceEndpoint> cTraceEndpoint = link.getAllTraceEndpoints();
				for(ITraceEndpoint endpoint: cTraceEndpoint){
					if(!endpointId.equals(endpoint.getLocation().getHref())){
						//FIXME endpoint parent hack below (allows parent to be seen in table)
						endpoints.add(new TraceEndpointModel(endpoint, link));
						System.out.println("~~~getRelatedLinks " + endpoint.getLocation().getHref());
					}

				}
			}
		}
		return endpoints;
	}

	public void setEndpointHref(String endpointHref){
		this.endpointHref = "#" + endpointHref;
	}

	//H: 5/28/08

	public String getEndpointHref(){
		return this.endpointHref;
	}

	public String getEndpointID(){
		return this.endpointHref.substring(1);
	}

	/**
	 * Method returns the description of the endpointHref
	 * 
	 * @param endpointID
	 *            unique identifier of an arch element (i.e. component/connector
	 *            ID
	 * @return description of the arch element
	 */
	public String getEndpointDesc(String endpointID){
		return xadlFacade.getElementDescription(endpointID);
	}

	/*
	 * public ObjRef getXArchRef() { return xadlFacade.getXArchRef(); }
	 */

	public void setXArchRef(String filename){
		xadlFacade.setXArchRef(filename);
	}

	/*
	 * public XArchFlatInterface getXArchFlatInterface() { return xArchFlat; }
	 */

	public IXADLFacade getXADLFacade(){
		return xadlFacade;
	}

	public void exportLinks(String filename){
		// TODO Auto-generated method stub

	}

	public void addLinkContainer(){
		xadlFacade.addArchTracelinks();

	}

	public Boolean hasLinkContainer(){
		return xadlFacade.hasArchTracelinks();

	}

	public String getSelectionID(String selectionObjRef){
		String id = xadlFacade.getID(selectionObjRef);

		return id;

	}

	/**
	 * Method removes a link element from the xADL file
	 * @param traceElement element to delete
	 */
	public void removeElementFromXadl(Object traceElement) {
		xadlFacade.deleteTraceElement(traceElement);
	}
	/*
	 * public void setOpenXADLFile(String filename) { // TODO Auto-generated
	 * method stub }
	 */

	/*
	 * public TraceRecorder getTraceRecorder() { if (recorder == null) recorder =
	 * new TraceRecorder(xadlFacade); return recorder; }
	 */
}
