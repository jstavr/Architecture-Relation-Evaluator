package edu.uci.isr.archstudio4.comp.xarchcs.changesetadt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers.DoNotResolveElementTypeResolver;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers.ElementTypeResolver;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers.IDResolver;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers.MultiValueResolver;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers.NoResolver;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers.ValueResolver;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach.XArchDetachFilterImpl;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach.XArchDetachInterface;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;
import edu.uci.isr.xarchflat.XArchPath;

/**
 * A non-cached, non thread-safe, unintelligent, no bells or whistles implementation of {@link IChangeSetADT} that
 * assumes each change set consists of its own tree of changes.
 */
public class ChangeSetADTImpl implements IChangeSetADT, XArchFileListener, XArchFlatListener {
	static final String NAME_REFERENCE_PREFIX = "Name:";
	protected final XArchFlatInterface xarch;
	protected final XArchFlatQueryInterface xarchf;
	protected final XArchFlatQueryInterface xarchcs;
	protected final IObjRefResolver[] resolvers;

	public ChangeSetADTImpl(XArchFlatInterface xarch, XArchDetachInterface xarchd) {
		assert xarch != null;
		assert xarchd != null;

		this.xarch = xarch;
		this.xarchf = new XArchDetachFilterImpl(xarch, xarchd);
		this.xarchcs = new ChangeSetFlatQueryImpl(xarch);

		List<IObjRefResolver> resolvers = new ArrayList<IObjRefResolver>();
		resolvers.add(new DoNotResolveElementTypeResolver(xarch, "hints#RenderingHints"));
		resolvers.add(new DoNotResolveElementTypeResolver(xarch, "hints2#HintedElement"));
		resolvers.add(new DoNotResolveElementTypeResolver(xarch, "hints3#RenderingHints3"));
		resolvers.add(new IDResolver());
		resolvers.add(new ValueResolver(xarch, "instance#XMLLink", "href"));
		resolvers.add(new ValueResolver(xarch, "instance#Point", "anchorOnInterface/href"));
		resolvers.add(new ValueResolver(xarch, "hints#Hints", "hintedThing/href"));
		resolvers.add(new ValueResolver(xarch, "hints#PropertyHint", "name"));
		resolvers.add(new ValueResolver(xarch, "hints2#HintedElement", "element/href"));
		resolvers.add(new ValueResolver(xarch, "hints2#Hint", "name"));
		resolvers.add(new MultiValueResolver(xarch, "hints3#HintBundle", "maintainer", "version"));
		resolvers.add(new ValueResolver(xarch, "hints3#HintedElement", "target/href"));
		resolvers.add(new ValueResolver(xarch, "hints3#Property", "name"));
		resolvers.add(new ElementTypeResolver());
		resolvers.add(new NoResolver());

		this.resolvers = resolvers.toArray(new IObjRefResolver[resolvers.size()]);
	}

