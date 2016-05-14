package edu.uci.isr.archstudio4.comp.xarchcs.changesetsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT.IChangeReference;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach.XArchDetachInterface;
import edu.uci.isr.sysutils.SystemUtils;
import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;
import edu.uci.isr.xarchflat.XArchMetadataUtils;

public class ChangeSetSyncImpl implements IChangeSetSync, XArchFileListener, XArchFlatListener {

	boolean DEBUG = false;

	// FIXME: This is referenced externally, not good
	public static enum RemovedReason {
		EXPLICITLY_REMOVED, OVERVIEW_ADDED, NEVER_ADDED
	}

	abstract class SyncElementHelper {

		private Collection<ObjRef> toAdd = new ArrayList<ObjRef>();

		public abstract void remove(ObjRef oldObjRef);

		public void add(ObjRef newObjRef) {
			toAdd.add(newObjRef);
		}

		public abstract void addImmediately(ObjRef newObjRef);

		public abstract boolean areChildrenElements();

		public void done() {
			for (ObjRef newObjRef : toAdd) {
				addImmediately(newObjRef);
			}
		}
	}

	private static final boolean equalz(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	protected final XArchFlatInterface xarch;
	protected final XArchDetachInterface xarchd;
	protected final IChangeSetADT csadt;
	protected Set<ObjRef> overviewModes = new HashSet<ObjRef>();

	public ChangeSetSyncImpl(XArchFlatInterface xarch, XArchDetachInterface xarchd, IChangeSetADT csadt) {
		this.xarch = xarch;
		this.xarchd = xarchd;
		this.csadt = csadt;
	}

	class ChangeSetParameterHelper {

		final int activeChangeSetIndex;
		final int excludeChangeSetsIndex;
		final ObjRef[] allChangeSetRefs;
		final int[] diffChangeSetIndecies;
		final boolean isOverview;

		public ChangeSetParameterHelper(ObjRef xArchRef, ObjRef mXArchRef, ObjRef[] changeSetRefs,
		        ObjRef[] changeSetDiffRefs, ObjRef activeChangeSetRef) {
			this.activeChangeSetIndex = activeChangeSetRef == null ? -1 : Arrays.asList(changeSetRefs).indexOf(
			        activeChangeSetRef);
			this.excludeChangeSetsIndex = changeSetRefs.length;
			this.isOverview = overviewModes.contains(mXArchRef);
			if (isOverview) {
				List<ObjRef> allChangeSetRefsList = new ArrayList<ObjRef>(XArchChangeSetUtils.getOrderedChangeSets(
				        xarch, xArchRef, "appliedChangeSets", false));
				allChangeSetRefsList.removeAll(new HashSet<ObjRef>(Arrays.asList(changeSetRefs)));
				allChangeSetRefsList.addAll(0, Arrays.asList(changeSetRefs));
				changeSetRefs = allChangeSetRefsList.toArray(new ObjRef[0]);
			}
			if (changeSetDiffRefs == null || changeSetDiffRefs.length == 0 || activeChangeSetRef != null) {
				this.allChangeSetRefs = changeSetRefs;
				this.diffChangeSetIndecies = null;
			}
			else {
				Map<ObjRef, Integer> changeSetRefsIndexMap = new HashMap<ObjRef, Integer>();
				List<ObjRef> allChangeSetRefsList = new ArrayList<ObjRef>(changeSetRefs.length
				        + changeSetDiffRefs.length);
				List<Integer> diffChangeSetIndeciesList = new ArrayList<Integer>(changeSetRefs.length
				        + changeSetDiffRefs.length);
				for (int i = 0; i < changeSetRefs.length; i++) {
					changeSetRefsIndexMap.put(changeSetRefs[i], i);
					allChangeSetRefsList.add(changeSetRefs[i]);
				}
				for (ObjRef changeSetDiff : changeSetDiffRefs) {
					Integer index = changeSetRefsIndexMap.get(changeSetDiff);
					if (index == null) {
						diffChangeSetIndeciesList.add(allChangeSetRefsList.size());
						allChangeSetRefsList.add(changeSetDiff);
					}
					else {
						diffChangeSetIndeciesList.add(index);
					}
				}
				this.allChangeSetRefs = allChangeSetRefsList.toArray(new ObjRef[allChangeSetRefsList.size()]);
				if (diffChangeSetIndeciesList.isEmpty()) {
					this.diffChangeSetIndecies = null;
				}
				else {
					this.diffChangeSetIndecies = new int[diffChangeSetIndeciesList.size()];
					for (int i = 0; i < diffChangeSetIndecies.length; i++) {
						diffChangeSetIndecies[i] = diffChangeSetIndeciesList.get(i);
					}
				}
			}

			assert new HashSet<ObjRef>(Arrays.asList(allChangeSetRefs)).containsAll(Arrays.asList(changeSetRefs));
			assert changeSetDiffRefs == null
			        || new HashSet<ObjRef>(Arrays.asList(allChangeSetRefs)).containsAll(Arrays
			                .asList(changeSetDiffRefs));
			assert activeChangeSetIndex == -1 ? activeChangeSetRef == null : activeChangeSetRef != null
			        && activeChangeSetRef.equals(changeSetRefs[activeChangeSetIndex]);
			assert activeChangeSetIndex < excludeChangeSetsIndex && 0 <= excludeChangeSetsIndex
			        && excludeChangeSetsIndex <= allChangeSetRefs.length;
		}
	}

	boolean doSync(IChangeReference reference, ObjRef[] changeSegmentRefs, int[] diffChangeSetIndecies) {
		if (diffChangeSetIndecies == null) {
			if (DEBUG) {
				System.err.println("    Synchronizing: " + reference);
			}
			return true;
		}
		for (int index : diffChangeSetIndecies) {
			if (changeSegmentRefs[index] != null) {
				if (DEBUG) {
					System.err.println("    Synchronizing: " + reference);
				}
				return true;
			}
		}

		if (DEBUG) {
			System.err.println("Not Synchronizing: " + reference);
		}
		return false;
	}

	static class AttributeMerger {

		String value = null;
		boolean merged = false;

		void mergeValue(String higherPrecedenceAttribute) {
			merged = true;
			value = higherPrecedenceAttribute;
		}

		@Override
		public int hashCode() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(Object obj) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "" + value + " (" + merged + ")";
		}
	}

