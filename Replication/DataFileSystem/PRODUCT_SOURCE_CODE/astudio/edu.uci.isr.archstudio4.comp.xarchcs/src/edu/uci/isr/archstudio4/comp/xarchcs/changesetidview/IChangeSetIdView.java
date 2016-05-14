package edu.uci.isr.archstudio4.comp.xarchcs.changesetidview;

import edu.uci.isr.xarchflat.ObjRef;


/**
 * Interface for ChangeSetIdView.
 * Retrieves id based information for change set relationships.
 * 
 * @author Kari
 * 
 */
public interface IChangeSetIdView {
	
	/**
	 * Given and Id and an xArchRef, returns the change segment reference that creates the object with the given id.
	 * @param xArchRef  xArch reference
	 * @param id look for the change segment ref that contains this id
	 * @return the change segment ref that creates the object with the given id
	 */
	public ObjRef whoCreated (ObjRef xArchRef, String id);

	/**
	 * Given and Id and an xArchRef, returns an array of the change segment references that reference the object with the give id.
	 * @param xArchRef xArch reference
	 * @param id id look for the change segment ref that reference this id
	 * @return an array of change set segments that reference the object with 
	 * the given id
	 */
	public ObjRef[] whoReferences (ObjRef xArchRef, String id);
	
	/**
	 * Returns an array of all ids of objects that have been created by all changesets within the given xArchRef.
	 * @param xArchRef
	 * @return array of all ids of objects that have been created by all changesets within the given xArchRef
	 */
	public String[] getAllIds(ObjRef xArchRef);

}
