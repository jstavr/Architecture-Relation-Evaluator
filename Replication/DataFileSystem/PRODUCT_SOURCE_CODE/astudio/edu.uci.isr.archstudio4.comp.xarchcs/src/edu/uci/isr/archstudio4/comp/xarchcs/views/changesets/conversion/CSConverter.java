package edu.uci.isr.archstudio4.comp.xarchcs.views.changesets.conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.sysutils.SystemUtils;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;
import edu.uci.isr.xarchflat.XArchFlatUtils;
import edu.uci.isr.xarchflat.XArchMetadataUtils;

public class CSConverter {

	private static ObjRef newChangeSet(XArchChangeSetInterface xarch, ObjRef xArchRef, String description) {

		ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
		ObjRef archChangeSetsRef = xarch.getElement(changesetsContextRef, "ArchChangeSets", xArchRef);
		// TODO: add target directory for new change sets/relationships to schema

		ObjRef newChangeSetRef = xarch.create(changesetsContextRef, "ChangeSet");
		xarch.set(newChangeSetRef, "id", UIDGenerator.generateUID("ChangeSet"));
		XArchFlatUtils.setDescription(xarch, newChangeSetRef, "description", description);
		xarch.add(archChangeSetsRef, "changeSet", newChangeSetRef);

		List<ObjRef> appliedChangeSetRefs = new ArrayList<ObjRef>(Arrays.asList(xarch.getAppliedChangeSetRefs(xArchRef)));
		appliedChangeSetRefs.add(newChangeSetRef);
		xarch.setAppliedChangeSetRefs(xArchRef, appliedChangeSetRefs.toArray(new ObjRef[appliedChangeSetRefs.size()]), null);

		return newChangeSetRef;
	}

	private static ObjRef cloneTypeAndAttributes(XArchChangeSetInterface xarch, ObjRef destXArchRef, ObjRef srcRef) {
		IXArchTypeMetadata srcType = xarch.getTypeMetadata(srcRef);
		ObjRef destRef;
		if (xarch.getXArch(srcRef).equals(xarch.getParent(srcRef))) {
			destRef = xarch.createElement(xarch.createContext(destXArchRef, XArchMetadataUtils.getTypeContext(srcType.getType())), XArchMetadataUtils
			        .getTypeName(srcType.getType()));
		}
		else {
			destRef = xarch.create(xarch.createContext(destXArchRef, XArchMetadataUtils.getTypeContext(srcType.getType())), XArchMetadataUtils
			        .getTypeName(srcType.getType()));
		}

		for (IXArchPropertyMetadata property : srcType.getProperties()) {
			// copy the attributes first so that the element can be resolved within a change set
			switch (property.getMetadataType()) {

			case IXArchPropertyMetadata.ATTRIBUTE:
				String name = property.getName();
				String value = (String) xarch.get(srcRef, name);
				if (value != null) {
					xarch.set(destRef, name, value);
				}
				else if ("id".equals(name)) {
					xarch.set(destRef, name, UIDGenerator.generateUID());
				}
				break;

			case IXArchPropertyMetadata.ELEMENT:
				continue;

			case IXArchPropertyMetadata.ELEMENT_MANY:
				continue;
			}
		}

		return destRef;
	}

	final static String a = "([^\\(\\)]+?)";

	private static String simplifyGuardString(String s) {
		String os = s;
		Matcher m;

		// (A && A) --> A
		if ((m = Pattern.compile("\\(" + a + " \\&\\& \\1\\)").matcher(s)).find()) {
			s = m.group(1);
		}

		// ((A && B) && A) --> (A && B)
		// ((A && B) && B) --> (A && B)
		if ((m = Pattern.compile("\\(\\(" + a + " \\&\\& " + a + "\\) \\&\\& (?:\\1|\\2)\\)").matcher(s)).find()) {
			s = "(" + m.group(1) + " && " + m.group(2) + ")";
		}

		// ((A || B) && A) --> (A && B)
		// ((A || B) && B) --> (A && B)
		if ((m = Pattern.compile("\\(\\(" + a + " \\|\\| " + a + "\\) \\&\\& (?:\\1|\\2)\\)").matcher(s)).find()) {
			s = "(" + m.group(1) + " && " + m.group(2) + ")";
		}

		while (!os.equals(s)) {
			os = s;
			s = simplifyGuardString(s);
		}

		s = s.replace(" == \"true\"", "");

		return s;
	}

