package edu.uci.isr.archstudio4.comp.xarchcs.views.changesets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTListener;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetListener;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetUtils;
import edu.uci.isr.sysutils.ListenerList;
import edu.uci.isr.xarchflat.ObjRef;

class ChangeSetState implements IChangeSetState, XArchChangeSetListener, ExplicitADTListener {

	private static final boolean equalz(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	private static final <T> List<T> nullListAsList(List<T> l) {
		if (l == null) {
			return new ArrayList<T>();
		}
		return l;
	}

	private static final <T> List<T> nullArrayAsList(T... a) {
		if (a == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(a);
	}

	protected final XArchChangeSetInterface xarch;
	protected final IExplicitADT explicit;
	private final ListenerList<IChangeSetStateListener> changeSetStateListeners = new ListenerList<IChangeSetStateListener>(IChangeSetStateListener.class);

	protected final List<ObjRef> orderedChangeSetRefs = new ArrayList<ObjRef>();
	protected final Set<ObjRef> explicitChangeSetRefs = new HashSet<ObjRef>();
	protected final Set<ObjRef> appliedChangeSetRefs = new HashSet<ObjRef>();
	protected final Map<ObjRef, EChangeSetState> changeSetRefsToState = new HashMap<ObjRef, EChangeSetState>();

	protected ObjRef xArchRef = null;

	public ChangeSetState(XArchChangeSetInterface xarch, IExplicitADT explicit) {
		this.xarch = xarch;
		this.explicit = explicit;
	}

	public EChangeSetState getChangeSetState(ObjRef changeSetRef) {
		if (xArchRef == null || !ChangeSetState.equalz(xArchRef, xarch.getXArch(changeSetRef))) {
			return EChangeSetState.UNAPPLIED;
		}

		if (appliedChangeSetRefs.contains(changeSetRef)) {
			if (EChangeSetState.IMPLIED.equals(changeSetRefsToState.get(changeSetRef))) {
				return EChangeSetState.IMPLIED;
			}
			return EChangeSetState.APPLIED;
		}
		else {
			if (EChangeSetState.EXCLUDED.equals(changeSetRefsToState.get(changeSetRef))) {
				return EChangeSetState.EXCLUDED;
			}
			return EChangeSetState.UNAPPLIED;
		}
	}

	public void setChangeSetState(ObjRef changeSetRef, EChangeSetState newChangeSetState) {
		if (xArchRef == null || !ChangeSetState.equalz(xArchRef, xarch.getXArch(changeSetRef))) {
			return;
		}

		changeSetRefsToState.put(changeSetRef, newChangeSetState);

		Set<ObjRef> newApplied = new HashSet<ObjRef>();
		Set<ObjRef> newExcluded = new HashSet<ObjRef>();
		for (Iterator<Map.Entry<ObjRef, EChangeSetState>> i = changeSetRefsToState.entrySet().iterator(); i.hasNext();) {
			Map.Entry<ObjRef, EChangeSetState> entry = i.next();
			switch (entry.getValue()) {
			case APPLIED:
				newApplied.add(entry.getKey());
				break;
			case UNAPPLIED:
				newApplied.remove(entry.getKey());
				break;
			case IMPLIED:
				i.remove();
				break;
			case EXCLUDED:
				newExcluded.add(entry.getKey());
				newApplied.remove(entry.getKey());
				break;
			}
		}
		Set<ObjRef> newImplied = XArchChangeSetUtils.autoSelect(xarch, xArchRef, newApplied, newExcluded);
		newImplied.removeAll(newApplied);
		Set<ObjRef> newAppliedChangeSetRefs = new HashSet<ObjRef>(newApplied);
		newAppliedChangeSetRefs.addAll(newImplied);

		changeSetRefsToState.clear();
		for (ObjRef newChangeSetRef : newApplied) {
			changeSetRefsToState.put(newChangeSetRef, EChangeSetState.APPLIED);
		}
		for (ObjRef newChangeSetRef : newImplied) {
			changeSetRefsToState.put(newChangeSetRef, EChangeSetState.IMPLIED);
		}
		for (ObjRef newChangeSetRef : newExcluded) {
			changeSetRefsToState.put(newChangeSetRef, EChangeSetState.EXCLUDED);
		}

		if (!newAppliedChangeSetRefs.equals(appliedChangeSetRefs)) {
			appliedChangeSetRefs.clear();
			appliedChangeSetRefs.addAll(newAppliedChangeSetRefs);
			List<ObjRef> orderedAppliedChangeSets = new ArrayList<ObjRef>(orderedChangeSetRefs);
			orderedAppliedChangeSets.retainAll(appliedChangeSetRefs);
			XArchChangeSetUtils.saveOrderedObjRefs(xarch, xArchRef, "initialChangeSets", new ArrayList<ObjRef>(newApplied));
			XArchChangeSetUtils.saveOrderedObjRefs(xarch, xArchRef, "excludeChangeSets", new ArrayList<ObjRef>(newExcluded));
			xarch.setAppliedChangeSetRefs(xArchRef, orderedAppliedChangeSets.toArray(new ObjRef[orderedAppliedChangeSets.size()]), null);
		}

		fireChangeSetStateEvent();
	}

	public boolean isExplicit(ObjRef changeSetRef) {
		if (xArchRef == null || !ChangeSetState.equalz(xArchRef, xarch.getXArch(changeSetRef))) {
			return false;
		}

		return explicitChangeSetRefs.contains(changeSetRef); // && (appliedChangeSetRefs.contains(changeSetRef) || EChangeSetState.IMPLIED == changeSetRefsToState.get(changeSetRef));
	}

	public void setExplicit(ObjRef changeSetRef, boolean newExplicit) {
		if (xArchRef == null || !ChangeSetState.equalz(xArchRef, xarch.getXArch(changeSetRef))) {
			return;
		}

		boolean modified;
		if (newExplicit) {
			modified = explicitChangeSetRefs.add(changeSetRef);
		}
		else {
			modified = explicitChangeSetRefs.remove(changeSetRef);
		}

		if (modified) {
			List<ObjRef> orderedExplicitChangeSets = new ArrayList<ObjRef>(orderedChangeSetRefs);
			orderedExplicitChangeSets.retainAll(explicitChangeSetRefs);
			explicit.setExplicitChangeSetRefs(xArchRef, orderedExplicitChangeSets.toArray(new ObjRef[orderedExplicitChangeSets.size()]));
		}
	}

	public ObjRef getXArchRef() {
		return xArchRef;
	}

	public void setXArchRef(ObjRef xArchRef) {
		if (!ChangeSetState.equalz(this.xArchRef, xArchRef)) {
			this.xArchRef = xArchRef;

			orderedChangeSetRefs.clear();
			appliedChangeSetRefs.clear();
			explicitChangeSetRefs.clear();
			changeSetRefsToState.clear();

			if (xArchRef != null) {
				orderedChangeSetRefs.addAll(ChangeSetState.nullListAsList(XArchChangeSetUtils.getOrderedChangeSets(xarch, xArchRef, "appliedChangeSets", false)));
				appliedChangeSetRefs.addAll(ChangeSetState.nullArrayAsList(xarch.getAppliedChangeSetRefs(xArchRef)));
				explicitChangeSetRefs.addAll(ChangeSetState.nullArrayAsList(explicit.getExplicitChangeSetRefs(xArchRef)));
				List<ObjRef> initialChangeSetRefs = XArchChangeSetUtils.getOrderedChangeSets(xarch, xArchRef, "initialChangeSets", true);
				if (initialChangeSetRefs == null || initialChangeSetRefs.size() == 0) {
					for (ObjRef changeSetRef : appliedChangeSetRefs) {
						changeSetRefsToState.put(changeSetRef, EChangeSetState.APPLIED);
					}
				}
				else {
					for (ObjRef changeSetRef : appliedChangeSetRefs) {
						changeSetRefsToState.put(changeSetRef, EChangeSetState.IMPLIED);
					}
					for (ObjRef initialChangeSetRef : initialChangeSetRefs) {
						changeSetRefsToState.put(initialChangeSetRef, EChangeSetState.APPLIED);
					}
					for (ObjRef excludeChangeSetRef : ChangeSetState.nullListAsList(XArchChangeSetUtils.getOrderedChangeSets(xarch, xArchRef,
					        "excludeChangeSets", true))) {
						changeSetRefsToState.put(excludeChangeSetRef, EChangeSetState.EXCLUDED);
					}
				}
			}
		}
	}

	public void handleXArchChangeSetEvent(XArchChangeSetEvent evt) {
		if (xArchRef == null || !ChangeSetState.equalz(xArchRef, evt.getXArchRef())) {
			return;
		}

		switch (evt.getEventType()) {
		case UPDATED_APPLIED_CHANGE_SETS:
		case UPDATED_ENABLED:
			appliedChangeSetRefs.clear();
			appliedChangeSetRefs.addAll(ChangeSetState.nullArrayAsList(evt.getAppliedChangeSets()));
			orderedChangeSetRefs.clear();
			orderedChangeSetRefs.addAll(ChangeSetState.nullListAsList(XArchChangeSetUtils.getOrderedChangeSets(xarch, xArchRef, "appliedChangeSets", false)));
		}
	}

	public void handleExplicitEvent(ExplicitADTEvent evt) {
		if (xArchRef == null || !ChangeSetState.equalz(xArchRef, evt.getXArchRef())) {
			return;
		}

		switch (evt.getEventType()) {
		case UPDATED_EXPLICIT_CHANGE_SETS:
			explicitChangeSetRefs.clear();
			explicitChangeSetRefs.addAll(ChangeSetState.nullArrayAsList(evt.getChangeSetRefs()));
		}
	}

	public void addChangeSetStateListener(IChangeSetStateListener listener) {
		changeSetStateListeners.add(listener);
	}

	public void removeChangeSetStateListener(IChangeSetStateListener listener) {
		changeSetStateListeners.remove(listener);
	}

	protected void fireChangeSetStateEvent() {
		ChangeSetStateEvent event = new ChangeSetStateEvent();
		for (IChangeSetStateListener listener : changeSetStateListeners) {
			listener.handleChangeSetStateEvent(event);
		}
	}
}