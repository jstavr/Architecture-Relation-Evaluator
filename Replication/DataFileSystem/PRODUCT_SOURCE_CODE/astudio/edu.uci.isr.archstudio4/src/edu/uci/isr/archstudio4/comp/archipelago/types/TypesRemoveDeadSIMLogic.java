package edu.uci.isr.archstudio4.comp.archipelago.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.bna4.things.swt.SWTTextThing;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesRemoveDeadSIMLogic extends AbstractThingLogic implements IBNAMenuListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	protected List<SWTTextThing> openControls = Collections.synchronizedList(new ArrayList<SWTTextThing>());
	
	public TypesRemoveDeadSIMLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
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
	
	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){
		if(matches(view, t)){
			for(IContributionItem item : getActions(view, t, worldX, worldY)){
				m.add(item);
			}
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}
	
	protected IContributionItem[] getActions(IBNAView view, IThing t, int worldX, int worldY){
		final IBNAView fview = view;
		final IThing ft = t;
		final int fworldX = worldX;
		final int fworldY = worldY;
		
		final String eltXArchID = getXArchID(view, t);
		if(eltXArchID == null){
			//Nothing to set description on
			return new IContributionItem[0];
		}
		
		final ObjRef eltRef = AS.xarch.getByID(xArchRef, eltXArchID);
		if(eltRef == null){
			//Nothing to set description on
			return new IContributionItem[0];
		}
		
		final ObjRef[] deadSIMRefs = XadlUtils.getInvalidSignatureInterfaceMappings(AS.xarch, eltRef);
		if((deadSIMRefs == null) || (deadSIMRefs.length == 0)){
			return new IContributionItem[0];
		}
		
		IMenuManager sm = new MenuManager("Remove Invalid Signature-Interface Mapping");
		
		IAction removeAllDeadRefsAction = new Action("Remove All"){
			public void run(){
				ObjRef subArchitectureRef = (ObjRef)AS.xarch.get(eltRef, "subArchitecture");
				if(subArchitectureRef != null){
					for(int i = 0; i < deadSIMRefs.length; i++){
						AS.xarch.remove(subArchitectureRef, "signatureInterfaceMapping", deadSIMRefs[i]);
					}
				}
			}
		};
		sm.add(removeAllDeadRefsAction);
		sm.add(new Separator());
		
		for(int i = 0; i < deadSIMRefs.length; i++){
			final ObjRef fdeadSIMRef = deadSIMRefs[i];
			String deadSIMRefDescription = XadlUtils.getDescription(AS.xarch, fdeadSIMRef);
			if((deadSIMRefDescription == null) || (deadSIMRefDescription.trim().length() == 0)){
				deadSIMRefDescription = "[Unknown Signature-Interface Mapping]";
			}
			IAction removeDeadRefAction = new Action(deadSIMRefDescription){
				public void run(){
					ObjRef subArchitectureRef = (ObjRef)AS.xarch.get(eltRef, "subArchitecture");
					if(subArchitectureRef != null){
						AS.xarch.remove(subArchitectureRef, "signatureInterfaceMapping", fdeadSIMRef);
					}
				}
			};
			sm.add(removeDeadRefAction);
		}
		
		return new IContributionItem[]{sm};
	}
	
}
