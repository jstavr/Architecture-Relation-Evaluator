package edu.uci.isr.archstudio4.graphlayout.gui;

import org.eclipse.swt.widgets.Composite;

import edu.uci.isr.archstudio4.graphlayout.GraphLayoutParameters;

interface IGraphLayoutParameterPanel{
	public void createPanel(Composite parent);
	
	public void storeParameters(GraphLayoutParameters params) throws DataValidationException;
	public void loadParameters(GraphLayoutParameters params);
}
