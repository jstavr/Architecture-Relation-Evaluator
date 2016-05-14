package edu.uci.isr.archstudio4.comp.fileman;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.uci.isr.xarchflat.ObjRef;

public interface IFileManagerListener{
	public void fileDirtyStateChanged(ObjRef xArchRef, boolean dirty);
	public void fileSaving(ObjRef xArchRef, IProgressMonitor monitor);
}
