/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.addtracelinks;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Hazel
 * Class enables users to select third party tools to recover links
 */
public class RecoverLinkToolDialog extends Dialog {

	//7/31/09 - comment out Lucene and trac temporarily because license notice still need to be added
	
	//public static final int LUCENE = 0;
	//public static final int TRAC = 1;
	//public static final int GOOGLE = 2;
	
	public static final int GOOGLE = 0;
	public static final String LUCENE_NAME = "Lucene";
	public static final String LUCENE_ALIAS = "Lucene";
	public static final String TRAC_NAME = "Trac Bug and Issue Tracking System";
	public static final String TRAC_ALIAS = "Trac";
	public static final String GOOGLE_NAME = "Google";
	public static final String GOOGLE_ALIAS = "Google";
	
	//private static final String[] TOOLS = { "Lucene",
	//      "Trac Bug and Issue Tracking System", "Google"};
	//7/31/09 - comment out Lucene and trac temporarily because license notice still need to be added 
	//private static final String[] TOOLS = { LUCENE_NAME,
	//      TRAC_NAME, GOOGLE_NAME};
	
	private static final String[] TOOLS = { GOOGLE_NAME};

	
	private int selectedTool;
	
	public RecoverLinkToolDialog(Shell parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Method opens the dialog for specifying a URL of the repository
	 * @return URI to add as a trace endpoint
	 */
	public int open() {
		
		Shell parent = getParent();
		final Shell shell =
			new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		shell.setText("Recover Links");
		//shell.setSize(600, 150);
		shell.setBounds(0, 0, 600, 150);
		
		//2 columns, false - equal column widths
	    shell.setLayout(new GridLayout(2, false));
	
	    Label label = new Label(shell, SWT.NULL);
	    label.setText("Select Recover Tool");
	
	    // Create the dropdown to allow icon selection
	    final Combo tools = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
	    for (int i = 0, n = TOOLS.length; i < n; i++)
	      tools.add(TOOLS[i]);
	    tools.select(0);

	    final Button buttonOK = new Button(shell, SWT.PUSH);
	    buttonOK.setText("OK");
	    buttonOK.setLayoutData(new GridData(SWT.END, SWT.DEFAULT, true, false));
	    Button buttonCancel = new Button(shell, SWT.PUSH);
	    buttonCancel.setText("Cancel");
		
	    buttonOK.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		selectedTool = tools.getSelectionIndex();
	    		shell.dispose();
	    	}
	    });
	
	    buttonCancel.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		selectedTool = -1;
	    		shell.dispose();
	    		
	    	}
	    });
	    shell.pack();
	    shell.open();
	
	    Display display = parent.getDisplay();
	    
	    while (!shell.isDisposed()) {
	      if (!display.readAndDispatch())
	        display.sleep();
	    }
	    return selectedTool;
	    
	}
		
}
