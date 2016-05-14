package edu.uci.isr.archstudio4.comp.tracelink.addtracelinks;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

//import org.eclipse.core.internal.resources.File;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IMarkSelection;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.IXADLFacade;
import edu.uci.isr.archstudio4.comp.tracelink.models.IMozillaBrowserHistoryModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.ISelectionModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITraceEndpointModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.SelectionModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceEndpoint;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceLink;
import edu.uci.isr.archstudio4.comp.tracelink.models.XMLSerializer;
import edu.uci.isr.archstudio4.comp.tracelink.preferences.PreferencesConstants;
import edu.uci.isr.archstudio4.comp.tracelink.views.MsgBox;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class RecordLinkView extends AbstractMyxSimpleBrick implements IRecordLinkView {

	public static final IMyxName INTERFACE_NAME_OUT_ADDTRACELINKS = 
		MyxUtils.createName("addtracelinks");
	public static final IMyxName INTERFACE_NAME_IN_INVOKERECORDVIEW =
		MyxUtils.createName("invokerecordview");
	
	MyxRegistry myxr = MyxRegistry.getSharedInstance();
	//ITracelinkController tracelinkController;
	protected IXADLFacade xadlFacade;
	
	//temporarily remove
	//protected String selectedEndpointID;
	//protected String selectedEndpointHref;
	
	//private Collection<ISelectionModel> selectionList;
	//private XADLFacade xADLFacade;
	
	
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
		if(interfaceName.equals(INTERFACE_NAME_IN_INVOKERECORDVIEW)){
			return this;
		}
		else
			return null;
	}
	
	
	public void invokeRecordView(Shell shell, Collection<ISelectionModel> selectionList, String archSelectedID) {
		System.out.println("RecordLinkView*******************");
		System.out.println("connected to : " + xadlFacade.toString());
		//this.selectionList = selectionList;
		
		//temporary take this out to see where this is used - does not seem to be used anywhere
		//selectedEndpointHref = archSelectedID;
		//selectedEndpointID = archSelectedID.substring(1);
		
		//for debugging
		System.out.println("---------PRE-PROCESS SELECTION:----------------------- ");
		for (ISelectionModel selection: selectionList) {
			System.out.println(selection.getElement());
		}
		System.out.println("---------end PRE-PROCESS SELECTION:----------------------- ");
		
		processSelections(selectionList);

		//for debugging
		System.out.println("---------POST-PROCESS SELECTION:----------------------- ");
		for (ISelectionModel selection: selectionList) {
			System.out.println(selection.getElement());
		}
		System.out.println("---------end POST-PROCESS SELECTION:----------------------- ");

		//get the visited urls from the browser
		RecordBrowserHistory_Moz3 rbh = new RecordBrowserHistory_Moz3();
		Collection<IMozillaBrowserHistoryModel> visitedURLs = rbh.getBrowserHistory(shell);
		if (visitedURLs != null) {
			Collection<ISelectionModel> mergedList = mergeVisitedURLs(visitedURLs, selectionList);
			
			
			//6/23/09 - apply the rules in the background
			mergedList = transformInBackground(mergedList);
			
			//6/23/09 the following are commented out
			//begin - code for interactive application of rules
			//int result = displayCapturedLinks(shell, mergedList);
			
			//while (result == SWT.YES) {
			//	mergedList = transformCapturedLinks(shell, mergedList);
			//	result = displayCapturedLinks(shell, mergedList);
			//}
			//end - code for interactive application of rules
			
			//add the tracelinks to xADL
			//if (! mergedList.isEmpty())
			//	serializeLinks(mergedList);
			
			//6/23/09 - TODO: write the removal of duplicates as a rule
			Collection<ISelectionModel> newMergedList = new Vector<ISelectionModel>();
			if (! mergedList.isEmpty()) {
				//mark succeeding duplicates
				String prevSelection = "     ";
				for (ISelectionModel aSelection: mergedList) {
					if (prevSelection.contains(aSelection.getElement()))
						//mergedList.remove(aSelection); //error is in this remove file
						aSelection.setElement("_");
					else
						prevSelection = aSelection.getElement();
				}
				
				//copy the correct entries
				for (ISelectionModel aSelection: mergedList) {
					if (! aSelection.getElement().equals("_"))
						newMergedList.add(aSelection);
				}
				
				/*
				//mark preceding duplicates
				prevSelection = "    ";
				ISelectionModel prevSelectedItem = null;
				for (ISelectionModel aSelection: mergedList) {
					if (aSelection.getElement().contains(prevSelection)) {
						//mergedList.remove(prevSelectedItem);
						if (prevSelectedItem != null) {
							
						}
						
					}
					prevSelection = aSelection.getElement();
					prevSelectedItem = aSelection;
				}
				*/				
				
				serializeLinks(newMergedList);  
				
			}
			
			MsgBox mbox = new MsgBox(shell, SWT.OK);
			mbox.displayMsgBox("Finished adding links", "Status");
		}
		else {
			MsgBox mbox = new MsgBox(shell, SWT.CANCEL);
			String message = "Unable to record links to the browser. "; 
			message = message + "\nPlease set the browser path in the Preferences Dialog";
			mbox.displayMsgBox(message, "Record Link");
		}
		
		
	}
	
	/**
	 * Method merges the browser history with the Eclipse selection events.
	 * Removes the extraneous urls in the browser history (i.e. all entries before the first 
	 * Archipelago selection)
	 * 
	 * @param browserHistoryList
	 * @param selectionList
	 */
	private Collection<ISelectionModel> mergeVisitedURLs(Collection<IMozillaBrowserHistoryModel> browserHistoryList, 
			Collection<ISelectionModel> selectionList) {
		
		Vector<ISelectionModel> filteredList = new Vector<ISelectionModel>();
		
		//go through each item in the selectionList and add to the mergedList
		//we're doing this manually (i.e. not using the .toArray method because we also 
		//need to add the items from the browser history 
		SelectionModel[] mergedList = new SelectionModel[selectionList.size()+ browserHistoryList.size()];
		int counter = 0;
		for (ISelectionModel aSelection: selectionList) {
			mergedList[counter] = (SelectionModel)aSelection;
			counter++;
		}
		
		//add all the items in the browserHistory List to the selectionList
		SelectionModel aSelection = new SelectionModel();
		for (IMozillaBrowserHistoryModel anEntry: browserHistoryList) {
			aSelection.setView("Mozilla Firefox");
			aSelection.setElement(anEntry.getUrl());
			aSelection.setTimeStamp(anEntry.getLastVisitDate());
			aSelection.setGroupNum(1);
			aSelection.setRelationship("unknown");
			mergedList[counter] = aSelection;
			counter++;
			aSelection = new SelectionModel();
		}
		
		//sort the list
		Arrays.sort(mergedList);
		

		
		//filter the extraneous urls from the merged list
		//get the first valid position
		int startIndex = 0;
		for (SelectionModel selection: mergedList) {
			if (selection.getView().contains("ArchipelagoEditor"))
				break;
			startIndex++;
		}
		
		//copy valid selections to filteredList
		for (int i = startIndex; i < mergedList.length; i++) {
			filteredList.add(mergedList[i]);
		}
	
		//test if the mergedList is now sorted
		//System.out.println("printing the merged and filtered list ");
		//for (ISelectionModel selection: filteredList) {
		//	System.out.println(selection.getElement() + " " + selection.getTimeStamp());
		//}
		
		return filteredList;

	}
	
	/*
	 * Method extracts the actual path of the endpoints.
	 * This is done before any serialization to XML to make sure that the contents of
	 * ISelection object is extracted.
	 * Right now we assume that there's only one location encapsulated
	 *   within the selection item 
	 * TODO: look into removing this assumption
	 */
	private void processSelections(Collection<ISelectionModel> selectionList) {
		Iterator iterator = selectionList.iterator();
		Iterator i;
		String location = "";
		ISelectionModel selection;
		Collection<String> locationList = new Vector<String>();
		while (iterator.hasNext()) {
			selection = (ISelectionModel)iterator.next();
			locationList = getLocations(selection);
			if (locationList.size()!=0) {
				i = locationList.iterator();
				if (i.hasNext()) {
					location = (String)i.next();
					if (location.length() > 0)
						selection.setElement(location);
				}
						
			}	
		}
	}
	
	/*
	 * Displays the captured endpoints stored in the ISelectionModel
	 * Returns the users response to the query (to transform the link or not)
	 */
	private int displayCapturedLinks(Shell shell, Collection<ISelectionModel> selectionList) {
		//display to the user the endpoints that are captured
		MsgBox mbox = new MsgBox(shell, SWT.YES);
		String endpoints = "Here are the recorded links: ";
		Iterator iterator = selectionList.iterator();
		ISelectionModel selection;
		String anEndpoint = "";
		while (iterator.hasNext()) {
			selection = (ISelectionModel)iterator.next();
			anEndpoint = selection.getElement();
			//if it is a component/connector, use the name instead of id
			if (selection.getElement().charAt(0) == '#')
				//anEndpoint = tracelinkController.getEndpointDesc(selection.getElement().substring(1));
				anEndpoint =xadlFacade.getElementDescription(selection.getElement().substring(1));
			endpoints = endpoints + "\n" + anEndpoint + " " + selection.getGroupNum() + " " + selection.getRelationship();
		}
		int result = mbox.displayMsgBox(endpoints, "Transform the captured links?");		
		
		return result;
	}
	
	/*
	 * Method serializes the captured endpoints into an XML file 
	 * so that the external rules (in XSLT) can be applied to it.
	 * Afterwards, it is deserialized back to a collection of ISelectionModel objects.
	 * Return the new selection model
	 */
	private Collection<ISelectionModel> transformCapturedLinks(Shell shell, Collection<ISelectionModel> selectionList) {
		
		//Use a path within AS dev because users don't need to see this file
		//This has the complete path, including the filename session.xml
		String sessionFilename= System.getProperty(PreferencesConstants.DEFAULT_PROP_RULE_SESSION);
		//if the path is null, fall back to the default
		String path;
		if ( (System.getProperty(PreferencesConstants.PROP_RULE) != null) &&
			 !(System.getProperty(PreferencesConstants.PROP_RULE).equals("")) ){
			path = System.getProperty(PreferencesConstants.PROP_RULE);
		}
		else {
			
			path = PreferencesConstants.getPathOnly(System.getProperty(PreferencesConstants.DEFAULT_PROP_RULE_SESSION));
		}

		System.out.println("input file: " + sessionFilename);
		if (! selectionList.isEmpty()) {
			try {
				XMLSerializer.serializeSelection(sessionFilename, selectionList);
			}
			catch (FileNotFoundException e1) {
				System.err.println("File not found: " + sessionFilename);
				e1.printStackTrace();
			}
		}		
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		fd.setText("Select transformation file");
		fd.setFilterPath(path);
		String[] filterExt = { "*.xsl", "*.*" };
		fd.setFilterExtensions(filterExt);

		String transformFilename = fd.open();
		System.out.println("transform file: " + transformFilename);
		
		String outputFilename = "";
		//transform the the file
		try {
			//runTransform(transformFilename, sessionFilename, System.getProperty("user.home"));
			outputFilename = runTransform(transformFilename, sessionFilename, path);
		}
		catch (TransformerException te) {
			System.err.println("TransformerException: " + te.getMessageAndLocation());
			te.getCause();
		}
		catch (FileNotFoundException fnfe) {
			System.err.println("FileNotFoundException: " + fnfe.getMessage());
		}
		catch (IOException ioe) {
			System.err.println("IO Exception: " + ioe.getMessage());
		}
		finally {
			
		}
		
		//read the transformed file and load into the selectionList	
		try {
			selectionList.clear();
			selectionList = XMLSerializer.deserializeSelection(outputFilename);
		}
		catch (FileNotFoundException e1) {
			System.err.println("File not found: " + sessionFilename);
			e1.printStackTrace();
		}
		
		return selectionList;
	}
	
	/* Added 6/23/09
	 * Method serializes the captured endpoints into an XML file 
	 * so that the external rules (in XSLT) can be applied to it.
	 * Afterwards, it is deserialized back to a collection of ISelectionModel objects.
	 * This is the background version of transformCapturedLinks.
	 * For now, this applies a fixed number of rules
	 * TODO: Let the user choose the rules from the preference window
	 * Return the new selection model
	 */
	private Collection<ISelectionModel> transformInBackground(Collection<ISelectionModel> selectionList) {

		//set the names of the rules
		Vector<String> ruleList = new Vector<String>();
		ruleList.add("removeSelectionsBasedOnTime_Use1.xsl");
		ruleList.add("removeDuplicates_Use1.xsl");				//added 7/31/09
		ruleList.add("assignSelectionGroups_Comp_use2.xsl");
		ruleList.add("determineRelationship_getFiles_use3_LunarLander.xsl");
		ruleList.add("determineRelationship_use4_LunarLander.xsl");
				
		
		//Use a path within AS dev because users don't need to see this file
		//This has the complete path, including the filename session.xml
		String sessionFilename= System.getProperty(PreferencesConstants.DEFAULT_PROP_RULE_SESSION);
		

		
		//if the path is null, fall back to the default
		String path;
		if ( (System.getProperty(PreferencesConstants.PROP_RULE) != null) &&
			 !(System.getProperty(PreferencesConstants.PROP_RULE).equals("")) ){
			path = System.getProperty(PreferencesConstants.PROP_RULE);
		}
		else {
			
			path = PreferencesConstants.getPathOnly(System.getProperty(PreferencesConstants.DEFAULT_PROP_RULE_SESSION));
		}
		
		//go through and apply each rule
		for (String transformFilename: ruleList) {
	
			System.out.println("input file: " + sessionFilename);
			System.out.println("rule file: " + transformFilename);
			if (! selectionList.isEmpty()) {
				try {
					XMLSerializer.serializeSelection(sessionFilename, selectionList);
				}
				catch (FileNotFoundException e1) {
					System.err.println("File not found: " + sessionFilename);
					e1.printStackTrace();
				}
			}	
			
			/*
			FileDialog fd = new FileDialog(shell, SWT.OPEN);
			fd.setText("Select transformation file");
			fd.setFilterPath(path);
			String[] filterExt = { "*.xsl", "*.*" };
			fd.setFilterExtensions(filterExt);
	
			String transformFilename = fd.open();
			System.out.println("transform file: " + transformFilename);
			*/
			
			
			
			String outputFilename = "";
			//transform the the file
			try {
				//runTransform(transformFilename, sessionFilename, System.getProperty("user.home"));
				outputFilename = runTransform(path + transformFilename, sessionFilename, path);
			}
			catch (TransformerException te) {
				System.err.println("TransformerException: " + te.getMessageAndLocation());
				te.getCause();
			}
			catch (FileNotFoundException fnfe) {
				System.err.println("FileNotFoundException: " + fnfe.getMessage());
			}
			catch (IOException ioe) {
				System.err.println("IO Exception: " + ioe.getMessage());
			}
			finally {
				
			}
			
			//read the transformed file and load into the selectionList	
			try {
				selectionList.clear();
				selectionList = XMLSerializer.deserializeSelection(outputFilename);
			}
			catch (FileNotFoundException e1) {
				System.err.println("File not found: " + sessionFilename);
				e1.printStackTrace();
			}
			
		}
		
		return selectionList;
	}

	
	/*
	 * Method applies an XSLT to the serialized selectionList (in XML file)
	 */
	public static String runTransform(String transformFilename, String inputFilename, String inputPath) 
			throws TransformerException, TransformerConfigurationException, 
					FileNotFoundException, IOException {
		
		//TODO: check if the license notice is needed here
		TransformerFactory tFactory = TransformerFactory.newInstance();
		//getting an error in the transform...perhaps because of the empty spaces in the path
		//Transformer transformer = tFactory.newTransformer(new StreamSource(transformFilename));
		
		File transformFile = new File(transformFilename);
		Transformer transformer = tFactory.newTransformer(new StreamSource(transformFile));
		
		
		String outputFilename = inputPath +  "sessionOut.xml";
		transformer.transform(new StreamSource(inputFilename), new StreamResult(new FileOutputStream(outputFilename)));
		
		System.out.println("************* The result is in " + outputFilename);		
		return outputFilename;
	}
	
	/*
	 * Method serializes the links to the xADL file
	 */
	public void serializeLinks(Collection<ISelectionModel> selectionList) {
		TraceLink tracelink = new TraceLink();
		
		Iterator iterator = selectionList.iterator();
		ISelectionModel selectionModel;
		int prevGroupNum = 1;
		Collection<String> locationList = new Vector<String>();
		ITraceEndpointModel traceEndpoint;
		while (iterator.hasNext()) {
			selectionModel = (ISelectionModel)iterator.next();
			if (prevGroupNum != selectionModel.getGroupNum()) {
				//TODO: assign description
				//assign the relationship
				//7/29/09 - comment out because this is getting the relationship of the next set of 
				//  links and assigning to the previous links
				//tracelink.setRelationship(selectionModel.getRelationship());
				//TODO: debug this line - not always being captured
				//System.out.println("%%%%new group num: " + tracelink.getRelationship());

				//7/29/09 serialize the current tracelink
				//tracelinkController.getXADLFacade().serializeTracelink(tracelink);
				if (tracelink.numOfEndpoints() > 1)
					xadlFacade.serializeTracelink(tracelink); 

				//create a new tracelink object
				tracelink = new TraceLink();
				
				prevGroupNum = selectionModel.getGroupNum();
				//create an endpoint and add it to the new tracelink object
				
			}
			//regardless of whether a new tracelink is created or not, do the following
			//locationList = getLocations(selectionModel);
			//create an endpoint
			//Iterator i = locationList.iterator();
			//while (i.hasNext()) {
			traceEndpoint = createEndpoint(selectionModel.getElement());
			//add this endpoint to the tracelink object
			tracelink.addEndpoint(traceEndpoint);
			//workaround to relationship not being set:
			if ( (tracelink.getRelationship() == null) || (tracelink.getRelationship().compareTo("") == 0))
				tracelink.setRelationship(selectionModel.getRelationship());
			System.out.println("%%%%add endpoint: " + tracelink.getRelationship());
			//}

		}
		//if (tracelink.numOfEndpoints()!=0) {
		if (tracelink.numOfEndpoints() > 1) {	//must have at least two endpoints - 7/28/09
			System.out.println("%%%%last endpoint: " + tracelink.getRelationship());
			//serialize the last current tracelink
			//tracelinkController.getXADLFacade().serializeTracelink(tracelink);
			xadlFacade.serializeTracelink(tracelink);
		}
	}	
	/*
	 * Method extracts the endpoint location from the ISelectionModel
	 */
	private Collection<String> getLocations(ISelectionModel selection) {
		
		//don't need this anymore, this is handledn now by rules
		//if (existsEndpoint(location))
		//	return null;
		ITraceEndpointModel endpoint = new TraceEndpoint();
		String location = new String();
		Vector<String> locationList = new Vector<String>();
		ISelection selectedItem = selection.getSelectedItem();
		
		if (selectedItem instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selectedItem;
			System.out.println("IStructuredSelection: " + ss.toString());
			//added 10/20/08 - to accommodate change in events being fired in BNA
			//Selection events are now strings and not necessarily ISelection objects
			//thus, need to pass in the selection model here so that 
			//we can later extract the string using selection.getElement();
			locationList = getLocationList(ss, selection);
		}
		else if (selectedItem instanceof ITextSelection) {
			ITextSelection ts  = (ITextSelection) selectedItem;
			location += ts.getText();

			locationList.add(location);
			//currTracelink.addEndpoint(createEndpoint(location));
		}
		else if (selectedItem instanceof IMarkSelection) {
			IMarkSelection ms = (IMarkSelection) selectedItem;

			try {
				location += ms.getDocument().get(ms.getOffset(), ms.getLength());
				locationList.add(location);
			}
			catch (BadLocationException ble) {
				System.err.println("TraceRecorder: Bad Location Exception: " + ms.getDocument());
			}
		}
		return locationList;
	}
	

	/*
	 * Method extracts the endpoint locations from the IStructuredSelection object 
	 */
	private Vector<String> getLocationList(IStructuredSelection ss, ISelectionModel selection) {
		Vector<String> locationList = new Vector<String>();

		Iterator<?> iterator = ss.iterator();
		Object obj;
		String location = "";
		String temp;
		URI locationURI;
		
		//ITraceEndpointModel endpoint;
		while (iterator.hasNext()) {
			obj = iterator.next();
			System.out.println("@@@obj class "  + obj.getClass().toString());
			//System.out.println("selection item event" + o.getClass().getName());
			
			/*
			if (obj instanceof File) {
				location = ((File) obj).getFullPath().toPortableString();
			}
			*/
			if (obj.toString().contains("ObjRef")) {
				//since it is an object ref, and the selections in BNA only represent the most recent selection, 
				//we need to get the string selected instead
				//location = "#" + xadlFacade.getID(obj.toString());
				location = "#" + xadlFacade.getID(selection.getElement());
			}			
			else {	//assume this is a selected File in the Resource Navigator
				try {
					temp = obj.toString();
					if (temp.contains("L/")) 
						temp = obj.toString().substring(1);			//truncate "L/"
					locationURI = new URI(temp);
					location = locationURI.toString();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

			
			
			//endpoint = createEndpoint(location);
			//if (endpoint != null)
			//	endpoints.add(endpoint);
			locationList.add(location);
		}

		return locationList;
	}
	
	/*
	 * Method creates a trace endpoint object for the given location
	 */
	private ITraceEndpointModel createEndpoint(String location) {
		TraceEndpoint endpoint = new TraceEndpoint();
		final String CAPTURE_MODE  = "auto_record";
		final String LOCATION_TYPE = "simple";
		
		endpoint.setAuthor(System.getProperty("user.name"));
		endpoint.setCaptureMode(CAPTURE_MODE);
		endpoint.setLocationType(LOCATION_TYPE);
		endpoint.setLocationHref(location);
		endpoint.setTimestamp(new Date());
		return endpoint;
	}



}
