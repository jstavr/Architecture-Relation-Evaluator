package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.fileman.IFileManagerListener;
import edu.uci.isr.xarchflat.ObjRef;

@Deprecated
public class ActivityDiagramsFileManagerListener
	implements IFileManagerListener{

	public ActivityDiagramsFileManagerListener(ArchipelagoServices services, ObjRef xArchRef){
	}

	public void fileDirtyStateChanged(ObjRef xArchRef, boolean dirty){
	}

	public void fileSaving(final ObjRef xArchRef, final IProgressMonitor monitor){
	}
}