	void syncAttribute(IChangeSetSyncMonitor monitor, IChangeReference attributeReference,
	        ObjRef[] attributeSegmentRefs, int[] diffChangeSetIndecies, int activeChangeSetIndex,
	        int excludeChangeSetsIndex, ObjRef xArchRef, ObjRef activeChangeSetRef, ObjRef mXArchRef,
	        boolean isOverview, ObjRef[] objRefAncestors, String attributeName) {

		assert objRefAncestors != null && objRefAncestors.length > 0;
		final ObjRef objRef = objRefAncestors[0];

		assert attributeReference != null;

		assert activeChangeSetIndex == -1 ? activeChangeSetRef == null : activeChangeSetRef != null;
		assert activeChangeSetIndex == -1 || xarch.isInstanceOf(activeChangeSetRef, "changesets#ChangeSet") : true;
		assert activeChangeSetIndex < excludeChangeSetsIndex && 0 <= excludeChangeSetsIndex
		        && excludeChangeSetsIndex <= attributeSegmentRefs.length;

		assert xArchRef != null;

		assert objRef != null; // && xarch.isAttached(objRef);
		assert attributeName != null && attributeName.length() > 0 && Character.isUpperCase(attributeName.charAt(0));
		assert xarch.getTypeMetadata(objRef).getProperty(attributeName).getMetadataType() == IXArchPropertyMetadata.ATTRIBUTE;

		if (doSync(attributeReference, attributeSegmentRefs, diffChangeSetIndecies)) {

			// the value before merging
			String preMergedValue = (String) xarch.get(objRef, attributeName);

			/*
			 * Determine the attribute according to the changes sets as they are
			 * (csMergedValue) and as if the active change set had the current
			 * attribute value (aMergedValue)
			 */
			AttributeMerger csMergedValue = new AttributeMerger();
			AttributeMerger aMergedValue = new AttributeMerger();
			AttributeMerger oMergedValue = new AttributeMerger();

			// calculate the merged values
			if (attributeSegmentRefs == null) {
				aMergedValue.mergeValue(preMergedValue);
			}
			else {
				for (int csIndex = 0, length = isOverview ? attributeSegmentRefs.length : excludeChangeSetsIndex; csIndex < length; csIndex++) {

					// include the pre-merged value for the active change set
					if (csIndex == activeChangeSetIndex) {
						aMergedValue.mergeValue(preMergedValue);
					}

					// now merge the change segment value
					ObjRef changeSegmentRef = attributeSegmentRefs[csIndex];
					if (changeSegmentRef == null) {
						continue;
					}
					assert xarch.isInstanceOf(changeSegmentRef, "changesets#AttributeSegment");

					String newValue = (String) xarch.get(changeSegmentRef, "value");
					oMergedValue.mergeValue(newValue);
					if (csIndex < excludeChangeSetsIndex) {
						csMergedValue.mergeValue(newValue);
						if (csIndex != activeChangeSetIndex) {
							aMergedValue.mergeValue(newValue);
						}
					}
				}
			}

			String postMergedValue = aMergedValue.value;

			boolean isOverviewAttribute = false;
			if (!aMergedValue.merged && !csMergedValue.merged) {
				isOverviewAttribute = true;
				postMergedValue = oMergedValue.value;
			}

			if (!isOverviewAttribute) {
				// if the two merged values differ, then we need to update the change set model
				if (!equalz(aMergedValue.value, csMergedValue.value)) {
					assert activeChangeSetRef != null;

					ObjRef attributeSegmentRef = csadt.getAttributeSegmentRef(xArchRef, activeChangeSetRef,
					        attributeReference, objRefAncestors);
					if (aMergedValue.value != null) {
						xarch.set(attributeSegmentRef, "value", aMergedValue.value);
					}
					else {
						xarch.set(attributeSegmentRef, "value", aMergedValue.value);
					}
				}
			}

			// now update the xADL model with the correct merged value
			if (!equalz(postMergedValue, preMergedValue)) {
				if (postMergedValue != null) {
					xarch.set(objRef, attributeName, postMergedValue);
				}
				else {
					xarch.clear(objRef, attributeName);
				}
			}
		}

		// everything is in sync, we're done
		if (monitor != null) {
			monitor.worked(1);
		}
	}

	public void syncAttribute(IChangeSetSyncMonitor monitor, ObjRef xArchRef, ObjRef mXArchRef, ObjRef[] changeSetRefs,
	        ObjRef[] changeSetDiffs, ObjRef activeChangeSetRef, IChangeReference preParentReference,
	        ObjRef[] objRefAncestors, String attributeName) {

		assert objRefAncestors != null && objRefAncestors.length > 0;
		final ObjRef objRef = objRefAncestors[0];

		assert xArchRef != null;

		assert changeSetRefs != null && !Arrays.asList(changeSetRefs).contains(null);

		assert activeChangeSetRef == null || Arrays.asList(changeSetRefs).contains(activeChangeSetRef);
		assert activeChangeSetRef == null || xarch.isInstanceOf(activeChangeSetRef, "changesets#ChangeSet");

		assert objRef != null; // && xarch.isAttached(objRef);
		assert attributeName != null && attributeName.length() > 0 && Character.isUpperCase(attributeName.charAt(0));
		assert xarch.getTypeMetadata(objRef).getProperty(attributeName).getMetadataType() == IXArchPropertyMetadata.ATTRIBUTE;

		IChangeReference postParentReference = csadt.getElementReference(xArchRef, objRefAncestors, true);
		if (preParentReference != null || postParentReference != null) {
			ChangeSetParameterHelper paramHelper = new ChangeSetParameterHelper(xArchRef, mXArchRef, changeSetRefs,
			        changeSetDiffs, activeChangeSetRef);
			if (activeChangeSetRef != null) {
				if (!equalz(preParentReference, postParentReference)
				        && (preParentReference != null || postParentReference != null)) {
					handleReferenceChange(monitor, paramHelper.allChangeSetRefs, paramHelper.diffChangeSetIndecies,
					        paramHelper.activeChangeSetIndex, paramHelper.excludeChangeSetsIndex, preParentReference,
					        postParentReference, xArchRef, activeChangeSetRef, mXArchRef, paramHelper.isOverview,
					        objRefAncestors);
					return;
				}
			}

			IChangeReference parentReference = csadt.getElementReference(xArchRef, objRefAncestors, false);
			IChangeReference attributeReference = csadt.getAttributeReference(xArchRef, parentReference, attributeName);
			if (attributeReference != null) {
				ObjRef[] elementSegmentRefs = csadt.getChangeSegmentRefs(xArchRef, paramHelper.allChangeSetRefs,
				        parentReference);
				ObjRef[] attributeSegmentRefs = csadt.getChildChangeSegmentRefs(xArchRef, parentReference,
				        elementSegmentRefs, attributeReference);
				syncAttribute(monitor, attributeReference, attributeSegmentRefs, null,
				        paramHelper.activeChangeSetIndex, paramHelper.excludeChangeSetsIndex, xArchRef,
				        activeChangeSetRef, mXArchRef, paramHelper.isOverview, objRefAncestors, attributeName);
			}
		}
	}

	ObjRef promoteToType(ObjRef xArchRef, ObjRef objRef, String newType, boolean isElement) {
		assert xArchRef != null;

		if (newType == null) {
			return null;
		}

		if (objRef == null) {
			ObjRef newContextRef = xarch.createContext(xArchRef, XArchMetadataUtils.getTypeContext(newType));
			if (isElement) {
				return xarch.createElement(newContextRef, XArchMetadataUtils.getTypeName(newType));
			}
			else {
				return xarch.create(newContextRef, XArchMetadataUtils.getTypeName(newType));
			}
		}

		String oldType = xarch.getTypeMetadata(objRef).getType();
		if (equalz(oldType, newType)) {
			return objRef;
		}

		if (!xarch.isAssignable(newType, oldType)) {
			// the old type cannot be promoted to the new type
			ObjRef newContextRef = xarch.createContext(xArchRef, XArchMetadataUtils.getTypeContext(newType));
			if (isElement) {
				return xarch.createElement(newContextRef, XArchMetadataUtils.getTypeName(newType));
			}
			else {
				return xarch.create(newContextRef, XArchMetadataUtils.getTypeName(newType));
			}

			// TODO: move old property values to new objRef
		}

		// the old type can be promoted to the new type
		for (String type : XArchMetadataUtils.getPromotionPathTypes(xarch, oldType, newType)) {
			ObjRef newContextRef = xarch.createContext(xArchRef, XArchMetadataUtils.getTypeContext(type));
			objRef = xarch.promoteTo(newContextRef, XArchMetadataUtils.getTypeName(type), objRef);
		}

		return objRef;
	}

	class ExplicitTypeMerger {

		String type = null;
		boolean mergedNull = false;

		void mergeType(String higherPrecedenceType) {
			if (higherPrecedenceType == null) {
				mergedNull = true;
				// Note: Do not reset type since we want the objRef to be present in explicit mode
				// type = null;
				return;
			}

			if (!higherPrecedenceType.equals(type)) {
				if (type == null || !xarch.isAssignable(higherPrecedenceType, type)) {
					type = higherPrecedenceType;
				}
			}
		}

