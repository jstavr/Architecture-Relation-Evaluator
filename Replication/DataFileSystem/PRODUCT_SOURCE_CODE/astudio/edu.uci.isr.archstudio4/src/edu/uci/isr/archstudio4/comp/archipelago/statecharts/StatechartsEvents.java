package edu.uci.isr.archstudio4.comp.archipelago.statecharts;

import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoEvent;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.xarchflat.ObjRef;

@Deprecated
public class StatechartsEvents{

	private StatechartsEvents(){
	}

	public static class WorldCreatedEvent
		implements IArchipelagoEvent{

		protected ObjRef statechartRef;
		protected IBNAWorld world;

		public WorldCreatedEvent(ObjRef statechartRef, IBNAWorld world){
			this.statechartRef = statechartRef;
			this.world = world;
		}

		public ObjRef getStatechartRef(){
			return statechartRef;
		}

		public IBNAWorld getWorld(){
			return world;
		}
	}
}
