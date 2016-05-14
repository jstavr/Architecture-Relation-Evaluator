package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoLabelProvider;
import edu.uci.isr.archstudio4.util.XadlTreeUtils;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesTreeLabelProvider implements IArchipelagoLabelProvider{
	protected ArchipelagoServices AS = null;
	
	public TypesTreeLabelProvider(ArchipelagoServices services){
		this.AS = services;
	}
	
	public String getText(Object element, String textFromPreviousProvider){
		if(element instanceof ObjRef){
			ObjRef ref = (ObjRef)element;
			if(AS.xarch.isInstanceOf(ref, "types#ArchTypes")){
				return "Types";
			}
			else if(AS.xarch.isInstanceOf(ref, "types#ComponentType") ||
				AS.xarch.isInstanceOf(ref, "types#ConnectorType") ||
				AS.xarch.isInstanceOf(ref, "types#InterfaceType")){
				String description = XadlUtils.getDescription(AS.xarch, ref);
				if(description == null){
					description = "[No Description]";
				}
				return description;
			}
		}
		return textFromPreviousProvider;
	}
	
	public Image getImage(Object element, Image imageFromPreviousProvider){
		if(element instanceof ObjRef){
			ObjRef ref = (ObjRef)element;
			if(AS.xarch.isInstanceOf(ref, "types#ArchTypes")){
				return AS.resources.getPlatformImage(ISharedImages.IMG_OBJ_FOLDER);
			}
			else if(AS.xarch.isInstanceOf(ref, "types#ComponentType")){
				return XadlTreeUtils.getIconForType(AS.resources, XadlTreeUtils.COMPONENT_TYPE);
			}
			else if(AS.xarch.isInstanceOf(ref, "types#ConnectorType")){
				return XadlTreeUtils.getIconForType(AS.resources, XadlTreeUtils.CONNECTOR_TYPE);
			}
			else if(AS.xarch.isInstanceOf(ref, "types#InterfaceType")){
				return XadlTreeUtils.getIconForType(AS.resources, XadlTreeUtils.INTERFACE_TYPE);
			}
		}
		return imageFromPreviousProvider;
	}
	
}