	private static ObjRef selectChangeSetForGuard(XArchChangeSetInterface xarch, ObjRef destXArchRef, Map<String, ObjRef> guardsToChangeSetRefs,
	        ObjRef currentCSRef, ObjRef srcRef) {
		ObjRef objRefWithGuard = null;
		if (xarch.isInstanceOf(srcRef, "types#SignatureInterfaceMapping")) {
			ObjRef newCSRef = selectChangeSetForGuard(xarch, destXArchRef, guardsToChangeSetRefs, currentCSRef, XadlUtils.resolveXLink(xarch, srcRef,
			        "innerInterface"));
			if (newCSRef.equals(guardsToChangeSetRefs.get(null))) {
				newCSRef = selectChangeSetForGuard(xarch, destXArchRef, guardsToChangeSetRefs, currentCSRef, XadlUtils.resolveXLink(xarch, srcRef,
				        "outerSignature"));
			}
			return newCSRef;
		}
		if (objRefWithGuard == null) {
			IXArchTypeMetadata srcType = xarch.getTypeMetadata(srcRef);
			IXArchPropertyMetadata guardProperty = srcType.getProperty("guard");
			if (guardProperty != null && xarch.isAssignable("options#Guard", guardProperty.getType())) {
				objRefWithGuard = srcRef;
			}
		}
		if (objRefWithGuard == null) {
			IXArchTypeMetadata srcType = xarch.getTypeMetadata(srcRef);
			IXArchPropertyMetadata optionalProperty = srcType.getProperty("optional");
			if (optionalProperty != null && xarch.isAssignable("options#Optional", optionalProperty.getType())) {
				ObjRef optionalRef = (ObjRef) xarch.get(srcRef, "optional");
				if (optionalRef != null) {
					objRefWithGuard = optionalRef;
				}
			}
		}
		if (objRefWithGuard == null) {
			ObjRef parentRef = xarch.getParent(srcRef);
			if (parentRef != null) {
				return selectChangeSetForGuard(xarch, destXArchRef, guardsToChangeSetRefs, currentCSRef, parentRef);
			}
		}

		if (objRefWithGuard != null) {
			String guardString = ComparableBooleanGuardConverter.booleanGuardToString(xarch, objRefWithGuard);
			if (guardString != null) {
				guardString = simplifyGuardString(guardString);
				ObjRef newCSRef = guardsToChangeSetRefs.get(guardString);
				if (newCSRef == null) {
					guardsToChangeSetRefs.put(guardString, newCSRef = newChangeSet(xarch, destXArchRef, guardString));
				}
				return newCSRef;
			}
		}
		return currentCSRef;
	}

