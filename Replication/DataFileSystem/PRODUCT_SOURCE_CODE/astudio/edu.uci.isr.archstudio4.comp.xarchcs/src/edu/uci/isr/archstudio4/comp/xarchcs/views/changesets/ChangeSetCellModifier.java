package edu.uci.isr.archstudio4.comp.xarchcs.views.changesets;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Item;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.views.changesets.IChangeSetState.EChangeSetState;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.xarchflat.NoSuchObjectException;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatUtils;

public class ChangeSetCellModifier implements ICellModifier {

	protected final TreeViewer viewer;

	protected final XArchChangeSetInterface xarch;

	protected final IChangeSetState changeSetState;

	public ChangeSetCellModifier(TreeViewer viewer, XArchChangeSetInterface xarch, IChangeSetState changeSetState) {
		this.viewer = viewer;
		this.xarch = xarch;
		this.changeSetState = changeSetState;
	}

	public boolean canModify(Object element, String property) {
		if ("Apply".equals(property)) {
			return true;
		}
		if ("View".equals(property)) {
			return true;
		}
		if ("Change Set".equals(property)) {
			return true;
		}
		return false;
	}

	public Object getValue(Object element, String property) {
		try {
			if ("Apply".equals(property)) {
				//ObjRef changeSetRef = (ObjRef) element;
				//return changeSetState.getChangeSetState(changeSetRef);
				return true;
			}
			if ("View".equals(property)) {
				//ObjRef changeSetRef = (ObjRef) element;
				//return changeSetState.isExplicit(changeSetRef);
				return true;
			}
			if ("Change Set".equals(property)) {
				return XArchFlatUtils.getDescriptionValue(xarch, ChangeSetUtils.resolveExternalChangeSetRef(xarch, (ObjRef) element), "");
			}

		}
		catch (NoSuchObjectException e) {
		}
		return null;
	}

	public void modify(Object element, String property, Object value) {
		if (element instanceof Item) {
			element = ((Item) element).getData();
		}

		try {
			if ("Apply".equals(property)) {
				ObjRef changeSetRef = (ObjRef) element;
				EChangeSetState newValue = EChangeSetState.UNAPPLIED;
				switch (changeSetState.getChangeSetState(changeSetRef)) {
				case UNAPPLIED:
					newValue = EChangeSetState.APPLIED;
					break;
				case IMPLIED:
					newValue = EChangeSetState.EXCLUDED;
					break;
				case APPLIED:
					newValue = EChangeSetState.UNAPPLIED;
					break;
				case EXCLUDED:
					newValue = EChangeSetState.APPLIED;
				}
				changeSetState.setChangeSetState(changeSetRef, newValue);
			}
			if ("View".equals(property)) {
				ObjRef changeSetRef = (ObjRef) element;
				changeSetState.setExplicit(changeSetRef, !changeSetState.isExplicit(changeSetRef));
			}
			if ("Change Set".equals(property)) {
				XArchFlatUtils.setDescription(xarch, ChangeSetUtils.resolveExternalChangeSetRef(xarch, (ObjRef) element), "description", (String) value);
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
