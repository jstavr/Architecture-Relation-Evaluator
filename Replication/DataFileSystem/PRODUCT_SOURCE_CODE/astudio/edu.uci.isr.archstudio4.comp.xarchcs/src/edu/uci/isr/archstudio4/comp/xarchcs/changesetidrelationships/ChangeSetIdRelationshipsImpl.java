package edu.uci.isr.archstudio4.comp.xarchcs.changesetidrelationships;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetidview.ChangeSetIdViewEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetidview.IChangeSetIdView;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetidview.IChangeSetIdViewListener;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager.IChangeSetRelationshipManager;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetutils.ChangeSetPathReference;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetutils.OrRelationshipBuilder;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ChangeSetIdRelationshipsImpl
	implements IChangeSetIdRelationships, IChangeSetIdViewListener{

	class ChangeSetIdRelationshipsData{

		// maps an id string to a change set relationship for that id
		Map<String, ObjRef> idToChangeSetOrRelationhip;

		public ChangeSetIdRelationshipsData(){
			idToChangeSetOrRelationhip = new HashMap<String, ObjRef>();
		}

		// returns a list of all relationships across all ids
		public ObjRef[] getRelationships(){
			return idToChangeSetOrRelationhip.values().toArray(new ObjRef[0]);
		}
	}

	/** Map to keep separate id relationships data for each root XArchRef */
	private Map<ObjRef, ChangeSetIdRelationshipsData> xArchToCSIdRelData;
	
	XArchFlatInterface xarch;
	IChangeSetADT changeSetADT;
	IChangeSetIdView csIdView;
	IChangeSetRelationshipManager csRelMgr;
	String[] ignorePaths;


	Boolean debug = false;

	/**
	 * Constructor
	 */
	public ChangeSetIdRelationshipsImpl(XArchFlatInterface xarch, IChangeSetADT changeSetADT, IChangeSetIdView csIdView, IChangeSetRelationshipManager csRelMgr){
		this.xarch = xarch;
		this.changeSetADT = changeSetADT;
		this.csIdView = csIdView;
		this.csRelMgr = csRelMgr;
		xArchToCSIdRelData = new HashMap<ObjRef, ChangeSetIdRelationshipsData>();
		
		List<String> ignorePaths = new ArrayList<String>();
		ignorePaths.add("xArch/Name:Object/ElementType:hints3#RenderingHints");
		this.ignorePaths = ignorePaths.toArray(new String[ignorePaths.size()]);
	}

	public ObjRef[] getAllRelationships(ObjRef xArchRef){
		ChangeSetIdRelationshipsData relData = xArchToCSIdRelData.get(xArchRef);
		if(relData == null){
			return new ObjRef[0];
		}

		return relData.getRelationships();
	}

	private static ObjRef getChangeSetRefFromSegment(XArchFlatInterface xarch, ObjRef csSegmentRef){
		ObjRef[] Ancestors = xarch.getAllAncestors(csSegmentRef);
        if (Ancestors.length <3) return null;
		// the change set ancestor is 3rd from the root
		// the root is the last element returned by getAllAncestors, therefore we need
		// the 3rd to last element.
		ObjRef csRef = Ancestors[Ancestors.length - 3];
		return csRef;
	}

	/**
	 * Given a change set segment, returns a new ChangeSetPathReference which includes the change set root object, 
	 * and the xArchPath corresponding to the segment.
	 */
	public ChangeSetPathReference getCSPathReferenceFromSegment (XArchFlatInterface xarch, IChangeSetADT changeSetADT, ObjRef xArchRef, ObjRef csSegmentRef) {
		if (csSegmentRef == null) return null;
		ObjRef changeSetRef = getChangeSetRefFromSegment(xarch, csSegmentRef);
		if (changeSetRef == null) return null;  // this can happen if the segment has been removed before but not yet detected
		String xArchPath = changeSetADT.getChangeReferencePath(changeSetADT.getChangeSegmentReference(xArchRef, csSegmentRef));
		return new ChangeSetPathReference(changeSetRef, xArchPath);	
	}

	public void updateCSRelationship(ObjRef xArchRef, String id, ChangeSetIdRelationshipsData relData){
		
		// get any old relationship for this id, removing it from the relationship table
		ObjRef oldRelationship = relData.idToChangeSetOrRelationhip.remove(id);
		if(oldRelationship != null){
			// if a relationship with this id was found, notify the relationship manager that it has now been removed
			csRelMgr.removeRelationship(xArchRef, oldRelationship);
		}
		
		ObjRef creatingChangeSetSegment = csIdView.whoCreated(xArchRef, id);
		
		if (creatingChangeSetSegment != null) {
			ChangeSetPathReference creatingCSPathReference = getCSPathReferenceFromSegment(xarch, changeSetADT, xArchRef, creatingChangeSetSegment);

			// set of all change set refs that contain a segment that reference the given id
			List<ChangeSetPathReference> referringCSPathReferences = new LinkedList<ChangeSetPathReference>();

			ObjRef[] references = csIdView.whoReferences(xArchRef, id);
			if (references != null) {
				ChangeSetPathReference referringCSPathReference;
				// for all change set segments that reference id
				for(ObjRef referringChangeSetSegment: references){
					referringCSPathReference = getCSPathReferenceFromSegment(xarch, changeSetADT, xArchRef, referringChangeSetSegment);
					if (referringCSPathReference != null) {
						// avoid a-> a
						// also, ignore any paths that are on the ignore paths list...
						if (! creatingCSPathReference.getChangeSetRef().equals(referringCSPathReference.getChangeSetRef()) && 
								! ignorePathReference (referringCSPathReference.getXArchPath())) {
							referringCSPathReferences.add(referringCSPathReference);
						}
					}
				}
			}



			if(creatingCSPathReference != null && referringCSPathReferences.size() > 0){
				// create an or-relationship for these references
				ObjRef orRelationship = OrRelationshipBuilder.buildOrRelationship(xarch, xArchRef, referringCSPathReferences, creatingCSPathReference);

				// notify the relationship manager of this current relationship
				csRelMgr.addRelationship(xArchRef, orRelationship);

				// keep track of all relationships
				relData.idToChangeSetOrRelationhip.put(id, orRelationship);
			}
		}

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

	public void handleCSIdViewEvent(ChangeSetIdViewEvent evt){
		String id = evt.getId();
		ObjRef xArchRef = evt.getXArchRef();

		if(debug){
			System.out.print("Got ChangeSet Id View Event: ");
		}

		ChangeSetIdRelationshipsData relData = xArchToCSIdRelData.get(xArchRef);
		// if no relationship data exists for this xarch, then create it.
		if(relData == null){
			relData = new ChangeSetIdRelationshipsData();
			xArchToCSIdRelData.put(xArchRef, relData);
		}

		switch (evt.getEventType()) {
		case CREATOR_ADDED: 
			if(debug){
				System.out.println("CREATOR_ADDED, id = " + id);
			} ; break ;
		case CREATOR_REMOVED: 
			if(debug){
				System.out.println("CREATOR_REMOVED, id = " + id);
			} ; break ;
		case REFERENCE_ADDED: 
			if(debug){
				System.out.println("REFERENCE_ADDED, id = " + id);
			} ; break ;
		case REFERENCE_REMOVED: 
			if(debug){
				System.out.println("REFERENCE_REMOVED, id = " + id);
			} ; break ;
		case INITIALIZED: 
			if(debug){
				System.out.println("*********Got an INITIALIZED event.");
			} 
			// newly initialzied xarch, the relationships for all Ids in the xarch
			updateAllIdRelationships(xArchRef, relData);
			break ;
		}

		// if xarch is currently being initialized, wait for the INITIALIZED event
		// generated after initialization is complete.
		if ((id != null) && (evt.getIsInitialized() != false)) {
			updateCSRelationship(xArchRef, id, relData);
		}

	}

	private void updateAllIdRelationships(ObjRef xArchRef, ChangeSetIdRelationshipsData relData) {
		String[] ids = csIdView.getAllIds(xArchRef);
		if ((ids != null) && (ids.length > 0)) {
			for(String id: ids){
				updateCSRelationship(xArchRef, id, relData);
			}
		}
	}

}