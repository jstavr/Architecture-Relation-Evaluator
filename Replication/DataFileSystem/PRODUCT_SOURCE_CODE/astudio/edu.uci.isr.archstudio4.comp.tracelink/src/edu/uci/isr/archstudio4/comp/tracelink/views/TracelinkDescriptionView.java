/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;

/**
 * @author dpurpura
 */
public class TracelinkDescriptionView
	implements IWidget{

	private ITracelinkController controller;

	private Composite parent;
	private Label descriptionLabel;
	private Label classificationLabel;

	public TracelinkDescriptionView(Composite parent, int style, ITracelinkController controller){
		this.controller = controller;

		this.parent = new Composite(parent, SWT.None);

		final int layoutStyle = GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL;
		this.parent.setLayoutData(new GridData(layoutStyle));

		this.parent.setLayout(new FillLayout(SWT.VERTICAL));

		descriptionLabel = new Label(this.parent, SWT.SHADOW_NONE);
		classificationLabel = new Label(this.parent, SWT.SHADOW_NONE);

		descriptionLabel.setText("Description: N/A");
		classificationLabel.setText("Classification: N/A");

		controller.registerView(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.archstudio4.comp.tracelink.views.IWidget#getWidget()
	 */
	public Widget getWidget(){
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.archstudio4.comp.tracelink.views.IWidget#update()
	 */
	public void update(){
		descriptionLabel.setText("Description: " + controller.getDescription());
		classificationLabel.setText("Classification: " + controller.getClassification());
	}

}