	private static void copyToCS(XArchChangeSetInterface xarch, ObjRef plaXArchRef, ObjRef csXArchRef, Map<String, ObjRef> guardsToChangeSetRefs,
	        Map<ObjRef, ObjRef> variantTypeRefToDestVariantTypeRef, ObjRef toCSRef, ObjRef srcRef, ObjRef destRef) {

		IXArchTypeMetadata srcType = xarch.getTypeMetadata(srcRef);

		for (IXArchPropertyMetadata property : srcType.getProperties()) {
			switch (property.getMetadataType()) {
			case IXArchPropertyMetadata.ATTRIBUTE:
				// these were already copied by the parent in order to ensure that the element was resolvable before copying its children
				continue;

			case IXArchPropertyMetadata.ELEMENT: {
				ObjRef childSrcRef = (ObjRef) xarch.get(srcRef, property.getName());
				if (childSrcRef != null) {
					if (xarch.isInstanceOf(childSrcRef, "options#Optional")) {
						// we don't need to copy the guards themselves, they are used to choose the change set
						continue;
					}
					ObjRef childToCSRef = selectChangeSetForGuard(xarch, csXArchRef, guardsToChangeSetRefs, toCSRef, childSrcRef);
					xarch.setActiveChangeSetRef(csXArchRef, childToCSRef);
					ObjRef childDestRef = cloneTypeAndAttributes(xarch, csXArchRef, childSrcRef);
					xarch.set(destRef, property.getName(), childDestRef);
					copyToCS(xarch, plaXArchRef, csXArchRef, guardsToChangeSetRefs, variantTypeRefToDestVariantTypeRef, childToCSRef, childSrcRef, childDestRef);
				}
			}
				break;

			case IXArchPropertyMetadata.ELEMENT_MANY: {
				for (ObjRef childSrcRef : xarch.getAll(srcRef, property.getName())) {
					if (xarch.isInstanceOf(childSrcRef, "variants#Variant")) {
						// skip these, they are converted to instances in a subarchitecture
						continue;
					}
					ObjRef childToCSRef = selectChangeSetForGuard(xarch, csXArchRef, guardsToChangeSetRefs, toCSRef, childSrcRef);
					xarch.setActiveChangeSetRef(csXArchRef, childToCSRef);
					ObjRef childDestRef = cloneTypeAndAttributes(xarch, csXArchRef, childSrcRef);
					xarch.add(destRef, property.getName(), childDestRef);
					copyToCS(xarch, plaXArchRef, csXArchRef, guardsToChangeSetRefs, variantTypeRefToDestVariantTypeRef, childToCSRef, childSrcRef, childDestRef);
					if (xarch.isInstanceOf(childSrcRef, "variants#VariantComponentType") || xarch.isInstanceOf(childSrcRef, "variants#VariantConnectorType")) {
						variantTypeRefToDestVariantTypeRef.put(childSrcRef, childDestRef);
						// we'll create variant instances later
					}
					if (xarch.isInstanceOf(childSrcRef, "types#Interface")) {
						if (xarch.get(childSrcRef, "signature") == null) {
							ObjRef brickSrcRef = xarch.getParent(childSrcRef);
							ObjRef typeSrcRef = XadlUtils.resolveXLink(xarch, brickSrcRef, "type");
							if (typeSrcRef != null) {
								ObjRef signatureSrcRef = searchForMatch(xarch, childSrcRef, xarch.getAll(typeSrcRef, "signature"));
								if (signatureSrcRef != null) {
									XadlUtils.setXLink(xarch, childDestRef, "signature", XadlUtils.getID(xarch, signatureSrcRef));
								}
							}
						}
					}
				}
			}
				break;
			}
		}
	}

