/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.preferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import edu.uci.isr.archstudio4.comp.tracelink.Activator;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.sysutils.SystemUtils;

/**
 * @author Hazel
 */
public class PreferencesView
	extends AbstractMyxSimpleBrick
	implements IPreferencesView{

	public static final IMyxName INTERFACE_NAME_IN_INVOKEPREFVIEW = MyxUtils.createName("invokeprefview");

	protected ITracelinkController controller;
	private IPreferenceStore prefStore = null;
	private File propertiesFile;

	private boolean hasPropValues = false;

	MyxRegistry myxr = MyxRegistry.getSharedInstance();

	@Override
	public void begin(){
		myxr.register(this);
		setupFiles();
		//only read from the file if the system properties are not set
		//we don't want to read from the file every time the preferences view is invoked		
		if(!hasPropValues){
			readPaths();
			//	hasPropValues = true;
		}

		//get the preferences info when this component is started up
		//setPreferences();
	}

	@Override
	public void init(){
		//prefStore = Activator.getDefault().getPreferenceStore();
		//prefs.addPropertyChangeListener(this);

		/*
		if (! hasPropValues) {
			//readPaths();
			//hasPropValues = true;
			hasPropValues = readPaths();
		}
		*/

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.myx.fw.IMyxProvidedServiceProvider#getServiceObject(edu.uci.isr.myx.fw.IMyxName)
	 */
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_INVOKEPREFVIEW)){
			return this;
		}
		else{
			return null;
		}
	}

	public void setITracelinkController(ITracelinkController controller){
		this.controller = controller;
	}

	public void invokePrefView(Shell parent){
		System.out.println("Preferences View*******************");
		//URL entry = Platform.getUserLocation().getURL();

		//for debugging
		//only read from the file if the system properties are not set
		//we don't want to read from the file every time the preferences view is invoked
		//if (System.getProperty(PreferencesConstants.PROP_REPORT) == null) {
		//if (! hasPropValues) {
		//readPaths();
		//hasPropValues = true;
		//hasPropValues = readPaths();
		//}

		if(parent == null){
			System.err.println("parent shell is null!");
			return;
		}

		Shell shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText("Preferences");
		shell.setSize(500, 350);
		shell.setLayout(new FillLayout());

		PreferencesViewer viewer = new PreferencesViewer(shell, 0, controller);
		shell.addDisposeListener(new DisposeAction(viewer));

		shell.open();

		while(!shell.isDisposed()){
			if(!shell.getDisplay().readAndDispatch()){
				shell.getDisplay().sleep();
			}
		}

	}

	/* 
	 * Method sets up the res directory in the user's workspace, then copies reports and rules
	 * TODO: copy of all the files work; however, there's a problem with the long user paths
	 *       for the XSLT processor.  Need to fix this.
	 */
	private void setupFiles(){
		// Copies the default configuration files to the workspace 
		try{
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			IPath destBasePath = Platform.getStateLocation(bundle);
			propertiesFile = destBasePath.append(PreferencesConstants.PROP_BUNDLE_STATE_PATH).toFile();
			if(!propertiesFile.exists()){

				URI sourceBaseURI = bundle.getEntry(PreferencesConstants.DEFAULT_CONFIG_BUNDLE_PATH).toURI();
				//TODO: fix the item below?
				Enumeration<URL> entries = bundle.findEntries(PreferencesConstants.DEFAULT_CONFIG_BUNDLE_PATH, null, true);
				for(; entries != null && entries.hasMoreElements();){
					URL url = entries.nextElement();
					URI uri = url.toURI();
					URI relativeURI = sourceBaseURI.relativize(uri);
					IPath destPath = destBasePath.append(relativeURI.toString());

					if(destPath.hasTrailingSeparator()){
						// it's a folder
						destPath.toFile().mkdirs();
					}
					else{
						// it's a file
						URL contentURL = FileLocator.resolve(url);
						SystemUtils.blt(contentURL.openStream(), new FileOutputStream(destPath.toFile()));
					}
				}
			}
			//now save the destination paths to the System properties
			//save the report paths
			String reportTemplatePath = destBasePath.append(PreferencesConstants.DEFAULT_REPORT_PATH
					+ PreferencesConstants.DEFAULT_REPORT_TEMPLATE_URL).toPortableString();
			System.setProperty(PreferencesConstants.DEFAULT_PROP_REPORT_TEMPLATE, 
					reportTemplatePath);
			System.out.println("report template file: " + reportTemplatePath);		
			
			String reportPath = destBasePath.append(PreferencesConstants.DEFAULT_REPORT_PATH
					+ PreferencesConstants.DEFAULT_REPORT_URL).toPortableString();
			System.setProperty(PreferencesConstants.DEFAULT_PROP_REPORT, 
					reportPath);			
			System.out.println("report file: " + reportPath);			
			
			//save the rule paths
			String ruleSessionPath = destBasePath.append(PreferencesConstants.DEFAULT_RULE_PATH
					+ PreferencesConstants.DEFAULT_RULE_SESSION_URL).toPortableString();
			System.setProperty(PreferencesConstants.DEFAULT_PROP_RULE_SESSION, 
					ruleSessionPath);			
			System.out.println("rule session file: " + ruleSessionPath);					
			
			String ruleSessionOutPath = destBasePath.append(PreferencesConstants.DEFAULT_RULE_PATH
					+ PreferencesConstants.DEFAULT_RULE_SESSION_OUT_URL).toPortableString();
			System.setProperty(PreferencesConstants.DEFAULT_PROP_RULE_SESSION_OUT, 
					ruleSessionOutPath);			
			System.out.println("rule session out file: " + ruleSessionOutPath);					
			
		}
		catch(Exception e){
			e.printStackTrace();
		}

		
		
		/*
		String pluginName = Activator.PLUGIN_ID;
		Bundle bundle = Platform.getBundle(pluginName);

		String propFileStr = getFixedPath(bundle, PreferencesConstants.PROP_BASE_URL + PreferencesConstants.PROP_URL);
		File sourcePropFile = new File(propFileStr);

		//propertiesFile = new File(fixedPath);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IPath location = root.getLocation();
		//location.toString()
		//String path = location.toString() + location.SEPARATOR + ".metadata" 
		//	+  location.SEPARATOR + PreferencesConstants.PROP_BASE_URL;
		String path = location.toString() + location.SEPARATOR + PreferencesConstants.PROP_BASE_URL;
		//URL entry = Platform.getInstallLocation().getURL();
		//String path = entry.toString() +  location.SEPARATOR + PreferencesConstants.PROP_BASE_URL;

		File usersResourceDir = new File(path);
		propertiesFile = new File(path + PreferencesConstants.PROP_URL);
		System.out.println("properties file " + propertiesFile.getAbsolutePath());
		File reportDir;
		File ruleDir;
		if(!usersResourceDir.exists()){
			try{
				SystemUtils.createDirectory(usersResourceDir);
			}
			catch(IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//copy the properties file

			try{
				SystemUtils.copyFile(sourcePropFile, propertiesFile);
			}
			catch(IOException e1){
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			//copy report directory
			String fixedReportPath = getFixedPath(bundle, PreferencesConstants.PROP_BASE_URL + PreferencesConstants.DEFAULT_REPORT_PATH);
			reportDir = new File(fixedReportPath);
			try{
				SystemUtils.copyDirectory(reportDir, usersResourceDir);
			}
			catch(IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//copy rule directory
			String fixedRulePath = getFixedPath(bundle, PreferencesConstants.PROP_BASE_URL + PreferencesConstants.DEFAULT_RULE_PATH);
			ruleDir = new File(fixedRulePath);
			try{
				SystemUtils.copyDirectory(ruleDir, usersResourceDir);
			}
			catch(IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("success");
		}
		System.setProperty(PreferencesConstants.DEFAULT_PROP_REPORT_TEMPLATE, usersResourceDir.toString() + usersResourceDir.separator + PreferencesConstants.DEFAULT_REPORT_PATH.substring(1) + usersResourceDir.separator + PreferencesConstants.DEFAULT_REPORT_TEMPLATE_URL);
		System.out.println("report template file: " + System.getProperty(PreferencesConstants.DEFAULT_PROP_REPORT_TEMPLATE));
		System.setProperty(PreferencesConstants.DEFAULT_PROP_REPORT, usersResourceDir.toString() + usersResourceDir.separator + PreferencesConstants.DEFAULT_REPORT_PATH.substring(1) + usersResourceDir.separator + PreferencesConstants.DEFAULT_REPORT_URL.substring(1));
		System.out.println("report file: " + System.getProperty(PreferencesConstants.DEFAULT_PROP_REPORT));
		System.setProperty(PreferencesConstants.DEFAULT_PROP_RULE_SESSION, usersResourceDir.toString() + usersResourceDir.separator + PreferencesConstants.DEFAULT_RULE_PATH.substring(1) + usersResourceDir.separator + PreferencesConstants.DEFAULT_RULE_SESSION_URL.substring(1));
		System.out.println("rule session file: " + System.getProperty(PreferencesConstants.DEFAULT_PROP_RULE_SESSION));

		System.setProperty(PreferencesConstants.DEFAULT_PROP_RULE_SESSION_OUT, usersResourceDir.toString() + usersResourceDir.separator + PreferencesConstants.DEFAULT_RULE_PATH.substring(1) + usersResourceDir.separator + PreferencesConstants.DEFAULT_RULE_SESSION_OUT_URL.substring(1));
		System.out.println("rule session file: " + System.getProperty(PreferencesConstants.DEFAULT_PROP_RULE_SESSION_OUT));
		*/
	}

	/*
	 * Method sets the preferences to the System properties
	 * 
	 
	private void setPreferences() {
		try {
			
			String pluginName = Activator.PLUGIN_ID;
			Bundle bundle = Platform.getBundle(pluginName);
			
			//System.out.println(path);
			//propertiesFile = new File()
			System.out.println("properties file: " + propertiesFile.getAbsolutePath());
			System.setProperty(PreferencesConstants.PROP_AS_INSTALL_PATH, propertiesFile.getPath());
			System.out.println("abs: " + propertiesFile.getAbsolutePath());
			
			//get the report paths
			String fixedReportTemplatePath = getFixedPath(bundle, PreferencesConstants.PROP_BASE_URL + 
					PreferencesConstants.DEFAULT_REPORT_PATH +
					PreferencesConstants.DEFAULT_REPORT_TEMPLATE_URL);
			System.setProperty(PreferencesConstants.DEFAULT_PROP_REPORT_TEMPLATE, 
					fixedReportTemplatePath);
			System.out.println("report template file: " + fixedReportTemplatePath);		
			
			String fixedReportPath = getFixedPath(bundle, PreferencesConstants.PROP_BASE_URL + 
					PreferencesConstants.DEFAULT_REPORT_PATH +
					PreferencesConstants.DEFAULT_REPORT_URL);
			System.setProperty(PreferencesConstants.DEFAULT_PROP_REPORT, 
					fixedReportPath);
			System.out.println("report file: " + fixedReportPath);			
			
			//get the rule paths
			String fixedRuleSessionPath = getFixedPath(bundle, PreferencesConstants.PROP_BASE_URL + 
					PreferencesConstants.DEFAULT_RULE_PATH +
					PreferencesConstants.DEFAULT_RULE_SESSION_URL);
			System.setProperty(PreferencesConstants.DEFAULT_PROP_RULE_SESSION, 
					fixedRuleSessionPath);
			System.out.println("rule session file: " + fixedRuleSessionPath);
			
			String fixedRuleSessionOutPath = getFixedPath(bundle, PreferencesConstants.PROP_BASE_URL + 
					PreferencesConstants.DEFAULT_RULE_PATH +
					PreferencesConstants.DEFAULT_RULE_SESSION_OUT_URL);
			System.setProperty(PreferencesConstants.DEFAULT_PROP_RULE_SESSION_OUT, 
					fixedRuleSessionOutPath);
			System.out.println("rule session file: " + fixedRuleSessionOutPath);
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	*/

