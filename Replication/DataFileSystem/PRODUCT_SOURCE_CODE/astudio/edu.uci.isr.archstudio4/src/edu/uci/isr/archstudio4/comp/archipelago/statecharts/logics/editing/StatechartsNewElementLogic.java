package edu.uci.isr.archstudio4.comp.archipelago.statecharts.logics.editing;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.util.ArchstudioResources;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class StatechartsNewElementLogic extends AbstractThingLogic implements IBNAMenuListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	public StatechartsNewElementLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	public boolean matches(IBNAView view, IThing t){
		return t == null;
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
		final IBNAView fview = view;
		final IThing ft = t;
		final int fworldX = worldX;
		final int fworldY = worldY;
		
		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(view.getWorld().getBNAModel());

		String statechartXArchID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		if(statechartXArchID == null){
			//Nothing to set description on
			return new IAction[0];
		}
		
		final ObjRef statechartRef = AS.xarch.getByID(xArchRef, statechartXArchID);
		if(statechartRef == null){
			//Nothing to add elements to
			return new IAction[0];
		}
		
		ArchipelagoUtils.setNewThingSpot(view.getWorld().getBNAModel(), fworldX, fworldY);
		
		Action newStateAction = new Action("New Ordinary State"){
			public void run(){
				ObjRef statechartsContextRef = AS.xarch.createContext(xArchRef, "statecharts");
				ObjRef stateRef = AS.xarch.create(statechartsContextRef, "state");
				AS.xarch.set(stateRef, "id", UIDGenerator.generateUID("state"));
				XadlUtils.setDescription(AS.xarch, stateRef, "[New State]");
				AS.xarch.set(stateRef, "stateType", "state");
				AS.xarch.add(statechartRef, "state", stateRef);
			}
			
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_STATE);
			}
		};
		
		Action newInitialStateAction = new Action("New Initial State"){
			public void run(){
				ObjRef statechartsContextRef = AS.xarch.createContext(xArchRef, "statecharts");
				ObjRef stateRef = AS.xarch.create(statechartsContextRef, "state");
				AS.xarch.set(stateRef, "id", UIDGenerator.generateUID("state"));
				XadlUtils.setDescription(AS.xarch, stateRef, "[Initial State]");
				AS.xarch.set(stateRef, "stateType", "initial");
				AS.xarch.add(statechartRef, "state", stateRef);
			}
			
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_INITIAL_STATE);
			}
		};
		
		Action newFinalStateAction = new Action("New Final State"){
			public void run(){
				ObjRef statechartsContextRef = AS.xarch.createContext(xArchRef, "statecharts");
				ObjRef stateRef = AS.xarch.create(statechartsContextRef, "state");
				AS.xarch.set(stateRef, "id", UIDGenerator.generateUID("state"));
				XadlUtils.setDescription(AS.xarch, stateRef, "[Final State]");
				AS.xarch.set(stateRef, "stateType", "final");
				AS.xarch.add(statechartRef, "state", stateRef);
			}
			
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_FINAL_STATE);
			}
		};
		
		Action newTransitionAction = new Action("New Transition"){
			public void run(){
				ObjRef statechartsContextRef = AS.xarch.createContext(xArchRef, "statecharts");
				ObjRef transitionRef = AS.xarch.create(statechartsContextRef, "transition");
				AS.xarch.set(transitionRef, "id", UIDGenerator.generateUID("transition"));
				XadlUtils.setDescription(AS.xarch, transitionRef, "[New Transition]");
				AS.xarch.add(statechartRef, "transition", transitionRef);
			}
			
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_LINK);
			}
		};
		
		return new IAction[]{newStateAction, newInitialStateAction, newFinalStateAction, newTransitionAction};
	}
	
}