	private static void variantsToInstancesInCS(XArchChangeSetInterface xarch, ObjRef plaXArchRef, ObjRef csXArchRef,
	        Map<String, ObjRef> guardsToChangeSetRefs, Map<ObjRef, ObjRef> variantTypeRefToDestVariantTypeRef, ObjRef toCSRef, ObjRef plaXArchRef2,
	        ObjRef csXArchRef2) {
		for (Map.Entry<ObjRef, ObjRef> e : variantTypeRefToDestVariantTypeRef.entrySet()) {
			ObjRef variantsTypeRef = e.getKey();
			ObjRef destVariantTypeRef = e.getValue();
			String typeId = XadlUtils.getID(xarch, variantsTypeRef);
			String variantId = typeId + "_Variant";

			Set<ObjRef> variantCSRefs = new HashSet<ObjRef>();
			for (ObjRef variantRef : xarch.getAll(variantsTypeRef, "variant")) {
				ObjRef typesContextRef = xarch.createContext(csXArchRef, "types");

				// get/create archsubstructure
				xarch.setActiveChangeSetRef(csXArchRef, selectChangeSetForGuard(xarch, csXArchRef, guardsToChangeSetRefs, toCSRef, destVariantTypeRef));
				ObjRef archStructureRef = xarch.getByID(csXArchRef, variantId);
				if (archStructureRef == null) {
					archStructureRef = xarch.createElement(typesContextRef, "ArchStructure");
					xarch.set(archStructureRef, "id", variantId);
					xarch.add(csXArchRef, "Object", archStructureRef);
					XadlUtils
					        .setDescription(xarch, archStructureRef, XadlUtils.getDescription(xarch, variantsTypeRef, "description", "[Unknown]") + " Variant");
					ObjRef subArchitectureRef = xarch.create(typesContextRef, "SubArchitecture");
					xarch.set(destVariantTypeRef, "subArchitecture", subArchitectureRef);
					XadlUtils.setXLink(xarch, subArchitectureRef, "ArchStructure", archStructureRef);
				}
				ObjRef subArchitectureRef = (ObjRef) xarch.get(destVariantTypeRef, "subArchitecture");

				ObjRef childToCSRef = selectChangeSetForGuard(xarch, csXArchRef, guardsToChangeSetRefs, toCSRef, variantRef);
				xarch.setActiveChangeSetRef(csXArchRef, childToCSRef);
				variantCSRefs.add(childToCSRef);

				// populate substructure with entry
				String componentOrConnector = xarch.getTypeMetadata(destVariantTypeRef).getType().indexOf("omponent") >= 0 ? "Component" : "Connector";
				ObjRef instanceRef = xarch.create(typesContextRef, componentOrConnector);
				xarch.set(instanceRef, "id", UIDGenerator.generateUID());
				xarch.add(archStructureRef, componentOrConnector, instanceRef);
				copyValue(xarch, csXArchRef, instanceRef, "type", variantRef, "variantType");
				ObjRef typeRef = XadlUtils.resolveXLink(xarch, instanceRef, "type");
				//XadlUtils.setXLink(xarch, instanceRef, "type", typeRef);
				//copyValue(xarch, csXArchRef, instanceRef, "type", variantRef, "variantType");
				copyValue(xarch, csXArchRef, instanceRef, "description", typeRef);

				// create outer
				for (ObjRef signatureRef : xarch.getAll(typeRef, "signature")) {
					ObjRef interfaceRef = xarch.create(typesContextRef, "Interface");
					xarch.set(interfaceRef, "id", UIDGenerator.generateUID());
					xarch.add(instanceRef, "interface", interfaceRef);
					copyValue(xarch, csXArchRef, interfaceRef, "description", signatureRef);
					copyValue(xarch, csXArchRef, interfaceRef, "direction", signatureRef);
					XadlUtils.setXLink(xarch, interfaceRef, "signature", signatureRef);
					ObjRef outerSignatureRef = searchForMatch(xarch, signatureRef, xarch.getAll(destVariantTypeRef, "signature"));
					if (outerSignatureRef != null) {
						ObjRef sigIfaceMapping = xarch.create(typesContextRef, "SignatureInterfaceMapping");
						xarch.set(sigIfaceMapping, "id", UIDGenerator.generateUID());
						XadlUtils.setXLink(xarch, sigIfaceMapping, "innerInterface", interfaceRef);
						XadlUtils.setXLink(xarch, sigIfaceMapping, "outerSignature", outerSignatureRef);
						xarch.add(subArchitectureRef, "signatureInterfaceMapping", sigIfaceMapping);
					}
				}
			}
			if (variantCSRefs.size() > 1) {
				ObjRef changesetsContextRef = xarch.createContext(csXArchRef, "changesets");
				ObjRef relationshipRef = xarch.create(changesetsContextRef, "VariantRelationship");
				xarch.set(relationshipRef, "id", UIDGenerator.generateUID());
				xarch.set(relationshipRef, "atLeast", "0");
				xarch.set(relationshipRef, "atMost", "1");
				for (ObjRef variantCSRef : variantCSRefs) {
					addXLink(xarch, relationshipRef, "variantChangeSet", variantCSRef);
				}
				ObjRef archChangeSetsRef = xarch.getElement(changesetsContextRef, "ArchChangeSets", csXArchRef);
				xarch.add(archChangeSetsRef, "relationship", relationshipRef);
			}
		}
	}

	private static ObjRef searchForMatch(XArchFlatQueryInterface xarch, ObjRef ifaceSigRef, ObjRef[] signatureRefs) {
		String ifaceSigDesc = XadlUtils.getDescription(xarch, ifaceSigRef);
		if (ifaceSigDesc != null) {
			for (ObjRef signatureRef : signatureRefs) {
				String sigDesc = XadlUtils.getDescription(xarch, signatureRef);
				if (sigDesc != null) {
					if (ifaceSigDesc.endsWith(sigDesc) || sigDesc.endsWith(ifaceSigDesc)) {
						return signatureRef;
					}
				}
			}
		}
		ObjRef candidateRef = null;
		String ifaceSigDir = XadlUtils.getDescription(xarch, ifaceSigRef, "direction", null);
		if (ifaceSigDir != null) {
			for (ObjRef signatureRef : signatureRefs) {
				String sigDir = XadlUtils.getDescription(xarch, signatureRef, "direction", null);
				if (sigDir != null) {
					if (ifaceSigDir.equals(sigDir)) {
						if (candidateRef != null) {
							candidateRef = null;
							break;
						}
						candidateRef = signatureRef;
					}
				}
			}
		}
		return candidateRef;
	}

