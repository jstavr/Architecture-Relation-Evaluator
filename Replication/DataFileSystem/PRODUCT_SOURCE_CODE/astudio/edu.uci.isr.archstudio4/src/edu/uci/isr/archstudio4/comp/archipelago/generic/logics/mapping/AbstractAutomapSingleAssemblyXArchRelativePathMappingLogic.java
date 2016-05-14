package edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.coordinating.MaintainXadlLinksLogic;
import edu.uci.isr.bna4.BNAModelEvent;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.facets.IHasEndpoints;
import edu.uci.isr.bna4.logics.coordinating.MaintainStickyPointLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainStickyPointLogic.StickyMode;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchMetadataUtils;
import edu.uci.isr.xarchflat.XArchPath;

public abstract class AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic<T extends IAssembly> extends SingleAssemblyXadlMappingLogic<T> {

	public interface ISingleAssemblyMapping<T> {

		public void updateAssembly(IBNAModel model, ObjRef objRef, T assembly, XArchFlatEvent evt, XArchPath relativeSourceTargetPath);

		public void storeAssemblyData(IBNAModel model, ObjRef objRef, T assembly, BNAModelEvent evt, BNAPath relativeBNAPath);
	}

	public interface IValueTranslator {

		public Object toXadlValue(Object bnaValue);

		public Object toBNAValue(Object xadlValue);
	}

	private Set<ISingleAssemblyMapping<T>> allMappings = new HashSet<ISingleAssemblyMapping<T>>();
	private Map<String, Collection<ISingleAssemblyMapping<T>>> mapByTagsOnlyPrefix = new HashMap<String, Collection<ISingleAssemblyMapping<T>>>();
	private Map<BNAPath, Collection<ISingleAssemblyMapping<T>>> mapByBNAPath = new HashMap<BNAPath, Collection<ISingleAssemblyMapping<T>>>();

