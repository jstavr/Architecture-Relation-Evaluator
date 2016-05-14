package edu.uci.isr.archstudio4.comp.archipelago.types;

import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoEvent;
import edu.uci.isr.archstudio4.comp.archipelago.types.StructureMapper.BrickKind;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.assemblies.EndpointAssembly;
import edu.uci.isr.bna4.assemblies.MappingAssembly;
import edu.uci.isr.bna4.assemblies.SplineAssembly;
import edu.uci.isr.xarchflat.ObjRef;

public class StructureEvents{
	private StructureEvents(){}
	
	public static class WorldCreatedEvent implements IArchipelagoEvent{
		protected ObjRef archStructureRef;
		protected IBNAWorld world;

		public WorldCreatedEvent(ObjRef archStructureRef, IBNAWorld world){
			this.archStructureRef = archStructureRef;
			this.world = world;
		}

		public ObjRef getArchStructureRef(){
			return archStructureRef;
		}

		public IBNAWorld getWorld(){
			return world;
		}
	}
	
	public static class StructureUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected ObjRef archStructureRef;

		public StructureUpdatedEvent(IBNAWorld bnaWorld, ObjRef archStructureRef){
			this.bnaWorld = bnaWorld;
			this.archStructureRef = archStructureRef;
		}

		public ObjRef getArchStructureRef(){
			return archStructureRef;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}
	}

	public static class BrickUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected ObjRef brickRef;
		protected BrickKind kind;
		protected BoxAssembly brickAssembly;
		
		public BrickUpdatedEvent(IBNAWorld bnaWorld, ObjRef brickRef, BrickKind kind, BoxAssembly brickAssembly){
			this.bnaWorld = bnaWorld;
			this.brickRef = brickRef;
			this.kind = kind;
			this.brickAssembly = brickAssembly;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}

		public ObjRef getBrickRef(){
			return brickRef;
		}

		public BrickKind getBrickKind(){
			return kind;
		}
		
		public BoxAssembly getBrickAssembly(){
			return brickAssembly;
		}
	}
	
	public static class SubarchitectureUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected BoxAssembly brickAssembly;
		protected ObjRef brickRef;
		
		public SubarchitectureUpdatedEvent(IBNAWorld bnaWorld, BoxAssembly brickAssembly, ObjRef brickRef){
			this.bnaWorld = bnaWorld;
			this.brickAssembly = brickAssembly;
			this.brickRef = brickRef;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}

		public BoxAssembly getBrickAssembly(){
			return brickAssembly;
		}

		public ObjRef getBrickRef(){
			return brickRef;
		}
	}
	
	public static class InterfaceInterfaceMappingUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected BoxAssembly brickAssembly;
		protected ObjRef brickRef;
		protected ObjRef brickTypeRef;
		protected IBNAWorld innerWorld;
		protected ObjRef signatureInterfaceMappingRef;
		protected MappingAssembly mappingAssembly;
		
		public InterfaceInterfaceMappingUpdatedEvent(IBNAWorld bnaWorld, BoxAssembly brickAssembly, ObjRef brickRef, ObjRef brickTypeRef, IBNAWorld innerWorld, ObjRef signatureInterfaceMappingRef, MappingAssembly mappingAssembly){
			this.bnaWorld = bnaWorld;
			this.brickAssembly = brickAssembly;
			this.brickRef = brickRef;
			this.brickTypeRef = brickTypeRef;
			this.innerWorld = innerWorld;
			this.signatureInterfaceMappingRef = signatureInterfaceMappingRef;
			this.mappingAssembly = mappingAssembly;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}

		public BoxAssembly getBrickAssembly(){
			return brickAssembly;
		}

		public ObjRef getBrickRef(){
			return brickRef;
		}

		public ObjRef getBrickTypeRef(){
			return brickTypeRef;
		}

		public IBNAWorld getInnerWorld(){
			return innerWorld;
		}

		public ObjRef getSignatureInterfaceMappingRef(){
			return signatureInterfaceMappingRef;
		}
		
		public MappingAssembly getMappingAssembly(){
			return mappingAssembly;
		}
	}
	
	public static class InterfaceUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected BoxAssembly brickAssembly;
		protected ObjRef interfaceRef;
		protected EndpointAssembly interfaceAssembly;
		
		public InterfaceUpdatedEvent(IBNAWorld bnaWorld, BoxAssembly brickAssembly, ObjRef interfaceRef, EndpointAssembly interfaceAssembly){
			this.bnaWorld = bnaWorld;
			this.brickAssembly = brickAssembly;
			this.interfaceRef = interfaceRef;
			this.interfaceAssembly = interfaceAssembly;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}

		public BoxAssembly getBrickAssembly(){
			return brickAssembly;
		}

		public ObjRef getInterfaceRef(){
			return interfaceRef;
		}
		
		public EndpointAssembly getInterfaceAssembly(){
			return interfaceAssembly;
		}
	}
	
	public static class LinkUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected ObjRef linkRef;
		protected SplineAssembly linkAssembly;
		
		public LinkUpdatedEvent(IBNAWorld bnaWorld, ObjRef linkRef, SplineAssembly linkAssembly){
			this.bnaWorld = bnaWorld;
			this.linkRef = linkRef;
			this.linkAssembly = linkAssembly;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}

		public ObjRef getLinkRef(){
			return linkRef;
		}
		
		public SplineAssembly getLinkAssembly(){
			return linkAssembly;
		}
	}
}
