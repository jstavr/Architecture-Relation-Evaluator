package edu.uci.isr.archstudio4.comp.tracelink.addtracelinks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.IXADLFacade;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceEndpoint;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceLink;
import edu.uci.isr.archstudio4.comp.tracelink.views.MsgBox;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;

public class RecoverLinkView extends AbstractMyxSimpleBrick implements IRecoverLinkView {

	public static final IMyxName INTERFACE_NAME_OUT_ADDTRACELINKS = 
		MyxUtils.createName("addtracelinks");
	public static final IMyxName INTERFACE_NAME_IN_INVOKEINVOKERECOVERVIEW =
		MyxUtils.createName("invokerecoverview");
	
	MyxRegistry myxr = MyxRegistry.getSharedInstance();
	//ITracelinkController tracelinkController;
	protected IXADLFacade xadlFacade;
	protected String selectedEndpointID;
	protected String selectedEndpointHref;
	
	Shell shell;
	
	
	public void begin() {

		xadlFacade = (IXADLFacade) MyxUtils.getFirstRequiredServiceObject(
				this, INTERFACE_NAME_OUT_ADDTRACELINKS);
		myxr.register(this);
		

	}
	/* (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxProvidedServiceProvider#getServiceObject(edu.uci.isr.myx.fw.IMyxName)
	 */
	
	public Object getServiceObject(IMyxName interfaceName) {
		if(interfaceName.equals(INTERFACE_NAME_IN_INVOKEINVOKERECOVERVIEW)){
			return this;
		}
		else
			return null;
		
	}	
	
	
	/*
	public void invokeRecoverView(Shell shell) {
		this.shell = shell;
		//MsgBox mbox = new MsgBox(shell, SWT.
		
	}
	*/
	public void invokeRecoverView(Shell shell, String archSelectedID) {
		
		this.shell = shell;
		selectedEndpointHref = "#" + archSelectedID;
		//selectedEndpointHref = archSelectedID;
		//selectedEndpointID = archSelectedID.substring(1);
		selectedEndpointID = archSelectedID;
		//MsgBox mbox = new MsgBox(shell, SWT.OK);
		//mbox.displayMsgBox("Recovering tracelinks for " + tracelinkController.getEndpointDesc(tracelinkController.getEndpointID()), "Recover Tracelinks");
		System.out.println("RecoverLinkView*******************");
		System.out.println("connected to : " + xadlFacade.toString());
		//controller.addTraceLinks();
		
		//display the choice between the tools
		//for now only have Lucene, Trac Issue System, and Google
		/*
		MsgBox mbox = new MsgBox(shell, SWT.YES);
		int choice = mbox.displayMsgBox("Recover artifacts using Lucene? " + "\n Yes: Recover using Lucene " 
				+ "\n No: Recover using Trac Issue and Bug Tracking System", "Select Recovery Tool");  
		if (choice == SWT.NO) {
			invokeTrac(shell);
		}
		else {
			invokeLucene(shell);
		}
		*/
		RecoverLinkToolDialog toolDialog = new RecoverLinkToolDialog(shell);
		int selection = toolDialog.open();
		
		//7/31/09 - comment out Lucene and trac temporarily because license notice still need to be added
		/*
		if (selection == RecoverLinkToolDialog.LUCENE)
			invokeLucene(shell);
		else if (selection == RecoverLinkToolDialog.TRAC)
			invokeTrac(shell);
		else if (selection == RecoverLinkToolDialog.GOOGLE)	
			invokeGoogle(shell);
		else
			//do nothing
			System.out.println("no recovery");
		*/
		if (selection == RecoverLinkToolDialog.GOOGLE)	
			invokeGoogle(shell);
		else
			//do nothing
			System.out.println("no recovery");
			
		
		
	}
	
