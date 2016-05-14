package edu.uci.isr.archstudio4.comp.tracelink.addtracelinks;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class ManualLinkInputDialog extends Dialog{
	
	String uri = "";

	public ManualLinkInputDialog(Shell shell) {
		super(shell);
	}
	
	/**
	 * Method opens the dialog for manually specifying a trace endpoint
	 * @return URI to add as a trace endpoint
	 */
	public String open() {
		
		Shell parent = getParent();
		final Shell shell =
			new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		shell.setText("Manual Link");
		//shell.setSize(600, 75);
		//sets the default location to almost center of the parent window
		shell.setBounds(0, 0, 600, 75);
	
		//3 columns, false - equal column widths
	    shell.setLayout(new GridLayout(3, false));
	
	    Label label = new Label(shell, SWT.NULL);
	    label.setText("Enter a URL or select a file to link:");
	
	    final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
	    //horizontal alignment = swt.fill, true - grab excess horizontal space
	    text.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
	    text.setText("                                                   ");
	    
	    Button buttonSelectFile = new Button(shell, SWT.PUSH);
	    buttonSelectFile.setText("...");
	    //buttonSelectFile.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
	    
	    //final Button buttonOK = new Button(shell, SWT.PUSH);
	    final Button buttonOK = new Button(shell, SWT.PUSH);
	    buttonOK.setText("Ok");
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
	
	    buttonSelectFile.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
		        fd.setText("Select File to link");
		        fd.setFilterPath("C:/");
		        String[] filterExt = { "*.txt", "*.doc", ".rtf", "*.*" };
		        fd.setFilterExtensions(filterExt);
		        String file = fd.open();
		        System.out.println(file);
		        text.setText(file);
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
	    //return text.getText();
	  }
	
}
