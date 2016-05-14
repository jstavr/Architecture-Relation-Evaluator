/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.preferences;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import edu.uci.isr.archstudio4.comp.tracelink.analysis.ISpecifyRule;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.SimpleTracelinkController;

/**
 * @author dpurpura
 *
 */
public class PreferencesViewer extends Composite{
	
	protected SimpleTracelinkController controller;

	protected Button notButton;
	protected ComboViewer attributeCombo;
	protected ComboViewer operatorCombo;
	protected Text text;
	protected ComboViewer andOrCombo;
	protected Button addButton;

	protected List rules;
	protected Button submitButton;
	
	
	protected Set<String> attributes;

	public PreferencesViewer(Composite parent, int style,
			ITracelinkController controller) {

		super(parent, style);
		
		this.controller = (SimpleTracelinkController) controller;
		
		attributes = new LinkedHashSet<String>();
		
		GridLayout layout = new GridLayout(1, false);
		this.setLayout(layout);

		init();
	}

	protected void init() {
		Label label;
		
		int style = SWT.None;
		RowLayout layout;
		RowData rData = new RowData(300, 15);
		
		//left justifies the widgets
		int gridStyle = GridData.GRAB_HORIZONTAL
		| GridData.HORIZONTAL_ALIGN_BEGINNING ;
		
		//centers the widgets
		int gridStyle2 = GridData.HORIZONTAL_ALIGN_FILL 
		| GridData.GRAB_HORIZONTAL
		| GridData.HORIZONTAL_ALIGN_CENTER ;

		Composite row;
		
		//-- Row 1 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle));
		layout = new RowLayout();
		layout.justify = true;
		row.setLayout(layout);
		
		label = new Label(row, style);
		label.setText("Specify attributes to view");
		
		//-- Row 2 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle2));
		row.setLayout(new FillLayout());
		
		String[] attributes = controller.getAttributeNames();
		String[] displayedAttributes = controller.getDisplayedAttributeNames();
		
		Table table = new Table (row, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		for (String attribute : attributes) {
			TableItem item = new TableItem (table, SWT.NONE);
			item.setText(attribute);
			
			for (String displayed : displayedAttributes) {
				if (attribute.equalsIgnoreCase(displayed)) {
					item.setChecked(true);
					this.attributes.add(attribute);
					break;
				}
					
			}
			
		}
		table.setSize (100, 300);
		table.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event event) {
				if (event.item instanceof TableItem) {
					String attribute = ((TableItem) event.item).getText();
					Set<String> attributes = PreferencesViewer.this.attributes; 
					
					if (event.detail == SWT.CHECK) {
						if (attributes.contains(attribute))
							attributes.remove(attribute);
						else
							attributes.add(attribute);
					}
				}
			}
		});


		//-- Row 3 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle2));
		layout = new RowLayout();
		layout.justify = true;
		layout.pack = true;
		row.setLayout(layout);
		
		label = new Label(row, style);
		label.setText("Report Path");
		
		final Text textReport = new Text(row, SWT.BORDER); 
		textReport.setLayoutData(rData);
		System.out.println("reporth path " + System.getProperty(PreferencesConstants.PROP_REPORT));
		if (System.getProperty(PreferencesConstants.PROP_REPORT)!=null)
			textReport.setText(System.getProperty(PreferencesConstants.PROP_REPORT));
		
		Button buttonReportFile = new Button(row, SWT.PUSH);
		buttonReportFile.setText("...");
		buttonReportFile.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		/*
				DirectoryDialog dialog = new DirectoryDialog(getShell());
			    dialog.setFilterPath("c:\\"); // TODO: this is Windows specific.  Make this platform specific
			    dialog.setText("Select directory to store report");
			    String dirToSearch = dialog.open();
			    */
	    		String path = getDirectoryPath("Select directory to store report");
			    textReport.setText(path);
	    	}
		});
		
		
		//-- Row 4 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle2));
		layout = new RowLayout();
		layout.justify = true;
		layout.pack = true;
		row.setLayout(layout);
		
		label = new Label(row, style);
		label.setText("Rules Path");
		
		final Text textRules = new Text(row, SWT.BORDER); 
		textRules.setLayoutData(rData);
		if (System.getProperty(PreferencesConstants.PROP_RULE)!=null)
			textRules.setText(System.getProperty(PreferencesConstants.PROP_RULE));
		
		Button buttonRulesFile = new Button(row, SWT.PUSH);
		buttonRulesFile.setText("...");
		buttonRulesFile.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		/*
				DirectoryDialog dialog = new DirectoryDialog(getShell());
			    dialog.setFilterPath("c:\\"); // TODO: this is Windows specific.  Make this platform specific
			    dialog.setText("Select rule directory");
			    String dirToSearch = dialog.open();
			    */
	    		String path = getDirectoryPath("Select rule directory");
			    textRules.setText(path);
	    	}
		});

		
		//-- Row 5 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle2));
		layout = new RowLayout();
		layout.justify = true;
		layout.pack = true;

		row.setLayout(layout);
		
		label = new Label(row, style);
		label.setText("Browser User Path");
		
		final Text textBrowser = new Text(row, SWT.BORDER); 
		textBrowser.setLayoutData(rData);
		if (System.getProperty(PreferencesConstants.PROP_BROWSER)!= null)
			textBrowser.setText(System.getProperty(PreferencesConstants.PROP_BROWSER));
		
		Button buttonBrowser = new Button(row, SWT.PUSH);
		buttonBrowser.setText("...");
		buttonBrowser.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		/*
				DirectoryDialog dialog = new DirectoryDialog(getShell());
			    dialog.setFilterPath("c:\\"); // TODO: this is Windows specific.  Make this platform specific
			    dialog.setText("Select browser user directory");
			    String dirToSearch = dialog.open();
			    */
	    		String path = getDirectoryPath("Select browser user directory");
			    textBrowser.setText(path);
	    	}
		});
				
		//-- Row 6 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle2));
		layout = new RowLayout();
		layout.justify = true;
		row.setLayout(layout);

		submitButton = new Button(row, style);
		submitButton.setText("Save and Close");
		
		submitButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
				//save the preferences to the System properties
	    		System.setProperty(PreferencesConstants.PROP_REPORT, textReport.getText());
	    		System.out.println("set the properties report " + textReport.getText());
	    		System.setProperty(PreferencesConstants.PROP_RULE, textRules.getText());
	    		System.setProperty(PreferencesConstants.PROP_BROWSER, textBrowser.getText());
	    		PreferencesViewer.this.getShell().close();
	    	}
		});		
		
	}
	
	private String getDirectoryPath(String dialogTitle) {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		URL userWorkspace = Platform.getUserLocation().getURL();
	    dialog.setFilterPath(userWorkspace.getPath()); 
	    dialog.setText(dialogTitle);
	    String dirToSearch = dialog.open();
	    return dirToSearch;
	}
	
	public String[] getAttributeNames() {
		return attributes.toArray(new String[attributes.size()]);
	}
	

}
