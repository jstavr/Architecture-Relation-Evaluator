package edu.uci.isr.archstudio4.comp.xarchcs.changesetsync;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT.IChangeReference;
import edu.uci.isr.xarchflat.ObjRef;

public interface IChangeSetSync {

	public enum ChangeStatus {

		/**
		 * Indicates that the given objRef is explicitly removed by at least one
		 * of the specified explicit change sets.
		 */
		REMOVED,

		/**
		 * Indicates that the given objRef is explicitly added by one or more of
		 * the specified explicit change sets.
		 */
		ADDED,

		/**
		 * Indicates that the given objRef is modified in some way by at least
		 * one of the specified explicit change sets.
		 */
		MODIFIED,

		/**
		 * Indicates that the given objRef is not modified or referenced by any
		 * of the specified explicit change sets, and that it is additionally
		 * removed by one of the other applied change sets.
		 */
		DETACHED,

		/**
		 * Indicates that the given objRef is not modified or referenced by any
		 * of the specified explicit change sets, and that it is not removed by
		 * any of the other applied change sets.
		 */
		UNMODIFIED,

		/**
		 * When in overview mode, indicates that the particular element is added
		 * by a change set that is not currently applied.
		 */
		UNADDED
	}

	public interface IChangeSetSyncMonitor {

		public void setCanceled(boolean canceled);

		public boolean isCanceled();

		public void beginTask(int totalWork);

		public void worked(int work);

		public void done();
	}

	/**
	 * Synchronizes a single attribute value between the merged model and the
	 * xADL model.
	 * 
	 * @param monitor
	 *            An optional monitor which allows the operation to be monitored
	 *            or canceled.
	 * @param xArchRef
	 *            The xArchRef of the model containing the change sets.
	 * @param mXArchRef
	 *            The xArchRef of the model containing the merged document.
	 * @param changeSetRefs
	 *            The change sets from which the merged model is created. These
	 *            are normally the applied change sets.
	 * @param changeSetDiffs
	 *            When present, indicates which change sets should be used to
	 *            guide the synch process. Only elements affected by these
	 *            change sets will be synchronized. A value of <code>null</code>
	 *            indicates that all changes of all change sets need to be
	 *            synchronized.
	 * @param activeChangeSetRef
	 *            When present, indicates the change set which should be updated
	 *            based on differences between the merged model and the model
	 *            that would be produced from the changeSetRefs. A value of
	 *            <code>null</code> indicates that only the merged model should
	 *            be updated.
	 * @param preParentReference
	 *            The reference before the change that is to be synchronized if
	 *            available or, or <code>null</code> otherwise.
	 * @param objRef
	 *            The objRef with the attribute to synchronize.
	 * @param attributeName
	 *            The attribute name whose value should be synchronized.
	 */
	public void syncAttribute(IChangeSetSyncMonitor monitor, ObjRef xArchRef, ObjRef mXArchRef, ObjRef[] changeSetRefs, ObjRef[] changeSetDiffs,
	        ObjRef activeChangeSetRef, IChangeReference preParentReference, ObjRef[] objRefAncestors, String attributeName);

	/**
	 * Synchronizes a named, child element between the merged model and the xADL
	 * model.
	 * 
	 * @param monitor
	 *            An optional monitor which allows the operation to be monitored
	 *            or canceled.
	 * @param xArchRef
	 *            The xArchRef of the model containing the change sets.
	 * @param mXArchRef
	 *            The xArchRef of the model containing the merged document.
	 * @param changeSetRefs
	 *            The change sets from which the merged model is created. These
	 *            are normally the applied change sets.
	 * @param changeSetDiffs
	 *            When present, indicates which change sets should be used to
	 *            guide the synch process. Only elements affected by these
	 *            change sets will be synchronized. A value of <code>null</code>
	 *            indicates that all changes of all change sets need to be
	 *            synchronized.
	 * @param activeChangeSetRef
	 *            When present, indicates the change set which should be updated
	 *            based on differences between the merged model and the model
	 *            that would be produced from the changeSetRefs. A value of
	 *            <code>null</code> indicates that only the merged model should
	 *            be updated.
	 * @param preParentReference
	 *            The reference before the change that is to be synchronized if
	 *            available or, or <code>null</code> otherwise.
	 * @param parentObjRef
	 *            The parent objRef containing the element that should be
	 *            synchronized, not the element itself.
	 * @param elementName
	 *            The name of the child element that should be synchronized.
	 */
	public void syncElement(IChangeSetSyncMonitor monitor, ObjRef xArchRef, ObjRef mXArchRef, ObjRef[] changeSetRefs, ObjRef[] changeSetDiffs,
	        ObjRef activeChangeSetRef, IChangeReference preParentReference, ObjRef[] parentObjRefAncestors, String elementName);

	///**
	// * Synchronizes a specific child element between the merged model and the
	// * xADL model. This determines the element's name and calls the appropriate
	// * method to synchronize it.
	// * 
	// * @param monitor
	// *            An optional monitor which allows the operation to be monitored
	// *            or canceled.
	// * @param xArchRef
	// *            The xArchRef of the model containing the change sets.
	// * @param mXArchRef
	// *            The xArchRef of the model containing the merged document.
	// * @param changeSetRefs
	// *            The change sets from which the merged model is created. These
	// *            are normally the applied change sets.
	// * @param changeSetDiffs
	// *            When present, indicates which change sets should be used to
	// *            guide the synch process. Only elements affected by these
	// *            change sets will be synchronized. A value of <code>null</code>
	// *            indicates that all changes of all change sets need to be
	// *            synchronized.
	// * @param activeChangeSetRef
	// *            When present, indicates the change set which should be updated
	// *            based on differences between the merged model and the model
	// *            that would be produced from the changeSetRefs. A value of
	// *            <code>null</code> indicates that only the merged model
	// *            should be updated.
	// * @param objRef
	// *            The objRef that should be synchronized.
	// */
	//public void syncElement(IChangeSetSyncMonitor monitor, ObjRef xArchRef, ObjRef[] changeSetRefs, ObjRef[] changeSetDiffs, ObjRef activeChangeSetRef, ObjRef objRef);

