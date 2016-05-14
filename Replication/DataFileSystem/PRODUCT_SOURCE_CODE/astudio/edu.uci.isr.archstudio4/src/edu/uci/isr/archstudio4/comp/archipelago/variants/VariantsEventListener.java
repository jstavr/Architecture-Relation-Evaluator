package edu.uci.isr.archstudio4.comp.archipelago.variants;

import org.eclipse.swt.SWT;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoEvent;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoEventListener;
import edu.uci.isr.archstudio4.comp.archipelago.types.StructureEvents;
import edu.uci.isr.archstudio4.comp.archipelago.types.TypesEditorSupport;
import edu.uci.isr.archstudio4.comp.archipelago.types.TypesEvents;
import edu.uci.isr.archstudio4.comp.archipelago.types.TypesMapper;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.DefaultBNAModel;
import edu.uci.isr.bna4.DefaultBNAWorld;
import edu.uci.isr.bna4.DefaultCoordinateMapper;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.IThingLogicManager;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.logics.coordinating.MirrorAnchorPointLogic;
import edu.uci.isr.bna4.logics.coordinating.MirrorBoundingBoxLogic;
import edu.uci.isr.bna4.logics.coordinating.MoveWithLogic;
import edu.uci.isr.bna4.logics.editing.StandardCursorLogic;
import edu.uci.isr.bna4.logics.events.WorldThingExternalEventsLogic;
import edu.uci.isr.bna4.logics.information.ToolTipLogic;
import edu.uci.isr.bna4.logics.tracking.ModelBoundsTrackingLogic;
import edu.uci.isr.bna4.logics.tracking.ReferenceTrackingLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.bna4.logics.tracking.TypedThingTrackingLogic;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class VariantsEventListener implements IArchipelagoEventListener {

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	public VariantsEventListener(ArchipelagoServices services, ObjRef xArchRef) {
		this.AS = services;
		this.xArchRef = xArchRef;
	}

	public void handleArchipelagoEvent(IArchipelagoEvent evt) {
		/*
		 * if(evt instanceof StructureEvents.WorldCreatedEvent){
		 * handle((StructureEvents.WorldCreatedEvent)evt); } else
		 */
		if (evt instanceof StructureEvents.BrickUpdatedEvent) {
			handle((StructureEvents.BrickUpdatedEvent) evt);
		}
		else if (evt instanceof TypesEvents.WorldCreatedEvent) {
			handle((TypesEvents.WorldCreatedEvent) evt);
		}
		else if (evt instanceof TypesEvents.BrickTypeUpdatedEvent) {
			handle((TypesEvents.BrickTypeUpdatedEvent) evt);
		}
	}

	/*
	 * protected void handle(StructureEvents.WorldCreatedEvent evt){ IBNAWorld
	 * world = evt.getWorld(); world.getThingLogicManager().addThingLogic(new
	 * EditOptionalLogic(AS, xArchRef)); }
	 */

	protected void handle(StructureEvents.BrickUpdatedEvent evt) {
		ObjRef brickRef = evt.getBrickRef();
		if (brickRef != null) {
			ObjRef brickTypeRef = XadlUtils.resolveXLink(AS.xarch, brickRef, "type");
			if (brickTypeRef != null) {
				if (AS.xarch.isInstanceOf(brickTypeRef, "variants#VariantComponentType")
				        || AS.xarch.isInstanceOf(brickTypeRef, "variants#VariantConnectorType")) {
					//evt.getBrickAssembly().getBoxBorderThing().setLineStyle(SWT.LINE_SOLID);
					ObjRef[] variantRefs = AS.xarch.getAll(brickTypeRef, "variant");
					if (variantRefs != null && variantRefs.length > 0) {
						IBNAWorld bnaWorld = (IBNAWorld) AS.treeNodeDataCache.getData(xArchRef, brickTypeRef, TypesEditorSupport.BNA_WORLD_KEY);
						if (bnaWorld == null) {
							bnaWorld = TypesEditorSupport.setupWorld(AS, xArchRef, brickTypeRef);
						}
						if (bnaWorld != null) {
							String brickTypeID = XadlUtils.getID(AS.xarch, brickTypeRef);
							if (brickTypeID != null) {
								//evt.getBrickAssembly().getBoxBorderThing().setLineStyle(SWT.LINE_DASHDOT);
								IBNAWorld oldWorld = evt.getBrickAssembly().getWorldThing().getWorld();
								evt.getBrickAssembly().getWorldThing().clearWorld();
								if (oldWorld != null) {
									oldWorld.destroy();
								}
								IBNAWorld world = createVariantWorld(AS, xArchRef, brickTypeRef, false);
								evt.getBrickAssembly().getWorldThing().setWorld(world);
							}
						}
					}
				}
			}
		}
	}

	/*
	 * protected void handle(StructureEvents.InterfaceUpdatedEvent evt){ ObjRef
	 * interfaceRef = evt.getInterfaceRef();
	 * evt.getInterfaceAssembly().getBoxBorderThing
	 * ().setLineStyle(SWT.LINE_SOLID); if(interfaceRef != null){
	 * if(AS.xarch.isInstanceOf(interfaceRef, "options#OptionalInterface")){
	 * if(OptionsUtils.isOptional(AS.xarch, interfaceRef)){
	 * evt.getInterfaceAssembly
	 * ().getBoxBorderThing().setLineStyle(SWT.LINE_DASH); } } } } protected
	 * void handle(StructureEvents.LinkUpdatedEvent evt){ ObjRef linkRef =
	 * evt.getLinkRef();
	 * evt.getLinkAssembly().getSplineThing().setLineStyle(SWT.LINE_SOLID);
	 * if(linkRef != null){ if(AS.xarch.isInstanceOf(linkRef,
	 * "options#OptionalLink")){ if(OptionsUtils.isOptional(AS.xarch, linkRef)){
	 * evt.getLinkAssembly().getSplineThing().setLineStyle(SWT.LINE_DASH); } } }
	 * }
	 */

	protected void handle(TypesEvents.WorldCreatedEvent evt) {
		IBNAWorld world = evt.getWorld();
		world.getThingLogicManager().addThingLogic(new PromoteToVariantLogic(AS, xArchRef));
		world.getThingLogicManager().addThingLogic(new AddVariantLogic(AS, xArchRef));
	}

	protected void handle(TypesEvents.BrickTypeUpdatedEvent evt) {
		ObjRef brickTypeRef = evt.getBrickTypeRef();
		if (brickTypeRef != null) {
			if (AS.xarch.isInstanceOf(brickTypeRef, "variants#VariantComponentType") || AS.xarch.isInstanceOf(brickTypeRef, "variants#VariantConnectorType")) {
				evt.getBrickTypeAssembly().getBoxBorderThing().setLineStyle(SWT.LINE_SOLID);
				ObjRef[] variantRefs = AS.xarch.getAll(brickTypeRef, "variant");
				if (variantRefs != null && variantRefs.length > 0) {
					evt.getBrickTypeAssembly().getBoxBorderThing().setLineStyle(SWT.LINE_DASHDOT);
					IBNAWorld oldWorld = evt.getBrickTypeAssembly().getWorldThing().getWorld();
					evt.getBrickTypeAssembly().getWorldThing().clearWorld();
					if (oldWorld != null) {
						oldWorld.destroy();
					}
					IBNAWorld world = createVariantWorld(AS, xArchRef, evt.getBrickTypeRef(), true);
					evt.getBrickTypeAssembly().getWorldThing().setWorld(world);
				}
			}
		}
	}

	public static IBNAWorld createVariantWorld(ArchipelagoServices AS, ObjRef xArchRef, ObjRef variantTypeRef, boolean addEditingBehaviors) {
		IBNAModel m = new DefaultBNAModel();

		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(m);
		ept.setProperty("typeRef", variantTypeRef);

		TypesMapper.TypeKind typeKind = null;
		if (AS.xarch.isInstanceOf(variantTypeRef, "types#ComponentType")) {
			typeKind = TypesMapper.TypeKind.COMPONENT_TYPE;
		}
		else if (AS.xarch.isInstanceOf(variantTypeRef, "types#ConnectorType")) {
			typeKind = TypesMapper.TypeKind.CONNECTOR_TYPE;
		}

		ObjRef[] variantRefs = AS.xarch.getAll(variantTypeRef, "variant");

		int offset = 0;
		for (ObjRef element : variantRefs) {
			ObjRef typeRef = XadlUtils.resolveXLink(AS.xarch, element, "variantType");
			if (typeRef != null) {
				boolean isAppropriateType = false;
				if (typeKind == TypesMapper.TypeKind.COMPONENT_TYPE && AS.xarch.isInstanceOf(typeRef, "types#ComponentType")) {
					isAppropriateType = true;
				}
				else if (typeKind == TypesMapper.TypeKind.CONNECTOR_TYPE && AS.xarch.isInstanceOf(typeRef, "types#ConnectorType")) {
					isAppropriateType = true;
				}
				if (isAppropriateType) {
					BoxAssembly typeAssembly = new BoxAssembly(m, null, null);

					String description = XadlUtils.getDescription(AS.xarch, typeRef);
					if (description == null) {
						description = "[No Description]";
					}

					typeAssembly.getBoxGlassThing().setBoundingBox(DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2 + offset,
					        (DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2), 150, 75);
					offset += 175;

					typeAssembly.getBoxedLabelThing().setText(description);
					ToolTipLogic.setToolTip(typeAssembly.getBoxGlassThing(), description);
					typeAssembly.getBoxThing().setGradientFilled(true);
					typeAssembly.getRootThing().setProperty("isVariant", true);
					typeAssembly.getRootThing().setProperty("variantRef", typeRef);
					String id = XadlUtils.getID(AS.xarch, typeRef);
					if (id != null) {
						typeAssembly.getRootThing().setProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME, id);
					}
					switch (typeKind) {
					case COMPONENT_TYPE:
						typeAssembly.getBoxThing().setColor(TypesMapper.getDefaultComponentTypeColor(AS.prefs));
						typeAssembly.getBoxBorderThing().setCount(2);
						break;
					case CONNECTOR_TYPE:
						typeAssembly.getBoxThing().setColor(TypesMapper.getDefaultConnectorTypeColor(AS.prefs));
						typeAssembly.getBoxBorderThing().setCount(1);
						break;
					}
				}
			}
		}

		IBNAWorld w = new DefaultBNAWorld("VARIANT", m);
		IThingLogicManager logicManager = w.getThingLogicManager();

		TypedThingTrackingLogic tttl = new TypedThingTrackingLogic();
		logicManager.addThingLogic(tttl);
		ThingPropertyTrackingLogic tptl = new ThingPropertyTrackingLogic();
		logicManager.addThingLogic(tptl);
		ReferenceTrackingLogic rtl = new ReferenceTrackingLogic();
		logicManager.addThingLogic(rtl);

		logicManager.addThingLogic(new MoveWithLogic(rtl));
		logicManager.addThingLogic(new MirrorAnchorPointLogic(rtl));
		logicManager.addThingLogic(new MirrorBoundingBoxLogic(rtl));
		logicManager.addThingLogic(new StandardCursorLogic());
		logicManager.addThingLogic(new ToolTipLogic());

		ModelBoundsTrackingLogic mbtl = new ModelBoundsTrackingLogic(tttl);
		logicManager.addThingLogic(mbtl);

		logicManager.addThingLogic(new WorldThingExternalEventsLogic(tttl));

		if (addEditingBehaviors) {
			logicManager.addThingLogic(new EditVariantsLogic(AS, xArchRef));
		}
		return w;
	}
}
