package edu.uci.isr.archstudio4.comp.fileman;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import edu.uci.isr.xarchflat.ObjRef;

public interface IFileManager{
	
	public boolean isOpen(IFile f);
	public ObjRef getXArch(IFile f);
	
	public ObjRef openXArch(String toolID, IFile f) throws CantOpenFileException;
	public ObjRef openXArch(String toolID, File f) throws CantOpenFileException;
	public void closeXArch(String toolID, ObjRef xArchRef);

	public void save(ObjRef xArchRef, IProgressMonitor monitor);
	public void saveAs(ObjRef xArchRef, IFile f);
	
	public void makeDirty(ObjRef xArchRef);
	public void makeClean(ObjRef xArchRef);
	public boolean isDirty(ObjRef xArchRef);
}
