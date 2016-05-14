package edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT.IChangeReference;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusview.ChangeSetStatusViewEvent.CSStatusViewEventType;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.ChangeStatus;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class ChangeSetStatusViewImpl
    implements IChangeSetStatusView, XArchFileListener, XArchFlatListener{

	XArchFlatInterface xarch;
	IChangeSetSync cssync;
	IChangeSetADT csadt;

	/**
	 * Keeps track of event listeners for all change set status data
	 */
	ChangeSetStatusViewListeners listeners;

	Boolean debug = false;

	/**
	 * This subclass encapsulates the status data stored for each XArchObj
	 * 
	 * @author Kari
	 */
	class XArchStatusData{

		// map an xarch path to its change set status data
		private final Map<String, ChangeSetStatusData> xArchToChangeSetStatusData;

		/**
		 * returns a list of all known xarch paths for which we have status data
		 * 
		 * @return String array of all known xarch path strings
		 */
		public String[] getAllArchPaths(){
			String[] strArray = new String[xArchToChangeSetStatusData.size()];
			int idx = 0;
			for(Map.Entry<String, ChangeSetStatusData> entry: xArchToChangeSetStatusData.entrySet()){
				strArray[idx] = entry.getKey();
				idx++;
			}
			return strArray;
		}

		public ChangeSetStatusData getStatusData(String xArchPath){
			return xArchToChangeSetStatusData.get(xArchPath);
		}

		public void addStatus(String xArchPath, ChangeSetStatusData statusData){
			xArchToChangeSetStatusData.put(xArchPath, statusData);
		}

		/**
		 * Constructor
		 */
		public XArchStatusData(){
			xArchToChangeSetStatusData = new HashMap<String, ChangeSetStatusData>();
		}
	}

	/** Map to keep a separate status data for each root XArchRef */
	private final Map<ObjRef, XArchStatusData> xArchToStatusData = new HashMap<ObjRef, XArchStatusData>();

	/**
	 * Constructor
	 */
	public ChangeSetStatusViewImpl(XArchFlatInterface xarch, IChangeSetSync cssync, IChangeSetADT csadt){
		this.xarch = xarch;
		this.cssync = cssync;
		this.csadt = csadt;
		listeners = new ChangeSetStatusViewListeners();
	}

	public ChangeSetStatusViewListeners getListeners(){
		return listeners;
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt){
		if(debug){
			System.err.println("Got Flat Event: " + evt);
		}
		if(evt.getIsAttached() && !evt.getIsExtra() && evt.getSourceTargetPath().toTagsOnlyString().startsWith("xArch/archChangeSets/changeSet/xArchElement")){

			ObjRef xArchRef = evt.getXArchRef();
			ObjRef archChangeSetsRef = evt.getSourceAncestors()[evt.getSourceAncestors().length - 2];
			ObjRef[] changeSetRefs = xarch.getAll(archChangeSetsRef, "changeSet");
			changeSetRefs = ChangeSetUtils.resolveExternalChangeSetRefs(xarch, changeSetRefs);
			IChangeReference changeReference = null;
			ObjRef[] changeSegmentRefs = null;
			int fakeChangeSegmentIndex = -1;

			switch(evt.getEventType()){
			case XArchFlatEvent.ADD_EVENT:
			case XArchFlatEvent.SET_EVENT:
			case XArchFlatEvent.PROMOTE_EVENT:
				if(evt.getTarget() instanceof ObjRef){
					ObjRef changeSegmentRef = (ObjRef)evt.getTarget();
					changeReference = csadt.getChangeSegmentReference(xArchRef, changeSegmentRef);
				}
				else{
					ObjRef changeSegmentRef = evt.getSource();
					changeReference = csadt.getChangeSegmentReference(xArchRef, changeSegmentRef);
				}
				changeSegmentRefs = csadt.getChangeSegmentRefs(xArchRef, changeSetRefs, changeReference);
				break;

			case XArchFlatEvent.REMOVE_EVENT:
			case XArchFlatEvent.CLEAR_EVENT:
				if(evt.getTarget() instanceof ObjRef){
					ObjRef changeSegmentRef = (ObjRef)evt.getTarget();
					IChangeReference parentChangeReference = csadt.getChangeSegmentReference(xArchRef, evt.getSource());
					changeReference = csadt.getDetachedChildChangeReference(xArchRef, parentChangeReference, changeSegmentRef);
					changeSegmentRefs = csadt.getChangeSegmentRefs(xArchRef, changeSetRefs, changeReference);

					// since the change segment ref was removed, it will not appear in the resulting change segment refs returned from csadt
					// so, we manually stick in the removed change segment ref here, so that we can rescan it (and its children)
					fakeChangeSegmentIndex = indexOf(changeSetRefs, evt.getSourceAncestors()[evt.getSourceAncestors().length - 3]);
					changeSegmentRefs[fakeChangeSegmentIndex] = changeSegmentRef;
				}
				else{
					ObjRef changeSegmentRef = evt.getSource();
					changeReference = csadt.getChangeSegmentReference(xArchRef, changeSegmentRef);
					changeSegmentRefs = csadt.getChangeSegmentRefs(xArchRef, changeSetRefs, changeReference);
				}
				break;
			}

			if(changeReference != null){
				scan(xArchRef, changeSetRefs, changeReference, changeSegmentRefs, fakeChangeSegmentIndex);
			}
		}
	}

	private int indexOf(ObjRef[] changeSetRefs, ObjRef changeSetRef){
		for(int i = 0; i < changeSetRefs.length; i++){
			if(changeSetRefs[i].equals(changeSetRef)){
				return i;
			}
		}
		return -1;
	}

	public void handleXArchFileEvent(XArchFileEvent evt){
		if(debug){
			System.out.println("Got File Event: " + evt);
		}
		if(evt.getEventType() == XArchFileEvent.XARCH_OPENED_EVENT){
			ObjRef xArchRef = evt.getXArchRef();

			// get the changesets context
			ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");

			// get the archchangesets element for the file
			// there should only be one of these per file
			ObjRef archChangeSetsRef = xarch.getElement(changesetsContextRef, "archChangeSets", xArchRef);

			if(archChangeSetsRef != null){
				// get all change sets
				ObjRef[] changeSetRefs = xarch.getAll(archChangeSetsRef, "changeSet");
				changeSetRefs = ChangeSetUtils.resolveExternalChangeSetRefs(xarch, changeSetRefs);
				IChangeReference rootReference = csadt.getElementReference(xArchRef, new ObjRef[]{xArchRef}, true);
				ObjRef[] rootChangeSegmentRefs = csadt.getChangeSegmentRefs(xArchRef, changeSetRefs, rootReference);
				scan(xArchRef, changeSetRefs, rootReference, rootChangeSegmentRefs, -1);
			}

		}
		else if(evt.getEventType() == XArchFileEvent.XARCH_CLOSED_EVENT){
			ObjRef xArchRef = evt.getXArchRef();
			xArchToStatusData.remove(xArchRef);
		}
	}

	private void scan(ObjRef xArchRef, ObjRef[] changeSetRefs, IChangeReference parentReference, ObjRef[] parentChangeSegmentRefs, int fakeChangeSegmentIndex){

		if(debug){
			System.err.println("Scanning: " + parentReference);
		}
		String changeSetPath = csadt.getChangeReferencePath(parentReference);

		ChangeSetStatusData statusData;
		if(fakeChangeSegmentIndex == -1){
			statusData = getStatusData(xArchRef, changeSetRefs, parentReference, parentChangeSegmentRefs);
		}
		else{
			ObjRef[] parentChangeSegmentRefsWithNull = new ObjRef[parentChangeSegmentRefs.length];
			System.arraycopy(parentChangeSegmentRefs, 0, parentChangeSegmentRefsWithNull, 0, parentChangeSegmentRefs.length);
			parentChangeSegmentRefsWithNull[fakeChangeSegmentIndex] = null;
			statusData = getStatusData(xArchRef, changeSetRefs, parentReference, parentChangeSegmentRefsWithNull);
		}
		// "for this reference, these change sets add, remove, and modify it"

		// keep track of status data per xarch file
		XArchStatusData xArchStatusData = xArchToStatusData.get(xArchRef);
		if(xArchStatusData == null){
			xArchStatusData = new XArchStatusData();
			xArchToStatusData.put(xArchRef, xArchStatusData);
		}

		ChangeSetStatusData oldStatusData = xArchStatusData.getStatusData(changeSetPath);
		// if this is new status data or if existing status data has changed...
		if(oldStatusData == null || !statusData.equals(oldStatusData)){
			// generate an event with this status Data
			ChangeSetStatusViewEvent event = new ChangeSetStatusViewEvent(CSStatusViewEventType.STATUS_UPDATED, xArchRef, changeSetPath, statusData);
			listeners.sendEvent(event);
			// add new or updated status data to the cache
			xArchStatusData.addStatus(changeSetPath, statusData);
		}

		Map<IChangeReference, ObjRef[]> childReferenceToChildChangeSegmentsMap;
		childReferenceToChildChangeSegmentsMap = csadt.getChildChangeSegmentRefs(xArchRef, parentReference, parentChangeSegmentRefs);
		for(Map.Entry<IChangeReference, ObjRef[]> entry: childReferenceToChildChangeSegmentsMap.entrySet()){
			IChangeReference childChangeReference = entry.getKey();
			ObjRef[] childChangeSegmentRefs = entry.getValue();
			scan(xArchRef, changeSetRefs, childChangeReference, childChangeSegmentRefs, fakeChangeSegmentIndex);
		}
	}

	public ChangeSetStatusData getChangeSetStatusData(ObjRef xArchRef, ObjRef[] changeSetRefs, IChangeReference reference){
		ObjRef[] changeSegmentRefs = csadt.getChangeSegmentRefs(xArchRef, changeSetRefs, reference);
		return getStatusData(xArchRef, changeSetRefs, reference, changeSegmentRefs);
	}

	private ChangeSetStatusData getStatusData(ObjRef xArchRef, ObjRef[] changeSetRefs, IChangeReference reference, ObjRef[] changeSegmentRefs){

		List<ObjRef> changeSetsThatCreate = new ArrayList<ObjRef>();
		List<ObjRef> changeSetsThatModify = new ArrayList<ObjRef>();
		List<ObjRef> changeSetsThatRemove = new ArrayList<ObjRef>();

		ChangeStatus[] changeStatuses = cssync.getChangeStatus(xArchRef, changeSetRefs, reference, changeSegmentRefs, false);

		for(int i = 0; i < changeSegmentRefs.length; i++){
			ObjRef changeSetRef = changeSetRefs[i];
			ChangeStatus changeStatus = changeStatuses[i];
			switch(changeStatus){
			case ADDED:
				changeSetsThatCreate.add(changeSetRef);
				break;
			case MODIFIED:
				changeSetsThatModify.add(changeSetRef);
				break;
			case REMOVED:
				changeSetsThatRemove.add(changeSetRef);
				break;
			case UNMODIFIED:
				break;
			case DETACHED:
				throw new RuntimeException(); // this shouldn't happen
			}
		}

		return new ChangeSetStatusData(reference, changeSetsThatCreate, changeSetsThatModify, changeSetsThatRemove);
	}
}