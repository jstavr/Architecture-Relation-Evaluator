/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.util.Date;

import org.eclipse.jface.viewers.ISelection;

/**
 * @author Hazel
 */
public class SelectionModel
	implements ISelectionModel, Comparable<SelectionModel>{

	/**
	 * Auto-generated UID
	 */
	private static final long serialVersionUID = -745524925823931101L;

	protected String view;
	protected String element; //name of element selected (i.e. filename, id in the xADL doc)
	protected Date timeStamp;
	protected String property;
	protected ISelection selectedItem;
	protected Integer groupNum;
	protected String relationship;

	/**
	 * Default constructor required for XMLSerializer
	 */
	public SelectionModel(){

	}

	//public SelectionModel(String view, String element, Date timeStamp, Integer groupNum, String relationship) {
	public SelectionModel(String view, String element, Date timeStamp){
		this.view = view;
		this.element = element;
		this.timeStamp = timeStamp;
		this.groupNum = 1;
		this.relationship = "unknown";
	}

	public String getView(){
		return view;
	}

	public void setView(String view){
		this.view = view;
	}

	public String getElement(){
		return element;
	}

	//This is private because the value is obtained from the selectedItem attribute

	public void setElement(String element){

		this.element = element;
	}

	public Date getTimeStamp(){
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp){
		this.timeStamp = timeStamp;
	}

	public String getProperty(){
		return property;
	}

	public void setProperty(String property){
		this.property = property;
	}

	//TODO: add code to extract the data from the ISelection object
	public ISelection getSelectedItem(){
		return selectedItem;
	}

	//TODO: fix the value for element
	public void setSelectedItem(ISelection selectedItem){
		this.selectedItem = selectedItem;
		//element = selectedItem.toString();
		//processSelection();
	}

	public Integer getGroupNum(){
		return groupNum;
	}

	public void setGroupNum(Integer groupNum){
		this.groupNum = groupNum;
	}

	public String getRelationship(){
		return relationship;
	}

	public void setRelationship(String relationship){
		this.relationship = relationship;
	}

	/**
	 * Method compares two selection models based on the selection's timestamp
	 * comparison
	 */
	public int compareTo(SelectionModel aSelection){

		return timeStamp.compareTo(aSelection.getTimeStamp());

	}

	//add the following later?
	public void processSelection(){
		/*
		 * String location = ""; if (selectedItem instanceof
		 * IStructuredSelection) { IStructuredSelection ss =
		 * (IStructuredSelection) selectedItem;
		 * //currTracelink.addAllEndpoints(createEndpoints(ss, filename)); }
		 * else if (selectedItem instanceof ITextSelection) { ITextSelection ts =
		 * (ITextSelection) selectedItem; location += ts.getText();
		 * //currTracelink.addEndpoint(createEndpoint(location)); } else if
		 * (selectedItem instanceof IMarkSelection) { IMarkSelection ms =
		 * (IMarkSelection) selectedItem; try { location +=
		 * ms.getDocument().get(ms.getOffset(), ms.getLength()); } catch
		 * (BadLocationException ble) { System.err.println("TraceRecorder: Bad
		 * Location Exception: " + ms.getDocument()); }
		 * //currTracelink.addEndpoint(createEndpoint(location)); }
		 */

	}

	/*
	 * private Collection<String> getLocations(ISelectionModel selection) {
	 * //don't need this anymore, this is handledn now by rules //if
	 * (existsEndpoint(location)) // return null; ITraceEndpointModel endpoint =
	 * new TraceEndpoint(); String location = new String(); Vector<String>
	 * locationList = new Vector<String>(); ISelection selectedItem =
	 * selection.getSelectedItem(); if (selectedItem instanceof
	 * IStructuredSelection) { IStructuredSelection ss = (IStructuredSelection)
	 * selectedItem; System.out.println("IStructuredSelection: " +
	 * ss.toString()); locationList = getLocationList(ss); } else if
	 * (selectedItem instanceof ITextSelection) { ITextSelection ts =
	 * (ITextSelection) selectedItem; location += ts.getText();
	 * locationList.add(location);
	 * //currTracelink.addEndpoint(createEndpoint(location)); } else if
	 * (selectedItem instanceof IMarkSelection) { IMarkSelection ms =
	 * (IMarkSelection) selectedItem; try { location +=
	 * ms.getDocument().get(ms.getOffset(), ms.getLength());
	 * locationList.add(location); } catch (BadLocationException ble) {
	 * System.err.println("TraceRecorder: Bad Location Exception: " +
	 * ms.getDocument()); } } return locationList; } private Vector<String>
	 * getLocationList(IStructuredSelection ss) { Vector<String> locationList =
	 * new Vector<String>(); Iterator<?> iterator = ss.iterator(); Object obj;
	 * String location = ""; //ITraceEndpointModel endpoint; while
	 * (iterator.hasNext()) { obj = iterator.next();
	 * //System.out.println("selection item event" + o.getClass().getName()); if
	 * (obj instanceof File) { location = ((File)
	 * obj).getFullPath().toPortableString(); } else if (obj instanceof ObjRef) {
	 * location = "#" + xADLFacade.getID((ObjRef) obj); } //endpoint =
	 * createEndpoint(location); //if (endpoint != null) //
	 * endpoints.add(endpoint); locationList.add(location); } return
	 * locationList; }
	 */
}
