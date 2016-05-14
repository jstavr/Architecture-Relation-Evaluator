package edu.uci.isr.archstudio4.comp.xarchcs.changesetidview;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.ChangeSetADTImpl;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;

public class WalkChangeSetTreeExample{

	public static void walkTheChangeSetTree(XArchFlatQueryInterface xarch, ObjRef xArchRef){

		System.err.println("Walking trees in file: " + xarch.getXArchURI(xArchRef));

		// get the changesets context
		ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");

		// get the archchangesets element for the file
		// there should only be one of these per file
		ObjRef archChangeSetsRef = xarch.getElement(changesetsContextRef, "archChangeSets", xArchRef);
		if(archChangeSetsRef == null){
			System.err.println("This file does not use change sets");
			return;
		}
		System.err.println("This file does use change sets... continuing");

		// get the list of change sets for the file
		// note that these will be in a random order, not the order of the change set viewer -- but that's okay
		ObjRef[] changeSetRefs = xarch.getAll(archChangeSetsRef, "changeSet");
		changeSetRefs = ChangeSetUtils.resolveExternalChangeSetRefs(xarch, changeSetRefs);

		// process each change set
		for(ObjRef changeSetRef: changeSetRefs){
			System.err.println("Walking change set: " + XadlUtils.getDescription(xarch, changeSetRef));

			// get the root change segment of the change set, it always uses this name
			ObjRef xArchRefChangeSegmentRef = (ObjRef)xarch.get(changeSetRef, "xArchElement");

			// and it's always an ElementSegment type, so we can treat it as such
			parseElementSegment(xarch, xArchRefChangeSegmentRef);
		}
	}

	private static void parseElementSegment(XArchFlatQueryInterface xarch, ObjRef elementSegmentRef){
		System.err.println("Found Element Segment at: " + elementSegmentRef);

		// figure out what type of object this element segment describes
		String typeString = (String)xarch.get(elementSegmentRef, "type");

		/*
		 * Some types were incorrectly recorded as ":XArch" rather than
		 * "#XArch". This corrects the problem.
		 */
		if(typeString != null){
			typeString = typeString.replace(':', '#');
		}

		if(typeString == null || typeString.trim().length() == 0){
			System.err.println("This segment is a removal, nothing more to search");
			return;
		}

		// figure out what the object this describes looks like, the IXArchTypeMetadata describes object types
		IXArchTypeMetadata type = xarch.getTypeMetadata(typeString);
		System.err.println("This element segment describes an object of type: " + type);
		if(type.getProperty("Id") != null){
			System.err.println("... and it has an ID property :)");
		}

		// now, let's recursively iterate through each child segment, which may be any type: attribute, element, or elementMany
		ObjRef[] childSegmentRefs = xarch.getAll(elementSegmentRef, "changeSegment");
		for(ObjRef childSegmentRef: childSegmentRefs){
			if(xarch.isInstanceOf(childSegmentRef, "changesets#AttributeSegment")){
				// this segment describes an attribute child segment
				parseAttributeSegment(xarch, childSegmentRef);
			}
			else if(xarch.isInstanceOf(childSegmentRef, "changesets#ElementManySegment")){
				// this segment describes an element many segment -- i.e., only a property name that will contain many different children
				parseElementManySegment(xarch, childSegmentRef);
			}
			else if(xarch.isInstanceOf(childSegmentRef, "changesets#ElementSegment")){
				// this describes an element segment -- call myself recursively
				parseElementSegment(xarch, childSegmentRef);
			}
			else{
				throw new IllegalArgumentException("This shouldn't happen.");
			}
		}
	}

	private static void parseElementManySegment(XArchFlatQueryInterface xarch, ObjRef elementManySegmentRef){
		System.err.println("Found an element many segment at " + elementManySegmentRef + " for elements named " + ChangeSetADTImpl.decodeNameReference((String)xarch.get(elementManySegmentRef, "reference")));

		// now parse all the element segments that are stored under this name
		// we know that they are all element segments, because we store elements under this name
		for(ObjRef childChangeSegment: xarch.getAll(elementManySegmentRef, "changeSegment")){
			parseElementSegment(xarch, childChangeSegment);
		}
	}

	private static void parseAttributeSegment(XArchFlatQueryInterface xarch, ObjRef attributeSegmentRef){
		System.err.println("Found an attribute segment at " + attributeSegmentRef + " for an attribute named " + ChangeSetADTImpl.decodeNameReference((String)xarch.get(attributeSegmentRef, "reference")));
	}
}
