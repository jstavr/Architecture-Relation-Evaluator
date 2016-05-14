package edu.uci.isr.archstudio4.comp.archlight.tools.schematron;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.uci.isr.archstudio4.archlight.ArchlightIssue;
import edu.uci.isr.archstudio4.archlight.ArchlightTest;
import edu.uci.isr.archstudio4.archlight.ArchlightTestError;
import edu.uci.isr.archstudio4.archlight.ArchlightTestResult;
import edu.uci.isr.archstudio4.archlight.IArchlightTool;
import edu.uci.isr.archstudio4.comp.archlight.issueadt.IArchlightIssueADT;
import edu.uci.isr.archstudio4.comp.archlight.noticeadt.IArchlightNoticeADT;
import edu.uci.isr.archstudio4.comp.archlight.testadt.IArchlightTestADT;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.sysutils.SystemUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class SchematronMyxComponent
	extends AbstractMyxSimpleBrick
	implements IArchlightTool, IMyxDynamicBrick{

	public static final String TOOL_ID = "Schematron";

	public static final IMyxName INTERFACE_NAME_IN_TOOL = MyxUtils.createName("archlighttool");

	public static final IMyxName INTERFACE_NAME_OUT_XARCHADT = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_OUT_ISSUEADT = MyxUtils.createName("archlightissueadt");
	public static final IMyxName INTERFACE_NAME_OUT_TESTADT = MyxUtils.createName("archlighttestadt");
	public static final IMyxName INTERFACE_NAME_OUT_NOTICEADT = MyxUtils.createName("archlightnoticeadt");
	public static final IMyxName INTERFACE_NAME_OUT_PREFERENCES = MyxUtils.createName("preferences");

	protected XArchFlatInterface xarch = null;
	protected IArchlightIssueADT issueadt = null;
	protected IArchlightNoticeADT noticeadt = null;
	protected IArchlightTestADT testadt = null;
	protected IPreferenceStore preferences = null;

	protected SchematronTestManager testManager = null;

	protected boolean xalanVersionOK = false;

	public SchematronMyxComponent(){
	}

	public String getToolID(){
		return TOOL_ID;
	}

	@Override
	public void init(){
		//init stuff
	}

	@Override
	public void begin(){
		testManager = new SchematronTestManager(TOOL_ID, preferences);
		noticeadt.addNotice(TOOL_ID, "Schematron Tron Tool Initialized at [" + SystemUtils.getDateAndTime() + "]");
		String xalanVersion = SchematronUtils.getXalanVersion();
		if(xalanVersion == null){
			xalanVersionOK = false;
			noticeadt.addNotice(TOOL_ID, "Error: No Xalan version found.  Tests cannot run.");
		}
		else{
			xalanVersionOK = true;
			noticeadt.addNotice(TOOL_ID, "Xalan version " + xalanVersion);
		}
		reloadTests();
	}

	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_ISSUEADT)){
			issueadt = (IArchlightIssueADT)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_NOTICEADT)){
			noticeadt = (IArchlightNoticeADT)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_TESTADT)){
			testadt = (IArchlightTestADT)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_XARCHADT)){
			xarch = (XArchFlatInterface)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_PREFERENCES)){
			preferences = (IPreferenceStore)serviceObject;
		}
	}

	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_ISSUEADT)){
			issueadt = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_NOTICEADT)){
			noticeadt = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_TESTADT)){
			testadt = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_XARCHADT)){
			xarch = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_PREFERENCES)){
			preferences = null;
		}
	}

	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_TOOL)){
			return this;
		}
		return null;
	}

	public void reloadTests(){
		noticeadt.addNotice(TOOL_ID, "Reloading tests at [" + SystemUtils.getDateAndTime() + "]");
		ArchlightTest[] oldTests = testadt.getAllTests(TOOL_ID);
		testManager.reload();
		ArchlightTest[] newTests = testManager.getAllArchlightTests();
		testadt.removeTests(oldTests);
		testadt.addTests(newTests);

		for(Object warning: testManager.getWarnings()){
			if(warning instanceof String){
				noticeadt.addNotice(TOOL_ID, "Warning:" + warning);
			}
			else if(warning instanceof Throwable){
				Throwable t = (Throwable)warning;
				noticeadt.addNotice(TOOL_ID, "Error: " + t.getMessage(), t);
			}
		}
	}

	public void runTests(ObjRef documentRef, String[] testUIDs){
		List schematronTestErrorList = new ArrayList();
		List archlightTestResultList = new ArrayList();

		if(!xalanVersionOK){
			SchematronTestException ste = new SchematronTestException("Schematron requires Xalan; but the version of Xalan available was not sufficient to run Schematron tests.");
			schematronTestErrorList.add(ste);
		}
		else{
			SchematronTestFile[] testFiles = testManager.getAllTestFiles();
			Set filesToRun = new HashSet();
			String[] testUIDsToRun = testUIDs;
			for(String testUID: testUIDs){
				for(SchematronTestFile testFile: testFiles){
					ArchlightTest[] testsInFile = testFile.getArchlightTests();
					for(ArchlightTest testInFile: testsInFile){
						if(testUID.equals(testInFile.getUID())){
							filesToRun.add(testFile);
							break;
						}
					}
				}
			}
			ObjRef docRef = documentRef;
			String xmlDocument = xarch.serialize(docRef);
			int filesToRunSize = filesToRun.size();
			int f = 0;
			for(Iterator it = filesToRun.iterator(); it.hasNext(); f++){
				SchematronTestFile fileToRun = (SchematronTestFile)it.next();
				try{
					fileToRun = SchematronTestFile.create(fileToRun, testUIDsToRun);
				}
				catch(SchematronTestFileParseException stfpe){
					SchematronTestException ste = new SchematronTestException("Error parsing Schematron test file.", stfpe);
					schematronTestErrorList.add(ste);
				}
				if(fileToRun != null){
					SchematronTester tester = new SchematronTester(xmlDocument, fileToRun);
					noticeadt.addNotice(TOOL_ID, "Processing: " + fileToRun.getSourceURL());
					float pct = (float)f / (float)filesToRunSize;
					pct *= 100;
					if(pct == 0){
						pct = 5;
					}
					//sendToolStatus("Running Tests", (int)pct);
					try{
						tester.runTest();
						for(Object result: SchematronTestResultParser.parseTestResults(xarch, docRef, TOOL_ID, tester.getResult())){
							if(result instanceof SchematronTestException){
								schematronTestErrorList.add(result);
							}
							else if(result instanceof ArchlightTestResult){
								//System.out.println("result: " + results[i]);
								archlightTestResultList.add(result);
							}
						}
					}
					catch(SchematronInitializationException sie){
						SchematronTestException ste = new SchematronTestException("Error initializing Schematron", sie);
						schematronTestErrorList.add(ste);
					}
					catch(SchematronTestException ste){
						schematronTestErrorList.add(ste);
					}
					catch(Throwable t){
						SchematronTestException ste = new SchematronTestException("Error", t);
						schematronTestErrorList.add(ste);
					}
				}
			}
			//sendToolStatus("Idle", -1);
		}
		//Now we have two lists: a list of TronTestResults and a list of
		//SchematronTestExceptions if anything went wrong during testing.

		ArchlightTestResult[] testResults = (ArchlightTestResult[])archlightTestResultList.toArray(new ArchlightTestResult[0]);

		//Remove old issues
		ArchlightIssue[] oldIssues = issueadt.getAllIssues(documentRef, TOOL_ID);
		issueadt.removeIssues(oldIssues);

		//Store the new issues
		if(testResults.length > 0){
			List issueList = new ArrayList(testResults.length);
			for(ArchlightTestResult testResult: testResults){
				ArchlightIssue[] issues = testResult.getIssues();
				for(ArchlightIssue issue: issues){
					issueList.add(issue);
				}
			}
			ArchlightIssue[] allIssues = (ArchlightIssue[])issueList.toArray(new ArchlightIssue[issueList.size()]);
			issueadt.addIssues(allIssues);
		}

		//Store the errors
		SchematronTestException[] allExceptions = (SchematronTestException[])schematronTestErrorList.toArray(new SchematronTestException[schematronTestErrorList.size()]);
		for(SchematronTestException ste: allExceptions){
			ste.printStackTrace();
			noticeadt.addNotice(new ArchlightTestError(ste.getTestUID(), TOOL_ID, ste.getMessage(), null, ste));
		}
	}
}
