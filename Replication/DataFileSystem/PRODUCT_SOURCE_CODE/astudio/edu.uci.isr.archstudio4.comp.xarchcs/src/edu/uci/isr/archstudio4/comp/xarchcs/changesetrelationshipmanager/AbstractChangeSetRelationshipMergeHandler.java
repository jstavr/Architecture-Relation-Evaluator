package edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager.MergeKey.RelationshipType;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager.RelationshipElement.RelationshipElementType;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetutils.OrRelationshipBuilder;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public abstract class AbstractChangeSetRelationshipMergeHandler
	implements IChangeSetRelationshipMergeHandler{

	XArchFlatInterface xarch;

	Boolean debug = false;

	static final String RELATIONSHIP_PREFIX = "CSRel";

	/**
	 * Constructor
	 */
	public AbstractChangeSetRelationshipMergeHandler(XArchFlatInterface xarch){
		this.xarch = xarch;
	}

	public abstract MergeKey getMergeKey(ObjRef xArchRef, ObjRef relationship);

	/**
	 * This version of doMerge merges all change set links of a list of
	 * relationships. It assumes that all relationships are of the same type
	 * (AND vs OR) This general case can be overloaded for alternate merges.
	 */
	public ObjRef doMerge(ObjRef xArchRef, Collection<ObjRef> relationshipList){
		if(debug){
			System.out.println("Performing a merge...");
		}

		ObjRef mergedRelationship = null;
		if(!relationshipList.isEmpty()){
			// create a relationship element set from the combined xlinks of all given relationships
			// duplicate relationship elements (made up of relationship type and the changeset id) will be removed
			Set<RelationshipElement> elementSet = new HashSet<RelationshipElement>();
			
			// create a relationship rationale set from the combined rationale references of all given relationships
			Set<RationaleElement> rationaleSet = new HashSet<RationaleElement>();

			for(ObjRef csRelationship: relationshipList){

				ObjRef[] orChangesets = xarch.getAll(csRelationship, "orChangeSet");
				for(ObjRef orChangeSet: orChangesets){
					elementSet.add(buildRelationshipElement(RelationshipElementType.OR, orChangeSet));
				}

				ObjRef[] orNotChangesets = xarch.getAll(csRelationship, "orNotChangeSet");
				for(ObjRef orNotChangeSet: orNotChangesets){
					elementSet.add(buildRelationshipElement(RelationshipElementType.OR_NOT, orNotChangeSet));
				}

				ObjRef[] andChangeSets = xarch.getAll(csRelationship, "andChangeSet");
				for(ObjRef andChangeSet: andChangeSets){
					elementSet.add(buildRelationshipElement(RelationshipElementType.AND, andChangeSet));
				}

				ObjRef[] andNotChangeSets = xarch.getAll(csRelationship, "andNotChangeSet");
				for(ObjRef andNotChangeSet: andNotChangeSets){
					elementSet.add(buildRelationshipElement(RelationshipElementType.AND_NOT, andNotChangeSet));
				}

				ObjRef[] impliesChangeSets = xarch.getAll(csRelationship, "impliesChangeSet");
				for(ObjRef impliesChangeSet: impliesChangeSets){
					elementSet.add(buildRelationshipElement(RelationshipElementType.IMPLIES, impliesChangeSet));
				}

				ObjRef[] impliesNotChangeSets = xarch.getAll(csRelationship, "impliesNotChangeSet");
				for(ObjRef impliesNotChangeSet: impliesNotChangeSets){
					elementSet.add(buildRelationshipElement(RelationshipElementType.IMPLIES_NOT, impliesNotChangeSet));
				}
				
				ObjRef[] rationales = xarch.getAll(csRelationship, "rationale");
				for(ObjRef rationale: rationales){
					RationaleElement re = new RationaleElement(xarch, rationale);
					rationaleSet.add(re);
				}
			}

			// we are assuming all relationships are of the same type here, taking the type from the 1st relationship
			ObjRef firstRelationship = relationshipList.iterator().next();
			RelationshipType relType = getRelationshipType(firstRelationship);

			// construct a new merged relationship
			ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
			if(relType == RelationshipType.OR_TYPE){
				mergedRelationship = xarch.create(changesetsContextRef, "OrRelationship");
			}
			else if(relType == RelationshipType.AND_TYPE){
				mergedRelationship = xarch.create(changesetsContextRef, "AndRelationship");
			}
			else{
				return null;
			}

			String id = UIDGenerator.generateUID(RELATIONSHIP_PREFIX);
			xarch.set(mergedRelationship, "id", id);
			xarch.set(mergedRelationship, "generated", "true");

			// generate xml links for each relationship element link
			for(RelationshipElement relationshipElement: elementSet){
				ObjRef changeSetLink = createXMLLinkTo(changesetsContextRef, relationshipElement.changeSetId);
				if(relationshipElement.relElemType == RelationshipElementType.OR){
					xarch.add(mergedRelationship, "orChangeSet", changeSetLink);
				}
				else if(relationshipElement.relElemType == RelationshipElementType.OR_NOT){
					xarch.add(mergedRelationship, "orNotChangeSet", changeSetLink);
				}
				else if(relationshipElement.relElemType == RelationshipElementType.AND){
					xarch.add(mergedRelationship, "andChangeSet", changeSetLink);
				}
				else if(relationshipElement.relElemType == RelationshipElementType.AND_NOT){
					xarch.add(mergedRelationship, "andNotChangeSet", changeSetLink);
				}
				else if(relationshipElement.relElemType == RelationshipElementType.IMPLIES){
					xarch.add(mergedRelationship, "impliesChangeSet", changeSetLink);
				}
				else if(relationshipElement.relElemType == RelationshipElementType.IMPLIES_NOT){
					xarch.add(mergedRelationship, "impliesNotChangeSet", changeSetLink);
				}
			}
			
			// generate merged rationale
			for (RationaleElement rationaleElem: rationaleSet) {
				ObjRef rationale = OrRelationshipBuilder.buildRationale(xarch, changesetsContextRef, rationaleElem.source, rationaleElem.requires);
				xarch.add(mergedRelationship, "rationale", rationale);
			}
		}
		return mergedRelationship;
	}

	private ObjRef createXMLLinkTo(ObjRef contextRef, String id){
		ObjRef orChangeSetLink = xarch.create(contextRef, "XMLLink");
		xarch.set(orChangeSetLink, "type", "simple");
		xarch.set(orChangeSetLink, "href", "#" + id);
		return orChangeSetLink;
	}

	public RelationshipElement buildRelationshipElement(RelationshipElementType relElementType, ObjRef changeSetLink){
		String csId = (String)xarch.get(changeSetLink, "href");
		assert (csId != null && csId.charAt(0) == '#');
		csId = csId.substring(1);
		return new RelationshipElement(relElementType, csId);
	}

	public RelationshipType getRelationshipType(ObjRef relationship){
		// figure out what type of relationship this is
		if(xarch.isInstanceOf(relationship, "changesets#OrRelationship")){
			return RelationshipType.OR_TYPE;
		}
		else if(xarch.isInstanceOf(relationship, "changesets#AndRelationship")){
			return RelationshipType.AND_TYPE;
		}
		else{
			return null;
		}
	}

}
