package edu.uci.isr.archstudio4.comp.archipelago;

import org.eclipse.jface.viewers.Viewer;

public interface IArchipelagoTreeContentProvider{
	public Object getParent(Object element, Object parentFromPreviousProvider);
	public Object[] getChildren(Object parentElement, Object[] childrenFromPreviousProvider);
	public boolean hasChildren(Object element, boolean hasChildrenFromPreviousProvider);
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput);
	public void dispose();
}