	public AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath,
	        ThingPropertyTrackingLogic tptl, Class<T> assemblyClass, IThing parentThing, Object kind) {
		super(xarch, rootObjRef, relativePath, tptl, assemblyClass, parentThing, kind);
	}

	@Override
	protected final void updateAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs, T assembly, XArchFlatEvent evt,
	        XArchPath relativeSourceTargetPath) {
		if (relativeSourceTargetPath == null) {
			for (ISingleAssemblyMapping<T> mapping : allMappings) {
				mapping.updateAssembly(model, objRef, assembly, evt, relativeSourceTargetPath);
			}
		}
		else {
			String tagsOnlyString = relativeSourceTargetPath.toTagsOnlyString();
			for (Map.Entry<String, Collection<ISingleAssemblyMapping<T>>> entry : mapByTagsOnlyPrefix.entrySet()) {
				if (tagsOnlyString.startsWith(entry.getKey())) {
					for (ISingleAssemblyMapping<T> mapping : entry.getValue()) {
						mapping.updateAssembly(model, objRef, assembly, evt, relativeSourceTargetPath);
					}
				}
			}
		}
	}

	@Override
	protected final void storeAssemblyData(IBNAModel model, ObjRef objRef, T assembly, BNAModelEvent evt, BNAPath relativeBNAPath) {
		Collection<ISingleAssemblyMapping<T>> mappings = mapByBNAPath.get(relativeBNAPath);
		if (mappings != null) {
			for (ISingleAssemblyMapping<T> mapping : mappings) {
				mapping.storeAssemblyData(model, objRef, assembly, evt, relativeBNAPath);
			}
		}
	}

	protected void addTagsOnlyPrefixMapping(String tagsOnlyPrefix, ISingleAssemblyMapping<T> mapping) {
		allMappings.add(mapping);
		Collection<ISingleAssemblyMapping<T>> mappings = mapByTagsOnlyPrefix.get(tagsOnlyPrefix);
		if (mappings == null) {
			mapByTagsOnlyPrefix.put(tagsOnlyPrefix, mappings = new ArrayList<ISingleAssemblyMapping<T>>());
		}
		mappings.add(mapping);
	}

	protected void addBNAPathMapping(BNAPath bnaPath, ISingleAssemblyMapping<T> mapping) {
		allMappings.add(mapping);
		Collection<ISingleAssemblyMapping<T>> mappings = mapByBNAPath.get(bnaPath);
		if (mappings == null) {
			mapByBNAPath.put(bnaPath, mappings = new ArrayList<ISingleAssemblyMapping<T>>());
		}
		mappings.add(mapping);
	}

	protected void automapSimpleValue(String elementName, Object defaultBNAValue, String partsPath, String propertyName, boolean reverse) {
		automapSimpleValue(elementName, "instance#Description", defaultBNAValue, partsPath, propertyName, reverse, null);
	}

	protected void automapSimpleValue(final String elementName, final String elementType, final Object defaultBNAValue, final String partsPath,
	        final String propertyName, final boolean reverse, final IValueTranslator valueTranslator) {
		final String[] parts = partsPath.split("\\/");
		ISingleAssemblyMapping<T> mapping = new ISingleAssemblyMapping<T>() {

			public void updateAssembly(IBNAModel model, ObjRef objRef, T assembly, XArchFlatEvent evt, XArchPath relativeSourceTargetPath) {
				IThing thing = AssemblyUtils.getThing(assembly, parts);
				if (thing != null) {
					Object value = null;
					ObjRef valueRef = (ObjRef) xarch.get(objRef, elementName);
					if (valueRef != null) {
						value = xarch.get(valueRef, "Value");
					}
					if (valueTranslator != null) {
						value = valueTranslator.toBNAValue(value);
					}
					thing.setProperty(propertyName, value == null ? defaultBNAValue : value);
				}
			}

			public void storeAssemblyData(IBNAModel model, ObjRef objRef, T assembly, BNAModelEvent evt, BNAPath relativeBNAPath) {
				if (reverse) {
					Object value = evt.getThingEvent().getNewPropertyValue();
					if (valueTranslator != null) {
						value = valueTranslator.toXadlValue(value);
					}
					if (value == null) {
						ObjRef valueRef = (ObjRef) xarch.get(objRef, elementName);
						if (valueRef != null) {
							if (xarch.get(valueRef, "Value") != null) { // FIXME: xarch flat shouldn't send out an event if it's already cleared -- but it does
								xarch.clear(valueRef, "Value");
							}
						}
					}
					else {
						ObjRef valueRef = (ObjRef) xarch.get(objRef, elementName);
						if (valueRef == null) {
							ObjRef contextRef = xarch.createContext(xArchRef, XArchMetadataUtils.getTypeContext(elementType));
							valueRef = xarch.create(contextRef, XArchMetadataUtils.getTypeName(elementType));
							xarch.set(valueRef, "Value", (String) value);
							xarch.set(objRef, elementName, valueRef);
						}
						else {
							xarch.set(valueRef, "Value", (String) value);
						}
					}
				}
			}
		};
		addTagsOnlyPrefixMapping(elementName, mapping);
		addBNAPathMapping(new BNAPath(parts, propertyName), mapping);
	}

	protected void automapXLinkToStuckPoint(final String elementName, final String partsPath, final String pointPropertyName, final boolean reverse) {
		final String[] parts = partsPath.split("\\/");
		ISingleAssemblyMapping<T> mapping = new ISingleAssemblyMapping<T>() {

			public void updateAssembly(IBNAModel model, ObjRef objRef, T assembly, XArchFlatEvent evt, XArchPath relativeSourceTargetPath) {
				IThing thing = AssemblyUtils.getThing(assembly, parts);
				if (thing != null) {
					if (relativeSourceTargetPath == null) {
						thing.setProperty(MaintainStickyPointLogic.getStickyModeName(pointPropertyName), StickyMode.EDGE_FROM_CENTER);
					}
					MaintainXadlLinksLogic.updateThingIDByXArchID(xarch, (ObjRef) xarch.get(objRef, elementName), MaintainStickyPointLogic
					        .getReferenceName(pointPropertyName), thing);
				}
			}

			public void storeAssemblyData(IBNAModel model, ObjRef objRef, T assembly, BNAModelEvent evt, BNAPath relativeBNAPath) {
				if (reverse) {
					IThing thing = AssemblyUtils.getThing(assembly, parts);
					if (thing != null) {
						setLinkEndpoint(model, MaintainStickyPointLogic.getStuckToThingId(pointPropertyName, thing), objRef, elementName);
					}
				}
			}

			void setLinkEndpoint(IBNAModel model, String targetThingID, ObjRef objRef, String linkName) {
				String xArchID = null;
				if (targetThingID != null) {
					IAssembly assembly = AssemblyUtils.getAssemblyWithPart(model.getThing(targetThingID));
					if (assembly != null) {
						xArchID = assembly.getRootThing().getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
					}
				}
				if (xArchID != null) {
					XadlUtils.setXLink(xarch, objRef, linkName, xArchID);
				}
				else {
					if (xarch.get(objRef, linkName) != null) { // FIXME: xarch flat shouldn't send out an event if it's already cleared -- but it does
						xarch.clear(objRef, linkName);
					}
				}
			}
		};
		addTagsOnlyPrefixMapping(elementName, mapping);
		addBNAPathMapping(new BNAPath(parts, MaintainStickyPointLogic.getReferenceName(pointPropertyName)), mapping);
	}

	protected void automapLinkPointsToSplineEndpoints(final String partsPath, final boolean reverse) {
		final String[] parts = partsPath.split("\\/");
		ISingleAssemblyMapping<T> updateAssemblyEndpoints = new ISingleAssemblyMapping<T>() {

			public void updateAssembly(IBNAModel model, ObjRef objRef, T assembly, XArchFlatEvent evt, XArchPath relativeSourceTargetPath) {
				// this updates both points
				IThing thing = AssemblyUtils.getThing(assembly, parts);
				if (thing != null) {

					ObjRef[] pointRefs = xarch.getAll(objRef, "point");
					if (pointRefs.length >= 1 && (relativeSourceTargetPath == null || relativeSourceTargetPath.getTagIndex(0) == 0)) {
						String targetId = getPointTargetId(pointRefs[0]);
						if (relativeSourceTargetPath == null) {
							thing.setProperty(MaintainStickyPointLogic.getStickyModeName(IHasEndpoints.ENDPOINT_1_PROPERTY_NAME), StickyMode.EDGE_FROM_CENTER);
						}
						MaintainXadlLinksLogic.updateThingIDByXArchID(targetId, MaintainStickyPointLogic
						        .getReferenceName(IHasEndpoints.ENDPOINT_1_PROPERTY_NAME), thing);
					}
					if (pointRefs.length >= 2 && (relativeSourceTargetPath == null || relativeSourceTargetPath.getTagIndex(0) == 1)) {
						String targetId = getPointTargetId(pointRefs[1]);
						if (relativeSourceTargetPath == null) {
							thing.setProperty(MaintainStickyPointLogic.getStickyModeName(IHasEndpoints.ENDPOINT_2_PROPERTY_NAME), StickyMode.EDGE_FROM_CENTER);
						}
						MaintainXadlLinksLogic.updateThingIDByXArchID(targetId, MaintainStickyPointLogic
						        .getReferenceName(IHasEndpoints.ENDPOINT_2_PROPERTY_NAME), thing);
					}
				}
			}

			public void storeAssemblyData(IBNAModel model, ObjRef objRef, T assembly, BNAModelEvent evt, BNAPath relativeBNAPath) {
			}

			String getPointTargetId(ObjRef pointRef) {
				ObjRef linkRef = (ObjRef) xarch.get(pointRef, "anchorOnInterface");
				if (linkRef != null) {
					String href = (String) xarch.get(linkRef, "Href");
					if (href != null) {
						int poundIndex = href.indexOf('#');
						if (poundIndex >= 0) {
							return href.substring(poundIndex + 1);
						}
					}
				}
				return null;
			}
		};

		addTagsOnlyPrefixMapping("point", updateAssemblyEndpoints);

		if (reverse) {
			ISingleAssemblyMapping<T> updateXadlPoints = new ISingleAssemblyMapping<T>() {

				public void updateAssembly(IBNAModel model, ObjRef objRef, T assembly, XArchFlatEvent evt, XArchPath relativeSourceTargetPath) {
				}

				public void storeAssemblyData(IBNAModel model, ObjRef objRef, T assembly, BNAModelEvent evt, BNAPath relativeBNAPath) {
					if (reverse) {
						IThing thing = AssemblyUtils.getThing(assembly, parts);
						if (thing != null) {
							ObjRef xArchRef = xarch.getXArch(objRef);
							List<ObjRef> pointRefs = new ArrayList<ObjRef>(Arrays.asList(xarch.getAll(objRef, "point")));
							ObjRef point1Ref = pointRefs.size() > 0 ? pointRefs.get(0) : null;
							if (evt == null
							        || point1Ref == null
							        || evt.getThingEvent().getPropertyName().equals(
							                MaintainStickyPointLogic.getReferenceName(IHasEndpoints.ENDPOINT_1_PROPERTY_NAME))) {
								point1Ref = setPointTargetId(model, MaintainStickyPointLogic.getStuckToThingId(IHasEndpoints.ENDPOINT_1_PROPERTY_NAME, thing),
								        xArchRef, point1Ref, "anchorOnInterface");
							}

							ObjRef point2Ref = pointRefs.size() > 1 ? pointRefs.get(1) : null;
							if (evt == null
							        || point2Ref == null
							        || evt.getThingEvent().getPropertyName().equals(
							                MaintainStickyPointLogic.getReferenceName(IHasEndpoints.ENDPOINT_2_PROPERTY_NAME))) {
								point2Ref = setPointTargetId(model, MaintainStickyPointLogic.getStuckToThingId(IHasEndpoints.ENDPOINT_2_PROPERTY_NAME, thing),
								        xArchRef, point2Ref, "anchorOnInterface");
							}
							Set<ObjRef> pointRefsToRemove = new HashSet<ObjRef>(pointRefs);
							if (!pointRefsToRemove.contains(point1Ref))
								xarch.add(objRef, "point", point1Ref);
							if (!pointRefsToRemove.contains(point2Ref))
								xarch.add(objRef, "point", point2Ref);
							pointRefsToRemove.remove(point1Ref);
							pointRefsToRemove.remove(point2Ref);
							if (pointRefsToRemove.size() > 0)
								xarch.remove(objRef, "point", pointRefsToRemove.toArray(new ObjRef[pointRefsToRemove.size()]));
						}
					}
				}

				ObjRef setPointTargetId(IBNAModel model, String targetThingID, ObjRef xArchRef, ObjRef objRef, String linkName) {
					String xArchID = null;
					if (targetThingID != null) {
						IAssembly assembly = AssemblyUtils.getAssemblyWithPart(model.getThing(targetThingID));
						if (assembly != null) {
							xArchID = assembly.getRootThing().getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
						}
					}
					if (objRef == null) {
						ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
						objRef = xarch.create(typesContextRef, "Point");
					}
					if (xArchID != null) {
						XadlUtils.setXLink(xarch, objRef, linkName, xArchID);
					}
					else {
						if (xarch.get(objRef, linkName) != null) { // FIXME: xarch flat shouldn't send out an event if it's already cleared -- but it does
							xarch.clear(objRef, linkName);
						}
					}
					return objRef;
				}
			};

			addBNAPathMapping(new BNAPath(parts, MaintainStickyPointLogic.getReferenceName(IHasEndpoints.ENDPOINT_1_PROPERTY_NAME)), updateXadlPoints);
			addBNAPathMapping(new BNAPath(parts, MaintainStickyPointLogic.getReferenceName(IHasEndpoints.ENDPOINT_2_PROPERTY_NAME)), updateXadlPoints);
		}
	}
}
