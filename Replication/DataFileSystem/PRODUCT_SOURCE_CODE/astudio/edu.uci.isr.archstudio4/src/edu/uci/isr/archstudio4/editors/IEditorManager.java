package edu.uci.isr.archstudio4.editors;

import org.eclipse.swt.graphics.Image;

import edu.uci.isr.xarchflat.ObjRef;

public interface IEditorManager{

	public void registerEditor(String name, Image icon);
	public void unregisterEditor(String name);

	public boolean isEditorRegistered(String name);
	public Image getIcon(String name);
	public String[] getEditors();
	public String getDefaultEditor();
	
	public void focusEditor(String name, ObjRef[] refs);
}
