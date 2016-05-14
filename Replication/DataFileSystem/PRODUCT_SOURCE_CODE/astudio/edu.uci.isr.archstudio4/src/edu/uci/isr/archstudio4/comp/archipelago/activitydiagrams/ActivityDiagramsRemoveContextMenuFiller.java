package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams;

import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractRemoveContextMenuFiller;
import edu.uci.isr.xarchflat.ObjRef;

public class ActivityDiagramsRemoveContextMenuFiller
	extends AbstractRemoveContextMenuFiller{

	public ActivityDiagramsRemoveContextMenuFiller(TreeViewer viewer, ArchipelagoServices services, ObjRef xArchRef){
		super(viewer, services, xArchRef);
	}

	@Override
	protected boolean matches(Object node){
		if(node != null){
			if(node instanceof ObjRef){
				ObjRef targetRef = (ObjRef)node;
				if(AS.xarch.isInstanceOf(targetRef, "activitydiagrams#ActivityDiagram")){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void remove(ObjRef targetRef){
		if(targetRef != null){
			if(AS.xarch.isInstanceOf(targetRef, "activitydiagrams#ActivityDiagram")){
				AS.xarch.remove(xArchRef, "Object", targetRef);
				return;
			}
		}
		super.remove(targetRef);
	}
}
