package edu.uci.isr.archstudio4.comp.archipelago.variants;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.types.TypesMapper;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.xarchflat.ObjRef;

public class PromoteToVariantLogic extends AbstractThingLogic implements IBNAMenuListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	public PromoteToVariantLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	protected boolean matches(IBNAView view, IThing t){
		if(t instanceof BoxGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				if(TypesMapper.isBrickTypeAssemblyRootThing(pt)){
					String xArchID = pt.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
					if(xArchID != null){
						ObjRef eltRef = AS.xarch.getByID(xArchRef, xArchID);
						if(eltRef != null){
							if(AS.xarch.isInstanceOf(eltRef, "types#ComponentType")){
								if(!AS.xarch.isInstanceOf(eltRef, "variants#VariantComponentType")){
									return true;
								}
							}
							else if(AS.xarch.isInstanceOf(eltRef, "types#ConnectorType")){
								if(!AS.xarch.isInstanceOf(eltRef, "variants#VariantConnectorType")){
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){
		IThing[] selectedThings = BNAUtils.getSelectedThings(view.getWorld().getBNAModel());
		if(selectedThings.length > 1) return;
		
		if(matches(view, t)){
			for(IAction action : getActions(view, t, worldX, worldY)){
				m.add(action);
			}
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}
	
	protected IAction[] getActions(IBNAView view, IThing t, int worldX, int worldY){
		if(t instanceof BoxGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				if(TypesMapper.isBrickTypeAssemblyRootThing(pt)){
					String xArchID = pt.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
					if(xArchID != null){
						final ObjRef eltRef = AS.xarch.getByID(xArchRef, xArchID);
						if(eltRef != null){
							if(AS.xarch.isInstanceOf(eltRef, "types#ComponentType")){
								IAction promoteToVariantComponentTypeAction = new Action("Promote to Variant Type"){
									public void run(){
										ObjRef variantsContextRef = AS.xarch.createContext(xArchRef, "variants");
										AS.xarch.promoteTo(variantsContextRef, "variantComponentType", eltRef);
									}
								};
								return new IAction[]{promoteToVariantComponentTypeAction};
							}
							else if(AS.xarch.isInstanceOf(eltRef, "types#ConnectorType")){
								IAction promoteToVariantConnectorTypeAction = new Action("Promote to Variant Type"){
									public void run(){
										ObjRef variantsContextRef = AS.xarch.createContext(xArchRef, "variants");
										AS.xarch.promoteTo(variantsContextRef, "variantConnectorType", eltRef);
									}
								};
								return new IAction[]{promoteToVariantConnectorTypeAction};
							}
						}
					}
				}
			}
		}
		return new IAction[0];
	}
	
	
}