//	/*
//	 * Method determines the path based on the plugin installation path
//	 * @param bundle for this plugin
//	 * @param relative path of the resource
//	 * @return path that conforms to the current OS
//	 */
//	private String getFixedPath(Bundle bundle, String relativePath){
//		URL entry = bundle.getEntry(relativePath);
//		try{
//			String path = FileLocator.toFileURL(entry).getPath();
//			//fix the path to conform to the current OS
//			String fixedPath = new File(path).getPath();
//			return fixedPath;
//
//		}
//		catch(IOException e){
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//
//	}
	/*
	 * Method reads a config file and stores the values as System.properties
	 */
	private void readPaths(){
		String curToken;
		String fileName;
		try{
			Scanner scanner = new Scanner(propertiesFile);

			while(scanner.hasNext()){
				curToken = scanner.next();
				if(curToken.equalsIgnoreCase(PreferencesConstants.PROP_REPORT)){
					//counter++;
					curToken = scanner.nextLine();
					//if ( (curToken.trim() != null) && (! curToken.trim().contains("")) ) 
					System.setProperty(PreferencesConstants.PROP_REPORT, curToken.trim());

					//PreferencesConstants.getFileSeparator(curToken.trim()) +
					//PreferencesConstants.getFileName(PreferencesConstants.DEFAULT_REPORT_URL));
				}
				else if(curToken.equalsIgnoreCase(PreferencesConstants.PROP_RULE)){
					//counter++;
					curToken = scanner.nextLine();
					System.setProperty(PreferencesConstants.PROP_RULE, curToken.trim());
					// + PreferencesConstants.DEFAULT_RULE_URL);
					//PreferencesConstants.getFileSeparator(curToken.trim()) +
					//PreferencesConstants.getFileName(PreferencesConstants.DEFAULT_RULE_URL));
				}
				else if(curToken.equalsIgnoreCase(PreferencesConstants.PROP_BROWSER)){
					//counter++;
					curToken = scanner.nextLine();
					System.setProperty(PreferencesConstants.PROP_BROWSER, curToken.trim());
				}
			}
			scanner.close();
		}
		catch(Exception ex){
			System.out.println(ex.getMessage());
		}
	}

	/*
	 * Method reads paths from IPreferenceStore
	 * TODO: completely remove the System.setProperty and just use the IPreferenceStore
	 *       For now, use System.setProperty in case we need to go back and read values from a file
	 
	private boolean readPaths() {
		//the compiler does not like this
		//IPreferenceStore ps = AbstractUIPlugin.getPreferenceStore();
		
		//reportPath =
		//System.setProperty(PreferencesConstants.PROP_REPORT, 
		//		AbstractUIPlugin.getPreferenceStore().getDefaultString(PreferencesConstants.PROP_REPORT) );
		boolean success = false;
		
		if (Activator.getDefault() != null) {
			prefStore = Activator.getDefault().getPreferenceStore();
			String reportPath = prefStore.getDefaultString(PreferencesConstants.PROP_REPORT);
			String rulePath = prefStore.getDefaultString(PreferencesConstants.PROP_RULE);
			String browserPath = prefStore.getDefaultString(PreferencesConstants.PROP_BROWSER);
			
			System.setProperty(PreferencesConstants.PROP_REPORT, reportPath);
			System.setProperty(PreferencesConstants.PROP_RULE, rulePath);
			System.setProperty(PreferencesConstants.PROP_BROWSER, browserPath);
			
			success = true;
		}

		return success;
	}
	*/

	/*
	 * Save the paths to the properties file
	 */
	private void savePaths(){
		System.out.println("save paths...");
		Writer output = null;
		//need to save the name of the file so that we can
		//create a new file with the same name
		String filePath = propertiesFile.getPath(); //this includes the resource filename

		if(propertiesFile.exists()){
			boolean success = propertiesFile.delete();
			System.out.println("able to delete " + success);
		}
		propertiesFile = new File(filePath);
		System.out.println("new properties file " + propertiesFile.getAbsolutePath());
		System.out.println("system properties " + System.getProperty(PreferencesConstants.PROP_REPORT));
		try{
			FileWriter fwriter = new FileWriter(propertiesFile);
			output = new BufferedWriter(fwriter);
			output.write(PreferencesConstants.PROP_REPORT + " " + System.getProperty(PreferencesConstants.PROP_REPORT));
			output.write("\n"); //new line
			output.write(PreferencesConstants.PROP_RULE + " " + System.getProperty(PreferencesConstants.PROP_RULE));
			output.write("\n"); //new line
			output.write(PreferencesConstants.PROP_BROWSER + " " + System.getProperty(PreferencesConstants.PROP_BROWSER));

			output.close();
			fwriter.close();
		}
		catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Your file has been written");
	}

	/*
	 * Save properties to the IPreferenceStore
	 * TODO: remove System.getProperty (see comment in readPaths() )
	 
	private void savePaths() {
		String reportPath = System.getProperty(PreferencesConstants.PROP_REPORT);
		String rulePath = System.getProperty(PreferencesConstants.PROP_RULE);
		String browserPath = System.getProperty(PreferencesConstants.PROP_BROWSER);
		
		prefStore.setDefault(PreferencesConstants.PROP_REPORT, reportPath);
		prefStore.setDefault(PreferencesConstants.PROP_RULE, rulePath);
		prefStore.setDefault(PreferencesConstants.PROP_BROWSER, browserPath);
	}
	*/

	private class DisposeAction
		implements DisposeListener{

		private PreferencesViewer viewer;

		public DisposeAction(PreferencesViewer viewer){
			this.viewer = viewer;
		}

		public void widgetDisposed(DisposeEvent e){
			//check if read is ok
			controller.setDisplayedAttributeNames(viewer.getAttributeNames());
			controller.updateViews();
			savePaths();
			System.out.println("Closing Preferences");
		}
	}

}
