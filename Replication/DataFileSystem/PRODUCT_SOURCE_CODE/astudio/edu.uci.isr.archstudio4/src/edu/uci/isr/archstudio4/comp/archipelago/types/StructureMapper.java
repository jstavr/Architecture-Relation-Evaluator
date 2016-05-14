package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.util.InterfaceLocationSyncUtils;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.UserEditableUtils;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.assemblies.EndpointAssembly;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.assemblies.MappingAssembly;
import edu.uci.isr.bna4.assemblies.SplineAssembly;
import edu.uci.isr.bna4.facets.IHasAnchorPoint;
import edu.uci.isr.bna4.facets.IHasAssemblyData;
import edu.uci.isr.bna4.facets.IHasLineStyle;
import edu.uci.isr.bna4.facets.IHasMutableBoundingBox;
import edu.uci.isr.bna4.facets.IHasMutableColor;
import edu.uci.isr.bna4.facets.IHasMutableSelected;
import edu.uci.isr.bna4.facets.IHasText;
import edu.uci.isr.bna4.facets.IRelativeMovable;
import edu.uci.isr.bna4.logics.coordinating.MaintainStickyPointLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainTagsLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainStickyPointLogic.StickyMode;
import edu.uci.isr.bna4.logics.information.ToolTipLogic;
import edu.uci.isr.bna4.things.utility.AssemblyRootThing;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.bna4.things.utility.NoThing;
import edu.uci.isr.widgets.swt.constants.Flow;
import edu.uci.isr.widgets.swt.constants.FontStyle;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class StructureMapper{

	private StructureMapper(){
	}

	public static enum BrickKind{
		COMPONENT, CONNECTOR;

		public static BrickKind fromName(String name){
			try{
				return BrickKind.valueOf(name);
			}
			catch(IllegalArgumentException e){
			}
			return null;
		}
	}

	public static final String ASSEMBLY_TYPE_COMPONENT = BrickKind.COMPONENT.name();
	public static final String ASSEMBLY_TYPE_CONNECTOR = BrickKind.CONNECTOR.name();
	public static final String ASSEMBLY_TYPE_INTERFACE = "interface";
	public static final String ASSEMBLY_TYPE_IIM = "interfaceInterfaceMapping";
	public static final String ASSEMBLY_TYPE_LINK = "link";

	public static void updateStructureInJob(ArchipelagoServices AS, IBNAWorld world, ObjRef archStructureRef){
		final ArchipelagoServices fAS = AS;
		final IBNAWorld fbnaWorld = world;
		final ObjRef farchStructureRef = archStructureRef;
		Job job = new Job("Updating Structure"){

			@Override
			protected IStatus run(IProgressMonitor monitor){
				updateStructure(fAS, fbnaWorld, farchStructureRef);
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.setPriority(Job.SHORT);
		job.schedule();
		/*
		 * try{ job.join(); } catch(InterruptedException ie){}
		 */
	}

	public static void updateStructure(ArchipelagoServices AS, IBNAWorld bnaWorld, ObjRef archStructureRef){
		removeOrphanedBricks(AS, bnaWorld, archStructureRef);

		ObjRef[] componentRefs = AS.xarch.getAll(archStructureRef, "component");
		for(ObjRef componentRef: componentRefs){
			updateComponent(AS, bnaWorld, componentRef);
		}

		ObjRef[] connectorRefs = AS.xarch.getAll(archStructureRef, "connector");
		for(ObjRef connectorRef: connectorRefs){
			updateConnector(AS, bnaWorld, connectorRef);
		}

		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(bnaWorld.getBNAModel());
		if(!ept.hasProperty("hintsApplied")){
			StructureEditorSupport.readHints(AS, AS.xarch.getXArch(archStructureRef), bnaWorld.getBNAModel(), archStructureRef);
			ept.setProperty("hintsApplied", true);
		}
		AS.eventBus.fireArchipelagoEvent(new StructureEvents.StructureUpdatedEvent(bnaWorld, archStructureRef));
	}

	public synchronized static void removeOrphanedBricks(ArchipelagoServices AS, IBNAWorld bnaWorld, ObjRef archStructureRef){
		IBNAModel bnaModel = bnaWorld.getBNAModel();
		IThing brickRootThing = getBrickRootThing(bnaModel);
		IThing[] childThings = bnaModel.getChildThings(brickRootThing);
		for(IThing t: childThings){
			if(isBrickAssemblyRootThing(t)){
				String xArchID = t.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
				if(xArchID != null){
					BrickKind kind = BrickKind.fromName((String)((AssemblyRootThing)t).getAssemblyKind());
					if(kind == null){
						continue;
					}

					ObjRef[] brickRefs = null;
					switch(kind){
					case COMPONENT:
						brickRefs = AS.xarch.getAll(archStructureRef, "component");
						break;
					case CONNECTOR:
						brickRefs = AS.xarch.getAll(archStructureRef, "connector");
						break;
					default:
						continue;
					}

					boolean found = false;
					for(ObjRef refToMatch: brickRefs){
						String idToMatch = XadlUtils.getID(AS.xarch, refToMatch);
						if(idToMatch != null && idToMatch.equals(xArchID)){
							found = true;
							break;
						}
					}
					if(!found){
						bnaModel.removeThingAndChildren(t);
					}
				}
			}
		}
	}

	public static void updateComponent(ArchipelagoServices AS, IBNAWorld bnaWorld, ObjRef componentRef){
		updateBrick(AS, bnaWorld, componentRef, BrickKind.COMPONENT);
	}

	public static void updateConnector(ArchipelagoServices AS, IBNAWorld bnaWorld, ObjRef connectorRef){
		updateBrick(AS, bnaWorld, connectorRef, BrickKind.CONNECTOR);
	}

	public synchronized static void updateBrick(ArchipelagoServices AS, IBNAWorld bnaWorld, ObjRef brickRef, BrickKind kind){
		IBNAModel bnaModel = bnaWorld.getBNAModel();

		String xArchID = XadlUtils.getID(AS.xarch, brickRef);
		if(xArchID == null){
			return;
		}

		IThing brickAssemblyRootThing = ArchipelagoUtils.findThing(bnaModel, xArchID);
		BoxAssembly brickAssembly;

		boolean isNew = false;

		if(brickAssemblyRootThing == null || !isBrickAssemblyRootThing(brickAssemblyRootThing)){
			isNew = true;

			brickAssembly = new BoxAssembly(bnaModel, getBrickRootThing(bnaModel), kind.name());

			Point p = ArchipelagoUtils.findOpenSpotForNewThing(bnaModel);
			brickAssembly.getBoxGlassThing().setBoundingBox(p.x, p.y, 100, 100);
			UserEditableUtils.addEditableQuality(brickAssembly.getBoxGlassThing(), IHasMutableSelected.USER_MAY_SELECT, IHasMutableBoundingBox.USER_MAY_RESIZE, IRelativeMovable.USER_MAY_MOVE);
			UserEditableUtils.addEditableQuality(brickAssembly.getBoxThing(), IHasMutableColor.USER_MAY_EDIT_COLOR);

			brickAssembly.getBoxThing().setGradientFilled(true);
			brickAssembly.getBoxBorderThing().setLineWidth(1);
			brickAssembly.getBoxBorderThing().setLineStyle(SWT.LINE_SOLID);

			switch(kind){
			case COMPONENT:
				brickAssembly.getBoxThing().setColor(getDefaultComponentColor(AS.prefs));
				brickAssembly.getBoxBorderThing().setCount(2);
				break;
			case CONNECTOR:
				brickAssembly.getBoxThing().setColor(getDefaultConnectorColor(AS.prefs));
				brickAssembly.getBoxBorderThing().setCount(1);
				break;
			}
		}
		else{
			brickAssembly = AssemblyUtils.getAssemblyWithRoot(brickAssemblyRootThing);
		}
		String description = XadlUtils.getDescription(AS.xarch, brickRef);
		if(description == null){
			description = "[No Description]";
		}

		brickAssembly.getBoxedLabelThing().setText(description);
		ToolTipLogic.setToolTip(brickAssembly.getBoxGlassThing(), description);

		// update the interfaces
		removeOrphanedInterfaces(AS, bnaWorld, brickAssembly, brickRef);
		ObjRef[] interfaceRefs = AS.xarch.getAll(brickRef, "interface");
		for(ObjRef interfaceRef: interfaceRefs){
			updateInterface(AS, bnaWorld, brickAssembly, interfaceRef);
		}

		if(isNew){
			InterfaceLocationSyncUtils.syncInterfaceLocations(AS, AS.xarch.getXArch(brickRef), bnaWorld, brickAssembly.getRootThing(), false);
		}

		FontData[] fd = PreferenceConverter.getFontDataArray(AS.prefs, ArchipelagoTypesConstants.PREF_DEFAULT_FONT);
		if(fd.length > 0){
			brickAssembly.getBoxedLabelThing().setFontName(fd[0].getName());
			brickAssembly.getBoxedLabelThing().setFontSize(fd[0].getHeight());
			brickAssembly.getBoxedLabelThing().setFontStyle(FontStyle.fromSWT(fd[0].getStyle()));
		}

		brickAssembly.getRootThing().setProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME, xArchID);
		updateSubarchitecture(AS, bnaWorld, brickAssembly, brickRef);

		AS.eventBus.fireArchipelagoEvent(new StructureEvents.BrickUpdatedEvent(bnaWorld, brickRef, kind, brickAssembly));
	}

	public synchronized static void updateSubarchitecture(ArchipelagoServices AS, IBNAWorld bnaWorld, BoxAssembly brickAssembly, ObjRef brickRef){
		ObjRef brickTypeRef = XadlUtils.resolveXLink(AS.xarch, brickRef, "type");
		if(brickTypeRef != null){
			ObjRef subarchitectureRef = (ObjRef)AS.xarch.get(brickTypeRef, "subArchitecture");
			if(subarchitectureRef != null){
				ObjRef internalStructureRef = XadlUtils.resolveXLink(AS.xarch, subarchitectureRef, "archStructure");
				if(internalStructureRef != null){
					ObjRef xArchRef = AS.xarch.getXArch(brickTypeRef);
					IBNAWorld internalWorld = StructureEditorSupport.setupWorld(AS, xArchRef, internalStructureRef);
					if(internalWorld != null){
						StructureMapper.updateStructure(AS, internalWorld, internalStructureRef);
						brickAssembly.getWorldThing().setWorld(internalWorld);

						removeOrphanedInterfaceInterfaceMappings(AS, bnaWorld, brickAssembly, brickRef, internalWorld);
						ObjRef[] simRefs = AS.xarch.getAll(subarchitectureRef, "signatureInterfaceMapping");
						for(ObjRef simRef: simRefs){
							updateInterfaceInterfaceMapping(AS, bnaWorld, brickAssembly, brickRef, brickTypeRef, internalWorld, simRef);
						}
						AS.eventBus.fireArchipelagoEvent(new StructureEvents.SubarchitectureUpdatedEvent(bnaWorld, brickAssembly, brickRef));
						return;
					}
				}
			}
		}
		// It doesn't have a valid subarchitecture, clear it
		brickAssembly.getWorldThing().clearWorld();

		AS.eventBus.fireArchipelagoEvent(new StructureEvents.SubarchitectureUpdatedEvent(bnaWorld, brickAssembly, brickRef));
	}

	public static void removeOrphanedInterfaceInterfaceMappings(ArchipelagoServices AS, IBNAWorld bnaWorld, BoxAssembly brickAssembly, ObjRef brickRef, IBNAWorld innerWorld){
		ObjRef[] interfaceRefs = AS.xarch.getAll(brickRef, "interface");
		ObjRef[] signatureRefs = new ObjRef[interfaceRefs.length];
		for(int i = 0; i < interfaceRefs.length; i++){
			signatureRefs[i] = XadlUtils.resolveXLink(AS.xarch, interfaceRefs[i], "signature");
		}

		ObjRef brickTypeRef = XadlUtils.resolveXLink(AS.xarch, brickRef, "type");
		if(brickTypeRef != null){
			IBNAModel bnaModel = bnaWorld.getBNAModel();
			ObjRef subArchitectureRef = (ObjRef)AS.xarch.get(brickTypeRef, "subArchitecture");
			if(subArchitectureRef != null){
				ObjRef[] simRefs = AS.xarch.getAll(subArchitectureRef, "signatureInterfaceMapping");

				IThing[] childThings = bnaModel.getChildThings(brickAssembly.getMappingRootThing());

				for(IThing t: childThings){
					IAssembly assembly = AssemblyUtils.getAssemblyWithRoot(t);
					if(assembly instanceof MappingAssembly){
						String xArchID = assembly.getRootThing().getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
						if(xArchID != null){
							boolean found = false;
							for(ObjRef refToMatch: simRefs){
								String idToMatch = XadlUtils.getID(AS.xarch, refToMatch);
								if(idToMatch != null && idToMatch.equals(xArchID)){
									ObjRef simSignature = XadlUtils.resolveXLink(AS.xarch, refToMatch, "outerSignature");
									for(ObjRef element: signatureRefs){
										if(BNAUtils.nulleq(simSignature, element)){
											found = true;
											break;
										}
									}
									if(found){
										break;
									}
								}
							}
							if(!found){
								bnaModel.removeThingAndChildren(t);
							}
						}
					}
				}
			}
		}
	}

	public static void updateInterfaceInterfaceMapping(ArchipelagoServices AS, IBNAWorld bnaWorld, BoxAssembly brickAssembly, ObjRef brickRef, ObjRef brickTypeRef, IBNAWorld innerWorld, ObjRef simRef){
		ObjRef[] interfaceRefs = AS.xarch.getAll(brickRef, "interface");
		ObjRef[] signatureRefs = new ObjRef[interfaceRefs.length];
		for(int i = 0; i < interfaceRefs.length; i++){
			signatureRefs[i] = XadlUtils.resolveXLink(AS.xarch, interfaceRefs[i], "signature");
		}
		if(brickTypeRef != null){
			IBNAModel bnaModel = bnaWorld.getBNAModel();
			String xArchID = XadlUtils.getID(AS.xarch, simRef);
			if(xArchID == null){
				return;
			}

			ObjRef outerSignatureRef = XadlUtils.resolveXLink(AS.xarch, simRef, "outerSignature");
			if(outerSignatureRef == null || !AS.xarch.isInstanceOf(outerSignatureRef, "types#Signature")){
				return;
			}
			String outerSignatureID = XadlUtils.getID(AS.xarch, outerSignatureRef);
			if(outerSignatureID == null){
				return;
			}

			ObjRef innerInterfaceRef = XadlUtils.resolveXLink(AS.xarch, simRef, "innerInterface");
			if(innerInterfaceRef == null || !AS.xarch.isInstanceOf(innerInterfaceRef, "types#Interface")){
				return;
			}
			String innerInterfaceID = XadlUtils.getID(AS.xarch, innerInterfaceRef);
			if(innerInterfaceID == null){
				return;
			}

			ObjRef outerInterfaceRef = null;
			for(int i = 0; i < signatureRefs.length; i++){
				if(outerSignatureRef != null && signatureRefs[i] != null && AS.xarch.isEqual(outerSignatureRef, signatureRefs[i])){
					outerInterfaceRef = interfaceRefs[i];
					break;
				}
			}
			String outerInterfaceXArchID = XadlUtils.getID(AS.xarch, outerInterfaceRef);

			if(outerInterfaceRef != null && outerInterfaceXArchID != null){
				MappingAssembly iimAssembly = null;
				IThing iimAssemblyRootThing = null;
				IThing[] iimAssemblyRootThings = ArchipelagoUtils.findAllThings(bnaModel, xArchID);
				for(IThing element: iimAssemblyRootThings){
					String iimAssemblyRootThingBrickXArchID = (String)element.getProperty("interfaceXArchID");
					if(iimAssemblyRootThingBrickXArchID != null && iimAssemblyRootThingBrickXArchID.equals(outerInterfaceXArchID)){
						iimAssemblyRootThing = element;
					}
				}

				if(iimAssemblyRootThing == null || !(iimAssemblyRootThing instanceof IHasAssemblyData)){
					iimAssembly = new MappingAssembly(bnaModel, brickAssembly.getMappingRootThing(), ASSEMBLY_TYPE_IIM);
					iimAssembly.getRootThing().setProperty("interfaceXArchID", outerInterfaceXArchID);

					iimAssembly.getMappingThing().setColor(new RGB(0, 0, 0));
					iimAssembly.getMappingThing().setLineWidth(1);
					iimAssembly.getMappingThing().setLineStyle(IHasLineStyle.LINE_STYLE_DASH);
				}
				else{
					iimAssembly = AssemblyUtils.getAssemblyWithRoot(iimAssemblyRootThing);
				}
				String description = XadlUtils.getDescription(AS.xarch, simRef);
				if(description == null){
					description = "[No Description]";
				}

				ToolTipLogic.setToolTip(iimAssembly.getMappingGlassThing(), description);

				IThing outerInterfaceAssemblyRootThing = ArchipelagoUtils.findThing(bnaModel, outerInterfaceXArchID);
				if(outerInterfaceAssemblyRootThing != null && outerInterfaceAssemblyRootThing instanceof IHasAssemblyData){
					EndpointAssembly outerInterfaceAssembly = AssemblyUtils.getAssemblyWithRoot(outerInterfaceAssemblyRootThing);
					if(outerInterfaceAssembly.getEndpointGlassThing() != null){
						MaintainStickyPointLogic.stickPoint(outerInterfaceAssembly.getEndpointGlassThing(), IHasAnchorPoint.ANCHOR_POINT_PROPERTY_NAME, StickyMode.CENTER, iimAssembly.getMappingGlassThing());
					}
				}
				if(brickAssembly.getWorldThing() != null){
					iimAssembly.getMappingGlassThing().setInternalEndpointWorldThingID(brickAssembly.getWorldThing().getID());
				}
				IBNAModel innerModel = innerWorld.getBNAModel();
				IThing interfaceAssemblyRootThing = ArchipelagoUtils.findThing(innerModel, innerInterfaceID);
				if(interfaceAssemblyRootThing != null && interfaceAssemblyRootThing instanceof IHasAssemblyData){
					EndpointAssembly interfaceAssembly = AssemblyUtils.getAssemblyWithRoot(interfaceAssemblyRootThing);
					if(interfaceAssembly.getEndpointGlassThing() != null){
						iimAssembly.getMappingGlassThing().setInternalEndpointStuckToThingID(interfaceAssembly.getEndpointGlassThing().getID());
					}
				}
				iimAssembly.getRootThing().setProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME, xArchID);
				AS.eventBus.fireArchipelagoEvent(new StructureEvents.InterfaceInterfaceMappingUpdatedEvent(bnaWorld, brickAssembly, brickRef, brickTypeRef, innerWorld, simRef, iimAssembly));
			}
		}
	}

	public synchronized static void removeOrphanedInterfaces(ArchipelagoServices AS, IBNAWorld bnaWorld, BoxAssembly brickAssembly, ObjRef brickRef){
		IBNAModel bnaModel = bnaWorld.getBNAModel();
		String brickXArchID = XadlUtils.getID(AS.xarch, brickRef);
		if(brickXArchID == null){
			return;
		}

		IThing[] childThings = bnaModel.getChildThings(brickAssembly.getEndpointRootThing());

		for(IThing t: childThings){
			if(t instanceof IHasAssemblyData){
				String xArchID = t.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
				if(xArchID != null){
					ObjRef[] interfaceRefs = AS.xarch.getAll(brickRef, "interface");

					boolean found = false;
					for(ObjRef refToMatch: interfaceRefs){
						String idToMatch = XadlUtils.getID(AS.xarch, refToMatch);
						if(idToMatch != null && idToMatch.equals(xArchID)){
							found = true;
							break;
						}
					}
					if(!found){
						bnaModel.removeThingAndChildren(t);
					}
				}
			}
		}
	}

	public synchronized static void updateInterface(ArchipelagoServices AS, IBNAWorld bnaWorld, BoxAssembly brickAssembly, ObjRef interfaceRef){
		IBNAModel bnaModel = bnaWorld.getBNAModel();
		String xArchID = XadlUtils.getID(AS.xarch, interfaceRef);
		if(xArchID == null){
			return;
		}

		EndpointAssembly endpointAssembly = null;
		IThing endpointAssemblyRootThing = ArchipelagoUtils.findThing(bnaModel, xArchID);
		if(endpointAssemblyRootThing == null || !(endpointAssemblyRootThing instanceof IHasAssemblyData)){
			endpointAssembly = new EndpointAssembly(bnaModel, brickAssembly.getEndpointRootThing(), ASSEMBLY_TYPE_INTERFACE);
			UserEditableUtils.addEditableQuality(endpointAssembly.getEndpointGlassThing(), IRelativeMovable.USER_MAY_MOVE);
			UserEditableUtils.addEditableQuality(endpointAssembly.getDirectionalLabelThing(), MaintainTagsLogic.USER_MAY_SHOW_TAG);
			UserEditableUtils.addEditableQuality(endpointAssembly.getBoxThing(), IHasMutableColor.USER_MAY_EDIT_COLOR);
			endpointAssembly.getEndpointGlassThing().setAnchorPoint(new Point(brickAssembly.getBoxGlassThing().getBoundingBox().x, brickAssembly.getBoxGlassThing().getBoundingBox().y));

			endpointAssembly.getBoxThing().setColor(new RGB(0xff, 0xff, 0xff));

			MaintainStickyPointLogic.stickPoint(brickAssembly.getBoxGlassThing(), IHasAnchorPoint.ANCHOR_POINT_PROPERTY_NAME, StickyMode.EDGE, endpointAssembly.getEndpointGlassThing());
		}
		else{
			endpointAssembly = AssemblyUtils.getAssemblyWithRoot(endpointAssemblyRootThing);
		}
		String description = XadlUtils.getDescription(AS.xarch, interfaceRef);
		if(description == null){
			description = "[No Description]";
		}

		ToolTipLogic.setToolTip(endpointAssembly.getEndpointGlassThing(), description);
		endpointAssembly.getDirectionalLabelThing().setProperty(IHasText.TEXT_PROPERTY_NAME, description);

		String direction = XadlUtils.getDirection(AS.xarch, interfaceRef);
		Flow flow = Flow.NONE;
		if(direction != null){
			if(direction.equals("in")){
				flow = Flow.IN;
			}
			else if(direction.equals("out")){
				flow = Flow.OUT;
			}
			else if(direction.equals("inout")){
				flow = Flow.INOUT;
			}
		}
		endpointAssembly.getDirectionalLabelThing().setFlow(flow);
		endpointAssembly.getRootThing().setProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME, xArchID);
		AS.eventBus.fireArchipelagoEvent(new StructureEvents.InterfaceUpdatedEvent(bnaWorld, brickAssembly, interfaceRef, endpointAssembly));
	}

	public static boolean isComponentAssemblyRootThing(IThing t){
		if(t instanceof AssemblyRootThing){
			return ASSEMBLY_TYPE_COMPONENT.equals(((AssemblyRootThing)t).getAssemblyKind());
		}
		return false;
	}

	public static boolean isConnectorAssemblyRootThing(IThing t){
		if(t instanceof AssemblyRootThing){
			return ASSEMBLY_TYPE_CONNECTOR.equals(((AssemblyRootThing)t).getAssemblyKind());
		}
		return false;
	}

	public static boolean isBrickAssemblyRootThing(IThing t){
		if(t instanceof AssemblyRootThing){
			return ASSEMBLY_TYPE_COMPONENT.equals(((AssemblyRootThing)t).getAssemblyKind()) || ASSEMBLY_TYPE_CONNECTOR.equals(((AssemblyRootThing)t).getAssemblyKind());
		}
		return false;
	}

	public static boolean isInterfaceAssemblyRootThing(IThing t){
		if(t instanceof AssemblyRootThing){
			return ASSEMBLY_TYPE_INTERFACE.equals(((AssemblyRootThing)t).getAssemblyKind());
		}
		return false;
	}

	public static boolean isLinkAssemblyRootThing(IThing t){
		if(t instanceof AssemblyRootThing){
			return ASSEMBLY_TYPE_LINK.equals(((AssemblyRootThing)t).getAssemblyKind());
		}
		return false;
	}

	public static RGB getDefaultComponentColor(IPreferenceStore prefs){
		if(prefs.contains(ArchipelagoTypesConstants.PREF_DEFAULT_COMPONENT_COLOR)){
			return PreferenceConverter.getColor(prefs, ArchipelagoTypesConstants.PREF_DEFAULT_COMPONENT_COLOR);
		}
		return ArchipelagoTypesConstants.DEFAULT_COMPONENT_RGB;
	}

	public static RGB getDefaultConnectorColor(IPreferenceStore prefs){
		if(prefs.contains(ArchipelagoTypesConstants.PREF_DEFAULT_CONNECTOR_COLOR)){
			return PreferenceConverter.getColor(prefs, ArchipelagoTypesConstants.PREF_DEFAULT_CONNECTOR_COLOR);
		}
		return ArchipelagoTypesConstants.DEFAULT_CONNECTOR_RGB;
	}

	// Functions to set up the BNA model with basic root things for the hierarchy
	public static final String BNA_BRICK_ROOT_THING_ID = "$BRICKROOT";

	public static final String BNA_LINK_ROOT_THING_ID = "$LINKROOT";

	protected static void initBNAModel(IBNAModel m){
		IThing normalRootThing = ArchipelagoUtils.getNormalRootThing(m);

		if(m.getThing(BNA_LINK_ROOT_THING_ID) == null){
			IThing linkRootThing = new NoThing(BNA_LINK_ROOT_THING_ID);
			m.addThing(linkRootThing);
		}

		if(m.getThing(BNA_BRICK_ROOT_THING_ID) == null){
			IThing brickRootThing = new NoThing(BNA_BRICK_ROOT_THING_ID);
			m.addThing(brickRootThing);
		}
	}

	protected static IThing getRootThing(IBNAModel m, String id){
		IThing rootThing = m.getThing(id);
		if(rootThing == null){
			initBNAModel(m);
			return m.getThing(id);
		}
		return rootThing;
	}

	public static IThing getBrickRootThing(IBNAModel m){
		return getRootThing(m, BNA_BRICK_ROOT_THING_ID);
	}

	public static IThing getLinkRootThing(IBNAModel m){
		return getRootThing(m, BNA_LINK_ROOT_THING_ID);
	}

	public static BoxAssembly findBrickAssembly(ArchipelagoServices AS, IBNAModel m, ObjRef brickRef){
		String xArchID = XadlUtils.getID(AS.xarch, brickRef);
		if(xArchID != null){
			IThing t = ArchipelagoUtils.findThing(m, xArchID);
			if(t != null && isBrickAssemblyRootThing(t)){
				BoxAssembly assembly = AssemblyUtils.getAssemblyWithRoot(t);
				return assembly;
			}
		}
		return null;
	}

	public static EndpointAssembly findInterfaceAssembly(ArchipelagoServices AS, IBNAModel m, ObjRef interfaceRef){
		String xArchID = XadlUtils.getID(AS.xarch, interfaceRef);
		if(xArchID != null){
			IThing t = ArchipelagoUtils.findThing(m, xArchID);
			if(t != null && isInterfaceAssemblyRootThing(t)){
				EndpointAssembly assembly = AssemblyUtils.getAssemblyWithRoot(t);
				return assembly;
			}
		}
		return null;
	}

	public static SplineAssembly findLinkAssembly(ArchipelagoServices AS, IBNAModel m, ObjRef linkRef){
		String xArchID = XadlUtils.getID(AS.xarch, linkRef);
		if(xArchID != null){
			IThing t = ArchipelagoUtils.findThing(m, xArchID);
			if(t != null && isLinkAssemblyRootThing(t)){
				SplineAssembly assembly = AssemblyUtils.getAssemblyWithRoot(t);
				return assembly;
			}
		}
		return null;
	}

}