	private static void copyValue(XArchChangeSetInterface xarch, ObjRef toXArchRef, ObjRef toObjRef, String toName, ObjRef fromObjRef) {
		copyValue(xarch, toXArchRef, toObjRef, toName, fromObjRef, toName);
	}

	private static void copyValue(XArchChangeSetInterface xarch, ObjRef toXArchRef, ObjRef toObjRef, String toName, ObjRef fromObjRef, String fromName) {
		ObjRef fromValueRef = (ObjRef) xarch.get(fromObjRef, fromName);
		if (fromValueRef != null) {
			xarch.set(toObjRef, toName, cloneTypeAndAttributes(xarch, toXArchRef, fromValueRef));
		}
	}

	public static void convertPLAtoCS(final XArchChangeSetInterface xarch, ObjRef plaXArchRef, ObjRef csXArchRef) {
		xarch.enableChangeSets(csXArchRef, null);
		ObjRef changesetsContextRef = xarch.createContext(csXArchRef, "changesets");
		ObjRef archChangeSetsRef = xarch.getElement(changesetsContextRef, "ArchChangeSets", csXArchRef);
		ObjRef baselineChangeSetRef = ChangeSetUtils.resolveExternalChangeSetRef(xarch, xarch.getAll(archChangeSetsRef, "ChangeSet")[0]);

		Map<String, ObjRef> guardsToChangeSetRefs = new HashMap<String, ObjRef>();
		Map<ObjRef, ObjRef> variantTypeRefToDestVariantTypeRef = new HashMap<ObjRef, ObjRef>();
		guardsToChangeSetRefs.put(null, baselineChangeSetRef);

		copyToCS(xarch, plaXArchRef, csXArchRef, guardsToChangeSetRefs, variantTypeRefToDestVariantTypeRef, baselineChangeSetRef, plaXArchRef, csXArchRef);
		variantsToInstancesInCS(xarch, plaXArchRef, csXArchRef, guardsToChangeSetRefs, variantTypeRefToDestVariantTypeRef, baselineChangeSetRef, plaXArchRef,
		        csXArchRef);

		ObjRef[] appliedChangeSetRefs = xarch.getAppliedChangeSetRefs(csXArchRef);
		String[] appliedChangeSetIds = new String[appliedChangeSetRefs.length];

		for (int i = 0, length = appliedChangeSetRefs.length; i < length; i++)
			appliedChangeSetIds[i] = XadlUtils.getID(xarch, appliedChangeSetRefs[i]);

		Arrays.sort(appliedChangeSetIds, new Comparator<String>() {
			public int compare(String id1, String id2) {
				ObjRef or1 = xarch.getByID(id1);
				ObjRef or2 = xarch.getByID(id2);
				String d1 = XadlUtils.getDescription(xarch, or1);
				String d2 = XadlUtils.getDescription(xarch, or2);

				if (d1.startsWith("Baseline") && !d2.startsWith("Baseline")) {
					return -1;
				}

				if (!d1.startsWith("Baseline") && d2.startsWith("Baseline")) {
					return 1;
				}

				return d1.compareTo(d2);
			}

			@Override
			public boolean equals(Object obj) {
				return false;
			}
		});

		String sortedAppliedChangeSetids = SystemUtils.mergeStrings(appliedChangeSetIds, "", ", ", "");

		xarch.set(archChangeSetsRef, "changeSetOrder", sortedAppliedChangeSetids);
		xarch.set(archChangeSetsRef, "appliedChangeSets", sortedAppliedChangeSetids);
	}

	private static void addXLink(XArchFlatInterface xarch, ObjRef ref, String linkElementManyName, ObjRef targetRef) {
		ObjRef refXArchRef = xarch.getXArch(ref);
		ObjRef targetXArchRef = xarch.getXArch(targetRef);
		ObjRef instanceContextRef = xarch.createContext(refXArchRef, "instance");
		ObjRef linkRef = xarch.create(instanceContextRef, "XMLLink");
		String href;
		if (refXArchRef.equals(targetXArchRef)) {
			href = "#" + XadlUtils.getID(xarch, targetRef);
		}
		else {
			href = xarch.getXArchURI(targetXArchRef) + "#" + XadlUtils.getID(xarch, targetRef);
		}
		xarch.set(linkRef, "type", "simple");
		xarch.set(linkRef, "href", href);
		xarch.add(ref, linkElementManyName, linkRef);
	}

}