	private void invokeTrac(Shell shell) {
		RecoverLinkInputDialog dialog = new RecoverLinkInputDialog(shell);
		
		String repositoryURL = dialog.open("Enter the URL of the Repository:");
		
		System.out.println("Adding endpoint " + repositoryURL);
		if (repositoryURL.compareTo("") != 0) {
			//String selectedArchElement = tracelinkController.getEndpointHref();
			
			//create a trace link
			TraceLink link = new TraceLink();
		    link.setDescription("Issue links");
		    link.setRelationship("unknown");
	    	
		    TraceEndpoint te = new TraceEndpoint();
	    	te.setCaptureMode("recovered");
	    	te.setAuthor(System.getProperty("user.name"));
	    	te.setLocationType("simple");
	    	//te.setLocationHref(tracelinkController.getEndpointHref());
	    	//te.setLocationHref(selectedArchElement);
	    	te.setLocationHref(selectedEndpointHref);
	    	Date timestamp = new Date();
	    	te.setTimestamp(timestamp);
	    	link.addEndpoint(te);

			//concatenate the query to the url
			//TODO: think about moving this to the actions attribute in the trace endpoint
	    	//make sure that to have a path separation
	    	repositoryURL = repositoryURL.trim();
	    	if (repositoryURL.lastIndexOf("/") + 1 != repositoryURL.length())
	    		repositoryURL = repositoryURL + "/";
			repositoryURL = repositoryURL + "query?status=new&status=reopened&summary=%7E";
			//repositoryURL = repositoryURL + tracelinkController.getEndpointDesc(tracelinkController.getEndpointID());
			repositoryURL = repositoryURL + xadlFacade.getElementDescription(selectedEndpointID);
			repositoryURL = repositoryURL + "&order=priority&col=id&col=summary&col=status&col=type&col=priority&col=component&col=reporter";



		    TraceEndpoint te2 = new TraceEndpoint();
		    te2.setCaptureMode("recovered");
		    te2.setAuthor(System.getProperty("user.name"));
		    te2.setLocationType("simple");
		    te2.setLocationHref(repositoryURL);
	    	timestamp = new Date();
	    	te2.setTimestamp(timestamp);
	    	link.addEndpoint(te2);
			
	    	//tracelinkController.addTraceLinks(link);
	    	xadlFacade.addTraceLinks(link);
		}
	}
	
	
	private void invokeGoogle(Shell shell) {
		RecoverLinkInputDialog dialog = new RecoverLinkInputDialog(shell);
		
		String domainURL = dialog.open("Enter the URL of the site to search:");
		
		System.out.println("Adding endpoint " + domainURL);
		
		//don't need to check whether the user entered a domain or not
		//either way, it will just be concatenated to the parameter
		
		//create a trace link
		TraceLink link = new TraceLink();
	    link.setDescription("Site links");
	    link.setRelationship("unknown");
    	
	    TraceEndpoint te = new TraceEndpoint();
    	te.setCaptureMode("recovered");
    	te.setAuthor(System.getProperty("user.name"));
    	te.setLocationType("simple");
    	te.setLocationHref(selectedEndpointHref);
    	Date timestamp = new Date();
    	te.setTimestamp(timestamp);
    	link.addEndpoint(te);

		//concatenate the query to the url
		//TODO: think about moving this to the actions attribute in the trace endpoint
    	
    	domainURL = domainURL.trim();
    	//Since google accepts site URL with or without http, don't need to truncate "http"
    	//or even the last "/" if it exists
    	System.out.println("domain is " + domainURL);
		String linkLocation = "http://www.google.com/search?hl=en&as_q=" + xadlFacade.getElementDescription(selectedEndpointID);
		linkLocation = linkLocation + "&as_sitesearch=" + domainURL + "&safe=images";

	    TraceEndpoint te2 = new TraceEndpoint();
	    te2.setCaptureMode("recovered");
	    te2.setAuthor(System.getProperty("user.name"));
	    te2.setLocationType("simple");
	    te2.setLocationHref(linkLocation);
    	timestamp = new Date();
    	te2.setTimestamp(timestamp);
    	link.addEndpoint(te2);
		
    	xadlFacade.addTraceLinks(link);
			
		
	}
	
	private void invokeLucene(Shell shell) {
		//TODO: clean up or move this code - 
		//check licensing - code taken from demo/IndexFiles
		//also see http://lucene.apache.org/java/2_2_0/api/overview-summary.html#overview_description
		final File INDEX_DIR = new File("index");
		
		DirectoryDialog dialog = new DirectoryDialog(shell);
	    dialog.setFilterPath("c:\\"); // TODO: this is Windows specific.  Make this platform specific
	    dialog.setText("Select directory to recover links");
	    String dirToSearch = dialog.open();
	    System.out.println("RESULT=" + dirToSearch);

		File docDir = new File(dirToSearch);
	    if (!docDir.exists() || !docDir.canRead()) {
	        System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
	        System.exit(1);
	      }
	      
	      Date start = new Date();
	      try {
	        IndexWriter writer = new IndexWriter(INDEX_DIR, new StandardAnalyzer(), true);
	        System.out.println("Indexing to directory '" +INDEX_DIR+ "'...");
	        indexDocs(writer, docDir);
	        System.out.println("Optimizing...");
	        writer.optimize();
	        writer.close();

	        Date end = new Date();
	        System.out.println(end.getTime() - start.getTime() + " total milliseconds");

	      } catch (IOException exc) {
	        System.out.println(" caught a " + exc.getClass() +
	         "\n with message: " + exc.getMessage());
	      }
	      
	      //remove the "#" from the component name
	      //String archElementDesc = tracelinkController.getEndpointDesc(tracelinkController.getEndpointID());
	      String archElementDesc = xadlFacade.getElementDescription(selectedEndpointID);
	      //search the files with the matching component/connector names in xADL
	      searchFiles(archElementDesc);


	}
	
