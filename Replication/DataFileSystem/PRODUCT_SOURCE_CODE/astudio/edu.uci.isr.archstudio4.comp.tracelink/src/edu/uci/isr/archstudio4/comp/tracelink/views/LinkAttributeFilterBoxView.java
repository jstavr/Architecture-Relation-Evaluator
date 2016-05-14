package edu.uci.isr.archstudio4.comp.tracelink.views;


import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;



public class LinkAttributeFilterBoxView implements IWidget{
	
	private ITracelinkController controller;
	
	private Composite	parent;
	private ComboViewer	combo;
	private Text		text;
	
	public LinkAttributeFilterBoxView(Composite parent, int style, ITracelinkController controller) {
		this.controller = controller;
		
		this.parent = new Composite(parent, style);
		
		final int layoutStyle = GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL; 
		this.parent.setLayoutData(new GridData(layoutStyle));
		
		this.parent.setLayout(new FillLayout());
		
		combo = new ComboViewer(this.parent, style);
		text = new Text(this.parent, style | SWT.SINGLE);
		
		text.addModifyListener(new InputListener());
		addComboOptions(combo);
		
		controller.registerView(this);
		
	}
	
	public Widget getWidget() {
		return parent;
	}
	
	public void update() {
		System.out.println("UPDATE: LinkAttributeFilterBox");
		
		//save the old combo value
		//H: need to first check if this is not null
		/*
		if(combo != null) {
			//this method is failing
			ISelection selected = combo.getSelection();
			
			combo.getCombo().removeAll();
			addComboOptions(combo);
			
			//set the combo selection
			//if (hasSelected(selected))
				combo.setSelection(selected, true);
			
		}
		*/
		
		//rewrite the above code to handle the exception
		ISelection selected;
		try {
			selected = combo.getSelection();
			combo.getCombo().removeAll();
			addComboOptions(combo);
			
			//set the combo selection
			//if (hasSelected(selected))
				combo.setSelection(selected, true);

		} 
		catch (SWTException e) {
			System.err.println(e.getStackTrace());
		}
		

	}
	
	public void addComboOptions(ComboViewer combo) {
		combo.add(controller.getAttributeNames());
	}
	
	
	public void filterLinks() {
		String key = combo.getCombo().getText();
		String property = text.getText();
		controller.setFilter(key, property);
	}
	
	private class InputListener implements ModifyListener{
		//
		public void modifyText(ModifyEvent e) {
			filterLinks();
			
		}
	}
	
}
