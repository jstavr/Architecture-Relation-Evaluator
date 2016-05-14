package edu.uci.isr.archstudio4.comp.tracelink.views;

import org.eclipse.swt.widgets.Widget;

public interface IWidget {
	
	
	/**
	 * @return  a SWT Widget to be added to a master view.
	 */
	public Widget getWidget();
	
	/**
	 * Notifies the IWidget to update itself
	 */
	public void update();
	
}
