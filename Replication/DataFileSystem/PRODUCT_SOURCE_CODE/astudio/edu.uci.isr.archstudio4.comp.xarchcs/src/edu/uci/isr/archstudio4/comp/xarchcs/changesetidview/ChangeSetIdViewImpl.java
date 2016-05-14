package edu.uci.isr.archstudio4.comp.xarchcs.changesetidview;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.ChangeSetADTImpl;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.NoSuchObjectException;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class ChangeSetIdViewImpl
    implements IChangeSetIdView, XArchFileListener, XArchFlatListener{

	/**
	 * This subclass encapsulates the id data stored for each XArchObj
	 * 
	 * @author Kari
	 */
	class ChangeSetIdData{

		Boolean initialized;

		/** Keeps track of all ids and their creators */
		public IdCreators creators;

		/** Keeps track of all ids and their references */
		public IdReferences references;

		/**
		 * returns a list of all known ids
		 * 
		 * @return String array of all known id strings
		 */
		public String[] getAllIds(){
			return creators.getAllIds();
		}

		/**
		 * Constructor
		 */
		public ChangeSetIdData(ChangeSetIdViewListeners listeners){
			creators = new IdCreators(listeners);
			references = new IdReferences(listeners);
			initialized = false;
		}
	}

	/** Map to keep a separate id data for each root XArchRef */
	private final Map<ObjRef, ChangeSetIdData> xArchToChangeSetIdData = Collections.synchronizedMap(new HashMap<ObjRef, ChangeSetIdData>());

	XArchFlatInterface xarch;

	/**
	 * Keeps track of event listeners for all change set id data
	 */
	ChangeSetIdViewListeners listeners;

	Boolean debug = false;

	/**
	 * Constructor
	 */
	public ChangeSetIdViewImpl(XArchFlatInterface xarch){
		this.xarch = xarch;
		listeners = new ChangeSetIdViewListeners();

		// start garbage collection thread
		Thread thGarbageCollection = new Thread(rGarbageCollection, "GarbageCollection");
		thGarbageCollection.setDaemon(true);
		thGarbageCollection.setPriority(Thread.MIN_PRIORITY);
		thGarbageCollection.start();
	}

	public String[] getAllIds(ObjRef XArchRef){
		// TODO Auto-generated method stub
		ChangeSetIdData csIdData = xArchToChangeSetIdData.get(XArchRef);
		if(csIdData == null || csIdData.initialized == false){
			return null;
		}
		return csIdData.getAllIds();
	}

	public ObjRef whoCreated(ObjRef XArchRef, String id){
		ChangeSetIdData csIdData = xArchToChangeSetIdData.get(XArchRef);
		if(csIdData == null || csIdData.initialized == false){
			return null;
		}
		return csIdData.creators.get(id);
	}

	public ObjRef[] whoReferences(ObjRef XArchRef, String id){
		// TODO Auto-generated method stub
		ChangeSetIdData csIdData = xArchToChangeSetIdData.get(XArchRef);
		if(csIdData == null || csIdData.initialized == false){
			return new ObjRef[0];
		}
		Collection<ObjRef> refs = csIdData.references.get(id);
		if(refs != null){
			return refs.toArray(new ObjRef[refs.size()]);
		}
		else{
			return null;
		}
	}

	public ChangeSetIdViewListeners getListeners(){
		return listeners;
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt){

		if(debug){
			System.out.println("Got Flat Event: " + evt);
		}

		ObjRef xArchRef = evt.getXArchRef();
		ObjRef objRef;
		ChangeSetIdData csIdData = xArchToChangeSetIdData.get(xArchRef);

		if(evt.getIsAttached()){
			if(evt.getEventType() == XArchFlatEvent.ADD_EVENT){
				objRef = (ObjRef)evt.getTarget();
			}
			else{
				objRef = evt.getSource();
			}
			if(evt.getEventType() == XArchFlatEvent.ADD_EVENT || evt.getEventType() == XArchFlatEvent.SET_EVENT){
				// handle if attribute segment with id attribute was added to a changeset
				try{
					if(xarch.isInstanceOf(objRef, "changesets#AttributeSegment")){
						parseAttributeSegment(objRef, csIdData, xArchRef);
					}
					else if(xarch.isInstanceOf(objRef, "changesets#ElementSegment")){
						parseElementSegment(objRef, csIdData, xArchRef);
					}
					else if(xarch.isInstanceOf(objRef, "changesets#ElementManySegment")){
						parseElementManySegment(objRef, csIdData, xArchRef);
					}
				}catch(NoSuchObjectException e){}
			}
		}
	}

	public void handleXArchFileEvent(XArchFileEvent evt){
		if(debug){
			System.out.println("Got File Event: " + evt);
		}

		ObjRef xArchRef = evt.getXArchRef();

		switch(evt.getEventType()){

		case XArchFileEvent.XARCH_CREATED_EVENT:
		case XArchFileEvent.XARCH_OPENED_EVENT:
			synchronized(xArchToChangeSetIdData){
				ChangeSetIdData csIdData = xArchToChangeSetIdData.get(xArchRef);
				if(csIdData == null){
					csIdData = new ChangeSetIdData(listeners);
					xArchToChangeSetIdData.put(xArchRef, csIdData);
				}
				recordIds(getChangeSetsRoot(xArchRef), csIdData, xArchRef);
				csIdData.initialized = true;

				// generate initialized event
				ChangeSetIdViewEvent initEvt = new ChangeSetIdViewEvent(xArchRef);
				listeners.sendEvent(initEvt);
			}
			break;

		case XArchFileEvent.XARCH_CLOSED_EVENT:
			synchronized(xArchToChangeSetIdData){
				xArchToChangeSetIdData.remove(xArchRef);
			}
			break;

		}
	}

	private ObjRef getChangeSetsRoot(ObjRef xArchRef){
		// get the changesets context
		ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");

		// get the archChangeSets element for the file
		// there should only be one of these per file
		return xarch.getElement(changesetsContextRef, "archChangeSets", xArchRef);
	}

	private void recordIds(ObjRef archChangeSetsRef, ChangeSetIdData csIdData, ObjRef xArchRef){
		if(archChangeSetsRef != null){
			// get the list of change sets for the file
			ObjRef[] changeSetRefs = xarch.getAll(archChangeSetsRef, "changeSet");
			changeSetRefs = ChangeSetUtils.resolveExternalChangeSetRefs(xarch, changeSetRefs);

			// process each change set
			for(ObjRef changeSetRef: changeSetRefs){
				if(debug){
					System.out.println("Walking change set: " + XadlUtils.getDescription(xarch, changeSetRef));
				}

				// get the root change segment of the change set, it always uses this name
				ObjRef changeSegmentRef = (ObjRef)xarch.get(changeSetRef, "xArchElement");

				if(changeSegmentRef != null){
					// and it's always an ElementSegment type, so we can treat it as such
					parseElementSegment(changeSegmentRef, csIdData, xArchRef);
				}
			}
		}
	}

	private void parseElementSegment(ObjRef elementSegmentRef, ChangeSetIdData csIdData, ObjRef xArchRef){
		if(debug){
			System.out.println("Found Element Segment at: " + elementSegmentRef);
		}

		// now, let's recursively iterate through each child segment, which may be any type: attribute, element, or elementMany
		ObjRef[] childSegmentRefs = xarch.getAll(elementSegmentRef, "changeSegment");
		for(ObjRef childSegmentRef: childSegmentRefs){
			if(xarch.isInstanceOf(childSegmentRef, "changesets#AttributeSegment")){
				// this segment describes an attribute child segment
				parseAttributeSegment(childSegmentRef, csIdData, xArchRef);
			}
			else if(xarch.isInstanceOf(childSegmentRef, "changesets#ElementManySegment")){
				// this segment describes an element many segment -- i.e., only a property name that will contain many different children
				parseElementManySegment(childSegmentRef, csIdData, xArchRef);
			}
			else if(xarch.isInstanceOf(childSegmentRef, "changesets#ElementSegment")){
				// this describes an element segment -- call myself recursively
				parseElementSegment(childSegmentRef, csIdData, xArchRef);
			}
			else{
				throw new IllegalArgumentException("This shouldn't happen.");
			}
		}
	}

	private void parseElementManySegment(ObjRef elementManySegmentRef, ChangeSetIdData csIdData, ObjRef xArchRef){
		if(debug){
			System.out.println("Found an element many segment at " + elementManySegmentRef + " for elements named " + ChangeSetADTImpl.decodeNameReference((String)xarch.get(elementManySegmentRef, "reference")));
		}

		// now parse all the element segments that are stored under this name
		// we know that they are all element segments, because we store elements under this name
		for(ObjRef childChangeSegment: xarch.getAll(elementManySegmentRef, "changeSegment")){
			parseElementSegment(childChangeSegment, csIdData, xArchRef);
		}
	}

	private void parseAttributeSegment(ObjRef attributeSegmentRef, ChangeSetIdData csIdData, ObjRef xArchRef){
		if(csIdData == null)
			return;
		if(debug){
			System.out.println("Found an attribute segment at " + attributeSegmentRef + " for an attribute named " + ChangeSetADTImpl.decodeNameReference((String)xarch.get(attributeSegmentRef, "reference")));
		}

		//if the attribute is named "Id" then add it to the change set id data
		if(ChangeSetADTImpl.decodeNameReference((String)xarch.get(attributeSegmentRef, "reference")).equals("Id")){
			String idName = (String)xarch.get(attributeSegmentRef, "value");
			if(idName != null){
				// keep track of who which change segment created this id
				if(debug){
					System.out.println("****Adding id " + idName + " to the csIdData.");
				}
				synchronized(xArchToChangeSetIdData){
					csIdData.creators.add(idName, attributeSegmentRef, xArchRef, csIdData.initialized);
				}
			}

			//if the attribute is named "Href" then add it to the change set id data
		}
		else if(ChangeSetADTImpl.decodeNameReference((String)xarch.get(attributeSegmentRef, "reference")).equals("Href")){
			String value = (String)xarch.get(attributeSegmentRef, "value");
			if(value != null){
				// take off leading # from href value
				String idName = value.substring(1);
				if(debug){
					System.out.println("****Adding href to id " + idName + " to the csIdData.");
				}
				synchronized(xArchToChangeSetIdData){
					csIdData.references.add(idName, attributeSegmentRef, xArchRef, csIdData.initialized);
				}
			}
		}
	}

	public String getId(ObjRef nodeRef){
		return (String)xarch.get(nodeRef, "id");
	}

	Runnable rGarbageCollection = new Runnable(){

		public void run(){
			while(true){
				try{
					Thread.sleep(30000);
				}
				catch(InterruptedException e){
					e.printStackTrace();
				}
				doGarbageCollection();
			}
		}
	};

	private void doGarbageCollection(){
		ObjRef[] xArchRefs;
		synchronized(xArchToChangeSetIdData){
			xArchRefs = xArchToChangeSetIdData.keySet().toArray(new ObjRef[0]);
		}

		// loop through each xarch to cs id data entry
		for(ObjRef xArchRef: xArchRefs){
			ChangeSetIdData csIdData = null;

			// loop through each id to creator entry		
			String[] ids = null;
			synchronized(xArchToChangeSetIdData){
				csIdData = xArchToChangeSetIdData.get(xArchRef);
				if(csIdData != null){
					ids = csIdData.creators.getAllIds();
				}
			}
			if(ids != null){
				for(String id: ids){
					synchronized(xArchToChangeSetIdData){
						ObjRef creator = csIdData.creators.get(id);
						if(!xarch.isAttached(creator)){
							// this node is not attached to the tree, remove it
							csIdData.creators.remove(id, xArchRef);
						}
					}
				}
			}

			// loop through each id to references entry			
			synchronized(xArchToChangeSetIdData){
				csIdData = xArchToChangeSetIdData.get(xArchRef);
				if(csIdData != null){
					ids = csIdData.references.getAllIds();
				}
			}
			if(ids != null){
				for(String id: ids){
					ObjRef[] refs = null;
					synchronized(xArchToChangeSetIdData){
						refs = csIdData.references.get(id).toArray(new ObjRef[0]);
					}
					if(refs != null){
						for(ObjRef ref: refs){
							if(!xarch.isValidObjRef(ref) || !xarch.isAttached(ref)){
								synchronized(xArchToChangeSetIdData){
									// this node is not attached to the tree, remove it	
									csIdData.references.remove(id, ref, xArchRef);
								}
							}
						}
					}
				}
			}
		}
	}
}