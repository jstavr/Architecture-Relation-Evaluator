package edu.uci.isr.archstudio4.comp.archipelago.types;

import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoEvent;
import edu.uci.isr.archstudio4.comp.archipelago.types.TypesMapper.TypeKind;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.assemblies.EndpointAssembly;
import edu.uci.isr.bna4.assemblies.MappingAssembly;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesEvents{
	private TypesEvents(){}
	
	public static class WorldCreatedEvent implements IArchipelagoEvent{
		protected ObjRef typeRef;
		protected IBNAWorld world;
		
		public WorldCreatedEvent(ObjRef typeRef, IBNAWorld world){
			this.typeRef = typeRef;
			this.world = world;
		}

		public ObjRef getTypeRef(){
			return typeRef;
		}

		public IBNAWorld getWorld(){
			return world;
		}
	}
	
	public static class TypeUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected ObjRef typeRef;
		
		public TypeUpdatedEvent(IBNAWorld bnaWorld, ObjRef typeRef){
			this.bnaWorld = bnaWorld;
			this.typeRef = typeRef;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}

		public ObjRef getTypeRef(){
			return typeRef;
		}
	}
	
	public static class BrickTypeUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected ObjRef brickTypeRef;
		protected TypeKind kind;
		protected BoxAssembly brickTypeAssembly;
		
		public BrickTypeUpdatedEvent(IBNAWorld bnaWorld, ObjRef brickTypeRef, TypeKind kind, BoxAssembly brickTypeAssembly){
			this.bnaWorld = bnaWorld;
			this.brickTypeRef = brickTypeRef;
			this.kind = kind;
			this.brickTypeAssembly = brickTypeAssembly;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}

		public BoxAssembly getBrickTypeAssembly(){
			return brickTypeAssembly;
		}

		public ObjRef getBrickTypeRef(){
			return brickTypeRef;
		}

		public TypeKind getKind(){
			return kind;
		}
	}
	
	public static class SubarchitectureUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected BoxAssembly brickTypeAssembly;
		protected ObjRef brickTypeRef;
		
		public SubarchitectureUpdatedEvent(IBNAWorld bnaWorld, BoxAssembly brickTypeAssembly, ObjRef brickTypeRef){
			this.bnaWorld = bnaWorld;
			this.brickTypeAssembly = brickTypeAssembly;
			this.brickTypeRef = brickTypeRef;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}

		public BoxAssembly getBrickTypeAssembly(){
			return brickTypeAssembly;
		}

		public ObjRef getBrickTypeRef(){
			return brickTypeRef;
		}
	}
	
	public static class InterfaceTypeUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected ObjRef interfaceTypeRef;
		protected EndpointAssembly interfaceTypeAssembly;
		
		public InterfaceTypeUpdatedEvent(IBNAWorld bnaWorld, ObjRef interfaceTypeRef, EndpointAssembly interfaceTypeAssembly){
			this.bnaWorld = bnaWorld;
			this.interfaceTypeRef = interfaceTypeRef;
			this.interfaceTypeAssembly = interfaceTypeAssembly;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}

		public EndpointAssembly getInterfaceTypeAssembly(){
			return interfaceTypeAssembly;
		}

		public ObjRef getInterfaceTypeRef(){
			return interfaceTypeRef;
		}
	}
	
	public static class SignatureInterfaceMappingUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected BoxAssembly brickTypeAssembly;
		protected ObjRef brickTypeRef;
		protected IBNAWorld innerWorld;
		protected ObjRef signatureInterfaceMappingRef;
		protected MappingAssembly signatureInterfaceMappingAssembly;
		
		public SignatureInterfaceMappingUpdatedEvent(IBNAWorld bnaWorld, BoxAssembly brickTypeAssembly, ObjRef brickTypeRef, IBNAWorld innerWorld, ObjRef signatureInterfaceMappingRef, MappingAssembly signatureInterfaceMappingAssembly){
			this.bnaWorld = bnaWorld;
			this.brickTypeAssembly = brickTypeAssembly;
			this.brickTypeRef = brickTypeRef;
			this.innerWorld = innerWorld;
			this.signatureInterfaceMappingRef = signatureInterfaceMappingRef;
			this.signatureInterfaceMappingAssembly = signatureInterfaceMappingAssembly;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}

		public BoxAssembly getBrickTypeAssembly(){
			return brickTypeAssembly;
		}

		public ObjRef getBrickTypeRef(){
			return brickTypeRef;
		}

		public IBNAWorld getInnerWorld(){
			return innerWorld;
		}

		public MappingAssembly getSignatureInterfaceMappingAssembly(){
			return signatureInterfaceMappingAssembly;
		}

		public ObjRef getSignatureInterfaceMappingRef(){
			return signatureInterfaceMappingRef;
		}
	}
	
	public static class SignatureUpdatedEvent implements IArchipelagoEvent{
		protected IBNAWorld bnaWorld;
		protected BoxAssembly brickTypeAssembly;
		protected ObjRef signatureRef;
		protected EndpointAssembly signatureAssembly;
		
		public SignatureUpdatedEvent(IBNAWorld bnaWorld, BoxAssembly brickTypeAssembly, ObjRef signatureRef, EndpointAssembly signatureAssembly){
			this.bnaWorld = bnaWorld;
			this.brickTypeAssembly = brickTypeAssembly;
			this.signatureRef = signatureRef;
			this.signatureAssembly = signatureAssembly;
		}

		public IBNAWorld getBNAWorld(){
			return bnaWorld;
		}

		public BoxAssembly getBrickTypeAssembly(){
			return brickTypeAssembly;
		}

		public EndpointAssembly getSignatureAssembly(){
			return signatureAssembly;
		}

		public ObjRef getSignatureRef(){
			return signatureRef;
		}
	}
}
