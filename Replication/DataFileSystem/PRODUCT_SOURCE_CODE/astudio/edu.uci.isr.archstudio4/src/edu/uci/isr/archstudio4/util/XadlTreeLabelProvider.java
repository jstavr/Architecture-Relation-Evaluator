package edu.uci.isr.archstudio4.util;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class XadlTreeLabelProvider extends LabelProvider implements ILabelProvider{

	protected XArchFlatInterface xarch = null;
	protected IResources resources = null;
	
	public XadlTreeLabelProvider(XArchFlatInterface xarch, IResources resources){
		this.xarch = xarch;
		this.resources = resources;
		ArchstudioResources.init(resources);
	}
	
	public String getText(Object element){
		if(element instanceof ObjRef){
			ObjRef ref = (ObjRef)element;
			int type = XadlTreeUtils.getType(xarch, ref);
			if(type == XadlTreeUtils.UNKNOWN){
				return "[Unknown Element]";
			}
			else if(type == XadlTreeUtils.DOCUMENT){
				return xarch.getXArchURI(ref);
			}
			else if(type == XadlTreeUtils.TYPES){
				return "Types";
			}
			else{
				String description = XadlUtils.getDescription(xarch, ref);
				if(description == null){
					String id = XadlUtils.getID(xarch, ref);
					if(id != null){
						description = "[id=" + id + "]";
					}
					description = "[No Identifier]";
				}
				return description;
			}
		}
		return super.getText(element);
	}
	
	public Image getImage(Object element){
		if(element instanceof ObjRef){
			ObjRef ref = (ObjRef)element;
			int type = XadlTreeUtils.getType(xarch, ref);
			Image image = XadlTreeUtils.getIconForType(resources, type);
			if(image != null){
				return image;
			}
		}
		return super.getImage(element);
	}
	
}
