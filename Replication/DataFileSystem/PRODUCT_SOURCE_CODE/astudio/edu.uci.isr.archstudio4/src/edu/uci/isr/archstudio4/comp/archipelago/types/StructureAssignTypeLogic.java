package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractXadlSelectorLogic;
import edu.uci.isr.archstudio4.util.ArchstudioResources;
import edu.uci.isr.archstudio4.util.XadlTreeUtils;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class StructureAssignTypeLogic extends AbstractXadlSelectorLogic{

	public StructureAssignTypeLogic(ArchipelagoServices services, ObjRef xArchRef){
		super(services, xArchRef);
	}
	
	public boolean matches(IBNAView view, IThing t){
		if(t instanceof BoxGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				return StructureMapper.isBrickAssemblyRootThing(pt);
			}
		}
		return false;
	}
	
	public String getXArchID(IBNAView view, IThing t){
		if(t instanceof BoxGlassThing){
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(t);
			return parentThing.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		}
		return null;
	}

	public Object getInitialValue(ObjRef eltRef){
		if(eltRef != null){
			ObjRef typeRef = XadlUtils.resolveXLink(AS.xarch, eltRef, "type");
			return typeRef;
		}
		return null;
	}
	
	public String getMenuItemString(){
		return "Assign Type...";
	}
	
	public ImageDescriptor getMenuItemIcon(ObjRef eltRef){
		if(AS.xarch.isInstanceOf(eltRef, "types#Component")){
			return AS.resources.getImageDescriptor(ArchstudioResources.ICON_COMPONENT_TYPE);
		}
		else if(AS.xarch.isInstanceOf(eltRef, "types#Connector")){
			return AS.resources.getImageDescriptor(ArchstudioResources.ICON_CONNECTOR_TYPE);
		}
		return null;
	}
	
	public ObjRef getRootRef(ObjRef eltRef){
		if(eltRef != null){
			if(true) return AS.xarch.getXArch(eltRef);
			ObjRef typeRef = XadlUtils.resolveXLink(AS.xarch, eltRef, "type");
			return typeRef;
		}
		return null;
	}
	
	public int getFlags(ObjRef eltRef){
		int flags = 0;
		if(AS.xarch.isInstanceOf(eltRef, "types#Component")){
			return flags | XadlTreeUtils.COMPONENT_TYPE;
		}
		else if(AS.xarch.isInstanceOf(eltRef, "types#Connector")){
			return flags | XadlTreeUtils.CONNECTOR_TYPE;
		}
		return flags | XadlTreeUtils.ANY_TYPE;
	}
	
	public void setValue(ObjRef eltRef, Object newValue){
		if(newValue instanceof ObjRef){
			ObjRef newRef = (ObjRef)newValue;
			if(AS.xarch.isInstanceOf(newRef, "types#ComponentType") ||
			AS.xarch.isInstanceOf(newRef, "types#ConnectorType")){
				String typeID = XadlUtils.getID(AS.xarch, newRef);
				if(typeID != null){
					XadlUtils.setXLink(AS.xarch, eltRef, "type", typeID);
				}
			}
		}
	}
	
	protected IAction[] getActions(IBNAView view, IThing t, int worldX, int worldY){
		IAction[] superActions = super.getActions(view, t, worldX, worldY);
		
		final ArchipelagoServices fAS = AS;
		final IBNAView fview = view;
		final IThing ft = t;
		final int fworldX = worldX;
		final int fworldY = worldY;
		
		final String eltXArchID = getXArchID(view, t);
		if(eltXArchID == null){
			return superActions;
		}
		
		final ObjRef eltRef = AS.xarch.getByID(xArchRef, eltXArchID);
		if(eltRef == null){
			//Nothing to set description on
			return superActions;
		}
		
		IAction clearTypeAction = new Action("Clear Type"){
			public void run(){
				fAS.xarch.clear(eltRef, "type");
				ArchipelagoUtils.showUserNotification(fview.getWorld().getBNAModel(), "Type Cleared", fworldX, fworldY);
			}
		};
		
		IAction[] retActions = new IAction[superActions.length + 1];
		System.arraycopy(superActions, 0, retActions, 0, superActions.length);
		retActions[retActions.length-1] = clearTypeAction;
		return retActions;
	}
}
