package edu.uci.isr.archstudio4.comp.archipelago;

import org.eclipse.jface.action.IMenuManager;

public interface IArchipelagoTreeContextMenuFiller{
	public void fillContextMenu(IMenuManager m, Object[] selectedNodes);
}
