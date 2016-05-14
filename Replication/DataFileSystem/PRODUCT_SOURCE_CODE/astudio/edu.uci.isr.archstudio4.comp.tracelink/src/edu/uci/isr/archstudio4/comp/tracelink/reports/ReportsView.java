/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.reports;

//import edu.uci.isr.archstudio4.comp.tracelink.TracelinkController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import edu.uci.isr.archstudio4.comp.tracelink.controllers.IXADLFacade;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITraceEndpointModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkModel;
import edu.uci.isr.archstudio4.comp.tracelink.preferences.PreferencesConstants;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;

/**
 * @author Hazel, dpurpura
 */
public class ReportsView
	extends AbstractMyxSimpleBrick
	implements IReportView{

	public static final IMyxName INTERFACE_NAME_OUT_READTRACELINKS = MyxUtils.createName("readtracelinks");
	public static final IMyxName INTERFACE_NAME_IN_INVOKEREPORTVIEW = MyxUtils.createName("invokereportview");

	//TODO: allow users to specify the filename of the report
	//private static final String PATH = "../../../../../../../../../res/report/";
	//private static final String TEMPLATE_FILENAME = "G:/eclipse/edu.uci.isr.archstudio4.comp.tracelink/res/report/template.html";
	//private static final String TEMPLATE_FILENAME = "C:/template.html";
	//private static final String REPORT_FILENAME = "C:/report.html";
	//private static final String REPORT_URL_FILENAME = "file:///C|/report.html";
	private Shell shell;

	protected MyxRegistry myxr = MyxRegistry.getSharedInstance();
	//protected ITracelinkController tracelinkController;
	protected IXADLFacade xadlFacade;

	private Vector<Criteria> criteriaList;
	private boolean isAndOperator;

	public ReportsView(){
		criteriaList = new Vector<Criteria>();
		isAndOperator = false;
	}

	
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
		if(interfaceName.equals(INTERFACE_NAME_IN_INVOKEREPORTVIEW)){
			return this;
		}
		else{
			return null;
		}
	}

	public void invokeReportView(Shell parent, String[] attributeNames){
		System.out.println("ReportsView*******************");
		System.out.println("connected to : " + xadlFacade.toString());

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText("Trace Analysis");
		shell.setSize(500, 300);
		shell.setLayout(new FillLayout());

		ExpandBar bar = new ExpandBar(shell, SWT.NONE);
		ReportCriteriaViewer viewer = new ReportCriteriaViewer(bar, this);
		viewer.setAttributeNames(attributeNames);
		//viewer.setController(tracelinkController);
		viewer.setup();

		ExpandItem itemReport = new ExpandItem(bar, SWT.NONE);

		itemReport.setText("Report Criteria");
		itemReport.setHeight(viewer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		itemReport.setControl(viewer);
		itemReport.setExpanded(true);

		//ExpandItem itemUpdate = new ExpandItem(bar, SWT.NONE);

		//itemUpdate.setText("Update Criteria");
		//itemUpdate.setHeight(viewer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		//itemUpdate.setControl(viewer);
		//itemUpdate.setExpanded(true);		

		shell.open();

		while(!shell.isDisposed()){
			if(!shell.getDisplay().readAndDispatch()){
				shell.getDisplay().sleep();
			}
		}

	}

	/**
	 * Method runs a report based on the given set of criteria
	 * 
	 * @param criteriaList
	 *            a vector of criteria obtained from the ReportCriteriaViewer
	 * @param isAndOperator
	 *            true if doing a union ("AND") with the rules; otherwise false
	 *            ("OR")
	 */
	public void runReport(Vector<Criteria> criteriaList, boolean isAndOperator){
		this.criteriaList = criteriaList;
		this.isAndOperator = isAndOperator;

		Vector<ITracelinkModel> selectedLinks = filterTracelinks(criteriaList, isAndOperator);

		//printReport(REPORT_FILENAME, selectedLinks);
		//if the user report path is not specified, use the default path
		String reportPath;
		if ( (System.getProperty(PreferencesConstants.PROP_REPORT) == null)  ||
				( System.getProperty(PreferencesConstants.PROP_REPORT).length() == 0)  ||
				( System.getProperty(PreferencesConstants.PROP_REPORT).contains("null")) )
			reportPath = System.getProperty(PreferencesConstants.DEFAULT_PROP_REPORT);
			//printReport(System.getProperty(PreferencesConstants.PROP_REPORT), selectedLinks);
		else
			reportPath = System.getProperty(PreferencesConstants.PROP_REPORT) + 
				PreferencesConstants.getFileSeparator(PreferencesConstants.PROP_REPORT) +
				PreferencesConstants.getFileName(PreferencesConstants.DEFAULT_REPORT_URL);;
			//printReport(System.getProperty(PreferencesConstants.DEFAULT_PROP_REPORT), selectedLinks);
		
		printReport(reportPath, selectedLinks);

		//close the criteria viewer
		shell.dispose();

		try{
			//displayFileInBrowser(new URL(REPORT_URL_FILENAME));
			File reportFile = new File(reportPath);
			URI reportURI = reportFile.toURI();
			displayFileInBrowser(reportURI.toURL());
			
			System.out.println("report is written to " + reportPath);
		}
		catch(MalformedURLException e){
			System.err.println("Report filename does not exist");
		}
	}

	/**
	 * Opens the given URL in a browser
	 * 
	 * @param url
	 *            the url to open
	 */
	public void displayFileInBrowser(URL url){
		IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
		IWebBrowser browser;
		try{
			browser = browserSupport.createBrowser("reportBrowser");
			browser.openURL(url);
		}
		catch(PartInitException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Creates an xHTML-based report with the given tracelinks
	 * 
	 * @param filename
	 *            the filename to save the report to
	 * @param tracelinks
	 *            the TraceLinks to run the report on
	 */
	public void printReport(String filename, Collection<ITracelinkModel> tracelinks){
		File file = new File(filename);
		try{
			System.err.println(file.getCanonicalPath());
			file.createNewFile();
			PrintWriter out = new PrintWriter(file);
			printHead(out);
			printBody(out, tracelinks);
			out.println("</html>");
			out.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Prints the "head" section of the html file
	 * 
	 * @param out
	 *            the PrintWriter to output the head to
	 */
	private void printHead(PrintWriter out){
		try{
			//Scanner scanner = new Scanner(new File(TEMPLATE_FILENAME));
			Scanner scanner = new Scanner(new File(System.getProperty(PreferencesConstants.DEFAULT_PROP_REPORT_TEMPLATE)));

			while(scanner.hasNext()){
				out.println(scanner.nextLine());
			}

		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}

	}

	/**
	 * Prints the "body" section of the html file.
	 * 
	 * @param out
	 *            the PrintWriter to output the body to
	 * @param tracelinks
	 *            the tracelinks to include in the report
	 */
	private void printBody(PrintWriter out, Collection<ITracelinkModel> tracelinks){
		out.println("<body>");
		out.println("<h1>Trace Report</h1>");

		printTracelinks(out, tracelinks);

		out.println("</body>");
	}

	/**
	 * Prints the collection of tracelinks
	 * 
	 * @param out
	 * @param tracelinks
	 */
	private void printTracelinks(PrintWriter out, Collection<ITracelinkModel> tracelinks){
		for(ITracelinkModel tracelink: tracelinks){
			printTracelink(out, tracelink);
			out.println("<br />");
		}

	}

	private void printTracelink(PrintWriter out, ITracelinkModel tracelink){
		Vector<ITraceEndpointModel> allEndpoints = tracelink.getEndpointList();
		Vector<ITraceEndpointModel> selectedEndpoints;

		if(allEndpoints.isEmpty()){
			return;
		}
		else{

			/*
			 * Collection<String> columnNames = endpoints.get(0).getKeys();
			 * String description = tracelink.getDescription(); //FIXME come up
			 * with a better default tracelink description for report if
			 * ((description == null) || description.equals("")) description =
			 * "Tracelink Description"; out.print("<h2>");
			 * out.print(description); out.println("</h2>"); out.println("<table>");
			 * printTableHeader(out, columnNames); printTableBody(out,
			 * endpoints); out.println("</table>");
			 */

			//H: fixed this 8/21/08
			//need to get the filter of the endpoints first to determine whether there are 
			//items to print before printing the header for the endpoints
			//If the criteria only relates to tracelinks, don't need to filter the criteria
			//This is a workaround.  Need to fix filterEndpoints to return all endpoints if
			//none of the criteria relates to endpoints
			if(isCriteriaForTracelinks(criteriaList)){
				selectedEndpoints = allEndpoints;
			}
			else{
				selectedEndpoints = filterEndpoints(criteriaList, isAndOperator, allEndpoints);
			}
			if(selectedEndpoints.size() != 0){
				Collection<String> columnNames = selectedEndpoints.get(0).getKeys();

				String relationship = tracelink.getRelationship();
				//FIXME come up with a better default tracelink description for report
				if(relationship == null || relationship.equals("")){
					relationship = "unspecified";
				}

				//TODO: Fix the misplaced header at the top
				//still don't know why it's doing this 
				//Is it possibly caused by the CSS?
				out.print("<h2>");
				out.print("Relationship: " + relationship);
				out.println("</h2>");

				out.println("<table>");

				printTableHeader(out, columnNames);
				printTableBody(out, selectedEndpoints);
				out.println("</table>");
			}

		}
	}

	/**
	 * @param out
	 * @param columnNames
	 */
	private void printTableHeader(PrintWriter out, Collection<String> columnNames){
		out.println("<thead>");
		out.println("<tr>");
		for(String key: columnNames){
			//temporarily do not print the headings for relationship and description
			if(!key.contains("relationship") && !key.contains("description")){
				out.print("<th>" + humanize(key) + "</th>");
			}
		}
		out.println("</tr>");
		out.println("</thead>");
	}

	/**
	 * @param out
	 * @param endpoints
	 *            The filtered list of endpoints
	 */
	private void printTableBody(PrintWriter out, Collection<ITraceEndpointModel> endpoints){

		//filter out non matching endpoints
		//H: removed the following. Endpoints passed in is already filtered 
		//endpoints = filterEndpoints(criteriaList, isAndOperator, endpoints);

		out.println("<tbody>");

		boolean isOdd = false;
		for(ITraceEndpointModel endpoint: endpoints){
			isOdd = !isOdd;
			printEndpoint(out, endpoint, isOdd);
		}
		out.println("</tbody>");
	}

	private void printEndpoint(PrintWriter out, ITraceEndpointModel element, boolean isOdd){

		//alternate row colors
		if(isOdd){
			out.println("<tr id='" + element.getID() + "' class='odd'>");
		}
		else{
			out.println("<tr id='" + element.getID() + "'>");
		}

		//print data
		String s;
		for(String key: element.getKeys()){
			//comment this out for now
			//out.println("<td>");

			//if a component/connector is an element, use the name instead of it's id
			if(key.equalsIgnoreCase("location")){
				out.println("<td>");
				out.print(getEndpointLocation(out, key, element));
				out.print("</td>");
			}
			//temporarily do not include status and description
			else if(!key.equalsIgnoreCase("relationship") && !key.equalsIgnoreCase("description")){
				out.println("<td>");
				out.print(element.getAttribute(key));
				out.println("</td>");
			}

			else{
				//do nothing
			}

			//comment this out for now
			//out.print("</td>");
		}
		out.println("</tr>");
	}

	/**
	 * @param out
	 * @param key
	 *            the name of the attribute
	 * @param endpoint
	 *            the endpoint to print
	 * @return returns the name if the component/connector is an element;
	 *         otherwise, returns it's id
	 */
	private String getEndpointLocation(PrintWriter out, String key, ITraceEndpointModel endpoint){
		String location = endpoint.getAttribute(key).toString();

		//check to see if component/connector (starts with "#")
		if(location == null || location.length() <= 0){
			location = "";
		}
		else if(location.charAt(0) == '#'){
			//location = tracelinkController.getEndpointDesc(location.substring(1));
			location = xadlFacade.getElementDescription(location.substring(1));
		}
		else if(location.contains("http:")){
			location = "<a href='" + location + "'>" + location + "</a>";
		}

		return location;
	}

	/**
	 * Capitalizes the first word and turns underscores into spaces and strips
	 * _id adds spaces between camel-cased words. This is meant for creating
	 * pretty output.
	 * 
	 * @param s
	 *            the string to humanize
	 * @return the humanized string
	 */
	public static String humanize(String s){
		if(s == null || s.length() <= 0){
			return "";
		}

		char firstLetter = Character.toTitleCase(s.charAt(0));

		if(s.length() == 1){
			return firstLetter + "";
		}

		String body = s.replace("_id", "").replace("_", " ").substring(1);

		//remove camel-casing
		StringBuffer spacedBody = new StringBuffer();
		for(char c: body.toCharArray()){
			if(Character.isUpperCase(c)){
				spacedBody.append(" ");
			}
			spacedBody.append(c);
		}

		return firstLetter + spacedBody.toString();
	}

	/**
	 * Select the tracelink objects that satisfy a set of criteria
	 * 
	 * @param criteriaList
	 *            set of criteria specified in the ReportCriteriaViewer
	 * @param isAndOperator
	 *            true if union ("AND"); otherwise false ("OR")
	 * @return selected tracelink objects
	 */
	public Vector<ITracelinkModel> filterTracelinks(Vector<Criteria> criteriaList, boolean isAndOperator){
		Vector<ITracelinkModel> selectedLinks = new Vector<ITracelinkModel>();
		//Collection<ITracelinkModel> allLinks = tracelinkController.getXADLFacade().getTracelinks();
		Collection<ITracelinkModel> allLinks = xadlFacade.getTracelinks();
		String key;
		String property;
		String operator;

		String attribute;

		//go through the list of criteria and check for those applicable to tracelink objects
		//keep track of how many criteria relate to the tracelink objects
		int count = 0;
		for(Criteria criteria: criteriaList){
			//if ( (criteria.attribute.compareToIgnoreCase("description") == 0) 
			//	|| (criteria.attribute.compareToIgnoreCase("relationship") == 0) )
			if(criteria.getAttribute().compareToIgnoreCase("description") == 0 || criteria.getAttribute().compareToIgnoreCase("relationship") == 0)

			{
				System.out.println("criteria is description or relationship");
				count++;
				key = criteria.getAttribute();
				property = criteria.getProperty().toLowerCase();
				operator = criteria.getOperator();

				for(ITracelinkModel link: allLinks){
					if(key.compareToIgnoreCase("description") == 0){
						attribute = link.getDescription();
					}
					else if(key.compareToIgnoreCase("relationship") == 0){
						attribute = link.getRelationship();
					}
					else if(link.hasAttribute(key)){
						//attribute = link.getAttribute(key).toString().toLowerCase();
						attribute = link.getAttribute(key).toString();
					}
					else{
						attribute = null;
					}
					if(attribute != null && attribute.compareTo("") != 0){
						attribute = attribute.toLowerCase();
						System.out.println(link.getID());
						System.out.println("linkAttribute: " + attribute);
						if(operator.equalsIgnoreCase("contains")){
							//if (property.equals(attribute)) {
							int x = attribute.indexOf(property);
							if(x != -1){
								selectedLinks.add(link);
							}
							else{ //if it did not pass and andOrOperator = "and", discard all selected links
								if(isAndOperator){
									selectedLinks.removeAllElements();
								}
							}
						}
						else{ //"equals" operator
							if(attribute.equalsIgnoreCase(property)){
								selectedLinks.add(link);
							}
							else{ //if it did not pass and andOrOperator = "and", discard all selected links
								if(isAndOperator){
									selectedLinks.removeAllElements();
								}
							}

						}

					}
				}
			}
		}

		//if none of the criteria applies to tracelink objects, return the entire set of links
		//if (count == 0) {
		//TODO: There's a hole in this logic - what if the criteria is only for the tracelinks, but 
		//it just didn't pass the criteria
		if(selectedLinks.size() == 0){
			for(ITracelinkModel link: allLinks){
				selectedLinks.add(link);
			}
		}

		//test to see if we get the correct links
		System.out.println("get selected links");
		for(ITracelinkModel link: selectedLinks){
			System.out.println("tracelink: " + link.getID());

		}

		//check to see if allLinks contains anything
		System.out.println("get all links");
		for(ITracelinkModel link: allLinks){
			System.out.print("relationship: " + link.getRelationship() + "desc: " + link.getDescription());
			//System.out.print("" + link.getAttribute(key))
		}

		return selectedLinks;

	}

	/**
	 * Method returns the component/connector name that corresponds to a given
	 * tracelink object
	 * 
	 * @param link
	 *            tracelink object
	 * @return
	 */
	private String getArchEndpointName(ITracelinkModel link){
		String archEndpoint = "";
		Vector<ITraceEndpointModel> endpointList = link.getEndpointList();
		for(ITraceEndpointModel endpoint: endpointList){
			if(endpoint.getLocationHref().substring(0, 1).compareTo("#") == 0){
				archEndpoint = endpoint.getLocationHref().substring(1);
				//return tracelinkController.getEndpointDesc(archEndpoint);
				return xadlFacade.getElementDescription(archEndpoint);
			}
		}

		return null;

	}

	/**
	 * Method selects tracelink endpoints that satisfy the given criteria list
	 * TODO: Add negate function. Also, and/or cannot be mixed. Also, need to
	 * return all the endpoints if the criteria does not relate to
	 * traceEndpoints
	 * 
	 * @param criteriaList
	 *            set of criteria specified in the ReportCriteriaViewer
	 * @param isAndOperator
	 *            true if "AND;" otherwise false ("OR")
	 * @param allEndpoints
	 *            list of endpoints to check
	 * @return selected tracelinks
	 */
	public Vector<ITraceEndpointModel> filterEndpoints(Collection<Criteria> criteriaList, boolean isAndOperator, Collection<ITraceEndpointModel> allEndpoints){
		//int length = tracelinkController.readTraceLinks().size();
		int length = allEndpoints.size();
		//ITraceEndpointModel[] tracelinkList = tracelinkController.readTraceLinks().toArray(new ITraceEndpointModel[length]);
		ITraceEndpointModel[] traceEndptList = allEndpoints.toArray(new ITraceEndpointModel[length]);
		boolean[] resultsList = new boolean[length];

		//initialize the resultsList according to the andOrOperator
		if(isAndOperator){
			for(int i = 0; i < resultsList.length; i++){
				resultsList[i] = true;
			}
		}
		else{ //or operator
			for(int i = 0; i < resultsList.length; i++){
				resultsList[i] = false;
			}
		}

		//use for-loop here because we need to keep track of the array counter
		//to make sure that we're assigning values to the correct location in the resultsList
		boolean curResult;
		String key;
		String property;
		String operator;
		String endpointLoc;
		Date aDate = new Date();
		DateFormat dateFormat;
		for(int n = 0; n < traceEndptList.length; n++){
			//if the endpoint is a component/connector, need to ignore this and don't go through the criteria
			endpointLoc = traceEndptList[n].getLocationHref();
			if(endpointLoc.substring(0, 1).compareTo("#") != 0){
				for(Criteria criteria: criteriaList){
					key = criteria.getAttribute();
					property = criteria.getProperty();
					operator = criteria.getOperator();
					if(key.compareToIgnoreCase("timestamp") == 0){
						dateFormat = DateFormat.getInstance();
						try{
							aDate = dateFormat.parse(property);
						}
						catch(ParseException e){
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//aDate = new Date(property);  	//TODO: check for an updated constructor

					}

					if(operator.compareToIgnoreCase("contains") == 0){
						if(traceEndptList[n].hasAttribute(key) && traceEndptList[n].getAttribute(key).toString().toLowerCase().contains(property.toLowerCase())){
							//TODO: "contains" does not handle the check for date

							//check for and operator
							if(isAndOperator){
								curResult = resultsList[n] && true;
								resultsList[n] = curResult;
							}
							else{ //or operator
								curResult = resultsList[n] || true;
								resultsList[n] = curResult;
							}

						}
						else{ //criteria fails
							//check for or operator
							if(isAndOperator){
								curResult = resultsList[n] && false;
								resultsList[n] = curResult;
							}
							else{ //or operator
								curResult = resultsList[n] || false;
								resultsList[n] = curResult;
							}
						}

					}
					else{ //equals operator
						if(traceEndptList[n].hasAttribute(key)
						//one of the two conditions must be true
						//the String attribute = property
						//or the Date attribute = property
						&& traceEndptList[n].getAttribute(key).toString().toLowerCase().compareTo(property.toLowerCase()) == 0
						//if key = timestamp, aDate != null
						|| aDate != null && aDate.equals(traceEndptList[n].getAttribute(key))){
							//TODO: the above will only pass if the dates match to the millisecond level
							//need to only get the year, month, date and check if these are equal
							//need to also convert the traceEndptList[n].getAttribute(key) into a date
							//just create a boolean that will be set to true when either of these two conditions are met

							//check for and operator
							if(isAndOperator){
								curResult = resultsList[n] && true;
								resultsList[n] = curResult;
							}
							else{ //or operator
								curResult = resultsList[n] || true;
								resultsList[n] = curResult;
							}

						}
						else{ //criteria fails
							//check for or operator
							if(isAndOperator){
								curResult = resultsList[n] && false;
								resultsList[n] = curResult;
							}
							else{ //or operator
								curResult = resultsList[n] || false;
								resultsList[n] = curResult;
							}
						}
					}

				}
			}
			else{ //keep the component/connector endpoint
				resultsList[n] = true;
			}

		}

		//now get all the links that passed all the criteria
		Vector<ITraceEndpointModel> endpoints = new Vector<ITraceEndpointModel>();
		for(int n = 0; n < traceEndptList.length; n++){
			if(resultsList[n]){
				endpoints.add(traceEndptList[n]);
			}
		}

		//H: need to check if the resulting endpoint is only one (i.e. one component/connector)
		//if so, don't need to return this component/connector
		if(endpoints.size() == 1){
			endpoints.clear();
		}

		//test if it works
		for(ITraceEndpointModel endpoint: endpoints){
			System.out.println("endpoint: " + endpoint.toString());
		}

		return endpoints;

	}

	/**
	 * Method checks whether all of the items in the criteria relates to
	 * tracelinks
	 * 
	 * @param criteriaList
	 *            set of criteria specified in the ReportCriteriaViewer
	 * @return true if any of items in the list relates to tracelinks only
	 * @return false if none of the items in the list relates to tracelinks only
	 */

	private boolean isCriteriaForTracelinks(Vector<Criteria> listOfCriteria){
		int countForTracelinks = 0;
		for(Criteria criteria: listOfCriteria){
			if(criteria.getAttribute().compareToIgnoreCase("description") == 0 || criteria.getAttribute().compareToIgnoreCase("relationship") == 0){
				countForTracelinks++;
			}
		}
		if(countForTracelinks == listOfCriteria.size()){
			return true;
		}
		else{
			return false;
		}
	}

	/*
	 * The following methods relate to the Run Update command
	 */

	/**
	 * Method runs an update based on the given set of criteria
	 * 
	 * @param criteriaList
	 *            a vector of criteria obtained from the ReportCriteriaViewer
	 * @param isAndOperator
	 *            true if doing a union ("AND") with the rules; otherwise false
	 *            ("OR")
	 */
	public void runUpdate(Vector<Criteria> criteriaList, boolean isAndOperator, String key, String property){
		updateEndpoints(criteriaList, isAndOperator, key, property);

	}

	/**
	 * Method updates the endpoints based on the given criteria
	 * 
	 * @param criteriaList
	 *            set of criteria specified in the ReportCriteriaViewer
	 * @param key
	 *            the field or the attribute to change
	 * @param property
	 *            the new value of the key
	 */
	private void updateEndpoints(Vector<Criteria> listOfCriteria, boolean isAndOperator, String key, String property){
		Vector<ITracelinkModel> allLinks = filterTracelinks(listOfCriteria, isAndOperator);
		Vector<ITraceEndpointModel> allEndpoints;
		Vector<ITraceEndpointModel> selectedEndpoints;
		for(ITracelinkModel link: allLinks){
			allEndpoints = link.getEndpointList();
			System.out.println("Printing all endpoints: ");
			printEndpoints(allEndpoints);
			selectedEndpoints = filterEndpoints(listOfCriteria, isAndOperator, allEndpoints);
			System.out.println("Printing selected endpoints: ");
			printEndpoints(selectedEndpoints);
			for(ITraceEndpointModel endpoint: selectedEndpoints){
				if(endpoint != null && endpoint.hasKey(key)){
					//call the update to the endpoint

					//tracelinkController.getXADLFacade().updateTraceElement(endpoint, key, property);
					xadlFacade.updateTraceElement(endpoint, key, property);

				}
			}
		}
	}

	//for debugging
	private void printEndpoints(Vector<ITraceEndpointModel> endpoints){
		for(ITraceEndpointModel endpoint: endpoints){
			System.out.println(endpoint.getID());
		}
	}

}
