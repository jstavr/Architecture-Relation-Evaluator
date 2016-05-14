package edu.uci.isr.archstudio4.comp.archipelago;

import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.util.XadlTreeUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class RootLabelProvider implements IArchipelagoLabelProvider{
	protected ArchipelagoServices AS = null;
	
	public RootLabelProvider(ArchipelagoServices services){
		this.AS = services;
	}
	
	public String getText(Object element, String textFromPreviousProvider){
		if(element instanceof ObjRef){
			ObjRef ref = (ObjRef)element;
			if(AS.xarch.isInstanceOf(ref, "#XArch")){
				return AS.xarch.getXArchURI(ref);
			}
		}
		return textFromPreviousProvider;
	}
	
	public Image getImage(Object element, Image imageFromPreviousProvider){
		if(element instanceof ObjRef){
			ObjRef ref = (ObjRef)element;
			if(AS.xarch.isInstanceOf(ref, "#XArch")){
				return XadlTreeUtils.getIconForType(AS.resources, XadlTreeUtils.DOCUMENT);
			}
		}
		return imageFromPreviousProvider;
	}
	
}
