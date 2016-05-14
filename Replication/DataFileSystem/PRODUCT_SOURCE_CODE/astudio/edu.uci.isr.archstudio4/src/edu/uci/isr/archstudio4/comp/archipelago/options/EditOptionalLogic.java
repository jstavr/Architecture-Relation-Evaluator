package edu.uci.isr.archstudio4.comp.archipelago.options;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.types.StructureMapper;
import edu.uci.isr.archstudio4.comp.archipelago.types.TypesMapper;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractEditGuardLogic;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.bna4.things.glass.EndpointGlassThing;
import edu.uci.isr.bna4.things.glass.MappingGlassThing;
import edu.uci.isr.bna4.things.glass.SplineGlassThing;
import edu.uci.isr.xarchflat.InvalidOperationException;
import edu.uci.isr.xarchflat.ObjRef;

public class EditOptionalLogic extends AbstractEditGuardLogic{
	public EditOptionalLogic(ArchipelagoServices services, ObjRef xArchRef){
		super(services, xArchRef);
	}
	
	public boolean matches(IBNAView view, IThing t){
		if(t instanceof BoxGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				return StructureMapper.isBrickAssemblyRootThing(pt);
			}
		}
		else if(t instanceof EndpointGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				return StructureMapper.isInterfaceAssemblyRootThing(pt) ||
					TypesMapper.isSignatureAssemblyRootThing(pt);
			}
		}
		else if(t instanceof SplineGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				return StructureMapper.isLinkAssemblyRootThing(pt);
			}
		}
		else if(t instanceof MappingGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				return TypesMapper.isSignatureInterfaceMappingAssemblyRootThing(pt);
			}
		}
		return false;
	}

	public String getXArchID(IBNAView view, IThing t){
		if(t instanceof BoxGlassThing){
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(t);
			return parentThing.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		}
		else if(t instanceof EndpointGlassThing){
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(t);
			return parentThing.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		}
		else if(t instanceof SplineGlassThing){
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(t);
			return parentThing.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		}
		else if(t instanceof MappingGlassThing){
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(t);
			return parentThing.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		}
		return null;
	}
	
	protected String getPromotedTypeName(ObjRef ref){
		if(AS.xarch.isInstanceOf(ref, "types#Component")){
			return "optionalComponent";
		}
		else if(AS.xarch.isInstanceOf(ref, "types#Connector")){
			return "optionalConnector";
		}
		else if(AS.xarch.isInstanceOf(ref, "types#Interface")){
			return "optionalInterface";
		}
		else if(AS.xarch.isInstanceOf(ref, "types#Link")){
			return "optionalLink";
		}
		else if(AS.xarch.isInstanceOf(ref, "types#Signature")){
			return "optionalSignature";
		}
		else if(AS.xarch.isInstanceOf(ref, "types#SignatureInterfaceMapping")){
			return "optionalSignatureInterfaceMapping";
		}
		return null;
	}
	
	public ObjRef getGuardParentRef(IBNAModel m, ObjRef eltRef){
		try{
			ObjRef optionalRef = (ObjRef)AS.xarch.get(eltRef, "optional");
			if(optionalRef == null){
				ObjRef optionsContextRef = AS.xarch.createContext(xArchRef, "options");
				optionalRef = AS.xarch.create(optionsContextRef, "optional");
				AS.xarch.set(eltRef, "optional", optionalRef);
			}
			return optionalRef;
		}
		catch(InvalidOperationException ioe){
			return null;
		}
	}
	
	protected IAction[] getActions(final IBNAView view, final IThing t, final int worldX, final int worldY){
		IAction[] localActions = getLocalActions(view, t, worldX, worldY);
		IAction[] inheritedActions = super.getActions(view, t, worldX, worldY);
		
		IAction[] allActions = new IAction[localActions.length + inheritedActions.length];
		System.arraycopy(localActions, 0, allActions, 0, localActions.length);
		System.arraycopy(inheritedActions, 0, allActions, localActions.length, inheritedActions.length);
		return allActions;
	}
	
	protected IAction[] getLocalActions(final IBNAView view, final IThing t, final int worldX, final int worldY){
		final String eltXArchID = getXArchID(view, t);
		if(eltXArchID == null){
			return new IAction[0];
		}
		
		final ObjRef eltRef = AS.xarch.getByID(xArchRef, eltXArchID);
		if(eltRef == null){
			return new IAction[0];
		}

		if(isPossiblyOptional(eltRef)){
			IAction removeGuardAction = new Action("Make Mandatory/Remove Guard"){
				public void run(){
					AS.xarch.clear(eltRef, "optional");
				}
			};
			return new IAction[]{removeGuardAction};
		}
		else{
			IAction promoteToOptionalAction = new Action("Promote to Optional"){
				public void run(){
					ObjRef optionsContext = AS.xarch.createContext(xArchRef, "options");
					String promotedTypeName = getPromotedTypeName(eltRef);
					if(promotedTypeName != null){
						AS.xarch.promoteTo(optionsContext, promotedTypeName, eltRef);
					}
				}
			};
			return new IAction[]{promoteToOptionalAction};
		}
	}
	
	public boolean isPossiblyOptional(ObjRef ref){
		try{
			ObjRef optionalRef = (ObjRef)AS.xarch.get(ref, "optional");
			return true;
		}
		catch(InvalidOperationException ioe){
			return false;
		}
	}
}