	static final boolean equalz(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	String encodeType(IXArchTypeMetadata type) {
		assert type != null;

		return type.getType();
	}

	IXArchTypeMetadata decodeType(String type) {
		assert type != null;

		return xarch.getTypeMetadata(type);
	}

	static boolean isNameReference(String reference) {
		assert reference != null;

		return reference.startsWith(ChangeSetADTImpl.NAME_REFERENCE_PREFIX);
	}

	static String capFirstLetter(String s) {
		if (s != null) {
			int l = s.length();

			if (l > 0) {
				char ch = s.charAt(0);
				char uch = Character.toUpperCase(ch);

				if (ch != uch) {
					char[] value = new char[l];
					value[0] = uch;
					s.getChars(1, l, value, 1);
					s = new String(value);
				}
			}
		}

		return s;
	}

	public final static String encodeNameReference(String name) {
		assert name != null;
		assert name.length() > 0;
		assert Character.isUpperCase(name.charAt(0));

		return ChangeSetADTImpl.NAME_REFERENCE_PREFIX + name;
	}

	public final static String decodeNameReference(String reference) {
		assert reference != null;
		assert ChangeSetADTImpl.isNameReference(reference);

		return reference.substring(ChangeSetADTImpl.NAME_REFERENCE_PREFIX.length());
	}

	IObjRefResolver getResolver(XArchFlatQueryInterface xarch, ObjRef objRef) {
		assert objRef != null;

		//assert xarch.isAttached(objRef);
		for (IObjRefResolver resolver : resolvers) {
			if (resolver.canResolve(xarch, objRef)) {
				return resolver;
			}
		}

		return null;
	}

	String encodeReference(XArchFlatQueryInterface xarch, ObjRef objRef) {
		assert objRef != null;

		//assert xarch.isAttached(objRef);
		//assert xarch.getTypeMetadata(xarch.getParent(objRef)).getProperty(getActualElementName(xarch, objRef)).getMaxOccurs() > 1;
		IObjRefResolver resolver = getResolver(xarch, objRef);

		if (resolver != null) {
			return resolver.getReference(xarch, objRef);
		}

		return null;
	}

	String updateReference(XArchFlatQueryInterface xarch, ObjRef objRef, String oldReference) {
		assert objRef != null;

		//assert xarch.isAttached(objRef);
		//assert xarch.getTypeMetadata(xarch.getParent(objRef)).getProperty(getActualElementName(xarch, objRef)).getMaxOccurs() > 1;
		IObjRefResolver resolver = getResolver(xarch, objRef);

		if (resolver != null) {
			if (resolver.isOldReference(xarch, objRef, oldReference)) {
				return resolver.getReference(xarch, objRef);
			}
		}

		return null;
	}

	public IChangeReference getElementReference(ObjRef xArchRef, ObjRef[] objRefAncestors, boolean excludeDetached) {

		assert objRefAncestors != null && objRefAncestors.length > 0;
		ObjRef objRef = objRefAncestors[0];

		assert xArchRef != null;
		assert objRef != null; // && xarch.isAttached(objRef);

		IChangeReference changeReference = new ChangeReference(null, "xArch");

		if (!ChangeSetADTImpl.equalz(objRef, xArchRef)) {
			for (int i = objRefAncestors.length - 2; changeReference != null && i >= 0; i--) {
				ObjRef ancestorParentRef = objRefAncestors[i+1];
				IXArchTypeMetadata ancestorParentType = xarch.getTypeMetadata(ancestorParentRef);

				String ancestorName = XArchChangeSetUtils.getActualElementName(xarch, objRefAncestors, i);
				IXArchPropertyMetadata ancestorChildType = ancestorParentType.getProperty(ancestorName);
				assert ancestorChildType != null : ancestorParentType + "." + ancestorName;

				switch (ancestorChildType.getMetadataType()) {
				case IXArchPropertyMetadata.ATTRIBUTE:
					throw new IllegalArgumentException(); // this shouldn't happen

				case IXArchPropertyMetadata.ELEMENT:
					changeReference = getElementReference(xArchRef, changeReference, ancestorName, excludeDetached);
					break;

				case IXArchPropertyMetadata.ELEMENT_MANY:
					changeReference = getElementManyReference(xArchRef, changeReference, ancestorName,
					        excludeDetached);
					changeReference = getElementReference(xArchRef, changeReference,
					        objRefAncestors[i], excludeDetached);

					break;
				}
			}
		}

		return changeReference;
	}

	public IChangeReference getAttributeReference(ObjRef xArchRef, IChangeReference elementReference,
	        String attributeName) {
		assert xArchRef != null;
		assert attributeName != null && attributeName.length() > 0 && Character.isUpperCase(attributeName.charAt(0));

		if (elementReference == null) {
			return null;
		}

		return new ChangeReference(elementReference, ChangeSetADTImpl.encodeNameReference(attributeName));
	}

	public IChangeReference getElementReference(ObjRef xArchRef, IChangeReference elementReference, String elementName,
	        boolean excludeDetached) {
		assert xArchRef != null;
		assert elementName != null && elementName.length() > 0 && Character.isUpperCase(elementName.charAt(0)) : elementName;

		if (elementReference == null) {
			return null;
		}

		return new ChangeReference(elementReference, ChangeSetADTImpl.encodeNameReference(elementName));
	}

	public IChangeReference getElementManyReference(ObjRef xArchRef, IChangeReference elementReference,
	        String elementManyName, boolean excludeDetached) {
		assert xArchRef != null;
		assert elementManyName != null && elementManyName.length() > 0
		        && Character.isUpperCase(elementManyName.charAt(0));

		if (elementReference == null) {
			return null;
		}

		return new ChangeReference(elementReference, ChangeSetADTImpl.encodeNameReference(elementManyName));
	}

	public IChangeReference getElementReference(ObjRef xArchRef, IChangeReference elementManyReference,
	        ObjRef childRef, boolean excludeDetached) {
		assert xArchRef != null;
		assert childRef != null && xarch.isAttached(childRef);

		if (elementManyReference == null) {
			return null;
		}

		String reference = encodeReference(excludeDetached ? xarchf : xarch, childRef);

		if (reference != null) {
			return new ChangeReference(elementManyReference, reference);
		}

		return null;
	}

	boolean assertMatches(ObjRef changeSegmentRef, IChangeReference changeReference) {
		String[] references = ((ChangeReference) changeReference).references;

		for (int i = references.length - 1; i >= 1; i--) {
			assert xarch.isInstanceOf(changeSegmentRef, "changesets#ChangeSegment") : changeSegmentRef;
			assert xarch.has(changeSegmentRef, "reference", references[i]) : "" + changeSegmentRef + ": "
			        + references[i] + " != " + xarch.get(changeSegmentRef, "reference");
			changeSegmentRef = xarch.getParent(changeSegmentRef);
		}

		return true;
	}

	public IChangeReference getParentReference(ObjRef xArchRef, IChangeReference elementReference) {
		if (elementReference == null) {
			return null;
		}

		return ((ChangeReference) elementReference).parent;
	}

	public ObjRef getAttributeSegmentRef(ObjRef xArchRef, ObjRef changeSetRef, IChangeReference attributeReference,
	        ObjRef[] createObjRefAncestors) {
		changeSetRef = ChangeSetUtils.resolveExternalChangeSetRef(xarch, changeSetRef);
		assert xArchRef != null;
		assert changeSetRef != null;
		assert xarch.isInstanceOf(changeSetRef, "changesets#ChangeSet");

		if (attributeReference != null) {
			ObjRef elementSegmentRef = getElementSegmentRef(xArchRef, changeSetRef, getParentReference(xArchRef,
			        attributeReference), createObjRefAncestors);

			if (elementSegmentRef != null) {
				String aReference = getElementReference(attributeReference);

				for (ObjRef childSegmentRef : xarch.getAll(elementSegmentRef, "changeSegment")) {
					if (xarch.has(childSegmentRef, "reference", aReference)) {
						return childSegmentRef;
					}
				}

				if (createObjRefAncestors != null) {
					ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
					ObjRef attributeSegmentRef = xarch.create(changesetsContextRef, "AttributeSegment");
					xarch.set(attributeSegmentRef, "reference", aReference);
					xarch.add(elementSegmentRef, "changeSegment", attributeSegmentRef);

					return attributeSegmentRef;
				}
			}
		}

		return null;
	}

	public ObjRef getElementSegmentRef(ObjRef xArchRef, ObjRef changeSetRef, IChangeReference elementReference,
	        ObjRef[] createUsingObjRefAncestors) {
		changeSetRef = ChangeSetUtils.resolveExternalChangeSetRef(xarch, changeSetRef);
		assert xArchRef != null;
		assert changeSetRef != null;
		assert xarch.isInstanceOf(changeSetRef, "changesets#ChangeSet");

		if (elementReference != null) {
			ObjRef changeSegmentRef = (ObjRef) xarch.get(changeSetRef, "xArchElement");

			if (changeSegmentRef == null && createUsingObjRefAncestors != null) {
				ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
				changeSegmentRef = xarch.create(changesetsContextRef, "ElementSegment");
				xarch.set(changeSegmentRef, "reference", "xArch");
				xarch.set(changeSegmentRef, "type", encodeType(xarch.getTypeMetadata("#XArch")));
				xarch.set(changeSetRef, "xArchElement", changeSegmentRef);
			}

			if (changeSegmentRef != null) {
				String[] references = ((ChangeReference) elementReference).references;
				int ancestorIndex = -1;
				int ancestorReferenceIndex = 1;
				REFERENCE: for (int i = 1; i < references.length; i++) {
					String reference = references[i];

					if (ChangeSetADTImpl.isNameReference(reference)) {
						ancestorIndex--;
						ancestorReferenceIndex = i;
					}

					for (ObjRef childSegmentRef : xarch.getAll(changeSegmentRef, "changeSegment")) {
						if (xarch.has(childSegmentRef, "reference", reference)) {
							changeSegmentRef = childSegmentRef;

							continue REFERENCE;
						}
					}

					if (createUsingObjRefAncestors == null) {
						return null;
					}

					ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
					ancestorIndex = createUsingObjRefAncestors.length + ancestorIndex;

					ObjRef ancestorParentRef = createUsingObjRefAncestors[ancestorIndex + 1];
					IXArchTypeMetadata ancestorParentType = xarch.getTypeMetadata(ancestorParentRef);

					while (ancestorIndex >= 0 && ancestorReferenceIndex < references.length) {
						ObjRef ancestorRef = createUsingObjRefAncestors[ancestorIndex--];
						IXArchTypeMetadata ancestorType = xarch.getTypeMetadata(ancestorRef);

						switch (ancestorParentType.getProperty(
						        ChangeSetADTImpl.decodeNameReference(references[ancestorReferenceIndex]))
						        .getMetadataType()) {
						case IXArchPropertyMetadata.ATTRIBUTE:
						default:
							throw new IllegalArgumentException(); // this shouldn't happen

						case IXArchPropertyMetadata.ELEMENT_MANY:

							if (ancestorReferenceIndex < i) {
								ancestorReferenceIndex++;
							}
							else {
								ObjRef elementManySegmentRef = xarch.create(changesetsContextRef, "ElementManySegment");
								xarch.set(elementManySegmentRef, "reference", references[ancestorReferenceIndex++]);
								xarch.add(changeSegmentRef, "changeSegment", elementManySegmentRef);
								changeSegmentRef = elementManySegmentRef;
							}

							// fall through
						case IXArchPropertyMetadata.ELEMENT:

							if (ancestorReferenceIndex < i) {
								ancestorReferenceIndex++;
							}
							else {
								ObjRef elementSegmentRef = xarch.create(changesetsContextRef, "ElementSegment");
								xarch.set(elementSegmentRef, "reference", references[ancestorReferenceIndex++]);
								xarch.set(elementSegmentRef, "type", encodeType(ancestorType));
								xarch.add(changeSegmentRef, "changeSegment", elementSegmentRef);
								changeSegmentRef = elementSegmentRef;
							}
						}

						ancestorParentRef = ancestorRef;
						ancestorParentType = ancestorType;
					}
					assert assertMatches(changeSegmentRef, elementReference);

					break;
				}

				return changeSegmentRef;
			}
		}

		return null;
	}

	public ObjRef getElementManySegmentRef(ObjRef xArchRef, ObjRef changeSetRef, IChangeReference elementManyReference,
	        ObjRef[] createUsingObjRefAncestors) {
		changeSetRef = ChangeSetUtils.resolveExternalChangeSetRef(xarch, changeSetRef);
		assert xArchRef != null;
		assert changeSetRef != null;
		assert xarch.isInstanceOf(changeSetRef, "changesets#ChangeSet");

		if (elementManyReference != null) {
			ObjRef elementSegmentRef = getElementSegmentRef(xArchRef, changeSetRef, getParentReference(xArchRef,
			        elementManyReference), createUsingObjRefAncestors);

			if (elementSegmentRef != null) {
				String emReference = getElementReference(elementManyReference);

				for (ObjRef childSegmentRef : xarch.getAll(elementSegmentRef, "changeSegment")) {
					if (xarch.has(childSegmentRef, "reference", emReference)) {
						return childSegmentRef;
					}
				}

				if (createUsingObjRefAncestors != null) {
					ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
					ObjRef attributeSegmentRef = xarch.create(changesetsContextRef, "ElementManySegment");
					xarch.set(attributeSegmentRef, "reference", emReference);
					xarch.add(elementSegmentRef, "changeSegment", attributeSegmentRef);

					return attributeSegmentRef;
				}
			}
		}

		return null;
	}

	@Deprecated
	public ObjRef getChangeSegmentRef(ObjRef xArchRef, ObjRef changeSetRef, IChangeReference segmentReference) {
		changeSetRef = ChangeSetUtils.resolveExternalChangeSetRef(xarch, changeSetRef);
		assert xArchRef != null;
		assert changeSetRef != null && xarch.isInstanceOf(changeSetRef, "changesets#ChangeSet");
		assert segmentReference != null;

		ObjRef changeSegmentRef = (ObjRef) xarch.get(changeSetRef, "xArchElement");

		if (changeSegmentRef != null) {
			String[] references = ((ChangeReference) segmentReference).references;
			REFERENCE: for (int i = 1; i < references.length; i++) {
				String reference = references[i];

				for (ObjRef childChangeSegmentRef : xarch.getAll(changeSegmentRef, "changeSegment")) {
					if (xarch.has(childChangeSegmentRef, "reference", reference)) {
						changeSegmentRef = childChangeSegmentRef;

						continue REFERENCE;
					}
				}

				return null;
			}
		}

		return changeSegmentRef;
	}

	@Deprecated
	public IChangeReference[] getChildReferences(ObjRef xArchRef, ObjRef[] changeSetRefs,
	        IChangeReference parentChangeReference) {
		changeSetRefs = ChangeSetUtils.resolveExternalChangeSetRefs(xarch, changeSetRefs);
		assert xArchRef != null;

		// assert elementManyReference != null;
		if (parentChangeReference == null) {
			return new IChangeReference[0];
		}

		Set<IChangeReference> changeReferences = new HashSet<IChangeReference>();

		for (ObjRef changeSetRef : changeSetRefs) {
			ObjRef changeSegmentRef = getChangeSegmentRef(xArchRef, changeSetRef, parentChangeReference);

			if (changeSegmentRef != null) {
				if (xarch.getTypeMetadata(changeSegmentRef).getProperty("changeSegment") != null) {
					for (ObjRef childSegmentRef : xarch.getAll(changeSegmentRef, "changeSegment")) {
						String reference = (String) xarch.get(childSegmentRef, "reference");
						changeReferences.add(new ChangeReference(parentChangeReference, reference));
					}
				}
			}
		}

		return changeReferences.toArray(new IChangeReference[changeReferences.size()]);
	}

	public boolean isElementSegmentResolvable(ObjRef xArchRef, ObjRef elementSegmentRef, boolean excludeDetached) {
		assert xArchRef != null;
		assert elementSegmentRef != null;
		assert xarch.isInstanceOf(elementSegmentRef, "changesets#ElementSegment");

		/*
		 * We must first check the type since the removal of an object is recorded as having a null type.
		 */
		String type = (String) xarch.get(elementSegmentRef, "type");

		/*
		 * Some types were incorrectly recorded as ":XArch" rather than "#XArch". This corrects the problem.
		 */
		if (type != null) {
			type = type.replace(':', '#');
		}

		if (type == null || type.length() == 0) {
			return false;
		}

		/*
		 * Name: references are always resolvable.
		 */
		String currentReference = (String) xarch.get(elementSegmentRef, "reference");

		if (currentReference != null && ChangeSetADTImpl.isNameReference(currentReference)) {
			return true;
		}

		String reference = encodeReference(xarchcs, elementSegmentRef);

		if (ChangeSetADTImpl.equalz(currentReference, reference)) {
			return true;
		}

		String updatedReference = updateReference(xarchcs, elementSegmentRef, currentReference);

		if (reference != null) {
			xarch.set(elementSegmentRef, "reference", updatedReference);
			return true;
		}

		return false;
	}

	// public boolean isElementResolvable(ObjRef xArchRef, ObjRef elementRef){
	// assert xArchRef != null;
	// assert elementRef != null;
	//
	// String reference = encodeReference(xarch, elementRef);
	// return reference != null;
	// }
	public void removeChildren(ObjRef xArchRef, ObjRef changeSetRef, IChangeReference elementReference) {
		changeSetRef = ChangeSetUtils.resolveExternalChangeSetRef(xarch, changeSetRef);
		assert xArchRef != null;
		assert changeSetRef != null;
		assert elementReference != null;

		ObjRef elementSegmentRef = getElementSegmentRef(xArchRef, changeSetRef, elementReference, null);

		if (elementSegmentRef != null) {
			assert xarch.isInstanceOf(elementSegmentRef, "changesets#ElementSegment");
			xarch.remove(elementSegmentRef, "changeSegment", xarch.getAll(elementSegmentRef, "changeSegment"));
		}
	}

	public IChangeReference getChangeSegmentReference(ObjRef xArchRef, ObjRef changeSegmentRef) {
		assert xArchRef != null;
		assert changeSegmentRef != null;
		assert xarch.isInstanceOf(changeSegmentRef, "changesets#ChangeSegment");
		assert xarch.isAttached(changeSegmentRef);

		ChangeReference changeReference = new ChangeReference(null, "xArch");
		ObjRef[] ancestors = xarch.getAllAncestors(changeSegmentRef);

		// -5 for to skip xArch/archChangeSets/changeSet/xArchElement
		for (int i = ancestors.length - 5; i >= 0; i--) {
			ObjRef segmentRef = ancestors[i];
			String reference = (String) xarch.get(segmentRef, "reference");

			if (xarch.isInstanceOf(segmentRef, "changesets#AttributeSegment")) {
			}
			else if (xarch.isInstanceOf(segmentRef, "changesets#ElementSegment")) {
			}
			else if (xarch.isInstanceOf(segmentRef, "changesets#ElementManySegment")) {
			}
			else {
				throw new IllegalArgumentException("Unrecognized change segment type: "
				        + xarch.getTypeMetadata(segmentRef));
			}

			changeReference = new ChangeReference(changeReference, reference);
		}

		return changeReference;
	}

	public String getElementReference(IChangeReference changeReference) {
		assert changeReference != null;

		String[] references = ((ChangeReference) changeReference).references;

		return references[references.length - 1];
	}

	public String getChangeReferencePath(IChangeReference changeReference) {
		assert changeReference != null;

		StringBuffer sb = new StringBuffer();

		for (String reference : ((ChangeReference) changeReference).references) {
			if (sb.length() > 0) {
				sb.append('/');
			}

			sb.append(reference);
		}

		return sb.toString();
	}

	public void handleXArchFileEvent(XArchFileEvent evt) {
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt) {
	}

	public ObjRef[] getChangeSegmentRefs(ObjRef xArchRef, ObjRef[] changeSetRefs, IChangeReference segmentReference) {
		changeSetRefs = ChangeSetUtils.resolveExternalChangeSetRefs(xarch, changeSetRefs);

		ObjRef[] changeSegmentRefs = new ObjRef[changeSetRefs.length];

		for (int i = 0; i < changeSetRefs.length; i++) {
			changeSegmentRefs[i] = getChangeSegmentRef(xArchRef, changeSetRefs[i], segmentReference);
		}

		return changeSegmentRefs;
	}

	public Map<IChangeReference, ObjRef[]> getChildChangeSegmentRefs(ObjRef archRef,
	        IChangeReference parentChangeReference, ObjRef[] parentChangeSegmentRefs) {
		Map<IChangeReference, ObjRef[]> changeSegmentRefsMap = new HashMap<IChangeReference, ObjRef[]>();

		if (parentChangeReference != null && parentChangeSegmentRefs != null) {
			for (int i = 0; i < parentChangeSegmentRefs.length; i++) {
				ObjRef parentChangeSegmentRef = parentChangeSegmentRefs[i];

				if (parentChangeSegmentRef != null
				        && xarch.getTypeMetadata(parentChangeSegmentRef).getProperty("changeSegment") != null) {
					for (ObjRef childSegmentRef : xarch.getAll(parentChangeSegmentRef, "changeSegment")) {
						IChangeReference changeReference = new ChangeReference(parentChangeReference, (String) xarch
						        .get(childSegmentRef, "reference"));
						ObjRef[] changeSegmentRefs = changeSegmentRefsMap.get(changeReference);

						if (changeSegmentRefs == null) {
							changeSegmentRefsMap.put(changeReference,
							        changeSegmentRefs = new ObjRef[parentChangeSegmentRefs.length]);
						}

						changeSegmentRefs[i] = childSegmentRef;
					}
				}
			}
		}

		return changeSegmentRefsMap;
	}

	public ObjRef[] getChildChangeSegmentRefs(ObjRef xArchRef, IChangeReference parentChangeReference,
	        ObjRef[] parentChangeSegmentRefs, IChangeReference childChangeReference) {
		String[] references = ((ChangeReference) childChangeReference).references;
		String reference = references[references.length - 1];
		ObjRef[] changeSegmentRefs = new ObjRef[parentChangeSegmentRefs.length];

		for (int i = 0; i < changeSegmentRefs.length; i++) {
			ObjRef parentChangeSegmentRef = parentChangeSegmentRefs[i];

			if (parentChangeSegmentRef != null
			        && xarch.getTypeMetadata(parentChangeSegmentRef).getProperty("changeSegment") != null) {
				for (ObjRef childChangeSegmentRef : xarch.getAll(parentChangeSegmentRef, "changeSegment")) {
					if (xarch.has(childChangeSegmentRef, "reference", reference)) {
						changeSegmentRefs[i] = childChangeSegmentRef;

						break;
					}
				}
			}
		}

		return changeSegmentRefs;
	}

	public Object[] getDeviation(ObjRef xArchRef, IChangeReference preReference, IChangeReference postReference, ObjRef[] objRefAncestors) {
		
		assert objRefAncestors != null && objRefAncestors.length > 0;
		ObjRef objRef = objRefAncestors[0];

		assert preReference != null || postReference != null;
		assert !ChangeSetADTImpl.equalz(preReference, postReference);

		ChangeReference pre = (ChangeReference) preReference;
		ChangeReference post = (ChangeReference) postReference;

		if (pre != null) {
			int index = 0;
			ObjRef o = objRefAncestors[index];
			ObjRef[] oa = XArchChangeSetUtils.remove(index, objRefAncestors);
			ObjRef p = objRefAncestors[index+1];
			ObjRef[] pa = XArchChangeSetUtils.remove(index+1, objRefAncestors);
			IChangeReference co = preReference;

			while (p != null && index < objRefAncestors.length) {
				IChangeReference cp = getElementReference(xArchRef, pa, false);

				if (pre.startsWith(cp)) {
					return new Object[] { co, getElementReference(xArchRef, oa, false), oa };
				}

				o = p;
				oa = pa;
				p = objRefAncestors[++index];
				pa = XArchChangeSetUtils.remove(index, objRefAncestors);
				co = cp;
			}
		}
		else if (post != null) {
			int index = 0;
			ObjRef o = objRefAncestors[index];
			ObjRef[] oa = XArchChangeSetUtils.remove(index, objRefAncestors);
			ObjRef p = objRefAncestors[index+1];
			ObjRef[] pa = XArchChangeSetUtils.remove(index+1, objRefAncestors);

			while (p != null && index < objRefAncestors.length) {
				String name = XArchChangeSetUtils.getActualElementName(xarch, oa, 0);

				if (xarch.getTypeMetadata(p).getProperty(name).getMaxOccurs() > 1) {
					return new Object[] { null, getElementReference(xArchRef, oa, false), oa };
				}

				index ++;
				o = p;
				oa = pa;
				p = objRefAncestors[++index];
				pa = XArchChangeSetUtils.remove(index, objRefAncestors);
			}
		}

		return null;
	}

	public IChangeReference getDetachedChildChangeReference(ObjRef xArchRef, IChangeReference parentReference,
	        ObjRef childSegmentRef) {
		return new ChangeReference(parentReference, (String) xarch.get(childSegmentRef, "reference"));
	}

	public void removeAssociatedChanges(ObjRef xArchRef, ObjRef[] changeSetRefs, ObjRef[] objRefAncestors) {
		changeSetRefs = ChangeSetUtils.resolveExternalChangeSetRefs(xarch, changeSetRefs);

		IChangeReference changeReference = getElementReference(xArchRef, objRefAncestors, false);

		for (ObjRef changeSetRef : changeSetRefs) {
			ObjRef elementSegmentRef = getElementSegmentRef(xArchRef, changeSetRef, changeReference, null);

			if (elementSegmentRef != null) {
				removeChildren(xArchRef, changeSetRef, changeReference);
				xarch.remove(xarch.getParent(elementSegmentRef), xarch.getElementName(elementSegmentRef),
				        elementSegmentRef);
			}
		}
	}

	static class ChangeReference implements IChangeReference {
		final ChangeReference parent;
		final String[] references;
		final int hashCode;

		public ChangeReference(IChangeReference parent, String reference) {
			this.parent = (ChangeReference) parent;

			if (parent == null) {
				this.references = new String[] { reference };
			}
			else {
				ChangeReference parent_ = (ChangeReference) parent;
				this.references = new String[parent_.references.length + 1];
				System.arraycopy(parent_.references, 0, references, 0, parent_.references.length);
				this.references[references.length - 1] = reference;
			}

			this.hashCode = Arrays.hashCode(references);
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < references.length; i++) {
				if (i > 0) {
					sb.append('/');
				}

				sb.append(references[i]);
			}

			return sb.toString();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		public boolean startsWith(IChangeReference o) {
			return toString().startsWith(o.toString());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}

			if (obj instanceof ChangeReference) {
				return Arrays.equals(references, ((ChangeReference) obj).references);
			}

			return false;
		}
	}
}
