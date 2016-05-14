package edu.uci.isr.archstudio4.rationale.actions;



import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

import edu.uci.isr.archstudio4.Archstudio4Activator;
import edu.uci.isr.archstudio4.comp.xarchcs.actions.IHasXArchRef;
import edu.uci.isr.archstudio4.rationale.RationaleViewManager;
import edu.uci.isr.widgets.swt.CustomizedMessageDialog;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class DeleteRationaleAction extends Action implements IHasXArchRef{

	XArchFlatInterface xArch;

	ObjRef xArchRef = null;

	TableViewer rationaleTableViewer;
	RationaleViewManager rationaleViewManager;


	public DeleteRationaleAction(XArchFlatInterface xArch,TableViewer rationaleTableViewer,RationaleViewManager rationaleViewManager) {
		super("Delete Rationale", Archstudio4Activator.getImageDescriptor("res/icons/delete_rationale.gif"));

		this.xArch = xArch;
		this.rationaleTableViewer = rationaleTableViewer;
		this.rationaleViewManager = rationaleViewManager;
	}

	@Override
	public synchronized void run() {
		ObjRef rationaleContextRef = xArch.createContext(xArchRef, "rationale");
		ObjRef rootRationaleRef = null;

		List<ObjRef>refs = rationaleViewManager.getCurrentSelectedRefs();

		if(refs.size() > 0) {

			List<ObjRef> selectedRationaleRefs = rationaleViewManager.getSelectedRationales();
			
			ObjRef ref = refs.get(0);
			ObjRef[] ancestors = xArch.getAllAncestors(ref);
			if(xArch.isInstanceOf(ancestors[ancestors.length - 2], "changesets#ArchChangeSets")) {
				ObjRef changeSetsContextRef = xArch.createContext(xArchRef, "changesets");
				ObjRef archChangeSetsElementRef = xArch.getElement(changeSetsContextRef,"ArchChangeSets", xArchRef);
				rootRationaleRef = (ObjRef)xArch.get(archChangeSetsElementRef, "ArchRationale");
			}
			else {
				rootRationaleRef = xArch.getElement(rationaleContextRef, "ArchRationale", xArchRef);
			}

			if(selectedRationaleRefs.size() > 0) {
				for(ObjRef selectedRationaleRef : selectedRationaleRefs) {
					if(!rationaleViewManager.getRationales(false).contains(selectedRationaleRef)) {
						return;
					}
					if(xArch.isInstanceOf(selectedRationaleRef, "changesets#RelationshipRationale")) {
						ObjRef[] refAncestors = xArch.getAllAncestors(selectedRationaleRef);
						if(refAncestors != null && refAncestors.length > 0) {
							String generated = (String)xArch.get(refAncestors[1],"generated");
							if(!"true".equals(generated)) {
								xArch.remove(rootRationaleRef, "Rationale", selectedRationaleRef);
							}
							else {
								String[] labels = {"Ok"};
								String message = "You cannot delete this rationale, as it belongs to a relationship that has been auto-generated.";
								CustomizedMessageDialog dialog = new CustomizedMessageDialog(rationaleTableViewer.getControl().getShell(), "Error",null,message,MessageDialog.ERROR,labels,0);
								dialog.openDialog();
							}
						}
						continue;
					}
					ObjRef[] xmlLinkRefs = xArch.getAll(selectedRationaleRef, "item");
					List<ObjRef> xmlLinkRefsInSelectedRationales = new ArrayList<ObjRef>();
					for(ObjRef xmlLinkRef : xmlLinkRefs) {
						String href = (String)xArch.get(xmlLinkRef,"Href");
						if(href != null && !"".equals(href.trim())) {
							String xArchID = href.replaceFirst("#", "");
							ObjRef idRef = xArch.getByID(xArchRef, xArchID);
							if(rationaleViewManager.getCurrentSelectedRefs().contains(idRef)) {
								xmlLinkRefsInSelectedRationales.add(xmlLinkRef);
							}
						}
					}
					if(xmlLinkRefs.length != xmlLinkRefsInSelectedRationales.size()) {
						String description = (String)xArch.get(((ObjRef)xArch.get(selectedRationaleRef,"Description")),"value");
						String message = "The rationale \"" + description +"\" is linked to components outside the current selection. Please select your preference.";
						String[] labels = {"Remove Rationale Completely","Remove Rationale From Current Selection","Cancel Operation"};
						CustomizedMessageDialog dialog = new CustomizedMessageDialog(rationaleTableViewer.getControl().getShell(), "Confirmation",null,message,MessageDialog.QUESTION,labels,0);
						int buttonLabelIndexSelected = dialog.openDialog();

						switch(buttonLabelIndexSelected) {
						case 0:
							xArch.remove(rootRationaleRef, "Rationale", selectedRationaleRef);
							break;
						case 1:
							for(ObjRef xmlLinkRef : xmlLinkRefsInSelectedRationales) {
								xArch.remove(selectedRationaleRef,"item",xmlLinkRef);
							}
							break;
						case 2:
							return;
						}
					}
					else {
						xArch.remove(rootRationaleRef, "Rationale", selectedRationaleRef);
					}
				}
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
		}
	}
	public void setXArchRef(ObjRef xArchRef) {
		this.xArchRef = xArchRef;
	}

	public ObjRef getXArchRef() {
		return this.getXArchRef();
	}
}
