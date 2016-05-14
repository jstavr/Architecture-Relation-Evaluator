package edu.uci.isr.archstudio4.comp.archipelago.variants;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractEditGuardLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class EditVariantsLogic extends AbstractEditGuardLogic{

	public EditVariantsLogic(ArchipelagoServices services, ObjRef xArchRef){
		super(services, xArchRef);
	}

	public boolean matches(IBNAView view, IThing t){
		if(t instanceof BoxGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				if(pt.hasProperty("isVariant")){
					return true;
				}
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
	
	public ObjRef getGuardParentRef(IBNAModel m, ObjRef eltRef){
		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(m);
		ObjRef variantTypeRef = ept.getProperty("typeRef");
		if(variantTypeRef != null){
			ObjRef[] variantRefs = AS.xarch.getAll(variantTypeRef, "variant");
			for(int i = 0; i < variantRefs.length; i++){
				ObjRef variantTargetRef = XadlUtils.resolveXLink(AS.xarch, variantRefs[i], "variantType");
				if(AS.xarch.equals(variantTargetRef, eltRef)){
					return variantRefs[i];
				}
			}
		}
		return null;
	}
	
	protected IAction[] getActions(final IBNAView view, final IThing t, final int worldX, final int worldY){
		if(view.getWorld().getID().equals("VARIANT")){
			IAction[] localActions = getLocalActions(view, t, worldX, worldY);
			IAction[] inheritedActions = super.getActions(view, t, worldX, worldY);
			
			IAction[] allActions = new IAction[localActions.length + inheritedActions.length];
			System.arraycopy(localActions, 0, allActions, 0, localActions.length);
			System.arraycopy(inheritedActions, 0, allActions, localActions.length, inheritedActions.length);
			return allActions;
		}
		return new IAction[0];
	}

	protected IAction[] getLocalActions(final IBNAView view, final IThing t, final int worldX, final int worldY){
		IBNAModel m = view.getWorld().getBNAModel();
		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(m);
		final ObjRef variantTypeRef = ept.getProperty("typeRef");
		if(variantTypeRef != null){
			if(t instanceof BoxGlassThing){
				IThing parentThing = m.getParentThing(t);
				if(parentThing != null){
					if(parentThing.hasProperty("isVariant")){
						final ObjRef typeRef = parentThing.getProperty("variantRef");
						if(typeRef != null){
							IAction removeVariantAction = new Action("Remove Variant"){
								public void run(){
									removeVariant(variantTypeRef, typeRef);
								}
							};
							
							
							return new IAction[]{removeVariantAction};
						}
					}
				}
			}
		}
		return new IAction[0];
	}

	protected void removeVariant(ObjRef variantTypeRef, ObjRef innerTypeRef){
		ObjRef[] variantRefs = AS.xarch.getAll(variantTypeRef, "variant");
		for(int i = 0; i < variantRefs.length; i++){
			ObjRef variantTargetRef = XadlUtils.resolveXLink(AS.xarch, variantRefs[i], "variantType");
			if(AS.xarch.equals(variantTargetRef, innerTypeRef)){
				AS.xarch.remove(variantTypeRef, "variant", variantRefs[i]);
				return;
			}
		}
	}
}
