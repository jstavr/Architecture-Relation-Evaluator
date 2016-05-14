package edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusrelationships;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager.IChangeSetRelationshipManager;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusview.ChangeSetStatusViewEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusview.IChangeSetStatusView;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusview.IChangeSetStatusViewListener;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusview.IChangeSetStatusView.ChangeSetStatusData;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetutils.ChangeSetPathReference;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetutils.OrRelationshipBuilder;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ChangeSetStatusRelationshipsImpl
	implements IChangeSetStatusRelationships, IChangeSetStatusViewListener{


	XArchFlatInterface xarch;
	IChangeSetADT changeSetADT;
	IChangeSetStatusView csStatusView;
	IChangeSetRelationshipManager csRelMgr;
	String[] ignorePaths;  
		// paths that match these prefixes should be omitted from any generated relationships

	Boolean debug = false;
	
	// maps an xArchPath string to a change set relationship for that path
	Map<String, ObjRef> pathToChangeSetOrRelationhip;

	/**
	 * Constructor
	 */
	public ChangeSetStatusRelationshipsImpl(XArchFlatInterface xarch, IChangeSetADT changeSetADT, IChangeSetStatusView csStatusView, IChangeSetRelationshipManager csRelMgr){
		this.xarch = xarch;
		this.changeSetADT = changeSetADT;
		this.csStatusView = csStatusView;
		this.csRelMgr = csRelMgr;
		pathToChangeSetOrRelationhip = new HashMap<String, ObjRef>();
		
		
		List<String> ignorePaths = new ArrayList<String>();
		ignorePaths.add("xArch/Name:Object/ElementType:hints3#RenderingHints");
		this.ignorePaths = ignorePaths.toArray(new String[ignorePaths.size()]);
	}

	public ObjRef[] getAllRelationships(ObjRef xArchRef){
		return pathToChangeSetOrRelationhip.values().toArray(new ObjRef[0]);
	}
	
	public void updateCSRelationship(ObjRef xArchRef, String xArchPath, ChangeSetStatusData statusData){
		if ((statusData.creators!= null) && (statusData.modifiers != null)) {
			ObjRef relationship;
			if ((statusData.creators.size() == 1) && (statusData.modifiers.size() > 0)) {

				relationship = buildStatusRelationship (xArchRef, xArchPath, statusData.creators.get(0), statusData.modifiers);

				// notify the relationship manager of this current relationship
				csRelMgr.addRelationship(xArchRef, relationship);

				// keep track of all relationships indexed by xArch path
				pathToChangeSetOrRelationhip.put(xArchPath, relationship);
			} else {
				// check to see if there is a relationship cached for this path,
				// if so, remove it and notify relationship manager
				relationship = pathToChangeSetOrRelationhip.remove(xArchPath);
				if (relationship != null) {
					csRelMgr.removeRelationship(xArchRef, relationship);
				}
			}
		}
	}

	private ObjRef buildStatusRelationship(ObjRef xArchRef, String xArchPath, ObjRef creator, List<ObjRef> modifiers) {
		ChangeSetPathReference creatingCSPathReference = new ChangeSetPathReference(creator, xArchPath);
		
		List<ChangeSetPathReference> modifyingCSPathReferences = new LinkedList<ChangeSetPathReference>();
		ChangeSetPathReference modifyingCSPathReference;
		
		// make a list of ChangeSetPathReferences for each change set that modifies 
		for(ObjRef modifier: modifiers){
			if (!ignorePathReference (xArchPath)) {
				modifyingCSPathReference = new ChangeSetPathReference(modifier, xArchPath);
				modifyingCSPathReferences.add(modifyingCSPathReference);
			}
		}
		return OrRelationshipBuilder.buildOrRelationship(xarch, xArchRef, modifyingCSPathReferences, creatingCSPathReference);
	}
	
	private boolean ignorePathReference (String csPath){
		for(String ignorePath: ignorePaths){
			// check if path prefix matches any ignore paths
			if (csPath.indexOf(ignorePath) == 0) {
				return true;
			}
		}
		return false;
	}

	public void handleCSStatusViewEvent(ChangeSetStatusViewEvent evt){

		if(debug){
			System.out.print("Got ChangeSet Status View Event: ");
		}
		
		ObjRef xArchRef = evt.getXArchRef();
		String xArchPath =evt.getXArchPath();
		ChangeSetStatusData statusData = evt.getStatusData();
		
		
		if(evt.getEventType().equals(ChangeSetStatusViewEvent.CSStatusViewEventType.STATUS_UPDATED)){
			updateCSRelationship(xArchRef, xArchPath, statusData);
		} 
	}
		
}