package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.editorsupport.events;

import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoEvent;

import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.assemblies.IAssembly;
//import edu.uci.isr.bna4.assemblies.SplineAssembly;
import edu.uci.isr.xarchflat.ObjRef;

public class ActivityDiagramsEvents{
	private ActivityDiagramsEvents(){}
	
	public static class WorldCreatedEvent implements IArchipelagoEvent{
		protected ObjRef activitydiagramRef;
		protected IBNAWorld world;

		public WorldCreatedEvent(ObjRef activitydiagramRef, IBNAWorld world){
			this.activitydiagramRef = activitydiagramRef;
			this.world = world;
		}

		public ObjRef getActvityDiagramRef(){
			return activitydiagramRef;
		}

		public IBNAWorld getWorld(){
			return world;
		}
	}
	
	public static class ActvityDiagramUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected ObjRef activitydiagramRef;

		public ActvityDiagramUpdatedEvent(IBNAWorld bnaWorld, ObjRef activitydiagramRef){
			this.bnaWorld = bnaWorld;
			this.activitydiagramRef = activitydiagramRef;
		}

		public ObjRef getActivityDiagramRef(){
			return activitydiagramRef;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}
	}

	public static class ActionUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected ObjRef actionRef;
		
		protected IAssembly actionAssembly;
		
		public ActionUpdatedEvent(IBNAWorld bnaWorld, ObjRef actionRef, IAssembly actionAssembly){
			this.bnaWorld = bnaWorld;
			this.actionRef = actionRef;
		
			this.actionAssembly = actionAssembly;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}

		public ObjRef getActionRef(){
			return actionRef;
		}
		
		public IAssembly getActionAssembly(){
			return actionAssembly;
		}
	}
	
	
}
