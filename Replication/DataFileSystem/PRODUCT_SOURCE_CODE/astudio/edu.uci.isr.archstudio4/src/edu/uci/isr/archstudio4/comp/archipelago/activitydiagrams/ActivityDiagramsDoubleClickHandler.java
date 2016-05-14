package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoTreeDoubleClickHandler;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchPath;

public class ActivityDiagramsDoubleClickHandler
	implements IArchipelagoTreeDoubleClickHandler{

	protected ArchipelagoServices AS = null;

	public ActivityDiagramsDoubleClickHandler(ArchipelagoServices archipelagoServices){
		this.AS = archipelagoServices;
	}

	protected boolean isTargetNode(Object ref){
		if(ref instanceof ObjRef){
			XArchPath refPath = AS.xarch.getXArchPath((ObjRef)ref);
			if(refPath.toTagsOnlyString().equals("xArch/activityDiagram")){
				return true;
			}
		}
		return false;
	}

	public void treeNodeDoubleClicked(Object treeNode){
		if(isTargetNode(treeNode)){
			ObjRef activitydiagramRef = (ObjRef)treeNode;
			ActivityDiagramsEditorSupport.setupEditor(AS, activitydiagramRef);
		}
	}

}
