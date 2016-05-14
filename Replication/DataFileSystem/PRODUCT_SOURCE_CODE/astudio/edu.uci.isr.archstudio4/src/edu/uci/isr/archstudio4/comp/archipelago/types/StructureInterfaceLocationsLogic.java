package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.util.InterfaceLocationSyncUtils;
import edu.uci.isr.archstudio4.comp.archipelago.util.InterfaceLocationSyncUtils.SyncInfo;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.xarchflat.ObjRef;

public class StructureInterfaceLocationsLogic extends AbstractThingLogic implements IBNAMenuListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	public StructureInterfaceLocationsLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	public boolean matches(IBNAView view, IThing t){
		if(t != null){
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(t);
			if((parentThing != null) && StructureMapper.isBrickAssemblyRootThing(parentThing)){
				return true;
			}
		}
		return false;
	}
	
	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){
		if(matches(view, t)){
			for(IAction action : getActions(view, t, worldX, worldY)){
				m.add(action);
			}
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}
	
	protected IAction[] getActions(IBNAView view, IThing t, int worldX, int worldY){
		SyncInfo si = InterfaceLocationSyncUtils.getSyncInfo(AS, xArchRef, view.getWorld(), t);
		if(si != null){
			return getActions(si);
		}
		return new IAction[0];
	}
	
	protected IAction[] getActions(final SyncInfo si){
		IAction pushAction = new Action("Copy Interface Locations to Type"){
			public void run(){
				InterfaceLocationSyncUtils.syncInterfaceLocations(si.brickAssembly, si.interfaceAssemblies, si.brickTypeAssembly, si.signatureAssemblies, true);
			}
		};
		
		IAction pullAction = new Action("Copy Interface Locations from Type"){
			public void run(){
				InterfaceLocationSyncUtils.syncInterfaceLocations(si.brickAssembly, si.interfaceAssemblies, si.brickTypeAssembly, si.signatureAssemblies, false);
			}
		};
		
		return new IAction[]{pushAction, pullAction};
	}
	
}
