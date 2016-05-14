package edu.uci.isr.archstudio4.comp.xarchcs.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.XArchCSActivator;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatUtils;

public class AddChangeSetAction
    extends Action
    implements IHasXArchRef{

	private static int count = 0;

	XArchChangeSetInterface xarch;

	ObjRef xArchRef = null;

	public AddChangeSetAction(XArchChangeSetInterface xarch){
		super("Add Change Set", XArchCSActivator.getDefault().getImageRegistry().getDescriptor("res/icons/add_changeset_action.gif"));
		this.xarch = xarch;
		setToolTipText(getText());
		setXArchRef(null);
	}

	@Override
	public void run(){
		try{
			if(xArchRef != null){
				ObjRef changeSetRef = ChangeSetUtils.createChangeSet(xarch, xArchRef, false, true);
				if(changeSetRef != null){
					ObjRef changeSetRefR = ChangeSetUtils.resolveExternalChangeSetRef(xarch, changeSetRef);
					XArchFlatUtils.setDescription(xarch, changeSetRefR, "description", "[New Change Set " + ++count + "]");

					List<ObjRef> appliedChangeSets = new ArrayList<ObjRef>(Arrays.asList(xarch.getAppliedChangeSetRefs(xArchRef)));
					appliedChangeSets.add(changeSetRef);
					xarch.setAppliedChangeSetRefs(xArchRef, appliedChangeSets.toArray(new ObjRef[appliedChangeSets.size()]), null);
					xarch.setActiveChangeSetRef(xArchRef, changeSetRef);
				}
			}
		}
		catch(Throwable t){
			t.printStackTrace();
			MessageDialog.openError(null, "Error", "Unable to create relationship: " + t.getMessage());
		}
	}

	public ObjRef getXArchRef(){
		return xArchRef;
	}

	public void setXArchRef(ObjRef xArchRef){
		this.xArchRef = xArchRef;
		setEnabled(xArchRef != null);
	}
}