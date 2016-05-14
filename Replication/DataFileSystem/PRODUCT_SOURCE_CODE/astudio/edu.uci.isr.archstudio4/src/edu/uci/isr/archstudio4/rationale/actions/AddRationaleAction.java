package edu.uci.isr.archstudio4.rationale.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

import edu.uci.isr.archstudio4.Archstudio4Activator;
import edu.uci.isr.archstudio4.comp.xarchcs.actions.IHasXArchRef;
import edu.uci.isr.archstudio4.rationale.RationaleViewManager;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.widgets.swt.CustomizedMessageDialog;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class AddRationaleAction
	extends Action
	implements IHasXArchRef{

	XArchFlatInterface xArch;

	ObjRef xArchRef = null;

	TableViewer rationaleTableViewer;
	RationaleViewManager rationaleViewManager;

	static int count = 1;

	public AddRationaleAction(XArchFlatInterface xArch, TableViewer rationaleTableViewer, RationaleViewManager rationaleViewManager){
		super("Add New Rationale", Archstudio4Activator.getImageDescriptor("res/icons/rationale.gif"));

		this.xArch = xArch;
		this.rationaleTableViewer = rationaleTableViewer;
		this.rationaleViewManager = rationaleViewManager;
	}

	@Override
	public synchronized void run(){

		List<ObjRef> currentSelectedRefs = rationaleViewManager.getCurrentSelectedRefs();
		ObjRef rationaleContextRef = xArch.createContext(xArchRef, "rationale");
		ObjRef rootRationaleRef = null;
		if(currentSelectedRefs.size() > 0){

			ObjRef firstRef = currentSelectedRefs.get(0);
			ObjRef[] ancestors = xArch.getAllAncestors(firstRef);
			if(xArch.isInstanceOf(ancestors[ancestors.length - 2], "changesets#ArchChangeSets")){
				ObjRef changeSetsContextRef = xArch.createContext(xArchRef, "changesets");
				ObjRef archChangeSetsElementRef = xArch.getElement(changeSetsContextRef, "ArchChangeSets", xArchRef);
				rootRationaleRef = (ObjRef)xArch.get(archChangeSetsElementRef, "ArchRationale");
			}
			else{
				rootRationaleRef = xArch.getElement(rationaleContextRef, "ArchRationale", xArchRef);
				if(rootRationaleRef == null) {
					rootRationaleRef = xArch.createElement(rationaleContextRef, "ArchRationale");
					xArch.add(xArchRef, "object", rootRationaleRef);
				}
			}

			for(ObjRef currentSelectedRef: currentSelectedRefs){
				if(xArch.isInstanceOf(currentSelectedRef, "changesets#Relationship")){
					String generated = (String)xArch.get(currentSelectedRef, "generated");
					if("true".equals(generated)){
						String[] labels = {"Ok"};
						String message = "You cannot manually add a rationale to a relationship that has been auto-generated.";
						CustomizedMessageDialog dialog = new CustomizedMessageDialog(rationaleTableViewer.getControl().getShell(), "Error", null, message, MessageDialog.ERROR, labels, 0);
						dialog.openDialog();
						return;
					}
				}
			}

			final ObjRef newRationaleRef = xArch.create(rationaleContextRef, "Rationale");
			ObjRef descriptionRef = xArch.create(rationaleContextRef, "Description");
			xArch.set(descriptionRef, "value", "[New Rationale " + count++ + "]");
			xArch.set(newRationaleRef, "Description", descriptionRef);
			String xArchID = UIDGenerator.generateUID();
			xArch.set(newRationaleRef, "id", xArchID);

			for(ObjRef currentSelectedRef: currentSelectedRefs){
				ObjRef itemRef = xArch.create(rationaleContextRef, "XMLLink");
				String href = "#" + xArch.get(currentSelectedRef, "id");
				xArch.set(itemRef, "Href", href);
				xArch.add(newRationaleRef, "item", itemRef);
			}

			xArch.add(rootRationaleRef, "Rationale", newRationaleRef);
			SWTWidgetUtils.async(rationaleTableViewer, new Runnable(){

				public void run(){
					rationaleTableViewer.refresh();
					rationaleTableViewer.setSelection(new StructuredSelection(newRationaleRef));
				}
			});
		}
	}

	public void setXArchRef(ObjRef xArchRef){
		this.xArchRef = xArchRef;
	}

	public ObjRef getXArchRef(){
		return this.getXArchRef();
	}
}
