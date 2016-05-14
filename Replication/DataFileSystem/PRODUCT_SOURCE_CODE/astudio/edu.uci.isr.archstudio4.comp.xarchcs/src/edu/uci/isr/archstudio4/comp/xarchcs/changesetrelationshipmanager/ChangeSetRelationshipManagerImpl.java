package edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.sysutils.DelayedExecuteOnceThread;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ChangeSetRelationshipManagerImpl implements IChangeSetRelationshipManager {

	private final Object lock = new Object();

	private final XArchFlatInterface xarch;
	private final Map<ObjRef, MergeKeyAndRelationshipRegistry> xArchToRelDataTable = new HashMap<ObjRef, MergeKeyAndRelationshipRegistry>();
	private final Collection<IChangeSetRelationshipMergeHandler> mergeHandlers = new ArrayList<IChangeSetRelationshipMergeHandler>();

	private final Collection<ObjRef> xArchRefsNeedingMerge = Collections.synchronizedCollection(new HashSet<ObjRef>());
	private final DelayedExecuteOnceThread mergeThread = new DelayedExecuteOnceThread(1000, new Runnable() {

		public void run() {
			Map<ObjRef, MergeKeyAndRelationshipRegistry> toMerge = new HashMap<ObjRef, MergeKeyAndRelationshipRegistry>();
			synchronized (lock) {
				if (xArchRefsNeedingMerge.isEmpty()) {
					return;
				}
				try {
					for (ObjRef xArchRef : xArchRefsNeedingMerge) {
						MergeKeyAndRelationshipRegistry relData = xArchToRelDataTable.get(xArchRef);
						if (relData != null) {
							toMerge.put(xArchRef, (MergeKeyAndRelationshipRegistry) relData.clone());
						}
					}

					for (Map.Entry<ObjRef, MergeKeyAndRelationshipRegistry> xArchRefEntry : toMerge.entrySet()) {
						ObjRef xArchRef = xArchRefEntry.getKey();
						ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
						ObjRef archChangeSets = xarch.getElement(changesetsContextRef, "archChangeSets", xArchRef);
						if (archChangeSets != null) {

							MergeKeyAndRelationshipRegistry registry = xArchRefEntry.getValue();
							while (true) {
								// find largest set to merge
								MergeKey largestMergeKey = null;
								Collection<ObjRef> largestRelationshipRefs = null;
								int largestRelationshipRefsSize = 1;
								for (Map.Entry<MergeKey, Collection<ObjRef>> regEntry : registry.getEntries()) {
									Collection<ObjRef> entryRelationshipRefs = regEntry.getValue();
									int entrySize = entryRelationshipRefs.size();
									if (entrySize > largestRelationshipRefsSize) {
										largestMergeKey = regEntry.getKey();
										largestRelationshipRefsSize = entrySize;
										largestRelationshipRefs = entryRelationshipRefs;
									}
								}

								if (largestMergeKey == null) {
									break;
								}

								mergeAndUpdateRegistry(xArchRef, registry, largestMergeKey, largestRelationshipRefs);
							}

							for (ObjRef relationshipRefU : xarch.getAll(archChangeSets, "relationship")) {
								ObjRef relationshipRef = ChangeSetUtils.resolveExternalRelationshipRef(xarch, relationshipRefU);
								if ("true".equals(xarch.get(relationshipRef, "generated"))) {
									xarch.remove(archChangeSets, "relationship", relationshipRefU);
								}
							}

							xarch.add(archChangeSets, "relationship", registry.getRelationships());
						}
					}
				}
				catch (Exception e) {
				}
				finally {
					xArchRefsNeedingMerge.clear();
				}
			}
		}

		public void mergeAndUpdateRegistry(ObjRef xArchRef, MergeKeyAndRelationshipRegistry reg, MergeKey mergeKey, Collection<ObjRef> relationshipRefs) {
			ObjRef newRelationshipRef = mergeKey.mergeHandler.doMerge(xArchRef, relationshipRefs);
			reg.remove(mergeKey);
			for (ObjRef relationshipRef : relationshipRefs) {
				reg.remove(relationshipRef);
			}
			reg.add(xArchRef, newRelationshipRef, mergeHandlers);
		}

	}, false, true);

	public ChangeSetRelationshipManagerImpl(XArchFlatInterface xarch) {
		this.xarch = xarch;
		mergeHandlers.add(new RightMergeHandler(xarch));
		mergeHandlers.add(new LeftMergeHandler(xarch));
	}

	public void addRelationship(ObjRef xArchRef, ObjRef changeSetRelationship) {
		synchronized (lock) {
			MergeKeyAndRelationshipRegistry relationshipRegistry = xArchToRelDataTable.get(xArchRef);
			if (relationshipRegistry == null) {
				xArchToRelDataTable.put(xArchRef, relationshipRegistry = new MergeKeyAndRelationshipRegistry());
			}

			relationshipRegistry.add(xArchRef, changeSetRelationship, mergeHandlers);
			xArchRefsNeedingMerge.add(xArchRef);
			mergeThread.execute();
		}
	}

	public void removeRelationship(ObjRef xArchRef, ObjRef changeSetRelationship) {
		synchronized (lock) {
			MergeKeyAndRelationshipRegistry relationshipRegistry = xArchToRelDataTable.get(xArchRef);
			if (relationshipRegistry == null) {
				xArchToRelDataTable.put(xArchRef, relationshipRegistry = new MergeKeyAndRelationshipRegistry());
			}

			relationshipRegistry.remove(changeSetRelationship);
			xArchRefsNeedingMerge.add(xArchRef);
			mergeThread.execute();
		}
	}

	private void updateRelationships(ObjRef xArchRef) {
		synchronized (lock) {
			MergeKeyAndRelationshipRegistry relationshipRegistry = xArchToRelDataTable.get(xArchRef);
			if (relationshipRegistry == null) {
				xArchToRelDataTable.put(xArchRef, relationshipRegistry = new MergeKeyAndRelationshipRegistry());
			}

			relationshipRegistry.clear();

			ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
			ObjRef archChangeSets = xarch.getElement(changesetsContextRef, "archChangeSets", xArchRef);
			if (archChangeSets != null) {

				for (ObjRef relationshipRefU : xarch.getAll(archChangeSets, "relationship")) {
					ObjRef relationshipRef = ChangeSetUtils.resolveExternalRelationshipRef(xarch, relationshipRefU);
					if ("true".equals(xarch.get(relationshipRef, "generated"))) {
						relationshipRegistry.add(xArchRef, relationshipRef, mergeHandlers);
						xarch.remove(archChangeSets, "relationship", relationshipRefU);
					}
				}

				xArchRefsNeedingMerge.add(xArchRef);
				mergeThread.execute();
			}
		}
	}

	public void waitForCompletion() {
		mergeThread.executeAndWait();
	}
}