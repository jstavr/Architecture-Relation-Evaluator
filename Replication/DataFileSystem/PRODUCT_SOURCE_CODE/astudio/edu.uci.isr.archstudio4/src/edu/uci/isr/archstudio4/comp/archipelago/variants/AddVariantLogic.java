package edu.uci.isr.archstudio4.comp.archipelago.variants;

import org.eclipse.jface.resource.ImageDescriptor;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.types.TypesMapper;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractXadlSelectorLogic;
import edu.uci.isr.archstudio4.util.ArchstudioResources;
import edu.uci.isr.archstudio4.util.XadlTreeUtils;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class AddVariantLogic extends AbstractXadlSelectorLogic{

	public AddVariantLogic(ArchipelagoServices services, ObjRef xArchRef){
		super(services, xArchRef);
	}
	
	public boolean matches(IBNAView view, IThing t){
		if(t instanceof BoxGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				if(TypesMapper.isBrickTypeAssemblyRootThing(pt)){
					String xArchID = pt.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
					if(xArchID != null){
						ObjRef eltRef = AS.xarch.getByID(xArchRef, xArchID);
						if(eltRef != null){
							if(AS.xarch.isInstanceOf(eltRef, "variants#VariantComponentType")){
								return true;
							}
							else if(AS.xarch.isInstanceOf(eltRef, "variants#VariantConnectorType")){
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	
	public String getXArchID(IBNAView view, IThing t){
		if(t instanceof BoxGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				if(TypesMapper.isBrickTypeAssemblyRootThing(pt)){
					String xArchID = pt.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
					return xArchID;
				}
			}
		}
		return null;
	}
	
	public Object getInitialValue(ObjRef eltRef){
		return null;
	}
	
	public ImageDescriptor getMenuItemIcon(ObjRef eltRef){
		if(AS.xarch.isInstanceOf(eltRef, "variants#VariantComponentType")){
			return AS.resources.getImageDescriptor(ArchstudioResources.ICON_COMPONENT_TYPE);
		}
		else if(AS.xarch.isInstanceOf(eltRef, "variants#VariantConnectorType")){
			return AS.resources.getImageDescriptor(ArchstudioResources.ICON_CONNECTOR_TYPE);
		}
		return null;
	}
	
	public String getMenuItemString(){
		return "Add a Variant...";
	}
	
	public int getFlags(ObjRef eltRef){
		int flags = 0;
		if(AS.xarch.isInstanceOf(eltRef, "types#ComponentType")){
			return flags | XadlTreeUtils.COMPONENT_TYPE;
		}
		else if(AS.xarch.isInstanceOf(eltRef, "types#ConnectorType")){
			return flags | XadlTreeUtils.CONNECTOR_TYPE;
		}
		return flags;
	}

	public ObjRef getRootRef(ObjRef eltRef){
		return XadlUtils.getArchTypes(AS.xarch, xArchRef);
	}
	
	public void setValue(ObjRef eltRef, Object newValue){
		if(newValue instanceof ObjRef){
			String targetID = XadlUtils.getID(AS.xarch, (ObjRef)newValue);
			if(targetID != null){
				ObjRef variantsContextRef = AS.xarch.createContext(xArchRef, "variants");
				ObjRef variantRef = AS.xarch.create(variantsContextRef, "variant");
				
				XadlUtils.setXLink(AS.xarch, variantRef, "variantType", targetID);
				AS.xarch.add(eltRef, "variant", variantRef);
			}
		}
	}
	
}
