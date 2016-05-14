package edu.uci.isr.archstudio4.comp.archipelago;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.dnd.DragSourceListener;

import edu.uci.isr.archstudio4.comp.fileman.IFileManagerListener;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatListener;

public interface IArchipelagoTreePlugin{
	public IArchipelagoTreeContentProvider getContentProvider();
	public IArchipelagoTreeDoubleClickHandler getDoubleClickHandler();
	public IArchipelagoLabelProvider getLabelProvider();
	public IArchipelagoTreeContextMenuFiller[] getContextMenuFillers();
	public ICellModifier[] getCellModifiers();
	public XArchFlatListener getXArchFlatListener();
	public XArchFileListener getXArchFileListener();
	public IFileManagerListener getFileManagerListener();
	public IArchipelagoEditorFocuser getEditorFocuser();
	
	public DragSourceListener getDragSourceListener();
}
