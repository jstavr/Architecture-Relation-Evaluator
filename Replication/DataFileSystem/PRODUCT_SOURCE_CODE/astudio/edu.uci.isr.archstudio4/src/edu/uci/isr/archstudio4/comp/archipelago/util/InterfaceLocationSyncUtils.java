package edu.uci.isr.archstudio4.comp.archipelago.util;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.types.StructureMapper;
import edu.uci.isr.archstudio4.comp.archipelago.types.TypesEditorSupport;
import edu.uci.isr.archstudio4.comp.archipelago.types.TypesMapper;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.assemblies.EndpointAssembly;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class InterfaceLocationSyncUtils {
	private InterfaceLocationSyncUtils() {
	}

	public static void syncInterfaceLocations(ArchipelagoServices AS, ObjRef xArchRef, IBNAWorld world, IThing brickThing, boolean push) {
		SyncInfo si = getSyncInfo(AS, xArchRef, world, brickThing);
		if (si != null) {
			syncInterfaceLocations(si.brickAssembly, si.interfaceAssemblies, si.brickTypeAssembly, si.signatureAssemblies, push);
		}
	}

	public static SyncInfo getSyncInfo(ArchipelagoServices AS, ObjRef xArchRef, IBNAWorld world, IThing t) {
		if (t != null) {
			if (t instanceof BoxGlassThing) {
				t = world.getBNAModel().getParentThing(t);
			}
			if (t != null && StructureMapper.isBrickAssemblyRootThing(t)) {
				String brickXArchID = t.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
				if (brickXArchID != null) {
					final ObjRef brickRef = AS.xarch.getByID(xArchRef, brickXArchID);
					if (brickRef != null) {
						if (AS.xarch.isInstanceOf(brickRef, "types#Component") || AS.xarch.isInstanceOf(brickRef, "types#Connector")) {
							ObjRef[] interfaceRefs = AS.xarch.getAll(brickRef, "interface");
							if (interfaceRefs != null && interfaceRefs.length > 0) {
								try {
									ObjRef brickTypeRef = XadlUtils.resolveXLink(AS.xarch, brickRef, "type");
									if (brickTypeRef != null) {
										if (AS.xarch.isInstanceOf(brickTypeRef, "types#ComponentType")
										        || AS.xarch.isInstanceOf(brickTypeRef, "types#ConnectorType")) {
											IBNAWorld typeWorld = TypesEditorSupport.setupWorld(AS, xArchRef, brickTypeRef);
											if (typeWorld != null) {
												IBNAModel typeWorldModel = typeWorld.getBNAModel();
												ObjRef[] signatureRefs = new ObjRef[interfaceRefs.length];
												for (int i = 0; i < interfaceRefs.length; i++) {
													signatureRefs[i] = XadlUtils.resolveXLink(AS.xarch, interfaceRefs[i], "signature");
												}
												return getSyncInfo(AS, world.getBNAModel(), brickRef, interfaceRefs, typeWorldModel, brickTypeRef,
												        signatureRefs);
											}
										}
									}
								}
								catch (Exception e) {
									// if we cannot resolve the brick type, then do nothing
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	public static SyncInfo getSyncInfo(ArchipelagoServices AS, IBNAModel structureModel, ObjRef brickRef, ObjRef[] interfaceRefs, IBNAModel typeModel,
	        ObjRef brickTypeRef, ObjRef[] signatureRefs) {
		String brickXArchID = XadlUtils.getID(AS.xarch, brickRef);
		if (brickXArchID != null) {
			IThing brickThing = ArchipelagoUtils.findThing(structureModel, brickXArchID);
			if (brickThing != null && StructureMapper.isBrickAssemblyRootThing(brickThing)) {
				BoxAssembly brickAssembly = AssemblyUtils.getAssemblyWithRoot(brickThing);

				EndpointAssembly[] interfaceAssemblies = new EndpointAssembly[interfaceRefs.length];
				for (int i = 0; i < interfaceRefs.length; i++) {
					String interfaceXArchID = XadlUtils.getID(AS.xarch, interfaceRefs[i]);
					if (interfaceXArchID != null) {
						IThing interfaceThing = ArchipelagoUtils.findThing(structureModel, interfaceXArchID);
						if (interfaceThing != null && StructureMapper.isInterfaceAssemblyRootThing(interfaceThing)) {
							interfaceAssemblies[i] = AssemblyUtils.getAssemblyWithRoot(interfaceThing);
						}
					}
				}

				String brickTypeXArchID = XadlUtils.getID(AS.xarch, brickTypeRef);
				if (brickTypeXArchID != null) {
					IThing brickTypeThing = ArchipelagoUtils.findThing(typeModel, brickTypeXArchID);
					if (brickTypeThing != null && TypesMapper.isBrickTypeAssemblyRootThing(brickTypeThing)) {
						BoxAssembly brickTypeAssembly = AssemblyUtils.getAssemblyWithRoot(brickTypeThing);

						EndpointAssembly[] signatureAssemblies = new EndpointAssembly[signatureRefs.length];
						for (int i = 0; i < signatureRefs.length; i++) {
							if (signatureRefs[i] != null) {
								String signatureXArchID = XadlUtils.getID(AS.xarch, signatureRefs[i]);
								if (signatureXArchID != null) {
									IThing signatureThing = ArchipelagoUtils.findThing(typeModel, signatureXArchID);
									if (signatureThing != null && TypesMapper.isSignatureAssemblyRootThing(signatureThing)) {
										signatureAssemblies[i] = AssemblyUtils.getAssemblyWithRoot(signatureThing);
									}
								}
							}
						}
						return new SyncInfo(brickAssembly, interfaceAssemblies, brickTypeAssembly, signatureAssemblies);
					}
				}
			}
		}
		return null;
	}

	public static void syncInterfaceLocations(BoxAssembly brickAssembly, EndpointAssembly[] interfaceAssemblies, BoxAssembly brickTypeAssembly,
	        EndpointAssembly[] signatureAssemblies, boolean push) {
		if (interfaceAssemblies.length != signatureAssemblies.length) {
			throw new IllegalArgumentException("Interface assemblies.length != Signature Assemblies.length");
		}
		Rectangle brickBoundingBox = brickAssembly.getBoxGlassThing().getBoundingBox();
		Rectangle brickTypeBoundingBox = brickTypeAssembly.getBoxGlassThing().getBoundingBox();

		if (brickBoundingBox != null && brickTypeBoundingBox != null) {
			for (int i = 0; i < interfaceAssemblies.length; i++) {
				if (interfaceAssemblies[i] != null && signatureAssemblies[i] != null) {
					Rectangle fromBoundingBox = push ? brickBoundingBox : brickTypeBoundingBox;
					EndpointAssembly fromEndpointAssembly = push ? interfaceAssemblies[i] : signatureAssemblies[i];

					Rectangle toBoundingBox = push ? brickTypeBoundingBox : brickBoundingBox;
					EndpointAssembly toEndpointAssembly = push ? signatureAssemblies[i] : interfaceAssemblies[i];

					Point fromAP = fromEndpointAssembly.getEndpointGlassThing().getAnchorPoint();
					Point toAP = toEndpointAssembly.getEndpointGlassThing().getAnchorPoint();
					if (fromAP != null && toAP != null) {
						double xpct = (double) (fromAP.x - fromBoundingBox.x) / (double) fromBoundingBox.width;
						double ypct = (double) (fromAP.y - fromBoundingBox.y) / (double) fromBoundingBox.height;

						int xoffset = BNAUtils.round(toBoundingBox.width * xpct);
						int yoffset = BNAUtils.round(toBoundingBox.height * ypct);

						Point newToAP = new Point(toBoundingBox.x + xoffset, toBoundingBox.y + yoffset);

						toEndpointAssembly.getEndpointGlassThing().setAnchorPoint(newToAP);
					}
				}
			}
		}
	}

	public static class SyncInfo {
		public BoxAssembly brickAssembly;
		public EndpointAssembly[] interfaceAssemblies;
		public BoxAssembly brickTypeAssembly;
		public EndpointAssembly[] signatureAssemblies;

		public SyncInfo(BoxAssembly brickAssembly, EndpointAssembly[] interfaceAssemblies, BoxAssembly brickTypeAssembly, EndpointAssembly[] signatureAssemblies) {
			super();
			this.brickAssembly = brickAssembly;
			this.interfaceAssemblies = interfaceAssemblies;
			this.brickTypeAssembly = brickTypeAssembly;
			this.signatureAssemblies = signatureAssemblies;
		}
	}

}
