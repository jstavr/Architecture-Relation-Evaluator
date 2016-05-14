package edu.uci.isr.archstudio4.rationale.dialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import edu.uci.isr.xarchflat.ObjRef;

public class CustomizedMessageDialog extends MessageDialog{

	public CustomizedMessageDialog(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
	}
	protected int buttonId;
	
	@Override
	protected void buttonPressed(int buttonId) {
		this.buttonId = buttonId;
		this.close();
	}
	
	public int openDialog() {
		this.open();
		return buttonId;
	}
	

}
