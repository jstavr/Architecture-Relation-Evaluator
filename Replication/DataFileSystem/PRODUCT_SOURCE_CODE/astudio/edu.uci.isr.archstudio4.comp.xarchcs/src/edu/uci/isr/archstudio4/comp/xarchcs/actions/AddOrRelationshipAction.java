package edu.uci.isr.archstudio4.comp.xarchcs.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.XArchCSActivator;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.xarchflat.ObjRef;

public class AddOrRelationshipAction
    extends Action
    implements IHasXArchRef{

	XArchChangeSetInterface xarch;

	ObjRef xArchRef = null;

	public AddOrRelationshipAction(XArchChangeSetInterface xarch){
		super("Add Or Relationship", XArchCSActivator.getDefault().getImageRegistry().getDescriptor("res/icons/add_or_relationship_action.gif"));
		this.xarch = xarch;
		setToolTipText(getText());
		setXArchRef(null);
	}

	@Override
	public void run(){
		try{
			if(xArchRef != null){
				ChangeSetUtils.createRelationship(xarch, xArchRef, "Or", false, true);
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