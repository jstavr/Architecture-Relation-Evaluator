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

public class TypesAssignStructureLogic extends AbstractXadlSelectorLogic{

	public TypesAssignStructureLogic(ArchipelagoServices services, ObjRef xArchRef){
		super(services, xArchRef);
	}
	
	public boolean matches(IBNAView view, IThing t){
		if(t instanceof BoxGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				return TypesMapper.isBrickTypeAssemblyRootThing(pt);
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
			ObjRef subArchitectureRef = (ObjRef)AS.xarch.get(eltRef, "subArchitecture");
			if(subArchitectureRef != null){
				ObjRef structureRef = XadlUtils.resolveXLink(AS.xarch, subArchitectureRef, "archStructure");
				return structureRef;
			}
		}
		return null;
	}
	
	public String getMenuItemString(){
		return "Assign Substructure...";
	}
	
	public ImageDescriptor getMenuItemIcon(ObjRef eltRef){
		return AS.resources.getImageDescriptor(ArchstudioResources.ICON_STRUCTURE);
	}
	
	public ObjRef getRootRef(ObjRef eltRef){
		return xArchRef;
	}
	
	public int getFlags(ObjRef eltRef){
		return XadlTreeUtils.STRUCTURE;
	}
	
	public void setValue(ObjRef eltRef, Object newValue){
		if(newValue instanceof ObjRef){
			ObjRef newRef = (ObjRef)newValue;
			
			if(AS.xarch.isInstanceOf(newRef, "types#ArchStructure")){
				String structureID = XadlUtils.getID(AS.xarch, newRef);
				if(structureID != null){
					boolean addSubArchitecture = false;
					ObjRef subArchitectureRef = (ObjRef)AS.xarch.get(eltRef, "subArchitecture");
					if(subArchitectureRef == null){
						ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "types");
						subArchitectureRef = AS.xarch.create(typesContextRef, "subArchitecture");
						addSubArchitecture = true;
					}
					XadlUtils.setXLink(AS.xarch, subArchitectureRef, "archStructure", structureID);
					if(addSubArchitecture){
						AS.xarch.set(eltRef, "subArchitecture", subArchitectureRef);
					}
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
			//Nothing to set
			return superActions;
		}
		
		IAction clearTypeAction = new Action("Clear Substructure"){
			public void run(){
				fAS.xarch.clear(eltRef, "subArchitecture");
				ArchipelagoUtils.showUserNotification(fview.getWorld().getBNAModel(), "Substructure Cleared", fworldX, fworldY);
			}
		};
		
		IAction[] retActions = new IAction[superActions.length + 1];
		System.arraycopy(superActions, 0, retActions, 0, superActions.length);
		retActions[retActions.length-1] = clearTypeAction;
		return retActions;
	}
}
