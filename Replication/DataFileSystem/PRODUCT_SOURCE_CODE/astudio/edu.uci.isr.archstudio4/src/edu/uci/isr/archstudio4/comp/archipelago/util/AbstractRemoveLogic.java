package edu.uci.isr.archstudio4.comp.archipelago.util;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.xarchflat.ObjRef;

public abstract class AbstractRemoveLogic extends AbstractThingLogic implements IBNAMenuListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	public AbstractRemoveLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	public abstract boolean matches(IBNAView view, IThing t);
	public abstract String getXArchID(IBNAView view, IThing t);
	
	/**
	 * By default this simply removes the xADL element from its
	 * parent but can be overridden for alternative behavior
	 */
	public void remove(IBNAView view, IThing t, ObjRef ref){
		String elementName = AS.xarch.getElementName(ref);
		if(elementName != null){
			ObjRef parentRef = AS.xarch.getParent(ref);
			if(parentRef != null){
				AS.xarch.remove(parentRef, elementName, ref);
			}
		}
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
		final IBNAView fview = view;
		final IThing ft = t;
		final int fworldX = worldX;
		final int fworldY = worldY;
		
		final String eltXArchID = getXArchID(view, t);
		if(eltXArchID == null){
			//Nothing to set description on
			return new IAction[0];
		}
		
		final ObjRef eltRef = AS.xarch.getByID(xArchRef, eltXArchID);
		if(eltRef == null){
			//Nothing to set description on
			return new IAction[0];
		}
		
		Action removeAction = new Action("Remove"){
			public void run(){
				remove(fview, ft, eltRef);
			}
		};
		
		return new IAction[]{removeAction};
	}
	
}
