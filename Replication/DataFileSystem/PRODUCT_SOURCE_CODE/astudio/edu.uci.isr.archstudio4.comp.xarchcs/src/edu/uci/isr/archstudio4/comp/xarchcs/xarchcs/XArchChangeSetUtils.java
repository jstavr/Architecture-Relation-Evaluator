package edu.uci.isr.archstudio4.comp.xarchcs.xarchcs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.IChangeSetSyncMonitor;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;

public class XArchChangeSetUtils {

	public static final String getActualElementName(XArchFlatQueryInterface xarch, ObjRef[] objRefAncestors, int index) {
		int length = objRefAncestors.length;
		if (index >= length - 1)
			throw new IndexOutOfBoundsException();
		if (index == length - 2) {
			if (xarch.isInstanceOf(objRefAncestors[length - 1], "#XArch")) {
				return "Object";
			}
		}
		return capFirstLetter(xarch.getElementName(objRefAncestors[index]));
	}

	public static final String capFirstLetter(String s) {
		if (s == null || s.length() == 0) {
			return s;
		}
		char ch = s.charAt(0);
		if (Character.isUpperCase(ch)) {
			return s;
		}
		return Character.toUpperCase(ch) + s.substring(1);
	}

	public static final ObjRef[] insert(ObjRef objRef, ObjRef[] objRefs) {
		int length = objRefs.length;
		ObjRef[] newObjRefs = new ObjRef[1 + length];
		System.arraycopy(objRefs, 0, newObjRefs, 1, length);
		newObjRefs[0] = objRef;
		return newObjRefs;
	}

	public static final ObjRef[] replace(ObjRef objRef, ObjRef[] objRefs) {
		if(objRef == objRefs[0])
			return objRefs;
		int length = objRefs.length;
		ObjRef[] newObjRefs = new ObjRef[length];
		System.arraycopy(objRefs, 1, newObjRefs, 1, length - 1);
		newObjRefs[0] = objRef;
		return newObjRefs;
	}

	public static final ObjRef[] remove(int count, ObjRef[] objRefs) {
		if(count == 0)
			return objRefs;
		int length = objRefs.length;
		ObjRef[] newObjRefs = new ObjRef[length - count];
		System.arraycopy(objRefs, count, newObjRefs, 0, length - count);
		return newObjRefs;
	}

