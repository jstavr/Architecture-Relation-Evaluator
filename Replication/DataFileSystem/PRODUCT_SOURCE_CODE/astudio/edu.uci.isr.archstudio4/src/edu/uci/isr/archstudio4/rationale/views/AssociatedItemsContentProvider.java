package edu.uci.isr.archstudio4.rationale.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import edu.uci.isr.archstudio4.rationale.RationaleViewManager;

public class AssociatedItemsContentProvider implements IStructuredContentProvider{

	protected RationaleViewManager rationaleViewManager;
	public AssociatedItemsContentProvider(RationaleViewManager rationaleViewManager) {
		this.rationaleViewManager = rationaleViewManager;
	}

	public Object[] getElements(Object arg0) {
		return rationaleViewManager.getAssociatedItemsForCurrentListOfRationales();
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		viewer.refresh();
	}
}
