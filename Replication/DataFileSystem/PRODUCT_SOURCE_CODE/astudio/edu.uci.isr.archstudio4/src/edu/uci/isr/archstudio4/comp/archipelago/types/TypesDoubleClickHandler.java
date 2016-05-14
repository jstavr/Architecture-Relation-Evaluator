package edu.uci.isr.archstudio4.comp.archipelago.types;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoTreeDoubleClickHandler;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchPath;

public class TypesDoubleClickHandler implements IArchipelagoTreeDoubleClickHandler{

	protected ArchipelagoServices AS = null;
	
	public TypesDoubleClickHandler(ArchipelagoServices archipelagoServices){
		this.AS = archipelagoServices;
	}
	
	protected boolean isTargetNode(Object ref){
		if(ref instanceof ObjRef){
			XArchPath refPath = AS.xarch.getXArchPath((ObjRef)ref);
			String refPathString = refPath.toTagsOnlyString();
			if(refPathString.equals("xArch/archTypes/componentType")){
				return true;
			}
			else if(refPathString.equals("xArch/archTypes/connectorType")){
				return true;
			}
			else if(refPathString.equals("xArch/archTypes/interfaceType")){
				return true;
			}
		}
		return false;
	}
	
	public void treeNodeDoubleClicked(Object treeNode){
		if(isTargetNode(treeNode)){		
			ObjRef typeRef = (ObjRef)treeNode;
			TypesEditorSupport.setupEditor(AS, typeRef);
		}
	}
	
}