		@Override
		public int hashCode() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(Object obj) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "" + type + " (" + mergedNull + ")";
		}
	}

	ObjRef syncElementType(IChangeSetSyncMonitor monitor, IChangeReference elementReference,
	        ObjRef[] elementSegmentRefs, int[] diffChangeSetIndecies, int activeChangeSetIndex,
	        int excludeChangeSetsIndex, ObjRef xArchRef, ObjRef activeChangeSetRef, ObjRef mXArchRef,
	        boolean isOverview, ObjRef[] oldObjRefAncestors, SyncElementHelper syncElementHelper) {

		assert oldObjRefAncestors != null && oldObjRefAncestors.length > 0;
		final ObjRef oldObjRef = oldObjRefAncestors[0];

		assert elementReference != null;

		assert activeChangeSetIndex == -1 ? activeChangeSetRef == null : activeChangeSetRef != null;
		assert activeChangeSetIndex == -1 || xarch.isInstanceOf(activeChangeSetRef, "changesets#ChangeSet") : true;
		assert activeChangeSetIndex < excludeChangeSetsIndex && 0 <= excludeChangeSetsIndex
		        && excludeChangeSetsIndex <= elementSegmentRefs.length;

		assert xArchRef != null;

		assert syncElementHelper != null;

		// the value before merging
		String preMergedType = oldObjRef == null || xarchd.isDetached(oldObjRef) ? null : xarch.getTypeMetadata(
		        oldObjRef).getType();

		/*
		 * Determine the type according to the changes sets as they are
		 * (csMergedType) and as if the active change set had the current objRef
		 * type (aMergedType)
		 */
		ExplicitTypeMerger csMergedType = new ExplicitTypeMerger();
		ExplicitTypeMerger aMergedType = new ExplicitTypeMerger();
		ExplicitTypeMerger oMergedType = new ExplicitTypeMerger();

		// calculate the merged values
		boolean isResolvable = false;
		if (elementSegmentRefs == null) {
			aMergedType.mergeType(preMergedType);
		}
		else {
			for (int csIndex = 0, length = isOverview ? elementSegmentRefs.length : excludeChangeSetsIndex; csIndex < length; csIndex++) {

				// include the pre-merged value for the active change set
				if (csIndex == activeChangeSetIndex) {
					aMergedType.mergeType(preMergedType);
					if (preMergedType != null) {
						/*
						 * The active change set does not remove the element, so
						 * we want to keep it around and record its value. We
						 * know it's resolvable because it has a valid
						 * elementReference (i.e., this method is not called
						 * until a valid element reference exists).
						 */
						isResolvable = true;
					}
				}

				// now merge the change segment value
				ObjRef elementSegmentRef = elementSegmentRefs[csIndex];
				if (elementSegmentRef == null) {
					continue;
				}
				assert xarch.isInstanceOf(elementSegmentRef, "changesets#ElementSegment");

				String newType = (String) xarch.get(elementSegmentRef, "type");

				/*
				 * Some types were incorrectly recorded as ":XArch" rather than
				 * "#XArch". This corrects the problem.
				 */
				if (newType != null) {
					newType = newType.replace(':', '#');
				}

				if (newType != null && newType.length() == 0) {
					newType = null;
				}
				oMergedType.mergeType(newType);
				if (csIndex < excludeChangeSetsIndex) {
					csMergedType.mergeType(newType);
					if (csIndex != activeChangeSetIndex) {
						aMergedType.mergeType(newType);
						/*
						 * The following uses the "=" operator rather than the
						 * "|=" because we only want to evaluate the expression
						 * if it is not already resolvable.
						 */
						isResolvable = isResolvable
						        || csadt.isElementSegmentResolvable(xArchRef, elementSegmentRef, false);
					}
				}
			}
		}

		// create the objRef of the new type
		ObjRef newObjRef = isResolvable ? promoteToType(mXArchRef, oldObjRef, aMergedType.type, syncElementHelper
		        .areChildrenElements()) : null;
		if (isOverview && newObjRef == null && !aMergedType.mergedNull) {
			newObjRef = promoteToType(mXArchRef, oldObjRef, oMergedType.type, syncElementHelper.areChildrenElements());
		}

		if (newObjRef == null) {
			if (!equalz(aMergedType.type, csMergedType.type) || aMergedType.mergedNull && !csMergedType.mergedNull) {
				if (oldObjRef != null) {
					ObjRef elementSegmentRef = csadt.getElementSegmentRef(xArchRef, activeChangeSetRef,
					        elementReference, oldObjRefAncestors);
					xarch.clear(elementSegmentRef, "type");
					csadt.removeChildren(xArchRef, activeChangeSetRef, elementReference);
				}
			}
			if (oldObjRef != null) {
				xarchd.detach(oldObjRef, aMergedType.mergedNull ? RemovedReason.EXPLICITLY_REMOVED
				        : RemovedReason.NEVER_ADDED);
			}
		}
		else {
			if (oldObjRef != newObjRef) {
				if (oldObjRef != null) {
					syncElementHelper.remove(oldObjRef);
					// it is actually removed now, so we unmark it
					xarchd.attach(oldObjRef);
				}
				syncElementHelper.add(newObjRef);
			}
			if (aMergedType.mergedNull || aMergedType.type == null) {
				xarchd.detach(newObjRef, isOverview ? RemovedReason.OVERVIEW_ADDED : RemovedReason.EXPLICITLY_REMOVED);
			}
			else {
				xarchd.attach(newObjRef);
			}
			if (!equalz(aMergedType.type, csMergedType.type)
			        || !equalz(aMergedType.mergedNull, csMergedType.mergedNull)) {
				if (aMergedType.mergedNull || aMergedType.type == null) {
					ObjRef elementSegmentRef = csadt.getElementSegmentRef(xArchRef, activeChangeSetRef,
					        elementReference, oldObjRef != null ? oldObjRefAncestors : XArchChangeSetUtils.replace(newObjRef,
					                oldObjRefAncestors));
					xarch.clear(elementSegmentRef, "type");
					csadt.removeChildren(xArchRef, activeChangeSetRef, elementReference);
				}
				else {
					ObjRef elementSegmentRef = csadt.getElementSegmentRef(xArchRef, activeChangeSetRef,
					        elementReference, oldObjRefAncestors);
					xarch.set(elementSegmentRef, "type", aMergedType.type);
				}
			}
		}
		return newObjRef;
	}

	void syncElementContent(IChangeSetSyncMonitor monitor, IChangeReference elementReference,
	        Map<IChangeReference, ObjRef[]> changeSegmentRefsMap, int changeSegmentRefsLength,
	        int[] diffChangeSetIndecies, int activeChangeSetIndex, int excludeChangeSetsIndex, ObjRef xArchRef,
	        ObjRef activeChangeSetRef, ObjRef mXArchRef, boolean isOverview, ObjRef[] objRefAncestors) {

		assert objRefAncestors != null && objRefAncestors.length > 0;
		final ObjRef objRef = objRefAncestors[0];

		assert elementReference != null;

		assert changeSegmentRefsMap != null;
		assert activeChangeSetIndex == -1 ? activeChangeSetRef == null : activeChangeSetRef != null;
		assert activeChangeSetIndex == -1 || xarch.isInstanceOf(activeChangeSetRef, "changesets#ChangeSet") : true;
		assert activeChangeSetIndex < excludeChangeSetsIndex && 0 <= excludeChangeSetsIndex
		        && excludeChangeSetsIndex <= changeSegmentRefsLength;

		assert xArchRef != null;

		assert objRef != null; // && xarch.isAttached(objRef);

		IXArchTypeMetadata type = xarch.getTypeMetadata(objRef);
		if (activeChangeSetRef != null && xarchd.isDetached(objRef)) {
			activeChangeSetRef = null;
			activeChangeSetIndex = -1;
		}

		// calculate the merged values
		try {
			if (monitor != null) {
				monitor.beginTask(type.getProperties().length);
			}

			for (IXArchPropertyMetadata prop : type.getProperties()) {
				if (monitor != null && monitor.isCanceled()) {
					return;
				}

				String name = XArchChangeSetUtils.capFirstLetter(prop.getName());
				switch (prop.getMetadataType()) {
				case IXArchPropertyMetadata.ATTRIBUTE:
					IChangeReference aReference = csadt.getAttributeReference(xArchRef, elementReference, name);
					ObjRef[] aSegmentRefs = changeSegmentRefsMap.get(aReference);
					if (aSegmentRefs == null) {
						changeSegmentRefsMap.put(aReference, aSegmentRefs = new ObjRef[changeSegmentRefsLength]);
					}
					syncAttribute(monitor, aReference, aSegmentRefs, diffChangeSetIndecies, activeChangeSetIndex,
					        excludeChangeSetsIndex, xArchRef, activeChangeSetRef, mXArchRef, isOverview,
					        objRefAncestors, name);
					break;
				case IXArchPropertyMetadata.ELEMENT:
					IChangeReference eReference = csadt.getElementReference(xArchRef, elementReference, name, false);
					ObjRef[] eSegmentRefs = changeSegmentRefsMap.get(eReference);
					if (eSegmentRefs == null) {
						changeSegmentRefsMap.put(eReference, eSegmentRefs = new ObjRef[changeSegmentRefsLength]);
					}
					syncElement(monitor, eReference, eSegmentRefs, diffChangeSetIndecies, activeChangeSetIndex,
					        excludeChangeSetsIndex, xArchRef, activeChangeSetRef, mXArchRef, isOverview,
					        objRefAncestors, name);
					break;
				case IXArchPropertyMetadata.ELEMENT_MANY:
					IChangeReference emReference = csadt.getElementManyReference(xArchRef, elementReference, name,
					        false);
					Map<IChangeReference, ObjRef[]> emSegmentRefsMap = csadt.getChildChangeSegmentRefs(xArchRef,
					        emReference, changeSegmentRefsMap.get(emReference));
					syncElementMany(monitor, emReference, emSegmentRefsMap, changeSegmentRefsLength,
					        diffChangeSetIndecies, activeChangeSetIndex, excludeChangeSetsIndex, xArchRef,
					        activeChangeSetRef, mXArchRef, isOverview, objRefAncestors, name);
					break;
				}
			}
		}
		finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	void syncElement(IChangeSetSyncMonitor monitor, IChangeReference elementReference, ObjRef[] elementSegmentRefs,
	        int[] diffChangeSetIndecies, int activeChangeSetIndex, int excludeChangeSetsIndex, ObjRef xArchRef,
	        ObjRef activeChangeSetRef, ObjRef mXArchRef, boolean isOverview, ObjRef[] oldObjRefAncestors,
	        SyncElementHelper syncElementHelper) {

		assert oldObjRefAncestors != null && oldObjRefAncestors.length > 0;
		final ObjRef oldObjRef = oldObjRefAncestors[0];

		assert elementReference != null;

		assert activeChangeSetIndex == -1 ? activeChangeSetRef == null : activeChangeSetRef != null;
		assert activeChangeSetIndex == -1 || xarch.isInstanceOf(activeChangeSetRef, "changesets#ChangeSet") : true;
		assert activeChangeSetIndex < excludeChangeSetsIndex && 0 <= excludeChangeSetsIndex
		        && excludeChangeSetsIndex <= elementSegmentRefs.length;

		assert xArchRef != null;

		assert syncElementHelper != null;

		if (doSync(elementReference, elementSegmentRefs, diffChangeSetIndecies)) {
			ObjRef newObjRef = syncElementType(monitor, elementReference, elementSegmentRefs, diffChangeSetIndecies,
			        activeChangeSetIndex, excludeChangeSetsIndex, xArchRef, activeChangeSetRef, mXArchRef, isOverview,
			        oldObjRefAncestors, syncElementHelper);
			if (newObjRef != null) {
				Map<IChangeReference, ObjRef[]> childSegmentRefs = csadt.getChildChangeSegmentRefs(xArchRef,
				        elementReference, elementSegmentRefs);
				syncElementContent(monitor, elementReference, childSegmentRefs, elementSegmentRefs.length,
				        diffChangeSetIndecies, activeChangeSetIndex, excludeChangeSetsIndex, xArchRef,
				        activeChangeSetRef, mXArchRef, isOverview, XArchChangeSetUtils.replace(newObjRef, oldObjRefAncestors));
			}
		}
	}

	void syncElement(IChangeSetSyncMonitor monitor, IChangeReference elementReference, ObjRef[] elementSegmentRefs,
	        int[] diffChangeSetIndecies, int activeChangeSetIndex, int excludeChangeSetsIndex, ObjRef xArchRef,
	        ObjRef activeChangeSetRef, ObjRef mXArchRef, boolean isOverview, final ObjRef[] parentObjRefAncestors,
	        final String elementName) {

		assert parentObjRefAncestors != null && parentObjRefAncestors.length > 0;
		final ObjRef parentObjRef = parentObjRefAncestors[0];

		assert elementReference != null;

		assert activeChangeSetIndex == -1 ? activeChangeSetRef == null : activeChangeSetRef != null;
		assert activeChangeSetIndex == -1 || xarch.isInstanceOf(activeChangeSetRef, "changesets#ChangeSet") : true;
		assert activeChangeSetIndex < excludeChangeSetsIndex && 0 <= excludeChangeSetsIndex
		        && excludeChangeSetsIndex <= elementSegmentRefs.length;

		assert xArchRef != null;

		assert parentObjRef != null; // && xarch.isAttached(parentObjRef);
		assert elementName != null && elementName.length() > 0 && Character.isUpperCase(elementName.charAt(0));
		assert xarch.getTypeMetadata(parentObjRef).getProperty(elementName).getMetadataType() == IXArchPropertyMetadata.ELEMENT;

		if (doSync(elementReference, elementSegmentRefs, diffChangeSetIndecies)) {
			ObjRef oldObjRef = (ObjRef) xarch.get(parentObjRef, elementName);
			SyncElementHelper syncElementHelper = new SyncElementHelper() {

				public void remove(ObjRef oldObjRef) {
					xarch.clear(parentObjRef, elementName);
				}

				public void addImmediately(ObjRef newObjRef) {
					xarch.set(parentObjRef, elementName, newObjRef);
				}

				public boolean areChildrenElements() {
					return false;
				}
			};
			syncElement(monitor, elementReference, elementSegmentRefs, diffChangeSetIndecies, activeChangeSetIndex,
			        excludeChangeSetsIndex, xArchRef, activeChangeSetRef, mXArchRef, isOverview, XArchChangeSetUtils.insert(oldObjRef,
			                parentObjRefAncestors), syncElementHelper);
			syncElementHelper.done();
		}
	}

	public void syncElement(IChangeSetSyncMonitor monitor, ObjRef xArchRef, ObjRef mXArchRef, ObjRef[] changeSetRefs,
	        ObjRef[] changeSetDiffs, ObjRef activeChangeSetRef, IChangeReference preParentReference,
	        ObjRef[] parentObjRefAncestors, String elementName) {

		assert parentObjRefAncestors != null && parentObjRefAncestors.length > 0;
		final ObjRef parentObjRef = parentObjRefAncestors[0];

		assert xArchRef != null;

		assert changeSetRefs != null && !Arrays.asList(changeSetRefs).contains(null);
		assert activeChangeSetRef == null || Arrays.asList(changeSetRefs).contains(activeChangeSetRef);
		assert activeChangeSetRef == null || xarch.isInstanceOf(activeChangeSetRef, "changesets#ChangeSet");

		assert parentObjRef != null; // && xarch.isAttached(parentObjRef);
		assert elementName != null && elementName.length() > 0 && Character.isUpperCase(elementName.charAt(0));
		assert xarch.getTypeMetadata(parentObjRef).getProperty(elementName).getMetadataType() == IXArchPropertyMetadata.ELEMENT;

		IChangeReference postParentReference = csadt.getElementReference(xArchRef, parentObjRefAncestors, true);
		if (preParentReference != null || postParentReference != null) {
			ChangeSetParameterHelper paramHelper = new ChangeSetParameterHelper(xArchRef, mXArchRef, changeSetRefs,
			        changeSetDiffs, activeChangeSetRef);
			if (activeChangeSetRef != null) {
				if (!equalz(preParentReference, postParentReference)
				        && (preParentReference != null || postParentReference != null)) {
					handleReferenceChange(monitor, paramHelper.allChangeSetRefs, paramHelper.diffChangeSetIndecies,
					        paramHelper.activeChangeSetIndex, paramHelper.excludeChangeSetsIndex, preParentReference,
					        postParentReference, xArchRef, activeChangeSetRef, mXArchRef, paramHelper.isOverview,
					        parentObjRefAncestors);
					return;
				}
			}

			IChangeReference parentReference = csadt.getElementReference(xArchRef, parentObjRefAncestors, false);
			IChangeReference elementReference = csadt
			        .getElementReference(xArchRef, parentReference, elementName, false);
			if (elementReference != null) {
				ObjRef[] elementSegmentRefs = csadt.getChangeSegmentRefs(xArchRef, paramHelper.allChangeSetRefs,
				        elementReference);
				syncElement(monitor, elementReference, elementSegmentRefs, paramHelper.diffChangeSetIndecies,
				        paramHelper.activeChangeSetIndex, paramHelper.excludeChangeSetsIndex, xArchRef,
				        activeChangeSetRef, mXArchRef, paramHelper.isOverview, parentObjRefAncestors, elementName);
			}
		}
	}

	void syncElementMany(IChangeSetSyncMonitor monitor, IChangeReference elementManyReference,
	        Map<IChangeReference, ObjRef[]> elementManySegmentRefMaps, int elementManySegmentRefsLength,
	        int[] diffChangeSetIndecies, int activeChangeSetIndex, int excludeChangeSetsIndex, final ObjRef xArchRef,
	        ObjRef activeChangeSetRef, final ObjRef mXArchRef, boolean isOverview,
	        final ObjRef[] parentObjRefAncestors, final String elementManyName) {

		assert parentObjRefAncestors != null && parentObjRefAncestors.length > 0;
		final ObjRef parentObjRef = parentObjRefAncestors[0];

		assert elementManyReference != null;

		assert elementManySegmentRefMaps != null;
		assert activeChangeSetIndex == -1 ? activeChangeSetRef == null : activeChangeSetRef != null;
		assert activeChangeSetIndex == -1 || xarch.isInstanceOf(activeChangeSetRef, "changesets#ChangeSet") : true;
		assert activeChangeSetIndex < excludeChangeSetsIndex && 0 <= excludeChangeSetsIndex
		        && excludeChangeSetsIndex <= elementManySegmentRefsLength;

		assert xArchRef != null;

		assert parentObjRef != null; // && xarch.isAttached(parentObjRef);
		assert elementManyName != null && elementManyName.length() > 0
		        && Character.isUpperCase(elementManyName.charAt(0));
		assert xarch.getTypeMetadata(parentObjRef).getProperty(elementManyName) != null : parentObjRef + " "
		        + xarch.getTypeMetadata(parentObjRef) + " " + elementManyName;
		assert xarch.getTypeMetadata(parentObjRef).getProperty(elementManyName).getMetadataType() == IXArchPropertyMetadata.ELEMENT_MANY : elementManyName;

		// Get all child objRefs by their current reference
		Map<IChangeReference, ObjRef> referenceToOldChildRefs = new HashMap<IChangeReference, ObjRef>();
		for (ObjRef objRef : xarch.getAll(parentObjRef, elementManyName)) {
			if (monitor != null && monitor.isCanceled()) {
				return;
			}

			IChangeReference reference = csadt.getElementReference(xArchRef, elementManyReference, objRef, false);
			if (reference != null) {
				ObjRef duplicateReferencedObjRef = referenceToOldChildRefs.put(reference, objRef);
				if (duplicateReferencedObjRef != null) {
					//System.err.println("ObjRef with a duplicate reference found, removing! (" + parentObjRef + ", " + elementManyName + ", replacing " + duplicateReferencedObjRef + " with " + objRef + ", which are both referenced by " + reference + ")");
					xarch.remove(parentObjRef, elementManyName, duplicateReferencedObjRef);
				}
			}
		}

		try {
			if (monitor != null) {
				monitor.beginTask(elementManySegmentRefMaps.size() + referenceToOldChildRefs.size());
			}

			SyncElementHelper syncElementHelper = new SyncElementHelper() {

				public void remove(ObjRef oldObjRef) {
					xarch.remove(parentObjRef, elementManyName, oldObjRef);
				}

				public void addImmediately(ObjRef newObjRef) {
					xarch.add(parentObjRef, elementManyName, newObjRef);
				}

				public boolean areChildrenElements() {
					return equalz(mXArchRef, parentObjRef);
				}
			};

			// process the expected references
			for (Map.Entry<IChangeReference, ObjRef[]> entry : elementManySegmentRefMaps.entrySet()) {
				if (monitor != null && monitor.isCanceled()) {
					return;
				}

				IChangeReference elementReference = entry.getKey();
				ObjRef[] elementSegmentRefs = entry.getValue();
				if (elementSegmentRefs == null) {
					entry.setValue(elementSegmentRefs = new ObjRef[elementManySegmentRefsLength]);
				}
				ObjRef oldObjRef = referenceToOldChildRefs.remove(elementReference);

				syncElement(monitor, elementReference, elementSegmentRefs, diffChangeSetIndecies, activeChangeSetIndex,
				        excludeChangeSetsIndex, xArchRef, activeChangeSetRef, mXArchRef, isOverview, XArchChangeSetUtils.insert(oldObjRef,
				                parentObjRefAncestors), syncElementHelper);
			}

			// process the remaining references
			if (activeChangeSetRef != null) {
				// synchronize with the active change set
				for (Map.Entry<IChangeReference, ObjRef> entry : referenceToOldChildRefs.entrySet()) {
					if (monitor != null && monitor.isCanceled()) {
						return;
					}

					IChangeReference elementReference = entry.getKey();
					ObjRef oldObjRef = entry.getValue();
					ObjRef[] elementSegmentRefs = new ObjRef[elementManySegmentRefsLength];

					syncElement(monitor, elementReference, elementSegmentRefs, diffChangeSetIndecies,
					        activeChangeSetIndex, excludeChangeSetsIndex, xArchRef, activeChangeSetRef, mXArchRef,
					        isOverview, XArchChangeSetUtils.insert(oldObjRef, parentObjRefAncestors), syncElementHelper);
				}
			}
			else {
				// there's no active change set, so just remove
				if (!referenceToOldChildRefs.isEmpty()) {
					xarchd.detach(referenceToOldChildRefs.values().toArray(new ObjRef[referenceToOldChildRefs.size()]),
					        RemovedReason.NEVER_ADDED);
				}
			}

			syncElementHelper.done();
		}
		finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	public void syncElementMany(IChangeSetSyncMonitor monitor, ObjRef xArchRef, ObjRef mXArchRef,
	        ObjRef[] changeSetRefs, ObjRef[] changeSetDiffs, ObjRef activeChangeSetRef, ObjRef[] parentObjRefAncestors,
	        String elementManyName) {

		assert parentObjRefAncestors != null && parentObjRefAncestors.length > 0;
		final ObjRef parentObjRef = parentObjRefAncestors[0];

		assert xArchRef != null;

		assert changeSetRefs != null && !Arrays.asList(changeSetRefs).contains(null);

		assert changeSetRefs != null && !Arrays.asList(changeSetRefs).contains(null);
		assert activeChangeSetRef == null || Arrays.asList(changeSetRefs).contains(activeChangeSetRef);
		assert activeChangeSetRef == null || xarch.isInstanceOf(activeChangeSetRef, "changesets#ChangeSet");

		assert parentObjRef != null; // && xarch.isAttached(parentObjRef);
		assert elementManyName != null && elementManyName.length() > 0
		        && Character.isUpperCase(elementManyName.charAt(0));
		assert xarch.getTypeMetadata(parentObjRef).getProperty(elementManyName).getMetadataType() == IXArchPropertyMetadata.ELEMENT_MANY;

		IChangeReference parentReference = csadt.getElementReference(xArchRef, parentObjRefAncestors, false);
		IChangeReference elementManyReference = csadt.getElementReference(xArchRef, parentReference, elementManyName,
		        false);
		if (elementManyReference != null) {
			ChangeSetParameterHelper paramHelper = new ChangeSetParameterHelper(xArchRef, mXArchRef, changeSetRefs,
			        changeSetDiffs, activeChangeSetRef);
			/*
			 * Note, we do not need to determine if the parent has been recorded
			 * yet -- parent references may not use ELEMENT_MANY values.
			 */

			ObjRef[] elementManySegmentRefs = csadt.getChangeSegmentRefs(xArchRef, paramHelper.allChangeSetRefs,
			        elementManyReference);
			if (doSync(elementManyReference, elementManySegmentRefs, paramHelper.diffChangeSetIndecies)) {
				Map<IChangeReference, ObjRef[]> elementManySegmentRefMaps = csadt.getChildChangeSegmentRefs(xArchRef,
				        elementManyReference, elementManySegmentRefs);
				syncElementMany(monitor, elementManyReference, elementManySegmentRefMaps,
				        paramHelper.allChangeSetRefs.length, paramHelper.diffChangeSetIndecies,
				        paramHelper.activeChangeSetIndex, paramHelper.excludeChangeSetsIndex, xArchRef,
				        activeChangeSetRef, mXArchRef, paramHelper.isOverview, parentObjRefAncestors, elementManyName);
			}
		}
	}

	public void syncElementMany(IChangeSetSyncMonitor monitor, final ObjRef xArchRef, final ObjRef mXArchRef,
	        ObjRef[] changeSetRefs, ObjRef[] changeSetDiffs, ObjRef activeChangeSetRef,
	        final ObjRef[] parentObjRefAncestors, final String elementManyName, ObjRef childObjRef) {

		assert parentObjRefAncestors != null && parentObjRefAncestors.length > 0;
		final ObjRef parentObjRef = parentObjRefAncestors[0];

		assert xArchRef != null;

		assert changeSetRefs != null && !Arrays.asList(changeSetRefs).contains(null);

		assert changeSetRefs != null && !Arrays.asList(changeSetRefs).contains(null);
		assert activeChangeSetRef == null || Arrays.asList(changeSetRefs).contains(activeChangeSetRef);
		assert activeChangeSetRef == null || xarch.isInstanceOf(activeChangeSetRef, "changesets#ChangeSet");

		assert parentObjRef != null; // && xarch.isAttached(parentObjRef);
		assert elementManyName != null && elementManyName.length() > 0
		        && Character.isUpperCase(elementManyName.charAt(0));
		assert xarch.getTypeMetadata(parentObjRef).getProperty(elementManyName).getMetadataType() == IXArchPropertyMetadata.ELEMENT_MANY;

		final ObjRef[] childObjRefAncestors = XArchChangeSetUtils.insert(childObjRef, parentObjRefAncestors);

		IChangeReference elementReference = csadt.getElementReference(xArchRef, childObjRefAncestors, false);
		IChangeReference elementManyReference = csadt.getParentReference(xArchRef, elementReference);
		if (elementManyReference != null) {
			ChangeSetParameterHelper paramHelper = new ChangeSetParameterHelper(xArchRef, mXArchRef, changeSetRefs,
			        changeSetDiffs, activeChangeSetRef);
			ObjRef[] elementManySegmentRefs = csadt.getChangeSegmentRefs(xArchRef, paramHelper.allChangeSetRefs,
			        elementManyReference);
			if (doSync(elementManyReference, elementManySegmentRefs, paramHelper.diffChangeSetIndecies)) {
				ObjRef[] elementSegmentRefs = csadt.getChildChangeSegmentRefs(xArchRef, elementManyReference,
				        elementManySegmentRefs, elementReference);

				SyncElementHelper syncElementHelper = new SyncElementHelper() {

					public void remove(ObjRef oldObjRef) {
						xarch.remove(parentObjRef, elementManyName, oldObjRef);
					}

					public void addImmediately(ObjRef newObjRef) {
						xarch.add(parentObjRef, elementManyName, newObjRef);
					}

					public boolean areChildrenElements() {
						return equalz(mXArchRef, parentObjRef);
					}
				};

				syncElement(monitor, elementReference, elementSegmentRefs, paramHelper.diffChangeSetIndecies,
				        paramHelper.activeChangeSetIndex, paramHelper.excludeChangeSetsIndex, xArchRef,
				        activeChangeSetRef, mXArchRef, paramHelper.isOverview, childObjRefAncestors, syncElementHelper);
				syncElementHelper.done();
			}
		}
	}

	void syncElement(IChangeSetSyncMonitor monitor, IChangeReference elementReference, ObjRef[] elementSegmentRefs,
	        int[] diffChangeSetIndecies, int activeChangeSetIndex, int excludeChangeSetsIndex, final ObjRef xArchRef,
	        ObjRef activeChangeSetRef, final ObjRef mXArchRef, boolean isOverview, ObjRef[] objRefAncestors) {

		assert objRefAncestors != null && objRefAncestors.length > 0;
		final ObjRef objRef = objRefAncestors[0];

		assert elementReference != null;

		assert activeChangeSetIndex == -1 ? activeChangeSetRef == null : activeChangeSetRef != null;
		assert activeChangeSetIndex == -1 || xarch.isInstanceOf(activeChangeSetRef, "changesets#ChangeSet") : true;
		assert activeChangeSetIndex < excludeChangeSetsIndex && 0 <= excludeChangeSetsIndex
		        && excludeChangeSetsIndex <= elementSegmentRefs.length;

		assert xArchRef != null;

		assert objRefAncestors.length > 1;
		final ObjRef parentObjRef = objRefAncestors[1];
		final ObjRef[] parentObjRefAncestors = XArchChangeSetUtils.remove(1, objRefAncestors);
		assert parentObjRef != null; // && xarch.isAttached(parentObjRef);

		final String name = XArchChangeSetUtils.getActualElementName(xarch, objRefAncestors, 0);

		assert name != null && name.length() > 0 && Character.isUpperCase(name.charAt(0));
		assert Arrays.asList(new Integer[] { IXArchPropertyMetadata.ELEMENT, IXArchPropertyMetadata.ELEMENT_MANY })
		        .contains(xarch.getTypeMetadata(parentObjRef).getProperty(name).getMetadataType());

		IXArchPropertyMetadata prop = xarch.getTypeMetadata(parentObjRef).getProperty(name);
		switch (prop.getMetadataType()) {
		case IXArchPropertyMetadata.ATTRIBUTE:
			throw new RuntimeException(); // this shouldn't happen

		case IXArchPropertyMetadata.ELEMENT:
			syncElement(monitor, elementReference, elementSegmentRefs, diffChangeSetIndecies, activeChangeSetIndex,
			        excludeChangeSetsIndex, xArchRef, activeChangeSetRef, mXArchRef, isOverview, parentObjRefAncestors,
			        name);
			return;

		case IXArchPropertyMetadata.ELEMENT_MANY:
			SyncElementHelper syncElementHelper = new SyncElementHelper() {

				public void remove(ObjRef oldObjRef) {
					xarch.remove(parentObjRef, name, oldObjRef);
				}

				public void addImmediately(ObjRef newObjRef) {
					xarch.add(parentObjRef, name, newObjRef);
				}

				public boolean areChildrenElements() {
					return equalz(mXArchRef, parentObjRef);
				}
			};

			syncElement(monitor, elementReference, elementSegmentRefs, diffChangeSetIndecies, activeChangeSetIndex,
			        excludeChangeSetsIndex, xArchRef, activeChangeSetRef, mXArchRef, isOverview, objRefAncestors,
			        syncElementHelper);
			syncElementHelper.done();
			return;
		}
	}

	public void handleXArchFileEvent(XArchFileEvent evt) {
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt) {
	}

	public ChangeStatus[] getChangeStatus(ObjRef xArchRef, ObjRef[] changeSetRefs, IChangeReference changeReference,
	        ObjRef[] changeSegmentRefs, boolean considerRemovalOfParent) {

		ChangeStatus[] changeStatuses = new ChangeStatus[changeSegmentRefs.length];

		for (int i = 0; i < changeSegmentRefs.length; i++) {

			ObjRef changeSegmentRef = changeSegmentRefs[i];
			if (changeSegmentRef != null) {
				if (xarch.isInstanceOf(changeSegmentRef, "changesets#ElementSegment")) {

					String type = (String) xarch.get(changeSegmentRef, "type");

					/*
					 * Some types were incorrectly recorded as ":XArch" rather
					 * than "#XArch". This corrects the problem.
					 */
					if (type != null) {
						type = type.replace(':', '#');
					}

					if (type == null || type.length() == 0) {
						changeStatuses[i] = ChangeStatus.REMOVED;
						continue;
					}

					if (xarch.isInstanceOf(xarch.getParent(changeSegmentRef), "changesets#ElementManySegment")) {
						if (csadt.isElementSegmentResolvable(xArchRef, changeSegmentRef, false)) {
							changeStatuses[i] = ChangeStatus.ADDED;
							continue;
						}
					}
				}

				changeStatuses[i] = ChangeStatus.MODIFIED;
				continue;
			}
			else {
				if (considerRemovalOfParent) {
					// check for removal of a parent by this change set
					IChangeReference parentReference = changeReference;
					while (changeSegmentRef == null
					        && (parentReference = csadt.getParentReference(xArchRef, parentReference)) != null) {
						changeSegmentRef = csadt
						        .getElementSegmentRef(xArchRef, changeSetRefs[i], parentReference, null);
					}
					if (changeSegmentRef != null && xarch.isInstanceOf(changeSegmentRef, "changesets#ElementSegment")) {
						String type = (String) xarch.get(changeSegmentRef, "type");

						/*
						 * Some types were incorrectly recorded as ":XArch"
						 * rather than "#XArch". This corrects the problem.
						 */
						if (type != null) {
							type = type.replace(':', '#');
						}

						if (type == null || type.length() == 0) {
							changeStatuses[i] = ChangeStatus.REMOVED;
							continue;
						}
					}
				}
			}

			changeStatuses[i] = ChangeStatus.UNMODIFIED;
		}

		return changeStatuses;
	}

	public ChangeStatus getChangeStatus(ObjRef xArchRef, ObjRef[] objRefAncestors, ObjRef[] changeSetRefs,
	        boolean considerRemovalOfParent, boolean overview) {

		assert objRefAncestors != null && objRefAncestors.length > 0;
		final ObjRef objRef = objRefAncestors[0];

		ChangeStatus s = ChangeStatus.UNMODIFIED;

		if (changeSetRefs.length > 0) {
			IChangeReference changeReference = csadt.getElementReference(xArchRef, objRefAncestors, false);
			if (changeReference != null) {
				ChangeStatus[] changeStatuses = getChangeStatus(xArchRef, changeSetRefs, changeReference, csadt
				        .getChangeSegmentRefs(xArchRef, changeSetRefs, changeReference), considerRemovalOfParent);
				for (ChangeStatus changeStatus : changeStatuses) {
					switch (changeStatus) {
					case ADDED:
						s = changeStatus;
						break;
					case MODIFIED:
						if (s != ChangeStatus.ADDED) {
							s = changeStatus;
						}
						break;
					case REMOVED:
						return changeStatus;
					case UNMODIFIED:
						break;
					case UNADDED:
						throw new RuntimeException(); // this shouldn't happen
					case DETACHED:
						throw new RuntimeException(); // this shouldn't happen
					}
				}
			}
		}

		if (s == ChangeStatus.UNMODIFIED) {
			Object removedReason = xarchd.getDetachedReason(objRef);
			if (removedReason == null && considerRemovalOfParent) {
				int index = 1;
				while (removedReason == null && index < objRefAncestors.length) {
					removedReason = xarchd.getDetachedReason(objRefAncestors[index++]);
				}
			}
			if (null == removedReason)
				return ChangeStatus.UNMODIFIED;
			if (RemovedReason.NEVER_ADDED == removedReason)
				return ChangeStatus.DETACHED;
			if (RemovedReason.OVERVIEW_ADDED == removedReason || RemovedReason.EXPLICITLY_REMOVED == removedReason)
				return overview ? ChangeStatus.UNADDED : ChangeStatus.DETACHED;
			return ChangeStatus.DETACHED;
		}

		return s;
	}

	private void handleReferenceChange(IChangeSetSyncMonitor monitor, ObjRef[] changeSetRefs,
	        int[] diffChangeSetIndecies, int activeChangeSetIndex, int excludeChangeSetsIndex,
	        IChangeReference preReference, IChangeReference postReference, ObjRef xArchRef, ObjRef activeChangeSetRef,
	        ObjRef mXArchRef, boolean isOverview, ObjRef[] objRefAncestors) {

		assert objRefAncestors != null && objRefAncestors.length > 0;
		final ObjRef objRef = objRefAncestors[0];

		assert activeChangeSetIndex >= 0; // required for this method
		assert activeChangeSetIndex == -1 ? activeChangeSetRef == null : activeChangeSetRef != null
		        && activeChangeSetRef.equals(changeSetRefs[activeChangeSetIndex]);
		assert activeChangeSetIndex == -1 || xarch.isInstanceOf(activeChangeSetRef, "changesets#ChangeSet") : true;
		assert activeChangeSetIndex < excludeChangeSetsIndex && 0 <= excludeChangeSetsIndex
		        && excludeChangeSetsIndex <= changeSetRefs.length;

		assert xArchRef != null;

		assert !equalz(preReference, postReference) && (preReference != null || postReference != null);
		assert objRef != null; // && xarch.isAttached(objRef);

		Object[] deviation = csadt.getDeviation(xArchRef, preReference, postReference, objRefAncestors);
		if (deviation != null) {
			IChangeReference preR = (IChangeReference) deviation[0];
			IChangeReference postR = (IChangeReference) deviation[1];
			ObjRef[] objR = (ObjRef[]) deviation[2];

			if (preR != null) {
				ObjRef changeSegmentRef = csadt.getElementSegmentRef(xArchRef, activeChangeSetRef, preR, null);
				if (changeSegmentRef != null) {
					assert xarch.isInstanceOf(changeSegmentRef, "changesets#ElementSegment");
					xarch.clear(changeSegmentRef, "type");
					csadt.removeChildren(xArchRef, activeChangeSetRef, preReference);
				}
			}
			if (postR != null) {
				ObjRef[] elementSegmentRefs = csadt.getChangeSegmentRefs(xArchRef, changeSetRefs, postR);
				syncElement(monitor, postR, elementSegmentRefs, diffChangeSetIndecies, activeChangeSetIndex,
				        excludeChangeSetsIndex, xArchRef, activeChangeSetRef, mXArchRef, isOverview, objR);
			}
		}
	}

	public void moveChanges(ObjRef xArchRef, ObjRef[] sourceChangeSetRefs, ObjRef targetChangeSetRef,
	        ObjRef targetObjRef) {
		ObjRef[] targetObjRefAncestors = xarch.getAllAncestors(targetObjRef);
		ChangeStatus changeStatus = getChangeStatus(xArchRef, targetObjRefAncestors, sourceChangeSetRefs, false, false);
		if (changeStatus != ChangeStatus.REMOVED) {

			//Check if target change set is included in source change set.
			boolean found = false;
			Set<ObjRef> changeSetList = new HashSet<ObjRef>();
			for (ObjRef sourceChangeSetRef : sourceChangeSetRefs) {
				changeSetList.add(sourceChangeSetRef);
				if (sourceChangeSetRef.equals(targetChangeSetRef)) {
					found = true;
					break;
				}
			}
			ObjRef[] parentObjRefAncestors = XArchChangeSetUtils.remove(1, targetObjRefAncestors);
			if (found) {
				String elementName = XArchChangeSetUtils.getActualElementName(xarch, targetObjRefAncestors, 0);
				syncElementMany(null, xArchRef, xArchRef, sourceChangeSetRefs, null, null, parentObjRefAncestors,
						elementName, targetObjRef);
				csadt.removeAssociatedChanges(xArchRef, sourceChangeSetRefs, targetObjRefAncestors);
				syncElementMany(null, xArchRef, xArchRef, sourceChangeSetRefs, null, targetChangeSetRef,
				        parentObjRefAncestors, elementName, targetObjRef);
			}
			else {
				//changeSetList will contain all the source change sets from the array.
				//We will need to put the target change set in the correct position of the list.
				ObjRef changeSetsContextRef = xarch.createContext(xArchRef, "changesets");
				ObjRef archChangeSetsElementRef = xarch.getElement(changeSetsContextRef, "ArchChangeSets", xArchRef);
				ObjRef[] allChangeSetRefs = xarch.getAll(archChangeSetsElementRef, "ChangeSet");
				allChangeSetRefs = ChangeSetUtils.resolveExternalChangeSetRefs(xarch, allChangeSetRefs);
				Set<ObjRef> finalChangeSetRefList = new HashSet<ObjRef>();
				for (ObjRef changeSetRef : allChangeSetRefs) {
					if (changeSetList.contains(changeSetRef)) {
						finalChangeSetRefList.add(changeSetRef);
					}
					else if (changeSetRef.equals(targetChangeSetRef)) {
						finalChangeSetRefList.add(targetChangeSetRef);
						finalChangeSetRefList.addAll(changeSetList);
						break;
					}
				}
				String elementName = XArchChangeSetUtils.getActualElementName(xarch, targetObjRefAncestors, 0);
				syncElementMany(null, xArchRef, xArchRef, sourceChangeSetRefs, null, null, parentObjRefAncestors,
						elementName, targetObjRef);
				csadt.removeAssociatedChanges(xArchRef, sourceChangeSetRefs, targetObjRefAncestors);
				syncElementMany(null, xArchRef, xArchRef, finalChangeSetRefList
				        .toArray(new ObjRef[finalChangeSetRefList.size()]), null, targetChangeSetRef,
				        parentObjRefAncestors, elementName, targetObjRef);
			}
		}
		else {
			csadt.removeAssociatedChanges(xArchRef, new ObjRef[] { targetChangeSetRef }, targetObjRefAncestors);

			List<ObjRef> removingChangeSetRefs = new ArrayList<ObjRef>();
			for (ObjRef sourceChangeSetRef : sourceChangeSetRefs) {
				ChangeStatus tempChangeStatus = getChangeStatus(xArchRef, targetObjRefAncestors,
				        new ObjRef[] { sourceChangeSetRef }, false, false);
				if (tempChangeStatus == ChangeStatus.REMOVED) {
					removingChangeSetRefs.add(sourceChangeSetRef);
				}
			}

			ObjRef removingChangeSetRef = removingChangeSetRefs.get(0);

			IChangeReference changeReference = csadt.getElementReference(xArchRef, targetObjRefAncestors, false);
			ObjRef changeSegmentRef = csadt.getChangeSegmentRef(xArchRef, removingChangeSetRef, changeReference);
			List<ObjRef> changeSegmentRefList = new ArrayList<ObjRef>();
			changeSegmentRefList.add(changeSegmentRef);
			ObjRef ancestorRef = xarch.getParent(changeSegmentRef);
			while (!"xArchElement".equals(xarch.getElementName(ancestorRef))) {
				changeSegmentRefList.add(ancestorRef);
				ancestorRef = xarch.getParent(ancestorRef);
			}

			ObjRef currentChildSegmentRef = null;
			IChangeReference currentChangeReference = csadt.getElementReference(xArchRef, targetObjRefAncestors, false);
			boolean attachToTree = true;
			for (ObjRef currentChangeSegmentRef : changeSegmentRefList) {
				ObjRef clonedChangeSegmentRef = xarch.cloneElement(currentChangeSegmentRef, 0);
				if (currentChildSegmentRef != null) {
					ObjRef targetChangeSegmentRef = csadt.getChangeSegmentRef(xArchRef, targetChangeSetRef,
					        currentChangeReference);
					if (targetChangeSegmentRef != null) {
						xarch.add(targetChangeSegmentRef, "ChangeSegment", currentChildSegmentRef);
						attachToTree = false;
						break;
					}
					else {
						xarch.add(clonedChangeSegmentRef, "ChangeSegment", currentChildSegmentRef);
					}
				}
				currentChildSegmentRef = clonedChangeSegmentRef;
				currentChangeReference = csadt.getParentReference(xArchRef, currentChangeReference);
			}
			if (attachToTree) {
				ObjRef xArchElementRef = (ObjRef) xarch.get(targetChangeSetRef, "xArchElement");
				if (xArchElementRef == null) {
					xArchElementRef = xarch.cloneElement((ObjRef) xarch.get(removingChangeSetRef, "xArchElement"), 0);
					xarch.set(targetChangeSetRef, "xArchElement", xArchElementRef);
				}
				xarch.add(xArchElementRef, "ChangeSegment", currentChildSegmentRef);
			}
			csadt.removeAssociatedChanges(xArchRef, removingChangeSetRefs.toArray(new ObjRef[removingChangeSetRefs
			        .size()]), targetObjRefAncestors);
		}
	}

	public boolean getOverviewMode(ObjRef xArchRef) {
		return overviewModes.contains(xArchRef);
	}

	public void setOverviewMode(ObjRef xArchRef, boolean overviewMode) {
		if (overviewMode ? overviewModes.add(xArchRef) : overviewModes.remove(xArchRef)) {
			updateOverviewMode(xArchRef);
		}
	}

	private void updateOverviewMode(ObjRef xArchRef) {
		syncElementMany(null, xArchRef, xArchRef, //
		        XArchChangeSetUtils.getOrderedChangeSets(xarch, xArchRef, "appliedChangeSets", true).toArray(
		                new ObjRef[0]), //
		        null, null, new ObjRef[] { xArchRef }, "Object");
	}
}
