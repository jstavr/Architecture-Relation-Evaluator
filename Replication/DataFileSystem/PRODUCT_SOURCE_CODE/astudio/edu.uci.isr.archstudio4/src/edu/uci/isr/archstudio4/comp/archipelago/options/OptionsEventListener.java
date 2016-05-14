package edu.uci.isr.archstudio4.comp.archipelago.options;

import org.eclipse.swt.SWT;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoEvent;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoEventListener;
import edu.uci.isr.archstudio4.comp.archipelago.types.StructureEvents;
import edu.uci.isr.archstudio4.comp.archipelago.types.TypesEvents;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.xarchflat.ObjRef;

public class OptionsEventListener implements IArchipelagoEventListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	public OptionsEventListener(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	public void handleArchipelagoEvent(IArchipelagoEvent evt){
		if(evt instanceof StructureEvents.WorldCreatedEvent){
			handle((StructureEvents.WorldCreatedEvent)evt);
		}
		else if(evt instanceof StructureEvents.BrickUpdatedEvent){
			handle((StructureEvents.BrickUpdatedEvent)evt);
		}
		else if(evt instanceof StructureEvents.InterfaceUpdatedEvent){
			handle((StructureEvents.InterfaceUpdatedEvent)evt);
		}
		else if(evt instanceof StructureEvents.LinkUpdatedEvent){
			handle((StructureEvents.LinkUpdatedEvent)evt);
		}
		else if(evt instanceof TypesEvents.WorldCreatedEvent){
			handle((TypesEvents.WorldCreatedEvent)evt);
		}
		else if(evt instanceof TypesEvents.SignatureUpdatedEvent){
			handle((TypesEvents.SignatureUpdatedEvent)evt);
		}
		else if(evt instanceof TypesEvents.SignatureInterfaceMappingUpdatedEvent){
			handle((TypesEvents.SignatureInterfaceMappingUpdatedEvent)evt);
		}
	}
	
	protected void handle(StructureEvents.WorldCreatedEvent evt){
		IBNAWorld world = evt.getWorld();
		world.getThingLogicManager().addThingLogic(new EditOptionalLogic(AS, xArchRef));
	}
	
	protected void handle(StructureEvents.BrickUpdatedEvent evt){
		ObjRef brickRef = evt.getBrickRef();
		evt.getBrickAssembly().getBoxBorderThing().setLineStyle(SWT.LINE_SOLID);
		if(brickRef != null){
			if(AS.xarch.isInstanceOf(brickRef, "options#OptionalComponent") || AS.xarch.isInstanceOf(brickRef, "options#OptionalConnector")){
				if(OptionsUtils.isOptional(AS.xarch, brickRef)){
					evt.getBrickAssembly().getBoxBorderThing().setLineStyle(SWT.LINE_DASH);
				}
			}
		}
	}

	protected void handle(StructureEvents.InterfaceUpdatedEvent evt){
		ObjRef interfaceRef = evt.getInterfaceRef();
		evt.getInterfaceAssembly().getBoxBorderThing().setLineStyle(SWT.LINE_SOLID);
		if(interfaceRef != null){
			if(AS.xarch.isInstanceOf(interfaceRef, "options#OptionalInterface")){
				if(OptionsUtils.isOptional(AS.xarch, interfaceRef)){
					evt.getInterfaceAssembly().getBoxBorderThing().setLineStyle(SWT.LINE_DASH);
				}
			}
		}
	}
	
	protected void handle(StructureEvents.LinkUpdatedEvent evt){
		ObjRef linkRef = evt.getLinkRef();
		evt.getLinkAssembly().getSplineThing().setLineStyle(SWT.LINE_SOLID);
		if(linkRef != null){
			if(AS.xarch.isInstanceOf(linkRef, "options#OptionalLink")){
				if(OptionsUtils.isOptional(AS.xarch, linkRef)){
					evt.getLinkAssembly().getSplineThing().setLineStyle(SWT.LINE_DASH);
				}
			}
		}
	}
	
	protected void handle(TypesEvents.WorldCreatedEvent evt){
		IBNAWorld world = evt.getWorld();
		world.getThingLogicManager().addThingLogic(new EditOptionalLogic(AS, xArchRef));
	}

	protected void handle(TypesEvents.SignatureUpdatedEvent evt){
		ObjRef signatureRef = evt.getSignatureRef();
		evt.getSignatureAssembly().getBoxBorderThing().setLineStyle(SWT.LINE_SOLID);
		if(signatureRef != null){
			if(AS.xarch.isInstanceOf(signatureRef, "options#OptionalSignature")){
				if(OptionsUtils.isOptional(AS.xarch, signatureRef)){
					evt.getSignatureAssembly().getBoxBorderThing().setLineStyle(SWT.LINE_DASH);
				}
			}
		}
	}
	
	protected void handle(TypesEvents.SignatureInterfaceMappingUpdatedEvent evt){
		ObjRef simRef = evt.getSignatureInterfaceMappingRef();
		evt.getSignatureInterfaceMappingAssembly().getMappingThing().setLineStyle(SWT.LINE_SOLID);
		if(simRef != null){
			if(AS.xarch.isInstanceOf(simRef, "options#OptionalSignatureInterfaceMapping")){
				if(OptionsUtils.isOptional(AS.xarch, simRef)){
					evt.getSignatureInterfaceMappingAssembly().getMappingThing().setLineStyle(SWT.LINE_DASH);
				}
			}
		}
	}

}
