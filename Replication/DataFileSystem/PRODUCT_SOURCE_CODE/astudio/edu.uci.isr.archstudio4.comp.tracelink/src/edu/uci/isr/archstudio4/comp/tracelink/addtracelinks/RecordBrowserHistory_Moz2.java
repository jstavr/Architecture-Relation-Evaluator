/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.addtracelinks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import edu.uci.isr.archstudio4.comp.tracelink.models.IMozillaBrowserHistoryModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.MozillaBrowserHistoryModel;
import edu.uci.isr.archstudio4.comp.tracelink.views.MsgBox;

/**
 * @author Hazel
 *
 */
public class RecordBrowserHistory_Moz2 {

	//TODO: in the future, can switch to the type of browser that we are recording from
	public RecordBrowserHistory_Moz2() {
		
	}

	/**
	 * Method reads the browser history, extracts the visited urls and saves them to a 
	 * data structure
	 * @return browser history listing
	 */
	public Collection<IMozillaBrowserHistoryModel> getBrowserHistory(Shell shell) {
		//this is just a test on the history parser
		//TODO: this is an absolute reference
		//		find a way to dynamically determine Mozilla location - or set this in an external 
		//		environment file.  Also need to check the presence of these paths
		String mozillaPath = "C:/Documents and Settings/Hazel/Application Data/Mozilla/Firefox/Profiles/pzy3o7jl.default/";
		String destinationPath = "C:/TEMP/";
		String sourceHistory = mozillaPath + "history.dat";
		String destinationHistory = destinationPath + "history.dat";
		String utility = "Dork.exe";
		
		//since the utility tool can't handle a file path with spaces,
		//need to copy the browser history file to another location without spaces
		try {
			copy(new File(sourceHistory), new File(destinationHistory));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//delete the previous parsed file (history.txt) before running the utility
		File oldHistoryText = new File(destinationPath + "history.txt");
		if (oldHistoryText.exists())
			oldHistoryText.delete();
		
		//run the utility
		String[] commands={destinationPath + utility, destinationHistory};
		try {
			Runtime.getRuntime().exec(commands);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		//output a status message to the user to create a delay the next execution
		MsgBox mbox = new MsgBox(shell, SWT.OK);
		mbox.displayMsgBox("Processing captured selections", "Status");
		
		//get the newly created history file
		File historyText = new File(destinationPath + "history.txt");
		
		//then we need to parse the text file
		Vector<IMozillaBrowserHistoryModel> historyList = parseBrowserHistory(historyText);
		
		//test if the parse method was successful
		System.out.println("printing parsed history: ");
		for (IMozillaBrowserHistoryModel anEntry: historyList) {
			System.out.print(anEntry.getVisitCount());
			System.out.print(" * " + anEntry.getFirstVisitDate());
			System.out.print(" * " + anEntry.getLastVisitDate());
			System.out.println(" * " + anEntry.getUrl());
		}
		
		return historyList;

	}
	
	//need to move the following later
	//and remove the imports of File, etc.
	private static void copy(File source, File dest) throws IOException {
	     FileChannel in = null, out = null;
	     try {          
	          in = new FileInputStream(source).getChannel();
	          out = new FileOutputStream(dest).getChannel();
	 
	          long size = in.size();
	          MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
	 
	          out.write(buf);
	 
	     } finally {
	          if (in != null)          in.close();
	          if (out != null)     out.close();
	     }
	}

	//move the following
	//the following is customized to parse Mozilla firefox browser history data
	//TODO: check how this will behave if the history file is huge
	private Vector<IMozillaBrowserHistoryModel> parseBrowserHistory(File historyTextFile) {
		Vector<IMozillaBrowserHistoryModel> historyList = new Vector<IMozillaBrowserHistoryModel>();
		try {
			Scanner scanner = new Scanner(historyTextFile);
			
			String curToken;
			Date dtTmp;
			final int numOfFields = 5;			//number of fields in the history text file
			int counter = 0;
			IMozillaBrowserHistoryModel historyModel = new MozillaBrowserHistoryModel();
			scanner.nextLine();					//skip the headings
		    while ( scanner.hasNext() ){
		    	//curToken = scanner.next();
		    	switch (counter) {
		    	case 0:  						//id - ignore this
		    		curToken = scanner.next();
		    		//counter++;
		    		break;
		    	case 1:							//visit count
		    		if (scanner.hasNextInt()) {
		    			historyModel.setVisitCount(scanner.nextInt());
		    			//counter++;
		    		}
		    		break;	
		    	case 2:							//first visit date
		    		//String strTmp = "Jan 3, 2007 23:59";
		    		//Date dtTmp = new SimpleDateFormat("MMM d, yyyy H:mm").parse(strTmp);
		    		curToken = scanner.next() + " " + scanner.next();	//date and time are separated by a space
		    		dtTmp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(curToken);
		    		//String strOutDt = new SimpleDateFormat("mm/dd/yyyy").format(dtTmp);
		    		//System.out.println("CustomReports_EscrowStatement_view.jsp strOutDt" + strOutDt);
		    		if (dtTmp != null)
		    			historyModel.setFirstVisitDate(dtTmp);
		    		break;
		    	case 3:							//last visit date
		    		curToken = scanner.next() + " " + scanner.next();	//date and time are separated by a space;
		    		dtTmp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(curToken);
		    		if (dtTmp != null) {
		    			//TODO: need a check for daylight savings time
		    			//      for now, just add an hour to accomodate the time difference
		    			//		also for now, adjust the milliseconds (1 hour = 3,600,000 milliseconds)
		    			//		because the Date.getHour is deprecated
		    			//		and changing the type from Date to Calendar will change the serialized
		    			//		XML file
		    			long adjTime = dtTmp.getTime() + 3600000;
		    			dtTmp.setTime(adjTime);
		    			historyModel.setLastVisitDate(dtTmp);
		    		}
		    			
		    		break;
		    		
		    	case 4:							//url
		    		curToken = scanner.next();
		    		if (curToken != null)
		    			historyModel.setUrl(curToken);
		    		break;
		    	default:
		    		
		    		break;
		    	
		    	}
		    	counter++;
		    	if (counter == numOfFields) {	//finished reading the last field
		    		counter = 0;
		    		//check first if the history is valid before adding it to the list
		    		if (isValid(historyModel))
		    			historyList.add(historyModel);
		    		historyModel = new MozillaBrowserHistoryModel();
		    		scanner.nextLine();
		    	}
		    }
	   
		    scanner.close();
		} catch (Exception ex){
			System.out.println(ex.getMessage());
		}

		return historyList;
	}

	private boolean isValid(IMozillaBrowserHistoryModel historyModel) {
		if ( (historyModel.getUrl().contains("http")) || (historyModel.getUrl().contains("file://")) ) {
			if ( (! historyModel.getUrl().contains("firefox?client=firefox-a&rls=org.mozilla:en-US:official"))
					&& (! historyModel.getUrl().contains("dg.specificclick.net")) 
					&& (historyModel.getVisitCount() > 1)) {
				//TODO: check the number of visits today.  However the current history.dat only provides the first
				//		and the last dates.
				//		1 day = 86 400 000 milliseconds 
				//long datePeriod = historyModel.getLastVisitDate().getTime() - historyModel.getFirstVisitDate().getTime();
				return true;
			}
				
			else
				return false;			//because this is just an empty page
		}
		else					
			return false;				//because this is not a web or file resource
	}

}
