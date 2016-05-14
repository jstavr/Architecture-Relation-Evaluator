package edu.uci.isr.archstudio4.comp.xarchcs;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.IChangeSetSyncMonitor;

public class ProgressToChangeSetMonitorWrapper
    implements IChangeSetSyncMonitor{

	IProgressMonitor m;

	public ProgressToChangeSetMonitorWrapper(IProgressMonitor m){
		this.m = m;
	}

	public void beginTask(int totalWork){
		m.beginTask("", totalWork);
	}

	public void done(){
		m.done();
	}

	public boolean isCanceled(){
		return m.isCanceled();
	}

	public void setCanceled(boolean value){
		m.setCanceled(value);
	}

	public void worked(int work){
		m.worked(work);
	}
}