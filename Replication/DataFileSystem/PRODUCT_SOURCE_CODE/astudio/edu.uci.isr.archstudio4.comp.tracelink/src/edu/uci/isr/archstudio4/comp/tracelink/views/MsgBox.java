/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Hazel
 *
 */
public class MsgBox {

	/*
	 * Display a generic alert box
	 * 
	 */
	MessageBox alert;
	
	public MsgBox (Shell shell, int style) {
		if (style == SWT.YES || style == SWT.NO)
			alert = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION );
		else if (style == SWT.OK) 
			alert = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
		else if (style == SWT.CANCEL)
			alert = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
		
			
	}
	public int displayMsgBox(String message, String title) {
		//MessageBox alert = new MessageBox(shell, SWT.YES | SWT.NO);
		alert.setMessage(message);
		alert.setText(title);
		
		int result = alert.open();
		return result;
		
	}
}
