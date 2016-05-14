package edu.uci.isr.archstudio4.comp.archlight.tools.schematron;

//import archstudio.comp.preferences.IPreferences;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.uci.isr.archstudio4.archlight.ArchlightTest;
import edu.uci.isr.sysutils.SystemUtils;

public class SchematronTestManager{

	public static final String DEFAULT_TEST_FILE_URL = "res:/edu/uci/isr/archstudio4/comp/archlight/tools/schematron/res/";
	//public static final String DEFAULT_TEST_FILE_URL = "res/";

	public static final String RULE_FILE_INDEX_NAME = "rulefileindex.txt";

	protected String toolID;

	protected IPreferenceStore preferences;

	protected String[] testFileBaseURLs = null;

	protected String[] testFileURLs = null;

	protected SchematronTestFile[] testFiles = null;

	protected ArchlightTest[] archlightTests = null;

	// Either strings or Throwables
	protected Object[] warnings = new Object[0];

	public SchematronTestManager(String toolID, IPreferenceStore preferences){
		this.toolID = toolID;
		this.preferences = preferences;
	}

	public SchematronTestFile[] getAllTestFiles(){
		return testFiles;
	}

	public ArchlightTest getArchlightTest(String uid){
		ArchlightTest[] archlightTests = getAllArchlightTests();
		for(ArchlightTest element: archlightTests){
			if(element.getUID().equals(uid)){
				return element;
			}
		}
		return null;
	}

	public ArchlightTest[] getAllArchlightTests(){
		return archlightTests;
	}

	public Object[] getWarnings(){
		return warnings;
	}

	public void reload(){
		clearWarnings();
		reloadBaseURLs();
		reloadFileURLs();
		reloadTestFiles();
		reloadArchlightTests();
	}

	private void clearWarnings(){
		warnings = new Object[0];
	}

	private void reloadBaseURLs(){
		List testFileBaseURLList = new ArrayList();
		testFileBaseURLList.add(DEFAULT_TEST_FILE_URL);

		int i = 0;
		String s = preferences.getString(SchematronConstants.PREF_TEST_FILE_PATHS);
		for(String p: s.split(File.pathSeparator)){
			//System.err.println(p);
			String url = new File(p).toURI().toString();
			//System.err.println(url);
			testFileBaseURLList.add(url);
		}
		testFileBaseURLs = (String[])testFileBaseURLList.toArray(new String[0]);
	}

	private void reloadFileURLs(){
		List testFileURLList = new ArrayList();
		List newWarningsList = new ArrayList();

		for(String urlString: testFileBaseURLs){
			if(!urlString.endsWith("/")){
				urlString += "/";
			}
			String ruleFileIndexURLString = urlString + RULE_FILE_INDEX_NAME;
			try{
				InputStream is = SystemUtils.openURL(ruleFileIndexURLString, this.getClass());
				if(is == null){
					throw new FileNotFoundException("Could not find file: " + ruleFileIndexURLString);
				}
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				while(true){
					String fileName = br.readLine();
					if(fileName == null){
						break;
					}
					fileName = fileName.trim();
					if(fileName.length() == 0){
						continue;
					}
					String ruleFileURLString = urlString + fileName;
					testFileURLList.add(ruleFileURLString);
				}
				br.close();
			}
			catch(MalformedURLException mue){
				newWarningsList.add(mue);
			}
			catch(FileNotFoundException fnfe){
				newWarningsList.add(fnfe);
			}
			catch(IOException ioe){
				newWarningsList.add(ioe);
			}
		}
		testFileURLs = (String[])testFileURLList.toArray(new String[0]);
		newWarningsList.addAll(Arrays.asList(warnings));
		warnings = newWarningsList.toArray();
	}

	private void reloadTestFiles(){
		List testFileList = new ArrayList();
		List newWarningsList = new ArrayList();

		for(String element: testFileURLs){
			try{
				SchematronTestFile stf = SchematronTestFile.create(toolID, element);
				testFileList.add(stf);
				String[] additionalWarnings = stf.getParseWarnings();
				for(String element2: additionalWarnings){
					newWarningsList.add(element2);
				}
			}
			catch(SchematronTestFileParseException stfpe){
				newWarningsList.add(stfpe);
			}
			catch(MalformedURLException mue){
				newWarningsList.add(mue);
			}
			catch(FileNotFoundException fnfe){
				newWarningsList.add(fnfe);
			}
			catch(IOException ioe){
				newWarningsList.add(ioe);
			}
		}

		testFiles = (SchematronTestFile[])testFileList.toArray(new SchematronTestFile[0]);
		newWarningsList.addAll(Arrays.asList(warnings));
		warnings = newWarningsList.toArray();
	}

	private void reloadArchlightTests(){
		List archlightTestList = new ArrayList();
		List newWarningsList = new ArrayList();
		Set testUIDs = new HashSet();
		for(SchematronTestFile element: testFiles){
			ArchlightTest[] fileTests = element.getArchlightTests();
			for(ArchlightTest element2: fileTests){
				String testUID = element2.getUID();
				if(testUIDs.contains(testUID)){
					SchematronInitializationException e = new SchematronInitializationException("Duplicate Test UID: " + testUID);
					newWarningsList.add(e);
				}
				else{
					archlightTestList.add(element2);
					testUIDs.add(testUID);
				}
			}
		}
		newWarningsList.addAll(Arrays.asList(warnings));
		warnings = newWarningsList.toArray();
		archlightTests = (ArchlightTest[])archlightTestList.toArray(new ArchlightTest[0]);
	}
}
