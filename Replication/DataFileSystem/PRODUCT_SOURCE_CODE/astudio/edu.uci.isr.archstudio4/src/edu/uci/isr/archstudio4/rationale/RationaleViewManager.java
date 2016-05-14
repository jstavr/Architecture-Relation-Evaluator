package edu.uci.isr.archstudio4.rationale;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class RationaleViewManager {

	List<ObjRef> currentSelectedRefs = new ArrayList<ObjRef>();
	List<ObjRef> selectedRationaleRefs = new ArrayList<ObjRef>();
	List<ObjRef> rationaleItems;
	XArchChangeSetInterface xarch;
	ObjRef xArchRef;
	TableViewer rationaleTableViewer;
	TableViewer associatedItemsViewer;
	IExplicitADT explicitADT;

	List<ObjRef> selectedAssociatedItemRefs = new ArrayList<ObjRef>();

	public RationaleViewManager(XArchChangeSetInterface xarch, TableViewer rationaleTableViewer, TableViewer associatedItemsViewer, IExplicitADT explicitADT) {
		this.rationaleTableViewer = rationaleTableViewer;
		this.associatedItemsViewer = associatedItemsViewer;
		this.xarch = xarch;
		this.explicitADT = explicitADT;
	}

	public List<ObjRef> getCurrentSelectedRefs() {
		return currentSelectedRefs;
	}

	public void setXArchRef(ObjRef xArchRef) {
		this.xArchRef = xArchRef;
	}

	public void loadRationales(List<ObjRef> selectedRefs) {
		currentSelectedRefs.clear();
		currentSelectedRefs.addAll(selectedRefs);
		if (!rationaleTableViewer.getControl().getDisplay().isDisposed()) {
			rationaleTableViewer.getControl().getDisplay().asyncExec(new Runnable() {

				public void run() {
					if (rationaleTableViewer == null)
						return;
					if (rationaleTableViewer.getControl().getDisplay().isDisposed())
						return;

					rationaleTableViewer.refresh();
				}
			});
		}
	}

	public void clearSelectedRationaleRefList() {
		selectedRationaleRefs.clear();
	}

	public void clearSelectedAssociatedItemRefList() {
		selectedAssociatedItemRefs.clear();
	}

	public List<ObjRef> getSelectedAssociatedItemRefList() {
		return this.selectedAssociatedItemRefs;
	}

	public void addSelectedAssociatedItemRefList(ObjRef ref) {
		if (!selectedAssociatedItemRefs.contains(ref)) {
			selectedAssociatedItemRefs.add(ref);
		}
	}

	public void addSelectedRationale(ObjRef rationaleRef) {
		if (!selectedRationaleRefs.contains(rationaleRef)) {
			selectedRationaleRefs.add(rationaleRef);
		}
	}

	public List<ObjRef> getSelectedRationales() {
		return this.selectedRationaleRefs;
	}

	public List<ObjRef> getRationales(boolean useExplicitXArch) {
		List<ObjRef> rationaleRefsForCurrentSelection = new ArrayList<ObjRef>();
		XArchFlatInterface xArch = null;
		if (useExplicitXArch) {
			xArch = xarch;
		}
		else {
			xArch = xarch;
		}

		if (xArchRef == null) {
			return rationaleRefsForCurrentSelection;
		}
		else {
			ObjRef rationaleContextRef = xArch.createContext(xArchRef, "rationale");
			ObjRef rationaleElementRef = xArch.getElement(rationaleContextRef, "ArchRationale", xArchRef);
			if (rationaleElementRef != null) {
				ObjRef[] rationaleRefs = xArch.getAll(rationaleElementRef, "Rationale");
				for (ObjRef rationaleRef : rationaleRefs) {
					ObjRef[] xmlLinkRefs = xArch.getAll(rationaleRef, "item");
					for (ObjRef xmlLinkRef : xmlLinkRefs) {
						String href = (String) xArch.get(xmlLinkRef, "Href");
						if (href != null && !"".equals(href.trim())) {
							String xArchID = href.replaceFirst("#", "");
							ObjRef ref = xArch.getByID(xArchRef, xArchID);
							if (currentSelectedRefs.contains(ref)) {
								rationaleRefsForCurrentSelection.add(rationaleRef);
								break;
							}
						}
					}
				}
			}
			ObjRef changeSetsContextRef = xArch.createContext(xArchRef, "changesets");
			ObjRef archChangeSetsElementRef = xArch.getElement(changeSetsContextRef, "ArchChangeSets", xArchRef);
			if (archChangeSetsElementRef != null) {
				if (xArch.isInstanceOf(archChangeSetsElementRef, "rationale#ArchChangeSetsRationale")) {
					ObjRef archRationaleRef = (ObjRef) xArch.get(archChangeSetsElementRef, "ArchRationale");
					ObjRef[] rationaleRefs = xArch.getAll(archRationaleRef, "Rationale");
					for (ObjRef rationaleRef : rationaleRefs) {
						ObjRef[] xmlLinkRefs = xArch.getAll(rationaleRef, "item");
						for (ObjRef xmlLinkRef : xmlLinkRefs) {
							String href = (String) xArch.get(xmlLinkRef, "Href");
							if (href != null && !"".equals(href.trim())) {
								String xArchID = href.replaceFirst("#", "");
								ObjRef ref = xArch.getByID(xArchRef, xArchID);
								if (currentSelectedRefs.contains(ref)) {
									rationaleRefsForCurrentSelection.add(rationaleRef);
									break;
								}
							}
						}
					}
				}
			}

			for (ObjRef selectedRef : currentSelectedRefs) {
				if (xArch.isInstanceOf(selectedRef, "changesets#Relationship")) {
					ObjRef[] relationshipRationales = xArch.getAll(selectedRef, "rationale");
					for (ObjRef relationshipRationale : relationshipRationales) {
						rationaleRefsForCurrentSelection.add(relationshipRationale);
					}
				}
			}
			return rationaleRefsForCurrentSelection;
		}
	}

	public void reloadRationales() {
		this.loadRationales(this.currentSelectedRefs);
	}

	public boolean areObjectsSelected() {
		if (currentSelectedRefs.size() > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public Object[] getAssociatedItemsForCurrentListOfRationales() {

		List<ObjRef> associatedItemRefs = new ArrayList<ObjRef>();
		associatedItemRefs.addAll(this.currentSelectedRefs);

		List<ObjRef> rationaleObjects = this.getRationales(true);

		for (Object rationaleObject : rationaleObjects) {
			if (rationaleObject instanceof ObjRef) {
				ObjRef rationaleRef = (ObjRef) rationaleObject;
				if (xarch.isInstanceOf(rationaleRef, "changesets#RelationshipRationale")) {

				}
				else {
					ObjRef[] itemRefArray = xarch.getAll(rationaleRef, "item");
					for (ObjRef itemRef : itemRefArray) {
						String href = (String) xarch.get(itemRef, "Href");
						if (href != null && !"".equals(href.trim())) {
							String xArchID = href.replaceFirst("#", "");
							ObjRef associatedItemRef = xarch.getByID(xArchID);
							if (!associatedItemRefs.contains(associatedItemRef)) {
								associatedItemRefs.add(associatedItemRef);
							}
						}
					}
				}
			}
		}

		if (this.currentSelectedRefs.size() > 0) {
			return associatedItemRefs.toArray(new ObjRef[associatedItemRefs.size()]);
		}
		else {
			return new Object[0];
		}
	}
}