	//H: 5/27/08
	private void indexDocs(IndexWriter writer, File file) throws IOException {
    // do not try to index files that cannot be read
    if (file.canRead()) {
      if (file.isDirectory()) {
        String[] files = file.list();
        // an IO error could occur
        if (files != null) {
          for (int i = 0; i < files.length; i++) {
            indexDocs(writer, new File(file, files[i]));
          }
        }
      } else {
        System.out.println("adding " + file);
        try {
        	//writer.addDocument(FileDocument.Document(file));
        	//from FileDocument.java
            // make a new, empty document
            Document doc = new Document();

            // Add the path of the file as a field named "path".  Use a field that is 
            // indexed (i.e. searchable), but don't tokenize the field into words.
            doc.add(new Field("path", file.getPath(), Field.Store.YES, Field.Index.UN_TOKENIZED));

            // Add the last modified date of the file a field named "modified".  Use 
            // a field that is indexed (i.e. searchable), but don't tokenize the field
            // into words.
            doc.add(new Field("modified",
                DateTools.timeToString(file.lastModified(), DateTools.Resolution.MINUTE),
                Field.Store.YES, Field.Index.UN_TOKENIZED));

            // Add the contents of the file to a field named "contents".  Specify a Reader,
            // so that the text of the file is tokenized and indexed, but not stored.
            // Note that FileReader expects the file to be in the system's default encoding.
            // If that's not the case searching for special characters will fail.
            doc.add(new Field("contents", new FileReader(file)));
            writer.addDocument(doc);
        }
        // at least on windows, some temporary files raise this exception with an "access denied" message
        // checking if the file can be read doesn't help
        catch (FileNotFoundException fnfe) {
          ;
        }
      }
    }
  }
	
	private void searchFiles(String searchTerm) {
		
	    String index = "index";
	    String field = "contents";
	    //String queries = null;			//search term
	    int repeat = 0;
	    boolean raw = false;
	    String normsField = null;
	    
	    
	    try {
	    	
	    	IndexReader reader = IndexReader.open(index);	
		    if (normsField != null)
			      reader = new OneNormsReader(reader, normsField);
		    Searcher searcher = new IndexSearcher(reader);
		    Analyzer analyzer = new StandardAnalyzer();

		    QueryParser parser = new QueryParser(field, analyzer);
		    Query query = parser.parse(searchTerm);
		    System.out.println("Searching for: " + query.toString(field));
	
		    Hits hits = searcher.search(query);
		      
		    System.out.println(hits.length() + " total matching documents");
	
		    //H: added 
		    TraceLink link = new TraceLink();
		    link.setDescription("Recovered links");
		    link.setRelationship("unknown");
	    	TraceEndpoint te = new TraceEndpoint();
	    	te.setCaptureMode("recovered");
	    	te.setAuthor(System.getProperty("user.name"));
	    	te.setLocationType("simple");
	    	//te.setLocationHref(tracelinkController.getEndpointHref());
	    	te.setLocationHref(selectedEndpointHref);
	    	Date timestamp = new Date();
	    	te.setTimestamp(timestamp);
	    	link.addEndpoint(te);

	    	String endpointPaths = "";
		    for (int i = 0; i < hits.length(); i++) {
	
		    	if (raw) {                              // output raw format
		    		System.out.println("doc="+hits.id(i)+" score="+hits.score(i));
		            continue;
		    	}
		    	
	
		    	Document doc = hits.doc(i);
		    	String path = doc.get("path");
		    	
		    	//H: added
		    	te = new TraceEndpoint();
		    	te.setCaptureMode("recovered");
		    	te.setAuthor(System.getProperty("user.name"));
		    	te.setLocationType("simple");
		    	te.setLocationHref(path);
		    	endpointPaths += path + "\n";
		    	timestamp = new Date();
		    	te.setTimestamp(timestamp);
		    	link.addEndpoint(te);
		    	
		    	if (path != null) {
		    		System.out.println((i+1) + ". " + path);
		            String title = doc.get("title");
		            if (title != null) {
		            	System.out.println("   Title: " + doc.get("title"));
		            }
		    	} else {
		            System.out.println((i+1) + ". " + "No path for this document");
		    	}
		        
		    }

		    String msgBoxTitle = "Recover Tracelinks";
		    if (hits.length() > 0) {
			    MsgBox mbox = new MsgBox(shell, SWT.YES);	//for yes/no msgbox
			    System.out.println("paths: " + endpointPaths);
			    int result = mbox.displayMsgBox("Add the following recovered links?" + "\n" + endpointPaths, msgBoxTitle);
			    if (result == SWT.YES) {
			        //tracelinkController.addTraceLinks(link);
			    	xadlFacade.addTraceLinks(link);
			        //tracelinkController.updateViews();
			    }
		    	
		    } else { //handle no results
		    	MsgBox mbox = new MsgBox(shell, SWT.OK);
		    	mbox.displayMsgBox("No recovered links", msgBoxTitle);
		    }
		    reader.close();
		  
	    }
	    catch (IOException exc){
	    	exc.printStackTrace();
	    }
	    catch (ParseException pexc) {
	    	
	    }
	}
}
	