	/**
	 * Synchronizes all named, child elements between the merged model and the
	 * xADL model.
	 * 
	 * @param monitor
	 *            An optional monitor which allows the operation to be monitored
	 *            or canceled.
	 * @param xArchRef
	 *            The xArchRef of the model containing the change sets.
	 * @param mXArchRef
	 *            The xArchRef of the model containing the merged document.
	 * @param changeSetRefs
	 *            The change sets from which the merged model is created. These
	 *            are normally the applied change sets.
	 * @param changeSetDiffs
	 *            When present, indicates which change sets should be used to
	 *            guide the synch process. Only elements affected by these
	 *            change sets will be synchronized. A value of <code>null</code>
	 *            indicates that all changes of all change sets need to be
	 *            synchronized.
	 * @param activeChangeSetRef
	 *            When present, indicates the change set which should be updated
	 *            based on differences between the merged model and the model
	 *            that would be produced from the changeSetRefs. A value of
	 *            <code>null</code> indicates that only the merged model should
	 *            be updated.
	 * @param parentObjRef
	 *            The parent objRef containing the elements that should be
	 *            synchronized, not the elements themselves.
	 * @param elementManyName
	 *            The name of the elements that should be synchronized.
	 */
	public void syncElementMany(IChangeSetSyncMonitor monitor, ObjRef xArchRef, ObjRef mXArchRef, ObjRef[] changeSetRefs, ObjRef[] changeSetDiffs,
	        ObjRef activeChangeSetRef, ObjRef[] parentObjRefAncestors, String elementManyName);

	/**
	 * Synchronizes a specific named, child element between the merged model and
	 * the xADL model. This is similar to
	 * {@link #syncElementMany(edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.IChangeSetSyncMonitor, ObjRef, ObjRef[], boolean[], ObjRef, ObjRef, String)}
	 * , except that it synchronizes only the specific child.
	 * 
	 * @param monitor
	 *            An optional monitor which allows the operation to be monitored
	 *            or canceled.
	 * @param xArchRef
	 *            The xArchRef of the model containing the change sets.
	 * @param mXArchRef
	 *            The xArchRef of the model containing the merged document.
	 * @param changeSetRefs
	 *            The change sets from which the merged model is created. These
	 *            are normally the applied change sets.
	 * @param changeSetDiffs
	 *            When present, indicates which change sets should be used to
	 *            guide the synch process. Only elements affected by these
	 *            change sets will be synchronized. A value of <code>null</code>
	 *            indicates that all changes of all change sets need to be
	 *            synchronized.
	 * @param activeChangeSetRef
	 *            When present, indicates the change set which should be updated
	 *            based on differences between the merged model and the model
	 *            that would be produced from the changeSetRefs. A value of
	 *            <code>null</code> indicates that only the merged model should
	 *            be updated.
	 * @param parentObjRef
	 *            The parent objRef containing the elements that should be
	 *            synchronized, not the elements themselves.
	 * @param elementManyName
	 *            The name of the elements that should be synchronized.
	 * @param childObjRef
	 *            The specific child element to synchronize.
	 */
	public void syncElementMany(IChangeSetSyncMonitor monitor, ObjRef xArchRef, ObjRef mXArchRef, ObjRef[] changeSetRefs, ObjRef[] changeSetDiffs,
	        ObjRef activeChangeSetRef, ObjRef[] parentObjRefAncestors, String elementManyName, ObjRef childObjRef);

	/**
	 * @param xArchRef
	 *            The xArchRef of the model containing the change sets and
	 *            merged document.
	 * @param changeSetRefs
	 *            The change sets of interest.
	 * @param changeReference
	 *            The reference within the change sets to check.
	 * @param changeSegmentRefs
	 *            The change segments for each change set at the given change
	 *            reference.
	 * @param considerRemovalOfParent
	 *            Additionally, determine if a change set removes one of the
	 *            parents. This is expensive.
	 * @param considerRemovalOfParent
	 *            Additionally, determine if another change set in the document
	 *            adds the element. This is expensive.
	 * @return The type of changes ({@link ChangeStatus#ADDED},
	 *         {@link ChangeStatus#MODIFIED}, {@link ChangeStatus#REMOVED}, or
	 *         {@link ChangeStatus#UNMODIFIED}) stored in each chagne set at
	 *         that reference.
	 */
	public ChangeStatus[] getChangeStatus(ObjRef xArchRef, ObjRef[] changeSetRefs, IChangeReference changeReference, ObjRef changeSegmentRefs[],
	        boolean considerRemovalOfParent);

	public ChangeStatus getChangeStatus(ObjRef xArchRef, ObjRef[] objRefAncestors, ObjRef[] changeSetRefs, boolean considerRemovalOfParent, boolean isOverview);

	public void moveChanges(ObjRef xArchRef, ObjRef[] sourceChangeSetRefs, ObjRef targetChangeSetRef, ObjRef targetObjRef);

	public boolean getOverviewMode(ObjRef xArchRef);

	public void setOverviewMode(ObjRef xArchRef, boolean overviewMode);
}
