package edu.uci.isr.archstudio4.comp.archlight;

import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import edu.uci.isr.archstudio4.archlight.IArchlightTool;
import edu.uci.isr.myx.conn.IMultiwayProgressListener;
import edu.uci.isr.myx.conn.IMultiwayResults;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class ArchlightToolAggregatorMyxComponent extends AbstractMyxSimpleBrick 
implements IArchlightTool, IMultiwayProgressListener, IMyxDynamicBrick{

	public static final String TOOL_ID = "Aggregator";
	
	public static final IMyxName INTERFACE_NAME_OUT_RESULTS = MyxUtils.createName("results");
	public static final IMyxName INTERFACE_NAME_OUT_TOOLS_MULTIWAY = MyxUtils.createName("archlighttools");
	public static final IMyxName INTERFACE_NAME_IN_TOOL = MyxUtils.createName("archlighttool");
	public static final IMyxName INTERFACE_NAME_IN_PROGRESS = MyxUtils.createName("progress");

	protected IArchlightTool tools = null;
	protected IMultiwayResults results = null;
	
	public ArchlightToolAggregatorMyxComponent(){
	}
	
	public void init(){
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_RESULTS)){
			results = (IMultiwayResults)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_TOOLS_MULTIWAY)){
			tools = (IArchlightTool)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_RESULTS)){
			results = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_TOOLS_MULTIWAY)){
			tools = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_TOOL)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_PROGRESS)){
			return this;
		}
		return null;
	}
	
	public String getToolID(){
		return TOOL_ID;
	}
	
	public void reloadTests(){
		if(tools != null) tools.reloadTests();
	}
	
	boolean runningTests = false;
	protected IProgressMonitor progressMonitor = null;
	
	public void runTests(ObjRef documentRef, String[] testUIDs){
		if(runningTests) return;
		
		runningTests = true;
		final ObjRef fdocumentRef = documentRef;
		final String[] ftestUIDs = testUIDs;
		if(tools != null){
			Job job = new Job("Running Archlight Tests"){
				protected IStatus run(IProgressMonitor monitor){
					try{
						progressMonitor = monitor;
						tools.runTests(fdocumentRef, ftestUIDs);
						return Status.OK_STATUS;
					}
					finally{
						runningTests = false;
					}
				}
			};
			job.setPriority(Job.LONG);
			job.setUser(true);
			job.schedule();
		}
	}
	
	public void callProgress(int calleeNum, int totalCallees, Object returnValue, Throwable exception){
		IProgressMonitor lprogressMonitor = progressMonitor;
		if(lprogressMonitor != null){
			if(calleeNum == 0){
				lprogressMonitor.beginTask("Running Archlight Tests", totalCallees);
			}
			else{
				lprogressMonitor.worked(calleeNum);
			}
			if(calleeNum == totalCallees){
				lprogressMonitor.done();
			}
		}
	}
	
}