	private static int parseInt(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		}
		catch (Throwable t) {
		}
		return defaultValue;
	}

	private static List<ObjRef> toList(final Map<ObjRef, Integer> orderMap) {
		List<ObjRef> orderedList = new ArrayList<ObjRef>(orderMap.keySet());
		Collections.sort(orderedList, new Comparator<ObjRef>() {

			public int compare(ObjRef o1, ObjRef o2) {
				return orderMap.get(o1).compareTo(orderMap.get(o2));
			}
		});
		return orderedList;
	}

	private static boolean containsAny(Set<ObjRef> set, Collection<ObjRef> objects) {
		for (ObjRef o : objects) {
			if (set.contains(o)) {
				return true;
			}
		}
		return false;
	}

	public static List<ObjRef> loadOrderObjRefs(XArchFlatInterface xarch, ObjRef xArchRef, ObjRef archChangeSetsRef,
	        String listName) {
		List<ObjRef> orderList = new ArrayList<ObjRef>();
		Set<ObjRef> orderSet = new HashSet<ObjRef>();
		String objRefOrder = (String) xarch.get(archChangeSetsRef, listName);
		if (objRefOrder != null) {
			for (String id : objRefOrder.split("\\,")) {
				id = id.trim();
				if (id.length() > 0) {
					ObjRef objRef = xarch.getByID(id);
					if (objRef != null && orderSet.add(objRef)) {
						orderList.add(objRef);
					}
				}
			}
		}
		return orderList;
	}

	public static void saveOrderedObjRefs(XArchFlatInterface xarch, ObjRef xArchRef, String listName,
	        List<ObjRef> objRefs) {
		ObjRef archChangeSetsRef = xarch.getElement(xarch.createContext(xArchRef, "changesets"), "archChangeSets",
		        xArchRef);
		if (archChangeSetsRef != null) {
			saveOrderObjRefs(xarch, xArchRef, archChangeSetsRef, listName, objRefs);
		}
	}

	private static void saveOrderObjRefs(XArchFlatInterface xarch, ObjRef xArchRef, ObjRef archChangeSetsRef,
	        String listName, List<ObjRef> objRefs) {
		StringBuffer sb = new StringBuffer();
		for (ObjRef changeSetRef : objRefs) {
			if (changeSetRef instanceof ObjRef) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(XadlUtils.getID(xarch, changeSetRef));
			}
		}
		xarch.set(archChangeSetsRef, listName, sb.toString());
	}

	public static List<ObjRef> getOrderedRelationships(XArchFlatInterface xarch, ObjRef xArchRef) {
		ObjRef changeSetContextRef = xarch.createContext(xArchRef, "changesets");
		ObjRef archChangeSetsRef = xarch.getElement(changeSetContextRef, "archChangeSets", xArchRef);
		List<ObjRef> orderedRelationshipRefs = null;
		if (archChangeSetsRef != null) {
			List<ObjRef> relationshipList = loadOrderObjRefs(xarch, xArchRef, archChangeSetsRef, "relationshipOrder");
			orderedRelationshipRefs = relationshipList;
		}
		return orderedRelationshipRefs;
	}

	private static List<ObjRef> getOrderedChangeSets(XArchFlatInterface xarch, ObjRef xArchRef,
	        ObjRef changeSetContextRef, ObjRef archChangeSetsRef, List<ObjRef> appliedList) {
		List<ObjRef> orderList = loadOrderObjRefs(xarch, xArchRef, archChangeSetsRef, "changeSetOrder");
		final Map<ObjRef, Integer> orderMap = new HashMap<ObjRef, Integer>();
		for (ObjRef changeSetRef : orderList) {
			if (changeSetRef != null && !orderMap.containsKey(changeSetRef)) {
				// place ordered change sets in natural order
				orderMap.put(changeSetRef, orderMap.size());
			}
		}
		for (ObjRef changeSetRef : appliedList) {
			if (changeSetRef != null && !orderMap.containsKey(changeSetRef)) {
				// place unordered applied change sets at the end
				orderMap.put(changeSetRef, orderMap.size());
			}
		}
		for (ObjRef changeSetRef : xarch.getAll(archChangeSetsRef, "changeSet")) {
			if (changeSetRef != null && !orderMap.containsKey(changeSetRef)) {
				// place unidentified change sets at the beginning
				orderMap.put(changeSetRef, -orderMap.size());
			}
		}
		List<ObjRef> newOrderList = toList(orderMap);
		if (!orderList.equals(newOrderList)) {
			// TODO: a "get" should not update the model
			saveOrderObjRefs(xarch, xArchRef, archChangeSetsRef, "changeSetOrder", newOrderList);
		}
		return newOrderList;
	}

	public static List<ObjRef> getOrderedChangeSets(XArchFlatInterface xarch, ObjRef xArchRef, String listName,
	        boolean limitToApplied) {
		ObjRef changeSetContextRef = xarch.createContext(xArchRef, "changesets");
		ObjRef archChangeSetsRef = xarch.getElement(changeSetContextRef, "archChangeSets", xArchRef);
		List<ObjRef> orderedChangeSetRefs = null;
		if (archChangeSetsRef != null) {
			List<ObjRef> appliedList = loadOrderObjRefs(xarch, xArchRef, archChangeSetsRef, listName);
			orderedChangeSetRefs = getOrderedChangeSets(xarch, xArchRef, changeSetContextRef, archChangeSetsRef,
			        appliedList);
			if (limitToApplied) {
				Set<ObjRef> appliedSet = new HashSet<ObjRef>(appliedList);
				orderedChangeSetRefs.retainAll(appliedSet);
			}
		}
		return orderedChangeSetRefs;
	}

	public static List<ObjRef> resolve(XArchFlatQueryInterface xarch, List<ObjRef> orderedChangeSetRefs) {
		ObjRef[] unresolvedOrderedChangeSetRefs = orderedChangeSetRefs.toArray(new ObjRef[orderedChangeSetRefs.size()]);
		ObjRef[] resolvedOrderedChangeSetRefs = ChangeSetUtils.resolveExternalChangeSetRefs(xarch,
		        unresolvedOrderedChangeSetRefs);
		return new ArrayList<ObjRef>(Arrays.asList(resolvedOrderedChangeSetRefs));
	}

	public static List<ObjRef> unresolveChangeSetRefs(XArchFlatQueryInterface xarch, ObjRef xArchRef,
	        List<ObjRef> orderedChangeSetRefs) {
		ObjRef[] resolvedOrderedChangeSetRefs = orderedChangeSetRefs.toArray(new ObjRef[orderedChangeSetRefs.size()]);
		ObjRef[] unresolvedOrderedChangeSetRefs = ChangeSetUtils.unresolveExternalChangeSetRefs(xarch, xArchRef,
		        resolvedOrderedChangeSetRefs);
		return new ArrayList<ObjRef>(Arrays.asList(unresolvedOrderedChangeSetRefs));
	}

	public static List<ObjRef> unresolveRelationshipRefs(XArchFlatQueryInterface xarch, ObjRef xArchRef,
	        List<ObjRef> orderedChangeSetRefs) {
		ObjRef[] resolvedOrderedChangeSetRefs = orderedChangeSetRefs.toArray(new ObjRef[orderedChangeSetRefs.size()]);
		ObjRef[] unresolvedOrderedChangeSetRefs = ChangeSetUtils.unresolveExternalRelationshipRefs(xarch, xArchRef,
		        resolvedOrderedChangeSetRefs);
		return new ArrayList<ObjRef>(Arrays.asList(unresolvedOrderedChangeSetRefs));
	}

	public static void move(XArchChangeSetInterface xarch, ObjRef xArchRef, ObjRef[] moveChangeSetRefs, int newIndex,
	        IChangeSetSyncMonitor monitor) {
		ObjRef changeSetContextRef = xarch.createContext(xArchRef, "changesets");
		ObjRef archChangeSetsRef = xarch.getElement(changeSetContextRef, "archChangeSets", xArchRef);
		if (archChangeSetsRef != null) {
			List<ObjRef> appliedList = loadOrderObjRefs(xarch, xArchRef, archChangeSetsRef, "appliedChangeSets");
			List<ObjRef> orderedList = getOrderedChangeSets(xarch, xArchRef, changeSetContextRef, archChangeSetsRef,
			        appliedList);
			if (newIndex < 0) {
				newIndex = orderedList.size() + newIndex + 1;
			}

			Set<ObjRef> movedChangeSetRefsSet = new HashSet<ObjRef>(Arrays.asList(moveChangeSetRefs));
			List<ObjRef> movedOrderedList = new ArrayList<ObjRef>(orderedList);
			movedOrderedList.retainAll(movedChangeSetRefsSet);
			{
				List<ObjRef> subList = orderedList.subList(0, newIndex);
				subList.removeAll(movedChangeSetRefsSet);
				newIndex = subList.size();
			}
			{
				List<ObjRef> subList = orderedList.subList(newIndex, orderedList.size());
				subList.removeAll(movedChangeSetRefsSet);
			}
			orderedList.addAll(newIndex, movedOrderedList);

			saveOrderObjRefs(xarch, xArchRef, archChangeSetsRef, "changeSetOrder", orderedList);
			orderedList.retainAll(new HashSet<ObjRef>(appliedList));
			// saveOrder(xarch, xArchRef, archChangeSetsRef, "appliedChangeSets", orderedList);
			xarch.setAppliedChangeSetRefs(xArchRef, orderedList.toArray(new ObjRef[orderedList.size()]), monitor);
		}
	}

	private static Set<ObjRef> resolveChangeSetLinks(XArchFlatQueryInterface xarch, ObjRef parent, String linkNames) {
		ObjRef[] changeSetLinkRefs = xarch.getAll(parent, linkNames);
		List<ObjRef> resolvedChangeSetRefs = new ArrayList<ObjRef>();
		for (ObjRef changeSetLinkRef : changeSetLinkRefs) {
			ObjRef changeSetRef = ChangeSetUtils.resolveExternalChangeSetRef(xarch, XadlUtils.resolveXLink(xarch,
			        changeSetLinkRef));
			if (changeSetRef != null) {
				resolvedChangeSetRefs.add(changeSetRef);
			}
		}
		return new HashSet<ObjRef>(resolvedChangeSetRefs);
	}

	public static Set<ObjRef> autoSelect(XArchChangeSetInterface xarch, ObjRef xArchRef,
	        Set<ObjRef> includeChangeSetRefs, Set<ObjRef> excludeChangeSetRefs) {
		Set<Set<ObjRef>> alreadyVisited = new HashSet<Set<ObjRef>>();
		Set<ObjRef> appliedSet = new HashSet<ObjRef>(resolve(xarch, new ArrayList<ObjRef>(includeChangeSetRefs)));
		ObjRef changeSetContextRef = xarch.createContext(xArchRef, "changesets");
		ObjRef archChangeSetsRef = xarch.getElement(changeSetContextRef, "archChangeSets", xArchRef);
		if (archChangeSetsRef != null) {
			while (true) {
				Set<ObjRef> updatedAppliedSet = new HashSet<ObjRef>(appliedSet);
				for (ObjRef relationshipRef : ChangeSetUtils.resolveExternalRelationshipRefs(xarch, xarch.getAll(
				        archChangeSetsRef, "relationship"))) {
					if (xarch.isInstanceOf(relationshipRef, "changesets#OrRelationship")) {
						if (XArchChangeSetUtils.containsAny(updatedAppliedSet, resolveChangeSetLinks(xarch,
						        relationshipRef, "orChangeSet"))
						        || !updatedAppliedSet.containsAll(resolveChangeSetLinks(xarch, relationshipRef,
						                "orNotChangeSet"))) {
							updatedAppliedSet.addAll(resolveChangeSetLinks(xarch, relationshipRef, "impliesChangeSet"));
							updatedAppliedSet.removeAll(resolveChangeSetLinks(xarch, relationshipRef,
							        "impliesNotChangeSet"));
						}
					}
					else if (xarch.isInstanceOf(relationshipRef, "changesets#AndRelationship")) {
						if (updatedAppliedSet
						        .containsAll(resolveChangeSetLinks(xarch, relationshipRef, "andChangeSet"))
						        && !containsAny(updatedAppliedSet, resolveChangeSetLinks(xarch, relationshipRef,
						                "andNotChangeSet"))) {
							updatedAppliedSet.addAll(resolveChangeSetLinks(xarch, relationshipRef, "impliesChangeSet"));
							updatedAppliedSet.removeAll(resolveChangeSetLinks(xarch, relationshipRef,
							        "impliesNotChangeSet"));
						}
					}
					//else if (xarch.isInstanceOf(relationshipRef, "changesets#AndRelationship")) {
					//	Set<ObjRef> variantRefs = resolveChangeSetLinks(xarch, relationshipRef, "andChangeSet");
					//	variantRefs.retainAll(updatedAppliedSet);
					//	int count = variantRefs.size();
					//	int atLeast = parseInt((String) xarch.get(relationshipRef, "atLeast"), 0);
					//	int atMost = parseInt((String) xarch.get(relationshipRef, "atMost"), 1);
					//	if (count < atLeast || count > atMost)
					//		break;
					//}
				}

				updatedAppliedSet.addAll(includeChangeSetRefs);
				updatedAppliedSet.removeAll(excludeChangeSetRefs);
				if (!alreadyVisited.add(updatedAppliedSet)) {
					break;
				}

				if (appliedSet.equals(updatedAppliedSet)) {
					break;
				}
				appliedSet = updatedAppliedSet;
			}
		}
		return new HashSet<ObjRef>(unresolveChangeSetRefs(xarch, xArchRef, new ArrayList<ObjRef>(appliedSet)));
	}
}
