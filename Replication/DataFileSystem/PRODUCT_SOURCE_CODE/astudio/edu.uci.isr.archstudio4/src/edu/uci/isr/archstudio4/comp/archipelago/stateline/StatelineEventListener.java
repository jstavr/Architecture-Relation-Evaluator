package edu.uci.isr.archstudio4.comp.archipelago.stateline;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoEvent;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoEventListener;
import edu.uci.isr.archstudio4.comp.archipelago.statecharts.StatechartsEvents;
import edu.uci.isr.archstudio4.comp.archipelago.types.StructureEvents;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.xarchflat.ObjRef;

public class StatelineEventListener implements IArchipelagoEventListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	public StatelineEventListener(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	public void handleArchipelagoEvent(IArchipelagoEvent evt){
		if(evt instanceof StructureEvents.WorldCreatedEvent){
			handle((StructureEvents.WorldCreatedEvent)evt);
		}
		else if(evt instanceof StatechartsEvents.WorldCreatedEvent){
			handle((StatechartsEvents.WorldCreatedEvent)evt);
		}
	}
	
	protected void handle(StructureEvents.WorldCreatedEvent evt){
		IBNAWorld world = evt.getWorld();
		world.getThingLogicManager().addThingLogic(new StatelineStructureLogic(AS, xArchRef));
	}
	
	protected void handle(StatechartsEvents.WorldCreatedEvent evt){
		IBNAWorld world = evt.getWorld();
		world.getThingLogicManager().addThingLogic(new StatelineStatechartsLogic(AS, xArchRef));
	}
	

}
