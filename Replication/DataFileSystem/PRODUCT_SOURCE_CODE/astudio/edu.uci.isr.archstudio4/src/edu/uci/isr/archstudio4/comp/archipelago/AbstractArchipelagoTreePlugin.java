package edu.uci.isr.archstudio4.comp.archipelago;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.dnd.DragSourceListener;

import edu.uci.isr.archstudio4.comp.fileman.IFileManagerListener;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class AbstractArchipelagoTreePlugin implements IArchipelagoTreePlugin{

	protected IArchipelagoTreeContentProvider contentProvider = null;
	protected IArchipelagoTreeDoubleClickHandler doubleClickHandler = null;
	protected IArchipelagoLabelProvider labelProvider = null;
	protected IArchipelagoTreeContextMenuFiller[] contextMenuFillers = null;
	protected ICellModifier[] cellModifiers = null;
	protected XArchFlatListener flatListener = null;
	protected XArchFileListener fileListener = null;
	protected IFileManagerListener fileManagerListener = null;
	protected DragSourceListener dragSourceListener = null;
	protected IArchipelagoEditorFocuser editorFocuser = null;
	
	public IArchipelagoTreeContentProvider getContentProvider(){
		return contentProvider;
	}
	
	public IArchipelagoTreeDoubleClickHandler getDoubleClickHandler(){
		return doubleClickHandler;
	}
	
	public IArchipelagoLabelProvider getLabelProvider(){
		return labelProvider;
	}
	
	public IArchipelagoTreeContextMenuFiller[] getContextMenuFillers(){
		return contextMenuFillers;
	}
	
	public ICellModifier[] getCellModifiers(){
		return cellModifiers;
	}
	
	public XArchFileListener getXArchFileListener(){
		return fileListener;
	}
	
	public XArchFlatListener getXArchFlatListener(){
		return flatListener;
	}
	
	public DragSourceListener getDragSourceListener(){
		return dragSourceListener;
	}
	
	public IFileManagerListener getFileManagerListener(){
		return fileManagerListener;
	}
	
	public IArchipelagoEditorFocuser getEditorFocuser(){
		return editorFocuser;
	}
}
