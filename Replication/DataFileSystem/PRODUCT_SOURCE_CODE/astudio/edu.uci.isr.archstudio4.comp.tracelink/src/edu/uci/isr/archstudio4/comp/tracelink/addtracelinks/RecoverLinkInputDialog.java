/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.addtracelinks;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Hazel
 *
 */
//TODO: This has a lot of similarities with the ManualLinkInputDialog
//      Refactor and do a generic InputDialog
public class RecoverLinkInputDialog extends Dialog {
	
	String uri = "";


	public RecoverLinkInputDialog(Shell shell) {
		super(shell);
	}
	
	/**
	 * Method opens the dialog for specifying a URL of the repository
	 * @param label the caption for the textbox
	 * @return URI to add as a trace endpoint
	 */
	public String open(String caption) {
		
		Shell parent = getParent();
		final Shell shell =
			new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		shell.setText("Recover Link");
		//shell.setSize(1000, 100);
		//sets the default location to almost center of the parent window
		shell.setBounds(0, 0, 600, 150);	
		RowData rData = new RowData(300, 15);
	
		//2 columns, false - equal column widths
	    shell.setLayout(new GridLayout(2, false));
	
	    Label label = new Label(shell, SWT.NULL);
	    //label.setText("Enter the URL of the Repository:");
	    label.setText(caption);
	
	    final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
	    //horizontal alignment = swt.fill, true - grab excess horizontal space
	    GridData gridData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
	    //gridData.minimumWidth = 500;	//this does not seem to work
	    //text.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
	    text.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
	    text.setText("                                                   ");
	    
	    //text.setLayoutData(rData);   //this is failing
	
	    /*
	    Button buttonSelectFile = new Button(shell, SWT.PUSH);
	    buttonSelectFile.setText("...");
	    */
	    
	    //final Button buttonOK = new Button(shell, SWT.PUSH);
	    final Button buttonOK = new Button(shell, SWT.PUSH);
	    buttonOK.setText("OK");
	    buttonOK.setLayoutData(new GridData(SWT.END, SWT.DEFAULT, true, false));
	    Button buttonCancel = new Button(shell, SWT.PUSH);
	    buttonCancel.setText("Cancel");
	    //buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
	    
	    text.addListener(SWT.Modify, new Listener() {
	    	public void handleEvent(Event event) {
	    		try {
	    			//value = new Double(text.getText());
	    			uri = text.getText();
	    			buttonOK.setEnabled(true);
	    		} catch (Exception e) {
	    			buttonOK.setEnabled(false);
	    		}
	    	}
	    });
	
	    buttonOK.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		uri = text.getText();
	    		shell.dispose();
	    	}
	    });
	
	    buttonCancel.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		//value = null;
	    		shell.dispose();
	    	}
	    });
	    
	    //TODO: check the purpose of the following
	    /*
	    shell.addListener(SWT.Traverse, new Listener() {
	    	public void handleEvent(Event event) {
	    		if(event.detail == SWT.TRAVERSE_ESCAPE)
	    			event.doit = false;
	    	}
	    });
	    */
	
	    //text.setText("");
	    shell.pack();
	    shell.open();
	
	    Display display = parent.getDisplay();
	    
	    while (!shell.isDisposed()) {
	      if (!display.readAndDispatch())
	        display.sleep();
	    }
	    
	    return uri;
	  }
	
}

 