package edu.uci.isr.archstudio4.comp.xarchcs.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.XArchCSActivator;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.xarchflat.ObjRef;

public class AddVariantRelationshipAction
    extends Action
    implements IHasXArchRef{

	XArchChangeSetInterface xarch;

	ObjRef xArchRef = null;

	public AddVariantRelationshipAction(XArchChangeSetInterface xarch){
		super("Add Variant Relationship", XArchCSActivator.getDefault().getImageRegistry().getDescriptor("res/icons/add_variant_relationship_action.gif"));
		this.xarch = xarch;
		setToolTipText(getText());
		setXArchRef(null);
	}

	@Override
	public void run(){
		try{
			if(xArchRef != null){
				ChangeSetUtils.createRelationship(xarch, xArchRef, "Variant", false, true);
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